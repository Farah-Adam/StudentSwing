import java.io.*;
import java.util.List;

public class ReadSerFile {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("students.ser"))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                List<Student> list = (List<Student>) obj;
                for (Student s : list) {
                    System.out.println(s);
                }
            } else {
                System.out.println("Unexpected file format.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
