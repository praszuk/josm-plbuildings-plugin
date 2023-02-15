package org.openstreetmap.josm.plugins.plbuildings.gui;

import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsDataSourcesPanel extends JPanel {
    private JList<DataSourceServer> serverJList;
    private final DataSourceConfig dataSourceConfig;
    public final String SERVERS = tr("Servers");
    public final String ADD_SERVER_TITLE = tr("Add new server");
    public final String REMOVE_SERVER_TITLE = tr("Remove server");
    public SettingsDataSourcesPanel(){
        super();
        this.dataSourceConfig = DataSourceConfig.getInstance();

        JPanel rootPanel = new JPanel(new GridLayout(2, 1));
        rootPanel.add(createServerListPanel());

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }

    private Component createServerListPanel() {
        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder(SERVERS)
        ));

        this.serverJList = new JList<>(this.dataSourceConfig.getServers().toArray(new DataSourceServer[0]));
        this.serverJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.serverJList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                DataSourceServer server = (DataSourceServer) value;
                setText(String.format("%s: %s", server.getName(), server.getUrl()));
                return this;
            }
        });
        this.serverJList.setVisibleRowCount(3);

        JScrollPane jScrollPane = new JScrollPane(serverJList);

        JPanel buttonsPanel = new JPanel();
        JButton addButton = new JButton(tr("Add"));
        JButton removeButton = new JButton(tr("Remove"));
        removeButton.setEnabled(false);
        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);

        this.serverJList.addListSelectionListener(
            (listSelectionEvent -> removeButton.setEnabled(this.serverJList.getSelectedValue() != null))
        );
        addButton.addActionListener((event) -> addServerAction());
        removeButton.addActionListener((event) -> removeServerAction());

        serverPanel.add(jScrollPane, BorderLayout.CENTER);
        serverPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return serverPanel;
    }

    private void refreshServerList(){
        DefaultListModel<DataSourceServer> listModel = new DefaultListModel<>();
        listModel.addAll(this.dataSourceConfig.getServers());
        this.serverJList.setModel(listModel);
    }
    private void addServerAction(){
        JPanel dialogPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(groupLayout);

        JTextField serverNameField = new JTextField(50);
        JTextField serverUrlField = new JTextField(50);

        JLabel serverNameLabel = new JLabel(tr("Server name") + ": ");
        JLabel serverUrlLabel = new JLabel(tr("Server URL") + ": ");

        serverNameLabel.setLabelFor(serverNameField);
        serverUrlLabel.setLabelFor(serverUrlField);

        GroupLayout.SequentialGroup hGroup = groupLayout.createSequentialGroup();
        hGroup.addGroup(
            groupLayout
                .createParallelGroup()
                .addComponent(serverNameLabel)
                .addComponent(serverUrlLabel)
        );
        hGroup.addGroup(
            groupLayout
                .createParallelGroup()
                .addComponent(serverNameField)
                .addComponent(serverUrlField)
        );
        groupLayout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = groupLayout.createSequentialGroup();
        vGroup.addGroup(
            groupLayout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(serverNameLabel)
                .addComponent(serverNameField)
        );
        vGroup.addGroup(
            groupLayout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(serverUrlLabel)
                .addComponent(serverUrlField)
        );
        groupLayout.setVerticalGroup(vGroup);

        int result = JOptionPane.showConfirmDialog(
                null,
                dialogPanel,
                ADD_SERVER_TITLE,
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result == JOptionPane.OK_OPTION){
            try{
                DataSourceServer newServer = new DataSourceServer(serverNameField.getText(), serverUrlField.getText());
                dataSourceConfig.addServer(newServer);
                refreshServerList();
            } catch (IllegalArgumentException exception){
                JOptionPane.showMessageDialog(
                    null,
                        tr("Error with adding a new server. Name must be unique and URL must be valid!"),
                        ADD_SERVER_TITLE,
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void removeServerAction(){
        DataSourceServer selectedServer = this.serverJList.getSelectedValue();

        int result = JOptionPane.showConfirmDialog(
            null,
            tr("Are you sure to remove server") + ": " + selectedServer.getName(),
            REMOVE_SERVER_TITLE,
            JOptionPane.OK_CANCEL_OPTION
        );
        if (result == JOptionPane.OK_OPTION){
            this.dataSourceConfig.removeServer(selectedServer);
            refreshServerList();
            // TODO refreshProfilesList
        }
    }
}
