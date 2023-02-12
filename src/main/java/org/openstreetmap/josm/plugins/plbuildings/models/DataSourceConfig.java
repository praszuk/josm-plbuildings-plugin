package org.openstreetmap.josm.plugins.plbuildings.models;

import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;

import java.util.*;
import java.util.stream.Collectors;


public class DataSourceConfig {
    private final ArrayList<DataSourceServer> servers;
    private final ArrayList<DataSourceProfile> profiles;

    private DataSourceProfile currentProfile;

    private static DataSourceConfig instance;

    public static DataSourceConfig getInstance() {
        if (instance == null){
            instance = new DataSourceConfig();
        }
        return instance;
    }

    private DataSourceConfig(){
        this.servers = new ArrayList<>();
        this.profiles = new ArrayList<>();

        load();
        this.currentProfile = profiles.isEmpty() ? null:profiles.get(0);
    }

    public DataSourceServer getServerByName(String name){
        return servers.stream()
            .filter(dataSourceServer -> dataSourceServer.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public DataSourceProfile getProfileByName(String serverName, String profileName){
        return profiles.stream()
            .filter(dataSourceProfile -> dataSourceProfile.getDataSourceServerName().equals(serverName) &&
                    dataSourceProfile.getName().equals(profileName))
            .findFirst()
            .orElse(null);
    }

    public Collection<DataSourceServer> getServers(){
        return new ArrayList<>(servers);
    }

    public Collection<DataSourceProfile> getProfiles(){
        return new ArrayList<>(profiles);
    }

    public DataSourceProfile getCurrentProfile() {
        return this.currentProfile;
    }

    public void setCurrentProfile(DataSourceProfile profile){
        this.currentProfile = profile;
    }

    public Collection<DataSourceProfile> getServerProfiles(DataSourceServer server){
        return profiles
            .stream()
            .filter(p -> p.getDataSourceServerName().equals(server.getName()))
            .collect(Collectors.toList());
    }

    public void addServer(DataSourceServer newServer){
        validateServer(newServer);
        servers.add(newServer);
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
    }

    public void addProfile(DataSourceProfile newProfile){
        validateProfile(newProfile);
        profiles.add(newProfile);
    }

    public void removeProfile(DataSourceProfile profile){
        profiles.remove(profile);
    }

    private void load(){
        servers.clear();
        profiles.clear();

        String serializedServers = BuildingsSettings.DATA_SOURCE_SERVERS.get();
        servers.addAll(DataSourceServer.fromStringJson(serializedServers));

        String serializedProfiles = BuildingsSettings.DATA_SOURCE_PROFILES.get();
        profiles.addAll(DataSourceProfile.fromStringJson(serializedProfiles));
    }

    private void save(){
        String serializedServers = DataSourceServer.toJson(servers).toString();
        BuildingsSettings.DATA_SOURCE_SERVERS.put(serializedServers);

        String serializedProfiles = DataSourceProfile.toJson(profiles).toString();
        BuildingsSettings.DATA_SOURCE_PROFILES.put(serializedProfiles);
    }

    private void validateServer(DataSourceServer newServer) throws IllegalArgumentException {
        if (servers.stream().anyMatch(s -> s.getName().equals(newServer.getName()))){
            throw new IllegalArgumentException("DataSourceServer name must be unique!");
        }
    }
    private void validateProfile(DataSourceProfile newProfile) throws IllegalArgumentException {
        if (profiles.stream().anyMatch(p -> p.getName().equals(newProfile.getName())
                && p.getDataSourceServerName().equals(newProfile.getDataSourceServerName()))){
            throw new IllegalArgumentException("DataSourceProfile name must be unique per server!");
        }
    }
}
