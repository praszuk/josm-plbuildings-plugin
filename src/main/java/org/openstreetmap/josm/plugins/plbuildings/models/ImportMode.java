package org.openstreetmap.josm.plugins.plbuildings.models;

import static org.openstreetmap.josm.tools.I18n.tr;

public enum ImportMode {
    FULL(tr("Full")),
    GEOMETRY(tr("Geometry")),
    TAGS(tr("Tags"));

    private final String text;

    ImportMode(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
