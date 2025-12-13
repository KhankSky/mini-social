# MiniSocial Desktop Application

## 🛠️ Công nghệ sử dụng

- **Java**: 17+
- **JavaFX**: 21.0.2
- **Maven**: Build tool và dependency management
- **FXML**: UI layout definition
- **CSS**: Styling cho JavaFX components
- **Jackson**: JSON processing cho API integration 

## 📦 Yêu cầu hệ thống

- **Java Development Kit (JDK)**: 17 hoặc cao hơn
- **Maven**: 3.6+ (hoặc sử dụng Maven Wrapper có sẵn)
- **Hệ điều hành**: Windows, macOS, hoặc Linux

## ▶️ Chạy ứng dụng

### Chạy từ Maven

**Windows:**
```bash
mvnw.cmd javafx:run
```

**Linux/macOS:**
```bash
./mvnw javafx:run
```

Hoặc:
```bash
mvn javafx:run
```

# Chạy JAR (cần JavaFX modules)
java --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml -jar target/social-desktop-1.0.0-SNAPSHOT.jar
```

Hiện tại ứng dụng đang sử dụng mock data. Để tích hợp với backend API:

1. Tạo service classes trong `com.minisocial.desktop.service`
2. Tạo DTO classes trong `com.minisocial.desktop.dto`
3. Cấu hình API endpoint trong `com.minisocial.desktop.config`
4. Cập nhật controllers để sử dụng services thay vì mock data


