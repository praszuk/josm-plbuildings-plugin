package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsUncommonTagsPanel extends SettingsTagValuesPanel {
    @Override
    String getAddNewValueTitle() {
        return tr("Add new common building value");
    }

    @Override
    String getValuesBorderTitle() {
        return tr("Common building values" + ":");
    }

    @Override
    String getDescription() {
        return String.format(
            "<html>"
                + tr(
                "When a building is imported, an additional check is performed to ensure that the building does not"
                    + " contain uncommon tags."
                    + " Common values for the {0} key that should not trigger the check mechanism can be added below."
                    + "{1} Keep in mind that if an object contains a common value"
                    + " but also contains a tag such as {2}, a dialog message will appear.",
                "<i>building</i>",
                "<br><br>",
                "<b>amenity</b>"
            )
                + "</html>"
        );
    }
}
