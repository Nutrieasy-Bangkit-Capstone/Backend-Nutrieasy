package nutrieasy.backend.service.impl;

import nutrieasy.backend.entity.Nutrients;
import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.repository.NutrientsRepository;
import nutrieasy.backend.service.NutritionixService;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
public class NutritionixServiceImpl implements NutritionixService {
    private final RestTemplate restTemplate;
    private final NutrientsRepository nutrientsRepository;

    @Value("${nutritionix.url}")
    private String URL;
    @Value("${nutritionix.app.id}")
    private String APP_ID;
    @Value("${nutritionix.app.key}")
    private String APP_KEY;

    public NutritionixServiceImpl(RestTemplate restTemplate, NutrientsRepository nutrientsRepository) {
        this.restTemplate = restTemplate;
        this.nutrientsRepository = nutrientsRepository;
    }

    @Override
    public NutritionixResponseVo getNutritionixData(NutritionixRequestVo query) {
        System.out.println("Sending query to Nutritionix API : " + query.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        headers.set("x-app-id", APP_ID);
        headers.set("x-app-key", APP_KEY);


        // Create HttpEntity
        HttpEntity<NutritionixRequestVo> requestEntity = new HttpEntity<>(query, headers);

        try {

            NutritionixResponseVo response = restTemplate.postForObject(URL, requestEntity, NutritionixResponseVo.class);
            System.out.println("Response from Nutritionix API : " + response.toString());

            List<Integer> nutrients = new ArrayList<>();

            response.getFoods().forEach(food -> {
                System.out.println("Food : " + food.toString());
                food.getFull_nutrients().forEach(fullNutrient -> nutrients.add(fullNutrient.getAttr_id()));
            });
            //System.out.println("Nutrients : " + nutrients);
            List<Nutrients> nutrientsList = nutrientsRepository.findAllById(nutrients);
            System.out.println("Nutrients : " + nutrientsList.toString());
            return response;
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return null;

    }
}
