package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;
import org.openstreetmap.josm.tools.Pair;

/**
 * It shows when user need to decide what strategy to use if buildings overlap less than setting threshold.
 */
public class ImportedBuildingOverlappingOptionDialog {
    public static CombineNearestOverlappingStrategy show(String geomDs, String tagsDs, double overlapPercentage) {
        ArrayList<Pair<CombineNearestOverlappingStrategy, Object>> choicesPairs = new ArrayList<>(Arrays.asList(
            Pair.create(CombineNearestOverlappingStrategy.MERGE_BOTH, tr("Merge both")),
            Pair.create(CombineNearestOverlappingStrategy.ACCEPT_GEOMETRY_SOURCE, String.format(tr("Use %s"), geomDs)),
            Pair.create(CombineNearestOverlappingStrategy.ACCEPT_TAGS_SOURCE, String.format(tr("Use %s"), tagsDs)),
            Pair.create(CombineNearestOverlappingStrategy.CANCEL, tr("Cancel"))
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
        if (result < 0 || choicesPairs.get(result).a == CombineNearestOverlappingStrategy.CANCEL) {
            return CombineNearestOverlappingStrategy.CANCEL;
        }
        return choicesPairs.get(result).a;
    }
}
