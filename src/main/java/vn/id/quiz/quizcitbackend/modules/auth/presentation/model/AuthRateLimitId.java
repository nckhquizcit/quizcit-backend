package vn.id.quiz.quizcitbackend.modules.auth.presentation.model;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class AuthRateLimitId implements Serializable {
    private String rateLimitKey;
    private String action;
}
