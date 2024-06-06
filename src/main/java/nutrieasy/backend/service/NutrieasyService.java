package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import nutrieasy.backend.entity.Food;
import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import nutrieasy.backend.model.FoodDetails;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.model.vo.IntakeResponseVo;
import nutrieasy.backend.model.vo.ScanResponseVo;
import nutrieasy.backend.repository.FoodRepository;
import nutrieasy.backend.repository.UserHistoryRepository;
import nutrieasy.backend.repository.UserRepository;
import nutrieasy.backend.utils.ConstantNutrient;
import nutrieasy.backend.utils.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
    private final UserHistoryRepository userHistoryRepository;
    private final UserRepository userRepository;
    private final GoogleCloudStorageService googleCloudStorageService;

    public NutrieasyService(FoodRepository foodRepository, NutritionixService nutritionixService, UserHistoryRepository userHistoryRepository, UserRepository userRepository, GoogleCloudStorageService googleCloudStorageService) {
        this.foodRepository = foodRepository;
        this.nutritionixService = nutritionixService;
        this.userHistoryRepository = userHistoryRepository;
        this.userRepository = userRepository;
        this.googleCloudStorageService = googleCloudStorageService;
    }

    public ScanResponseVo scan(String uid, MultipartFile img) {
        ScanResponseVo scanResponseVo = new ScanResponseVo();
        FoodDetails foodDetails = new FoodDetails();
        String scanModelResult = "orange";
        String uploadedImageUrl = null;

        try {
            uploadedImageUrl = googleCloudStorageService.uploadFile(img, uid);
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
            foodDetails.setNutrientsDetailList(JsonUtil.convertJsonToList(food.getNutrientsJson(), new TypeReference<List<NutrientsDetail>>() {
            }));
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


    private void saveScanHistory(Food food, User user, String img) {
        UserHistory userHistory = new UserHistory();
        userHistory.setUser(user);
        userHistory.setFood(food);
        userHistory.setImageUrl(img);
        userHistory.setCreatedAt(Timestamp.from(java.time.Instant.now()));

        userHistoryRepository.save(userHistory);
    }

    public IntakeResponseVo calculateIntake(String uid) throws ParseException {
        User user = userRepository.findByUid(uid);

        if (user == null) {
            return new IntakeResponseVo(false, "User not found", null);
        }

        String date = "2024-06-04";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date d = formatter.parse(date);

        List<UserHistory> userHistoryList = userHistoryRepository.findAllByUserAndDate(user, d);
        System.out.println(userHistoryList);

        List<FoodDetails> foodDetailsList = new ArrayList<>();
        userHistoryList.forEach(userHistory -> {
            FoodDetails foodDetails = new FoodDetails();
            foodDetails.setNutrientsDetailList(
                    JsonUtil.convertJsonToList(userHistory.getFood().getNutrientsJson(),
                            new TypeReference<List<NutrientsDetail>>() {
                            }));
            foodDetailsList.add(foodDetails);
        });

        List<NutrientsDetail> nutrientsDetailList = new ArrayList<>();
        foodDetailsList.forEach(foodDetails -> nutrientsDetailList.addAll(foodDetails.getNutrientsDetailList()));

        System.out.println(nutrientsDetailList);
        nutrientsDetailList.stream().forEach(nutrientsDetail -> {
            if (nutrientsDetail.getAttrId() == ConstantNutrient.VITAMIN_C) {
                System.out.println("Vitamin C : " + nutrientsDetail.getValue() + " " + nutrientsDetail.getUnit());
            }
        });


        return null;
    }
}
