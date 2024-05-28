package org.openstreetmap.josm.plugins.plbuildings.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;

public class UncommonTags {
    public static final String COMMON_BUILDING_VALUES = "common_building_values";

    private static UncommonTags instance;

    private ArrayList<String> commonBuildingValues;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    private UncommonTags() {
        load();
    }

    public static UncommonTags getInstance() {
        if (instance == null) {
            instance = new UncommonTags();
        }
        return instance;
    }

    private void load() {
        this.commonBuildingValues = new ArrayList<>(BuildingsSettings.COMMON_BUILDING_TAGS.get());
        Collections.sort(commonBuildingValues);
    }

    private void save() {
        BuildingsSettings.COMMON_BUILDING_TAGS.put(commonBuildingValues);
    }

    public void addCommonBuildingValue(String value) {
        commonBuildingValues.add(value);
        Collections.sort(commonBuildingValues);
        save();
        propertyChangeSupport.firePropertyChange(COMMON_BUILDING_VALUES, null, commonBuildingValues);
    }

    public void removeCommonBuildingValue(String value) {
        commonBuildingValues.remove(value);
        Collections.sort(commonBuildingValues);
        save();
        propertyChangeSupport.firePropertyChange(COMMON_BUILDING_VALUES, null, commonBuildingValues);
    }

    public List<String> getCommonBuildingValues() {
        return new ArrayList<>(commonBuildingValues);
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
