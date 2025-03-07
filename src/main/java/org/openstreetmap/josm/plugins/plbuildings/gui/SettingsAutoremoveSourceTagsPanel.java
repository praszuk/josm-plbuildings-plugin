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

public class SettingsAutoremoveSourceTagsPanel extends JPanel {
    private static final String SOURCE_VALUES = tr("Source values" + ":");
    private static final String ADD_SOURCE_VALUE_TITLE = tr("Add new source value");

    private static final String DESCRIPTION = String.format(
        "<html>"
            + tr(
            "Below list contains values which can be automatically removed "
                + "if will be found at building import in {0} key.",
            "<i>source</i>"
        )
            + "</html>"
    );

    private JList<Object> sourceValuesList;
    private JButton addSourceValueBtn;
    private JButton removeSourceValueBtn;

    public SettingsAutoremoveSourceTagsPanel() {
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
            BorderFactory.createTitledBorder(SOURCE_VALUES)
        ));

        this.sourceValuesList = new JList<>();
        this.sourceValuesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JScrollPane jScrollPane = new JScrollPane(sourceValuesList);

        final JPanel buttonsPanel = new JPanel();
        addSourceValueBtn = new JButton(tr("Add"));
        removeSourceValueBtn = new JButton(tr("Remove"));
        removeSourceValueBtn.setEnabled(false);
        buttonsPanel.add(addSourceValueBtn);
        buttonsPanel.add(removeSourceValueBtn);

        serverPanel.add(jScrollPane, BorderLayout.CENTER);
        serverPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return serverPanel;
    }

    public String promptNewSourceValue() {
        return JOptionPane.showInputDialog(ADD_SOURCE_VALUE_TITLE);
    }

    public void removeSourceValueBtnSetEnabled(Boolean enabled) {
        removeSourceValueBtn.setEnabled(enabled);
    }

    public void addSourceValueBtnAddActionListener(ActionListener listener) {
        addSourceValueBtn.addActionListener(listener);
    }

    public void removeSourceValueBtnAddActionListener(ActionListener listener) {
        removeSourceValueBtn.addActionListener(listener);
    }

    public int getSourceValuesListSelectedIndex() {
        return sourceValuesList.getSelectedIndex();
    }

    public void setSourceValuesListModel(ListModel<Object> sourceValuesListModel) {
        sourceValuesList.setModel(sourceValuesListModel);
    }

    public void sourceValuesListAddListSelectionListener(ListSelectionListener listener) {
        sourceValuesList.addListSelectionListener(listener);
    }
}
