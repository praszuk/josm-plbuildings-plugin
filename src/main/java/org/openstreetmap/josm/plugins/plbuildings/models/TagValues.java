package org.openstreetmap.josm.plugins.plbuildings.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openstreetmap.josm.data.preferences.ListProperty;

public class TagValues {
    public static final String VALUES_CHANGED = "values_changed";

    private ArrayList<String> values;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final ListProperty josmValuesSetting;

    public TagValues(ListProperty josmValuesSetting) {
        this.josmValuesSetting = josmValuesSetting;

        josmValuesSetting.addListener(valueChangeEvent -> {
            load();
            propertyChangeSupport.firePropertyChange(VALUES_CHANGED, null, values);
        });
        load();
    }

    private void load() {
        values = new ArrayList<>(josmValuesSetting.get());
        Collections.sort(values);
    }

    private void save() {
        josmValuesSetting.put(values);
    }

    public void addValue(String value) {
        values.add(value);
        Collections.sort(values);
        save();
    }

    public void removeValue(String value) {
        values.remove(value);
        Collections.sort(values);
        save();
    }

    public List<String> getValues() {
        return new ArrayList<>(values);
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
