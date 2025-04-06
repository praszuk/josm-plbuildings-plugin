package org.openstreetmap.josm.plugins.plbuildings.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;
import org.openstreetmap.josm.tools.Pair;


/**
 * Extension to JOSM Command classes which allow to return error message and status after command execution.
 * Be default JOSM returns only boolean value there (.executeCommand())
 */
public interface CommandWithErrorReason {
    /**
     * @return description/full error message reason which could be shown to the user
     *     It's being helpful to run in for commands used in SequenceCommand (chained).
     */
    String getErrorReason();

    ImportStatus getErrorStatus();

    static Pair<String, ImportStatus> getLatestErrorReasonStatus(Collection<Command> commands) {
        ArrayList<Command> reversedCommands = new ArrayList<>(commands);
        Collections.reverse(reversedCommands);
        return reversedCommands.stream().filter(obj -> obj instanceof CommandWithErrorReason)
            .map(obj -> new Pair<>(
                ((CommandWithErrorReason) obj).getErrorReason(), ((CommandWithErrorReason) obj).getErrorStatus())
            )
            .filter(obj -> obj.a != null && obj.b != null)
            .findFirst()
            .orElseThrow();
    }
}
