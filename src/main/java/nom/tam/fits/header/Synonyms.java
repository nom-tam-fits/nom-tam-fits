package nom.tam.fits.header;

/**
 * This enum wil try to list synonyms inside or over different dictionaries. So
 * please use always the higest level keyword you can find.
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public enum Synonyms {
    EQUINOX(Standard.EQUINOX, Standard.EPOCH);

    private final IFitsHeader primaryKeyword;

    private final IFitsHeader[] synonyms;

    public IFitsHeader primaryKeyword() {
        return primaryKeyword;
    }

    public IFitsHeader[] getSynonyms() {
        return synonyms;
    }

    private Synonyms(IFitsHeader primaryKeyword, IFitsHeader... synonyms) {
        this.primaryKeyword = primaryKeyword;
        this.synonyms = synonyms;
    }

}
