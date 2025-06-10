# 📚 WordUp Dictionary App - Aplikasi Kamus Digital Interaktif

**Android | Java | SQLite | API Integration | Material Design | Dark Mode**

> *"Belajar Bahasa Inggris dengan Cara yang Menyenangkan dan Interaktif"*

WordUp Dictionary App adalah aplikasi Android berbasis Java yang memberikan pengalaman belajar bahasa Inggris yang komprehensif. Aplikasi ini menggabungkan fitur kamus digital, sistem quiz interaktif, tracking progress, dan achievement system untuk membuat pembelajaran vocabulary menjadi lebih engaging dan efektif.

---

## 📋 Daftar Isi

1. [🎯 Tentang Proyek](#-tentang-proyek)
2. [✨ Fitur Utama](#-fitur-utama)
3. [📱 Tampilan Aplikasi](#-tampilan-aplikasi)
4. [🛠️ Implementasi Teknis](#️-implementasi-teknis)
5. [🔧 Teknologi yang Digunakan](#-teknologi-yang-digunakan)
6. [🚀 Cara Memulai](#-cara-memulai)
7. [📄 Lisensi](#-lisensi)

---

## 🎯 Tentang Proyek

WordUp Dictionary App lahir dari kebutuhan akan platform digital yang memudahkan pengguna mempelajari vocabulary bahasa Inggris. Aplikasi ini **bukan hanya menjadi kamus biasa**, tapi juga alat bantu bagi siapa saja yang ingin belajar bahasa Inggris dengan cara yang menyenangkan dan personal.

### Masalah yang Diselesaikan
- **Sulitnya mencari definisi kata yang akurat dan lengkap**
- **Kurangnya motivasi dalam belajar vocabulary**
- **Keterbatasan fitur tracking progress belajar**
- **Minimnya gamifikasi dalam aplikasi pembelajaran**

### Solusi
Aplikasi ini menyediakan:
- **Akses ke ribuan definisi kata melalui WordsAPI**
- **Sistem quiz interaktif dengan 10 pertanyaan per sesi**
- **Achievement system untuk meningkatkan motivasi**
- **Progress tracking berbasis level**
- **Dark mode untuk kenyamanan mata**
- **Penyimpanan kata favorit secara offline**

---

## ✨ Fitur Utama

| 🔧 **Fitur** | 📝 **Deskripsi** |
|--------------|------------------|
| 🔐 **Sistem Autentikasi** | Registrasi dan login dengan validasi lengkap menggunakan SQLite |
| 🔍 **Dictionary Search** | Pencarian definisi kata menggunakan WordsAPI dengan fallback offline |
| 💾 **Simpan Favorit** | Menyimpan kata-kata favorit secara lokal untuk akses offline |
| 🧠 **Quiz Interaktif** | 10 pertanyaan per quiz dengan feedback real-time dan progress bar |
| 🌙 **Dark Mode** | Toggle tema gelap/terang dengan SharedPreferences |
| 🏆 **Achievement System** | Sistem pencapaian untuk memotivasi pembelajaran |
| 📊 **Progress Tracking** | Tracking level berdasarkan jawaban benar (setiap 10 jawaban = 1 level) |
| 🎯 **Navigation Component** | Navigasi antar fragment dengan Bottom Navigation |

---

## 📱 Tampilan Aplikasi

### 🔐 Authentication Flow
- **Splash Screen** dengan session check otomatis
- **Login Screen** dengan remember me functionality
- **Register Screen** dengan validasi email unik

### 🏠 Main Dashboard
- **Home Fragment** untuk pencarian kata dan menampilkan definisi
- **Favorite Fragment** untuk kata-kata yang disimpan
- **Profile Section** dengan informasi user progress

### 🧠 Quiz System
- **Quiz Activity** dengan 10 pertanyaan acak
- **Real-time scoring** dan progress indicator
- **Achievement popup** setelah menyelesaikan milestone

---

## 🛠️ Implementasi Teknis

Aplikasi ini dikembangkan dengan pendekatan **modular dan clean architecture** sederhana menggunakan Java sebagai bahasa utama dan **Android Studio** sebagai IDE. Berikut adalah beberapa komponen penting yang digunakan:

### 🏗️ **Architecture & Components**

#### **Intent & Activity**
- Digunakan untuk berpindah antar halaman (MainActivity → DashboardActivity)
- Mengirimkan data resep menggunakan `Intent.putExtra()`

#### **Fragment & Navigation Component**
- Terdapat 3 fragment: HomeFragment, FavoriteFragment, ProfileFragment
- Diintegrasikan dengan BottomNavigationView untuk navigasi yang smooth

#### **RecyclerView**
- Digunakan untuk menampilkan daftar definisi kata secara dinamis
- Adapter dirancang untuk binding data dari API response

#### **Background Thread**
- Semua operasi jaringan dan pencarian dilakukan di thread background
- Mencegah UI freeze selama proses loading

### 🌐 **Networking**
- Menggunakan **Retrofit** untuk API calls ke WordsAPI
- **Endpoint**: `https://wordsapiv1.p.rapidapi.com/words/`
- **Error handling** lengkap untuk koneksi bermasalah
- **Tombol refresh** jika koneksi bermasalah

### 💾 **Local Data Persistence**
- **SQLite Database** digunakan untuk menyimpan data user dan autentikasi
- **SharedPreferences** digunakan untuk menyimpan preferensi tema dan progress user

---

## 🔧 Teknologi yang Digunakan

| **Teknologi** | **Fungsi** |
|---------------|------------|
| **Java** | Bahasa pemrograman utama |
| **Android Studio** | Lingkungan pengembangan |
| **Retrofit** | Untuk request API |
| **SQLite** | Menyimpan data user dan autentikasi |
| **SharedPreferences** | Menyimpan preferensi user terhadap tema dan progress |
| **Material Design** | UI/UX components |
| **Navigation Component** | Navigasi antar-fragment |
| **WordsAPI** | Sumber data definisi kata |

---

## 🚀 Cara Memulai

### **Prasyarat**
- Android Studio (versi terbaru)
- JDK 8 atau lebih tinggi
- Perangkat fisik atau emulator Android
- Koneksi internet (opsional untuk mode offline)
- API Key dari WordsAPI (untuk fitur pencarian online)

### **Instalasi**

```bash
git clone https://github.com/username/WordUp-Dictionary-App.git
```

1. **Buka project di Android Studio**
2. **Sync Gradle** untuk mengunduh dependencies
3. **Konfigurasi API Key** di file `NetworkUtils.java` jika diperlukan
4. **Jalankan aplikasi** pada emulator atau device

### **Konfigurasi**
- Jika menggunakan API key, pastikan sudah dimasukkan di `DictionaryApiClient.java`
- Sesuaikan endpoint API sesuai kebutuhan di `WordsApiService.java`

---

## 📋 Struktur Project

```
/app/src/main/
├── java/com/example/project_pendidikan/
│   ├── activities/
│   │   ├── MainActivity.java              # Login screen
│   │   ├── RegisterActivity.java          # Registration
│   │   ├── DashboardActivity.java         # Main dashboard
│   │   ├── QuizActivity.java             # Quiz interface
│   │   ├── AchievementsActivity.java     # Achievements
│   │   ├── ProfileActivity.java          # User profile
│   │   ├── SplashActivity.java           # Splash screen
│   │   └── FavoriteWordsActivity.java    # Favorite words
│   ├── model/
│   │   ├── User.java                     # User data model
│   │   ├── UserProgress.java             # Progress tracking
│   │   ├── QuizQuestion.java             # Quiz question model
│   │   └── WordResponse.java             # API response model
│   ├── api/
│   │   ├── DictionaryApiClient.java      # API client setup
│   │   └── WordsApiService.java          # API service interface
│   └── db/
│       └── DatabaseHelper.java           # SQLite database helper
│
└── res/
    ├── layout/                           # XML layouts
    ├── values/                           # Colors, themes, styles
    ├── drawable/                         # Icons and graphics
    ├── font/                            # Poppins font family
    └── menu/                            # Navigation menus
```

---

## 🎮 Cara Menggunakan

1. **Registrasi/Login** - Buat akun baru atau login dengan akun existing
2. **Cari Kata** - Gunakan search bar di home fragment untuk mencari definisi
3. **Simpan Favorit** - Tap tombol bintang untuk menyimpan kata ke favorit
4. **Mulai Quiz** - Akses quiz melalui menu untuk menguji vocabulary
5. **Lihat Progress** - Cek level dan achievement di profile section
6. **Toggle Dark Mode** - Gunakan icon tema di toolbar untuk mengubah tema

---

## 🏆 Achievement System

- **"First Steps"**: Menyelesaikan quiz pertama
- **"Word Master"**: Mendapatkan 10 jawaban benar berturut-turut  
- **"Vocabulary Expert"**: Mencapai Level 5
- **Level System**: Setiap 10 jawaban benar = naik 1 level

---

## 🔐 Keamanan & Privasi

- **Password encryption** menggunakan hash untuk keamanan data
- **Input validation** untuk mencegah SQL injection
- **Session management** dengan SharedPreferences
- **Offline capability** untuk data favorit

---


## 📄 Lisensi

Dilisensikan di bawah **MIT License** - lihat file `LICENSE` untuk detail lebih lanjut.
| Oleh: Ahmad Rafly Putra Hasrun
Email: ahmd.rfly19@gmail.com Instagram: @aarflyy
---

## 👨‍💻 Developer

Dikembangkan dengan ❤️ untuk pembelajaran bahasa Inggris yang lebih interaktif dan menyenangkan.

**WordUp Dictionary App** - Making English Learning Fun and Interactive! 🚀📚
