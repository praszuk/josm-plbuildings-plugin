package org.openstreetmap.josm.plugins.plbuildings.models.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.DefaultTableModel;

public class SettingsDataSourcesProfilesTableModel extends DefaultTableModel {
    public static final String COL_PROFILE = tr("Profile");
    public static final String COL_SERVER = tr("Server");
    public static final String COL_TAGS = tr("Tags");
    public static final String COL_GEOMETRY = tr("Geometry");
    public static final String COL_VISIBLE = tr("Visible");
    public static final ArrayList<String> PROFILE_COLUMNS = new ArrayList<>(
        Arrays.asList(COL_PROFILE, COL_SERVER, COL_TAGS, COL_GEOMETRY, COL_VISIBLE)
    );

    public SettingsDataSourcesProfilesTableModel() {
        PROFILE_COLUMNS.forEach(this::addColumn);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == PROFILE_COLUMNS.indexOf(COL_VISIBLE);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == PROFILE_COLUMNS.indexOf(COL_VISIBLE)) {
            return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
    }
}
