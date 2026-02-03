

---

# **HUMA – Smart Task & Focus Management App** 🧠⏱️

**HUMA** adalah aplikasi Android untuk manajemen tugas dan fokus yang dirancang untuk membantu pengguna **mengatur pekerjaan harian, menjaga konsistensi, dan meningkatkan produktivitas** melalui sistem task yang fleksibel dan fitur fokus yang terstruktur.

Aplikasi ini dibangun dengan **Jetpack Compose** dan arsitektur **MVVM**, sehingga memiliki tampilan modern, performa ringan, dan mudah dikembangkan.

---

## 🎯 Konsep Utama Aplikasi

HUMA berfokus pada **3 hal utama**:

1. **Task Management yang fleksibel**
2. **Fokus & konsistensi (Focus + Streak)**
3. **Quick Access tanpa distraksi**

---



## ⚡ Quick Access Menu

Quick Access adalah menu utama yang bisa diakses dengan cepat dari dashboard.

### 🎯 **Focus Mode**

Mode fokus berbasis timer untuk membantu pengguna bekerja tanpa distraksi.

* Fokus ke satu task tertentu
* Timer start / pause / resume
* Tampilan full-screen minimalis
* Notifikasi berjalan (Foreground Service)
* Peringatan saat keluar dari mode fokus

---

### 🔥 **Streak**

Menunjukkan **konsistensi fokus harian** pengguna.

* Menghitung hari fokus berturut-turut
* Streak akan bertambah jika fokus tercapai
* Membantu membangun kebiasaan produktif

---

### ⚖️ **Life Balance**

Ringkasan keseimbangan aktivitas pengguna.

* Distribusi task (harian & upcoming)
* Gambaran beban kerja
* Membantu menghindari overwork

---

### 📊 **Stats**

Statistik produktivitas pengguna.

* Total task selesai
* Riwayat fokus
* Progress harian & mingguan
* Insight sederhana untuk evaluasi diri

---

### 📝 **Quick Notes**

> Catatan cepat yang bisa diakses kapan saja

* Menyimpan ide atau catatan singkat
* Tidak terikat task
* Cocok untuk:

  * Brainstorming
  * Reminder mendadak
  * Thought dump
* Akses instan dari dashboard

---

## ✅ Fitur Utama

### 🗂️ **Task Management**

Fitur inti aplikasi untuk mengatur pekerjaan.

* Tambah, edit, dan hapus task
* Task berdasarkan waktu:

  * **Today Tasks**
  * **Upcoming Tasks**
* Task dengan tanggal (due date)
* Status task:

  * Pending
  * Done
* Konfirmasi sebelum task ditandai selesai
* Detail task:

  * Judul
  * Deskripsi
  * Prioritas
  * Tanggal

---

### 📅 **Upcoming Task**

Mengatur task masa depan dengan rapi.

* Menentukan tanggal task
* Preview task yang akan datang
* Membantu perencanaan jangka pendek

---

### 🔁 **Daily Commitment**

> Komitmen harian yang ditentukan oleh pengguna

Berbeda dengan streak otomatis, **Daily Commitment** bersifat **custom**.

* Pengguna menentukan target harian sendiri
* Contoh:

  * Fokus 2 jam
  * Menyelesaikan 3 task
* Progress ditampilkan secara visual
* Lebih fleksibel & personal dibanding streak biasa

---

## 🛠️ Tools & Technologies

Berikut adalah teknologi dan tools yang digunakan dalam pengembangan aplikasi **HUMA**:

### 📱 Android Development
- **Kotlin** — Bahasa utama pengembangan aplikasi
- **Jetpack Compose** — UI modern berbasis declarative
- **Material Design 3** — Sistem desain UI

### 🧱 Architecture
- **MVVM (Model–View–ViewModel)** — Arsitektur aplikasi
- **ViewModel** — Manajemen state UI
- **StateFlow & Flow** — Reactive state management

### 🗄️ Data & Storage
- **Room Database** — Penyimpanan data lokal
- **DAO Pattern** — Akses data terstruktur

### 🧭 Navigation
- **Navigation Compose** — Manajemen navigasi antar screen

### 🔔 System & Background
- **Foreground Service** — Focus Mode Notification
- **Broadcast Receiver** — Aksi Pause / Resume dari notifikasi
- **Notification Channel** — Support Android 8+

### 🧪 Development Tools
- **Android Studio**
- **Gradle (Kotlin DSL)**
- **Git & GitHub**

### 🎨 UI / UX
- **Figma** — Desain UI/UX
- **Custom Theme & Typography**

---


## 🖼️ Preview Aplikasi

> 📌 Tambahkan screenshot / video di folder `docs/`

### 📱 Dashboard

![Dashboard Preview](docs/images/dashboard.png)

### 🎯 Focus Mode

![Focus Mode Preview](docs/images/focus_mode.png)

### 📝 Quick Notes

![Notes Preview](docs/images/quick_notes.png)

---

## 🛠️ Teknologi yang Digunakan

* **Kotlin**
* **Jetpack Compose**
* **Material 3**
* **MVVM Architecture**
* **Room Database**
* **ViewModel & StateFlow**
* **Navigation Compose**
* **Foreground Service (Focus Notification)**

---

## 📂 Struktur Proyek (Ringkas)

```
com.huma.app
│
├── data
│   ├── local
│   └── repository
│
├── ui
│   ├── screen
│   ├── notification
│   └── viewmodel
│
└── MainActivity.kt
```

---

## 🚀 Rencana Pengembangan

* Visual statistik yang lebih detail
* Tema gelap & terang
* Sinkronisasi cloud
* Export data task
* Reminder otomatis

---

## 👨‍💻 Developer

Dikembangkan oleh **Muhamad Rohisul Iman**
Mahasiswa Informatika dengan fokus pada **Android Development**, **UI/UX**, dan **Productivity Apps**.

---
