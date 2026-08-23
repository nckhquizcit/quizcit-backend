package vn.id.quiz.quizcitbackend.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthUserState;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.QuizSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AdminDataStatsResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CacheConfigTest {

    private final CacheConfig cacheConfig = new CacheConfig();
    private final GenericJackson2JsonRedisSerializer serializer = cacheConfig.genericJackson2JsonRedisSerializer();

    @Test
    void shouldSerializeAndDeserializeAuthUserState() {
        AuthUserState original = new AuthUserState("u123", UserStatus.VERIFIED, 5L);
        byte[] bytes = serializer.serialize(original);
        assertThat(bytes).isNotNull();

        Object deserialized = serializer.deserialize(bytes);
        assertThat(deserialized).isInstanceOf(AuthUserState.class);
        AuthUserState result = (AuthUserState) deserialized;
        assertThat(result.userId()).isEqualTo("u123");
        assertThat(result.status()).isEqualTo(UserStatus.VERIFIED);
        assertThat(result.tokenVersion()).isEqualTo(5L);
    }

    @Test
    void shouldSerializeAndDeserializeSubjectResponseWithZonedDateTime() {
        SubjectResponse original = new SubjectResponse(
                "s1", "CS101", "Computer Science", "Desc", true, ZonedDateTime.now(), 1, "img.png");
        byte[] bytes = serializer.serialize(original);
        assertThat(bytes).isNotNull();

        Object deserialized = serializer.deserialize(bytes);
        assertThat(deserialized).isInstanceOf(SubjectResponse.class);
        SubjectResponse result = (SubjectResponse) deserialized;
        assertThat(result.subjectId()).isEqualTo("s1");
        assertThat(result.courseCode()).isEqualTo("CS101");
        assertThat(result.name()).isEqualTo("Computer Science");
    }

    @Test
    void shouldSerializeAndDeserializeSkillResponseList() {
        SkillResponse s1 = SkillResponse.builder().skillId("sk1").name("Algorithm").description("Tree").build();
        List<SkillResponse> list = List.of(s1);
        byte[] bytes = serializer.serialize(list);
        assertThat(bytes).isNotNull();

        Object deserialized = serializer.deserialize(bytes);
        assertThat(deserialized).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<SkillResponse> result = (List<SkillResponse>) deserialized;
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkillId()).isEqualTo("sk1");
    }

    @Test
    void shouldSerializeAndDeserializeAdminDataStatsResponse() {
        AdminDataStatsResponse original = new AdminDataStatsResponse(10, 5, 100, 20, 50);
        byte[] bytes = serializer.serialize(original);
        assertThat(bytes).isNotNull();

        Object deserialized = serializer.deserialize(bytes);
        assertThat(deserialized).isInstanceOf(AdminDataStatsResponse.class);
        AdminDataStatsResponse result = (AdminDataStatsResponse) deserialized;
        assertThat(result.totalUsers()).isEqualTo(10);
        assertThat(result.totalQuestions()).isEqualTo(100);
    }

    @Test
    void errorHandlerShouldLogAndNotThrow() {
        CacheErrorHandler handler = cacheConfig.errorHandler();
        Cache mockCache = mock(Cache.class);
        when(mockCache.getName()).thenReturn("test_cache");
        RuntimeException ex = new RuntimeException("Redis connection refused");

        assertThatCode(() -> handler.handleCacheGetError(ex, mockCache, "key1")).doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCachePutError(ex, mockCache, "key1", "val1")).doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCacheEvictError(ex, mockCache, "key1")).doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCacheClearError(ex, mockCache)).doesNotThrowAnyException();
    }
}
