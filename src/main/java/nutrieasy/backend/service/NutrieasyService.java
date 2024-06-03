package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import nutrieasy.backend.entity.Food;
import nutrieasy.backend.model.FoodDetails;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.model.vo.ScanResponseVo;
import nutrieasy.backend.repository.FoodRepository;
import nutrieasy.backend.utils.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Resa S.
 * Date: 31-05-2024
 * Created in IntelliJ IDEA.
 */
@Service
public class NutrieasyService {


    private final FoodRepository foodRepository;
    private final NutritionixService nutritionixService;
    private final GoogleCloudStorageService googleCloudStorageService;

    public NutrieasyService(FoodRepository foodRepository, NutritionixService nutritionixService, GoogleCloudStorageService googleCloudStorageService) {
        this.foodRepository = foodRepository;
        this.nutritionixService = nutritionixService;
        this.googleCloudStorageService = googleCloudStorageService;
    }

    public ScanResponseVo scan(String uid, MultipartFile img) {
        ScanResponseVo scanResponseVo = new ScanResponseVo();
        FoodDetails foodDetails = new FoodDetails();
        String scanModelResult = "durian";
        String uploadedImageUrl = null;
        try {
            uploadedImageUrl = uploadToBucket(img, uid);
        } catch (IOException e) {
            e.printStackTrace();
            return new ScanResponseVo(false, "Error uploading image", null);
        }

        // TODO: 0 Upload and Scan the image to Machine Learning model

        System.out.println("Uploaded Image URL : " + uploadedImageUrl);

        // TODO: 1. Check if the food is already in the database
        Food food = foodRepository.findByName(scanModelResult);

        // TODO: 2. If not, send the request to Nutritionix API
        if (food == null) {
            NutritionixRequestVo nutritionixRequestVo = new NutritionixRequestVo(scanModelResult);
            NutritionixResponseVo nutritionixResponseVo = nutritionixService.getNutritionixData(nutritionixRequestVo);

            if (nutritionixResponseVo == null) {
                return new ScanResponseVo(false, "Nutritionix API error", null);
            } else {
                // TODO: 2.1 Save the food details to database
                foodDetails = convertNutritionixResponse(nutritionixResponseVo);
                food = new Food();
                food.setName(scanModelResult);
                food.setServingWeightGrams(foodDetails.getServingWeightGrams());
                food.setServingQty(foodDetails.getServingQty());
                food.setServingUnit(foodDetails.getServingUnit());
                food.setCreatedAt(Timestamp.from(java.time.Instant.now()));
                food.setNutrientsJson(JsonUtil.convertListToJson(foodDetails.getNutrientsDetailList()));

                foodRepository.save(food);
            }

        } else {
            foodDetails.setFoodName(food.getName());
            foodDetails.setServingWeightGrams(food.getServingWeightGrams());
            foodDetails.setServingQty(food.getServingQty());
            foodDetails.setServingUnit(food.getServingUnit());
            foodDetails.setNutrientsDetailList(JsonUtil.convertJsonToList(food.getNutrientsJson(),  new TypeReference<List<NutrientsDetail>>() {}));
        }

        // TODO: 3. Save the scan history


        foodDetails.setImageUrl(uploadedImageUrl);

        scanResponseVo.setSuccess(true);
        scanResponseVo.setMessage("Food details saved successfully");
        scanResponseVo.setData(foodDetails);

        return scanResponseVo;
    }

    private FoodDetails convertNutritionixResponse(NutritionixResponseVo nutritionixResponseVo) {
        FoodDetails foodDetails = new FoodDetails();
        List<NutrientsDetail> nutrientsDetailList = new ArrayList<>();
        nutritionixResponseVo.getFoods().forEach(food -> {
            food.getFull_nutrients().forEach(fullNutrient -> {
                System.out.println("Food : " + food.toString());
                System.out.println("Full Nutrient : " + fullNutrient.toString());
                NutrientsDetail nutrientsDetail = nutritionixService.getNutrientAttribute(fullNutrient.getAttr_id());
                nutrientsDetail.setValue(fullNutrient.getValue());
                nutrientsDetailList.add(nutrientsDetail);
            });
        });

        List<NutrientsDetail> sortedList = nutrientsDetailList.stream()
                .filter(n -> n.getValue() != 0.0)
                .sorted(Comparator.comparingDouble(NutrientsDetail::getValue).reversed())
                .collect(Collectors.toList());

        foodDetails.setFoodName(nutritionixResponseVo.getFoods().get(0).getFood_name());
        foodDetails.setServingWeightGrams(nutritionixResponseVo.getFoods().get(0).getServing_weight_grams());
        foodDetails.setServingQty(nutritionixResponseVo.getFoods().get(0).getServing_qty());
        foodDetails.setServingUnit(nutritionixResponseVo.getFoods().get(0).getServing_unit());
        foodDetails.setNutrientsDetailList(sortedList);

        return foodDetails;
    }

    private String uploadToBucket(MultipartFile img, String uid) throws IOException {
        return  googleCloudStorageService.uploadFile(img, uid);
    }
}
