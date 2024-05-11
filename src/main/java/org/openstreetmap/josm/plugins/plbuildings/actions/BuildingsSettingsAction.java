package org.openstreetmap.josm.plugins.plbuildings.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.controllers.SettingsController;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class BuildingsSettingsAction extends JosmAction {
    public static final String DESCRIPTION = tr("Show plbuildings settings");
    public static final String TITLE = tr("PlBuildings settings");

    private final SettingsController settingsController;

    public BuildingsSettingsAction(SettingsController settingsController) {
        super(
            TITLE,
            (ImageProvider) null,
            DESCRIPTION,
            Shortcut.registerShortcut(
                "plbuildings:settings",
                tr("Open") + " " + TITLE,
                KeyEvent.CHAR_UNDEFINED,
                Shortcut.NONE
            ),
            true,
            String.format("%s:buildings_settings", BuildingsPlugin.info.name),
            false
        );
        this.settingsController = settingsController;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        settingsController.initGui();
    }
}
