package nom.tam.fits.header.extra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.VALUE;

public class GenerateNoa {

    public static String NAME = "NAME:";

    public static String KEYWORD = "KEYWORD:";

    public static String DEFAULT = "DEFAULT:";

    public static String INDEX = "INDEX:";

    public static String HDU = "HDU:";

    public static String VALUE = "VALUE:";

    public static String UNITS = "UNITS:";

    public static String COMMENT = "COMMENT:";

    public static String EXAMPLE = "EXAMPLE:";

    public static String DESCRIPTION = "DESCRIPTION:";

    public static String[] attributes = {
        NAME,
        KEYWORD,
        DEFAULT,
        INDEX,
        HDU,
        VALUE,
        UNITS,
        COMMENT,
        EXAMPLE,
        DESCRIPTION
    };

    public static void main(String[] args) throws Exception {
        BufferedReader file = new BufferedReader(new FileReader(new File("src/main/resources/nom/tam/fits/header/extra/NOAOExt.java")));
        String line;
        String lastAttribute = "";
        Map<String, String> element = new HashMap<>();
        boolean firstLine = true;
        while ((line = file.readLine()) != null) {
            firstLine = false;
            line = line.trim();
            for (String att : attributes) {
                if (line.startsWith(att)) {
                    lastAttribute = att;
                    line = line.substring(line.indexOf(':') + 1).trim();
                    firstLine = true;
                }
            }
            if (firstLine && lastAttribute.equals(NAME)) {
                if (!element.isEmpty()) {
                    newElement(element);
                }
                element = new HashMap<>();
            }
            String oldValue = element.get(lastAttribute);
            if (oldValue == null) {
                oldValue = "";
            }
            element.put(lastAttribute, oldValue + " " + line);
        }
        newElement(element);
    }

    private static void newElement(Map<String, String> element) {
        String name = element.get(NAME);
        if (name != null && !name.isEmpty()) {
            String keywords = element.get(KEYWORD);
            StringTokenizer tokens = new StringTokenizer(keywords);
            while (tokens.hasMoreTokens()) {
                String keyword = tokens.nextToken();
                keyword = keyword.replaceAll("%d", "n");
                keyword = keyword.replaceAll("%2d", "nn");
                keyword = keyword.replaceAll("%3d", "nnn");
                keyword = keyword.replaceAll("%4d", "nnn");
                String hdu = element.get(HDU).trim().toUpperCase();
                String value = element.get(VALUE).trim().toUpperCase();
                String nameValue = element.get(NAME).trim().toUpperCase();
                String decription = element.get(DESCRIPTION).trim();

                String defaultValue = element.get(DEFAULT).trim();
                String index = element.get(INDEX).trim();
                String units = element.get(UNITS).trim();
                String comment = element.get(COMMENT).trim();

                System.out.println("/**");
                System.out.println(" *" + decription);
                if (!units.isEmpty()) {
                    System.out.println(" * <p>");
                    System.out.println(" * units = " + units);
                    System.out.println(" * </p>");
                }
                if (!defaultValue.isEmpty()) {
                    System.out.println(" * <p>");
                    System.out.println(" * default value = " + defaultValue);
                    System.out.println(" * </p>");
                }
                if (!index.isEmpty()) {
                    System.out.println(" * <p>");
                    System.out.println(" * index = " + index);
                    System.out.println(" * </p>");
                }
                if (value.startsWith("%G")) {
                    value = "REAL";
                }
                if (value.startsWith("%H")) {
                    value = "STRING";
                }
                if (value.startsWith("%S")) {
                    value = "STRING";
                }
                if (value.startsWith("%D")) {
                    value = "INTEGER";
                }
                if (value.startsWith("%B")) {
                    value = "LOGICAL";
                }
                
                if (value.isEmpty()) {
                    value = "NONE";
                }
                hdu = hdu.replaceAll(" \\& ", "_");
                hdu = hdu.replaceAll(" \\| ", "_");
                hdu = hdu.replaceAll(" \\|\\| ", "_");
                System.out.println(" */");
                if (keyword.indexOf('-') >= 0) {
                    System.out.println(keyword.replace('-', '_') + "(\"" + keyword + "\",HDU." + hdu + ", VALUE." + value + ", \"" + comment + "\"),");
                } else {
                    System.out.println(keyword + "(HDU." + hdu + ", VALUE." + value + ", \"" + comment + "\"),");
                }
            }
        }
    }
}
