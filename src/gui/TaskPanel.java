package gui;

import manager.TaskManager;
import model.Priority;
import model.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The Tasks tab — displays all tasks in a sortable table with
 * Add / Complete / Delete actions and a filter toolbar.
 */
public class TaskPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Column indices
    private static final int COL_ID       = 0;
    private static final int COL_TITLE    = 1;
    private static final int COL_DEADLINE = 2;
    private static final int COL_PRIORITY = 3;
    private static final int COL_STATUS   = 4;

    private final TaskManager taskManager;
    private final Frame       parentFrame;

    private DefaultTableModel tableModel;
    private JTable            table;

    // Filter state
    private String currentFilter = "ALL";

    public TaskPanel(TaskManager taskManager, Frame parentFrame) {
        this.taskManager = taskManager;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 0));
        buildUI();
        refresh();
    }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void buildUI() {
        // ── Top toolbar (filter buttons) ──────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterBar.setBackground(new Color(245, 246, 250));
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JLabel filterLbl = new JLabel("Show:");
        filterLbl.setFont(filterLbl.getFont().deriveFont(Font.BOLD));
        filterBar.add(filterLbl);

        for (String filter : new String[]{"ALL", "PENDING", "OVERDUE", "DONE"}) {
            JButton btn = filterButton(filter);
            filterBar.add(btn);
        }

        add(filterBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"ID", "Task Title", "Deadline", "Priority", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(174, 214, 241));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Column widths
        table.getColumnModel().getColumn(COL_ID).setMaxWidth(50);
        table.getColumnModel().getColumn(COL_TITLE).setPreferredWidth(260);
        table.getColumnModel().getColumn(COL_DEADLINE).setPreferredWidth(100);
        table.getColumnModel().getColumn(COL_PRIORITY).setPreferredWidth(80);
        table.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(90);

        // Custom row renderer: color overdue rows red, done rows grey
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String status = (String) tableModel.getValueAt(row, COL_STATUS);
                if (!sel) {
                    if ("OVERDUE".equals(status)) {
                        c.setBackground(new Color(255, 235, 235));
                        c.setForeground(new Color(180, 30, 30));
                    } else if ("DONE".equals(status)) {
                        c.setBackground(new Color(240, 255, 240));
                        c.setForeground(new Color(100, 140, 100));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 252));
                        c.setForeground(Color.BLACK);
                    }
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // ── Bottom action bar ─────────────────────────────────────────────────
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actionBar.setBackground(new Color(245, 246, 250));
        actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton addBtn      = actionButton("＋  Add Task",          new Color(39, 174, 96),  Color.WHITE);
        JButton completeBtn = actionButton("✔  Mark Complete",     new Color(52, 152, 219), Color.WHITE);
        JButton deleteBtn   = actionButton("✖  Delete Task",       new Color(231, 76, 60),  Color.WHITE);
        JButton refreshBtn  = actionButton("↺  Refresh",           new Color(149, 165, 166), Color.WHITE);

        addBtn.addActionListener(e -> onAdd());
        completeBtn.addActionListener(e -> onComplete());
        deleteBtn.addActionListener(e -> onDelete());
        refreshBtn.addActionListener(e -> refresh());

        actionBar.add(addBtn);
        actionBar.add(completeBtn);
        actionBar.add(deleteBtn);
        actionBar.add(Box.createHorizontalStrut(10));
        actionBar.add(refreshBtn);

        add(actionBar, BorderLayout.SOUTH);
    }

    // ── Data Loading ──────────────────────────────────────────────────────────

    /**
     * Reloads task data from TaskManager and repopulates the table.
     */
    public void refresh() {
        tableModel.setRowCount(0);
        List<Task> tasks = taskManager.getTasksFiltered(currentFilter);
        for (Task t : tasks) {
            String status;
            if (t.isCompleted())    status = "DONE";
            else if (t.isOverdue()) status = "OVERDUE";
            else                    status = "PENDING";

            tableModel.addRow(new Object[]{
                t.getId(),
                t.getTitle(),
                t.getDeadline().format(DATE_FMT),
                t.getPriority(),
                status
            });
        }
    }

    // ── Action Handlers ───────────────────────────────────────────────────────

    private void onAdd() {
        AddTaskDialog dialog = new AddTaskDialog(parentFrame);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            taskManager.addTask(
                dialog.getResultTitle(),
                dialog.getResultDeadline(),
                dialog.getResultPriority()
            );
            refresh();
            showSuccess("Task \"" + dialog.getResultTitle() + "\" added!");
        }
    }

    private void onComplete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a task to mark as completed.");
            return;
        }
        int id     = (int) tableModel.getValueAt(selectedRow, COL_ID);
        String title = (String) tableModel.getValueAt(selectedRow, COL_TITLE);
        String status = (String) tableModel.getValueAt(selectedRow, COL_STATUS);
        if ("DONE".equals(status)) {
            showWarning("This task is already marked as completed.");
            return;
        }
        taskManager.markCompleted(id);
        refresh();
        showSuccess("Task \"" + title + "\" marked as complete! ✔");
    }

    private void onDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a task to delete.");
            return;
        }
        String title = (String) tableModel.getValueAt(selectedRow, COL_TITLE);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete task: \"" + title + "\"?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(selectedRow, COL_ID);
            taskManager.deleteTask(id);
            refresh();
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private JButton filterButton(String filter) {
        JButton btn = new JButton(filter);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 12f));
        btn.addActionListener(e -> {
            currentFilter = filter;
            refresh();
        });
        return btn;
    }

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

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}
