package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.Pair;

import java.util.*;

public class TagConflictUtils {
    /**
     * Compare tags on both objects and generate list which contains only conflict tags List<old, new>
     */
    public static List<Pair<Tag, Tag>> getConflictTags(OsmPrimitive oldPrimitive, OsmPrimitive newPrimitive){
        List<Pair<Tag, Tag>> conflicts = new ArrayList<>();

        newPrimitive.getKeys().forEach((key, value) -> {
            if (oldPrimitive.hasKey(key) && !oldPrimitive.get(key).equals(value)){
                conflicts.add(new Pair<>(new Tag(key, oldPrimitive.get(key)), new Tag(key, value)));
            }
        });
        return conflicts;
    }

    /**
     * Checks if tag can be replaced without user asking about it.
     * It compares it with noConflictTags Set.
     * There are available wildcard for key only.
     * If Set contains tag("key", "*") with * as value, then whole tag for replace is allowed
     */
    public static boolean isConflictTagReplaceIsAllowed(Tag tag, Set<Tag> noConflictTags){
        return noConflictTags.contains(new Tag(tag.getKey(), "*")) || noConflictTags.contains(tag);
    }

    /**
     * It allows to skip tag merging conflicts using noConflictTagMap
     * @param oldBuilding – current existing building in dataset
     * @param newBuilding – imported building
     * @param noConflictTagList – config TagMap which contains allowed tags – they will be replaced without producing
     * any conflict later
     * @return ChangePropertyCommand with changed tags
     */
    public static ChangePropertyCommand replaceNoConflictTags(
            OsmPrimitive oldBuilding,
            OsmPrimitive newBuilding,
            List<String> noConflictTagList
    ){
        Set<Tag> noConflictTag = new HashSet<>();
        for (int i = 0; i < noConflictTagList.size() - 1; i += 2){
            noConflictTag.add(new Tag(noConflictTagList.get(i), noConflictTagList.get(i + 1)));
        }

        Map<String, String> newTags = new HashMap<>();
        getConflictTags(oldBuilding, newBuilding)
            .forEach(conflictPair -> {
                // check if old tag can be replaced in conflict without asking user
                if (isConflictTagReplaceIsAllowed(conflictPair.a, noConflictTag)){
                    newTags.put(conflictPair.b.getKey(), conflictPair.b.getValue()); // replace old value by new
                }
            });

        return newTags.isEmpty() ? null:new ChangePropertyCommand(Collections.singletonList(oldBuilding), newTags);
    }
}