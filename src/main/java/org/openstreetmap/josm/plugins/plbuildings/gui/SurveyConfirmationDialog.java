package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

/**
 * Creates confirmation dialog to proceed with import data which contains "survey" value in tags.
 * It should prevent most accidental data breaking
 */
public class SurveyConfirmationDialog {

    /**
     * Shows confirmation dialog.
     *
     * @return true if user clicks "yes", else false ("no" button/canceled)
     */
    public static boolean show() {
        int result = JOptionPane.showConfirmDialog(
            null,
            tr(
                "A {0} value was detected in the tags.\n"
                + "Are you sure you want to proceed with updating this object?",
                "survey"
            ),
            tr("Building import confirmation"),
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.OK_OPTION;
    }
}
