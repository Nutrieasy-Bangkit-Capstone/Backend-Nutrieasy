package nutrieasy.backend.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by Resa S.
 * Date: 03-06-2024
 * Created in IntelliJ IDEA.
 */

@Service
public class GoogleCloudStorageService {

    @Value("${spring.cloud.gcp.credentials.location}")
    private String credentialsPath;

    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;

    private final Storage storage;

    public GoogleCloudStorageService() {
        this.storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    }

    public String uploadFile(MultipartFile file, String folderPath) throws IOException {
        String fileName = folderPath + "/" + file.getOriginalFilename();
        BlobInfo blobInfo = storage.create(
                BlobInfo.newBuilder("image-scan-history", fileName).build(),
                file.getInputStream());
        return blobInfo.getMediaLink();
    }
}