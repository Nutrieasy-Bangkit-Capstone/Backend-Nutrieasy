package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import nutrieasy.backend.entity.Food;
import nutrieasy.backend.entity.Nutrients;
import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import nutrieasy.backend.model.FoodDetails;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.model.vo.IntakeResponseVo;
import nutrieasy.backend.model.vo.ScanResponseVo;
import nutrieasy.backend.repository.FoodRepository;
import nutrieasy.backend.repository.NutrientsRepository;
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
import java.time.Instant;
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
@Slf4j
public class NutrieasyService {


    private final FoodRepository foodRepository;
    private final NutritionixService nutritionixService;
    private final UserHistoryRepository userHistoryRepository;
    private final UserRepository userRepository;
    private final GoogleCloudStorageService googleCloudStorageService;
    private final NutrientsRepository nutrientsRepository;

    public NutrieasyService(FoodRepository foodRepository, NutritionixService nutritionixService, UserHistoryRepository userHistoryRepository, UserRepository userRepository, GoogleCloudStorageService googleCloudStorageService, NutrientsRepository nutrientsRepository) {
        this.foodRepository = foodRepository;
        this.nutritionixService = nutritionixService;
        this.userHistoryRepository = userHistoryRepository;
        this.userRepository = userRepository;
        this.googleCloudStorageService = googleCloudStorageService;
        this.nutrientsRepository = nutrientsRepository;
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
                food.setCreatedAt(Timestamp.from(Instant.now()));
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

        log.info("Scan Response : " + JsonUtil.convertObjectToJson(scanResponseVo));

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
        userHistory.setCreatedAt(Timestamp.from(Instant.now()));

        userHistoryRepository.save(userHistory);
    }

    public IntakeResponseVo calculateIntake(String uid, String date) throws ParseException {
        User user = userRepository.findByUid(uid);

        if (user == null) {
            return new IntakeResponseVo(false, "User not found", null);
        }


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (date == null || date.isEmpty()) {
            date = formatter.format(new Date());
        }
        Date d = formatter.parse(date);
        log.info("Date : " + d.toString());

        List<UserHistory> userHistoryList = userHistoryRepository.findAllByUserAndDate(user, d);
        log.info("User History List : " + JsonUtil.convertObjectToJson(userHistoryList));

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


        NutrientsDetail totalVitaminC = new NutrientsDetail(ConstantNutrient.VITAMIN_C, null, 0, null);
        NutrientsDetail totalVitaminA = new NutrientsDetail(ConstantNutrient.VITAMIN_A, null, 0, null);
        NutrientsDetail totalVitaminD = new NutrientsDetail(ConstantNutrient.VITAMIN_D, null, 0, null);
        NutrientsDetail totalVitaminE = new NutrientsDetail(ConstantNutrient.VITAMIN_E, null, 0, null);
        NutrientsDetail totalVitaminB6 = new NutrientsDetail(ConstantNutrient.VITAMIN_B6, null, 0, null);
        NutrientsDetail totalVitaminB12 = new NutrientsDetail(ConstantNutrient.VITAMIN_B12, null, 0, null);
        NutrientsDetail totalCalcium = new NutrientsDetail(ConstantNutrient.CALCIUM, null, 0, null);
        NutrientsDetail totalIron = new NutrientsDetail(ConstantNutrient.IRON, null, 0, null);
        NutrientsDetail totalMagnesium = new NutrientsDetail(ConstantNutrient.MAGNESIUM, null, 0, null);
        NutrientsDetail totalPotassium = new NutrientsDetail(ConstantNutrient.POTASSIUM, null, 0, null);
        NutrientsDetail totalSodium = new NutrientsDetail(ConstantNutrient.SODIUM, null, 0, null);
        NutrientsDetail totalZinc = new NutrientsDetail(ConstantNutrient.ZINC, null, 0, null);
        NutrientsDetail totalFiber = new NutrientsDetail(ConstantNutrient.FIBER, null, 0, null);

        List<NutrientsDetail> totalIntakeList = new ArrayList<>();

        List<Integer> query = new ArrayList<>();
        query.add(ConstantNutrient.VITAMIN_C);
        query.add(ConstantNutrient.VITAMIN_A);
        query.add(ConstantNutrient.VITAMIN_D);
        query.add(ConstantNutrient.VITAMIN_E);
        query.add(ConstantNutrient.VITAMIN_B6);
        query.add(ConstantNutrient.VITAMIN_B12);
        query.add(ConstantNutrient.CALCIUM);
        query.add(ConstantNutrient.IRON);
        query.add(ConstantNutrient.MAGNESIUM);
        query.add(ConstantNutrient.POTASSIUM);
        query.add(ConstantNutrient.SODIUM);
        query.add(ConstantNutrient.ZINC);
        query.add(ConstantNutrient.FIBER);

        List<Nutrients> listOfNutrients = nutrientsRepository.findAllById(query);

        listOfNutrients.forEach(nutrient -> {
            switch (nutrient.getAttrID()) {
                case ConstantNutrient.VITAMIN_C:
                    totalVitaminC.setAttrId(nutrient.getAttrID());
                    totalVitaminC.setName(nutrient.getName());
                    totalVitaminC.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.VITAMIN_A:
                    totalVitaminA.setAttrId(nutrient.getAttrID());
                    totalVitaminA.setName(nutrient.getName());
                    totalVitaminA.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.VITAMIN_D:
                    totalVitaminD.setAttrId(nutrient.getAttrID());
                    totalVitaminD.setName(nutrient.getName());
                    totalVitaminD.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.VITAMIN_E:
                    totalVitaminE.setAttrId(nutrient.getAttrID());
                    totalVitaminE.setName(nutrient.getName());
                    totalVitaminE.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.VITAMIN_B6:
                    totalVitaminB6.setAttrId(nutrient.getAttrID());
                    totalVitaminB6.setName(nutrient.getName());
                    totalVitaminB6.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.VITAMIN_B12:
                    totalVitaminB12.setAttrId(nutrient.getAttrID());
                    totalVitaminB12.setName(nutrient.getName());
                    totalVitaminB12.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.CALCIUM:
                    totalCalcium.setAttrId(nutrient.getAttrID());
                    totalCalcium.setName(nutrient.getName());
                    totalCalcium.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.IRON:
                    totalIron.setAttrId(nutrient.getAttrID());
                    totalIron.setName(nutrient.getName());
                    totalIron.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.MAGNESIUM:
                    totalMagnesium.setAttrId(nutrient.getAttrID());
                    totalMagnesium.setName(nutrient.getName());
                    totalMagnesium.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.POTASSIUM:
                    totalPotassium.setAttrId(nutrient.getAttrID());
                    totalPotassium.setName(nutrient.getName());
                    totalPotassium.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.SODIUM:
                    totalSodium.setAttrId(nutrient.getAttrID());
                    totalSodium.setName(nutrient.getName());
                    totalSodium.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.ZINC:
                    totalZinc.setAttrId(nutrient.getAttrID());
                    totalZinc.setName(nutrient.getName());
                    totalZinc.setUnit(nutrient.getUnit());
                    break;
                case ConstantNutrient.FIBER:
                    totalFiber.setAttrId(nutrient.getAttrID());
                    totalFiber.setName(nutrient.getName());
                    totalFiber.setUnit(nutrient.getUnit());
                    break;
                default:
                    break;
            }
        });


        for (NutrientsDetail nd : nutrientsDetailList) {
            switch (nd.getAttrId()) {
                case ConstantNutrient.VITAMIN_C:
                    totalVitaminC.setValue(totalVitaminC.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.VITAMIN_A:
                    totalVitaminA.setValue(totalVitaminA.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.VITAMIN_D:
                    totalVitaminD.setValue(totalVitaminD.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.VITAMIN_E:
                    totalVitaminE.setValue(totalVitaminE.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.VITAMIN_B6:
                    totalVitaminB6.setValue(totalVitaminB6.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.VITAMIN_B12:
                    totalVitaminB12.setValue(totalVitaminB12.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.CALCIUM:
                    totalCalcium.setValue(totalCalcium.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.IRON:
                    totalIron.setValue(totalIron.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.MAGNESIUM:
                    totalMagnesium.setValue(totalMagnesium.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.POTASSIUM:
                    totalPotassium.setValue(totalPotassium.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.SODIUM:
                    totalSodium.setValue(totalSodium.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.ZINC:
                    totalZinc.setValue(totalZinc.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.FIBER:
                    totalFiber.setValue(totalFiber.getValue() + nd.getValue());
                    break;
                default:
                    break;
            }
        }

        totalIntakeList.add(totalVitaminA);
        totalIntakeList.add(totalVitaminC);
        totalIntakeList.add(totalVitaminD);
        totalIntakeList.add(totalVitaminE);
        totalIntakeList.add(totalVitaminB6);
        totalIntakeList.add(totalVitaminB12);
        totalIntakeList.add(totalCalcium);
        totalIntakeList.add(totalIron);
        totalIntakeList.add(totalMagnesium);
        totalIntakeList.add(totalPotassium);
        totalIntakeList.add(totalSodium);
        totalIntakeList.add(totalZinc);
        totalIntakeList.add(totalFiber);


        IntakeResponseVo intakeResponseVo = new IntakeResponseVo();
        intakeResponseVo.setSuccess(true);
        intakeResponseVo.setMessage("Intake calculated successfully");
        intakeResponseVo.setTotalIntakeList(totalIntakeList);

        log.info("Intake Response : " + JsonUtil.convertObjectToJson(intakeResponseVo));

        return intakeResponseVo;
    }
}
