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
                "Lifecycle prefixes have been detected in the resulting building: {0}."
                    + " Please verify that this is correct and update the data if necessary.",
                lifecyclePrefixBuildingTags
            ),
            tr("Lifecycle prefix tags detected"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
