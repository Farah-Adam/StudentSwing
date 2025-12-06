# StudentSwing
Java small project for Student management system that helps to add the student info and store, update, and delete from DB table and export it as CSV file  
<img width="1124" height="680" alt="Screenshot 2025-12-06 150623" src="https://github.com/user-attachments/assets/673276ad-8af8-4325-bcb0-3ac0244c2e76" />
# Student Manager – Java Swing Application

A desktop-based **Student Management System** built using **Java Swing**, featuring data validation, JTable integration, custom runtime packaging (jlink), and a Windows installer created using **jpackage**.

This project demonstrates:
- GUI design using Java Swing
- Object serialization (`students.ser`)
- JTable-based CRUD operations
- Exporting student data to CSV
- Packaging a standalone `.exe` installer using `jpackage`
- Custom application icon integration

---

## 🚀 Features

### ✔ Add, Update, Delete Students  
Each student record includes:
- ID  
- Name  
- Email  
- Course  
- GPA  

### ✔ JTable Integration  
A clean, sortable JTable is used instead of JList.

### ✔ Input Validation  
- Email format validation  
- GPA range validation  
- Red-border visual error indicators  

### ✔ Save & Load  
All student records are saved using Java **Object Serialization** (`students.ser`).

### ✔ Export to CSV  
Exports data in a clean, readable CSV format.

### ✔ Windows Installer (.exe)  
Packaged using:
- **jlink** → creates a custom lightweight runtime  
- **jpackage** → creates the final installer  
- Custom **icon.ico**

---

