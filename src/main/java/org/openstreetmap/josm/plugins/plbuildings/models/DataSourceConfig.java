package org.openstreetmap.josm.plugins.plbuildings.models;

import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;

import java.util.*;
import java.util.stream.Collectors;


public class DataSourceConfig {
    private final LinkedHashMap<String, DataSourceServer> servers;
    private final LinkedHashMap<String, DataSourceProfile> profiles;

    private static DataSourceConfig instance;

    public static DataSourceConfig getInstance() {
        if (instance == null){
            instance = new DataSourceConfig();
        }
        return instance;
    }

    private DataSourceConfig(){
        this.servers = new LinkedHashMap<>();
        this.profiles = new LinkedHashMap<>();

        load();
    }

    public DataSourceServer getServerByName(String name){
        return servers.get(name);
    }

    public DataSourceProfile getProfileByName(String name){
        return profiles.get(name);
    }

    public Collection<DataSourceServer> getServers(){
        return new ArrayList<>(servers.values());
    }

    public Collection<DataSourceProfile> getProfiles(){
        return new ArrayList<>(profiles.values());
    }

    public Collection<DataSourceProfile> getServerProfiles(DataSourceServer server){
        return profiles
            .values()
            .stream()
            .filter(p -> p.getDataSourceServerName().equals(server.getName()))
            .collect(Collectors.toList());
    }

    public void addServer(DataSourceServer newServer){
        validateServer(newServer);
        servers.put(newServer.getName(), newServer);
    }


    /**
     * It removes server and all related profiles.
     */
    public void removeServer(DataSourceServer server) {
        profiles
            .values()
            .stream()
            .filter(p -> p.getDataSourceServerName().equals(server.getName()))
            .forEach(this::removeProfile);

        servers.remove(server.getName());
    }

    public void addProfile(DataSourceProfile newProfile){
        validateProfile(newProfile);
        profiles.put(newProfile.getName(), newProfile);
    }

    public void removeProfile(DataSourceProfile profile){
        profiles.remove(profile.getName());
    }

    private void load(){
        servers.clear();
        profiles.clear();

        String serializedServers = BuildingsSettings.DATA_SOURCE_SERVERS.get();
        DataSourceServer.fromStringJson(serializedServers).forEach(dss -> servers.put(dss.getName(), dss));

        String serializedProfiles = BuildingsSettings.DATA_SOURCE_PROFILES.get();
        DataSourceProfile.fromStringJson(serializedProfiles).forEach(dsp -> profiles.put(dsp.getName(), dsp));
    }

    private void save(){
        String serializedServers = DataSourceServer.toJson(servers.values()).toString();
        BuildingsSettings.DATA_SOURCE_SERVERS.put(serializedServers);

        String serializedProfiles = DataSourceProfile.toJson(profiles.values()).toString();
        BuildingsSettings.DATA_SOURCE_PROFILES.put(serializedProfiles);
    }

    private void validateServer(DataSourceServer newServer) throws IllegalArgumentException {
        if (servers.containsKey(newServer.getName())){
            throw new IllegalArgumentException("DataSourceServer name must be unique!");
        }
    }
    private void validateProfile(DataSourceProfile profile) throws IllegalArgumentException {
        if (profiles.containsKey(profile.getName())){
            throw new IllegalArgumentException("DataSourceProfile name must be unique!");
        }
        assert servers.containsKey(profile.getDataSourceServerName());
    }
}
