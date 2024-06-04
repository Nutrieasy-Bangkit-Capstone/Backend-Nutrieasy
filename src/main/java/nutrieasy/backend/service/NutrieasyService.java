package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import nutrieasy.backend.entity.Food;
import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserMealHistory;
import nutrieasy.backend.model.FoodDetails;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.model.vo.ScanResponseVo;
import nutrieasy.backend.repository.FoodRepository;
import nutrieasy.backend.repository.UserMealHistoryRepository;
import nutrieasy.backend.repository.UserRepository;
import nutrieasy.backend.utils.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
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
    private final UserMealHistoryRepository userMealHistoryRepository;
    private final UserRepository userRepository;

    public NutrieasyService(FoodRepository foodRepository, NutritionixService nutritionixService, UserMealHistoryRepository userMealHistoryRepository, UserRepository userRepository) {
        this.foodRepository = foodRepository;
        this.nutritionixService = nutritionixService;
        this.userMealHistoryRepository = userMealHistoryRepository;
        this.userRepository = userRepository;
    }

    public ScanResponseVo scan(String uid, MultipartFile img) {
        ScanResponseVo scanResponseVo = new ScanResponseVo();
        FoodDetails foodDetails = new FoodDetails();
        String scanModelResult = "durian";
        String uploadedImageUrl = null;

        try {
            uploadedImageUrl = convertImage(img);
        } catch (IOException e) {
            e.printStackTrace();
            return new ScanResponseVo(false, "Error uploading image", null);
        }

        Food food = foodRepository.findByName(scanModelResult);

        if (food == null) {
            NutritionixRequestVo nutritionixRequestVo = new NutritionixRequestVo(scanModelResult);
            NutritionixResponseVo nutritionixResponseVo = nutritionixService.getNutritionixData(nutritionixRequestVo);

            if (nutritionixResponseVo == null) {
                return new ScanResponseVo(false, "Nutritionix API error", null);
            } else {
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

        User user = userRepository.findByUid(uid);
        saveScanHistory(food, user, uploadedImageUrl);



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

    private String convertImage(MultipartFile img) throws IOException {
        String base64Image = "";
        byte[] imageBytes = img.getBytes();
        base64Image = Base64Utils.encodeToString(imageBytes);
        return base64Image;
    }

    private void saveScanHistory(Food food, User user,String img) {
        UserMealHistory userMealHistory = new UserMealHistory();
        userMealHistory.setUser(user);
        userMealHistory.setFood(food);
        userMealHistory.setImageUrl(img);
        userMealHistory.setCreatedAt(Timestamp.from(java.time.Instant.now()));

        userMealHistoryRepository.save(userMealHistory);
    }
}
