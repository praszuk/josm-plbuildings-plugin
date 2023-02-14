package org.openstreetmap.josm.plugins.plbuildings.actions;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static org.openstreetmap.josm.tools.I18n.tr;

public class BuildingsSettingsAction extends JosmAction {
    public static final String DESCRIPTION = tr("Show plbuildings settings");
    public static final String TITLE = tr("PlBuildings settings");

    public BuildingsSettingsAction(){
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
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        new SettingsDialog();
    }
}
