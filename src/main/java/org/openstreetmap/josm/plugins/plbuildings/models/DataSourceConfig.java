package org.openstreetmap.josm.plugins.plbuildings.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.io.DataSourceProfileDownloader;
import org.openstreetmap.josm.tools.Logging;


public class DataSourceConfig {
    public static final String PROFILES = "profiles";
    public static final String SERVERS = "servers";

    private final ArrayList<DataSourceServer> servers;
    private final ArrayList<DataSourceProfile> profiles;

    private DataSourceProfile currentProfile;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public DataSourceConfig() {
        this.servers = new ArrayList<>();
        this.profiles = new ArrayList<>();

        BuildingsSettings.DATA_SOURCE_SERVERS.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(SERVERS, null, getServers());
        });
        BuildingsSettings.DATA_SOURCE_PROFILES.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(PROFILES, null, getProfiles());
        });
        BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(PROFILES, null, getProfiles());
        });

        load();
    }

    public DataSourceServer getServerByName(String name) {
        return servers.stream()
            .filter(dataSourceServer -> dataSourceServer.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public DataSourceProfile getProfileByName(String serverName, String profileName) {
        return profiles.stream()
            .filter(
                dataSourceProfile -> dataSourceProfile.getDataSourceServerName().equals(serverName)
                    && dataSourceProfile.getName().equals(profileName))
            .findFirst()
            .orElse(null);
    }

    public List<DataSourceServer> getServers() {
        return new ArrayList<>(servers);
    }

    public List<DataSourceProfile> getProfiles() {
        return new ArrayList<>(profiles);
    }

    public DataSourceProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(DataSourceProfile profile) {
        currentProfile = profiles.stream().filter(p -> p.equals(profile)).findFirst().orElse(null);
        save();
    }

    public Collection<DataSourceProfile> getServerProfiles(DataSourceServer server) {
        return profiles
            .stream()
            .filter(p -> p.getDataSourceServerName().equals(server.getName()))
            .collect(Collectors.toList());
    }

    public void addServer(DataSourceServer newServer) {
        validateServer(newServer);
        servers.add(newServer);
        save();
    }


    /**
     * It removes server and all related profiles.
     */
    public void removeServer(DataSourceServer server) {
        new ArrayList<>(profiles)
            .stream()
            .filter(p -> p.getDataSourceServerName().equals(server.getName()))
            .forEach(this::removeProfile);

        servers.remove(server);
        save();
    }

    public void addProfile(DataSourceProfile newProfile) {
        validateProfile(newProfile);
        profiles.add(newProfile);
        save();
    }

    public void removeProfile(DataSourceProfile profile) {
        profiles.remove(profile);
        save();
    }

    private void load() {
        servers.clear();
        profiles.clear();
        currentProfile = null;

        String serializedServers = BuildingsSettings.DATA_SOURCE_SERVERS.get();
        servers.addAll(DataSourceServer.fromStringJson(serializedServers));

        String serializedProfiles = BuildingsSettings.DATA_SOURCE_PROFILES.get();
        profiles.addAll(DataSourceProfile.fromStringJson(serializedProfiles));

        List<String> serverAndProfileNames = BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.get();
        if (serverAndProfileNames.size() != 2) {
            return;
        }
        String currentProfileServerName = serverAndProfileNames.get(0);
        String currentProfileName = serverAndProfileNames.get(1);
        currentProfile = getProfileByName(currentProfileServerName, currentProfileName);
    }

    private void save() {
        String serializedServers = DataSourceServer.toJson(servers).toString();
        BuildingsSettings.DATA_SOURCE_SERVERS.put(serializedServers);

        String serializedProfiles = DataSourceProfile.toJson(profiles).toString();
        BuildingsSettings.DATA_SOURCE_PROFILES.put(serializedProfiles);

        if (currentProfile != null) {
            BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.put(
                new ArrayList<>(List.of(currentProfile.getDataSourceServerName(), currentProfile.getName()))
            );
        } else {
            BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.put(null);
        }
    }

    /**
     * It fetches data to update DataSourceProfiles from each server.
     *
     * @param save – save all config to JOSM settings
     */
    public void refreshFromServer(boolean save) {
        // Update profiles
        for (DataSourceServer server : getServers()) {
            Collection<DataSourceProfile> downloadedCollection =
                DataSourceProfileDownloader.downloadProfiles(server);
            if (downloadedCollection == null) {
                Logging.warn("Error at refreshing profiles from server: " + server.getName());
                continue;
            }
            Map<String, DataSourceProfile> newServerProfiles = downloadedCollection
                .stream()
                .collect(Collectors.toMap(DataSourceProfile::getName, (d) -> d));

            Map<String, DataSourceProfile> currentServerProfiles = getServerProfiles(server)
                .stream()
                .collect(Collectors.toMap(DataSourceProfile::getName, (d) -> d));

            // Remove old profiles from config which don't appear in newServerProfiles
            currentServerProfiles.keySet().stream()
                .filter(key -> !newServerProfiles.containsKey(key))
                .forEach(key -> profiles.remove(currentServerProfiles.get(key)));

            // Update existing profiles
            currentServerProfiles.keySet().stream()
                .filter(newServerProfiles::containsKey)
                .forEach(key -> {
                    DataSourceProfile currentProfile = getProfileByName(server.getName(), key);
                    DataSourceProfile newProfile = newServerProfiles.get(key);
                    newProfile.setVisible(currentProfile.isVisible());

                    currentProfile.updateProfile(newProfile);
                });

            // Add missing profiles
            newServerProfiles.entrySet().stream()
                .filter(e -> !currentServerProfiles.containsKey(e.getKey()))
                .forEach(e -> addProfile(e.getValue()));
        }
        if (save) {
            save();
        }
    }

    /**
     * Swap data source profile order in collection
     *
     * @param src object to move to dst position
     * @param dst object which will be swapped with src object
     */
    public void swapProfileOrder(DataSourceProfile src, DataSourceProfile dst) {
        assert profiles.contains(src);
        assert profiles.contains(dst);

        int srcIndex = profiles.indexOf(src);
        int dstIndex = profiles.indexOf(dst);

        profiles.set(srcIndex, dst);
        profiles.set(dstIndex, src);

        save();
    }

    public void setProfileVisible(DataSourceProfile profile, boolean value) {
        profile.setVisible(value);
        save();
    }

    private void validateServer(DataSourceServer newServer) throws IllegalArgumentException {
        if (servers.stream().anyMatch(s -> s.getName().equals(newServer.getName()))) {
            throw new IllegalArgumentException("DataSourceServer name must be unique!");
        }
    }

    private void validateProfile(DataSourceProfile newProfile) throws IllegalArgumentException {
        if (profiles.stream().anyMatch(p -> p.getName().equals(newProfile.getName())
            && p.getDataSourceServerName().equals(newProfile.getDataSourceServerName()))) {
            throw new IllegalArgumentException("DataSourceProfile name must be unique per server!");
        }
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
