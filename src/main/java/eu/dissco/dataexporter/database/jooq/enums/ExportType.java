/*
 * This file is generated by jOOQ.
 */
package eu.dissco.dataexporter.database.jooq.enums;


import eu.dissco.dataexporter.database.jooq.Public;

import org.jooq.Catalog;
import org.jooq.EnumType;
import org.jooq.Schema;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public enum ExportType implements EnumType {

    DOI_LIST("DOI_LIST"),

    DWC_DP("DWC_DP"),

    DWCA("DWCA");

    private final String literal;

    private ExportType(String literal) {
        this.literal = literal;
    }

    @Override
    public Catalog getCatalog() {
        return getSchema().getCatalog();
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public String getName() {
        return "export_type";
    }

    @Override
    public String getLiteral() {
        return literal;
    }

    /**
     * Lookup a value of this EnumType by its literal. Returns
     * <code>null</code>, if no such value could be found, see {@link
     * EnumType#lookupLiteral(Class, String)}.
     */
    public static ExportType lookupLiteral(String literal) {
        return EnumType.lookupLiteral(ExportType.class, literal);
    }
}
