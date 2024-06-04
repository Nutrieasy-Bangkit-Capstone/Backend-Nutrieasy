package nutrieasy.backend.controller;

import nutrieasy.backend.service.GoogleCloudStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Resa S.
 * Date: 04-05-2024
 * Created in IntelliJ IDEA.
 */
@RestController
public class HelloWorldController {

    private final GoogleCloudStorageService googleCloudStorageService;

    public HelloWorldController(GoogleCloudStorageService googleCloudStorageService) {
        this.googleCloudStorageService = googleCloudStorageService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, World!");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String uplod = googleCloudStorageService.uploadFile(file, "sdgvakgdvfydffk");
        return String.format("File %s uploaded to bucket as %s",
                file.getOriginalFilename(), uplod);
    }
}
