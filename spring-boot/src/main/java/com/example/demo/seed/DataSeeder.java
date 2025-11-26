package com.example.demo.seed;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.ProductStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Component
@Profile("!test")  // 在测试环境中不执行
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("執行 DataSeeder - 初始化系統數據");

        // 初始化用戶數據
        seedUserData();

        // 初始化商品數據
        seedProductData();
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

    private void seedProductData() {
        Product[] products = new Product[] {
            Product.builder()
                .productNo("PROD000001")
                .name("北海道生乳捲")
                .description("人氣甜點，嚴選北海道鮮奶油，口感綿密滑順。")
                .price(new BigDecimal("350.00"))
                .stock(100)
                .imageUrl("https://example.com/images/cake.jpg")
                .status(ProductStatus.OPEN)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusMonths(6))
                .build(),
            Product.builder()
                .productNo("PROD000002")
                .name("UNIQLO 男裝圓領T恤")
                .description("經典百搭，舒適純棉材質，適合日常穿搭。")
                .price(new BigDecimal("399.00"))
                .stock(200)
                .imageUrl("https://example.com/images/tshirt.jpg")
                .status(ProductStatus.OPEN)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusMonths(6))
                .build(),
            Product.builder()
                .productNo("PROD000003")
                .name("飛利浦氣炸鍋")
                .description("健康無油料理，輕鬆享受美味。容量4L，適合家庭使用。")
                .price(new BigDecimal("2990.00"))
                .stock(50)
                .imageUrl("https://example.com/images/airfryer.jpg")
                .status(ProductStatus.OPEN)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusMonths(6))
                .build(),
            Product.builder()
                .productNo("PROD000004")
                .name("哈利波特：神秘的魔法石（繁中版）")
                .description("經典奇幻小說，適合各年齡層閱讀。")
                .price(new BigDecimal("320.00"))
                .stock(80)
                .imageUrl("https://example.com/images/harrypotter1.jpg")
                .status(ProductStatus.OPEN)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusMonths(6))
                .build(),
            Product.builder()
                .productNo("PROD000005")
                .name("象印不鏽鋼保溫杯 480ml")
                .description("長效保溫，輕巧好攜帶，適合上班族與學生。")
                .price(new BigDecimal("890.00"))
                .stock(120)
                .imageUrl("https://example.com/images/thermos.jpg")
                .status(ProductStatus.OPEN)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusMonths(6))
                .build(),
            Product.builder()
                .productNo("PROD000006")
                .name("IKEA LACK 茶几")
                .description("簡約設計，輕巧實用，適合各種居家空間。")
                .price(new BigDecimal("499.00"))
                .stock(60)
                .imageUrl("https://example.com/images/ikea_lack.jpg")
                .status(ProductStatus.OPEN)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusMonths(6))
                .build(),
            Product.builder()
                .productNo("PROD000007")
                .name("小米無線吸塵器")
                .description("輕量無線設計，強力吸塵，適合日常清潔。")
                .price(new BigDecimal("2490.00"))
                .stock(40)
                .imageUrl("https://example.com/images/xiaomi_vacuum.jpg")
                .status(ProductStatus.CLOSED)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusMonths(6))
                .build()
        };
        for (Product seed : products) {
            Product existing = productRepository.findByProductNoAndDeletedAtIsNull(seed.getProductNo());
            if (existing != null) {
                existing.setName(seed.getName());
                existing.setDescription(seed.getDescription());
                existing.setPrice(seed.getPrice());
                existing.setStock(seed.getStock());
                existing.setImageUrl(seed.getImageUrl());
                existing.setStatus(seed.getStatus());
                existing.setStartAt(seed.getStartAt());
                existing.setEndAt(seed.getEndAt());
                productRepository.save(existing);
            } else {
                productRepository.save(seed);
            }
        }
        log.info("商品資料同步完成");
    }
}
