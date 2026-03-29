package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single task with a title, deadline, priority, and completion status.
 * Supports serialization to/from a pipe-delimited file format.
 */
public class Task {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static int idCounter = 1;

    private final int id;
    private final String title;
    private final LocalDate deadline;
    private final Priority priority;
    private boolean completed;

    /**
     * Constructor for creating a brand-new task (auto-assigns ID).
     */
    public Task(String title, LocalDate deadline, Priority priority) {
        this.id       = idCounter++;
        this.title    = title;
        this.deadline = deadline;
        this.priority = priority;
        this.completed = false;
    }

    /**
     * Constructor used when loading tasks from a file (explicit ID).
     */
    public Task(int id, String title, LocalDate deadline, Priority priority, boolean completed) {
        this.id        = id;
        this.title     = title;
        this.deadline  = deadline;
        this.priority  = priority;
        this.completed = completed;
        if (id >= idCounter) idCounter = id + 1;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int       getId()        { return id; }
    public String    getTitle()     { return title; }
    public LocalDate getDeadline()  { return deadline; }
    public Priority  getPriority()  { return priority; }
    public boolean   isCompleted()  { return completed; }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * A task is overdue if it is not yet completed and its deadline has passed.
     */
    public boolean isOverdue() {
        return !completed && deadline.isBefore(LocalDate.now());
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    /**
     * Converts this task to a single-line pipe-delimited string for file storage.
     */
    public String toFileString() {
        return id + "|" + title + "|" + deadline.format(DATE_FMT) + "|" + priority + "|" + completed;
    }

    /**
     * Parses a pipe-delimited line back into a Task object.
     */
    public static Task fromFileString(String line) {
        String[] parts = line.split("\\|", 5);
        int      id        = Integer.parseInt(parts[0].trim());
        String   title     = parts[1].trim();
        LocalDate deadline = LocalDate.parse(parts[2].trim(), DATE_FMT);
        Priority priority  = Priority.fromString(parts[3].trim());
        boolean  completed = Boolean.parseBoolean(parts[4].trim());
        return new Task(id, title, deadline, priority, completed);
    }

    // ── Display ───────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        String status;
        if (completed)      status = "[DONE]";
        else if (isOverdue()) status = "[OVERDUE]";
        else                  status = "[PENDING]";

        return String.format("ID: %-3d | %-35s | Due: %s | Priority: %-6s | %s",
                id, title, deadline.format(DATE_FMT), priority, status);
    }
}
