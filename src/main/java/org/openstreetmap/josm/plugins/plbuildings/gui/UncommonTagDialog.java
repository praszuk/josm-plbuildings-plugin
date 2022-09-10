package org.openstreetmap.josm.plugins.plbuildings.gui;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Creates info dialog to alert the mapper that imported data contains some uncommon tags.
 * It can help to map more POI or decrease importing wrong values.
 */
public class UncommonTagDialog {

    public static void show(String uncommonTags){
        JOptionPane.showMessageDialog(null,
            tr(
                "Uncommon tags ({0}) has been detected." +
                    "\nYou can verify that are correct and add more details.",
                uncommonTags
            ),
            tr("Uncommon tags detected"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
