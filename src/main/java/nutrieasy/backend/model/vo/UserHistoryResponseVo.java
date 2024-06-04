package nutrieasy.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nutrieasy.backend.model.HistoryModel;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 04-06-2024
 * Created in IntelliJ IDEA.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHistoryResponseVo {
    private Boolean success;
    private String message;
    private List<HistoryModel> history;
}
