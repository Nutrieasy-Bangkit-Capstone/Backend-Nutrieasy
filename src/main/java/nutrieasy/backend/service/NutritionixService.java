package nutrieasy.backend.service;

import nutrieasy.backend.entity.Nutrients;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.repository.NutrientsRepository;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Resa S.
 * Date: 28-05-2024
 * Created in IntelliJ IDEA.
 */
@Service
public class NutritionixService {
    private final RestTemplate restTemplate;
    private final NutrientsRepository nutrientsRepository;

    @Value("${nutritionix.url}")
    private String URL;
    @Value("${nutritionix.app.id}")
    private String APP_ID;
    @Value("${nutritionix.app.key}")
    private String APP_KEY;

    public NutritionixService(RestTemplate restTemplate, NutrientsRepository nutrientsRepository) {
        this.restTemplate = restTemplate;
        this.nutrientsRepository = nutrientsRepository;
    }

    public NutritionixResponseVo getNutritionixData(NutritionixRequestVo query) throws RestClientException {
        System.out.println("Sending query to Nutritionix API : " + query.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        headers.set("x-app-id", APP_ID);
        headers.set("x-app-key", APP_KEY);

        HttpEntity<NutritionixRequestVo> requestEntity = new HttpEntity<>(query, headers);

        try {

            NutritionixResponseVo response = restTemplate.postForObject(URL, requestEntity, NutritionixResponseVo.class);
            System.out.println("Response from Nutritionix API : " + response.toString());

            List<Integer> nutrients = new ArrayList<>();

            response.getFoods().forEach(food -> {
                System.out.println("Food : " + food.toString());
                food.getFull_nutrients().forEach(fullNutrient -> nutrients.add(fullNutrient.getAttr_id()));
            });

            List<Nutrients> nutrientsList = nutrientsRepository.findAllById(nutrients);
            return response;
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RestClientException("Error while sending request to Nutritionix API");
        }

    }

    public NutrientsDetail getNutrientAttribute(int attrId) {
        Nutrients nutrient = nutrientsRepository.findById(attrId).orElse(null);
        if (nutrient == null) {
            return null;
        }
        return new NutrientsDetail(nutrient.getAttrID(), nutrient.getName(), 0.0, nutrient.getUnit());
    }
}
