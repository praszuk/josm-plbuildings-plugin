package org.openstreetmap.josm.plugins.plbuildings.data;

public enum ImportDataSourceConfigType {
    SIMPLE,         // only one data source for geometry and tags
    DUAL_SEPARATED, // one data source for geometry, second one for the tags
    COMPLEX         // one data source for geometry and multiple for the tags
}
