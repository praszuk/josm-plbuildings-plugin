package org.openstreetmap.josm.plugins.plbuildings.models.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportMode;

public class TogleDialogImportModeComboBoxModel implements ComboBoxModel<Object> {
    private final List<Object> items = new ArrayList<>(List.of(ImportMode.values()));
    private Object selectedItem;


    public TogleDialogImportModeComboBoxModel(ImportMode selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    public void setSelectedItem(Object o) {
        this.selectedItem = o;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public Object getElementAt(int i) {
        return items.get(i);
    }

    @Override
    public void addListDataListener(ListDataListener listDataListener) {}

    @Override
    public void removeListDataListener(ListDataListener listDataListener) {}
}
