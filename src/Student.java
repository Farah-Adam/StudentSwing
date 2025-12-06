import java.io.Serializable;
import java.util.Objects;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple serializable Student model with helper methods for JTable and CSV.
 */
public class Student implements Serializable, Comparable<Student> {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String course;
    private double gpa;

    // Basic email regex appropriate for simple validation in the UI
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Student() { }

    public Student(int id, String name, String email, String course, double gpa) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.course = course;
        this.gpa = gpa;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    @Override
    public String toString() {
        return String.format("%d - %s (%s) | %s | GPA: %.2f", id, name, email, course, gpa);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Comparable by id for easy sorting
    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.id, other.id);
    }

    // Helper: return a table row suitable for DefaultTableModel
    // Note: GPA formatted to two decimals as a String for consistent display
    public Object[] toTableRow() {
        return new Object[] {
            id,
            name != null ? name : "",
            email != null ? email : "",
            course != null ? course : "",
            String.format("%.2f", gpa)
        };
    }

    // Helper: build a Student from a table row (expected 5 elements)
    public static Student fromTableRow(Object[] row) {
        if (row == null || row.length < 5) return new Student();
        int id = parseIntSafe(row[0]);
        String name = safeToString(row[1]);
        String email = safeToString(row[2]);
        String course = safeToString(row[3]);
        double gpa = parseDoubleSafe(row[4]);
        return new Student(id, name, email, course, gpa);
    }

    // CSV helpers
    // Produces one CSV line with fields properly quoted when necessary
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%.2f",
                csvEscape(Integer.toString(id)),
                csvEscape(name),
                csvEscape(email),
                csvEscape(course),
                gpa);
    }

    // Simple CSV line parser supporting quoted fields with doubled quotes
    // This parser handles the common cases produced by toCsvLine().
    public static Student fromCsvLine(String line) {
        if (line == null) return new Student();
        String[] fields = splitCsvLine(line);
        int id = fields.length > 0 ? parseIntSafe(fields[0]) : 0;
        String name = fields.length > 1 ? fields[1] : "";
        String email = fields.length > 2 ? fields[2] : "";
        String course = fields.length > 3 ? fields[3] : "";
        double gpa = fields.length > 4 ? parseDoubleSafe(fields[4]) : 0.0;
        return new Student(id, name, email, course, gpa);
    }

    // Basic validation helper for email format
    public boolean isValidEmail() {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // --- Private utility methods ---

    private static String safeToString(Object o) {
        return o == null ? "" : o.toString();
    }

    private static int parseIntSafe(Object o) {
        if (o == null) return 0;
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDoubleSafe(Object o) {
        if (o == null) return 0.0;
        try {
            return Double.parseDouble(o.toString().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static String csvEscape(String field) {
        if (field == null) return "";
        String f = field;
        boolean mustQuote = f.contains(",") || f.contains("\"") || f.contains("\n") || f.contains("\r");
        if (f.contains("\"")) f = f.replace("\"", "\"\"");
        return mustQuote ? "\"" + f + "\"" : f;
    }

    // Splits a CSV line into fields, handling quoted fields and double-quote escaping.
    // Not a full RFC4180 parser but sufficient for the CSV created by toCsvLine().
    private static String[] splitCsvLine(String line) {
        if (line == null) return new String[0];
        List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // look ahead for doubled quote
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++; // skip next quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
