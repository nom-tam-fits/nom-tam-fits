# Changelog

All notable changes to the nom.tam.fits library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased] 

Bug fix release candidate.


### Fixed

 - [#636] Compressed table columns lost their column names in 1.20.0. (thanks to @keastrid)

 - [#656] Fixed incorrect value types in several FITS keywords extension dictionaries. (thanks to @johnmurphyastro)

 - [#664] Recalculate stored checksums when writing headers to avoid invalid checksums being set by `setChecksum()` methods. (thanks to @johnmurphyastro)


### Added

 - [#657] Added `ESOExt` enum with ESO-specific standard FITS keywords, and `CommonExt` for commonly used keywords in the amateur astronomy community.

 - [#657] Added more standard keywords to `CXCEXt`, `STScIExt`, `MaxImDLExt`, and `SBFitsExt` dictionaries based on the available documentation online.

 - [#657] Additional keyword synonyms in `Synonyms` enum.


### Changed

 - [#657] Updated comment fields and Javadoc for many extension keywords in the `nom.tam.fits.header.extra` package.

 - [#665] Keep original comments for mandatory keywords when writing headers that were previously read from an input.

 - [#665] `CHECKSUM` and `DATASUM` comments now store timestamp when checksums were calculated, as is the recommended practice by the checksumming standard.

 - Do not generate reports that have very little use.

 - Uses the latest maven build and runtime dependencies.


### Deprecated

 - [#657] Deprecated the `CXCStclShareExt` enum. Not only it's name was completely botched, its definitions are now included properly in the `CXCExt` and `STScIExt` dictionaries (without reduntant definitions).

 - [#657] Deprecated a few extension keywords, which are either part of the standard or are recognised by HEASARC as commonly used keywords. The mentioned more common definitions should be used instead going forward.

 - [#665] Deprecated some checksuming methods that are no longer useful, as they do not offer a mean to verify FITS files. Users might want to use `BasicHDU.verifyIntegrity()` or `Fits.verifyIntegrity()` instead.


## [1.20.0] - 2024-05-10

Image quantization, slicing and sampling, and more.


### Fixed

 - [#596] Botched compression of multicolumn tables, resulting in extra rows in the compressed table (as many rows as there were total table tiles across all columns). (thanks to @keastrid)

 - [#610] `Standard.TNULLn` keyword can take any argument type (was previously restricted to strings only).

 - [#621] `MultiArrayIterator` fixes to `size()` and `deepComponentType()` methods.


### Added

 - [#586] Default column names. Some tools, including (C)FITSIO have issues dealing with table columns that have no `CTYPEn` keywords set to define column names. Therefore to enhance interoperability we will use 'Column N' type naming, until the user sets a column name otherwise.

 - [#611] New `Quantizer` class to handle generic decimal-integer conversions in line with the FITS specification for integer representation of floating-point data. The new class will interpret the necessary header keywords for both images and tables, and can fill the required values in FITS headers.

 - [#611] `ImageData.setQuantizer()` / `.getQuantizer()` and `BinaryTable.ColumnDesc.setQuantizer()` / `.getQuantizer()` methods to access the quantizers that handle decimal-integer conversions for images and binary table columns, respectively.

 - [#610] `ImageData.convertTo(Class type)` will return the equivalent image data with another storage type (but otherwise the same as the original image). Any decimal-integer conversion is handled by the image's own quantizer (which is either specified in the image header or else set by the user) if it is available.

 - [#610] New `BinaryTable.getArrayElement(row, col)` and `.getArrayElementAs(int row, int col, Class asType)` methods to return the table element as an array always (without the boxing of scalar entries). The former is similar to the original `.getElement()` method except that it represents FITS logicals as `Boolean[]...`, bits as `boolean[]...`, and complex values a `ComplexValue[]...` arrays. The second form allows accessing numerical table elements as arrays of a desired other numerical type.

 - [#610] `ArrayFuncs.convertArray(Object array, Class newType, Quantizer quant)` method provides generic conversions to numerical type arrays, including `ComplexValue` type arrays, and can be used for explicit quantization conversions outside of images and table columns.

 - [#592] `ImageData` now supports complex valued images as per the FITS standard, both when constructed programatically from `ComplexValue[]...` data; or if so described with an image axis whose `CTYPEn` header value is 'COMPLEX' and the axis contains 2 components (so long as the complex axis is either the first or last axis). The latter is true even if integer representation is used in the FITS, e.g. via a quantization. When reading complex valued images, the native `ImageData` is that of the stored type (e.g. `float[]..[2]` or `float[2]..[]`) for compatibility, but `ImageData.convertTo(ComplexValue.class)` or `.convertTo(ComplexValue.Float.class)` can convert these to be true complex valued images in a second step after reading. (You may also use `ImageData.isComplexValued()` to check if the data were designated as complex-valued at origination, by the FITS header or the constructor.)

 - [#619] New `ArrayFuncs.sample()` and `.slice()` methods, to provide generic support for obtaining slices and samplings from arrays in one or more dimensions, with the option of reversed order sampling along selected axes. (thanks to @at88mph)


### Changed

 - [#590] Removed `Main` class, and `main()` entry points from JAR, which also clears up a whole lot of junk being added to the classpath into `MANIFEST.MF`. (thanks to @bogdanni)

 - [#592] `ArrayFuncs.convertArray(Object, Class)` is now the same as the new `ArrayFuncs.convertArray(Object, Class, Quantizer)` method without a quantizer, and thus allows conversions to/from complex valued data also, when primitive arrays have a trailing dimension of 2 (e.g. `float[]..[2]` or `int[]..[2]`).

 - [#609] `MultiArrayCopier.copyInto()` now rounds floating point values before casting to integer types for a more regular behavior.

 - [#611] `BinaryTable.getDouble(...)` and `.getLong(...)` and `.set(...)` methods will now use the specified quantization (as specified in the header or else set explicitly by the user) when converting from decimal to integer types and vice-versa.

 - [#620] Updated github.io landing page with history and partial listing of software that uses nom-tam-fits.

 - [#620] Fix up POM to cure various build warnings, and allow Google Analytics for online Javadoc.

 - [#623] Fix up test code to use latest `nanohttpd-webserver` for unit testing.

 - Updated User's Guide (`README.md`).

 - Uses the latest maven build and runtime dependencies.


## [1.19.1] - 2024-03-25

Bug fix release, with improved table compression support.


### Fixed

 - [#562] Compressed tables had the `ZCTYPn` keywords written with a 0-based index, and thus were off-by-one in the FITS convention. Now fixed. (thanks to @keastrid)

 - [#552] Fix inconsistently maintained `ColumnDesc.fileSize`, by replacing it with dynamic `.rowLen()`. (thanks to @keastrid)

 - [#557] Fix the compression of variable-length table columns. These were completely off-spec in prior releases, but are now properly supported as per the (C)FITSIO convention, which is slightly deviating from the documented standard. However, based on private communication with the maintainers, they are inclined to resolve this discrepancy by updating the standard to match the (C)FITSIO implementation, and therefore we adopt the (C)FITSIO way also for compressing tables. Nevertheless, we provide the static `CompressedTableHDU.useOldStandardIndexing(boolean)` method to allow reading compressed VLA columns produced in the other way also (i.e as the current standard actually describes it). Note, that our VLA compression support is now much better than that of (C)FITSIO, whose current release contains multiple severe bugs in that regard. (thanks to @keastrid)

 - [#570] Fix potential narrowing conversion issues uncovered by GitHub's CodeQL.

 - [#576] Eliminate unexpected exception thrown if trying to write an empty table to a file/stream. Now it works as expected.

 - [#578] Check setting elements in ColumnTable more consistently to avoid creating corrupted FITS tables.

 - [#580] Fix `ArrayIndexOutOfBoundsException` when defragmenting larger heaps. (thanks to @keastrid)


### Added

 - [#558] Find header cards with regex pattern matching with `Header.findCards()`.

 - [#563] Added support for optionless Rice and HCOMPRESS compression, using the default option values as per the FITS standard. As a result `RICE_1` can now be used for compressing integer-type binary table columns.

 - [#575] Added support for reserving space in FITS files for future additions to binary tables in situ without disturbing the rest of the FITS file. Space for extra table rows can be reserved via `BinaryTable.reserveRowSpace(int)`, while additional heap space for VLA additions / updates can be set aside by `BinaryTable.reserveHeapSpace(int)` prior to writing the table to a file. Note, however that (C)FITSIO may not be able to read files with reserved row space due to its lack of support for the standard `THEAP` keyword.


### Changed

 - [#563] Creating compressed tables now also checks that the requested compression algorithms are in fact suitable for table compression, or else throws an IllegalArgumentException. (Currently, only the lossless `GZIP_1`, `GZIP_2`, `RICE_1`, and `NOCOMPRESS` are allowed by the FITS standard for tables.)

 - [#567] The parallel table compression has previously resulted in a heap with irreproducible random ordering. Now, the compressed heap is ordered consistently for optimal sequential read performance.

 - [#571] From here on we'll omit writing the THEAP keyword into binary table headers when they are not necessary (that is when the heap follows immediately after the main table). This allows better interoperability with (C)FITSIO, which currently lacks proper support for this standard FITS keyword.

 - [#582] Support heaps up to 2GB (previously they were limited to 1GB max).

 - Uses the latest maven build and runtime dependencies.


## [1.19.0] - 2024-01-19

Feature release with bug fixes.


### Fixed

 - [#496] Workaround for read-only FITS files on Windows network shares.

 - [#531] Keywords with hyphens in `STScIExt` had the wrong form previously.

 - [#532] Small fixes to HashedList handling iterator access corner cases.

 - [#535] Binary table `NAXIS1` value was sometimes stored as a string.


### Added

 - [#488] New targeted Data and HDU creation from Java objects via new `Data+.from(Object)` and `Data+.toHDU()` methods, replacing the now deprecated and unsafe `[...]HDU.encapsulate(Object)` methods.

 - [#489] New `Header.mergeDistinct(Header)` method to allow copying non-conflicting header keywords from one FITS header to another.

 - [#490] New inspection methods to `UndefinedData` class, to easily retrieve `XTENSION` type, `BITPIX`, and data size/shape information from HDUs containing unsupported data types.

 - [#492] New access methods for parameters stored in `RandomGroupsHDU` types, according to the FITS specification.

 - [#494] A better way to control how FITS `I10` type columns are treated in ASCII tables, via static `AsciiTable.setI10PreferInt(boolean)` and `.isI10PreferInt()` methods.

 - [#520] New methods to make the decompression of selected `CompressedTableHDU` tiles even easier (and safer) to use.

 - [#531] New `WCS` and `DateTime` enums with an enumeration of all standard FITS WCS and date-time related keywords, and recognized values (constants). The WCS enums support alternate coordinate systems via the `WCS.alt(char)` method. For example to generate the "CTYPE1A" standard keyword you may call `WCS.CTYPEna.alt('A').n(1)`.

 - [#532] Support for `INHERIT` keyword via `Fits.getCompleteHeader(...)` methods. These methods will populate the mandatory FITS header keywords in the the selected HDU, and return either the updated header or else a new header composed up of both the explicitly defined keywords in the selected HDU and the inherited (non-conflicting) entries from the primary HDU.

 - [#534] Adding standardized (`IFitsHeader`) keywords to HDU headers will now check that the keyword can be used with the associated HDU type (if any), and may throw an `IllegalArgumentException` if the usage is inappropriate. The type of checks (if any) can be adjusted via the new `Header.setKeywordChecking()` and/or the static `Header.setDefaultKeywordChecking()` methods.

 - [#534] Setting values for `HeaderCard`s with standardized (`IFitsHeader`) keywords now checks that the assigned value is compatible for the given keyword, and may throw an appropriate exception (such as a newly added `ValueTypeException`), or else log a warning message depending on the current value checking policy. The policy can be adjusted via the new static `HeaderCard.setValueCheckPolicy()` method.

 - [#538] New `FitsKey` class to replace the poorly named `FitsHeaderImpl` class. (The latter continues to exist in deprecated form for compatibility). Added new functionality.

 - [#538] New `Standard.match(String)` method to match one of the reserved keyword templates of the FITS standard to a keyword instance, such as "CTYPE1A" to `WCS.CTYPEna`.


### Changed

 - [#519] Demoted `FitsException` to be a soft exception, extending `IllegalStateException`. As a result, users are no longer required to catch these, or its derivatives such as `HeaderCardException`. Some methods that have previously thrown `IllegalStateException` may now throw a `FitsException`, in a backwards compatible way.

 - [#518] The constructor of `PaddingException` does not throw a `FitsException`, and `FitsCheckSum.Checksum` also does not throw an `IllegalArgumentException` -- so they are now declared as such. Some related Javadoc updates.

 - [#525] Improvements to `AsciiTable` with `addColumn()` argument checking and more specific documentation.

 - [#531] `Synonyms` has been extended to enumerate all synonymous WCS keys and a few other keywords with equivalent definitions.

 - [#531] Added `impl()` method to `IFitsHeader` interface with a default implementation to eliminate code that was duplicated many times over across enums.

 - [#531] `IFitsHeader.n(int...)` now checks that the supplied indices are in the 0-999 range, or else throw an `IndexOutOfBoundsException`. If too many indices are supplied it will throw a `NoSuchElementException`. And, if the resulting indexed keyword exceeds the 8-byte limit of standard FITS keywords it will throw an `IllegalStateException`.

 - [#531] Attempting to create a `HeaderCard` with a standard keyword that has unfilled indices will throw an `IllegalArgumentException`.

 - `CompressedImageHDU.getTileHDU()` now updates `CRPIXna` keywords also in the alternate coordinate systems (if present) for the selected cutout image.

 - [#538] Corrections to standard keyword sources, deprecations, synonyms, comments and associated Javadoc.

 - Change from unmaintained findbugs build dep to spotbugs successor.

 - Migrate testing from JUnit 4 to JUnit 5 Vintage.

 - Uses the latest maven build and runtime dependencies

 - Fully revised `README` (User's guide), with better organization, new sections, further clarifications, and many corrections.


### Deprecated

 - [#519] Deprecated `IHeaderAccess` interface and its implementations. These were meant for internal use anyway, and with HeaderCardException now being a soft exception these classes just add confusion without providing any functionality that isn't readily available without them.


## [1.18.1] - 2023-09-11

Important bug fix release with minor enhancements.


### Fixed

 - [#466] Fixed broken default reading of ASCII tables in 1.18.0, without explicitly having to set `FitsFactory.setUseAsciiTables(true)`.


### Added

 - [#474] New `Fits.verifyIntegrity()`, `BasicHDU.verifyIntegrity()` and `.verifyDataIntegrity()` methods for verifying file / HDU integrity based on the stored checksums and/or data sums. The now useless checksum access methods of before have been deprecated and should be avoided.

 - [#476] `FitsInputStream` to calculate checksums for all bytes that are read/skipped, allowing checksum verification for stream-based inputs also.

 - [#475] Added `Header.getCard()` methods similar to existing `.findCard()`, but without changing the position at which new cards are added. The new method is now used more widely internally when interpreting headers. New `Header.prevCard()` method to complement existing `.nextCard()`, and `.seekHead()` / `.seekTail()` methods to set the position for new additions at the head or tail of the header space respectively.


### Changed

 - [#473] Updated `README` for proper checksum verification procedures.

 - [#472] New cleaner-looking logos for github icons / profile image.

 - Updated maven build and runtime dependencies to their latest releases.


## [1.18.0] - 2023-07-24

Feature release with compression fixes and many improvements.


### Fixed

 - [#377] Fixed use of `ZDITHER0` keyword to restore or record random generator seed when dithering is used in (de)compression.

 - [#349] Fixed tiled (de)compresssion of some quantized images via GZIP.

 - [#436] Fixed incorrect handling of `CONTINUE` keywords with no quoted string value (a regression since 1.16).

 - [#438] Conversion of variable length `float[]` or `double[]` arrays to complex was broken since forever. Now it works for the first time as expected.

 - [#455] Fixed incorrect variable-length complex array descriptors in binary tables.

 - [#445] Consistent String return for both fixed and variable-length string columns in binary tables.

 - [#456] Fixed compressed table tiling with tile sizes that do not divide the table rows.

 - [#462] Fixed incorrect `HCompress` `SCALE` parameter handling.


### Added

 - [#387] Added ability to stride while tiling.

 - [#356] Added ability to stream image cutouts.

 - [#400] Added ability to stream compressed image cutouts.

 - [#335] Added `NullDataHDU` for more intuitive support for header only HDUs with no associated data.

 - [#352] Added support for alternative `RandomAccess` immplementations.

 - [#183] Added support for `ComplexValue`-based column in `BinaryTable`

 - [#450] Added support for bit-based columns (FITS type `X`) in binary tables.

 - [#451] Added support for `null` (undefined) logical values for FITS logical columns in binary tables.

 - [#448] More user-friendly data access in `BinaryTable`, using Java boxing for scalar primitive entries, and automatic type conversion (both narrowing and widening) for singleton values when possible. Many new BinaryTable methods to make it easier to build and use binary tables.

 - [#437] Allow accessing binary table entries / rows directly from file in deferred read mode. The `README` for reading binary tables has been updated accordingly.

 - [#453] Allow defragmenting binary table heaps to expunge stale data, and to optimize heap area for best sequential read performance.

 - [#452] Simplified access to select tiles / regions of compressed data in both tile compressed tables and images.

 - [#178] Support for the Substring Array Convention. (However, we will prefer the use of the more generic `TDIM` over the convention for fixed-width string-based columns.)

 - [#458] Use `HIERARCH`-style keywords more easily within the library via the static methods of the `Hierarch` class.


### Changed

 - [#414] Significantly improved IO performance, now typically 2.5 to 4 times faster for images than prior releases and a whopping 25-50 times faster for reading streams than 1.15.2.

 - [#407] Improved dithering performance by calculating the random sequence only once, and more efficiently.

 - [#441] Significantly faster table data access and row additions.

 - [#207] Unified deferred read implementation for all `Data` classes.

 - [#395] Consistent behavior and method visibilities across `Data` subclasses.

 - [#434] Stop the automatic creation of random group HDUs from compatible table data. Instead create binary or ASCII tables as appropriate given the `FitsFactory` settings. The FITS standard discourages the use of random group HDUs going forward, and support should be typically limited to dealing with some older existing data files in that format. If there is ever a need to create new random group HDUs, you can do it explicitly via `RandomGroupsHDU.createFrom(Object[][])`.

 - [#446] Disable automatic (and inconsistent) detection of complex binary table data, when adding real-valued data columns to a binary table. Users should call `BinaryTable.setComplexColumn()` explicitly if pairwise real values should be converted to complex.

 - [#447] Check data consistency when adding or setting data in binary tables.

 - [#454] Do not use ASCII `NUL` (`0x00`) for padding between string array elements in binary tables, since it violates the FITS standard, in which an ASCII `NUL` terminates the entire string array entry.

 - [#131] Tables now support up to `Integer.MAX_VALUE` (~2-billion) rows regardless of entry size.

 - [#461] Improved handling of shared compression settings across option copies, and including validation

 - [#394] Various tweaks to `FitsDate` that should not affect user behavior.

 - [#420] Revised `PaddingException` API and seamless internal handling.

 - [#435] Header access methods in BasicHDU no longer remove significant leading spaces from strings.

 - [#426] No longer exposing mutable internal arrays to users, giving them copies instead.

 - [#429] Remove unnecessary IO synchronization in `FitsFile`, `FitsInputStream`, and `FitsOutputStream` classes.

 - [#424] Code cleanup -- Consistent source code formating etc.

 - [#160] Public user API javadoc now complete. It could be improved, but at least all packages, classes, and methods intentionally exposed to users are documented.

 - [#427] Online API documentation is restricted to public classes and methods only, as appropriate for users who want to read/write FITS files. Classes and methods intended for internal use are clearly indicated when these are exposed in the public. Contributors can still generate the full documentation locally if desired using maven.

 - Fully revised `README` (Getting Started guide) with better and more up-to-date examples.

 - [#402] Upgrade from JDK 11 to 17 for creating signed packages in the CI (but still in Java 8 compatibility mode).

 - Updated maven build and runtime dependencies to their latest releases.


### Deprecated

 - [#336] Deprecate unsafe methods that should only be used internally (if at all) and never by users.

 - [#209] `FitsUtil.reposition()` deprecated and changed to take FitsIO argument to match usage.

 - [#425] Deprecate hexadecimal values in headers. FITS does not support these, and you should not use them.

 - [#430] Deprecate `BigInteger` and `BigDecimal` values in headers. FITS does not support these, and you should not use them.


## [1.17.1] - 2023-03-15

Maintenance release with critical bug fixes.


### Fixed

 - [#368] Fixed first extension written as primary if primary HDU contained no data (affects 1.17.0).

 - [#367] Fixed incorrect checksum calculated directly from file in multi-HDU FITS, and other checksum fixes (affects 1.17.0).

 - [#341] Fixed occasional `NullPointerException` in tiled image compression resulting from incomplete initializiation of `TiledImageCompressionOperation`.


### Added

 - [#376] Upload coverage report to Coveralls.io also (forks need to set a repo token via the repo secret `COVERALLS_REPO_TOKEN` if want to enable reporting also).


### Changed

 - [#373] Fixed GitHub Actions site build error, by removing unused broken dependency from POM.


## [1.17.0] - 2022-09-11

Improved image compression, checksum support, and incremental writing


### Fixed

 - [#318] Fixed broken tile compression of non-square images.

 - [#318] `CompressedImageHDU.fromInageHDU()` now defaults to tiling by row if tile size is not explicitly specified, as per FITS specification and also for consistent behavior in higher dimensions.


### Added

 - [#319] Generalized tile compression for any-dimensional images based on the FITSIO convention.

 - [#323] New checksumming methods: `BasicHDU.calcChecksum()`, `.setChecksum()`, `.getStoredChecksum()`, and `.getStoredDataSum()`, `Data.calcChecksum()`, `Fits.calcChecksum(int)` and `.setChecksum(int)` -- as well as an extended static API for `FitsCheckSum`.

 - [#323] Added `Fits.rewrite()` to simplify re-writing the entire Fits objects, e.g. after updating checksums. The implementation is efficient in that it skips data segments in deferred read mode. `BasicHDU.rewrite()` is modified to make it efficient also.

 - [#283] User adjustable header comment alignment position via `Header.setCommentAlignPosition(int)` and checking via `.getCommentAlignPosition()`.

 - [#145] New `Fits.getHDU()` methods to select HDU from Fits by name (and version).

 - [#323] Added `Data.isDeferred()` method (defaulting to false) that can be used to check if a FITS Data object might be in deferred read mode, that is if it may not be fully loaded into RAM. Note, that while the method is properly implemented for the built-in data types of the library, it may not properly reflect the deferred status of external data implementations unless these override the method with something meaningful also.


### Changed

 - [#266] Safe incremental HDU writing via new `FitsOutput` interface, which is used to check whether HDUs should be set primary or not depending on where it is located in the output.

 - [#323] Simpler, faster, and more versatile FitsChecksum class, with support for incremental checksum updates, checksum retrieval, and checksum computations directly from files.

 - [#328] Checksum in-memory headers/data with less overhead through piped streams.

 - [#323] `Fits.setChecksum()`, `.setCheckSum(int)`, and `.calcChecksum(int)` compute checksum directly from the file, if possible, for deferred read data (i.e. data not currently loaded into RAM). This eliminates the need to keep potentially huge data volumes in RAM when computing or updating checksums in an existing FITS file.

 - [#323] `Fits.setChecksum()` will now checksum all HDUs, including those not already loaded from disk -- keeping data in deferred read mode if possible.

 - [#311] Duplicate header keys, during parsing, are repoted though separate logger instance from `Header`'s, with verbosity controlled via `Header.setParserWarningsEnabled(boolean)`.

 - [#292] Suppress repeated duplicate keyword warnings when parsing and improve checks for FITS standard violations. Added `Header.getDuplicateKeySet()` method to check which keywords have duplicates.

 - [#292] Replacing header keys logs a warning when existing value type is incompatible wth the newly associated standardized keyword.

 - [#292] Creation of header entries with standardized keywords logs a warning if associated value type is incompatible with the keyword.

 - [#292] Improved header card ordering implementation.

 - [#74] New logo.


## [1.16.1] - 2022-03-21

Maintenance release with bug fixes.


### Fixed

 - [#252] Fixed broken Java 8 compatibility of 1.16.0 due to Java compiler flags in POM. Note, after the change the build itself will require Java 9 or later!

 - [#243] Fixed potential unchecked `null` in `BinaryTableHDU`.


### Changed

 - [#257] No `Logger` warnings about duplicate header cards if Header parser warnings are disabled.

 - [#210] Added default `ArrayDataInput` / `ArrayDataOutput` implementations.

 - [#229] Removed runtime dependency on `javax.annotation-api`.

 - Mostly automated updates of dependencies.


## [1.16.0] - 2021-12-13

Compliance to FITS 4.0 standard, plus many more fixes and improvements.


### Fixed

 - [#171] Prevent the creation of invalid header entries from code, by throwing informative runtime exceptions. New runtime exception classes `HierarchNotEnabledException`, `LongStringsNotEnabledException`, `LongValueException`, `UnclosedQuoteException` are used to report when illegal action was pre-empted relating to FITS headers.

 - [#165] Prevent creating header cards with `NaN` and `Infinite` values. The FITS standard does not support these.

 - [#187] No `EOFException` is thrown when skipping beyond the end of file, since it should be allowed in random access mode.

 - [#186] Consistent handling of logical (`true`/`false`) values in FITS binary tables, including newly added support for `null` (or undefined) values also as per FITS standard.

 - [#184] In prior versions `char[]` arrays in binary tables were written as 16-bit Unicode and read back as `short[]` integers. FITS recognises only ASCII character arrays with 1-byte per character. A new `FitsFactory.setUseUnicodeChars(boolean)` option can toggle compliance to the FITS standard for `char[]` arrays. However, the misconceived prior behavior remains the default to maintain back compatibility until the next major release.

 - [#153] No more `Logger` warnings on multiple `CONTINUE` keywords, tolerant `HIERARCH` parsing, and other small fixes.

 - [#135] Fix management of sub-seconds in `FitsDate`

 - [#130] Check for and reject non-ASCII or non-printable characters in headers. The FITS standard allows only ASCII characters in the range of `0x20` to `0x7E` in the headers. The new static method `HeaderCard.sanitize(String)` is available to users to replace characters outside of the supported range with `?`.

 - [#123] Minor fixes prior to release

 - [#162] Revised when exceptions are thrown, and they are made more informative by providing more essential details and traceable causes.

 - [#159] `HIERARCH` header cards are now written to conform to (C)FITSIO specification, which requires a space before '='. While the `HIERARCH` convention itself does not specify the extra space, it certainly allows for it, and with the change our FITS files shall be more conformant to, and readable, by yet another widely used library.

 - [#158] Check for `markSupported()` when attempting to use `mark()` or `reset()` methods in `ArrayDataInput`, and throw an appropriate runtime exception if the methods are not supported by the implementation.

 - [#156] Fixed issues with handling of single quotes as part of user-supplied strings.

 - [#143] `I10` format ASCII tables are parsed as 32-bit `int[]` by default (for back compatibility), unless `TLMIN`/`TLMAX` or `TDMIN`/`TDMAX` header entries indicate a more extended range. Added new `AsciiTable(Header, boolean)` constructor to optionally change the preference to read `I10` ASCII table data as 64-bit `long[]` columns.

 - Various smaller fixes and improvements throughout, increased unit test coverage, and more comprehensive unit tests.

 - A lot of the Javadoc API documentation has been revised and improved.


### Added

 - [#177] Added support for preallocated blank header space, as per FITS 4.0 standard. via `Header.ensureCardSpace(int)` and `Header.getMinimumSize()`. Headers remain rewritable in-place as long as they don't exceed their original size in the file.

 - [#172] Added support for complex values in FITS headers, as specified by the FITS standard, via new `ComplexValue` class.

 - [#167] Added support for header integers in hexadecimal format, as specified by the FITS standard, e.g. via `addHexValue(...)` and `getHexValue(...)` methods in both `Header` and `HeaderCard` classes.

 - [#120] Added optional support for using `D` instead of `E` as the exponent in decimal representations (via `FitsFactory.setUseExponentD(boolean)` setting), as specified by the FITS standard.

 - [#182] Replace fragmented `PrimitiveType...` hierarchy with a more aptly named one-stop `ElementType` class. The old hierarchy is also available, albeit in deprecated form.

 - [#191] Type safe `BITPIX` values via new `Bitpix` enum providing a restricted set. The unsafe `BITPIX` methods have been deprecated for removal in a future release.

 - [#175] Added new `Header.setParserWarningsEnabled(boolean)` option to log FITS standard violations when reading (3rd party) headers.

 - [#138] `FitsDate.equals()` / `hashCode()` / `compareTo()` implementations.


### Changed

 - [#197] This release contains numerous API changes and additions. While the source code is generally back-compatible with previous versions of this library for compiling, some method signatures have changed, and as a result the JAR should not be used as a drop-in replacement for applications that were compiled against earlier versions. To use version 1.16.0 of this library you should always compile your application against it.

 - [#161] Long strings enabled by default (FITS 4.0 standard).

 - [#195] Permissive default `FitsFactory` settings: error-free reading of some flawed 3rd party files by default (as long as they can be made sense of). However, issued encountered with 3rd party FITS files are logged so they can be inspected. Added new `FitsFactory.setDefaults()` method to restore default settings more easily.

 - [#125] Set `FitsFactory.useHierarch(true)` by default. `HIERARCH` style keys are written upper-case only by default, but case-sensitive support can also be enabled via a call to the `setCaseSensitive(boolean)` method of the `IHierarchKeyFormatter` instance used by `FitsFactory`.

 - [#169] More predictable explicit precision control for header decimals. The same number of decimal places are shown after the leading figure regardless whether fixed-decimal or scientific (exponential) notation is used.

 - [#173] Fully preserve long comments for string values, including internal spaces in the comment, using the now standard long string convention.

 - [#170] Simpler, better methods for adding creating comment and history entries in headers, such as via `Header.insertComment(String)` or `.insertHistory(String)`, or via `HeaderCard.createCommentCard(String)` or `.createHistoryCard(String)`.

 - [#170] `Header.addValue(...)` and `Header.insert...(...)` methods now return the newly created `HeaderCard` objects for convenience.

 - [#121] More predictable header card ordering when editing headers, both directly or indirectly via an iterator.

 - [#188] `FitsHeap` access made a lot more efficient with true random access.

 - [#164] Source code updated for Java 8, with diamond operators and try-with-resources used throughout as appropriate.

 - [#190] Changed to generated `serialVersionUIDs` from `1L` for classes that require it.
 

### Deprecated

 - [#192] New FITS IO class hierarchies for better layering and separation of functionality. Standard IO functions (for reading, writing, positioning, and skipping) now conform to their canonical contracts in the core Java API. The messy old IO API is also supported, though deprecated, to provide back compatibility until the next major release. The new IO classes are also 2 to 3 times faster than before.

 - Deprecated classes and methods that (a) were exposed in the public API even though they should not have been, (b) had names that poorly reflected their function, (c) were poorly conceived/designed in the first place, and/or (d) were prone to misuse with unpredictable results. The deprecated API remains supported nonetheless, and slated for removal in the next major release (2.0) only.

## [1.15.2] - 2017-04-28

Maintenance release with bug fixes.


### Fixed

 - [#112] `ImageHDU` tiler corrupts values after 2GB worth of data bug fixed.

 - [#108] Non standard `BITPIX` allowed during de/compression.

 - [#107] Add tiler support for `ImageHDU` from uncompressing a `CompressedImageHDU`?

 - [#106] Remove redundant spaces in `HIERARCH` keys.

 - [#105] Fix integer overflow in case of negative values in combination with a defined blank value of `Integer.MIN_VANUE`.

 - [#104] make the worker threads deamons so they do not hold of a shutdown enhancement.

 - [#98] Update outdated documentation in introduction enhancement, thanks to MaxNoe.

 - [#90] Fix Reading `boolean` arrays with `getColumn` bug.


### Added

 - [#113] `Header` can be controlled to specify the header card order.


### Changed

 - Maintenance release with bug fixes.


## [1.15.1] - 2016-08-19

Maintenance release with bug fixes.


### Fixed

 - [#102] Comment type header cards where not protected against to long comments, that can result in corrupted headers.

 - [#101] Introduction document verified and corrected kind thanks to Maximilian Nöthe.


### Changed

 - Maintenance release with bug fixes.


## [1.15.0] - 2016-08-07

Table compression activated.


### Added

 - [#61] Binary table compression now fully supported.

 - [#70] The dummy compression algorithm `NOCOMPRESS` is now supported.


### Changed

 - Binary table compression and tiling are now fully supported by nom-tam-fits. An API for easy handling of compressed tables is now provided.

 - [#96] Multiple code quality fixes, provided by various developers.


## [1.14.3] - 2016-06-05

Maintenance release with minor bug fixes.


### Changed

 - Maintenance release with bug fixes.

 - [#92] Removal of redundent attribute `rowSize`. Attention here the public api has a minor change, the `deleteColumns` in the `ColumnTable` does not return an `int` anymore.

 - [#91] Fix for a bug in encurling the multidim arrays of the `BinaryTable` with variable length columns.


## [1.14.2] - 2016-03-11

Maintenance release with minor bug fixes and enhancements.


### Changed

 - Maintenance release with important bug fixes and the restoration of java 6 support.

 - [#84] `Fits` does not handle comments that start with 8 blank characters correctly when reading/writing/reading bug.

 - [#80] Restored Java 6 compatibility.


## [1.14.1] - 2016-02-24

Maintenance release with minor bug fixes and enhancements.


### Fixed

 - [#76] In case of long strings the difference between a null comment and an empty string was not detected correctly.


### Added

 - [#60] Image compression support for the null pixel mask, this allows correct `NaN` with the use of lossy compression's.


### Changed

 - Maintenance release with minor bug fixes and enhancements.

 - [#79] Important note for all users, since 1.13.0 a bug is fixed in the table behavior. This can cause problems for users expecting the "buggy" result. See the issue on github for more details.

 - [#77] Since a approximately 1.12.0 nom-tam-fits uses `java.util.logging` for all logs, the details what and where to log to can therefore be configured freely.


## [1.14.0] - 2016-01-10

full Image compression support


### Fixed

 - [#26] Wrong checksum calculation corrected.

 - [#54] Some problems with data segments that are bigger than 2GB corrected.

 - [#62] Header parsing performance optimization.

 - [#68] Comment style cards with a empty key value can now be used multiple times.


### Added

 - [#48] Added a [de]compression API supporting all compression methods in the proposed updates to the FITS standard.

 - [#72] The formatting of hierarch card keys can mow be be controlled. Two formats are provided.


### Changed

 - Image compression and tiling are now fully supported by nom-tam-fits. A 100% Java implementation of the compression libraries available in cfitsio was implemented. An API for easy handling of compressed images is now provided. Support for binary table compression and the `NULL_PIXEL_MASK` features is anticipated in the next release.

 - When [de]compressing all available CPU's are automatically utilized.

 - Internal compression allows FITS files to be created where the data are efficiently stored, but the metadata is still easily accessible. The tiling of images is particularly critical for supporting efficient access to subsets of very large images. A user can easily access only the tiles that overlap the region of interest and can skip data not of interest. While some skipping might be possible with uncompressed FITS files (i.e., read only the rows overlapping the desired subset), internal tiles can be much more efficient when the image is substantially larger than the subset. Most compression algorithms interfere with the ability to skip uninteresting data, but tiles are compressed independently, so users can benefit both from the compression and the selection of only a subset of the image.

 - [#68] Alignment of hierarch headercard values deactivated.


## [1.13.1] - 2015-08-21

Maintenance release with fixes for hierarch/longstring and rewrite bugs


### Fixed

 - [#46] After the correction of #44 the padding calculation of the fits header was wrong, now the calculation is consistent.

 - [#44] Improved the calculation of the number of cards to be used for a longstring in case of a hierarch cards because it was wrong when some special string lengths where used. Now rewriting is useing the new calculation to see if the header fits in place.

 - [#43] More variants of the hierarch keywords as in #16.

 - [#16] More variants of the hierarch keywords allowed (lowercase and dot), but writing will convert the keywords back to the standard.


### Changed

 - Maintenance release with fixes for hierarch/longstring and rewrite bugs.


## [1.13.0] - 2015-07-17

This is a stability release, before the new fits standard will be implemented in version 2.0


### Fixed

 - [#24] Fixed handling of binary tables built from an empty state row by row. Fixed coupling of binary tables and the FITS heap to allow copying of binary tables using the internal `ColumnTable` representation.

 - [#20] Compression dependecy to apache compression is now optional again.

 - [#12] When reading/writing the same card the comment moved one blank to the right.

 - [#29] All javadoc's are now java-8 compatible and produce no warnings.


### Removed

 - [#23] Tile compression, will be implemented from scratch in 2.0 and is not yet available in 1.13.0


### Changed

 - Major updates to the util package including a set of routines for efficient copyying of arrays of all types. Some of the special FITS values are collected in the `FitsIO` class. New buffering utility classes have also been created. Some methods that were public in the util package but which were not used in the FITS library have been removed. The logging is now using `java.util.logging`, no standard out or stanard error is used anymore.

 - Added utilty class for updating checksums

 - Added examples in utilities package for how to use new `Header` enumerations.

 - [#35] Builder pattern for the creation of cards introduced.

 - [#23] Reorganized compression and added internal compression package.

 - [#29] Unit tests extended to cover 92% of the library .

 - [#17] Longstring support was improved and longer comments are now supported.

 - [#15] Support for biginteger and bigdecimal.

 - [#14] Generic detection of the value type of a card.

 - [#7] Insert a header card at a specific position.

 - [#36] All internally used keyword references are now enumerations.

 - [#37] Comment mapping is now moved to the standard keyword enumeration.

 - [#21] The settings of `FitsFactory` are now changeable to thread local specific settings.


## [1.12.0] - 2015-02-20


### Added

 - Enumerations where added for ~1000 more or less `Standard` fits headers are now included (making compile references to headers possible).

 - Moved the sources to github and the artifacts to the central repo.

 - Moved the license to maven as build system, incl reorganisation of the project. tests are no included in the published jars any more.

 - Creation of a project site on github with issue management.

 - Richard van Nieuwenhoven joined the team.


### Changed

 - Moved the license to the official unlicensed.

 - Java formatting/licence updating is now done by the maven plug ins.

 - Moved the code to Java 1.6 and `@Override` is now consistent.


## [1.11.1] - 2014-07-07


### Changed

 - Debuggging statements inadverently left in V111.0 were removed.


## [1.11.0] - 2013-06-07


### Fixed

 - Fixed error in handling of strings with non-terminated quotes so that these don't crash program (thanks to Kevin McAbee)


### Added

 - The source code JAR (`fits_src.jar`) includes a number of new classes for which the corresponding class files are not included in `fits.jar`. These classes are pre-alpha versions of support for tile compressed data that is being developed. Interested Users may take a look at these, but they definitely are not expected to work today. Support for Rice, Gzip and HCompress compression is expected.


### Changed

 - Deferred allocation of memory when creating `FitsHeap`s (thanks to Vincenzo Forchi, ESO).

 - Fixed error in getting size of associated data for dummy headers in `Header.getDataSize()`. This could return 1 rather than 0 if `NAXIS=0` was used to signal a dummy. This doesn't affect data when read normally (as full HDUs) since the `ImageData` class does the computation correctly. (Thanks to Pat Dowler, CADC)


## [1.10.0] - 2012-10-25


### Changed

 - No functional changes to the FITS code are included in this release. All internal documentation has been updated to reflect that this library is now available in the public domain.


## [1.08.1] - 2012-02-01


### Fixed

 - Fixed error in the writing of ASCII table columns where all of the elements of the table were null or 0 length strings. Previously we would write a column with `TFORM` `A0`. This is not supported by CFITSIO and since it is not valid Fortran is of dubious legality for FITS. Such columns are now written with A1 (issue noted by Jason Weiss, UCLA).

 - `AsciiTable` did not check if columns were of a valid type (if the `FitsFactory` methods were used, then a `BinaryTable` would be written, but a user can explicitly instantiate an `AsciiTable`). A `FitsException` is now returned if a column other than a `String`, `double`, `int` or `long` array is used


## [1.07.0] - 2012-01-20


### Added

 - Added `boolean hadDuplicates()` and `List getDuplicates()` methods to `Header` to allow users to track if there were duplicate keywords in a header.


## [1.07.0] - 2011-11-11


### Fixed

 - Fixed the order of the `EXTEND` keyword to follow the `NAXISn` keywords when it is specified. This constraint on the `EXTEND` keyword is no longer required in the latest version of the standard, but it doesn't hurt anything and may make the file more acceptable to some readers.

 - Fixed JavaDoc errors for a number of files.

 - Fixed a bug in `Header.rewriteable()` related to the same issue as the original size.


### Changed

 - This release contains some new features suggested by Booth Hartley (IPAC) who supplied modified code including:

 - Allow a FITS file to have invalid data after a valid FITS HDU. User can call `FitsFactory.setAllowTerminalJunk(true)` to enable this. The Fits object will return all valid HDUs. Note that whatever follows the valid FITS data must start with something that is clearly not FITs. This includes modifications to `FitsFactory` and `Header`.

 - Allow users to find original size of headers as they were read from some source file. The library throws away duplicate key values, so that the number of header cards in the header as read may be smaller than the original data. The `getOriginalSize()` gets the original size of the header in bytes. A `resetOriginalSize()` allows the user to tell the library that this header has been updated on disk and now has the same number of records as internally.

 - Updated `Fits.setChecksum` so that it will now set both the `CHECKSUM` and `DATASUM` keywords.

 - Added tests for the new capabilities above and updated the checksum test.


## [1.06.0] - 2011-05-23


### Fixed

 - A bug in the `UndefinedData` class was detected Vincenzo Forchi and has been corrected.


### Changed

 - Substantial reworking of compression to accommodate BZIP2 compression. The Apache Bzip library is used or since this is very slow, the user can specify a local command to do the decompression using the `BZIP_DECOMPRESSOR` environment variable. This is assumed to require a '-' argument which is added if not supplied by the user. The decompressor should act as a filter between standard input and output.

 - User compression flags are now completely ignored and the compression and the compression is determined entirely by the content of the stream. The Apache library will be needed in the classpath to accommodate BZIP2 inputs if the user does not supply the `BZIP_DECOMPRESSOR`.

 - Adding additional compression methods should be much easier and may only involve adding a couple of lines in the FitsUtil.decompress function if a decompressor class is available.

 - One subtle consequence of how compression is now handled is that there is no advantage for users to create their own `BufferedDataInputStream`s. Users should just provide a standard input stream and allow the FITS library to wrap it in a `BufferedDataInputStream`.

 - The `nom.tam.util.AsciiFuncs` class now handles ASCII encoding to more cleanly separate this functionality from the FITS library and to enable Java 1.5 compatibitity. (Suggested by changes of L.Bourges) Other V1.5 incompatiblities removed.

 - The `HeaderCommentsMap` class is now provided to enable users to control the comments that are generated in system generated header cards. The map is initialized to values that should be the same as the current defaults. This should allow users to emulate the comments of other packages.

 - All Java code has been passed through NetBeans formatter so that it should have a more uniform appearance.


## [1.05.1] - 2011-02-16


### Fixed

 - The implementation of long string values was incorrect, using `COMMENT` rather than `CONTINUE` cards.noted originally by V. Forchi.

 - The placement of the header cursor after opening a primary array was such that unless the user took explicit action to move the cursor, new header records would be written before the `EXTEND` keyword which is a violation of the FITS standard (although it would not affect the operations of this library). The library now leaves the cursor just after the `EXTEND` keyword where new keywords are legal. It's still possible for users to write an illegal header but now it requires at least a little effort on their part.noted originally by V. Forchi.


### Changed

 - This build procedure for FITS library has been changed. The library is now stored as a NetBeans project and the standard NetBeans build script has been modified to generate the `fits.jar` and `fits_src.jar`. The names of a number of the test procedures have been slightly modified (`XXXTester` -> `XXXTest`) and test data are included in the class jar file.


## [1.05.0] - 2010-12-12


### Changed

 - Adding methods to allow finer control of the placement of metadata records for columns of FITS tables. This could previously be done using `Cursor`s, but the `TableHDU.setTableMeta()` methods now allow these to be specified more directly. This involves changes only to `TableHDU`. Usage is illustrated in the test method `BinaryTableTest.columnMetaTest`. By Laurent Bourges.

 - Adding more rigor to the transformation between bytes and strings and fixing a bug in the handling of strings with embedded nuls. According to the standard an embedded null should terminate an string in a binary table by Laurent Bourges. The standard also precludes other non-printing characters from strings. This has been ignored previously, but there is now a method `FitsFactory.setCheckAsciiString(boolean flag)` which can be called to turn on checking. A warning will be issued and non-printing characters will be converted to spaces if this flag is set. Only a single warning will be issued regardless of the number of invalid characters are seen. There are changes in a number of classes where the conversions occur to ensure that the ASCII charset is used. How these changes work is illustrated in `BinaryTableTest.specialStringsTest`. By Laurent Bourges.

 - Handling fixed and variable length, single and double precision complex data. The library uses a `float[2]` or `double[2]` for a complex scalar. This is mostly bug fixes to existing code and changes are only in BinaryTable and BinaryTableHDU. The method `BinaryTableHDU.setComplexColumn()` allows the user to tell the FITS writer that a field which otherwise would be treated as a float or double array (with most rapidly varying dimensionality of 2) should be treated as complex. The internal data representations are identical. Variable length complex data will be found automatically if number of elements actually varies. A variable length complex column is a 3-D float or double array where the last dimension (for Java) is always 2, the first dimension is the number of rows in the table and the middle dimension is the number of complex numbers in the row and may vary from row to row. Other variable length columns are represented as 2-D arrays, where the first index points to the row, and the second index enumerates the elements in the row. Use of complex columns is illustrated in `BinaryTableTest` in the routines `testSimpleComplex` (fixed columns), `testVar` (variable length columns), and `buildByColumn` and `buildByRow` where columns and rows containing complex numbers are added to existing tables. By Laurent Bourges.

 - Changing the null HDU created when a table is to be written without a prior image to use a vector with dimensionality 0 rather than a one-dimensional vector with a dimension of 0. I.e, use `NAXIS=0` rather than `NAXIS=1`, `NAXIS1=0`. By Laurent Bourges.

 - Consolidating the writing of padding at the end of FITS elements into the `FitsUtil.pad` methods.

 - Adding the `reset()` method to the `FitsElement` interface. This attempts to reset the Fits input stream pointer to the beginning of the element. It does not throw exceptions but will return false if not successful. This is intended to make it easier for user who wish to use low-level I/O to read FITS data, by allowing them to position the stream to the beginning of the data they are interested in.

 - Changed `FitsUtil.HDUFactory(Object x)` to accept a `Header` object as well as the various kinds of data inputs.

 - Provided a method in `BinaryTable` to get back the ModelRow array. This makes is easier for users to do low level I/O in binary tables. An `ArrayDataInput` object will read a row of the table, given the result of `getModelRow()`.

 - Added a `getColumns()` method to `TableHDU`. This returns an `Object[]` array where each entry is the result of `getColumn(n)`


## [1.04.0] - 2009-12-24


### Fixed

 - A bug in the processing of keyword values with embedded apostrophes was fixed. Apostrophe's were properly doubled in encoding but the doubling was left when the values were read.

 - A potential bug in the processing of headers discovered by Mark Taylor was fixed.


### Changed

 - Support for the `HEASARC` long strings convention has been added. This affects only the `Header` class. Two new public static methods have been added. `setLongStringsEnabled(boolean)` allows the use to enable/disable the handling of long strings. `getLongStringsEnabled()` returns the current setting. By default long strings are disabled. The convention is enabled automatically whenever a header is read which has the `LONGSTRN` keyword is read. It is not disabled if subsequent headers are read which do not have this keyword. The `addValue(String,String,String)`, `getStringValue(String)` and `removeCard(String)` methods are affected, allowing the user to set, read and delete long string values. The library does NOT ensure that users do not interpolate new keywords or comments inside the card sequence that is used to store the long string value.


## [1.03.1] - 2009-07-27


### Added

 - The implementation of the `FitsUtil.byteArrayToStrings` method was changed so that trimmed space from strings can be cleaned up more efficiently. Change suggested by J.C. Segovia (ESA). There should be no effect -- other than memory usage -- on external programs.


### Changed

 - Users might want to note that when reading string values in binary tables, both leading and trailing spaces are trimmed from the string values.


## [1.03.0] - 2009-07-22


### Fixed

 - A bug in the new `PaddingException` was fixed which allows use of Tilers with truncated Image HDUs.


### Added

 - This release adds further support for large datasets where the size of an HDU may exceed 2GB. In `ArrayDataInput` (and the `BufferedFile` and `BufferedDataInputStream` that implement it) `int skipBytes(int)` method of `java.io.DataInput` is now overloaded with `long skipBytes(long)`. In `ArrayFuncs` `int computeSize(Object)` method is augmented with `long computeLSize(Object)`. It was not possible to use the same name here since the argument type is the same. Similarly int `nElements(Object)` is now matched with long `nLElements(Object)`. These changes should not affect current usage of the existing methods. References to `skipBytes` and `computeSize` in the FITS classes now take advantage of these new methods. While these changes increase the support of the library for large datasets, there are still a number of restictions that arise from Java's limit that array indices must be ints. E.g., no single dimension can exceed 2 GB, and the total size of the heap for a given binary table HDU cannot exceed 2 GB. ASCII tables may also be limited to 2 GB in some circumstances. Files which exceed these limits may be readable using line by line approaches, but users will need to use the library at a much lower level.

 - The `Header.read()` method may now throw an `IOException` in circumstances where it would previously throw an Error. It probably should throw a `FitsException`, but that was not in the signature and might have broken existing programs.


### Changed

 - Some obsolete comments indicating that `BITPIX=64` was an extension of FITS were deleted. FITS has officially supported longs for a fair number of years now.

 - The regression tests have been augmented to test the new features, but users should note that the new BigFileTester test takes a very long time to run. E.g., on the primary development machine this takes 240 seconds while all of the other tests finish in just a few seconds. The time is simply the time it takes to write a file of known content that is more than 2 GB in size.


## [1.02.0] - 2009-07-08


### Changed

 - ASCII tables with data fields that were blank filled were not being handled properly. According to the FITS standards, numeric fields where the FITS table has blanks are to be treated as containing 0. A parsing error was being returned. The `getInt`, `getLong`, and `getDouble` methods in `ByteParser` were changed to acoommodate this case (`getFloat` simply calls `getDouble`). suggested by L. Michel.

 - A new exception, `PaddingException`, which inherits from `FitsException` has been added. This exception is thrown when an otherwise valid HDU is not properly padded to the next 2880 byte boundary. The exception class has a `getTruncatedHDU` method which allows the user to get the information in the truncated HDU. In addition to the new class changes were made in `BinaryTable`, `AsciiTable`, `UndefinedData`, `ImageData` and `RandomGroupsData` to throw the exception at the appropriate time. The main Fits method was also updated so that when its `readHDU()` method is being used, the notional header that is given to the truncated HDU in the `Data` classes is replaced by the actual header. If a user wishes to ignore padding exceptions, then a FITS file may be read using the following idiom in the new `nom.tam.fits.test.PaddingTester` to see a complete example of this idiom.


## [1.01.0] - 2009-06-24


### Fixed

 - A bug noted by Thomas Granzer in the handling of `HIERARCH` keyword values was also corrected.


### Changed

 - A number of changes were implemented to handle large FITS files more gracefully and to correct bugs associated with large files. This includes a change to the method `Data.getTrueSize()`. This method was public only for the `BinaryTable` data type and previously returned an `int`. It now returns a `long`. User programs which called this method will need to be recompiled. Specific bugs were noted by Javier Diaz and Juan Carlos Segovia. Note that the program may still fail on very large files but it should give more informative error messages when it does so


## [1.00.2] - 2009-03-09


### Fixed

 - Fixed bug where reading a table by rows caused reading a subsequent HDU to fail. Added tests to `BinaryTableTester` and `HeaderCardTester`.


## [1.00.1] - 2009-02-19


### Fixed

 - Fixed bug where exponential notation in FITS header keywords used `e` rather than `E` (noted by Javier Diaz)


## [1.00.0.1] - 2008-07-11


### Added

 - The major chage to this release is support for `.Z` compressed images. A problem reading past the end of files in normal FITS processing was included in the 1.0 release and was fixed (7/11/08). The earlier jars were overwritten. The problem shows up in the regression test suite.


## [1.00.0] - 2008-06-10


### Fixed

 - Bug fix to `BinaryTable` by A. Kovacs


### Added

 - The major chage to this release is support for `.Z` compressed images. This is implemented by using the uncompress command which must be in the user's execution path.


### Changed

 - There is much more dynamic checking of the magic number of inputs to determine whether the input to the FITS constructor is compressed or not, and if compressed what the compression type is. This can still be confused but in many cases it will get the compression right regardless of what the user specifies. Future versions may completely ignore the user specified compression flag.


## [0.99.3] - 2007-12-04


### Fixed

 - Binary table handling of 1 character strings.


## [0.99.5] - 2007-12-04


### Fixed

 - Additional fixes for zero length and null strings

 - Fix to handling of Strings in Binary tables (A. Kovacs)


### Changed

 - Added Support HTTPS, FTP and FILE URLs

 - Added `Fits(String,compressed)` constructor

 - Made some of the methods in `FitsFactory` public.

 - Added `getRawElement` method to Binary table.

 - Changed handling of `double` values in header so that they all fit into the fixed format. In rare circumstances this may result in a loss of precision.


## [0.99.3] - 2006-12-21


### Changed

 - Additional changes to handle `null` and zero length strings.


## [0.99.2] - 2006-12-15


### Fixed

 - `AsciiTable`: Setting a row, column or element de-nulls any elements that were set to `null`. Fixed offsets in columns after column was deleted.

 - `FitsUtil`: Fixed bug in `maxLength` which looked for nulls in the array pointer rather than the individual strings. Added check for nulls in `stringsToByteArray`

 - `HeaderCard`: Truncated String in one argument constructor to a maximum of 80 characters.

 - `BinaryTable`: Fixed handling of columns with 0 width (e.g., 0 length strings, or arrays of 0 length.

 - `ColumnTable`: Fixed handling of columns with 0 width.


### Added

 - `ArrayFuncs`: Added `arrayEquals()` methods which allow comparison of arrays of arbitrary dimensionality. Used extensively in the updated test classes.


### Changed

 - Moved code to use subversion repository and Ant compile scripts.

 - Major transformations of all test code to use Junit and automated checking rather than comparing print outs.

 - `nom.tam.fits.utilities` package created and `FitsCopy` and `FitsReader` classes were moved there.

 - A few test classes, e.g., `BigImage` and `RMFUpdTest` were deleted and their functions subsumed into the other tests.

 - Test routines now considered standard part of library. There are not separate JARs for the test routines.

 - Note that the test routines use Annotations and may not compile with versions of Java prior to 1.5.


## [0.99.1] - 2006-07-29


### Fixed

 - A number of errors in the handling of variable length arrays were fixed. These were pointed out by Guillame Belanger. This included changes to `util.ColumnTable` but mostly `BinaryTable` and `FitsHeap`.


### Changed

 - Added new methods to delete rows and columns from both binary and ASCII tables. There are changes to many of the table classes including `util/ColumnTable`. These changes were suggested by row deletion code written by R. Mathar, but the actual implementation is entirely independent and errors are handled somewhat differently than in his code. There are `deleteColumns` and `deleteRows` methods in `TableHDU` that delete either a specified range or all tables or columns after (and including) the one specified.

 - The `util.HashedList` implementation has been completely revised. It now uses a `HashedMap` for keyed access and an `ArrayList` for sequential access. It no longer implements a simple but custom list structure. The public interface was not significantly changed.

 - `Header` now sorts keywords before a header is written to ensure that required keywords come where they need to be. Previously users needed to work to make sure that they wrote required keywords in the right location in the header. A new class, `HeaderOrder`, is used.

 - A number of changes mostly to `BinaryTable` or documentation in other routines suggested by R. Mathar.


## [0.99.0] - 2006-06-23


### Fixed

 - Corrected bug in writing a binary table when the read of that table had been deferred.


### Added

 - Added support for checksums. Use the `setChecksum` methods in the FITS class to add checksums to FITS HDUs. The static method `setChecksum(HDU)` adds a checksum to a given HDU. The instance method `setChecksum()` adds checksums to all HDUs in the file. Note that setting the checksum should be the last step before writing the file since any manipulation of the file is likely to invalidate the checksum. (This code was contributed by R.J. Mathar, Leiden University).

 - Changed handling of 1-d arrays with a single element so that they can be distinguished from scalar values. No `TDIM` will be be created for scalar columns, and a `TDIMn = '(1)'` will give an array rather than a scalar value. (Suggested by Jorgo Bakker. ESA) For data written using the previous version of the FITS library, this may cause problems when the data is read with the new version, since the type of the returned column will be different.

 - When checking if a file is compressed, the actual content of the file will be used if possible rather than the name (Suggested by Laurent Michel, CDS)

 - The `ArrayFuncs.newInstance` method now accepts an dimension array of length 0 and returns a 1-d array of length 1 to emulate a scalar.


### Changed

 - The three packages, `nom.tam.fits`, `nom.tam.util` and `nom.tam.image` have been combined into a single JAR file for the convenience of the user.

 - The code used to support `TFORMn = 'xNNN'` where the array dimension followed rather than preceded the format type. This has been deleted (Suggested by Laurent Michel, CDS)

 - Zero-length string values should now be allowed as header keyword values (Bug noted by Fred Romelfanger, ST ScI and Jorgo Bakker, ESA).

 - The `addLine` methods in `Header` are now public rather than protected.

 - If the `Fits.write()` method is called using a `BufferedFile`, then the size of the file is truncated at the end of the write. Otherwise if the FITS data was being written into a previously existing file of greater length, there would be extra bytes at the end of the file. This is still possible if the user uses the write methods for individual constituents of the FITS object.


## [0.97] - 2003-11-01


### Fixed

 - Version 0.97 corrects several bugs in the handling header keywords and ASCII tables and Images.

 - The handling of the `EXTEND` keyword has been made consistent with FITS standards

 - ASCII tables are first read to an intermediate byte buffer and then parsed as needed. Bugs where this buffer was being deleted at inappropriate times, or left undeleted when it was invalid were fixed. This should fix errors when `AsciiTable`s are read from non-seekable sources. This should slightly speed up most access to ASCII tables

 - In certain circumstances an `Image` would not be properly initialized before it was to be written

 - The routines `Header`, `HeaderCard`, `ImageData` and `AsciiTableData` where modified in this release


### Added

 - The `getChannel` method was added to `BufferedFile`


### Changed

 - The `HeaderCard` class now has constructor with the signature `(String,String,boolean)` which may be used to generate either a comment style card with the keyword and value given, or a card with a `null` value


## [0.96] - 2003-03-20


### Fixed

 - A bug in the creation of ASCII Table Headers was fixed. Some of the header cards in the header were being inserted as if they were comments, allowing multiple copies to be generated. This was also possible when a Header was created from an array of strings.


### Changed

 - The handling of `PCOUNT`, `GCOUNT` and `EXTEND` keywords was changed in images so that the first two are only generated for extensions and the first only for primary HDU's.


## [0.93] - 2001-01-01


### Fixed

 - Several bugs relating to null images were corrected. (Thanks to Jens Knudstrup) (`ImageData`)

 - The handling of EOF conditions in array reads in the `BufferedFile` and `BufferedDataInputStream` classes was made consistent with the behavior of `java.io` classes reading byte arrays

 - Several bug fixes implemented by Alan Brighton (and already fixed in the Jsky distribution) were incorporated

 - All references to the `java.lang.reflect.Array.newInstance()` methods were modified to use new methods with the same signature in `ArrayFuncs`. These new methods throw an `OutOfMemory` exception when an array cannot be created. The JVM methods seem -- in contradiction to the documentation -- to simply return `null`. Previously the program could mysteriously crash when used to read large files, when the null in a dynamic allocation was eventually dereferenced


### Added

 - The `HeaderCard` class has been modified to handle The `HIERARCH` keyword convention. The `FitsFactory` now has methods `set`/`getUseHierarch` to enable/disable this processing

 - A new interface `FitsElement` has been added which is implemented by the `BasicHDU`, `Header`, `Data` and `FitsHeap` classes. It enables users to more easily deal with FITS data at the byte level. There is also a public method `getDataSize` in `Header` to get the size in bytes of the associated data element including padding. The `FitsHeap` class has been made public


## [0.92] - 2000-10-12


### Changed

 - `BinaryTable` Fixed bug initializing `BinaryTable`s read from streams (rather than files)


## [0.91] - 1996-01-02


### Changed

 - `FitsDate`: added `getFitsDateString`

 - `Header`: `FitsDate`: made several methods public

 - added checking for initial keywords before write

 - `BinaryTable`: removed `TDIM` keywords for variable length columns

 - `BinaryTable`: fixed bug that made `BinaryTable(Object[][])` constructor unusable

 - `BinaryTableHDU`: fixed usage of `THEAP` keyword

 - `AsciiTable`: use blanks for data filler rather than `null`s

 - `BasicHDU` made `getDummyHDU` public

 - `HeaderCard` fixed padding of string values which sometimes had one too many spaces

 - `image.ImageTiler` allow requests for tiles that are not fully within the original image

 - `util.ByteFormatter`: changed formatter to use `E` (rather than `e`) for exponents since `e` not legal for FITS ASCII tables


## [0.90] - 1996-01-01


### Changed

 - Support for ASCII tables

 - Deferred input for images and tables (data is read only when user actually requests it)

 - Image subsetting without reading the entire image

 - Reading individual rows and elements of tables without reading the entire table

 - Support for in-place rewriting of headers and data

 - Transparent support for Strings in ASCII and Binary tables

 - Transparent support for booleans in binary tables, including varying length columns

 - Efficient buffered random access methods

 - More flexible support for I/O of primitive arrays

