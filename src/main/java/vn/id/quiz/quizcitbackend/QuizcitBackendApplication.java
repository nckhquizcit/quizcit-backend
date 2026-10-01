package vn.id.quiz.quizcitbackend;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = { RedisRepositoriesAutoConfiguration.class })
@EnableScheduling
@EnableAsync
public class QuizcitBackendApplication {

    public static void main(String[] args) {
        // Windows "SE Asia Standard Time" maps to Asia/Saigon; PostgreSQL rejects that
        // legacy IANA name. Force the modern equivalent before any JDBC connection.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(QuizcitBackendApplication.class, args);
    }

}
