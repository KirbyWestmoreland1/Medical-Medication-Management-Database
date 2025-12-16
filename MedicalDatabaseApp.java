import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MedicalDatabaseApp extends JFrame {
    private Connection conn;
    private JTabbedPane tabs;
    private JTextField patientNameField, phoneField, emailField, dosageField;
    private JComboBox<String> patientBox, doctorBox, medBox;

    public MedicalDatabaseApp() {
        setTitle("Medical Medication Management System - CPSC 3375 Project");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/medical_db?useSSL=false", "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to MySQL. Start XAMPP MySQL first!");
            System.exit(0);
        }

        tabs = new JTabbedPane();

        tabs.addTab("Dashboard", createDashboardPanel());
        tabs.addTab("Add Patient", createAddPatientPanel());
        tabs.addTab("New Prescription", createPrescriptionPanel());
        tabs.addTab("View Prescriptions", createViewPrescriptionsPanel());

        add(tabs);
        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Medical Medication Management System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(0, 102, 204));

        JTextArea welcome = new JTextArea(
                "Welcome!\n\nThis system manages patient medication records.\n" +
                        "Features:\n" +
                        "• Add patients and prescriptions\n" +
                        "• View all prescriptions with doctor and dosage\n" +
                        "• Enforces unique email/phone and dosage safety\n" +
                        "• Built with Java + MySQL as per project specification"
        );
        welcome.setEditable(false);
        welcome.setFont(new Font("Arial", Font.PLAIN, 16));
        welcome.setBackground(panel.getBackground());

        panel.add(title, BorderLayout.NORTH);
        panel.add(welcome, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAddPatientPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Patient Name:"));
        patientNameField = new JTextField();
        panel.add(patientNameField);

        panel.add(new JLabel("Phone Number:"));
        phoneField = new JTextField();
        panel.add(phoneField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        JButton addBtn = new JButton("Add Patient");
        addBtn.addActionListener(e -> addPatient());
        panel.add(addBtn);

        return panel;
    }

    private JPanel createPrescriptionPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Select Patient:"));
        patientBox = new JComboBox<>();
        panel.add(patientBox);

        panel.add(new JLabel("Select Doctor:"));
        doctorBox = new JComboBox<>();
        panel.add(doctorBox);

        panel.add(new JLabel("Select Medication:"));
        medBox = new JComboBox<>();
        panel.add(medBox);

        panel.add(new JLabel("Dosage (e.g., 500 mg daily):"));
        dosageField = new JTextField();
        panel.add(dosageField);

        JButton prescribeBtn = new JButton("Create Prescription");
        prescribeBtn.addActionListener(e -> createPrescription());
        panel.add(prescribeBtn);

        loadComboBoxes();
        return panel;
    }

    private JPanel createViewPrescriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"Patient", "Doctor", "Medication", "Dosage", "Date"}, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh List");
        refresh.addActionListener(e -> loadPrescriptions(model));
        panel.add(refresh, BorderLayout.SOUTH);

        loadPrescriptions(model);
        return panel;
    }

    private void addPatient() {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Patient (Name, PhoneNumber, Email) VALUES (?, ?, ?)");
            ps.setString(1, patientNameField.getText());
            ps.setString(2, phoneField.getText());
            ps.setString(3, emailField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient added successfully!");
            patientNameField.setText(""); phoneField.setText(""); emailField.setText("");
            loadComboBoxes();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void createPrescription() {
        String dosage = dosageField.getText();
        if (dosage.toLowerCase().contains("mg") && dosage.matches(".*\\d+.*")) {
            int mg = Integer.parseInt(dosage.replaceAll("\\D+", ""));
            if (mg > 2000) {
                JOptionPane.showMessageDialog(this, "DOSAGE TOO HIGH! Blocked for safety.");
                return;
            }
        }

        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Prescription (PatientID, DoctorID, MedicationID, Dosage) VALUES (?, ?, ?, ?)");
            ps.setInt(1, getIdFromCombo(patientBox));
            ps.setInt(2, getIdFromCombo(doctorBox));
            ps.setInt(3, getIdFromCombo(medBox));
            ps.setString(4, dosage);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Prescription created!");
            dosageField.setText("");
        } catch (Exception ex) {
JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadComboBoxes() {
        try {
            patientBox.removeAllItems();
            doctorBox.removeAllItems();
            medBox.removeAllItems();

            ResultSet rs = conn.createStatement().executeQuery("SELECT PatientID, Name FROM Patient");
            while (rs.next()) patientBox.addItem(rs.getInt(1) + " - " + rs.getString(2));

            rs = conn.createStatement().executeQuery("SELECT DoctorID, Name FROM Doctor");
            while (rs.next()) doctorBox.addItem(rs.getInt(1) + " - " + rs.getString(2));

            rs = conn.createStatement().executeQuery("SELECT MedicationID, Name FROM Medication");
            while (rs.next()) medBox.addItem(rs.getInt(1) + " - " + rs.getString(2));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int getIdFromCombo(JComboBox<String> box) {
        String item = (String) box.getSelectedItem();
        return Integer.parseInt(item.split(" - ")[0]);
    }

    private void loadPrescriptions(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("""
                SELECT p.Name, d.Name, m.Name, pr.Dosage, pr.PrescriptionDate
                FROM Prescription pr
                JOIN Patient p ON pr.PatientID = p.PatientID
                JOIN Doctor d ON pr.DoctorID = d.DoctorID
                JOIN Medication m ON pr.MedicationID = m.MedicationID
                ORDER BY pr.PrescriptionDate DESC
                """);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDate(5)});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MedicalDatabaseApp::new);
    }
}