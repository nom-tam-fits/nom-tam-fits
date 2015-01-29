package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.util.BufferedDataOutputStream;

/**
 * Do Rice compression. Integer data in any local region rarely use the full
 * dynamic range available for the data type. Rice compression looks at the
 * differences between groups of integers and strips out leading 0's from the
 * differences. Given a sequence of integers we: (** for steps where we actually
 * write something)
 * <ol>
 * <li>** Write out the value of the first integer
 * <li>Replace the sequence with the sequence of differences.
 * <li>Replace positive differences, n, with 2*n and negative differences with
 * -2*n-1. So 0 -> 0, -1 -> 1, 1 -> 2, -2 -> 3, 2 -> 4,... This means that
 * differences will have the same number of bits (pretty much) for a given
 * absolute value.
 * <li>Loop over the sequence in blocks of 16 or 32 integers. There may be a
 * shorter terminal block.
 * <li>For each block:
 * <ol>
 * <li>Calculate the sum of the values and get the average of this sum. Find the
 * number of bits it takes to represent this value, e.g., if the average is
 * 110.4 then we would need 7 bits to represent this (2^7 = 128 which is >
 * 110.4). Call this number F.
 * <li>** Write out (F+1) (using a number of bits appropriate for the data
 * size). We use F+1 rather than F to cater to the first special case below.
 * <li>For each value in the block
 * <ol>
 * <li>Break up the number into two parts: the top,T, any bits more significant
 * than the F low order bits, and the bottom, B, the lowest F bits in the value.
 * To get the top we can divide by 2^F or left shift F bits. To get the bottom
 * we can do a modulo operation or a bitwise AND with the mask 2^F - 1.
 * <p>
 * The top will often be 0 and will always be less than or equal to the number
 * of values in the block. [In the worst case only one difference in the block
 * in non-zero, then the average difference is the just that difference over the
 * block size.]. Typically it will be 0, 1, or 2.
 * <li>** Write out T 0 bits followed by a single 1 bit.
 * <li>** Write out the B using just F bits.
 * <li>In a typical case we will write about B+2 bits for each value. Say we
 * have 16 bit pixels with values fluctuating typically by 30 units between
 * adjacent pixels. Then we will write ~8 bits per pixel for a savings of >50%
 * in storage. Overheads include writing the first value at the beginning of the
 * file and 4 bits (for shorts) per block giving the value of F for each block.
 * If we use 32 pixel blocks, a block of uncompressed data will be 512 bits
 * while the compressed block would be ~240 bits.
 * </ol>
 * </ol>
 * </ol>
 * There are two special cases for each block.
 * <ul>
 * <li>If all the differences are 0, then instead of writing F+1 we just write 0
 * to mark the block as constant.
 * <li>If the differences between adjacent pixels are too high, i.e., there are
 * largely random values in the original data, then splitting into top and
 * bottom wastes a bit of space and time. This is indicated when F is greater
 * than some maximum value. When we see this we don't do the splitting we just
 * write the values directly to the output.
 * </ul>
 * 
 * @author tmcglynn
 */
public class Rice implements CompressionScheme {

    private int block;

    private int bitpix;

    private boolean initialized = false;

    private int fsbits;

    private int fsmax;

    private int bbits;

    public String name() {
        return "RICE_1";
    }

    public void initialize(Map<String, String> params) {
        // Rice compression expects a length and block size parameter
        try {

            System.err.println("Start1");
            block = Integer.parseInt(params.get("block"));
            System.err.println("Start2");
            System.err.println("keys:" + params.keySet());
            System.err.println("keys:" + params.values());
            String bp = params.get("bitpix");
            System.err.println("bp is:" + bp);
            bitpix = Integer.parseInt(bp);
            System.err.println("Start3");
            if (bitpix == 8) {
                fsbits = 3;
                fsmax = 6;
            } else if (bitpix == 16) {
                fsbits = 4;
                fsmax = 14;
            } else if (bitpix == 32) {
                fsbits = 5;
                fsmax = 25;
            } else {
                throw new IllegalArgumentException("Invalid bitpix for Rice compression");
            }
            bbits = 1 << fsbits;

        } catch (Exception e) {
            System.err.println("Required parameters not found for rice compression");
            e.printStackTrace(System.err);
            throw new RuntimeException("Invalid compression", e);
        }
        initialized = true;
    }

    int maxTop = 0;

    /**
     * Compress an input block. While the input is a byte array it may represent
     * integers of any length. This routine sets up input and output processing
     * streams.
     */
    public byte[] compress(byte[] in) throws IOException {
        if (!initialized) {
            throw new IllegalStateException("Rice compressor not initialized");
        }

        // The number of elements is just the number of bytes
        // over the number of bytes per element.
        int len = in.length / (bitpix / 8);

        DataInputStream ds = new DataInputStream(new ByteArrayInputStream(in));
        ByteArrayOutputStream out = new ByteArrayOutputStream(32768);
        OutputBitStream bo = new OutputBitStream(out);
        compressStream(ds, bo, len);
        bo.flush();
        bo.close();
        return out.toByteArray();
    }

    /**
     * This routine compresses a stream of input values into the output. It
     * handles the looping over all blocks.
     */
    public void compressStream(DataInputStream ds, OutputBitStream bo, int len) throws IOException {

        int offset = 0;
        int word = 0;
        if (offset < len) {
            word = getWord(ds);
            // Write out the first word. We'll be computing
            // differences starting from this value.
            bo.writeBits(word, bitpix);
        }

        boolean first = true;
        // Loop over individual compression blocks.
        while (offset < len) {
            int thisBlock = Math.min(block, len - offset);
            word = compressBlock(first, word, thisBlock, ds, bo);
            first = false;
            offset += thisBlock;
        }
        bo.close();
    }

    /**
     * This routine compresses a single block.
     * 
     * @param first
     *            Is this the first block in the compression stream?
     * @param lastWord
     *            What was the last word (not difference) read int from the
     *            input. If this is the first block it should be the first word
     *            in the file.
     * @param len
     *            The number of words in this block
     * @param ds
     *            The input stream from which we are reading data.
     * @param bo
     *            The output bit stream on which we are writing results.
     * @return The last word read (not the last difference).
     */
    private int compressBlock(boolean first, int lastWord, int len, DataInputStream ds, OutputBitStream bo) throws IOException {

        int[] diffs = new int[len];

        // For the first block we've already read the first word (and
        // written it out) so we treat it a bit specially.
        if (!first) {
            diffs[0] = getWord(ds);
        } else {
            diffs[0] = lastWord;
        }
        int newLast = diffs[diffs.length - 1];

        // Fill in the rest of the array.
        for (int i = 1; i < diffs.length; i += 1) {
            diffs[i] = getWord(ds);
        }

        // Convert the integer values to integer differences
        // and compute the sum of the differences.
        long sum = convertToDiffs(lastWord, diffs);

        // Get the number of noise bits.
        int fs = getFs(sum, len);

        if (sum == 0) {
            // A constant block
            specialCaseZero(bo);
        } else if (sum >= fsmax) {
            // To much variation to comprss
            specialCaseRandom(bo, diffs);
        } else {

            // The normal case.
            int mask = (1 << fs) - 1;
            // Write out the number of 'noise' bits.
            bo.writeBits(fs + 1, fsbits);
            for (int i = 0; i < len; i += 1) {
                // Now write out the encoded differences
                emit(diffs[i], fs, bo, mask);
            }
        }

        // Return the last input value which we will need when
        // computing the differences for the next block.
        return newLast;
    }

    /**
     * Emit the coding for a single compressed word.
     * 
     * @param val
     *            The input value to be emitted (normally a pixel difference).
     * @param fs
     *            The number of 'noise' bits for this block.
     * @param bo
     *            The output bit stream.
     * @param mask
     *            A mask getting the noise bits (= (1<<fs)-1).
     */
    private void emit(int val, int fs, OutputBitStream bo, int mask) throws IOException {
        int top = val >>> fs;
        int bot = val & mask;
        bo.writeBits(1, top + 1);
        bo.writeBits(bot, fs);
    }

    /**
     * Handle the case where all the pixels are the same
     */
    private void specialCaseZero(OutputBitStream bo) throws IOException {
        bo.writeBits(0, fsbits);
    }

    /**
     * Handle the case where the noise is so large that no compression is
     * possible
     */
    private void specialCaseRandom(OutputBitStream bo, int[] diffs) throws IOException {
        bo.writeBits(fsmax + 1, fsbits);
        for (int i = 0; i < diffs.length; i += 1) {
            bo.writeBits(diffs[i], bitpix);
        }
    }

    /**
     * Convert an array of integers into an array of differences.
     * 
     * @param lastWord
     *            The last word in the previous output. For the first call this
     *            should be the same as the first input element of diffs. For
     *            all later calls it should be the last input value of diffs in
     *            the previous call.
     * @param diffs
     *            On input this contains the original integer values. On output
     *            it contains the integer differences coded so that all values
     *            are positive. Positive values go to 2*val Negative values got
     *            to 2*|val|-1.
     * @return The sum of the returned elements in the array.
     */
    private long convertToDiffs(int lastWord, int[] diffs) {
        long sum = 0;
        for (int i = 0; i < diffs.length; i += 1) {
            int d = diffs[i] - lastWord;
            lastWord = diffs[i];
            if (d < 0) {
                d = -2 * d - 1;
            } else {
                d *= 2;
            }
            sum += d;
            diffs[i] = d;
        }
        return sum;
    }

    /**
     * Get the number of 'noise' bits in the block
     */
    private int getFs(long sum, int len) {
        // We made 1 -> 1. since in original sum was double,
        // so expression in parens should be double.

        // We want to compute the 'average' difference. Not
        // sure why we have the thisBlock/2 - 1 there, but
        // presumably that comes out in the details.
        double dpSum = (sum - len / 2 - 1.) / len;
        if (dpSum < 0) {
            dpSum = 0;
        }

        // How many bits does it take to represent the 'average' difference.
        int fs;
        int psum = ((int) dpSum >> 1);
        for (fs = 0; psum > 0; fs += 1) {
            psum >>= 1;
        }
        return fs;
    }

    /**
     * Read a full pixel from the input where the length of pixels has been set
     * by the user
     */
    private int getWord(DataInputStream is) throws IOException {
        if (bitpix == 8) {
            return is.readByte() & 0xFF;
        } else if (bitpix == 16) {
            return is.readShort() & 0xFFFF;
        } else {
            return is.readInt();
        }
    }

    /**
     * Decompress input data using the Rice algorithm.
     * 
     * @param in
     *            The compressed data.
     * @param len
     *            The number of pixels expected on the input.
     * @return A byte array representing the uncompressed data. This may need to
     *         be read through a ByteArrayInputStream to recover the appropriate
     *         pixel values (which may not be bytes).
     */
    public byte[] decompress(byte[] in, int len) throws IOException {

        if (!initialized) {
            throw new IllegalStateException("Rice compressor not initialized");
        }

        InputBitStream bin = new InputBitStream(new ByteArrayInputStream(in));
        ByteArrayOutputStream out = new ByteArrayOutputStream(32768);
        DataOutputStream ds = new DataOutputStream(out);

        decompressStream(bin, ds, len);
        bin.close();
        return out.toByteArray();
    }

    /** Decompress an input stream expecting a given number of words */

    public void decompressStream(InputBitStream bin, DataOutputStream ds, int len) throws IOException {

        int offset = 0;
        // Read the first pixel.
        int word = bin.readBits(bitpix);
        while (offset < len) {
            int thisBlock = block;
            if (offset + block > len) {
                thisBlock = len - offset;
            }
            decompressBlock(word, bin, ds, thisBlock);
            offset += thisBlock;
        }
    }

    /** Decompress a single block */

    private void decompressBlock(int word, InputBitStream bin, DataOutputStream ds, int len) throws IOException {

        int fs = bin.readBits(fsbits) - 1;

        if (fs < 0) {
            decompressConstant(word, ds, len);

        } else if (fs >= fsmax) {
            word = decompressRandom(word, bin, ds, len);

        } else {
            for (int i = 0; i < len; i += 1) {
                word = decodeWord(word, fs, bin);
                writeWord(ds, word);
            }
        }
    }

    /** Decompress a block of constant values */
    private void decompressConstant(int word, DataOutputStream ds, int len) throws IOException {
        for (int i = 0; i < len; i += 1) {
            writeWord(ds, word);
        }
    }

    /**
     * Write a block of 'random' values, i.e., where the noise is to large to be
     * usefully compressed.
     */
    private int decompressRandom(int word, InputBitStream bin, DataOutputStream ds, int len) throws IOException {
        for (int i = 0; i < len; i += 1) {
            int diff = bin.readBits(bbits);
            if (diff % 2 == 0) {
                diff >>= 1;
            } else {
                diff = ~(diff >> 1);
            }
            word += diff;
            writeWord(ds, word);
        }
        return word;
    }

    /** Reconstruct a single word. */
    private int decodeWord(int word, int fs, InputBitStream bin) throws IOException {
        int nbits = 0;
        int b = 0;
        // Find the number of 0 bits. That count is the
        // high order part of the difference.
        int high = bin.skipBits(false);
        // Skip the 1 separator bit
        bin.readBits(1);
        // Read the fs 'noise' bits.
        int low = bin.readBits(fs);
        // We don't worry about sign extension since fs is always < 32.
        int diff = (high << fs) | low;

        // Remember that this is not quite the difference, so
        // convert back to the actual difference and add to the previous
        // word.
        if (diff % 2 == 0) {
            word += diff / 2;
        } else {
            word -= (diff + 1) / 2;
        }
        return word;
    }

    /**
     * Write a full pixel value to the output decompression stream.
     */
    private void writeWord(DataOutputStream ds, int val) throws IOException {
        if (bitpix == 8) {
            ds.writeByte(val);
        } else if (bitpix == 16) {
            ds.writeShort(val);
        } else if (bitpix == 32) {
            ds.writeInt(val);
        }
    }

    public static void main(String[] args) throws Exception {
        int[] test = new int[100];
        for (int i = 0; i < test.length; i += 1) {
            if (i % 2 != 0) {
                test[i] = 1000 - 2 * i;
            } else {
                test[i] = 1000 + 2 * i;
            }
        }
        Rice comp = new Rice();
        Map<String, String> init = new HashMap<String, String>();
        init.put("bitpix", "32");
        init.put("block", "32");
        init.put("length", "100");
        comp.initialize(init);

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        BufferedDataOutputStream d = new BufferedDataOutputStream(bo);
        d.write(test);
        d.close();
        byte[] input = bo.toByteArray();
        byte[] result = comp.compress(input);
        System.out.println("Result len:" + result.length);
        for (int i = 0; i < result.length; i += 1) {
            System.out.printf("%d: %3d %2x\n", i, result[i], result[i]);
        }

        result = comp.decompress(result, 100);
        DataInputStream bi = new DataInputStream(new ByteArrayInputStream(result));
        for (int i = 0; i < 100; i += 1) {
            System.out.println(i + ": " + bi.readInt());
        }
    }

    public void updateForWrite(Header hdr, Map<String, String> parameters) throws FitsException {

        int bitpix = hdr.getIntValue("ZBITPIX", -1);

        int block;
        if (!parameters.containsKey("block")) {
            parameters.put("block", "" + 32);
            block = 32;
        } else {
            block = Integer.parseInt(parameters.get("block"));
        }

        hdr.addValue("ZNAME1", "BLOCKSIZE", "Compression region size");
        hdr.addValue("ZVAL1", block, "Compression region size");

        hdr.addValue("ZNAME2", "BYTEPIX", "Bytes in pixel");
        if (bitpix > 0) {
            parameters.put("bitpix", "" + bitpix);
            hdr.addValue("ZVAL2", bitpix / 8, "Bytes in pixel");
        } else {
            parameters.put("bitpix", "32");
            hdr.addValue("ZVAL2", 4, "Bytes in pixel");
        }
    }

    public void getParameters(Map<String, String> params, Header hdr) {
        if (!params.containsKey("bitpix")) {
            params.put("bitpix", hdr.getIntValue("ZBITPIX") + "");
        }
    }
}
