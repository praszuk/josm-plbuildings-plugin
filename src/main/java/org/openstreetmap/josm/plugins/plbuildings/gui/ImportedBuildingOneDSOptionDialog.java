package org.openstreetmap.josm.plugins.plbuildings.gui;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * It shows when user need to decide what strategy to use if only one data source has building data.
 */
public class ImportedBuildingOneDSOptionDialog {
    /**
     * Shows confirmation dialog
     * @return true if user clicks to use available data source, else false ("no" button/canceled)
     */
    public static boolean show(String availableDatasource){
        Object[] CHOICES = {tr("Use") + " " + availableDatasource, tr("Cancel")};

        int result = JOptionPane.showOptionDialog(
            null,
            tr("Incomplete data. Do you want to use only one data source to import building?"),
            tr("Building import confirmation"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            CHOICES,
            CHOICES[0]
        );
        return result == JOptionPane.YES_OPTION;
    }
}