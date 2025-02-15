/*
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.s1tbx.io.gaofen3;

import org.esa.s1tbx.commons.io.S1TBXFileFilter;
import org.esa.s1tbx.commons.io.S1TBXProductReaderPlugIn;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.engine_utilities.gpf.ReaderUtils;
import org.esa.snap.engine_utilities.util.ZipUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

/**
 * @author Jakob Grahn
 */
public class Gaofen3ProductReaderPlugIn implements S1TBXProductReaderPlugIn {

    private static final String[] PRODUCT_PREFIX = new String[] {"GF3_"};
    private static final String PRODUCT_FORMAT = "Gaofen3";
    private static final String METADATA_EXT = ".meta.xml";
    private static final String RPC_EXT = ".rpc";

    private static final String PLUGIN_DESCRIPTION = "Gaofen-3 Product Format";
    private static final Class[] VALID_INPUT_TYPES = new Class[]{Path.class, File.class, String.class};

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     *
     * @param input any input object
     * @return true if this product reader can decode the given input, otherwise false.
     */
    public DecodeQualification getDecodeQualification(final Object input) {

        final Path path = ReaderUtils.getPathFromInput(input);
        if (path != null) {
            try {
                String realName = path.toRealPath().getFileName().toString();
                if (ZipUtils.isZip(path)) {
                    for(String prefix : PRODUCT_PREFIX) {
                        if (realName.startsWith(prefix)) {
                            return DecodeQualification.INTENDED;
                        }
                    }
                }
                if (findMetadataFile(path) != null) {
                    return DecodeQualification.INTENDED;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return DecodeQualification.UNABLE;
    }

    /**
     * Returns an array containing the classes that represent valid input types for this reader.
     * <p>
     * <p> Intances of the classes returned in this array are valid objects for the <code>setInput</code> method of the
     * <code>ProductReader</code> interface (the method will not throw an <code>InvalidArgumentException</code> in this
     * case).
     *
     * @return an array containing valid input types, never <code>null</code>
     */
    public Class[] getInputTypes() {
        return VALID_INPUT_TYPES;
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
     */
    public ProductReader createReaderInstance() {
        return new Gaofen3ProductReader(this);
    }

    public SnapFileFilter getProductFileFilter() {
        return new S1TBXFileFilter(this);
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never <code>null</code>
     */
    public String[] getFormatNames() {
        return new String[] {PRODUCT_FORMAT};
    }

    /**
     * Gets the default file extensions associated with each of the format names returned by the <code>{@link
     * #getFormatNames}</code> method. <p>The string array returned shall always have the same length as the array
     * returned by the <code>{@link #getFormatNames}</code> method. <p>The extensions returned in the string array shall
     * always include a leading colon ('.') character, e.g. <code>".hdf"</code>
     *
     * @return the default file extensions for this product I/O plug-in, never <code>null</code>
     */
    public String[] getDefaultFileExtensions() {
        return  new String[] {METADATA_EXT};
    }

    /**
     * Gets a short description of this plug-in. If the given locale is set to <code>null</code> the default locale is
     * used.
     * <p>
     * <p> In a GUI, the description returned could be used as tool-tip text.
     *
     * @param locale the local for the given decription string, if <code>null</code> the default locale is used
     * @return a textual description of this product reader/writer
     */
    public String getDescription(final Locale locale) {
        return PLUGIN_DESCRIPTION;
    }

    @Override
    public String getProductMetadataFileExtension() {
        return METADATA_EXT;
    }

    @Override
    public String[] getProductMetadataFilePrefixes() {
        return PRODUCT_PREFIX;
    }
}