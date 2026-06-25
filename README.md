Markdown# Rainafa Coffee - Modern POS System ☕

Aplikasi **Point of Sale (POS) / Sistem Kasir Modern** berbasis desktop yang dirancang khusus untuk manajemen operasional Rainafa Coffee. Aplikasi ini dibangun menggunakan **Java Swing (GUI)** dengan menerapkan berbagai struktur data fundamental secara mandiri dan pola desain (*design pattern*) arsitektur yang efisien untuk menjamin performa yang cepat dan manajemen data yang aman.

---

##  Fitur Utama

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

1. Object-Oriented Programming (OOP)OOP adalah fondasi utama program ini. Kita membungkus data dan fungsi ke dalam objek nyata.Contoh Kode (Model Pesanan.java)Javapackage model;

public class Pesanan {
    // 1. ENCAPSULATION: Menyembunyikan data dengan akses private
    private String id;
    private String rincianMenu;
    private double totalHarga;
    private String status;

    // Constructor untuk inisialisasi objek
    public Pesanan(String id, String rincianMenu, double totalHarga, String status) {
        this.id = id;
        this.rincianMenu = rincianMenu;
        this.totalHarga = totalHarga;
        this.status = status;
    }

    // Getter dan Setter untuk mengakses data secara aman
    public String getId() { return id; }
    public String getRincianMenu() { return rincianMenu; }
    public double getTotalHarga() { return totalHarga; }
    public String getStatus() { return status; }
}
Cara Membuktikannya ke Dosen: Buka file di dalam folder model. Tunjukkan bahwa data transaksi tidak dibbiarkan telanjang (tidak menggunakan variabel biasa di Main), melainkan dibungkus menjadi sebuah objek Pesanan.2. Array / ArrayListArray digunakan untuk data yang ukurannya statis (pasti), sedangkan ArrayList digunakan untuk data dinamis yang bisa bertambah terus seiring adanya transaksi baru.Contoh Kode (Diambil dari RiwayatPanel.java)Java// 1. Array Primitif (Ukurannya Tetap untuk Header Tabel)
String[] kolom = {"ID Transaksi", "Keterangan Pesanan", "Metode", "Total Pendapatan", "Status"};

// 2. ArrayList (Ukurannya Dinamis untuk Menampung Banyak Objek)
List<Pesanan> listPesanan = new ArrayList<>();

// Mengisi data (Contoh simulasi)
listPesanan.add(new Pesanan("TRX001", "1x Rainafa Coffee", 15000, "LUNAS"));
listPesanan.add(new Pesanan("TRX002", "1x Matcha", 18000, "LUNAS"));

// Looping mundur untuk menampilkan data terbaru di baris paling atas
for (int i = listPesanan.size() - 1; i >= 0; i--) {
    Pesanan p = listPesanan.get(i);
    // Masukkan ke JTable...
}
Cara Membuktikannya ke Dosen: Buka file RiwayatPanel.java, tunjukkan baris List<Pesanan> listPesanan = new ArrayList<>(...). Jelaskan bahwa baris ini membuktikan data di dalam tabel diambil secara dinamis dari ArrayList.3. Queue (Antrian FIFO)Queue digunakan untuk mengelola antrian pelanggan di kasir. Siapa yang memesan duluan, dia yang harus dilayani dan membayar duluan.Contoh Kode (Simulasi di DataManager.java atau AntrianSistem.java)Javaimport java.util.LinkedList;
import java.util.Queue;

public class AntrianSistem {
    // Menggunakan Jelas Queue dengan implementasi LinkedList
    private Queue<Pesanan> antrianMencuci = new LinkedList<>();

    // Tambah Antrian Baru (Enqueue) saat kasir klik pesan
    public void tambahPesananKeAntrian(Pesanan pesananBaru) {
        antrianMencuci.add(pesananBaru); 
    }

    // Ambil dan hapus antrian paling depan (Dequeue) setelah selesai bayar
    public Pesanan prosesPembayaranSelesai() {
        return antrianMencuci.poll(); // Mengambil antrian pertama (FIFO)
    }
}
Cara Membuktikannya ke Dosen: Jelaskan bahwa di balik layar menu Antrian, data disimpan dalam bentuk Queue. Fungsi .add() digunakan untuk memasukkan pelanggan ke antrian belakang, dan .poll() digunakan untuk menghapus antrian depan yang sudah lunas.4. Hash / Map (HashMap)HashMap digunakan agar pencarian menu makanan/minuman berjalan instan tanpa perlu melakukan looping satu per satu yang bikin aplikasi lemot.Contoh Kode (Pencarian Menu Berdasarkan Kode)Javaimport java.util.HashMap;

public class ManajemenMenu {
    // Key: String (Kode Menu), Value: Objek Menu
    private HashMap<String, Double> mapHargaMenu = new HashMap<>();

    public void inisialisasiMenu() {
        // Mengisi data master ke dalam Map
        mapHargaMenu.put("RNF", 15000.0); // Rainafa Coffee
        mapHargaMenu.put("MTC", 18000.0); // Matcha
        mapHargaMenu.put("CKL", 22000.0); // Cokelat
    }

    // Pencarian Instan O(1) tanpa looping
    public double dapatkanHarga(String kodeMenu) {
        return mapHargaMenu.get(kodeMenu); // Langsung dapet harganya
    }
}
Cara Membuktikannya ke Dosen: Katakan bahwa saat Kasir mengetik kode produk atau memilih item, sistem menggunakan .get(kodeMenu) dari HashMap. Kompleksitas waktunya adalah $O(1)$, jauh lebih cepat dibanding mencari di ArrayList yang harus memakai perulangan ($O(n)$).5. Stack (LIFO)Stack digunakan untuk merekam jejak halaman yang dibuka user. Konsepnya Last-In-First-Out (LIFO). Halaman terakhir yang dibuka adalah yang pertama kali muncul saat tombol Back ditekan.Contoh Kode (Fitur Navigasi Undo / Back)Javaimport java.util.Stack;

public class NavigasiSistem {
    private Stack<String> riwayatHalaman = new Stack<>();

    // Setiap kali user pindah halaman, push (simpan) ke Stack
    public void pindahHalaman(String namaHalamanBaru) {
        riwayatHalaman.push(namaHalamanBaru);
    }

    // Saat tombol "KEMBALI / UNDO" diklik, pop (ambil yang terakhir)
    public String tombolBackDiklik() {
        if (riwayatHalaman.size() > 1) {
            riwayatHalaman.pop(); // Buang halaman saat ini
            return riwayatHalaman.peek(); // Lihat halaman sebelumnya
        }
        return "Dashboard"; // Default jika stack habis
    }
}
Cara Membuktikannya ke Dosen: Jelaskan bahwa ini diusulkan untuk mekanisme tombol kembali pintar. Sifat Stack yang menumpuk data ke atas sangat pas untuk merekam hierarki perpindahan panel UI.6. TreeTree (Pohon) merepresentasikan data bertingkat. Di aplikasi ini, Tree ideal untuk membagi kategori produk secara terstruktur dari akar (root) sampai daun (leaf).Contoh Kode (Struktur Node Kategori Menu bertingkat)Javaimport java.util.ArrayList;
import java.util.List;

class KategoriNode {
    String namaKategori;
    List<KategoriNode> children; // Cabang di bawahnya

    public KategoriNode(String nama) {
        this.namaKategori = nama;
        this.children = new ArrayList<>();
    }

    public void tambahSubKategori(KategoriNode child) {
        this.children.add(child);
    }
}

// Cara menyusunnya secara logis:
// Root: "Menu Rainafa"
//  ├── Cabang 1: "Minuman" ── Sub-Cabang: "Coffee", "Non-Coffee"
//  └── Cabang 2: "Makanan" ── Sub-Cabang: "Snack", "Main Course"
Cara Membuktikannya ke Dosen: Jelaskan bahwa konsep Tree ini diimplementasikan pada visualisasi pengelompokan menu di database atau UI manajemen produk agar kasir bisa menyaring menu berdasarkan sub-kategori secara terstruktur.
