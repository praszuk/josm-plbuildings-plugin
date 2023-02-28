package org.openstreetmap.josm.plugins.plbuildings.gui;

import org.openstreetmap.josm.plugins.plbuildings.data.CombineNearestStrategy;
import org.openstreetmap.josm.tools.Pair;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * It shows when user need to decide what strategy to use if buildings overlap less than setting threshold.
 */
public class ImportedBuildingOverlapOptionDialog {
    public static CombineNearestStrategy show(String geomDS, String tagsDS, double overlapPercentage){
        ArrayList<Pair<CombineNearestStrategy, Object>> choicesPairs = new ArrayList<>(Arrays.asList(
            Pair.create(CombineNearestStrategy.ACCEPT, tr("Merge both")),
            Pair.create(CombineNearestStrategy.ACCEPT_GEOMETRY, String.format(tr("Use %s (geometry)"), geomDS)),
            Pair.create(CombineNearestStrategy.ACCEPT_TAGS, String.format(tr("Use %s (tags)"), tagsDS)),
            Pair.create(CombineNearestStrategy.CANCEL, tr("Cancel"))
        ));
        Object[] choices = choicesPairs.stream().map(pair -> pair.b).toArray();

        int result = JOptionPane.showOptionDialog(
            null,
            String.format(
                tr("Buildings from different data sources are not overlapping enough (%.2f%%)."),
                overlapPercentage
            ),
            tr("Building import confirmation"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices,
            choices[0]
        );
        if (result < 0 || choicesPairs.get(result).a == CombineNearestStrategy.CANCEL){
            return CombineNearestStrategy.CANCEL;
        }
        return choicesPairs.get(result).a;
    }
}
