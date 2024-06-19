package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import nutrieasy.backend.entity.Food;
import nutrieasy.backend.entity.Nutrients;
import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import nutrieasy.backend.model.FoodDetails;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.NutrientsIntakeDetail;
import nutrieasy.backend.model.ScanResult;
import nutrieasy.backend.model.nutritionix.NutritionixRequestVo;
import nutrieasy.backend.model.nutritionix.response.NutritionixResponseVo;
import nutrieasy.backend.model.vo.IntakeResponseVo;
import nutrieasy.backend.model.vo.ScanResponseVo;
import nutrieasy.backend.model.vo.TrackHistoryResponseVo;
import nutrieasy.backend.model.vo.TrackScanRequestVo;
import nutrieasy.backend.repository.FoodRepository;
import nutrieasy.backend.repository.NutrientsRepository;
import nutrieasy.backend.repository.UserHistoryRepository;
import nutrieasy.backend.repository.UserRepository;
import nutrieasy.backend.utils.BodyCountUtils;
import nutrieasy.backend.utils.ConstantNutrient;
import nutrieasy.backend.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
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
    private final RestTemplate restTemplate;

    public NutrieasyService(FoodRepository foodRepository, NutritionixService nutritionixService, UserHistoryRepository userHistoryRepository, UserRepository userRepository, GoogleCloudStorageService googleCloudStorageService, NutrientsRepository nutrientsRepository, RestTemplate restTemplate) {
        this.foodRepository = foodRepository;
        this.nutritionixService = nutritionixService;
        this.userHistoryRepository = userHistoryRepository;
        this.userRepository = userRepository;
        this.googleCloudStorageService = googleCloudStorageService;
        this.nutrientsRepository = nutrientsRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${ml-model-url}")
    private String urlModel;

    public ScanResponseVo scan(String uid, MultipartFile img) {
        ScanResponseVo scanResponseVo = new ScanResponseVo();
        FoodDetails foodDetails = new FoodDetails();
        String scanModelResult = scanModel(img).getResult().toLowerCase();
        String uploadedImageUrl = null;

        if (scanModelResult == null || scanModelResult.toLowerCase().equalsIgnoreCase("unknown") || scanModelResult.isEmpty()) {
            return new ScanResponseVo(false, "Model could not recognize the image", null);
        }

        try {
            uploadedImageUrl = googleCloudStorageService.uploadFile(img, uid);


            Food food = foodRepository.findByName(scanModelResult);

            if (food == null) {
                NutritionixRequestVo nutritionixRequestVo = new NutritionixRequestVo(scanModelResult);
                NutritionixResponseVo nutritionixResponseVo = nutritionixService.getNutritionixData(nutritionixRequestVo);
                log.info("Nutritionix Response : " + JsonUtil.convertObjectToJson(nutritionixResponseVo));

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


            foodDetails.setImageUrl(uploadedImageUrl);

            scanResponseVo.setSuccess(true);
            scanResponseVo.setMessage("Scan successful");
            scanResponseVo.setData(foodDetails);

            log.info("Scan Response : " + JsonUtil.convertObjectToJson(scanResponseVo));
        } catch (Exception e) {
            e.printStackTrace();
            return new ScanResponseVo(false, "Error uploading image", null);
        }
        return scanResponseVo;
    }

    private ScanResult scanModel(MultipartFile img) {
        ScanResult scanResult = new ScanResult();
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", convertToFileResource(img));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            scanResult = restTemplate.postForObject(urlModel, requestEntity, ScanResult.class);
            log.info("Scan Result : " + JsonUtil.convertObjectToJson(scanResult));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scanResult;
    }

    private Resource convertToFileResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    private FoodDetails convertNutritionixResponse(NutritionixResponseVo nutritionixResponseVo) {
        FoodDetails foodDetails = new FoodDetails();
        List<NutrientsDetail> nutrientsDetailList = new ArrayList<>();
        log.info("Nutritionix Convert : " + JsonUtil.convertObjectToJson(nutritionixResponseVo));
        for (nutrieasy.backend.model.nutritionix.response.Food food : nutritionixResponseVo.getFoods()) {
            food.getFull_nutrients().forEach(fullNutrient -> {
                NutrientsDetail nutrientsDetail = nutritionixService.getNutrientAttribute(fullNutrient.getAttr_id());
                if (nutrientsDetail != null) {
                    nutrientsDetail.setValue(fullNutrient.getValue());
                    if (food.getServing_qty() == 0.5) {
                        nutrientsDetail.setValue(nutrientsDetail.getValue() * 2);
                    }
                    nutrientsDetailList.add(nutrientsDetail);
                }
            });
        }

        List<NutrientsDetail> sortedList = nutrientsDetailList.stream()
                .filter(n -> n.getValue() != 0.0)
                .sorted(Comparator.comparingDouble(NutrientsDetail::getValue).reversed())
                .collect(Collectors.toList());

        int servingQty = nutritionixResponseVo.getFoods().get(0).getServing_qty() == 0.5 ? 1 : (int) nutritionixResponseVo.getFoods().get(0).getServing_qty();

        foodDetails.setFoodName(nutritionixResponseVo.getFoods().get(0).getFood_name());
        foodDetails.setServingWeightGrams(nutritionixResponseVo.getFoods().get(0).getServing_weight_grams());
        foodDetails.setServingQty(servingQty);
        foodDetails.setServingUnit(nutritionixResponseVo.getFoods().get(0).getServing_unit());
        foodDetails.setNutrientsDetailList(sortedList);

        return foodDetails;
    }


    public IntakeResponseVo calculateIntake(String uid, String date) throws ParseException {
        User user = userRepository.findByUid(uid);

        if (user == null) {
            return new IntakeResponseVo(false, "User not found", null);
        }

        if (user.getGender() == null || user.getGender().isEmpty()) {
            user.setGender("Female");
        }


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (date == null || date.isEmpty()) {
            date = formatter.format(new Date());
        }
        Date d = formatter.parse(date);
        log.info("Date : " + d.toString());

        List<UserHistory> userHistoryList = userHistoryRepository.findAllByUserAndDate(user, d);

        log.info("User History List = " + userHistoryList.size() + "\n" + JsonUtil.convertObjectToJson(userHistoryList));

        List<FoodDetails> foodDetailsList = new ArrayList<>();
        userHistoryList.forEach(userHistory -> {
            FoodDetails foodDetails = new FoodDetails();
            log.info("User History : " + userHistory.toString());
            for (int i = 0; i < userHistory.getQuantity(); i++) {
                foodDetails.setNutrientsDetailList(
                        JsonUtil.convertJsonToList(userHistory.getFood().getNutrientsJson(),
                                new TypeReference<List<NutrientsDetail>>() {
                                }));
                foodDetailsList.add(foodDetails);
            }

        });

        log.info("Food Details List : " + foodDetailsList.size() + "\n" + JsonUtil.convertObjectToJson(foodDetailsList));

        List<NutrientsDetail> nutrientsDetailList = new ArrayList<>();
        foodDetailsList.forEach(foodDetails -> nutrientsDetailList.addAll(foodDetails.getNutrientsDetailList()));


        NutrientsIntakeDetail totalVitaminC = new NutrientsIntakeDetail(ConstantNutrient.VITAMIN_C, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalVitaminA = new NutrientsIntakeDetail(ConstantNutrient.VITAMIN_A, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalVitaminD = new NutrientsIntakeDetail(ConstantNutrient.VITAMIN_D, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalVitaminE = new NutrientsIntakeDetail(ConstantNutrient.VITAMIN_E, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalVitaminB6 = new NutrientsIntakeDetail(ConstantNutrient.VITAMIN_B6, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalVitaminB12 = new NutrientsIntakeDetail(ConstantNutrient.VITAMIN_B12, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalCalcium = new NutrientsIntakeDetail(ConstantNutrient.CALCIUM, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalIron = new NutrientsIntakeDetail(ConstantNutrient.IRON, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalMagnesium = new NutrientsIntakeDetail(ConstantNutrient.MAGNESIUM, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalPotassium = new NutrientsIntakeDetail(ConstantNutrient.POTASSIUM, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalSodium = new NutrientsIntakeDetail(ConstantNutrient.SODIUM, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalZinc = new NutrientsIntakeDetail(ConstantNutrient.ZINC, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalFiber = new NutrientsIntakeDetail(ConstantNutrient.FIBER, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalEnergy = new NutrientsIntakeDetail(ConstantNutrient.ENERGY, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalProtein = new NutrientsIntakeDetail(ConstantNutrient.PROTEIN, null, 0, null, 0, 0);
        NutrientsIntakeDetail totalSugar = new NutrientsIntakeDetail(ConstantNutrient.SUGAR, null, 0, null, 0, 0);

        List<NutrientsIntakeDetail> totalIntakeList = new ArrayList<>();

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
        query.add(ConstantNutrient.ENERGY);
        query.add(ConstantNutrient.PROTEIN);
        query.add(ConstantNutrient.SUGAR);

        List<Nutrients> listOfNutrients = nutrientsRepository.findAllById(query);

        listOfNutrients.forEach(nutrient -> {
            switch (nutrient.getAttrID()) {
                case ConstantNutrient.VITAMIN_C:
                    totalVitaminC.setAttrId(nutrient.getAttrID());
                    totalVitaminC.setName(nutrient.getName());
                    totalVitaminC.setUnit(nutrient.getUnit());

                    totalVitaminC.setMinValue(BodyCountUtils.getVitaminCMin(user.getGender()));
                    totalVitaminC.setMaxValue(2000);
                    break;
                case ConstantNutrient.VITAMIN_A:
                    totalVitaminA.setAttrId(nutrient.getAttrID());
                    totalVitaminA.setName(nutrient.getName());
                    totalVitaminA.setUnit(nutrient.getUnit());

                    totalVitaminA.setMinValue(BodyCountUtils.getVitaminAMin(user.getGender()));
                    totalVitaminA.setMaxValue(3000);
                    break;
                case ConstantNutrient.VITAMIN_D:
                    totalVitaminD.setAttrId(nutrient.getAttrID());
                    totalVitaminD.setName(nutrient.getName());
                    totalVitaminD.setUnit(nutrient.getUnit());

                    totalVitaminD.setMinValue(15);
                    totalVitaminD.setMaxValue(100);
                    break;
                case ConstantNutrient.VITAMIN_E:
                    totalVitaminE.setAttrId(nutrient.getAttrID());
                    totalVitaminE.setName(nutrient.getName());
                    totalVitaminE.setUnit(nutrient.getUnit());

                    totalVitaminE.setMinValue(15);
                    totalVitaminE.setMaxValue(1000);
                    break;
                case ConstantNutrient.VITAMIN_B6:
                    totalVitaminB6.setAttrId(nutrient.getAttrID());
                    totalVitaminB6.setName(nutrient.getName());
                    totalVitaminB6.setUnit(nutrient.getUnit());

                    totalVitaminB6.setMinValue(1.3);
                    totalVitaminB6.setMaxValue(100);
                    break;
                case ConstantNutrient.VITAMIN_B12:
                    totalVitaminB12.setAttrId(nutrient.getAttrID());
                    totalVitaminB12.setName(nutrient.getName());
                    totalVitaminB12.setUnit(nutrient.getUnit());

                    totalVitaminB12.setMinValue(2.4);
                    totalVitaminB12.setMaxValue(100);
                    break;
                case ConstantNutrient.CALCIUM:
                    totalCalcium.setAttrId(nutrient.getAttrID());
                    totalCalcium.setName(nutrient.getName());
                    totalCalcium.setUnit(nutrient.getUnit());

                    totalCalcium.setMinValue(1000);
                    totalCalcium.setMaxValue(2500);
                    break;
                case ConstantNutrient.IRON:
                    totalIron.setAttrId(nutrient.getAttrID());
                    totalIron.setName(nutrient.getName());
                    totalIron.setUnit(nutrient.getUnit());

                    totalIron.setMinValue(BodyCountUtils.getIronMin(user.getGender()));
                    totalIron.setMaxValue(45);
                    break;
                case ConstantNutrient.MAGNESIUM:
                    totalMagnesium.setAttrId(nutrient.getAttrID());
                    totalMagnesium.setName(nutrient.getName());
                    totalMagnesium.setUnit(nutrient.getUnit());

                    totalMagnesium.setMinValue(BodyCountUtils.getMagnesiumMin(user.getGender()));
                    totalMagnesium.setMaxValue(500);
                    break;
                case ConstantNutrient.POTASSIUM:
                    totalPotassium.setAttrId(nutrient.getAttrID());
                    totalPotassium.setName(nutrient.getName());
                    totalPotassium.setUnit(nutrient.getUnit());

                    totalPotassium.setMinValue(3500);
                    totalPotassium.setMaxValue(4700);
                    break;
                case ConstantNutrient.SODIUM:
                    totalSodium.setAttrId(nutrient.getAttrID());
                    totalSodium.setName(nutrient.getName());
                    totalSodium.setUnit(nutrient.getUnit());

                    totalSodium.setMinValue(1500);
                    totalSodium.setMaxValue(2300);
                    break;
                case ConstantNutrient.ZINC:
                    totalZinc.setAttrId(nutrient.getAttrID());
                    totalZinc.setName(nutrient.getName());
                    totalZinc.setUnit(nutrient.getUnit());

                    totalZinc.setMinValue(BodyCountUtils.getZincMin(user.getGender()));
                    totalZinc.setMaxValue(40);
                    break;
                case ConstantNutrient.ENERGY:
                    totalEnergy.setAttrId(nutrient.getAttrID());
                    totalEnergy.setName(nutrient.getName());
                    totalEnergy.setUnit(nutrient.getUnit());

                    totalEnergy.setMinValue(
                            BodyCountUtils.getCaloriesDailyIntake(
                                    user.getActivityLevel(),
                                    BodyCountUtils.getBmr(
                                            user.getGender(),
                                            user.getWeight(),
                                            user.getHeight(),
                                            BodyCountUtils.getAgeByBirthDate(user.getDateOfBirth()))));
                    totalEnergy.setMaxValue(totalEnergy.getMinValue() + 200);
                    break;
                case ConstantNutrient.SUGAR:
                    totalSugar.setAttrId(nutrient.getAttrID());
                    totalSugar.setName(nutrient.getName());
                    totalSugar.setUnit(nutrient.getUnit());

                    totalSugar.setMinValue(0);
                    totalSugar.setMaxValue(30);
                    break;
                case ConstantNutrient.FIBER:
                    totalFiber.setAttrId(nutrient.getAttrID());
                    totalFiber.setName(nutrient.getName());
                    totalFiber.setUnit(nutrient.getUnit());

                    totalFiber.setMinValue(25);
                    totalFiber.setMaxValue(50);
                    break;
                case ConstantNutrient.PROTEIN:
                    totalProtein.setAttrId(nutrient.getAttrID());
                    totalProtein.setName(nutrient.getName());
                    totalProtein.setUnit(nutrient.getUnit());

                    totalProtein.setMinValue(BodyCountUtils.getProteinMin(user.getGender()));
                    totalProtein.setMaxValue(200);
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
                case ConstantNutrient.ENERGY:
                    totalEnergy.setValue(totalEnergy.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.SUGAR:
                    totalSugar.setValue(totalSugar.getValue() + nd.getValue());
                    break;
                case ConstantNutrient.PROTEIN:
                    totalProtein.setValue(totalProtein.getValue() + nd.getValue());
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
        totalIntakeList.add(totalEnergy);
        totalIntakeList.add(totalProtein);
        totalIntakeList.add(totalSugar);


        IntakeResponseVo intakeResponseVo = new IntakeResponseVo();
        intakeResponseVo.setSuccess(true);
        intakeResponseVo.setMessage("Intake calculated successfully");
        intakeResponseVo.setTotalIntakeList(totalIntakeList);

        log.info("Intake Response : " + JsonUtil.convertObjectToJson(intakeResponseVo));

        return intakeResponseVo;
    }

    public TrackHistoryResponseVo trackScan(TrackScanRequestVo trackScanRequestVo) {
        User user = userRepository.findByUid(trackScanRequestVo.getUid());
        Food food = foodRepository.findByName(trackScanRequestVo.getFoodName());

        log.info("User : " + user);
        log.info("Food : " + food);
        if (user == null || food == null) {
            log.error("User or Food not found");
            return new TrackHistoryResponseVo(false, "User or Food not found");
        }

        UserHistory userHistory = new UserHistory();
        userHistory.setUser(user);
        userHistory.setFood(food);
        userHistory.setImageUrl(trackScanRequestVo.getImageUrl());
        userHistory.setQuantity(trackScanRequestVo.getQuantity());
        userHistory.setCreatedAt(Timestamp.from(Instant.now()));

        userHistoryRepository.save(userHistory);

        return new TrackHistoryResponseVo(true, "Scan tracked successfully");

    }

}
