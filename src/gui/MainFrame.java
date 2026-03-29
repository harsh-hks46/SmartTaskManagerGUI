package gui;

import manager.DiaryManager;
import manager.TaskManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * The main application window.
 * Contains a header banner and a tabbed pane with Task and Diary panels.
 */
public class MainFrame extends JFrame {

    private final TaskManager  taskManager  = new TaskManager();
    private final DiaryManager diaryManager = new DiaryManager();

    private TaskPanel  taskPanel;
    private DiaryPanel diaryPanel;

    public MainFrame() {
        super("Smart Task Manager with Daily Diary");
        buildUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 580);
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(null);  // centre on screen
        setVisible(true);
    }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Header banner ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("Smart Task Manager  +  Daily Diary");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        JLabel dateLbl = new JLabel(today);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(new Color(189, 195, 199));

        header.add(titleLbl, BorderLayout.WEST);
        header.add(dateLbl,  BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Tabbed pane ───────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        taskPanel  = new TaskPanel(taskManager, this);
        diaryPanel = new DiaryPanel(diaryManager);

        tabs.addTab("📋  Tasks", taskPanel);
        tabs.addTab("📖  Daily Diary", diaryPanel);

        // Refresh task table when switching back to the Tasks tab
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) taskPanel.refresh();
            if (tabs.getSelectedIndex() == 1) diaryPanel.refreshSidebar();
        });

        add(tabs, BorderLayout.CENTER);

        // ── Status bar ────────────────────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        statusBar.setBackground(new Color(236, 240, 241));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));
        JLabel statusLbl = new JLabel("Data is saved automatically to the /data folder.");
        statusLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLbl.setForeground(new Color(120, 120, 120));
        statusBar.add(statusLbl);
        add(statusBar, BorderLayout.SOUTH);
    }
}
