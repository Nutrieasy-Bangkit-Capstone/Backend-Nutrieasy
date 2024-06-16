package nutrieasy.backend.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Created by Resa S.
 * Date: 06-06-2024
 * Created in IntelliJ IDEA.
 */
@Slf4j
public class BodyCountUtils {
    public static int getAgeByBirthDate(String birthDateString) {
        if (birthDateString == null || birthDateString.isEmpty()) {
            return 25;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(birthDateString, formatter);

        LocalDate currentDate = LocalDate.now();

        return Period.between(birthDate, currentDate).getYears();
    }

    public static double getBmi(double weight, double height) {
        return weight / (height * height);
    }

    public static double getBmr(String gender, double weight, double height, int age) {
        if (weight == 0) {
            weight = 50;
        }
        if (height == 0) {
            height = 165;
        }
        if (age == 0) {
            age = 25;
        }


        log.info("=> Entered getBmr method");
        double bmr = 0;
        if (gender.equalsIgnoreCase("Male")) {
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else if (gender.equalsIgnoreCase("Female")) {
            bmr = (447.593 + (9.247 * weight) + (3.098 * height)) - (4.330 * age);
        }

        return bmr;
    }

    public static int getCaloriesDailyIntake(String activityLevel, double bmr) {
        if (activityLevel == null || activityLevel.isEmpty()) {
            activityLevel = "Moderately Active";
        }

        log.info("=> Entered getCaloriesDailyIntake method with activity level: " + activityLevel + " and bmr: " + bmr);
        int calories = 0;
        if (activityLevel.equalsIgnoreCase("Sedentary")) {
            calories = (int) (bmr * 1.2);
        } else if (activityLevel.equalsIgnoreCase("Lightly Active")) {
            calories = (int) (bmr * 1.375);
        } else if (activityLevel.equalsIgnoreCase("Moderately Active")) {
            calories = (int) (bmr * 1.55);
        } else if (activityLevel.equalsIgnoreCase("Very Active")) {
            calories = (int) (bmr * 1.725);
        } else if (activityLevel.equalsIgnoreCase("Extra Active")) {
            calories = (int) (bmr * 1.9);
        }

        return calories;
    }

    public static double getVitaminCMin(String gender) {
        if (gender.equalsIgnoreCase("Male")) {
            return 90;
        } else {
            return 75;
        }
    }

    public static double getVitaminAMin(String gender) {
        if (gender.equalsIgnoreCase("Male")) {
            return 900;
        } else {
            return 700;
        }
    }

    public static double getIronMin(String gender) {
        if (gender.equalsIgnoreCase("Male")) {
            return 8;
        } else {
            return 18;
        }
    }

    public static double getMagnesiumMin(String gender) {
        if (gender.equalsIgnoreCase("Male")) {
            return 400;
        } else {
            return 310;
        }
    }

    public static double getZincMin(String gender) {
        if (gender.equalsIgnoreCase("Male")) {
            return 11;
        } else {
            return 8;
        }
    }

    public static double getProteinMin(String gender) {
        if (gender.equalsIgnoreCase("Male")) {
            return 56;
        } else {
            return 46;
        }
    }
}
