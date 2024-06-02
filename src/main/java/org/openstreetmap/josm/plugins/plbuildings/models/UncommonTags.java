package org.openstreetmap.josm.plugins.plbuildings.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;

public class UncommonTags {
    public static final String COMMON_BUILDING_VALUES = "common_building_values";

    private ArrayList<String> commonBuildingValues;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public UncommonTags() {
        BuildingsSettings.COMMON_BUILDING_TAGS.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(COMMON_BUILDING_VALUES, null, commonBuildingValues);
        });
        load();
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
    }

    public void removeCommonBuildingValue(String value) {
        commonBuildingValues.remove(value);
        Collections.sort(commonBuildingValues);
        save();
    }

    public List<String> getCommonBuildingValues() {
        return new ArrayList<>(commonBuildingValues);
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
