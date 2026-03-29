package gui;

import model.Priority;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for adding a new task.
 * Returns title, deadline, and priority to the caller if confirmed.
 */
public class AddTaskDialog extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Result fields (null if user cancelled)
    private String    resultTitle;
    private LocalDate resultDeadline;
    private Priority  resultPriority;
    private boolean   confirmed = false;

    // Input components
    private final JTextField titleField    = new JTextField(25);
    private final JTextField deadlineField = new JTextField(12);
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());

    public AddTaskDialog(Frame parent) {
        super(parent, "Add New Task", true);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(20, 20, 10, 20));

        // ── Form ──────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints lc = labelConstraints();
        GridBagConstraints fc = fieldConstraints();

        lc.gridy = 0; fc.gridy = 0;
        form.add(new JLabel("Task Title:"), lc);
        form.add(titleField, fc);

        lc.gridy = 1; fc.gridy = 1;
        JLabel deadlineLbl = new JLabel("Deadline:");
        form.add(deadlineLbl, lc);
        JPanel deadlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        deadlineField.setText(LocalDate.now().plusDays(1).format(DATE_FMT));
        deadlinePanel.add(deadlineField);
        deadlinePanel.add(Box.createHorizontalStrut(6));
        deadlinePanel.add(new JLabel("(yyyy-MM-dd)"));
        fc.gridy = 1;
        form.add(deadlinePanel, fc);

        lc.gridy = 2; fc.gridy = 2;
        form.add(new JLabel("Priority:"), lc);
        priorityBox.setSelectedItem(Priority.MEDIUM);
        form.add(priorityBox, fc);

        root.add(form, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton addBtn    = styledButton("Add Task",  new Color(52, 152, 219), Color.WHITE);
        JButton cancelBtn = styledButton("Cancel",    new Color(189, 195, 199), Color.DARK_GRAY);

        addBtn.addActionListener(e -> onAdd());
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(cancelBtn);
        buttons.add(addBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(addBtn);
    }

    // ── Event Handlers ────────────────────────────────────────────────────────

    private void onAdd() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError("Task title cannot be empty.");
            titleField.requestFocus();
            return;
        }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(deadlineField.getText().trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            showError("Invalid date. Use format: yyyy-MM-dd\nExample: 2025-06-15");
            deadlineField.requestFocus();
            return;
        }

        resultTitle    = title;
        resultDeadline = deadline;
        resultPriority = (Priority) priorityBox.getSelectedItem();
        confirmed      = true;
        dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    // ── Result Accessors ──────────────────────────────────────────────────────

    public boolean    isConfirmed()    { return confirmed; }
    public String     getResultTitle() { return resultTitle; }
    public LocalDate  getResultDeadline() { return resultDeadline; }
    public Priority   getResultPriority() { return resultPriority; }

    // ── Layout Helpers ────────────────────────────────────────────────────────

    private GridBagConstraints labelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 0;
        c.anchor  = GridBagConstraints.WEST;
        c.insets  = new Insets(6, 0, 6, 12);
        c.fill    = GridBagConstraints.NONE;
        return c;
    }

    private GridBagConstraints fieldConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 1;
        c.anchor  = GridBagConstraints.WEST;
        c.insets  = new Insets(6, 0, 6, 0);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        return c;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
