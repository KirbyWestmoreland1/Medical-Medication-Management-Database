CREATE DATABASE IF NOT EXISTS medical_db;
USE medical_db;

DROP TABLE IF EXISTS Prescription;
DROP TABLE IF EXISTS Patient;
DROP TABLE IF EXISTS Doctor;
DROP TABLE IF EXISTS Medication;

CREATE TABLE Patient (
    PatientID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    PhoneNumber VARCHAR(15) UNIQUE,
    Email VARCHAR(100) UNIQUE,
    Address TEXT
);

CREATE TABLE Doctor (
    DoctorID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL
);

CREATE TABLE Medication (
    MedicationID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Prescription (
    PrescriptionID INT AUTO_INCREMENT PRIMARY KEY,
    PatientID INT,
    DoctorID INT,
    MedicationID INT,
    Dosage VARCHAR(50) NOT NULL,
    PrescriptionDate DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (PatientID) REFERENCES Patient(PatientID) ON DELETE CASCADE,
    FOREIGN KEY (DoctorID) REFERENCES Doctor(DoctorID),
    FOREIGN KEY (MedicationID) REFERENCES Medication(MedicationID)
);

-- Sample data
INSERT INTO Doctor (Name) VALUES 
('Dr. Sarah Chen'), ('Dr. James Lee'), ('Dr. Emily Rodriguez'), ('Dr. Michael Brown');

INSERT INTO Medication (Name) VALUES 
('Metformin'), ('Insulin'), ('Lisinopril'), ('Atorvastatin'), ('Levothyroxine'), ('Amlodipine');

INSERT INTO Patient (Name, PhoneNumber, Email, Address) VALUES 
('John Walker', '555-0101', 'john@example.com', '123 Main St'),
('Emma Rodriguez', '555-0102', 'emma@example.com', '456 Oak Ave'),
('Michael Chen', '555-0103', 'mchen@example.com', '789 Pine Rd');

INSERT INTO Prescription (PatientID, DoctorID, MedicationID, Dosage) VALUES 
(1, 1, 1, '500 mg twice daily'),
(2, 2, 1, '1000 mg daily'),
(3, 1, 3, '20 mg daily');