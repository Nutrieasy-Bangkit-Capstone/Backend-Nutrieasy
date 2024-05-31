import com.fasterxml.jackson.core.type.TypeReference;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Resa S.
 * Date: 31-05-2024
 * Created in IntelliJ IDEA.
 */

public class TestJsonUtil {
    List<NutrientsDetail> details = new ArrayList<>();
    @BeforeEach
    void setUp() {

    }

    @Test
    public void testJsonUtil() {
         String json = JsonUtil.convertListToJson(details);
         System.out.println(json);
         List<NutrientsDetail> details = JsonUtil.convertJsonToList(json, new TypeReference<List<NutrientsDetail>>() {});
         System.out.println(details);
    }
}
