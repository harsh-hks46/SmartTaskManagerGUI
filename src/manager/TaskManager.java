package manager;

import model.Priority;
import model.Task;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all task operations: adding, viewing, completing, and deleting tasks.
 * Tasks are persisted to a local flat file (data/tasks.txt).
 */
public class TaskManager {

    private static final String FILE_PATH = "data/tasks.txt";

    private final List<Task> tasks = new ArrayList<>();

    public TaskManager() {
        loadFromFile();
    }

    // ── Core Operations ───────────────────────────────────────────────────────

    /**
     * Creates a new task and saves it to disk.
     */
    public void addTask(String title, LocalDate deadline, Priority priority) {
        Task task = new Task(title, deadline, priority);
        tasks.add(task);
        saveToFile();
    }

    public void markCompleted(int id) {
        findById(id).ifPresent(t -> { t.setCompleted(true); saveToFile(); });
    }

    public void deleteTask(int id) {
        findById(id).ifPresent(t -> { tasks.remove(t); saveToFile(); });
    }

    // ── Legacy CLI views (kept for backward compatibility) ───────────────────
    public void viewAllTasks()     { tasks.forEach(System.out::println); }
    public void viewPendingTasks() { tasks.stream().filter(t -> !t.isCompleted()).forEach(System.out::println); }
    public void viewOverdueTasks() { tasks.stream().filter(Task::isOverdue).forEach(System.out::println); }

    // ── GUI Data Access ───────────────────────────────────────────────────────

    /**
     * Returns a filtered and sorted list of tasks for the GUI table.
     * @param filter "ALL", "PENDING", "OVERDUE", or "DONE"
     */
    public List<Task> getTasksFiltered(String filter) {
        return tasks.stream()
                .filter(t -> {
                    switch (filter) {
                        case "PENDING": return !t.isCompleted() && !t.isOverdue();
                        case "OVERDUE": return t.isOverdue();
                        case "DONE":    return t.isCompleted();
                        default:        return true;
                    }
                })
                .sorted(Comparator.comparing(Task::getPriority).reversed()
                                  .thenComparing(Task::getDeadline))
                .collect(Collectors.toList());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Optional<Task> findById(int id) {
        return tasks.stream().filter(t -> t.getId() == id).findFirst();
    }

    // ── File Persistence ──────────────────────────────────────────────────────

    /**
     * Writes all tasks to the flat file, overwriting previous content.
     */
    private void saveToFile() {
        new File("data").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Task task : tasks) {
                pw.println(task.toFileString());
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not save tasks: " + e.getMessage());
        }
    }

    /**
     * Reads tasks from the flat file on startup.
     * Corrupted lines are skipped with a warning.
     */
    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        tasks.add(Task.fromFileString(line));
                    } catch (Exception e) {
                        System.out.println("  [Warning] Skipping corrupted task entry.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not load tasks: " + e.getMessage());
        }
    }
}
