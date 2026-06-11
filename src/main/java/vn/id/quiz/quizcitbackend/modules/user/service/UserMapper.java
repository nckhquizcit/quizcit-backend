package vn.id.quiz.quizcitbackend.modules.user.service;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User entity);
    List<UserResponse> toUserResponseList(List<User> entities);
}
