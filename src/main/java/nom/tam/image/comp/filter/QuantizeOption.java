package nom.tam.image.comp.filter;

import nom.tam.image.comp.ICompressOption;
import nom.tam.image.comp.INullCheck;

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

public class QuantizeOption implements ICompressOption {

    private double qlevel = Double.NaN;

    private double bScale = Double.NaN;

    private double bZero = Double.NaN;

    private int intMaxValue;

    private int intMinValue;

    private int tileWidth;

    private int tileHeigth;

    private boolean dither;

    private boolean dither2;

    private long seed;

    private boolean checkNull;

    private boolean checkZero;
    private boolean centerOnZero;

    private double nullValue;

    private double minValue;

    private double maxValue;

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

    public int getTileHeigth() {
        return this.tileHeigth;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public QuantizeOption setBScale(double value) {
        this.bScale = value;
        return this;
    }

    public QuantizeOption setBZero(double value) {
        this.bZero = value;
        return this;
    }

    public QuantizeOption setIntMaxValue(int value) {
        this.intMaxValue = value;
        return this;
    }

    public QuantizeOption setIntMinValue(int value) {
        this.intMinValue = value;
        return this;
    }

    public QuantizeOption setQLevel(double value) {
        this.qlevel = value;
        return this;
    }

    public QuantizeOption setTileHeigth(int value) {
        this.tileHeigth = value;
        return this;
    }

    public QuantizeOption setTileWidth(int value) {
        this.tileWidth = value;
        return this;
    }

    public INullCheck getNullCheck() {
        // TODO Auto-generated method stub
        return null;
    }

    public IDither getDither() {
        // TODO Auto-generated method stub
        return null;
    }

    public double getQlevel() {
        return qlevel;
    }

    public boolean isDither2() {
        return dither2;
    }

    public boolean isDither() {
        return dither;
    }

    public boolean isCenterOnZero() {
        return centerOnZero;
    }
    
    

    public long getSeed() {
        return seed;
    }

    public boolean isCheckNull() {
        return checkNull;
    }

    public boolean isCheckZero() {
        return checkZero;
    }

    public double getNullValue() {
        return nullValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public QuantizeOption setQlevel(double value) {
        this.qlevel = value;
        return this;
    }

    public QuantizeOption setDither(boolean value) {
        this.dither = value;
        return this;
    }

    public QuantizeOption setDither2(boolean value) {
        this.dither2 = value;
        return this;
    }

    public QuantizeOption setCenterOnZero(boolean value) {
        this.centerOnZero = value;
        return this;
    }

    public QuantizeOption setSeed(long value) {
        this.seed = value;
        return this;
    }

    public QuantizeOption setCheckNull(boolean value) {
        this.checkNull = value;
        return this;
    }

    public QuantizeOption setCheckZero(boolean value) {
        this.checkZero = value;
        return this;
    }

    public QuantizeOption setNullValue(double value) {
        this.nullValue = value;
        return this;
    }

    public QuantizeOption setMinValue(double value) {
        this.minValue = value;
        return this;
    }

    public QuantizeOption setMaxValue(double value) {
        this.maxValue = value;
        return this;
    }
}
