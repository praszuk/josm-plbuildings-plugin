package org.openstreetmap.josm.plugins.plbuildings.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.openstreetmap.josm.command.Command;


/**
 * Extension to JOSM Command classes which allow to return error message after command execution.
 * Be default JOSM returns only boolean value there (.executeCommand())
 */
public interface CommandWithErrorReason {
    /**
     * @return description/full error message reason which could be shown to the user
     *     It's being helpful to run in for commands used in SequenceCommand (chained).
     */
    String getErrorReason();

    static String getLatestErrorReason(Collection<Command> commands) {
        ArrayList<Command> reversedCommands = new ArrayList<>(commands);
        Collections.reverse(reversedCommands);
        return reversedCommands.stream().filter(obj -> obj instanceof CommandWithErrorReason)
            .map(obj -> ((CommandWithErrorReason) obj).getErrorReason())
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow();
    }
}
