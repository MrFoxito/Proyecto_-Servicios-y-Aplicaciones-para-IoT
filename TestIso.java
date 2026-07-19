import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class TestIso {
    public static void main(String[] args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse("2026-07-18T19:25:00Z");
            System.out.println("Parsed date: " + date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
