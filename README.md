Markdown# Rainafa Coffee - Modern POS System ☕

Aplikasi **Point of Sale (POS) / Sistem Kasir Modern** berbasis desktop yang dirancang khusus untuk manajemen operasional Rainafa Coffee. Aplikasi ini dibangun menggunakan **Java Swing (GUI)** dengan menerapkan berbagai struktur data fundamental secara mandiri dan pola desain (*design pattern*) arsitektur yang efisien untuk menjamin performa yang cepat dan manajemen data yang aman.

---

## 🚀 Fitur Utama

Aplikasi ini dilengkapi dengan fitur manajemen kafe ujung-ke-ujung (*end-to-end*):

* **Sistem Otentikasi & Multi-Role**: Pembatasan hak akses berbasis peran kerja.
    * **Admin**: Akses penuh ke Dashboard finansial, Manajemen Menu (CRUD), Riwayat Transaksi global, dan Manajemen Akun Karyawan.
    * **Kasir**: Akses terbatas ke Katalog Menu, Kasir POS, dan Antrian Pembayaran Pembeli.
* **Modul Kasir POS Interaktif**: Keranjang belanja dinamis, kalkulator uang kembalian otomatis, pembuat nomor nota urut otomatis (`TRX001`), dan filter produk berdasarkan kategori.
* **Sistem Antrian FIFO (Barista Queue)**: Mengatur urutan pembuatan kopi berdasarkan siapa yang memesan terlebih dahulu menggunakan konsep antrian adil.
* **Manajemen Menu (CRUD) & Unggah Gambar**: Pengelolaan penuh item produk kafe (nama, harga, kategori, dan stok) lengkap dengan fitur potong (*crop/autofit*) gambar otomatis.
* **Integrasi WhatsApp & Upload Bukti QRIS**: Membuka API WhatsApp web secara otomatis untuk mengirimkan nota pelaporan gambar bukti transfer QRIS langsung ke nomor Admin Pusat.
* **Dashboard Analitik Real-time**: Menampilkan total cup terjual, omset pendapatan harian, ringkasan antrian aktif, serta grafik mini peringatan kritis untuk stok produk terendah.
* **Penyimpanan Data Lokal (Flat Files Persistence)**: Otomatis melakukan sinkronisasi data dari memori RAM ke file teks lokal (`.txt`) setiap ada perubahan transaksi tanpa memerlukan database eksternal.

---

## 🛠️ Implementasi Struktur Data & Arsitektur Kode

Proyek ini sangat ideal untuk pembelajaran karena mengimplementasikan struktur data kustom dan *Design Pattern* terstruktur:

1.  **Singleton Pattern (`DataManager.java`)**: Menjamin hanya ada satu gerbang kendali utama akses data di dalam aplikasi untuk menghindari tabrakan data (*data race*).
2.  **Queue (FIFO) (`PesananQueue.java`)**: Menggunakan struktur data antrian antarmuka Barista untuk memproses pesanan masuk secara adil (*First-In, First-Out*).
3.  **HashMap & LinkedHashMap (`MenuHashMap.java`, `AkunHashMap.java`)**: Pencarian data produk dan validasi login akun instan dengan kompleksitas waktu $O(1)$. `LinkedHashMap` digunakan pada menu agar urutan layout GUI tetap konsisten.
4.  **Custom Singly Linked List (`PesananLinkedList.java`)**: Implementasi struktur data berantai secara mandiri tanpa library bawaan untuk mencatat log riwayat seluruh transaksi secara dinamis di memori.

---

## 📁 Struktur Direktori Proyek

```text
src/
├── Main.java                        # Entry point utama aplikasi
├── model/                           # Paket Objek Data (POJO)
│   ├── Akun.java
│   ├── Menu.java
│   └── Pesanan.java
├── struktur/                        # Paket Logika Struktur Data & Storage
│   ├── DataManager.java             # Singleton Data Access Controller
│   ├── AkunHashMap.java
│   ├── MenuHashMap.java
│   ├── PesananLinkedList.java       # Custom Singly Linked List
│   └── PesananQueue.java            # FIFO Barista Queue
└── ui/                              # Paket Desain Antarmuka (GUI)
    ├── MainFrame.java               # Jendela utama dengan CardLayout
    ├── Theme.java                   # Palet warna Flat UI & Pabrik Komponen
    ├── LoginPanel.java
    ├── DashboardPanel.java
    ├── KasirPanel.java
    ├── AntrianPanel.java
    ├── ManajemenMenuPanel.java
    └── RiwayatPanel.java
🔧 Persiapan & Cara MenjalankanPrasyaratJava Development Kit (JDK) versi 8 atau yang lebih baru.IDE pilihan Anda (VS Code, IntelliJ IDEA, NetBeans, atau Eclipse).Langkah InstalasiClone RepositoriBashgit clone [https://github.com/username-anda/rainafa-coffee-pos.git](https://github.com/username-anda/rainafa-coffee-pos.git)
cd rainafa-coffee-pos
Buat Folder Aset GambarAplikasi membutuhkan folder direktori penyimpanan lokal untuk menaruh file foto menu dan bukti transfer. Buat dua folder baru di direktori utama proyek Anda (sejajar dengan folder src):Bashmkdir images
mkdir proofs
Tempatkan logo kafe Anda di folder images/logo.jpg atau images/logo.png.Kompilasi dan JalankanBuka file src/Main.java di IDE Anda lalu tekan Run, atau eksekusi perintah terminal berikut:Bashjavac -d bin src/Main.java src/model/*.java src/struktur/*.java src/ui/*.java
java -cp bin Main
🔐 Kredensial Akun Default (Uji Coba)Saat pertama kali dijalankan, sistem otomatis membuat file teks akun default. Anda dapat masuk menggunakan akun berikut:UsernamePasswordOtoritas (Role)Admin123Akses Penuh (Atasan)Kasir111Akses Terbatas (Pegawai Toko)Anda juga dapat mendaftarkan akun baru langsung lewat tombol "Daftar Akun Baru" di halaman Login.
