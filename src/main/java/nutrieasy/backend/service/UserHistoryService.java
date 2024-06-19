package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import nutrieasy.backend.model.HistoryModel;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.vo.UserHistoryResponseVo;
import nutrieasy.backend.repository.UserHistoryRepository;
import nutrieasy.backend.repository.UserRepository;
import nutrieasy.backend.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Resa S.
 * Date: 04-06-2024
 * Created in IntelliJ IDEA.
 */
@Service
@Slf4j
public class UserHistoryService {

    private final UserHistoryRepository userHistoryRepository;
    private final UserRepository userRepository;

    public UserHistoryService(UserHistoryRepository userHistoryRepository, UserRepository userRepository) {
        this.userHistoryRepository = userHistoryRepository;
        this.userRepository = userRepository;
    }

    public UserHistoryResponseVo getUserHistory(String uid, String date) throws ParseException {
        User user = userRepository.findByUid(uid);
        if (user == null) {
            return new UserHistoryResponseVo(false, "User not found", null);
        }
        List<UserHistory> userHistory = null;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (date == null || date.isEmpty()) {
            date = formatter.format(new Date());
        }

        log.info("Getting user history for user : " + uid + " and date : " + date);

        Date d = formatter.parse(date);
        userHistory = userHistoryRepository.findAllByUserAndDate(user, d);


        if (userHistory.isEmpty()) {
            return new UserHistoryResponseVo(true, "User have no history", new ArrayList<>());
        }

        List<HistoryModel> historyModelList = convertUserHistory(userHistory);

        UserHistoryResponseVo resp = new UserHistoryResponseVo(true, "History found", historyModelList);
        log.info("User history response : " + JsonUtil.convertObjectToJson(resp));

        return resp;
    }


    public UserHistoryResponseVo getAllUserHistory(String uid) throws ParseException {
        User user = userRepository.findByUid(uid);
        if (user == null) {
            return new UserHistoryResponseVo(false, "User not found", new ArrayList<>());
        }
        List<UserHistory> userHistory = null;


        log.info("Getting all user history for user : " + uid);

        userHistory = userHistoryRepository.findAllByUserOrderByCreatedAtDesc(user);


        if (userHistory.isEmpty()) {
            return new UserHistoryResponseVo(true, "User have no history", new ArrayList<>());
        }

        List<HistoryModel> historyModelList = convertUserHistory(userHistory);


        UserHistoryResponseVo resp = new UserHistoryResponseVo(true, "History found", historyModelList);
        log.info("User history response : " + JsonUtil.convertObjectToJson(resp));

        return resp;
    }

    private List<HistoryModel> convertUserHistory(List<UserHistory> userHistory) {
        List<HistoryModel> historyModelList = new ArrayList<>();
        userHistory.forEach(useh -> {

            HistoryModel historyModel = new HistoryModel();
            historyModel.setId(useh.getId());
            historyModel.setUserId(useh.getUser().getUid());
            historyModel.setFoodId(useh.getFood().getId());
            historyModel.setFoodName(useh.getFood().getName());
            historyModel.setImageUrl(useh.getImageUrl());
            historyModel.setServingQty(useh.getQuantity());
            historyModel.setServingUnit(useh.getFood().getServingUnit());
            historyModel.setServingWeightGrams(useh.getFood().getServingWeightGrams());

            List<NutrientsDetail> listOfNutrientsDetail = JsonUtil.convertJsonToList(useh.getFood().getNutrientsJson(),  new TypeReference<List<NutrientsDetail>>() {});
            List<NutrientsDetail> finalListOfNutrientsDetail = new ArrayList<>();

            if (useh.getFood().getServingQty() != useh.getQuantity()) {
                for (NutrientsDetail nd : listOfNutrientsDetail) {
                    NutrientsDetail newNd = new NutrientsDetail();
                    newNd.setAttrId(nd.getAttrId());
                    newNd.setUnit(nd.getUnit());
                    newNd.setValue(nd.getValue() * useh.getQuantity());
                    newNd.setName(nd.getName());

                    finalListOfNutrientsDetail.add(newNd);
                }
            } else {
                finalListOfNutrientsDetail = listOfNutrientsDetail;
            }

            historyModel.setNutrientsDetailList(finalListOfNutrientsDetail);
            historyModel.setCreatedAt(useh.getCreatedAt().toString());
            historyModelList.add(historyModel);
        });
        return historyModelList;
    }
}
