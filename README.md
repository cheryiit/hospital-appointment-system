# Hospital Appointment System

## 🚀 Hızlı Başlangıç

Bu proje, Spring Boot ve MySQL tabanlı bir randevu ve hasta takip sistemidir. Tüm ayarlar `.env` dosyasından yönetilir. Docker ile veya manuel olarak kolayca kurup çalıştırabilirsiniz.

---

## 1️⃣ Docker ile Kurulum (En Kolay ve Otomatik Yöntem)

1. **.env dosyasını oluşturun:**
   ```bash
   cp .env.example .env
   # .env dosyasını açıp şifre, kullanıcı adı ve port gibi bilgileri düzenleyin
   ```

2. **Docker ile başlatın:**
   ```bash
   docker-compose up --build
   ```

3. **Uygulamaya erişin:**
   - [http://localhost:8080](http://localhost:8080) (veya .env'de belirttiğiniz port)

> Docker ile başlatınca MySQL ve uygulama otomatik başlar, veritabanı ve tablolar otomatik oluşur. Hiçbir manuel veritabanı işlemi gerekmez.

---

## 2️⃣ Manuel (Lokal) Kurulum (Geliştiriciler için)

### A. Gereksinimler
- Java 17+
- Maven
- MySQL (lokalde çalışır durumda)

### B. Adımlar

1. **MySQL'i kurun ve başlatın.**
   - `.env` dosyasındaki kullanıcı adı, şifre ve veritabanı adını MySQL'de oluşturun (veya root ile otomatik oluşmasını sağlayın).

2. **.env dosyasını oluşturun ve düzenleyin:**
   ```bash
   cp .env.example .env
   # .env dosyasını açıp bilgileri doldurun
   ```

3. **Environment variable'ları terminale yükleyin:**
   > Spring Boot, `.env` dosyasını otomatik okumaz! Değişkenleri terminal ortamına yüklemeniz gerekir.
   ```bash
   export $(grep -v '^#' .env | xargs)
   ```

4. **Profil seçimi:**
   - Lokal çalıştırma için `.env` dosyanızda şu satır olmalı:
     ```
     SPRING_PROFILES_ACTIVE=local
     ```
   - Docker ile çalıştıracaksanız:
     ```
     SPRING_PROFILES_ACTIVE=docker
     ```

5. **Projeyi başlatın:**
   ```bash
   mvn clean spring-boot:run
   # veya profil belirtmek için:
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

6. **Uygulamaya erişin:**
   - [http://localhost:8080](http://localhost:8080) (veya .env'de belirttiğiniz port)

---

## 3️⃣ Sık Karşılaşılan Hatalar ve Çözümleri

- **Hata:** `Unable to determine Dialect without JDBC metadata` veya `Failed to determine a suitable driver class`
  - **Çözüm:**
    - `.env` dosyasındaki değişkenleri terminale yüklediğinizden emin olun:
      ```bash
      export $(grep -v '^#' .env | xargs)
      ```
    - `mysql-connector-j` dependency'si pom.xml'de mevcut olmalı (bu projede zaten var).
    - Profilin doğru olduğundan emin olun: `SPRING_PROFILES_ACTIVE=local`

- **Hata:** `UnknownHostException: db`
  - **Çözüm:** Yanlış profil seçilmiş olabilir. Lokal çalıştırmada `SPRING_PROFILES_ACTIVE=local` olmalı.

---

## 4️⃣ .env ve .env.example Dosyası

`.env` dosyanız örnek olarak şöyle olmalı:
```
MYSQL_DATABASE=hospital_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456
MYSQL_ROOT_PASSWORD=123456
SERVER_PORT=8080
SECURITY_USER_NAME=admin
SECURITY_USER_PASSWORD=admin
SPRING_PROFILES_ACTIVE=local
```

> **Not:** Docker ile çalıştırırken `SPRING_PROFILES_ACTIVE=docker` olmalı ve `SPRING_DATASOURCE_URL` yerine sadece yukarıdaki gibi değişkenler olmalı.

---

## 5️⃣ Ekstra: Tek Komutla Her Şey

Her yeni terminalde, hiçbir şeyi unutmak istemiyorsan:
```bash
export $(grep -v '^#' .env | xargs) && mvn clean spring-boot:run
```

---

## 6️⃣ Geliştirici Notları
- Tüm şifre, kullanıcı adı, port veya admin bilgilerini `.env` dosyasından güncelleyebilirsiniz.
- Proje ilk açılışta otomatik olarak veritabanını, tabloları ve demo kullanıcıları oluşturur.
- Docker ile kurulum önerilir, manuel kurulum ise geliştirme/test için uygundur.
- Sorun yaşarsanız, environment variable'ların terminalde yüklü olduğundan ve doğru profili kullandığınızdan emin olun. 