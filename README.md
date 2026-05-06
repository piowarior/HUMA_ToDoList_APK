````
on going
````

---

# **HUMA – Smart Task & Focus Management App** 🧠⏱️

**HUMA** adalah aplikasi Android untuk manajemen tugas dan fokus yang dirancang untuk membantu pengguna **mengatur pekerjaan harian, menjaga konsistensi, dan meningkatkan produktivitas** melalui sistem task yang fleksibel dan fitur fokus yang terstruktur.

Aplikasi ini dibangun dengan **Jetpack Compose** dan arsitektur **MVVM**, sehingga memiliki tampilan modern, performa ringan, dan mudah dikembangkan.

---


## 📥 Download APK

Coba aplikasi **HUMA** secara langsung melalui file APK berikut:

👉 [Download APK HUMA](https://github.com/piowarior/HUMA_ToDoList_APK/releases/download/v1.1/app-debug.apk)

> Pastikan mengaktifkan izin install aplikasi dari sumber luar (Unknown Sources) pada Android.

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

### 📱 Splash
<p align="center">
  <img src="https://github.com/user-attachments/assets/6b13557a-beef-4703-b001-2d7e91ea58f9" width="250">
</p>

### 📱 Dashboard
<p align="center">
  <img src="https://github.com/user-attachments/assets/614b50d4-12d6-489b-a144-e9ae2f23225f" width="250">
  <img src="https://github.com/user-attachments/assets/b281fc04-e549-4311-9277-b194a473952b" width="250">
</p>



### ✅ Task Management (Add & Preview)
<p align="center">
  <img src="https://github.com/user-attachments/assets/9a14aab2-a2dd-4c0f-8ab8-38e5f21a9048" width="250" alt="Add Task">
  <img src="https://github.com/user-attachments/assets/6e85bca3-e8a3-421d-8afd-e23fece60690" width="250" alt="Preview Task">
  <img src="https://github.com/user-attachments/assets/fb69f293-7185-4af4-afbd-649a73711c95" width="250" alt="Task on Dashboard">
</p>


### 🔥 Daily Comitment
<p align="center">
  <img src="https://github.com/user-attachments/assets/82715ce9-a84a-4624-83e9-e838096d0231" width="250">
  <img src="https://github.com/user-attachments/assets/3b3be4e5-8507-461f-acdb-f8bd5eb06011" width="250">
  <img src="https://github.com/user-attachments/assets/4fe6a289-b26d-498f-a1e3-4c2038b06545" width="250">
</p>

### 📊 Feature
<p align="center">
 <img src="https://github.com/user-attachments/assets/61be3cf6-c380-49b7-99b3-836b2f5b8a9d" width="250">
 <img src="https://github.com/user-attachments/assets/ca39afe5-14dc-4f18-9e1c-77928430b87c" width="250">
 <img src="https://github.com/user-attachments/assets/d07c35f1-f56e-4346-9f5e-07bdb584b5c3" width="250">
</p>


### 🎯 Focus Mode
<p align="center">
  <img src="https://github.com/user-attachments/assets/ec5950c7-8c29-42ad-8882-506246033c8c" width="250">
</p>

### 🔥 Streak
<p align="center">
  <img src="https://github.com/user-attachments/assets/06846a2a-b691-4d64-8328-00f4847b17c7" width="250">
  <img src="https://github.com/user-attachments/assets/a95d5a83-c738-4d37-a5e5-5c751346f8d7" width="250">
</p>

### ⚖️ Life Balance
<p align="center">

  <img src="https://github.com/user-attachments/assets/fe0c0a14-9280-46ae-8837-1bd5dcf659c6" width="250">
</p>

### 📊 Stats
<p align="center">

  <img src="https://github.com/user-attachments/assets/620736eb-a6f5-4a23-ba51-b1da20b2a741" width="250">
  <img src="https://github.com/user-attachments/assets/ff8cb0a0-9fe6-40a1-92ce-b3affa04c31e" width="250">
</p>

### 📝 Quick Notes
<p align="center">
  <img src="https://github.com/user-attachments/assets/a5d1c79b-e2c5-4989-b487-933eb3dfeb8a" width="250">
  <img src="https://github.com/user-attachments/assets/4fc9f534-2673-4e83-9436-5648c57716fe" width="250">
</p>

### 🗄️ Widget 
<p align="center">
  <img src="https://github.com/user-attachments/assets/b5b56e55-a715-413b-b1c5-e38e57bae65c" width="250">
</p>

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

### 🎮
<p align="center">
  <img width="200px" src="https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExaWNsOWo3N3RpbHJ0cTl3cjE1NHg2ajhsbjlvamcwb29veTlwOXJ4aSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/11lxCeKo6cHkJy/giphy.gif">
</p>

