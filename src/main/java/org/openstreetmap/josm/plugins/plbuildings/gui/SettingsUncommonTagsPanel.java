package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;

public class SettingsUncommonTagsPanel extends JPanel {
    private static final String COMMON_BUILDING_VALUES = tr("Common building values" + ":");
    private static final String ADD_COMMON_BUILDING_VALUE_TITLE = tr("Add new common building value");

    private static final String DESCRIPTION = String.format(
        "<html>"
        + tr(
            "When a building is imported, an additional check is performed to ensure that the building does not"
            + " contain uncommon tags."
            + " Common values for the %s key that should not trigger the check mechanism can be added below."
            + "%sKeep in mind that if an object contains a common value, but also contains a tag such as %s, a dialog"
            + " message will appear."
        )
        + "</html>",
        "<i>building</i>",
        "<br><br>",
        "<b>amenity</b>"
    );

    private JList<Object> commonBuildingValuesList;
    private JButton addBuildingValueBtn;
    private JButton removeBuildingValueBtn;

    public SettingsUncommonTagsPanel() {
        super();

        setLayout(new BorderLayout());
        JLabel descriptionLabel = new JLabel(DESCRIPTION);
        descriptionLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(descriptionLabel, BorderLayout.NORTH);
        add(createTagListPanel());
    }

    private Component createTagListPanel() {
        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(COMMON_BUILDING_VALUES)
        ));

        this.commonBuildingValuesList = new JList<>();
        this.commonBuildingValuesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JScrollPane jScrollPane = new JScrollPane(commonBuildingValuesList);

        final JPanel buttonsPanel = new JPanel();
        addBuildingValueBtn = new JButton(tr("Add"));
        removeBuildingValueBtn = new JButton(tr("Remove"));
        removeBuildingValueBtn.setEnabled(false);
        buttonsPanel.add(addBuildingValueBtn);
        buttonsPanel.add(removeBuildingValueBtn);

        serverPanel.add(jScrollPane, BorderLayout.CENTER);
        serverPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return serverPanel;
    }

    public String promptNewCommonBuildingValue() {
        return JOptionPane.showInputDialog(ADD_COMMON_BUILDING_VALUE_TITLE);
    }

    public void removeBuildingValueBtnSetEnabled(Boolean enabled) {
        removeBuildingValueBtn.setEnabled(enabled);
    }

    public void addBuildingValueBtnAddActionListener(ActionListener listener) {
        addBuildingValueBtn.addActionListener(listener);
    }

    public void removeBuildingValueBtnAddActionListener(ActionListener listener) {
        removeBuildingValueBtn.addActionListener(listener);
    }

    public int getCommonBuildingValuesListSelectedIndex() {
        return commonBuildingValuesList.getSelectedIndex();
    }

    public void setCommonBuildingValuesListModel(ListModel<Object> commonBuildingValuesListModel) {
        commonBuildingValuesList.setModel(commonBuildingValuesListModel);
    }

    public void commonBuildingValuesListAddListSelectionListener(ListSelectionListener listener) {
        commonBuildingValuesList.addListSelectionListener(listener);
    }
}
