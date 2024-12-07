package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * It shows when user need to decide what strategy to use if only one data source has building data.
 */
public class ImportedBuildingOneDsOptionDialog {
    private boolean doNotShowAgainThisSession;
    private boolean userConfirmedOneDs;

    private final String availableDatasource;

    public ImportedBuildingOneDsOptionDialog(String availableDatasource) {
        this.availableDatasource = availableDatasource;
    }

    public void show() {
        final Object[] choices = {tr("Use") + " " + availableDatasource, tr("Cancel")};
        // TODO You can change it permamently in the settings.
        JCheckBox doNotShowAgainThisSessionCheckBox = new JCheckBox(tr("Do not show again (this session)."));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(
            new JLabel(tr("Incomplete data. Do you want to use only one data source to import building?")),
            BorderLayout.CENTER)
        ;
        panel.add(doNotShowAgainThisSessionCheckBox, BorderLayout.SOUTH);

        int result = JOptionPane.showOptionDialog(
            null,
            panel,
            tr("Building import confirmation"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices,
            choices[0]
        );

        userConfirmedOneDs = result == JOptionPane.YES_OPTION;
        doNotShowAgainThisSession = doNotShowAgainThisSessionCheckBox.isSelected();
    }

    public boolean isDoNotShowAgainThisSession() {
        return doNotShowAgainThisSession;
    }

    public boolean isUserConfirmedOneDs() {
        return userConfirmedOneDs;
    }
}