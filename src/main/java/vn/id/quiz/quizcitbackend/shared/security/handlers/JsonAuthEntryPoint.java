package vn.id.quiz.quizcitbackend.shared.security.handlers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

/**
 * Trả về JSON {@link ApiResponse} thay vì trang HTML mặc định khi
 * request thiếu token hoặc token không hợp lệ. Tương thích với cấu
 * trúc response mà frontend đang mong đợi (status, message, data).
 */
@Component
public class JsonAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> body = ApiResponse.error(
                HttpStatus.UNAUTHORIZED,
                "Yeu cau dang nhap de truy cap tai nguyen nay");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
