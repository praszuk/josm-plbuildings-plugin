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

public abstract class SettingsTagValuesPanel extends JPanel {
    private JList<Object> tagValuesList;
    private JButton addValueBtn;
    private JButton removeValueBtn;

    abstract String getAddNewValueTitle();

    abstract String getValuesBorderTitle();

    abstract String getDescription();

    public SettingsTagValuesPanel() {
        super();

        setLayout(new BorderLayout());
        JLabel descriptionLabel = new JLabel(getDescription());
        descriptionLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(descriptionLabel, BorderLayout.NORTH);
        add(createTagValueListPanel());
    }

    private Component createTagValueListPanel() {
        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(getValuesBorderTitle())
        ));

        tagValuesList = new JList<>();
        tagValuesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JScrollPane jScrollPane = new JScrollPane(tagValuesList);

        final JPanel buttonsPanel = new JPanel();
        addValueBtn = new JButton(tr("Add"));
        removeValueBtn = new JButton(tr("Remove"));
        removeValueBtn.setEnabled(false);
        buttonsPanel.add(addValueBtn);
        buttonsPanel.add(removeValueBtn);

        serverPanel.add(jScrollPane, BorderLayout.CENTER);
        serverPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return serverPanel;
    }

    public String promptNewValue() {
        return JOptionPane.showInputDialog(getAddNewValueTitle());
    }

    public void removeValueBtnSetEnabled(Boolean enabled) {
        removeValueBtn.setEnabled(enabled);
    }

    public void addValueBtnAddActionListener(ActionListener listener) {
        addValueBtn.addActionListener(listener);
    }

    public void removeValueBtnAddActionListener(ActionListener listener) {
        removeValueBtn.addActionListener(listener);
    }

    public int getValuesListSelectedIndex() {
        return tagValuesList.getSelectedIndex();
    }

    public void setValuesListModel(ListModel<Object> commonBuildingValuesListModel) {
        tagValuesList.setModel(commonBuildingValuesListModel);
    }

    public void valuesListAddListSelectionListener(ListSelectionListener listener) {
        tagValuesList.addListSelectionListener(listener);
    }
}
