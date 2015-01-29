package nom.tam.fits.utilities;

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

import nom.tam.fits.*;
import nom.tam.util.*;

public class FitsCopy {

    public static void main(String[] args) throws Exception {

        String file = args[0];

        Fits f = new Fits(file);
        int i = 0;
        BasicHDU h;

        do {
            h = f.readHDU();
            if (h != null) {
                if (i == 0) {
                    System.out.println("\n\nPrimary header:\n");
                } else {
                    System.out.println("\n\nExtension " + i + ":\n");
                }
                i += 1;
                h.info();
            }
        } while (h != null);
        BufferedFile bf = new BufferedFile(args[1], "rw");
        f.write(bf);
        bf.close();

    }
}
