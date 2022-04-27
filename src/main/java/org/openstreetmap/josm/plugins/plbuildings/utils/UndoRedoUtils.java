package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;

public class UndoRedoUtils {
    /**
     * Undo all commands until specific command object.
     * @param handler instance
     * @param untilCmd command to which it will be undone
     * @param inclusive undo untilCmd too
     */
    public static void undoUntil(UndoRedoHandler handler, Command untilCmd, boolean inclusive) {
        while (handler.hasUndoCommands()){
            if (handler.getLastCommand() == untilCmd){
                if (inclusive){
                    handler.undo();
                }
                break;
            }

            handler.undo();
        }
    }
}
