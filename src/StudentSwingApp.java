import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Swing GUI for Student Management using JTable.
 * Persistence: students.ser (serialization).
 * Extra: Export to CSV.
 */
public class StudentSwingApp extends JFrame {
    private static final String STORAGE_FILE = "students.ser";

    // UI models & components
    private DefaultTableModel tableModel;
    private JTable studentTable;

    private JTextField idField = new JTextField(10);
    private JTextField nameField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JTextField courseField = new JTextField(15);
    private JTextField gpaField = new JTextField(6);

    private JTextArea infoArea = new JTextArea(8, 30);

    // Email regex: simple, adequate for basic validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public StudentSwingApp() {
        setTitle("Student Manager (Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 460);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(main);

        // Left panel: JTable
        String[] cols = {"ID", "Name", "Email", "Course", "GPA"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // edits via form only
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setAutoCreateRowSorter(true);
        studentTable.setFillsViewportHeight(true);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(50);

        JScrollPane listScroll = new JScrollPane(studentTable);
        listScroll.setPreferredSize(new Dimension(360, 360));
        main.add(listScroll, BorderLayout.WEST);

        // Right panel: form + buttons + info
        JPanel right = new JPanel(new BorderLayout(8, 8));
        main.add(right, BorderLayout.CENTER);

        // Form using GridBagLayout
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        int y = 0;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("ID:"), gc);
        gc.gridx = 1; form.add(idField, gc);
        y++;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Name:"), gc);
        gc.gridx = 1; form.add(nameField, gc);
        y++;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Email:"), gc);
        gc.gridx = 1; form.add(emailField, gc);
        y++;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Course:"), gc);
        gc.gridx = 1; form.add(courseField, gc);
        y++;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("GPA:"), gc);
        gc.gridx = 1; form.add(gpaField, gc);

        right.add(form, BorderLayout.NORTH);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JButton exportBtn = new JButton("Export CSV");

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(loadBtn);
        btnPanel.add(exportBtn);

        right.add(btnPanel, BorderLayout.CENTER);

        // Info text area
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        right.add(new JScrollPane(infoArea), BorderLayout.SOUTH);

        // Event handlers

        addBtn.addActionListener(e -> {
            String validationError = validateForm(true);
            if (validationError != null) { showMessage(validationError); return; }
            try {
                int id = Integer.parseInt(idField.getText().trim());
                if (findById(id) != null) {
                    showMessage("A student with this ID already exists.");
                    return;
                }
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String course = courseField.getText().trim();
                double gpa = Double.parseDouble(gpaField.getText().trim());
                Student s = new Student(id, name, email, course, gpa);
                tableModel.addRow(s.toTableRow());
                infoArea.setText("Added: " + s);
                clearForm();
            } catch (NumberFormatException ex) {
                showMessage("ID and GPA must be numeric.");
            }
        });

        updateBtn.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row == -1) { showMessage("Select a student to update."); return; }
            int modelRow = studentTable.convertRowIndexToModel(row);

            String validationError = validateForm(false);
            if (validationError != null) { showMessage(validationError); return; }

            try {
                // ID stays as-is; we won't allow changing the ID via update (form may show it, but keep it)
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String course = courseField.getText().trim();
                double gpa = Double.parseDouble(gpaField.getText().trim());

                tableModel.setValueAt(name, modelRow, 1);
                tableModel.setValueAt(email, modelRow, 2);
                tableModel.setValueAt(course, modelRow, 3);
                tableModel.setValueAt(String.format("%.2f", gpa), modelRow, 4);

                infoArea.setText("Updated student at row " + (modelRow + 1));
                clearForm();
            } catch (NumberFormatException ex) {
                showMessage("GPA must be numeric.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row == -1) { showMessage("Select a student to delete."); return; }
            int modelRow = studentTable.convertRowIndexToModel(row);
            String idStr = tableModel.getValueAt(modelRow, 0).toString();
            String name = tableModel.getValueAt(modelRow, 1).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected student?\n" + idStr + " - " + name,
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.removeRow(modelRow);
                infoArea.setText("Deleted student: " + idStr + " - " + name);
                clearForm();
            }
        });

        clearBtn.addActionListener(e -> clearForm());

        saveBtn.addActionListener(e -> {
            saveToFile();
            infoArea.setText("Saved " + tableModel.getRowCount() + " students to " + STORAGE_FILE);
        });

        loadBtn.addActionListener(e -> {
            loadFromFile();
            infoArea.setText("Loaded " + tableModel.getRowCount() + " students from " + STORAGE_FILE);
        });

        exportBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export students to CSV");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
            int r = chooser.showSaveDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                File outFile = chooser.getSelectedFile();
                if (!outFile.getName().toLowerCase().endsWith(".csv")) {
                    outFile = new File(outFile.getParentFile(), outFile.getName() + ".csv");
                }
                try {
                    exportToCsv(outFile);
                    infoArea.setText("Exported to: " + outFile.getAbsolutePath());
                } catch (IOException ex) {
                    showMessage("Export failed: " + ex.getMessage());
                }
            }
        });

        // Table selection -> populate form
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = studentTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = studentTable.convertRowIndexToModel(row);
                    idField.setText(tableModel.getValueAt(modelRow, 0).toString());
                    idField.setEditable(false);
                    nameField.setText(tableModel.getValueAt(modelRow, 1).toString());
                    emailField.setText(tableModel.getValueAt(modelRow, 2).toString());
                    courseField.setText(tableModel.getValueAt(modelRow, 3).toString());
                    gpaField.setText(tableModel.getValueAt(modelRow, 4).toString());
                }
            }
        });

        // Load at startup
        loadFromFile();
    }

    // Validate form: when adding, ID required; when updating, ID field is locked and not validated here
    private String validateForm(boolean isAdding) {
        String idText = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String gpaText = gpaField.getText().trim();

        if (isAdding) {
            if (idText.isEmpty()) return "ID is required.";
            try {
                int id = Integer.parseInt(idText);
                if (id <= 0) return "ID must be a positive integer.";
            } catch (NumberFormatException e) { return "ID must be an integer."; }
        }

        if (name.isEmpty()) return "Name is required.";
        if (email.isEmpty()) return "Email is required.";
        if (!EMAIL_PATTERN.matcher(email).matches()) return "Email format is invalid.";

        if (gpaText.isEmpty()) return "GPA is required.";
        try {
            double gpa = Double.parseDouble(gpaText);
            if (gpa < 0.0 || gpa > 10.0) return "GPA must be between 0.0 and 10.0.";
        } catch (NumberFormatException e) { return "GPA must be numeric."; }

        return null;
    }

    // find by id in table model
    private Student findById(int id) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object o = tableModel.getValueAt(i, 0);
            try {
                int rowId = Integer.parseInt(o.toString());
                if (rowId == id) {
                    // build Student from row
                    Object[] row = new Object[]{
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4)
                    };
                    return Student.fromTableRow(row);
                }
            } catch (Exception ignored) { }
        }
        return null;
    }

    private void clearForm() {
        idField.setText("");
        idField.setEditable(true);
        nameField.setText("");
        emailField.setText("");
        courseField.setText("");
        gpaField.setText("");
        studentTable.clearSelection();
    }

    // Serialization save
    private void saveToFile() {
        List<Student> list = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object[] row = new Object[]{
                    tableModel.getValueAt(i, 0),
                    tableModel.getValueAt(i, 1),
                    tableModel.getValueAt(i, 2),
                    tableModel.getValueAt(i, 3),
                    tableModel.getValueAt(i, 4)
            };
            list.add(Student.fromTableRow(row));
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORAGE_FILE))) {
            oos.writeObject(list);
        } catch (IOException ex) {
            showMessage("Failed to save: " + ex.getMessage());
        }
    }

    // Serialization load
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File f = new File(STORAGE_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                List<Student> list = (List<Student>) obj;
                tableModel.setRowCount(0);
                for (Student s : list) {
                    tableModel.addRow(s.toTableRow());
                }
            }
        } catch (Exception ex) {
            showMessage("Failed to load: " + ex.getMessage());
        }
    }

    // Export CSV (simple)
    private void exportToCsv(File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            bw.write("ID,Name,Email,Course,GPA");
            bw.newLine();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object idObj = tableModel.getValueAt(i, 0);
                Object nameObj = tableModel.getValueAt(i, 1);
                Object emailObj = tableModel.getValueAt(i, 2);
                Object courseObj = tableModel.getValueAt(i, 3);
                Object gpaObj = tableModel.getValueAt(i, 4);

                String id = idObj == null ? "" : idObj.toString();
                String name = csvEscape(nameObj == null ? "" : nameObj.toString());
                String email = csvEscape(emailObj == null ? "" : emailObj.toString());
                String course = csvEscape(courseObj == null ? "" : courseObj.toString());
                String gpa = gpaObj == null ? "" : gpaObj.toString();

                bw.write(String.format("%s,%s,%s,%s,%s", id, name, email, course, gpa));
                bw.newLine();
            }
            bw.flush();
        }
    }

    private String csvEscape(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            String escaped = field.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return field;
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentSwingApp app = new StudentSwingApp();
            app.setVisible(true);
        });
    }
}
