package nutrieasy.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nutrieasy.backend.entity.Food;
import nutrieasy.backend.model.ScanHistory;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 31-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanHistoryResponseVo {
    private Boolean success;
    private String message;
    private List<ScanHistory> data;
}
