# Göktürk 0.2.1 — Kişisel Asistan

Türkçe yapay zekâ sohbet uygulamasının ilk sürümü. Android istemcisi Kotlin/Jetpack Compose, çevrimiçi yapay zekâ katmanı Python/FastAPI ve konuşma saklama katmanı Firebase için hazırlanmıştır.

## Özellikler

- Turkuaz, lacivert ve altın renkli modern arayüz
- Sohbet arka planında yarı şeffaf Selçuklu yıldızı motifleri
- Türkçe sohbet ve metinden görsel üretme
- Parmak izi, yüz veya cihaz kilidiyle sahibine özel açılış
- Türkçe sesle komut ve sesli yanıt
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

Firebase Android yapılandırması projeye eklenmiştir. `android/app/src/main/java/com/gokturk/ai/AppConfig.kt` içindeki `API_BASE_URL` değerini HTTPS API adresinizle değiştirin. Android Studio ile `android` klasörünü açın.

### GitHub üzerinden APK

Depo GitHub'a gönderildiğinde **Actions → Android APK → Run workflow** ile derleme başlatılabilir. Oluşan `app-debug.apk`, Artifacts bölümünden indirilir.

## Güvenlik

- `.env` ve Firebase servis hesabı gibi sunucu sırları Git'e gönderilmez. Android `google-services.json` istemci yapılandırmasıdır; erişim Firestore kurallarıyla korunur.
- Her kullanıcı yalnızca kendi Firestore belgelerini okuyabilir.
- Üretimde API kimlik doğrulaması, hız sınırı ve App Check eklenmelidir.

Bu paket çalışan bir MVP kaynak kodudur. Gerçek cevaplar ve görseller için OpenAI uyumlu bir sağlayıcı ya da kendi model sunucunuz bağlanmalıdır.
