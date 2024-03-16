package org.openstreetmap.josm.plugins.plbuildings.gui;

import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SettingsDataSourcesPanel extends JPanel {
    private JList<DataSourceServer> serverJList;
    private JTable profileJTable;
    private final DataSourceConfig dataSourceConfig;
    public final String SERVERS = tr("Servers");
    public final String PROFILES = tr("Profiles");
    public final String ADD_SERVER_TITLE = tr("Add new server");
    public final String REMOVE_SERVER_TITLE = tr("Remove server");
    private final String COL_PROFILE = tr("Profile");
    private final String COL_SERVER = tr("Server");
    private final String COL_TAGS = tr("Tags");
    private final String COL_GEOMETRY = tr("Geometry");
    private final String COL_VISIBLE = tr("Visible");
    private final ArrayList<String> PROFILE_COLUMNS = new ArrayList<>(
            Arrays.asList(COL_PROFILE, COL_SERVER, COL_TAGS, COL_GEOMETRY, COL_VISIBLE)
    );


    public SettingsDataSourcesPanel(){
        super();
        this.dataSourceConfig = DataSourceConfig.getInstance();

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

    private void updateServerList(){
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
                refreshData();
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
            refreshData();
        }
    }

    private Component createProfileTablePanel(){
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder(PROFILES)
        ));

        this.profileJTable = new JTable();
        this.profileJTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateProfileTable();

        JToolBar jToolBar = new JToolBar(SwingConstants.VERTICAL);
        jToolBar.setFloatable(false);

        JButton upBtn = new JButton();
        JButton downBtn = new JButton();
        JButton refreshBtn = new JButton();

        // All icons have same size (35x35). To set different icon size it must be rescaled manually.
        upBtn.setIcon(ImageProvider.get("dialogs/up.svg"));
        downBtn.setIcon(ImageProvider.get("dialogs/down.svg"));
        refreshBtn.setIcon(ImageProvider.get("dialogs/refresh.svg"));

        upBtn.setEnabled(false);
        downBtn.setEnabled(false);

        upBtn.addActionListener(actionEvent -> {
            upBtn.setEnabled(false);
            moveProfileUp();
            updateProfileTable();
        });
        downBtn.addActionListener(actionEvent -> {
            downBtn.setEnabled(false);
            moveProfileDown();
            updateProfileTable();
        });
        refreshBtn.addActionListener(actionEvent -> {
            this.setEnabled(false);
            try {
                refreshData();
            }catch (Exception exception){
                Logging.warn("Error at refreshing GUI data");
            }
            this.setEnabled(true);
        });

        this.profileJTable.getSelectionModel().addListSelectionListener((listSelectionEvent) -> {
            int index = this.profileJTable.getSelectedRow();

            if (index == 0){
                upBtn.setEnabled(false);
                downBtn.setEnabled(true);
            } else if (index == this.profileJTable.getRowCount() - 1){
                upBtn.setEnabled(true);
                downBtn.setEnabled(false);
            } else if (index == -1) { // no selection
                upBtn.setEnabled(false);
                downBtn.setEnabled(false);
            } else {
                upBtn.setEnabled(true);
                downBtn.setEnabled(true);
            }
        });

        jToolBar.add(upBtn);
        jToolBar.add(downBtn);
        jToolBar.add(refreshBtn);

        JScrollPane jScrollPane = new JScrollPane(profileJTable);
        profilePanel.add(jScrollPane, BorderLayout.CENTER);
        profilePanel.add(jToolBar, BorderLayout.EAST);

        return profilePanel;
    }

    private void moveProfile(int srcRowIndex, int dstRowIndex){
        int indexColServer = this.profileJTable.getColumnModel().getColumnIndex(COL_SERVER);
        int indexColProfile = this.profileJTable.getColumnModel().getColumnIndex(COL_PROFILE);

        DataSourceProfile srcProfile = this.dataSourceConfig.getProfileByName(
                (String) this.profileJTable.getValueAt(srcRowIndex, indexColServer),
                (String) this.profileJTable.getValueAt(srcRowIndex, indexColProfile)
        );
        DataSourceProfile dstProfile = this.dataSourceConfig.getProfileByName(
                (String) this.profileJTable.getValueAt(dstRowIndex, indexColServer),
                (String) this.profileJTable.getValueAt(dstRowIndex, indexColProfile)
        );
        this.dataSourceConfig.swapProfileOrder(srcProfile, dstProfile);
    }
    private void moveProfileUp() {
        int rowIndex = this.profileJTable.getSelectedRow();
        if (rowIndex == 0){
            Logging.error("Trying to move up first profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex - 1);
    }

    private void moveProfileDown() {
        int rowIndex = this.profileJTable.getSelectedRow();
        if (rowIndex == this.dataSourceConfig.getProfiles().size() - 1){
            Logging.error("Trying to move down last profile in the table!");
            return;
        }
        moveProfile(rowIndex, rowIndex + 1);
    }

    private void updateProfileTable(){
        DefaultTableModel tableModel = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == PROFILE_COLUMNS.indexOf(COL_VISIBLE);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == PROFILE_COLUMNS.indexOf(COL_VISIBLE)){
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        PROFILE_COLUMNS.forEach(tableModel::addColumn);

        tableModel.addTableModelListener((tableModelEvent -> {
            int row = tableModelEvent.getFirstRow();
            int column = tableModelEvent.getColumn();

            if (column == PROFILE_COLUMNS.indexOf(COL_VISIBLE)) {
                TableModel model = (TableModel) tableModelEvent.getSource();
                Boolean checked = (Boolean) model.getValueAt(row, column);

                String serverName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_SERVER));
                String profileName = (String) model.getValueAt(row, PROFILE_COLUMNS.indexOf(COL_PROFILE));
                DataSourceProfile dataSourceProfile = dataSourceConfig.getProfileByName(serverName, profileName);
                dataSourceConfig.setProfileVisible(dataSourceProfile, checked);
            }
        }));

        this.dataSourceConfig.getProfiles().forEach(profile -> tableModel.addRow(new Object[]{
            profile.getName(),
            profile.getDataSourceServerName(),
            profile.getTags(),
            profile.getGeometry(),
            profile.isVisible()
        }));
        this.profileJTable.setModel(tableModel);
    }

    private void refreshData(){
        this.dataSourceConfig.refresh(true);
        updateServerList();
        updateProfileTable();
    }
}
