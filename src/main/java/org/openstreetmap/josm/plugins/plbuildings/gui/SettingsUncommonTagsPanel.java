package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsUncommonTagsPanel extends SettingsTagValuesPanel {
    @Override
    String getAddNewValueTitle() {
        return tr("Add new common value for the {0} key", "building");
    }

    @Override
    String getValuesBorderTitle() {
        return tr("Common values of the {0} key:", "building");
    }

    @Override
    String getDescription() {
        return String.format(
            "<html>"
                + tr(
                "When a building is imported, an additional check is performed to ensure that the building does not"
                    + " contain uncommon tags."
                    + " Common values for the {0} key that should not trigger the check mechanism"
                    + " can be added below."
                    + "<br><br>"
                    + " Keep in mind that if an object contains a common value"
                    + " but also contains a key such as {1}, a dialog message will appear.",
                "<i>building</i>",
                "<b>amenity</b>"
            )
                + "</html>"
        );
    }
}
