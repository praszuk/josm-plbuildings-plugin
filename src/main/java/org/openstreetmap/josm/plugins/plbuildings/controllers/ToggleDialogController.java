package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.stream.Collectors;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsToggleDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.ToggleDialogProfilesComboBoxModel;
import org.openstreetmap.josm.tools.Logging;

public class ToggleDialogController {
    private final DataSourceConfig dataSourceConfigModel;
    private final BuildingsToggleDialog toggleDialogView;
    private final ToggleDialogProfilesComboBoxModel dataSourceProfilesComboBoxModel;

    static final Color COLOR_DEFAULT = Color.BLACK;
    /** Hex orange better than Color.ORANGE */
    static final Color COLOR_ORANGE = Color.decode("#ff781f");

    public ToggleDialogController(DataSourceConfig dataSourceConfig, BuildingsToggleDialog toggleDialog) {
        this.dataSourceConfigModel = dataSourceConfig;
        this.toggleDialogView = toggleDialog;
        this.dataSourceProfilesComboBoxModel = new ToggleDialogProfilesComboBoxModel();

        toggleDialogView.setDataSourceProfilesComboBoxModel(dataSourceProfilesComboBoxModel);
        toggleDialogView.setDataSourceProfilesComboBoxRenderer((profile -> ((DataSourceProfile) (profile)).getName()));
        toggleDialogView.addDataSourceProfilesComboBoxActionListener(new DataSourceProfileComboBoxChanged());
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.PROFILES, new DataSourceModelChanged());

        initDefaultValues();
    }

    public void updateTags(String buildingType, String buildingLevels, boolean hasUncommonTag) {
        Logging.info(
            "Updating tags: building: {0}, building:levels: {1}, uncommonTags: {2}",
            buildingType,
            buildingLevels,
            hasUncommonTag
        );
        toggleDialogView.setBuildingTypeText(buildingType.isEmpty() ? "--" : buildingType);
        toggleDialogView.setBuildingLevelsText(buildingLevels.isEmpty() ? "--" : buildingLevels);
        toggleDialogView.setHasUncommonTagText(hasUncommonTag ? tr("Yes") : tr("No"));

        toggleDialogView.setBuildingTypeForeground(hasUncommonTag ? COLOR_ORANGE : COLOR_DEFAULT);
        toggleDialogView.setHasUncommonTagForeground(hasUncommonTag ? COLOR_ORANGE : COLOR_DEFAULT);
    }

    public void setStatus(ImportStatus status, boolean autoChangeToDefault) {
        Logging.info("Changing status to: {0}", status);
        toggleDialogView.setStatusText(status.toString());
        toggleDialogView.setStatusForeground(getStatusTextColor(status));

        if (autoChangeToDefault) {
            setDefaultStatus();
        }
    }

    /**
     * Load data from model to profiles comboxbox, update tags and set default status (IDLE).
     */
    private void initDefaultValues() {
        updateDataSourceProfiles();
        updateTags("", "", false);
        setDefaultStatus();
    }

    private void updateDataSourceProfiles() {
        updateProfilesComboBoxModel();
        int currentProfileIndex = getCurrentProfileComboBoxModelIndex();
        toggleDialogView.setDataSourceProfilesComboBoxSelectedIndex(currentProfileIndex);
    }

    private int getCurrentProfileComboBoxModelIndex() {
        int currentProfileIndex;
        try {
            currentProfileIndex = dataSourceProfilesComboBoxModel.getIndexOf(dataSourceConfigModel.getCurrentProfile());
        } catch (NullPointerException ignore) {
            currentProfileIndex = -1;
        }
        return currentProfileIndex;
    }


    private List<DataSourceProfile> getFilteredDataSourceProfiles() {
        return dataSourceConfigModel.getProfiles().stream()
            .filter(DataSourceProfile::isVisible)
            .collect(Collectors.toList());
    }

    private void updateProfilesComboBoxModel() {
        dataSourceProfilesComboBoxModel.removeAllElements();
        dataSourceProfilesComboBoxModel.addAll(getFilteredDataSourceProfiles());
    }

    private DataSourceProfile getSelectedDataSourceProfileFromComboBox() {
        int index = toggleDialogView.getDataSourceProfilesComboBoxSelectedIndex();
        DataSourceProfile selectedProfile;
        if (index == -1) {  // no selection
            selectedProfile = null;
        } else {
            selectedProfile = getFilteredDataSourceProfiles().get(index);
        }
        return selectedProfile;
    }

    /**
     * Select color for the status text depends on the ImportStatus.
     */
    private Color getStatusTextColor(ImportStatus status) {
        Color statusColor;
        switch (status) {
            case ACTION_REQUIRED:
                statusColor = COLOR_ORANGE;
                break;
            case CANCELED:
            case NO_DATA:
            case NO_UPDATE:
                statusColor = Color.GRAY;
                break;
            case CONNECTION_ERROR:
            case IMPORT_ERROR:
                statusColor = Color.RED;
                break;
            default: // IDLE, DOWNLOADING, DONE
                statusColor = COLOR_DEFAULT;
        }
        return statusColor;
    }

    private void setDefaultStatus() {
        Logging.debug("Changing status to default");
        GuiHelper.scheduleTimer(
            1500,
            actionEvent -> setStatus(ImportStatus.IDLE, false),
            false
        );
    }

    private class DataSourceProfileComboBoxChanged implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            DataSourceProfile selectedProfile = getSelectedDataSourceProfileFromComboBox();
            dataSourceConfigModel.setCurrentProfile(selectedProfile);
        }
    }

    private class DataSourceModelChanged implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            updateDataSourceProfiles();
        }
    }
}
