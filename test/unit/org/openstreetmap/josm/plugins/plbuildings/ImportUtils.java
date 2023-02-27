package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.io.importexport.OsmImporter;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/*
 * Source: <a href="https://github.com/tsmock/KaartValidatorPlugin">https://github.com/tsmock/KaartValidatorPlugin</a>
 */
public final class ImportUtils {

    public static final String DATA_SOURCE = "test_source";
    public static final DataSourceServer testServer = new DataSourceServer("server", "127.0.0.1");
    public static final DataSourceProfile testProfile = new DataSourceProfile(
        testServer.getName(), DATA_SOURCE, DATA_SOURCE, "profile1"
    );

    private ImportUtils() {}

    public static DataSet importOsmFile(File file, String layerName) {

        OsmImporter importer = new OsmImporter();
        ProgressMonitor progressMonitor = NullProgressMonitor.INSTANCE;

        try {
            InputStream in = new FileInputStream(file);
            OsmImporter.OsmImporterData oid = importer.loadLayer(in, file, layerName, progressMonitor);
            OsmDataLayer layer = oid.getLayer();
            return layer.getDataSet();

        } catch (FileNotFoundException | IllegalDataException e) {
            e.printStackTrace();
        }

        return null;
    }
}
