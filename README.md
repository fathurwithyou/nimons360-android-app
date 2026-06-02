# NIMONS360

## Deskripsi

NIMONS360 adalah aplikasi Android untuk memantau lokasi dan status anggota keluarga secara real-time, mengelola keluarga, serta menyimpan lokasi favorit. Aplikasi ini mengintegrasikan REST API, WebSocket, penyimpanan lokal, GPS, dan sensor perangkat untuk mendukung pengalaman tracking yang responsif di perangkat Android.

## Cara Setup App

### Requirements

- Android Studio
- JDK 11
- Android SDK 35
- Emulator atau device Android minimal API 30
- Koneksi internet aktif
- GPS atau location service aktif

### Langkah Menjalankan

1. Clone repository dan masuk ke direktori project.

```bash
git clone https://github.com/Labpro-22/ms1-k02-ege.git
cd ms1-k02-ege
```

2. Buka root project di Android Studio, lalu pastikan environment Android sesuai dengan konfigurasi project
3. Jalankan `Gradle Sync` hingga seluruh dependency berhasil ter-resolve tanpa error.
4. Verifikasi bahwa project dapat dibangun dengan sukses melalui Gradle Wrapper.

```bash
./gradlew clean
./gradlew assembleDebug
```

5. Jika diperlukan, jalankan unit test untuk memastikan logika dasar aplikasi berjalan sesuai harapan.

```bash
./gradlew test
```

6. Hubungkan emulator atau physical device dengan Android 11 ke atas, lalu install build debug ke perangkat.

```bash
./gradlew installDebug
```

7. Alternatifnya, aplikasi dapat dijalankan langsung dari Android Studio menggunakan konfigurasi `app`.
8. Saat aplikasi pertama kali dijalankan, lakukan autentikasi menggunakan kredensial berikut:

   - Email: `{NIM}@std.stei.itb.ac.id`
   - Password: `{NIM}`
9. Berikan izin lokasi ketika diminta agar fitur peta, pelacakan posisi, dan sinkronisasi presence dapat berjalan dengan benar.

## Library yang Digunakan

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Navigation
- ViewModel, Lifecycle
- Room
- DataStore
- Retrofit
- OkHttp
- Kotlinx Serialization
- Kotlin Coroutines
- osmdroid
- Coil
- Media3

## Command

```bash
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
./gradlew build
./gradlew test
```

## OWASP Mobile Security

Analisis dilakukan dengan asumsi server tidak sepenuhnya aman, sehingga validasi dan konfigurasi aman tetap diterapkan di sisi client.

### M4: Insufficient Input/Output Validation

Risiko:

- Input nama profil, pesan notifikasi, kode family, dan marked location dapat kosong atau tidak sesuai format.
- Upload foto profil dan foto marked location berisiko menerima file selain gambar atau ukuran terlalu besar.

Perbaikan:

- Form join family membatasi kode menjadi 6 karakter.
- Form pesan notifikasi dan greeting tidak dapat dikirim jika pesan kosong.
- Upload foto profil dan foto marked location hanya menerima `image/png` atau `image/jpeg`.
- Ukuran foto dibatasi maksimal 500 KB sebelum dikirim ke API atau disimpan lokal.
- Response API dipetakan melalui DTO/domain model dan `ignoreUnknownKeys`, sehingga field tambahan dari server tidak langsung merusak UI.

### M8: Security Misconfiguration

Risiko:

- Cleartext traffic global sebelumnya aktif.
- Logging HTTP body dapat mengekspos token, data profil, pesan, dan payload multipart.

Perbaikan:

- `android:usesCleartextTraffic` diset `false`.
- `network_security_config.xml` memblokir cleartext secara default.
- Pengecualian cleartext hanya diberikan untuk `10.0.2.2` agar coordinator livestream lokal tetap bisa dipakai saat pengembangan.
- Logging OkHttp API utama diturunkan dari `BODY` menjadi `BASIC`, sehingga request/response body sensitif tidak dicetak.

### M9: Insecure Data Storage

Risiko:

- Auth token masih disimpan di DataStore preferences biasa.
- Foto marked location disimpan di filesystem lokal aplikasi.

Perbaikan saat ini:

- Foto marked location disimpan di internal app files directory, bukan public external storage.
- Metadata marked location disimpan di Room/SQLite.
- Saat marked location dihapus, semua path foto terkait dihapus dari filesystem melalui repository.
- Preferensi notifikasi dan location sharing disimpan lokal memakai SharedPreferences sesuai spesifikasi.

Sisa risiko:

- Auth token belum memakai encrypted storage. Perbaikan lanjutan yang direkomendasikan adalah migrasi token dari DataStore biasa ke penyimpanan terenkripsi berbasis Android Keystore.

## Screenshot

Simpan screenshot aplikasi di folder `screenshot/`.

### Splash

- Menampilkan splash screen saat aplikasi dibuka sebagai transisi awal ke login atau home.

<img src="screenshot/splash.jpeg" width="270"/>

### Login

- Menampilkan form autentikasi dengan input email dan password.
- Setelah login berhasil, pengguna diarahkan ke halaman `Home`.

<img src="screenshot/login.png" width="270"/>

### Home

- Menampilkan daftar `My Families` dan `Discover Families`.
- Terdapat avatar menuju `Profile` dan tombol `+` menuju `Create Family`.

<img src="screenshot/home.jpeg" width="270"/>

### Families

- Menampilkan daftar seluruh family dengan fitur search, filter, dan pin lokal.
- Terdapat akses ke `Profile` dan `Create Family`.

<img src="screenshot/families.jpeg" width="270"/>

### Create Family

- Menampilkan form pembuatan family dengan input nama dan pemilihan icon.
- Setelah berhasil dibuat, pengguna diarahkan ke halaman detail family.

<img src="screenshot/createFamily.jpeg" width="270"/>

### Family Detail

- Menampilkan detail family, daftar anggota, family code, serta aksi `Join` atau `Leave`.
- Family code dapat disalin jika pengguna sudah tergabung dalam family.

<img src="screenshot/familyDetail.jpeg" width="270"/>

### Map

- Menampilkan peta interaktif, posisi pengguna, posisi anggota family lain, dan favorite location.
- Tersedia filter family, kontrol zoom, detail member, dan integrasi lokasi realtime.

<img src="screenshot/map.jpeg" width="270"/>

### Profile

- Menampilkan avatar, nama, email, serta fitur edit nama dan sign out.

<img src="screenshot/map.jpeg" width="270"/>

## Pembagian Kerja

| Anggota        | NIM      | Tugas                                                  |
| -------------- | -------- | ------------------------------------------------------ |
| Muhammad Fathur Rizky | 13523105 | Auth, Login, Logout, Profile, Open API, Livestreaming  |
| Reza Ahmad Syarif | 13523119 | Home, Map, Network Sensing                             |
| Ahmad Wicaksono | 13523121 | Families, Create Family, Detail Families               |

## Jam Persiapan dan Pengerjaan

| Anggota        | Jam Persiapan | Jam Pengerjaan |
| -------------- | ------------: | -------------: |
| Muhammad Fathur Rizky |        10 jam |         46 jam |
| Reza Ahmad Syarif |        12 jam |         51 jam |
| Ahmad Wicaksono |         8 jam |         43 jam |
