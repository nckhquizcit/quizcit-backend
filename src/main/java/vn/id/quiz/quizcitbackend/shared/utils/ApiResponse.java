package vn.id.quiz.quizcitbackend.shared.utils;

/**
 * ApiResponse dùng để thống nhất mọi API trả về luôn có cùng một
 * cấu trúc cho frontend dễ dàng xử lý mà không gặp bất cứ rắc rồi nào
 * @param status mã trạng thái trả về của HTTP
 * @param message thông điệp muốn gửi kèm
 * @param data dữ liệu muốn kèm theo trong response
 * @param <T> Kiểu dữ liêu trả về của data
 */
public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(200, "Success", data);
    }
    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(200, message, data);
    }
    public static <T> ApiResponse<T> success(org.springframework.http.HttpStatus status, String message, T data){
        return new ApiResponse<>(status.value(), message, data);
    }
    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>(400, message, null);
    }
    public static <T> ApiResponse<T> error(int code, String message){
        return new ApiResponse<>(code, message, null);
    }
    public static <T> ApiResponse<T> error(org.springframework.http.HttpStatus status, String message){
        return new ApiResponse<>(status.value(), message, null);
    }
}
