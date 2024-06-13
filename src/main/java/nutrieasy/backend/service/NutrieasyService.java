package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import nutrieasy.backend.entity.Food;
import nutrieasy.backend.entity.Nutrients;
import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import nutrieasy.backend.model.FoodDetails;
import nutrieasy.backend.model.NutrientsDetail;
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
import nutrieasy.backend.utils.ConstantNutrient;
import nutrieasy.backend.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
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
        String scanModelResult = scanModel(img).getResult();
        String uploadedImageUrl = null;

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
                    nutrientsDetailList.add(nutrientsDetail);
                }
            });
        }

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
