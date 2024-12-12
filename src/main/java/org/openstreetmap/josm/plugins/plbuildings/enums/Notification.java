package org.openstreetmap.josm.plugins.plbuildings.enums;

import jakarta.annotation.Nullable;

public enum Notification {
    NO_DATA("no_data", ImportStatus.NO_DATA.toString(), NotificationType.IMPORT_STATUS),
    NO_UPDATE("no_update", ImportStatus.NO_UPDATE.toString(), NotificationType.IMPORT_STATUS),
    CONNECTION_ERROR("connection_error", ImportStatus.CONNECTION_ERROR.toString(), NotificationType.IMPORT_STATUS),
    IMPORT_ERROR("import_error", ImportStatus.IMPORT_ERROR.toString(), NotificationType.IMPORT_STATUS);

    private final String name;
    private final String label;
    private final NotificationType type;

    Notification(final String name, final String label, NotificationType type) {
        this.name = name;
        this.label = label;
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public NotificationType getType() {
        return type;
    }

    public static Notification fromName(String name) {
        for (Notification n : Notification.values()) {
            if (n.name.equalsIgnoreCase(name)) {
                return n;
            }
        }
        throw new IllegalArgumentException("No constant with text " + name + " found");
    }

    @Nullable
    public static Notification fromImportStatus(ImportStatus status) {
        switch (status) {
            case NO_DATA:
                return NO_DATA;
            case NO_UPDATE:
                return NO_UPDATE;
            case CONNECTION_ERROR:
                return CONNECTION_ERROR;
            case IMPORT_ERROR:
                return IMPORT_ERROR;
            default:
                return null;
        }
    }
}
