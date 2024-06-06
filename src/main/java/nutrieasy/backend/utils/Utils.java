package nutrieasy.backend.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Created by Resa S.
 * Date: 06-06-2024
 * Created in IntelliJ IDEA.
 */
public class Utils {
    static int getAgeByBirthDate(String birthDateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(birthDateString, formatter);

        LocalDate currentDate = LocalDate.now();

        return Period.between(birthDate, currentDate).getYears();
    }
}
