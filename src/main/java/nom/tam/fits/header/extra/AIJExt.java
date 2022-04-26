package nom.tam.fits.header.extra;

import nom.tam.fits.header.FitsHeaderImpl;
import nom.tam.fits.header.IFitsHeader;

public enum AIJExt implements IFitsHeader {
    /**
     * Custom ANNOTATE key for AstroImageJ, where multiple ANNOTATE keys are allowed
     */
    ANNOTATE(SOURCE.AIJ);

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    private static final ThreadLocal<Class<?>> COMMENT_CONTEXT = new ThreadLocal<Class<?>>();

    AIJExt(SOURCE status) {
        this.key = new FitsHeaderImpl(name(), status, HDU.IMAGE, VALUE.STRING, null);
    }

    @Override
    public String comment() {
        return this.key.comment();
    }

    @Override
    public HDU hdu() {
        return this.key.hdu();
    }

    @Override
    public String key() {
        return this.key.key();
    }

    @Override
    public IFitsHeader n(int... number) {
        return this.key.n(number);
    }

    @Override
    public SOURCE status() {
        return this.key.status();
    }

    @Override
    @SuppressWarnings("CPD-END")
    public VALUE valueType() {
        return this.key.valueType();
    }

    public static void context(Class<?> clazz) {
        COMMENT_CONTEXT.set(clazz);
    }
}
