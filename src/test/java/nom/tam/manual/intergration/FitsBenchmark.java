package nom.tam.manual.intergration;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

import java.io.IOException;
import java.util.regex.Pattern;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.util.BlackBoxImages;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

@State(Scope.Benchmark)
public class FitsBenchmark {

    String blankKey = "      ";

    boolean result;

    Pattern ws = Pattern.compile("[COMMENT|HISTORY]? *");

    public static void main(String[] args) throws RunnerException, IOException {
        Main.main(args);
    }

    private void helloWorld() {
        try {
            long count = 0;
            Fits f = null;
            try {
                f = new Fits(BlackBoxImages.getBlackBoxImage("OEP.fits"));
                BasicHDU<?> hdu;
                while ((hdu = f.readHDU()) != null) {
                    count = count + hdu.getHeader().getSize();
                    if (count > 10) {
                        return;
                    }
                }
            } finally {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Benchmark()
    public void testEmptyString1() {
        result = unkeyedKey(blankKey);
    }

    private boolean unkeyedKey(String key) {
        return "COMMENT".equals(key) || "HISTORY".equals(key) || key.trim().isEmpty();
    }

    @Benchmark
    public void testEmptyString2() {
        result = unkeyedKey2(blankKey);
    }

    private boolean unkeyedKey2(String key) {
        return ws.matcher(key).matches();
    }
}
