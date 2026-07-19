import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class TestDate {
    public static void main(String[] args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
            
            try {
                Date date2 = sdf.parse("08 jun 2026");
                System.out.println("Parsed date 2: " + date2);
            } catch (Exception e) { e.printStackTrace(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
