package nutrieasy.backend.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private final RestTemplate restTemplate;

    private final Storage storage;
    public HelloWorldController(RestTemplate restTemplate) throws IOException {
        this.restTemplate = restTemplate;
        // Path to the service account key file inside the container
        String keyFilePath = "/app/service-account.json";
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(keyFilePath));
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
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
        String bucketName = "image-scan-history";  // replace with your bucket name
        String blobName = file.getOriginalFilename();

        BlobInfo blobInfo = storage.create(
                BlobInfo.newBuilder(bucketName, blobName).build(),
                file.getInputStream()
        );

        System.out.println(blobInfo.getMediaLink());

        return String.format("File %s uploaded to bucket %s as %s",
                file.getOriginalFilename(), bucketName, blobInfo.getMediaLink());
    }
}
