package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single diary entry associated with a specific date.
 * Multi-line content is encoded as a single line for file storage
 * using escaped newline characters (\n).
 */
public class DiaryEntry {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LocalDate date;
    private String content;

    public DiaryEntry(LocalDate date, String content) {
        this.date    = date;
        this.content = content;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public LocalDate getDate()    { return date; }
    public String    getContent() { return content; }

    public void setContent(String content) {
        this.content = content;
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    /**
     * Encodes this entry as a single pipe-delimited line.
     * Newlines within the content are stored as the two-character sequence \n.
     */
    public String toFileString() {
        String encoded = content
                .replace("\\", "\\\\")   // escape existing backslashes first
                .replace("\n", "\\n");    // then encode newlines
        return date.format(DATE_FMT) + "|" + encoded;
    }

    /**
     * Reconstructs a DiaryEntry from a stored line.
     */
    public static DiaryEntry fromFileString(String line) {
        int sep = line.indexOf('|');
        if (sep == -1) throw new IllegalArgumentException("Malformed diary line: " + line);

        LocalDate date    = LocalDate.parse(line.substring(0, sep).trim(), DATE_FMT);
        String    encoded = line.substring(sep + 1);
        String    content = encoded
                .replace("\\n", "\n")     // restore newlines
                .replace("\\\\", "\\");   // restore backslashes
        return new DiaryEntry(date, content);
    }

    // ── Display ───────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Date: " + date.format(DATE_FMT) + "\n" + content;
    }
}
