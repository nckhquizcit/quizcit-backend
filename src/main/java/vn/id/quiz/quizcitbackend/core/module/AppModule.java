package vn.id.quiz.quizcitbackend.core.module;


/**
 * Interface dành cho các module của App, bắt buộc các module của app phải implements interface này
 */
public interface AppModule {
    /**
     * Phương thức dùng để khai báo bản thân module với app
     * <br/>Thực chất DI đã tự tim nó vào app rồi, nhưng vấn đề là nó sẽ ngủ cho đến khi được gọi hàm
     * <br/>Việc gọi initialize là một cách chủ động để module chạy ngay, không rơi vào trạng thái chừo
     * */
    public void initialize();
    /**
     * Dùng để lấy name cho ModuleEngine
     * */
    public String getName();
}
