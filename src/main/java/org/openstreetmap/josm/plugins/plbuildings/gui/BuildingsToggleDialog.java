package org.openstreetmap.josm.plugins.plbuildings.gui;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import jakarta.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Create sidebar window which contains the latest status of import action and allows to change some cfg.
 */
public class BuildingsToggleDialog extends ToggleDialog {
    private static final Color COLOR_DEFAULT = Color.BLACK;
    private static final Color COLOR_ORANGE = Color.decode("#ff781f"); // hex orange better than Color.ORANGE

    private static final int DATA_SOURCE_PROFILE_MAX_CHARS = 20;

    private final JLabel status;
    private final JComboBox<DataSourceProfile> dataSourceProfile;

    private final JLabel building;

    private final JLabel bLevels;
    private final JLabel uncommonTags;

    public BuildingsToggleDialog() {
        super(
            "PlBuildings",
            "plbuildings",
            tr("Open the {0} window", BuildingsPlugin.info.name),
            Shortcut.registerShortcut(
                "plbuildings:window",
                tr("PlBuildings window"),
                KeyEvent.CHAR_UNDEFINED, Shortcut.NONE
            ),
            150
        );

        this.status = new JLabel("");
        this.dataSourceProfile = new JComboBox<>();

        this.building = new JLabel("");
        this.bLevels = new JLabel("");
        this.uncommonTags = new JLabel("");

        updateDataSourceProfileComboBox();
        this.dataSourceProfile.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null){
                    setText("");
                }
                else{
                    DataSourceProfile profile = (DataSourceProfile) value;
                    setText(profile.getName().substring(
                        0,
                        Math.min(DATA_SOURCE_PROFILE_MAX_CHARS, profile.getName().length()))
                    );
                }
                return this;
            }
        });
        this.dataSourceProfile.addActionListener(
            actionEvent -> DataSourceConfig.getInstance().setCurrentProfile(
                (DataSourceProfile) dataSourceProfile.getSelectedItem()
            )
        );

        JPanel rootPanel = new JPanel(new GridLayout(0, 1));

        JPanel configPanel = new JPanel(new GridLayout(2,2));
        JLabel statusLabel = new JLabel(tr("Status") + ": ");
        statusLabel.setBorder(new EmptyBorder(0,5,0,0));

        configPanel.add(statusLabel);
        configPanel.add(status);

        JLabel dataSourceLabel = new JLabel(tr("Data source") + ": ");
        dataSourceLabel.setBorder(new EmptyBorder(0,5,0,0));

        configPanel.add(dataSourceLabel);
        configPanel.add(dataSourceProfile);

        JPanel lastImportTagsPanel = new JPanel(new GridLayout(0, 2));

        lastImportTagsPanel.add(new JLabel("building: "));
        lastImportTagsPanel.add(building);

        lastImportTagsPanel.add(new JLabel("building:levels: "));
        lastImportTagsPanel.add(bLevels);

        lastImportTagsPanel.add(new JLabel(tr("Uncommon tags") +": "));
        lastImportTagsPanel.add(uncommonTags);

        lastImportTagsPanel.setBorder(BorderFactory.createTitledBorder(tr("Latest tags")));
        rootPanel.add(configPanel);

        rootPanel.add(lastImportTagsPanel);

        rootPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        createLayout(rootPanel, true, null);

        setDefaultStatus();
        updateTags("", "", false);
    }

    private void updateDataSourceProfileComboBox() {
        DataSourceConfig config = DataSourceConfig.getInstance();
        ArrayList<DataSourceProfile> profiles = (ArrayList<DataSourceProfile>) config.getProfiles()
            .stream()
            .filter(DataSourceProfile::isVisible)
            .collect(Collectors.toList());
        profiles.add(0, null); // add empty element to handle no profile

        this.dataSourceProfile.setModel(new DefaultComboBoxModel<>(profiles.toArray(DataSourceProfile[]::new)));
        this.dataSourceProfile.setSelectedItem(config.getCurrentProfile());
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

    public void setStatus(@Nonnull ImportStatus status, boolean autoChangeToDefault) {
        GuiHelper.runInEDT(() ->{
            Logging.info("Changing status to: {0}", status);
            this.status.setText(status.toString());
            this.status.setForeground(getStatusTextColor(status));
        });

        if (autoChangeToDefault){
            setDefaultStatus();
        }

    }

    public void updateTags(String buildingVal, String bLevelsVal, boolean hasUncommonTags){
        GuiHelper.runInEDT(() -> {
            Logging.info(
                "Updating tags: building: {0}, building:levels: {1}, uncommonTags: {2}",
                buildingVal,
                bLevelsVal,
                hasUncommonTags
            );
            this.building.setText(buildingVal.isEmpty() ? "--":buildingVal);
            this.bLevels.setText(bLevelsVal.isEmpty() ? "--":bLevelsVal);
            this.uncommonTags.setText(hasUncommonTags ? tr("Yes"):tr("No"));

            this.building.setForeground(hasUncommonTags ? COLOR_ORANGE:COLOR_DEFAULT);
            this.uncommonTags.setForeground(hasUncommonTags ? COLOR_ORANGE:COLOR_DEFAULT);
        });
    }

}
