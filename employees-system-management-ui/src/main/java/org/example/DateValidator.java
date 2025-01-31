package org.example;
import javax.swing.*;
        import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValidator {

    // Date format: yyyy-MM-dd (e.g., 2023-10-01)
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static {
        DATE_FORMAT.setLenient(false); // Disallow invalid dates like 2023-02-30
    }

    // Earliest valid date (e.g., 1970-01-01)
    private static final Date MIN_DATE;
    static {
        try {
            MIN_DATE = DATE_FORMAT.parse("1970-01-01");
        } catch (ParseException e) {
            throw new RuntimeException("Invalid minimum date setup", e);
        }
    }

    // Validate the input date string
    public static boolean isValidDate(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            Date today = new Date(); // Current date/time

            // Check if date is after 1970-01-01 AND before today
            return date.after(MIN_DATE) && date.before(today);
        } catch (ParseException e) {
            return false; // Invalid format
        }
    }
}
