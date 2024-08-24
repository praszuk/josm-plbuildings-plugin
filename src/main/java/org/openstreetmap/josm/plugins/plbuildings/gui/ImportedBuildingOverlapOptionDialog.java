package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestStrategy;
import org.openstreetmap.josm.tools.Pair;

/**
 * It shows when user need to decide what strategy to use if buildings overlap less than setting threshold.
 */
public class ImportedBuildingOverlapOptionDialog {
    public static CombineNearestStrategy show(String geomDs, String tagsDs, double overlapPercentage) {
        ArrayList<Pair<CombineNearestStrategy, Object>> choicesPairs = new ArrayList<>(Arrays.asList(
            Pair.create(CombineNearestStrategy.ACCEPT, tr("Merge both")),
            Pair.create(CombineNearestStrategy.ACCEPT_GEOMETRY, String.format(tr("Use %s (geometry)"), geomDs)),
            Pair.create(CombineNearestStrategy.ACCEPT_TAGS, String.format(tr("Use %s (tags)"), tagsDs)),
            Pair.create(CombineNearestStrategy.CANCEL, tr("Cancel"))
        ));
        Object[] choices = choicesPairs.stream().map(pair -> pair.b).toArray();

        int result = JOptionPane.showOptionDialog(
            null,
            tr("Buildings from different data sources are not overlapping enough")
                + String.format(" (%.2f%%).", overlapPercentage),
            tr("Building import confirmation"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices,
            choices[0]
        );
        if (result < 0 || choicesPairs.get(result).a == CombineNearestStrategy.CANCEL) {
            return CombineNearestStrategy.CANCEL;
        }
        return choicesPairs.get(result).a;
    }
}
