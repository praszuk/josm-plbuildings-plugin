package org.openstreetmap.josm.plugins.plbuildings.data;


public enum ImportDataSource {
    BDOT("BDOT");  // from budynki.openstreetmap.org.pl
    // EGIB("EGiB");  // In future (maybe)
    private final String text;
    ImportDataSource(final String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static ImportDataSource fromString(String text) {
        for (ImportDataSource ds : ImportDataSource.values()) {
            if (ds.text.equalsIgnoreCase(text)) {
                return ds;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
