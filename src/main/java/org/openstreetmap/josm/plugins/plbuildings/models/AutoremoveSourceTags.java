package org.openstreetmap.josm.plugins.plbuildings.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;

public class AutoremoveSourceTags {
    public static final String SOURCE_VALUES = "source_values";

    private ArrayList<String> sourceValues;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public AutoremoveSourceTags() {
        BuildingsSettings.UNWANTED_SOURCE_VALUES.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(SOURCE_VALUES, null, sourceValues);
        });
        load();
    }

    private void load() {
        this.sourceValues = new ArrayList<>(BuildingsSettings.UNWANTED_SOURCE_VALUES.get());
        Collections.sort(sourceValues);
    }

    private void save() {
        BuildingsSettings.UNWANTED_SOURCE_VALUES.put(sourceValues);
    }

    public void addSourceValue(String value) {
        sourceValues.add(value);
        Collections.sort(sourceValues);
        save();
    }

    public void removeSourceValue(String value) {
        sourceValues.remove(value);
        Collections.sort(sourceValues);
        save();
    }

    public List<String> getSourceValues() {
        return new ArrayList<>(sourceValues);
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
