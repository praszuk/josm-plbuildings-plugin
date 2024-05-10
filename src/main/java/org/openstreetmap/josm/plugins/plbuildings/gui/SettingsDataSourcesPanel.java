package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import org.openstreetmap.josm.tools.ImageProvider;

public class SettingsDataSourcesPanel extends JPanel {

    private static final String SERVERS = tr("Servers");
    private static final String PROFILES = tr("Profiles");
    private static final String ADD_SERVER_TITLE = tr("Add new server");
    private static final String REMOVE_SERVER_TITLE = tr("Remove server");

    private JButton upBtn;
    private JButton downBtn;
    private JButton refreshBtn;
    private JButton addServerBtn;
    private JButton removeServerBtn;
    private JTextField addServerNameField;
    private JTextField addServerUrlField;

    private JList<Object> serverList;
    private JTable profileTable;


    public SettingsDataSourcesPanel() {
        super();

        JPanel rootPanel = new JPanel(new GridLayout(2, 1));
        rootPanel.add(createServerListPanel());
        rootPanel.add(createProfileTablePanel());

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }

    private Component createServerListPanel() {
        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(SERVERS)
        ));

        this.serverList = new JList<>();
        this.serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.serverList.setVisibleRowCount(3);

        final JScrollPane jScrollPane = new JScrollPane(serverList);

        final JPanel buttonsPanel = new JPanel();
        addServerBtn = new JButton(tr("Add"));
        removeServerBtn = new JButton(tr("Remove"));
        removeServerBtn.setEnabled(false);
        buttonsPanel.add(addServerBtn);
        buttonsPanel.add(removeServerBtn);

        serverPanel.add(jScrollPane, BorderLayout.CENTER);
        serverPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return serverPanel;
    }

    public JPanel createServerConfirmDialog() {
        final JPanel dialogPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(groupLayout);

        addServerNameField = new JTextField(50);
        addServerUrlField = new JTextField(50);

        JLabel serverNameLabel = new JLabel(tr("Server name") + ": ");
        JLabel serverUrlLabel = new JLabel(tr("Server URL") + ": ");

        serverNameLabel.setLabelFor(addServerNameField);
        serverUrlLabel.setLabelFor(addServerUrlField);

        GroupLayout.SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
        horizontalGroup.addGroup(
            groupLayout
                .createParallelGroup()
                .addComponent(serverNameLabel)
                .addComponent(serverUrlLabel)
        );
        horizontalGroup.addGroup(
            groupLayout
                .createParallelGroup()
                .addComponent(addServerNameField)
                .addComponent(addServerUrlField)
        );
        groupLayout.setHorizontalGroup(horizontalGroup);

        GroupLayout.SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
        verticalGroup.addGroup(
            groupLayout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(serverNameLabel)
                .addComponent(addServerNameField)
        );
        verticalGroup.addGroup(
            groupLayout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(serverUrlLabel)
                .addComponent(addServerUrlField)
        );
        groupLayout.setVerticalGroup(verticalGroup);
        return dialogPanel;
    }

    public boolean promptNewServerNameUrl() {
        int result = JOptionPane.showConfirmDialog(
            null,
            createServerConfirmDialog(),
            ADD_SERVER_TITLE,
            JOptionPane.OK_CANCEL_OPTION
        );
        return result == JOptionPane.OK_OPTION;
    }

    public void showAddNewServerErrorDialog() {
        JOptionPane.showMessageDialog(
            null,
            tr("Error with adding a new server. Name must be unique and URL must be valid!"),
            ADD_SERVER_TITLE,
            JOptionPane.ERROR_MESSAGE
        );
    }

    public boolean showRemoveServerConfirmDialog(String serverName) {
        int result = JOptionPane.showConfirmDialog(
            null,
            tr("Are you sure to remove server") + ": " + serverName,
            REMOVE_SERVER_TITLE,
            JOptionPane.OK_CANCEL_OPTION
        );
        return result == JOptionPane.OK_OPTION;
    }

    private Component createProfileTablePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(PROFILES)
        ));

        this.profileTable = new JTable();
        this.profileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.setFloatable(false);

        upBtn = new JButton();
        downBtn = new JButton();
        refreshBtn = new JButton();

        // All icons have same size (35x35). To set different icon size it must be rescaled manually.
        upBtn.setIcon(ImageProvider.get("dialogs/up.svg"));
        downBtn.setIcon(ImageProvider.get("dialogs/down.svg"));
        refreshBtn.setIcon(ImageProvider.get("dialogs/refresh.svg"));

        upBtn.setEnabled(false);
        downBtn.setEnabled(false);

        toolBar.add(upBtn);
        toolBar.add(downBtn);
        toolBar.add(refreshBtn);

        JScrollPane scrollPane = new JScrollPane(profileTable);
        profilePanel.add(scrollPane, BorderLayout.CENTER);
        profilePanel.add(toolBar, BorderLayout.EAST);

        return profilePanel;
    }

    public void upBtnAddActionListener(ActionListener listener) {
        upBtn.addActionListener(listener);
    }

    public void downBtnAddActionListener(ActionListener listener) {
        downBtn.addActionListener(listener);
    }

    public void refreshBtnAddActionListener(ActionListener listener) {
        refreshBtn.addActionListener(listener);
    }

    public void upBtnSetEnabled(Boolean enabled) {
        upBtn.setEnabled(enabled);
    }

    public void downBtnSetEnabled(Boolean enabled) {
        downBtn.setEnabled(enabled);
    }

    public void refreshBtnSetEnabled(Boolean enabled) {
        refreshBtn.setEnabled(enabled);
    }

    public void removeServerBtnSetEnabled(Boolean enabled) {
        removeServerBtn.setEnabled(enabled);
    }

    public void profilesTableAddListSelectionListener(ListSelectionListener listener) {
        profileTable.getSelectionModel().addListSelectionListener(listener);
    }

    public void profilesTableClearSelection() {
        profileTable.getSelectionModel().clearSelection();
        profileTable.getColumnModel().getSelectionModel().clearSelection();
    }

    public void setProfilesTableModel(TableModel model) {
        this.profileTable.setModel(model);
    }

    public int getProfilesTableSelectedRowIndex() {
        return profileTable.getSelectedRow();
    }

    public int getProfilesTableRowCount() {
        return profileTable.getRowCount();
    }

    public void addServerBtnAddActionListener(ActionListener listener) {
        addServerBtn.addActionListener(listener);
    }

    public void removeServerBtnAddActionListener(ActionListener listener) {
        removeServerBtn.addActionListener(listener);
    }

    public String getAddServerNameFieldText() {
        return addServerNameField.getText();
    }

    public String getAddServerUrlFieldText() {
        return addServerUrlField.getText();
    }

    public int getServerListSelectedIndex() {
        return serverList.getSelectedIndex();
    }

    public void setServersListModel(ListModel<Object> serversListModel) {
        serverList.setModel(serversListModel);
    }

    public void serversListAddListSelectionListener(ListSelectionListener listener) {
        serverList.addListSelectionListener(listener);
    }
}
