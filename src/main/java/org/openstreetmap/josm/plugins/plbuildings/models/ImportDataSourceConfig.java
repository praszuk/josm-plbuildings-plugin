package org.openstreetmap.josm.plugins.plbuildings.models;

import org.openstreetmap.josm.plugins.plbuildings.data.ImportDataSource;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportDataSourceConfigType;

import java.util.*;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.trc;

/**
 * Manages providing selected data source for given import.
 * For now, "only one" option works, but in future it can be extended to use DUAL or COMPLEX (multiple) data sources.
 * e.g. 1st for geometry, 2nd for tags.
 */
public class ImportDataSourceConfig {
    private static ImportDataSourceConfig instance;

    private ImportDataSource geometry;
    private List<ImportDataSource> tags;

    private HashMap<String, ImportDataSource> keysFromSource;

    /**
     * Creates COMPLEX config object.
     * It uses 2+ sources. First for the geometry and second (multiple allowed) for the tags.
     * @param keysFromSource – tell which tags should be obtained from which source e.g.
     * [("building", BDOT), ("building:levels", EGiB) should give 2 tags from 2 different sources
     */
    private ImportDataSourceConfig(
        ImportDataSource geometry,
        List<ImportDataSource> tags,
        HashMap<String, ImportDataSource> keysFromSource
    ){
        this.geometry = geometry;
        this.tags = tags;
        this.keysFromSource = keysFromSource;
    }
    /**
     * Creates DUAL_SEPARATED config object.
     * It uses 2 source. First for the geometry and second for the tags.
     */
    private ImportDataSourceConfig(ImportDataSource geometry, ImportDataSource dataSource) {
        this(geometry, Collections.singletonList(dataSource), null);
    }

    /**
     * Creates SIMPLE config object.
     * It uses only one source to obtain geometry/tags.
     */
    private ImportDataSourceConfig(ImportDataSource dataSource){
        this(dataSource, dataSource);
    }

    /**
     * Create config using default data source (BDOT).
     */
    private ImportDataSourceConfig(){
        // TODO add settings load
        this(ImportDataSource.BDOT); // change it basing on loaded settings
    }

    public static ImportDataSourceConfig getInstance(){
        if (instance == null){
            instance = new ImportDataSourceConfig();
        }
        return instance;
    }

    // Currently only private until custom data sources will be implemented on the server side
    @SuppressWarnings("all")  // TODO remove after server side implementation
    private void changeDataSource(
        ImportDataSource geometrySource,
        List<ImportDataSource> tagsSource,
        HashMap<String, ImportDataSource> keysFromSource
    ){
        this.geometry = geometrySource;
        this.tags = tagsSource;
        this.keysFromSource = keysFromSource;
        // TODO add settings save
    }
    // Currently only private until custom data sources will be implemented on the server side
    private void changeDataSource(ImportDataSource geometrySource, ImportDataSource tagsSource){
        changeDataSource(geometrySource, Collections.singletonList(tagsSource), null);
    }

    @SuppressWarnings("unused")  // TODO remove after server side implementation
    public void changeDataSource(ImportDataSource dataSource){
        changeDataSource(dataSource, dataSource);
    }

    /**
     * @return current config type SIMPLE/DUAL_SEPARATED/COMPLEX.
     */
    public ImportDataSourceConfigType getConfigType(){
        if (this.keysFromSource != null && !this.keysFromSource.isEmpty()){
            return ImportDataSourceConfigType.COMPLEX;
        }
        else if (!this.geometry.equals(this.tags.get(0))){
            return ImportDataSourceConfigType.DUAL_SEPARATED;
        }
        else {
            return ImportDataSourceConfigType.SIMPLE;
        }
    }

    /**
     * @return all data sources used in config (random order)
     */
    public Set<ImportDataSource> getDataSources(){
        Set<ImportDataSource> ds = new HashSet<>();
        ds.add(this.geometry);
        ds.addAll(this.tags);
        return ds;
    }

    @Override
    public String toString() {
        if (getConfigType() == ImportDataSourceConfigType.SIMPLE){
            return this.geometry.toString();
        }
        String tagsList = this.tags.stream()
            .map(ImportDataSource::toString)
            .collect(Collectors.joining(","));
        return String.format(trc("G – geometry, T – tags ", "G: %s, T: %s"), this.geometry, tagsList);
    }
}
