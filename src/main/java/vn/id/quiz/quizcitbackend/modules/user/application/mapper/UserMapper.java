package vn.id.quiz.quizcitbackend.modules.user.application.mapper;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.domain.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User entity);
    List<UserResponse> toUserResponseList(List<User> entities);
}
