package nom.tam.util.type;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nom.tam.fits.FitsException;
import nom.tam.fits.header.Bitpix;

/**
 * A base data element type in a FITS image or table column, with associated
 * functions.
 *
 * @param <B> the generic type of data buffer
 */
public abstract class ElementType<B extends Buffer> {

    /**
     * Tha size value to use to indicate that instance have their own size each
     */
    private static final int VARIABLE_SIZE = -1;

    /** Number of bytes to copy as a block */
    public static final int COPY_BLOCK_SIZE = 1024;

    /** The BITPIX integer value associated with this type of element */
    private final int bitPix;

    /** The class of NIO Buffer associated with this type of element */
    private final Class<B> bufferClass;

    /** The primitive data class of this element */
    private final Class<?> primitiveClass;

    /** The fixed size for this element, if any */
    private final int size;

    /**
     * The second character of the Java array type, e.g. `J` from `[J` for
     * `long[]`
     */
    private final char javaType;

    /** A boxing class for the primitive type */
    private final Class<?> wrapperClass;

    /**
     * Instantiates a new FITS data element type.
     *
     * @param size the number of bytes in the FITS representation of that type.
     * @param varSize <code>true</code> if the element has a size that varies
     *            from object to object.
     * @param primitiveClass The primitive data type, e.g. `int.class`, or
     *            <code>null</code> if no primitive type is associated.
     * @param wrapperClass The boxed data type, e.g. `Integer.class`, or
     *            <code>null</code> if no boxed type is associated.
     * @param bufferClass The type of underlying buffer (in FITS), or
     *            <code>null</code> if arrays of this type cannot be wrapped into
     *            a buffer directly (e.g. because of differing byrte size or
     *            order).
     * @param type The second character of the Java array type, e.g. `J` from
     *            `[J` for `long[]`.
     * @param bitPix The BITPIX header value for an image HDU of this type.
     */
    protected ElementType(int size, boolean varSize, Class<?> primitiveClass, Class<?> wrapperClass, Class<B> bufferClass,
            char type, int bitPix) {
        this.size = varSize ? VARIABLE_SIZE : size;
        this.primitiveClass = primitiveClass;
        this.wrapperClass = wrapperClass;
        this.bufferClass = bufferClass;
        javaType = type;
        this.bitPix = bitPix;
    }

    /**
     * Appends data from one buffer to another.
     *
     * @param buffer the destination buffer
     * @param dataToAppend the buffer containing the data segment to append.
     */
    public void appendBuffer(B buffer, B dataToAppend) {
        throw new UnsupportedOperationException("no primitive type");
    }

    /**
     * Appends data from one buffer to a byte buffer.
     *
     * @param byteBuffer the destination buffer
     * @param dataToAppend the buffer containing the data segment to append.
     */
    public void appendToByteBuffer(ByteBuffer byteBuffer, B dataToAppend) {
        byte[] temp = new byte[Math.min(COPY_BLOCK_SIZE * size(), dataToAppend.remaining() * size())];
        B typedBuffer = asTypedBuffer(ByteBuffer.wrap(temp));
        Object array = newArray(Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining()));
        while (dataToAppend.hasRemaining()) {
            int part = Math.min(COPY_BLOCK_SIZE, dataToAppend.remaining());
            getArray(dataToAppend, array, part);
            putArray(typedBuffer, array, part);
            byteBuffer.put(temp, 0, part * size());
        }
    }

    public B asTypedBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("no primitive buffer available");
    }

    /**
     * Returns the integer BITPIX value to set in FITS headers for image HDUs of
     * this element type.
     *
     * @return The BITPIX value that FITS uses to specify images of this element
     *             type.
     */
    public int bitPix() {
        return bitPix;
    }

    /**
     * Return the class of buffer that can be used to serialize or deserialize
     * elements of this type.
     *
     * @return The class of buffer that can transact elements of this type.
     *
     * @see #getArray(Buffer, Object, int, int)
     * @see #putArray(Buffer, Object, int, int)
     */
    public Class<B> bufferClass() {
        return bufferClass;
    }

    /**
     * Serializes a 1D Java array containing Java native elements into a buffer
     * using the appropriate FITS representation
     *
     * @param array the 1D Java array of elements for this type
     *
     * @return The FITS serialized representation as a buffer of bytes.
     */
    public ByteBuffer convertToByteBuffer(Object array) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[Array.getLength(array) * size()]);
        putArray(asTypedBuffer(buffer), array);
        buffer.rewind();
        return buffer;
    }

    /**
     * Gets all elements of an array from a buffer
     *
     * @param buffer the typed buffer from which to retrieve elements
     * @param array the 1D array of matching type
     *
     * @see #getArray(Buffer, Object, int)
     * @see #getArray(Buffer, Object, int, int)
     * @see #putArray(Buffer, Object)
     */
    public final void getArray(B buffer, Object array) {
        getArray(buffer, array, Array.getLength(array));
    }

    /**
     * Gets elements of an array from a buffer, starting from the beginning of
     * the array.
     *
     * @param buffer the typed buffer from which to retrieve elements
     * @param array the 1D array of matching type
     * @param length the number of elements to fretrieve
     *
     * @see #getArray(Buffer, Object)
     * @see #getArray(Buffer, Object, int, int)
     * @see #putArray(Buffer, Object, int)
     */
    public final void getArray(B buffer, Object array, int length) {
        getArray(buffer, array, 0, length);
    }

    /**
     * Gets elements of an array from a buffer, starting from the specified array
     * index.
     *
     * @param buffer the typed buffer from which to retrieve elements
     * @param array the 1D array of matching type
     * @param offset the array index of the first element to retrieve
     * @param length the number of elements to fretrieve
     *
     * @see #getArray(Buffer, Object)
     * @see #putArray(Buffer, Object, int, int)
     */
    public void getArray(B buffer, Object array, int offset, int length) {
        throw new UnsupportedOperationException("no primitive type");
    }

    /**
     * Checks if this type of element has a variable size, rather than a fixed
     * size
     *
     * @return <code>true</code> if this element may appear with different sizes
     *             in the FITS binary stream. Otherwise <code>false</code> if it
     *             is always the same fixed size.
     *
     * @see #size()
     */
    public boolean isVariableSize() {
        return size == VARIABLE_SIZE;
    }

    /**
     * @deprecated Use {@link #isVariableSize()} instead.
     *
     * @return <code>true</code> if this type of element comes in all sizes, and
     *             the particular size of an obejct of this element type is
     *             specific to its instance. Or, <code>false</code> for
     *             fixed-sized elements.
     */
    @Deprecated
    public final boolean individualSize() {
        return isVariableSize();
    }

    /**
     * Checks if this element type is the same as another.
     *
     * @param other Another element type
     *
     * @return <code>true</code> if both element types are the same, otherwise
     *             <code>false</code>.
     */
    public boolean is(ElementType<? extends Buffer> other) {
        return bitPix == other.bitPix();
    }

    /**
     * Creates a new 1D Java array for storing elements of this type.
     *
     * @param length the number of elements to store in the array
     *
     * @return the Java array suitable for storing the elements, or
     *             <code>null</code> if the operation is not supported or
     *             possible.
     *
     * @see #newBuffer(int)
     */
    public Object newArray(int length) {
        return null;
    }

    /**
     * Creates a new new buffer of the specified size for this type of elements
     *
     * @param length the number of elements in the buffer
     *
     * @return a new buffer of the specified size for this type of elements
     *
     * @see #newArray(int)
     * @see #newBuffer(long)
     */
    public final B newBuffer(int length) {
        return wrap(newArray(length));
    }

    /**
     * Currently the same as {@link #newBuffer(int)}, but in the future it may be
     * used to implement large memory mapped buffers....
     *
     * @param length the number of elements in the buffer
     *
     * @return a new buffer of the specified size for this type of elements, or
     *             <code>null</code> if the argument is beyond the supported
     *             range
     *
     * @throws IllegalArgumentException if the length is larger than what can be
     *             supported.
     *
     * @see #newBuffer(int)
     */
    public final B newBuffer(long length) throws IllegalArgumentException {
        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Currently only buffers of 32-bit integer size are supported.");
        }
        // TODO handle big arrays differently by using memory mapped files.
        return newBuffer((int) length);
    }

    /**
     * Returns the Java primitive type corresponsing to this element, if any
     *
     * @return the Java primitive type that corresponds to this element, or
     *             <code>null</code> if there is no primitive type equivalent to
     *             this FITS element type.
     *
     * @see #wrapperClass()
     * @see #type()
     */
    public Class<?> primitiveClass() {
        return primitiveClass;
    }

    /**
     * Puts all elements from an array into the given buffer
     *
     * @param buffer the typed buffer in which to put elements
     * @param array the 1D array of matching type
     *
     * @see #putArray(Buffer, Object, int)
     * @see #putArray(Buffer, Object, int, int)
     * @see #getArray(Buffer, Object)
     *
     * @since 1.18
     */
    public final void putArray(B buffer, Object array) {
        putArray(buffer, array, Array.getLength(array));
    }

    /**
     * Puts elements from an array into the given buffer, starting from the
     * beginning of the array
     *
     * @param buffer the typed buffer in which to put elements
     * @param array the 1D array of matching type
     * @param length the number of elements to put into the buffer
     *
     * @see #putArray(Buffer, Object)
     * @see #putArray(Buffer, Object, int, int)
     * @see #getArray(Buffer, Object, int)
     *
     * @since 1.18
     */
    public final void putArray(B buffer, Object array, int length) {
        putArray(buffer, array, 0, length);
    }

    /**
     * Puts elements from an array into the given buffer, starting from the
     * specified array index.
     *
     * @param buffer the typed buffer in which to put elements
     * @param array the 1D array of matching type
     * @param offset the array index of the first element to put into the buffer
     * @param length the number of elements to put into the buffer
     *
     * @see #putArray(Buffer, Object)
     * @see #getArray(Buffer, Object, int, int)
     *
     * @since 1.18
     */
    public void putArray(B buffer, Object array, int offset, int length) {
        throw new UnsupportedOperationException("no primitive type");
    }

    /**
     * Returns the number of bytes per elements
     *
     * @return the number of bytes each element of this type occupies in FITS
     *             binary representation
     *
     * @see #isVariableSize()
     */
    public int size() {
        return size;
    }

    /**
     * currently the only individual size primitive so, keep it simple
     *
     * @param instance the object to calculate the size
     *
     * @return the size in bytes of the object instance
     *
     * @throws IllegalArgumentException if the object is not of the type expected
     *             by this class.
     */
    public int size(Object instance) {
        if (instance == null) {
            return 0;
        }

        Class<?> cl = instance.getClass();
        if (!(primitiveClass.isAssignableFrom(cl) || wrapperClass.isAssignableFrom(cl))) {
            throw new IllegalArgumentException(
                    "Class " + cl.getName() + " does not match type " + getClass().getSimpleName());
        }

        return size();
    }

    /**
     * Returns a new typed buffer that starts the the current position of the
     * supplied typed buffer for this element. See {@link Buffer#slice()} for the
     * contract on slices.
     *
     * @param buffer the buffer from which to create the new slice
     *
     * @return A new buffer of the same type as the argument, that begins at the
     *             current position of the original buffer, or <code>null</code>
     *             if the slicing is not possuble or not implemented.
     *
     * @see Buffer#slice()
     */
    public B sliceBuffer(B buffer) {
        return null;
    }

    /**
     * Returns the Java letter-code for this FITS element type. For example Java
     * <code>long</code> would be type 'J' since 1D <code>long[]</code> arrays
     * report as <code>[J</code> by Java.
     *
     * @return the boxed Java type for this FITS element type.
     *
     * @see #primitiveClass()
     * @see #wrapperClass()
     * @see #forDataID(char)
     */
    public char type() {
        return javaType;
    }

    /**
     * Returns a buffer for this element type by wrapping a suitable 1D array as
     * its backing store.
     *
     * @param array the matching 1D array for this type to serve as the backing
     *            store of the buffer. Changes to the array will be visible
     *            through the buffer and vice versa.
     *
     * @return A new buffer for this type of element that uses the specified
     *             array as its backing store.
     */
    public B wrap(Object array) {
        return null;
    }

    /**
     * Returns the boxed Java type for this type of element.
     *
     * @return the boxed Java type that corresponds to this type of element.
     *
     * @see #primitiveClass()
     * @see #type()
     */
    public Class<?> wrapperClass() {
        return wrapperClass;
    }

    public static final ElementType<Buffer> BOOLEAN = new BooleanType();

    public static final ElementType<ByteBuffer> BYTE = new ByteType();

    public static final ElementType<ByteBuffer> CHAR = new CharType();

    public static final ElementType<DoubleBuffer> DOUBLE = new DoubleType();

    public static final ElementType<FloatBuffer> FLOAT = new FloatType();

    public static final ElementType<IntBuffer> INT = new IntType();

    public static final ElementType<LongBuffer> LONG = new LongType();

    public static final ElementType<ShortBuffer> SHORT = new ShortType();

    public static final ElementType<Buffer> STRING = new StringType();

    public static final ElementType<Buffer> UNKNOWN = new UnknownType();

    private static Map<Class<?>, ElementType<?>> byClass;

    private static Map<Character, ElementType<?>> byType;

    static {
        Map<Class<?>, ElementType<?>> initialByClass = new HashMap<>();
        Map<Character, ElementType<?>> initialByType = new HashMap<>();
        for (ElementType<?> type : values()) {
            initialByType.put(type.type(), type);
            initialByClass.put(type.primitiveClass(), type);
            initialByClass.put(type.wrapperClass(), type);
            if (type.bufferClass() != null) {
                initialByClass.put(type.bufferClass(), type);
            }
        }
        byClass = Collections.unmodifiableMap(initialByClass);
        byType = Collections.unmodifiableMap(initialByType);
    }

    /**
     * Returns the Fits element type for a given Java array type letter. For
     * example {@link #LONG} is returned for 'J' since Java denotes
     * <code>long[]</code> arrays as <code>[J</code> in shorthand.
     *
     * @param type the letter code used for denoting java arrays of a given type
     *            in shorthand
     *
     * @return the matching FITS element type.
     *
     * @see #type()
     */
    public static ElementType<Buffer> forDataID(char type) {
        return cast(byType.get(type));
    }

    /**
     * Returns the FITS element type for a given Java type
     *
     * @param <B> The generic type of buffer for the FITS element type
     * @param clazz The Java primitive or boxed type for the corresponding
     *            element, or else the buffer class that it uses.
     *
     * @return The matching FITS element type.
     *
     * @see #primitiveClass()
     * @see #wrapperClass()
     * @see #bufferClass()
     * @see #forBuffer(Buffer)
     */
    public static <B extends Buffer> ElementType<B> forClass(Class<?> clazz) {
        ElementType<?> primitiveType = byClass.get(clazz);
        if (primitiveType == null) {
            for (Class<?> interf : clazz.getInterfaces()) {
                primitiveType = byClass.get(interf);
                if (primitiveType != null) {
                    return cast(primitiveType);
                }
            }
            return forClass(clazz.getSuperclass());
        }
        return cast(primitiveType);
    }

    /**
     * Returns the FITS element type that can transact with the specified buffer
     * type directly.
     *
     * @param <B> the generic type of buffer
     * @param b a typed buffer instance
     *
     * @return the FITS element type that goes with the specified typed buffer
     *
     * @see #forClass(Class)
     */
    public static <B extends Buffer> ElementType<B> forBuffer(B b) {
        return forClass(b.getClass());
    }

    /**
     * Returns the FITS element type that matches the specified BITPIX value
     * exactly.
     *
     * @param bitPix the BITPIX value that FITS uses to specify the element type
     *            for images.
     *
     * @return The matching FITS element type, or <code>null</code> if there is
     *             no matching FITS element type.
     *
     * @see #forNearestBitpix(int)
     * @see #bitPix()
     */
    public static ElementType<Buffer> forBitpix(int bitPix) {
        try {
            return cast(Bitpix.forValue(bitPix).getElementType());
        } catch (FitsException e) {
            return null;
        }
    }

    /**
     * Returns the FITS element type that is nearest to the specified BITPIX
     * value exactly. This method can be used to guess what the element type may
     * be when the BITPIX value is not strictly to specification in the FITS
     * header.
     *
     * @param bitPix the BITPIX value that FITS uses to specify the element type
     *            for images.
     *
     * @return The FITS element type that is closest to the specified value, or
     *             <code>UNKNOWN</code> if the specified values is not near any
     *             known BITPIX type.
     *
     * @see #forBitpix(int)
     * @see #bitPix()
     */
    public static ElementType<Buffer> forNearestBitpix(int bitPix) {
        try {
            return cast(Bitpix.forValue(bitPix, true).getElementType());
        } catch (FitsException e) {
            return UNKNOWN;
        }
    }

    /**
     * Casts a FITS element type to its own type.
     *
     * @param <B> the genetic type of buffer used to the element
     * @param e some FITS element
     *
     * @return the element cast to its proper type.
     */
    @SuppressWarnings("unchecked")
    private static <B extends Buffer> ElementType<B> cast(ElementType<?> e) {
        return (ElementType<B>) e;
    }

    private static ElementType<?>[] values() {
        return new ElementType[] {BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, STRING, UNKNOWN};
    }

}
