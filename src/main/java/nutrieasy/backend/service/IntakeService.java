package nutrieasy.backend.service;

import nutrieasy.backend.model.NutrientsDetail;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 06-06-2024
 * Created in IntelliJ IDEA.
 */
@Service
public class IntakeService {
    public int calculateTotal(List<NutrientsDetail> detailList) {
        int total = 0;
        for (NutrientsDetail detail : detailList) {
            total += (int) detail.getValue();
        }
        return total;
    }

    public int calculateTotalVitaminC(int age) {
        if (age < 1) {
            return 50;
        } else if (age < 4) {
            return 15;
        } else if (age < 9) {
            return 25;
        } else if (age < 14) {
            return 45;
        } else if (age < 19) {
            return 75;
        } else if (age < 51) {
            return 90;
        } else if (age < 71) {
            return 90;
        } else {
            return 90;
        }
    }

    public int calculateTotalVitaminD(int age) {
        if (age < 1) {
            return 10;
        } else if (age < 4) {
            return 15;
        } else if (age < 9) {
            return 15;
        } else if (age < 14) {
            return 15;
        } else if (age < 19) {
            return 15;
        } else if (age < 51) {
            return 15;
        } else if (age < 71) {
            return 15;
        } else {
            return 15;
        }
    }

    public int calculateTotalVitaminE(int age) {
        if (age < 1) {
            return 4;
        } else if (age < 4) {
            return 6;
        } else if (age < 9) {
            return 7;
        } else if (age < 14) {
            return 11;
        } else if (age < 19) {
            return 15;
        } else if (age < 51) {
            return 15;
        } else if (age < 71) {
            return 15;
        } else {
            return 15;
        }
    }
}
