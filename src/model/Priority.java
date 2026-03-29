package model;

/**
 * Represents the priority level of a task.
 */
public enum Priority {
    LOW, MEDIUM, HIGH;

    /**
     * Parses a string into a Priority value, case-insensitively.
     * Defaults to MEDIUM if unrecognized.
     */
    public static Priority fromString(String s) {
        try {
            return Priority.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM;
        }
    }
}
