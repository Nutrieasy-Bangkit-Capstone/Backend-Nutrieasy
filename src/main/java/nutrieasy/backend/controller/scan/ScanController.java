package nutrieasy.backend.controller.scan;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Resa S.
 * Date: 08-05-2024
 * Created in IntelliJ IDEA.
 */
@RestController
public class ScanController {

    @GetMapping("/scan")
    @Secured("ROLE_USER")
    public String scan() {
        return "Scanning...";
    }
}
