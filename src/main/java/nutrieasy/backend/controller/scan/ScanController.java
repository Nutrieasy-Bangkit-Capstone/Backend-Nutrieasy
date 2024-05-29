package nutrieasy.backend.controller.scan;

import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.service.NutritionixService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Resa S.
 * Date: 08-05-2024
 * Created in IntelliJ IDEA.
 */
@RestController
public class ScanController {

    private final NutritionixService nutritionixService;

    public ScanController(NutritionixService nutritionixService) {
        this.nutritionixService = nutritionixService;
    }

    @PostMapping("/scan")
    public NutritionixResponseVo scan(@RequestBody NutritionixRequestVo query) {
        System.out.println("Sening query to Nutritionix API : " + query.toString());
        return nutritionixService.getNutritionixData(query);
    }
}
