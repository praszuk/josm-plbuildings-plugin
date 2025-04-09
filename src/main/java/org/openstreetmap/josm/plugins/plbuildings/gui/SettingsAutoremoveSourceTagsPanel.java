package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsAutoremoveSourceTagsPanel extends SettingsTagValuesPanel {
    @Override
    String getAddNewValueTitle() {
        return tr("Add new value for source key");
    }

    @Override
    String getValuesBorderTitle() {
        return tr("Source tag values" + ":");
    }

    @Override
    String getDescription() {
        return String.format(
            "<html>"
                + tr(
                "Below list contains values which can be automatically removed "
                    + "if will be found at building import in {0} key.",
                "<i>source</i>"
            )
                + "</html>"
        );
    }
}
