package nutrieasy.backend.controller.scan;

import nutrieasy.backend.model.vo.IntakeResponseVo;
import nutrieasy.backend.model.vo.ScanHistoryResponseVo;
import nutrieasy.backend.model.vo.ScanResponseVo;
import nutrieasy.backend.service.NutrieasyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;

/**
 * Created by Resa S.
 * Date: 08-05-2024
 * Created in IntelliJ IDEA.
 */
@RestController
public class NutrieasyController {

    private final NutrieasyService nutrieasyService;

    public NutrieasyController(NutrieasyService nutrieasyService) {
        this.nutrieasyService = nutrieasyService;
    }


    @PostMapping("/scan")
    public ResponseEntity<ScanResponseVo> scan(@RequestParam("img") MultipartFile img, @RequestParam("uid") String uid) {
        if (img == null) {
            return ResponseEntity.badRequest().body(new ScanResponseVo(false, "Image is required", null));
        }
        return ResponseEntity.ok(nutrieasyService.scan(uid, img));
    }

    @GetMapping("/intake")
    public ResponseEntity<IntakeResponseVo> getScanHistory(
            @RequestParam(value = "uid") String uid,
            @RequestParam(value = "date", required = false) String date
            ) throws ParseException {
        return ResponseEntity.ok(nutrieasyService.calculateIntake(uid, date));
    }
}
