package nutrieasy.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import nutrieasy.backend.model.HistoryModel;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.model.vo.UserHistoryResponseVo;
import nutrieasy.backend.repository.UserHistoryRepository;
import nutrieasy.backend.repository.UserRepository;
import nutrieasy.backend.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Resa S.
 * Date: 04-06-2024
 * Created in IntelliJ IDEA.
 */
@Service
public class UserHistoryService {

    private final UserHistoryRepository userHistoryRepository;
    private final UserRepository userRepository;

    public UserHistoryService(UserHistoryRepository userHistoryRepository, UserRepository userRepository) {
        this.userHistoryRepository = userHistoryRepository;
        this.userRepository = userRepository;
    }

    public UserHistoryResponseVo getUserHistory(String uid, String date) {
        User user = userRepository.findByUid(uid);
        if (user == null) {
            return new UserHistoryResponseVo(false, "User not found", null);
        }
        List<UserHistory> userHistory = null;

        if (date == null || date.isEmpty()) {
            userHistory = userHistoryRepository.findAllByUser(user);
        } else {
            Timestamp t1 = Timestamp.valueOf(date+" 00:00:00");
            Timestamp t2 = Timestamp.valueOf(date+" 23:59:59");
            userHistory = userHistoryRepository.findAllByUserAndCreatedAtBetween(user, t1, t2);
        }

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

        return new UserHistoryResponseVo(true, "History found", historyModelList);
    }
}
