package nom.tam.image;

import java.io.IOException;
import java.lang.reflect.Field;

import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;
import nom.tam.util.RandomAccess;
import nom.tam.util.SaveClose;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StandardImageTilerTest {

    private final class TestImageTiler extends StandardImageTiler {

        private TestImageTiler(RandomAccess f, long fileOffset, int[] dims, Class<?> base) {
            super(f, fileOffset, dims, base);
        }

        @Override
        protected Object getMemoryImage() {
            return StandardImageTilerTest.this.dataArray;
        }

        public void setFile(Object file) throws Exception {
            Field declaredField = StandardImageTiler.class.getDeclaredField("randomAccessFile");
            declaredField.setAccessible(true);
            declaredField.set(this, file);
        }

    }

    private TestImageTiler tiler;

    private BufferedFile file;

    private int[][] dataArray;

    @Before
    public void setup() throws Exception {
        dataArray = new int[10][10];
        BufferedFile file = new BufferedFile("target/StandardImageTilerTest", "rw");
        tiler = new TestImageTiler(file, 0, ArrayFuncs.getDimensions(dataArray), ArrayFuncs.getBaseClass(dataArray));

    }

    @After
    public void close() {
        SaveClose.close(file);
    }

    @Test
    public void testFailedGetTile() throws Exception {
        dataArray = null;
        tiler.setFile(null);
        IOException actual = null;
        try {
            tiler.getTile(null, new int[2], new int[2]);
        } catch (IOException e) {
            actual = e;
        }
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getMessage().contains("No data"));

    }
}
