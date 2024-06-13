package nutrieasy.backend.model.vo;

import lombok.Data;

/**
 * Created by Resa S.
 * Date: 11-06-2024
 * Created in IntelliJ IDEA.
 */
@Data
public class TrackScanRequestVo {
    private String uid;
    private String foodName;
    private int quantity;
    private String imageUrl;
}
