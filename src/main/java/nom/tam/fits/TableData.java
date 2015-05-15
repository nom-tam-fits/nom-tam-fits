package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

/**
 * This class allows FITS binary and ASCII tables to be accessed via a common
 * interface.
 */

public interface TableData {

    public abstract int addColumn(Object newCol) throws FitsException;

    public abstract int addRow(Object[] newRow) throws FitsException;

    public abstract void deleteColumns(int row, int len) throws FitsException;

    public abstract void deleteRows(int row, int len) throws FitsException;

    public abstract Object getColumn(int col) throws FitsException;

    public abstract Object getElement(int row, int col) throws FitsException;

    public abstract int getNCols();

    public abstract int getNRows();

    public abstract Object[] getRow(int row) throws FitsException;

    public abstract void setColumn(int col, Object newCol) throws FitsException;

    public abstract void setElement(int row, int col, Object element) throws FitsException;

    public abstract void setRow(int row, Object[] newRow) throws FitsException;

    public abstract void updateAfterDelete(int oldNcol, Header hdr) throws FitsException;

}
