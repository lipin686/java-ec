package com.example.demo.seed;

import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("執行 DataSeeder - 初始化系統數據");

        // 初始化用戶數據
        seedUserData();
    }

    private void seedUserData() {
        // 檢查是否已有管理員
        if (userRepository.countAdmins() == 0) {
            log.info("創建默認管理員帳戶...");

            // 創建超級管理員（同時擁有前台和後台權限）
            User superAdmin = User.builder()
                    .name("Super Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(UserRole.USER, UserRole.ADMIN))  // 同時擁有兩個角色
                    .authProvider("local")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .deleted(false)
                    .createdBy("SYSTEM")
                    .build();

            userRepository.save(superAdmin);
            log.info("默認管理員帳戶創建完成 - Email: admin@example.com, Password: admin123 (前台+後台權限)");
        }

        // 檢查是否已有前台用戶
        if (!userRepository.existsByEmailAndDeletedFalse("user@example.com")) {
            log.info("創建測試前台用戶...");

            // 創建測試前台用戶
            User testUser = User.builder()
                    .name("Test User")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .roles(Set.of(UserRole.USER))
                    .authProvider("local")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .deleted(false)
                    .createdBy("SYSTEM")
                    .build();

            userRepository.save(testUser);
            log.info("測試前台用戶創建完成 - Email: user@example.com, Password: user123");
        }

        // 創建一個同時擁有兩個角色的用戶（展示多角色功能）
        if (!userRepository.existsByEmailAndDeletedFalse("hybrid@example.com")) {
            log.info("創建混合角色用戶...");

            User hybridUser = User.builder()
                    .name("Hybrid User")
                    .email("hybrid@example.com")
                    .password(passwordEncoder.encode("hybrid123"))
                    .roles(Set.of(UserRole.USER, UserRole.ADMIN))  // 同時擁有兩個角色
                    .authProvider("local")
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .deleted(false)
                    .createdBy("SYSTEM")
                    .build();

            userRepository.save(hybridUser);
            log.info("混合角色用戶創建完成 - Email: hybrid@example.com, Password: hybrid123");
        }

        log.info("用戶數據初始化完成");
        log.info("=== 測試帳戶信息 ===");
        log.info("管理員: admin@example.com / admin123 (前台+後台)");
        log.info("前台用戶: user@example.com / user123 (僅前台)");
        log.info("混合角色: hybrid@example.com / hybrid123 (前台+後台)");
        log.info("==================");
    }
}
