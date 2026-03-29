package manager;

import model.DiaryEntry;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Manages all diary entry operations: writing, viewing, and deleting entries.
 * Entries are keyed by date and persisted to data/diary.txt.
 * Only one entry is allowed per date; writing again on the same date overwrites.
 */
public class DiaryManager {

    private static final String FILE_PATH = "data/diary.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // LinkedHashMap preserves insertion order (chronological)
    private final Map<LocalDate, DiaryEntry> entries = new LinkedHashMap<>();

    public DiaryManager() {
        loadFromFile();
    }

    // ── Core Operations ───────────────────────────────────────────────────────

    /**
     * Saves (or overwrites) a diary entry for the given date.
     */
    public void writeEntry(LocalDate date, String content) {
        entries.put(date, new DiaryEntry(date, content));
        saveToFile();
    }

    public void deleteEntry(LocalDate date) {
        entries.remove(date);
        saveToFile();
    }

    // ── GUI Data Access ───────────────────────────────────────────────────────

    /** Returns the entry for a given date, or null if none exists. */
    public DiaryEntry getEntry(LocalDate date) {
        return entries.get(date);
    }

    /** Returns all entries sorted newest-first. */
    public List<DiaryEntry> getAllEntriesSorted() {
        return entries.values().stream()
                .sorted(Comparator.comparing(DiaryEntry::getDate).reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    // ── File Persistence ──────────────────────────────────────────────────────

    /**
     * Writes all entries to disk, one entry per line.
     */
    private void saveToFile() {
        new File("data").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (DiaryEntry entry : entries.values()) {
                pw.println(entry.toFileString());
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not save diary: " + e.getMessage());
        }
    }

    /**
     * Reads all diary entries from disk at startup.
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
                        DiaryEntry entry = DiaryEntry.fromFileString(line);
                        entries.put(entry.getDate(), entry);
                    } catch (Exception e) {
                        System.out.println("  [Warning] Skipping corrupted diary entry.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not load diary: " + e.getMessage());
        }
    }
}
