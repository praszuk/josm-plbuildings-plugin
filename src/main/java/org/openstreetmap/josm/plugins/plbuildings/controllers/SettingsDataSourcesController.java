package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsDataSourcesProfilesTableModel.COL_PROFILE;
import static org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsDataSourcesProfilesTableModel.COL_SERVER;
import static org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsDataSourcesProfilesTableModel.COL_VISIBLE;
import static org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsDataSourcesProfilesTableModel.PROFILE_COLUMNS;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDataSourcesPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsDataSourcesProfilesTableModel;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsDataSourcesServersListModel;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.SettingsImportOneDsStrategyComboBoxModel;
import org.openstreetmap.josm.tools.Logging;

public class SettingsDataSourcesController implements SettingsTabController {

    private final DataSourceConfig dataSourceConfigModel;
    private final SettingsDataSourcesPanel settingsDataSourcesPanelView;

    private final SettingsDataSourcesProfilesTableModel profilesTableModel;
    private final SettingsDataSourcesServersListModel serversListModel;
    private final SettingsImportOneDsStrategyComboBoxModel importOneDsStrategyComboBoxModel;

    public SettingsDataSourcesController(DataSourceConfig dataSourceConfig,
                                         SettingsDataSourcesPanel settingsDataSourcesPanelView) {
        this.dataSourceConfigModel = dataSourceConfig;
        this.settingsDataSourcesPanelView = settingsDataSourcesPanelView;
        this.profilesTableModel = new SettingsDataSourcesProfilesTableModel();
        this.serversListModel = new SettingsDataSourcesServersListModel();
        this.importOneDsStrategyComboBoxModel = new SettingsImportOneDsStrategyComboBoxModel();

        settingsDataSourcesPanelView.setProfilesTableModel(profilesTableModel);
        settingsDataSourcesPanelView.setServersListModel(serversListModel);
        settingsDataSourcesPanelView.setImportOneDsStrategyComboBoxModel(importOneDsStrategyComboBoxModel);

        initViewListeners();
        initModelListeners();

        updateServerList();
        updateProfilesTable();
        updateImportOneDsStrategyComboBox();
    }

    private void initModelListeners() {
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.PROFILES, evt -> updateProfilesTable());
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.SERVERS, evt -> updateServerList());
    }

    private void initViewListeners() {
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

        profilesTableModel.addTableModelListener((tableModelEvent -> {
            int row = tableModelEvent.getFirstRow();
            int column = tableModelEvent.getColumn();

            if (column == PROFILE_COLUMNS.indexOf(COL_VISIBLE)) {
                SettingsDataSourcesProfilesTableModel model =
                    (SettingsDataSourcesProfilesTableModel) tableModelEvent.getSource();
                Boolean checked = (Boolean) model.getValueAt(row, column);

                String serverName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_SERVER));
                String profileName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_PROFILE));
                DataSourceProfile dataSourceProfile = dataSourceConfigModel.getProfileByName(serverName, profileName);
                dataSourceConfigModel.setProfileVisible(dataSourceProfile, checked);
            }
        }));

        settingsDataSourcesPanelView.profilesTableAddListSelectionListener((listSelectionEvent) -> {
            int index = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();

            if (index == 0) {
                settingsDataSourcesPanelView.upBtnSetEnabled(false);
                settingsDataSourcesPanelView.downBtnSetEnabled(true);
            } else if (index == settingsDataSourcesPanelView.getProfilesTableRowCount() - 1) {
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

        settingsDataSourcesPanelView.serversListAddListSelectionListener(
            listSelectionEvent -> settingsDataSourcesPanelView.removeServerBtnSetEnabled(
                settingsDataSourcesPanelView.getServerListSelectedIndex() != -1
            )
        );

        settingsDataSourcesPanelView.addServerBtnAddActionListener(actionEvent -> addServerAction());
        settingsDataSourcesPanelView.removeServerBtnAddActionListener(actionEvent -> removeServerAction());
        settingsDataSourcesPanelView.addImportOneDsStrategyComboBoxItemListener(
            new ImportOneDsStrategyComboBoxItemChanged()
        );
    }

    private void updateServerList() {
        serversListModel.clear();
        dataSourceConfigModel.getServers().forEach(server -> serversListModel.addElement(
            String.format("%s: %s", server.getName(), server.getUrl())
        ));
    }

    private void updateProfilesTable() {
        settingsDataSourcesPanelView.profilesTableClearSelection();
        profilesTableModel.getDataVector().removeAllElements();
        dataSourceConfigModel.getProfiles().forEach(profile -> profilesTableModel.addRow(new Object[] {
            profile.getName(),
            profile.getDataSourceServerName(),
            profile.getTags(),
            profile.getGeometry(),
            profile.isVisible()
        }));
    }

    private void updateImportOneDsStrategyComboBox() {
        importOneDsStrategyComboBoxModel.removeAllElements();
        importOneDsStrategyComboBoxModel.addAll(List.of(CombineNearestOneDsStrategy.values()));
        importOneDsStrategyComboBoxModel.setSelectedItem(
            CombineNearestOneDsStrategy.fromString(BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.get())
        );
    }

    private void moveProfile(int srcRowIndex, int dstRowIndex) {
        int indexColServer = profilesTableModel.findColumn(COL_SERVER);
        int indexColProfile = profilesTableModel.findColumn(COL_PROFILE);

        DataSourceProfile srcProfile = dataSourceConfigModel.getProfileByName(
            (String) profilesTableModel.getValueAt(srcRowIndex, indexColServer),
            (String) profilesTableModel.getValueAt(srcRowIndex, indexColProfile)
        );
        DataSourceProfile dstProfile = dataSourceConfigModel.getProfileByName(
            (String) profilesTableModel.getValueAt(dstRowIndex, indexColServer),
            (String) profilesTableModel.getValueAt(dstRowIndex, indexColProfile)
        );
        dataSourceConfigModel.swapProfileOrder(srcProfile, dstProfile);
    }

    private void moveProfileUp() {
        int rowIndex = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (rowIndex == 0) {
            Logging.error("Trying to move up first profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex - 1);
    }

    private void moveProfileDown() {
        int rowIndex = settingsDataSourcesPanelView.getProfilesTableSelectedRowIndex();
        if (rowIndex == settingsDataSourcesPanelView.getProfilesTableRowCount() - 1) {
            Logging.error("Trying to move down last profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex + 1);
    }

    private void addServerAction() {
        boolean success = settingsDataSourcesPanelView.promptNewServerNameUrl();
        if (!success) {
            return;
        }
        try {
            DataSourceServer newServer = new DataSourceServer(
                settingsDataSourcesPanelView.getAddServerNameFieldText(),
                settingsDataSourcesPanelView.getAddServerUrlFieldText()
            );
            dataSourceConfigModel.addServer(newServer);
        } catch (IllegalArgumentException exception) {
            settingsDataSourcesPanelView.showAddNewServerErrorDialog();
        }

    }

    private void removeServerAction() {
        int serverIndex = settingsDataSourcesPanelView.getServerListSelectedIndex();
        DataSourceServer selectedServer = dataSourceConfigModel.getServers().get(serverIndex);

        boolean success = settingsDataSourcesPanelView.showRemoveServerConfirmDialog(selectedServer.getName());
        if (success) {
            dataSourceConfigModel.removeServer(selectedServer);
        }
    }

    @Override
    public String getTabTitle() {
        return tr("Data sources");
    }

    @Override
    public Component getTabView() {
        return settingsDataSourcesPanelView;
    }

    private class ImportOneDsStrategyComboBoxItemChanged implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                int selectedIndex = settingsDataSourcesPanelView.getImportOneDsStrategyComboBoxSelectedIndex();
                CombineNearestOneDsStrategy strategy =
                    (CombineNearestOneDsStrategy) importOneDsStrategyComboBoxModel.getElementAt(selectedIndex);
                BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(strategy.toString());
            }
        }
    }

}
