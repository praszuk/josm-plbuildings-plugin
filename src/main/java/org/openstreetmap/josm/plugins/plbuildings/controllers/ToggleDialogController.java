package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.BuildingsToggleDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.ui.ToggleDialogProfilesComboBoxModel;
import org.openstreetmap.josm.tools.Logging;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

public class ToggleDialogController {
    private final DataSourceConfig dataSourceConfigModel;
    private final BuildingsToggleDialog toggleDialogView;

    private static final Color COLOR_DEFAULT = Color.BLACK;
    private static final Color COLOR_ORANGE = Color.decode("#ff781f"); // hex orange better than Color.ORANGE
    private final ToggleDialogProfilesComboBoxModel dataSourceProfilesComboBoxModel;

    public ToggleDialogController(DataSourceConfig dataSourceConfig, BuildingsToggleDialog toggleDialog) {
        this.dataSourceConfigModel = dataSourceConfig;
        this.toggleDialogView = toggleDialog;
        this.dataSourceProfilesComboBoxModel = new ToggleDialogProfilesComboBoxModel();

        toggleDialogView.setDataSourceProfilesComboBoxModel(dataSourceProfilesComboBoxModel);

        initViewListeners();
        initModelListeners();

        setDefaultStatus();
        updateTags("", "", false);
        updateDataSourceProfilesComboBox();
    }

    private void initModelListeners() {
        dataSourceConfigModel.addPropertyChangeListener(DataSourceConfig.PROFILES, evt -> updateDataSourceProfilesComboBox());
    }

    private void initViewListeners() {
        toggleDialogView.addDataSourceProfilesComboBoxActionListener(actionEvent -> {
            int index = toggleDialogView.getDataSourceProfilesComboBoxSelectedIndex();
            DataSourceProfile newCurrentProfile;
            if (index == -1) {  // no selection
                newCurrentProfile = null;
            } else {
                newCurrentProfile = dataSourceConfigModel.getProfiles().get(index);
            }
            dataSourceConfigModel.setCurrentProfile(newCurrentProfile);
        });
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

        if (autoChangeToDefault){
            setDefaultStatus();
        }

    }

    protected void updateDataSourceProfilesComboBox() {
        dataSourceProfilesComboBoxModel.removeAllElements();
        List<DataSourceProfile> profiles = dataSourceConfigModel.getProfiles();

        dataSourceProfilesComboBoxModel.addAll(
            profiles.stream() // TODO fix it, move it above!!!!
                .filter(DataSourceProfile::isVisible)
                .map(DataSourceProfile::getName)
                .collect(Collectors.toList())
        );
        int currentProfileIndex;
        try {
            currentProfileIndex = profiles.indexOf(dataSourceConfigModel.getCurrentProfile());

        } catch (NullPointerException ignore){
            currentProfileIndex = -1;  // no selection
        }
        this.toggleDialogView.setDataSourceProfilesComboBoxSelectedIndex(currentProfileIndex);
    }

    /**
     * Select color for the JLabel status text depends on the ImportStatus.
     */
    private Color getStatusTextColor(ImportStatus status){
        Color statusColor;
        switch(status) {
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

    private void setDefaultStatus(){
        Logging.debug("Changing status to default");
        GuiHelper.scheduleTimer(
            1500,
            actionEvent -> setStatus(ImportStatus.IDLE, false),
            false
        );
    }
}
