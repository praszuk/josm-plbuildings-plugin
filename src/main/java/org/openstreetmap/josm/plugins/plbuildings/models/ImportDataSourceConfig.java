package org.openstreetmap.josm.plugins.plbuildings.models;

import org.openstreetmap.josm.plugins.plbuildings.data.ImportDataSource;

/**
 * Manages providing selected data source for given import.
 * For now, "only one" option works, but in future it can be extended to use mixed data sources:
 * e.g. 1st for geometry, 2nd for tags.
 */
public class ImportDataSourceConfig {
    private static ImportDataSourceConfig instance;

    private ImportDataSource geometry;
    private ImportDataSource tags;

    private ImportDataSourceConfig(ImportDataSource geometry, ImportDataSource tags) {
        this.geometry = geometry;
        this.tags = tags;
    }

    private ImportDataSourceConfig(ImportDataSource dataSource){
        this(dataSource, dataSource);
    }

    /**
     * Create config using default data source (BDOT).
     */
    private ImportDataSourceConfig(){
        this(ImportDataSource.BDOT); // TODO add settings load
    }

    public static ImportDataSourceConfig getInstance(){
        if (instance == null){
            instance = new ImportDataSourceConfig();
        }
        return instance;
    }

    public ImportDataSource getTagsSource() {
        return tags;
    }

    // Currently only private until custom data sources will be implemented on the server side
    private void changeDataSource(ImportDataSource geometrySource, ImportDataSource tagsSource){
        this.geometry = geometrySource;
        this.tags = tagsSource;
        // TODO add settings save
    }

    public void changeDataSource(ImportDataSource dataSource){
        changeDataSource(dataSource, dataSource);
    }

    /**
     * @return true if data source is simple (same source for geometry and tags)
     */
    public boolean isSimple(){
        return this.geometry == this.tags;
    }
}
