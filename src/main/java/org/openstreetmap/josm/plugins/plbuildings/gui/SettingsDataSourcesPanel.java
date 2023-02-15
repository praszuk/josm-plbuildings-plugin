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

        serverPanel.add(jScrollPane, BorderLayout.CENTER);
        serverPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return serverPanel;
    }
}
