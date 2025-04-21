package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;
import org.openstreetmap.josm.tools.Pair;

/**
 * It shows when user need to decide what strategy to use if buildings overlap less than setting threshold.
 */
public class ImportedBuildingOverlappingOptionDialog {
    private boolean doNotShowAgainThisSession;
    private CombineNearestOverlappingStrategy userConfirmedStrategy;

    private final String geometryDataSource;
    private final String tagsDataSource;
    private final double overlapPercentage;


    public ImportedBuildingOverlappingOptionDialog(
        String geometryDataSource, String tagsDataSource, double overlapPercentage) {
        this.geometryDataSource = geometryDataSource;
        this.tagsDataSource = tagsDataSource;
        this.overlapPercentage = overlapPercentage;
    }

    public void show() {
        ArrayList<Pair<CombineNearestOverlappingStrategy, Object>> choicesPairs = new ArrayList<>(Arrays.asList(
            Pair.create(CombineNearestOverlappingStrategy.MERGE_BOTH, tr("Merge both")),
            Pair.create(
                CombineNearestOverlappingStrategy.ACCEPT_GEOMETRY_SOURCE,
                String.format(tr("Use {0}"), geometryDataSource)
            ),
            Pair.create(
                CombineNearestOverlappingStrategy.ACCEPT_TAGS_SOURCE,
                String.format(tr("Use {0}"), tagsDataSource)
            ),
            Pair.create(CombineNearestOverlappingStrategy.CANCEL, tr("Cancel"))
        ));
        Object[] choices = choicesPairs.stream().map(pair -> pair.b).toArray();

        JPanel panel = new JPanel(new BorderLayout());
        JCheckBox doNotShowAgainThisSessionCheckBox = new JCheckBox(tr("Do not show again (this session)."));
        panel.add(
            new JLabel(tr(
                "Buildings from different data sources are not overlapping enough ({0}).",
                String.format("%.2f%%", overlapPercentage)
            )),
            BorderLayout.CENTER
        );
        panel.add(doNotShowAgainThisSessionCheckBox, BorderLayout.SOUTH);

        int result = JOptionPane.showOptionDialog(
            null,
            panel,
            tr("Building import confirmation"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices,
            choices[0]
        );
        doNotShowAgainThisSession = doNotShowAgainThisSessionCheckBox.isSelected();

        if (result < 0 || choicesPairs.get(result).a == CombineNearestOverlappingStrategy.CANCEL) {
            userConfirmedStrategy = CombineNearestOverlappingStrategy.CANCEL;
            return;
        }
        userConfirmedStrategy = choicesPairs.get(result).a;
    }

    public boolean isDoNotShowAgainThisSession() {
        return doNotShowAgainThisSession;
    }

    public CombineNearestOverlappingStrategy getUserConfirmedStrategy() {
        return userConfirmedStrategy;
    }
}
