package vn.id.quiz.quizcitbackend.shared.utils;

import org.mapstruct.Mapper;

/**
 * Cái này sinh ra giải quyết vấn đề JPA chỉ nhận entity và ta luôn trả DTO về cho client,
 * việc convert qua lại giữa entity và dto sẽ luôn làm cho code trong service rất rất dài, vì thế
 * sử dụng mapper để convert sẽ là một lựa chọn hay. Tất nhiên sau khi convert vẫn sẽ bọc nó lại với
 * ApiResponse để thống nhất dữ liệu trả về
 * @param <Entity> entity gốc
 * @param <Response> dto của response
 * @param <Request> dto của request
 */

public interface BaseMapper<Entity, Response, Request> {
    Entity toEntity(Request dto);
    Response toResponse(Entity entity);
    Request toRequest(Entity entity);

}
