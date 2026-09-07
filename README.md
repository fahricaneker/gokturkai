# Göktürk

Türkçe yapay zekâ sohbet uygulamasının ilk sürümü. Android istemcisi Kotlin/Jetpack Compose, çevrimiçi yapay zekâ katmanı Python/FastAPI ve konuşma saklama katmanı Firebase için hazırlanmıştır.

## Özellikler

- Turkuaz, lacivert ve altın renkli modern arayüz
- Sohbet arka planında yarı şeffaf Selçuklu yıldızı motifleri
- Türkçe sohbet ve metinden görsel üretme
- Firebase hazır olduğunda anonim kullanıcı ve Firestore sohbet kaydı
- API anahtarını APK içine koymayan Python ara sunucusu
- GitHub Actions ile otomatik debug APK üretimi

## Hızlı başlangıç

### Firebase

Firebase Console'da Android uygulaması açın ve paket adını `com.gokturk.ai` seçin. `google-services.json` dosyasını `android/app/google-services.json` yoluna koyun. Authentication içinde Anonymous sağlayıcısını açın, Firestore oluşturun ve kökteki `firestore.rules` kurallarını yayınlayın.

### Python API

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload
```

`.env` içine OpenAI uyumlu hizmetinizin adresi, modeli ve anahtarı yazılır. Gizli anahtar Android uygulamasına veya GitHub deposuna eklenmez.

### Android

`android/app/src/main/java/com/gokturk/ai/AppConfig.kt` içindeki `API_BASE_URL` değerini HTTPS API adresinizle değiştirin. Android Studio ile `android` klasörünü açın.

### GitHub üzerinden APK

Depo GitHub'a gönderildiğinde **Actions → Android APK → Run workflow** ile derleme başlatılabilir. Oluşan `app-debug.apk`, Artifacts bölümünden indirilir. Firebase kullanılacaksa depo secret'ı olarak `GOOGLE_SERVICES_JSON_BASE64` eklenebilir.

## Güvenlik

- `.env`, servis hesabı dosyaları ve `google-services.json` Git'e gönderilmez.
- Her kullanıcı yalnızca kendi Firestore belgelerini okuyabilir.
- Üretimde API kimlik doğrulaması, hız sınırı ve App Check eklenmelidir.

Bu paket çalışan bir MVP kaynak kodudur. Gerçek cevaplar ve görseller için OpenAI uyumlu bir sağlayıcı ya da kendi model sunucunuz bağlanmalıdır.
