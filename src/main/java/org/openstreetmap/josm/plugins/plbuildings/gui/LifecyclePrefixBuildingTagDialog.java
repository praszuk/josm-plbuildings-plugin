package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

/**
 * Creates info dialog to alert the mapper that imported data contains some leftover tags with lifecycle prefixes.
 */
public class LifecyclePrefixBuildingTagDialog {

    public static void show(String lifecyclePrefixBuildingTags) {
        JOptionPane.showMessageDialog(null,
            tr(
                "Lifecycle prefixes have been detected in the result building: {0}."
                    + "\nPlease verify if this is correct and fix the data if needed.",
                lifecyclePrefixBuildingTags
            ),
            tr("Lifecycle prefix tags detected"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
