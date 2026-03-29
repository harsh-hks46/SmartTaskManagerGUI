package gui;

import manager.DiaryManager;
import model.DiaryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The Daily Diary tab.
 * Left: sidebar listing all diary dates.
 * Right: editor area to write or read an entry.
 */
public class DiaryPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DiaryManager diaryManager;

    // UI Components
    private DefaultListModel<String> sidebarModel;
    private JList<String>            sidebarList;
    private JTextArea                entryArea;
    private JTextField               dateField;
    private JLabel                   statusLabel;

    public DiaryPanel(DiaryManager diaryManager) {
        this.diaryManager = diaryManager;
        setLayout(new BorderLayout());
        buildUI();
        refreshSidebar();
    }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void buildUI() {

        // ══ LEFT SIDEBAR ══════════════════════════════════════════════════════
        sidebarModel = new DefaultListModel<>();
        sidebarList  = new JList<>(sidebarModel);
        sidebarList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sidebarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sidebarList.setFixedCellHeight(34);
        sidebarList.setCellRenderer(new DiaryListCellRenderer());
        sidebarList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSidebarSelect();
        });

        JScrollPane sidebarScroll = new JScrollPane(sidebarList);
        sidebarScroll.setBorder(BorderFactory.createEmptyBorder());
        sidebarScroll.setPreferredSize(new Dimension(155, 0));

        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setBackground(new Color(44, 62, 80));

        JLabel sidebarTitle = new JLabel("  Past Entries");
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sidebarTitle.setForeground(Color.WHITE);
        sidebarTitle.setBorder(new EmptyBorder(12, 8, 12, 8));
        sidebarTitle.setBackground(new Color(44, 62, 80));
        sidebarTitle.setOpaque(true);

        sidebarWrapper.add(sidebarTitle, BorderLayout.NORTH);
        sidebarWrapper.add(sidebarScroll, BorderLayout.CENTER);

        // ══ RIGHT EDITOR ══════════════════════════════════════════════════════
        JPanel editorPanel = new JPanel(new BorderLayout(0, 0));

        // ── Date toolbar ──────────────────────────────────────────────────────
        JPanel dateBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        dateBar.setBackground(new Color(245, 246, 250));
        dateBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JLabel dateLbl = new JLabel("Entry Date:");
        dateLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dateField = new JTextField(LocalDate.now().format(DATE_FMT), 12);
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton todayBtn = smallButton("Today");
        todayBtn.addActionListener(e -> {
            dateField.setText(LocalDate.now().format(DATE_FMT));
            loadEntryForDate(LocalDate.now());
        });

        dateBar.add(dateLbl);
        dateBar.add(dateField);
        dateBar.add(todayBtn);

        editorPanel.add(dateBar, BorderLayout.NORTH);

        // ── Text area ─────────────────────────────────────────────────────────
        entryArea = new JTextArea();
        entryArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        entryArea.setLineWrap(true);
        entryArea.setWrapStyleWord(true);
        entryArea.setMargin(new Insets(16, 16, 16, 16));
        entryArea.setBackground(new Color(255, 253, 245));

        JScrollPane editorScroll = new JScrollPane(entryArea);
        editorScroll.setBorder(BorderFactory.createEmptyBorder());
        editorPanel.add(editorScroll, BorderLayout.CENTER);

        // ── Bottom action bar ─────────────────────────────────────────────────
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(new Color(245, 246, 250));
        actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        btnPanel.setOpaque(false);

        JButton saveBtn   = actionButton("💾  Save Entry",   new Color(39, 174, 96),  Color.WHITE);
        JButton loadBtn   = actionButton("📂  Load Entry",   new Color(52, 152, 219), Color.WHITE);
        JButton deleteBtn = actionButton("✖  Delete Entry", new Color(231, 76, 60),  Color.WHITE);
        JButton clearBtn  = actionButton("✦  Clear",        new Color(149, 165, 166), Color.WHITE);

        saveBtn.addActionListener(e   -> onSave());
        loadBtn.addActionListener(e   -> onLoad());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e  -> entryArea.setText(""));

        btnPanel.add(saveBtn);
        btnPanel.add(loadBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        statusLabel = new JLabel("  ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(100, 100, 100));

        actionBar.add(btnPanel, BorderLayout.WEST);
        actionBar.add(statusLabel, BorderLayout.CENTER);
        editorPanel.add(actionBar, BorderLayout.SOUTH);

        // ══ ASSEMBLE ══════════════════════════════════════════════════════════
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarWrapper, editorPanel);
        split.setDividerLocation(155);
        split.setDividerSize(4);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);

        // Load today's entry on open
        loadEntryForDate(LocalDate.now());
    }

    // ── Action Handlers ───────────────────────────────────────────────────────

    private void onSave() {
        LocalDate date = parseDate();
        if (date == null) return;

        String content = entryArea.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Cannot save an empty entry.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        diaryManager.writeEntry(date, content);
        refreshSidebar();
        setStatus("Entry saved for " + date.format(DATE_FMT) + " ✔");
    }

    private void onLoad() {
        LocalDate date = parseDate();
        if (date == null) return;
        loadEntryForDate(date);
    }

    private void onDelete() {
        LocalDate date = parseDate();
        if (date == null) return;

        DiaryEntry entry = diaryManager.getEntry(date);
        if (entry == null) {
            JOptionPane.showMessageDialog(this,
                "No entry found for " + date.format(DATE_FMT), "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete diary entry for " + date.format(DATE_FMT) + "?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            diaryManager.deleteEntry(date);
            entryArea.setText("");
            refreshSidebar();
            setStatus("Entry deleted.");
        }
    }

    private void onSidebarSelect() {
        String selected = sidebarList.getSelectedValue();
        if (selected == null) return;
        try {
            LocalDate date = LocalDate.parse(selected, DATE_FMT);
            dateField.setText(selected);
            loadEntryForDate(date);
        } catch (DateTimeParseException ignored) {}
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void loadEntryForDate(LocalDate date) {
        DiaryEntry entry = diaryManager.getEntry(date);
        if (entry != null) {
            entryArea.setText(entry.getContent());
            entryArea.setCaretPosition(0);
            setStatus("Loaded entry for " + date.format(DATE_FMT));
        } else {
            entryArea.setText("");
            setStatus("No entry yet for " + date.format(DATE_FMT) + " — start writing!");
        }
    }

    public void refreshSidebar() {
        String selected = sidebarList.getSelectedValue();
        sidebarModel.clear();
        List<DiaryEntry> all = diaryManager.getAllEntriesSorted();
        for (DiaryEntry e : all) {
            sidebarModel.addElement(e.getDate().format(DATE_FMT));
        }
        if (selected != null) sidebarList.setSelectedValue(selected, true);
    }

    private LocalDate parseDate() {
        try {
            return LocalDate.parse(dateField.getText().trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void setStatus(String msg) {
        statusLabel.setText("  " + msg);
        // Auto-clear after 4 seconds
        Timer timer = new Timer(4000, e -> statusLabel.setText("  "));
        timer.setRepeats(false);
        timer.start();
    }

    // ── Inner: Custom list cell renderer ─────────────────────────────────────

    private static class DiaryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            lbl.setBorder(new EmptyBorder(4, 12, 4, 8));
            lbl.setText("📅 " + value);
            if (isSelected) {
                lbl.setBackground(new Color(52, 152, 219));
                lbl.setForeground(Color.WHITE);
            } else {
                lbl.setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 246, 250));
                lbl.setForeground(new Color(44, 62, 80));
            }
            return lbl;
        }
    }

    // ── Button Factories ──────────────────────────────────────────────────────

    private JButton actionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private JButton smallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
