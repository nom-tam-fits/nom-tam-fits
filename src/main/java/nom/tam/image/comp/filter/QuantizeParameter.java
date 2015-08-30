package nom.tam.image.comp.filter;

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

public class QuantizeParameter {

    private double qlevel;

    private double bScale;

    private double bZero;

    private int intMaxValue;

    private int intMinValue;

    public double getBScale() {
        return this.bScale;
    }

    public double getBZero() {
        return this.bZero;
    }

    public int getIntMaxValue() {
        return this.intMaxValue;
    }

    public int getIntMinValue() {
        return this.intMinValue;
    }

    public double getQLevel() {
        return this.qlevel;
    }

    public QuantizeParameter setBScale(double value) {
        this.bScale = value;
        return this;
    }

    public QuantizeParameter setBZero(double value) {
        this.bZero = value;
        return this;
    }

    public QuantizeParameter setIntMaxValue(int value) {
        this.intMaxValue = value;
        return this;
    }

    public QuantizeParameter setIntMinValue(int value) {
        this.intMinValue = value;
        return this;
    }

    public QuantizeParameter setQLevel(double value) {
        this.qlevel = value;
        return this;
    }
}
