package vn.id.quiz.quizcitbackend.core.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLOutput;
import java.util.List;

@Component
public class ModuleEngine {
    private final List<AppModule> availableModules;

    //Thật ra trong Spring 4.3 trở đi thì không cần autowire các component chỉ có duy nhất một constructor
    @Autowired // Vẫn thêm Autowired cho dễ đọc
    public ModuleEngine(List<AppModule> availableModules){
        this.availableModules = availableModules;
    }
//Cái event bên dưới tự được spring publish sau khi khởi tạo xong các thành phần.
    @EventListener(ApplicationReadyEvent.class)
    public void runModules(){
        System.out.println("==== Đang bắt đầu kích hoạt các module ====");
        availableModules.forEach(module->{
            System.out.println("- Đang gọi initialize() của module: " + module.getName());
            try{
                module.initialize();
            }catch(Exception e){ //Nhớ đổi kiểu exception nếu có kiểu custom cho lỗi này
                System.out.println("!!! Gặp lỗi khi kích hoạt module "+module.getName()+" !!!");
                System.out.println("---- Start traceback lỗi ----");
                e.printStackTrace();
                System.out.println("---- End traceback lỗi ----");
            }
        });
        System.out.println("==== Tất cả module đã được nạp thành công ====");

    }
}
