package nutrieasy.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Resa S.
 * Date: 03-06-2024
 * Created in IntelliJ IDEA.
 */

@Service
public class GoogleCloudStorageService {

//    private final String credentialsPath = "src/main/resources/service-account.json";
    private final String credentialsPath = "/app/service-account.json";

    private final Storage storage;

    @Value("${gcp.bucket.name.image.history}")
    private String bucketName;

    public GoogleCloudStorageService() throws IOException {

        GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(Paths.get(credentialsPath)));
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public String uploadFile(MultipartFile file, String uid) throws IOException {
        String fileName = uid + "/" + file.getOriginalFilename();
        BlobInfo blobInfo = storage.create(
                BlobInfo.newBuilder(bucketName, fileName).build(),
                file.getInputStream());
        return "https://storage.googleapis.com/" + bucketName + "/" + blobInfo.getName();
    }
}