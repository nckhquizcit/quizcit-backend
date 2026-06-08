import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @Colunm(name="question_id", length = 50)
    private String questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name="content")
    private String content;

    @Column(name="explain")
    private String explain;

    @Colunm(name="difficulty_b")
    private Double difficultyB;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive;

    public static Question create(Subject subject, Chapter chapter, String content, String explain, Double difficultyB){
        return Question.builder()
        .questionId(UUID.randomUUID().toString())
        .subject(subject)
        .chapter(chapter)
        .content(content)
        .explain(explain)
        .difficultyB(difficultyB)
        .createAt(ZonedDateTime.now())
        .isActive(true)
        .build();
    }
}
