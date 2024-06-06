import com.fasterxml.jackson.core.type.TypeReference;
import nutrieasy.backend.model.NutrientsDetail;
import nutrieasy.backend.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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

    @Test
    public void testJsonUtil2() {



//        Timestamp start = Timestamp.valueOf(now +" 00:00:00");
//        Timestamp end = Timestamp.valueOf(now +" 23:59:59");

//        System.out.println(now);
//        System.out.println(start);
//        System.out.println(end);
    }
}
