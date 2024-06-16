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
            return new UserHistoryResponseVo(true, "User have no history", null);
        }

        List<HistoryModel> historyModelList = new ArrayList<>();
        userHistory.forEach(useh -> {

            HistoryModel historyModel = new HistoryModel();
            historyModel.setId(useh.getId());
            historyModel.setUserId(useh.getUser().getUid());
            historyModel.setFoodId(useh.getFood().getId());
            historyModel.setFoodName(useh.getFood().getName());
            historyModel.setImageUrl(useh.getImageUrl());
            historyModel.setServingQty(useh.getFood().getServingQty());
            historyModel.setServingUnit(useh.getFood().getServingUnit());
            historyModel.setServingWeightGrams(useh.getFood().getServingWeightGrams());
            historyModel.setNutrientsDetailList(JsonUtil.convertJsonToList(useh.getFood().getNutrientsJson(),  new TypeReference<List<NutrientsDetail>>() {}));
            historyModel.setCreatedAt(useh.getCreatedAt().toString());
            historyModelList.add(historyModel);
        });

        UserHistoryResponseVo resp = new UserHistoryResponseVo(true, "History found", historyModelList);
        log.info("User history response : " + JsonUtil.convertObjectToJson(resp));

        return resp;
    }
}
