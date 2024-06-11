package nutrieasy.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Created by Resa S.
 * Date: 04-05-2024
 * Created in IntelliJ IDEA.
 */

@SpringBootApplication
@Slf4j
public class NutrieasyBackendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NutrieasyBackendServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+7"));
        log.info("Application started... with timezone GMT+7");
    }
}
