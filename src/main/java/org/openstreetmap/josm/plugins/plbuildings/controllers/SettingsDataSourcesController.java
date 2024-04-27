package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDataSourcesPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Arrays;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsDataSourcesController {

    private final DataSourceConfig dataSourceConfigModel;

    private final SettingsDataSourcesPanel settingsDataSourcesPanelView;

    private final String COL_PROFILE = tr("Profile");
    private final String COL_SERVER = tr("Server");
    private final String COL_TAGS = tr("Tags");
    private final String COL_GEOMETRY = tr("Geometry");
    private final String COL_VISIBLE = tr("Visible");
    private final ArrayList<String> PROFILE_COLUMNS = new ArrayList<>(
            Arrays.asList(COL_PROFILE, COL_SERVER, COL_TAGS, COL_GEOMETRY, COL_VISIBLE)
    );
    private DefaultTableModel tableModel;

    public SettingsDataSourcesController(DataSourceConfig dataSourceConfig, SettingsDataSourcesPanel settingsDataSourcesPanelView) {
        this.dataSourceConfigModel = dataSourceConfig;
        this.settingsDataSourcesPanelView = settingsDataSourcesPanelView;

        initViewListeners();
        initModelListeners();

        updateServerList();
        updateProfilesTable();
    }

    private void initModelListeners() {
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.PROFILES, evt -> updateProfilesTable());
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.SERVERS, evt -> updateServerList());
    }

    private void initViewListeners(){
        settingsDataSourcesPanelView.upBtnAddActionListener(actionEvent -> {
            settingsDataSourcesPanelView.upBtnSetEnabled(false);
            moveProfileUp();
        });

        settingsDataSourcesPanelView.downBtnAddActionListener(actionEvent -> {
            settingsDataSourcesPanelView.downBtnSetEnabled(false);
            moveProfileDown();
        });

        settingsDataSourcesPanelView.refreshBtnAddActionListener(actionEvent -> {
            settingsDataSourcesPanelView.refreshBtnSetEnabled(false);
            dataSourceConfigModel.refreshFromServer(true);
            settingsDataSourcesPanelView.refreshBtnSetEnabled(true);
        });

        settingsDataSourcesPanelView.profilesTableAddListSelectionListener((listSelectionEvent) -> {
            int index = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();

            if (index == 0){
                settingsDataSourcesPanelView.upBtnSetEnabled(false);
                settingsDataSourcesPanelView.downBtnSetEnabled(true);
            } else if (index == settingsDataSourcesPanelView.getProfilesTableRowCount() - 1){
                settingsDataSourcesPanelView.upBtnSetEnabled(true);
                settingsDataSourcesPanelView.downBtnSetEnabled(false);
            } else if (index == -1) { // no selection
                settingsDataSourcesPanelView.upBtnSetEnabled(false);
                settingsDataSourcesPanelView.downBtnSetEnabled(false);
            } else {
                settingsDataSourcesPanelView.upBtnSetEnabled(true);
                settingsDataSourcesPanelView.downBtnSetEnabled(true);
            }
        });

        settingsDataSourcesPanelView.addServerBtnAddActionListener(actionEvent -> addServerAction());
        settingsDataSourcesPanelView.removeServerBtnAddActionListener(actionEvent -> removeServerAction());
    }

    public SettingsDataSourcesPanel getSettingsDataSourcesPanelView() {
        return settingsDataSourcesPanelView;
    }

    private void updateServerList(){
        settingsDataSourcesPanelView.setServerList(dataSourceConfigModel.getServers());
    }

    private void updateProfilesTable(){
        tableModel = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == PROFILE_COLUMNS.indexOf(COL_VISIBLE);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == PROFILE_COLUMNS.indexOf(COL_VISIBLE)){
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        PROFILE_COLUMNS.forEach(tableModel::addColumn);

        tableModel.addTableModelListener((tableModelEvent -> {
            int row = tableModelEvent.getFirstRow();
            int column = tableModelEvent.getColumn();

            if (column == PROFILE_COLUMNS.indexOf(COL_VISIBLE)) {
                TableModel model = (TableModel) tableModelEvent.getSource();
                Boolean checked = (Boolean) model.getValueAt(row, column);

                String serverName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_SERVER));
                String profileName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_PROFILE));
                DataSourceProfile dataSourceProfile = dataSourceConfigModel.getProfileByName(serverName, profileName);
                dataSourceConfigModel.setProfileVisible(dataSourceProfile, checked);
            }
        }));

        dataSourceConfigModel.getProfiles().forEach(profile -> tableModel.addRow(new Object[]{
                profile.getName(),
                profile.getDataSourceServerName(),
                profile.getTags(),
                profile.getGeometry(),
                profile.isVisible()
        }));
        settingsDataSourcesPanelView.setProfilesTableModel(tableModel);
    }

    private void moveProfile(int srcRowIndex, int dstRowIndex){
        int indexColServer = tableModel.findColumn(COL_SERVER);
        int indexColProfile = tableModel.findColumn(COL_PROFILE);

        DataSourceProfile srcProfile = dataSourceConfigModel.getProfileByName(
                (String) tableModel.getValueAt(srcRowIndex, indexColServer),
                (String) tableModel.getValueAt(srcRowIndex, indexColProfile)
        );
        DataSourceProfile dstProfile = dataSourceConfigModel.getProfileByName(
                (String) tableModel.getValueAt(dstRowIndex, indexColServer),
                (String) tableModel.getValueAt(dstRowIndex, indexColProfile)
        );
        dataSourceConfigModel.swapProfileOrder(srcProfile, dstProfile);
    }
    private void moveProfileUp() {
        int rowIndex = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (rowIndex == 0){
            Logging.error("Trying to move up first profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex - 1);
    }

    private void moveProfileDown() {
        int rowIndex = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (rowIndex == settingsDataSourcesPanelView.getProfilesTableRowCount() - 1){
            Logging.error("Trying to move down last profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex + 1);
    }

    private void addServerAction(){
        boolean success = settingsDataSourcesPanelView.promptNewServerNameUrl();
        if (!success){
            return;
        }
        try{
            DataSourceServer newServer = new DataSourceServer(
                    settingsDataSourcesPanelView.getAddServerNameFieldText(),
                    settingsDataSourcesPanelView.getAddServerUrlFieldText()
            );
            dataSourceConfigModel.addServer(newServer);
        } catch (IllegalArgumentException exception){
            settingsDataSourcesPanelView.showAddNewServerErrorDialog();
        }

    }
    private void removeServerAction(){
        DataSourceServer selectedServer = settingsDataSourcesPanelView.getServerListSelected();
        boolean success = settingsDataSourcesPanelView.showRemoveServerConfirmDialog(selectedServer.getName());

        if (success){
            dataSourceConfigModel.removeServer(selectedServer);
        }
    }
}
