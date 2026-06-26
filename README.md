# Rainafa Coffee POS

Implementasi konsep **Object-Oriented Programming (OOP)** dan berbagai **Struktur Data** yang digunakan dalam pengembangan aplikasi *Point of Sale (POS) Modern Rainafa Coffee*. 

---

## Daftar Isi
1. [Object-Oriented Programming (OOP)](#1-object-oriented-programming-oop)
2. [Array / ArrayList](#2-array--arraylist)
3. [Queue (Antrian FIFO)](#3-queue-antrian-fifo)
4. [Hash / Map (HashMap)](#4-hash--map-hashmap)
5. [Linked List (Kustom)](#5-linked-list-kustom)
6. [Tree (Hierarki Kategori)](#6-tree-hierarki-kategori)

---

## 1. Object-Oriented Programming (OOP)

### Deskripsi & Penerapan
Pendekatan OOP digunakan untuk fondasi utama program ini. Kita membungkus data dan fungsi ke dalam objek nyata. Semua kelas model (`Pesanan`, `Menu`, `Akun`) menerapkan **Encapsulation** dengan atribut `private` dan akses melalui getter/setter. Kelas `Pesanan` bahkan menerapkan **Constructor Overloading** untuk membedakan pembuatan pesanan baru dengan pemuatan ulang data dari file.

### (model/Pesanan.java)
```java
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Pesanan {
    // ENCAPSULATION: Menyembunyikan data dengan akses private
    private String id;
    private String namaPelanggan;
    private String rincianMenu;
    private long totalHarga;
    private int totalCup;
    private String metodePembayaran;
    private long uangTunai; // Jumlah uang tunai yang diberikan oleh pelanggan
    private LocalDateTime waktu;
    private String status;

    // Constructor utama untuk membuat pesanan baru
    // Status otomatis diset "BELUM BAYAR" dan waktu diset ke saat ini
    public Pesanan(String id, String namaPelanggan, String rincianMenu, long totalHarga,
                   int totalCup, String metodePembayaran, long uangTunai) {
        this.id = id;
        this.namaPelanggan = namaPelanggan;
        this.rincianMenu = rincianMenu;
        this.totalHarga = totalHarga;
        this.totalCup = totalCup;
        this.metodePembayaran = metodePembayaran;
        this.uangTunai = uangTunai;
        this.waktu = LocalDateTime.now(); // Mengambil waktu lokal saat object dibuat
        this.status = "BELUM BAYAR";
    }

    // Constructor Overloading: Memuat ulang data pesanan yang sudah ada (misal dari file .txt)
    public Pesanan(String id, String namaPelanggan, String rincianMenu, long totalHarga,
                   int totalCup, String metodePembayaran, long uangTunai,
                   String status, LocalDateTime waktu) {
        this(id, namaPelanggan, rincianMenu, totalHarga, totalCup, metodePembayaran, uangTunai);
        this.status = status;
        this.waktu = waktu;
    }

    // Getter Methods
    public String getId() { return id; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public String getRincianMenu() { return rincianMenu; }
    public long getTotalHarga() { return totalHarga; }
    public int getTotalCup() { return totalCup; }
    public String getMetodePembayaran() { return metodePembayaran; }
    public long getUangTunai() { return uangTunai; }
    public String getStatus() { return status; }
    public LocalDateTime getWaktu() { return waktu; }

    // Menghitung kembalian secara otomatis
    public long getKembalian() {
        return uangTunai - totalHarga;
    }

    // Setter
    public void setStatus(String status) { this.status = status; }

    // Memformat LocalDateTime menjadi String dd/MM/yyyy
    public String getWaktuFormatted() {
        return waktu.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
```

---

## 2. Array / ArrayList

### Deskripsi & Penerapan

* **Array Primitif:** Digunakan untuk menyimpan data statis yang ukurannya sudah pasti sejak awal program berjalan, seperti nama-nama kolom pada tabel.
* **ArrayList:** Digunakan untuk menampung koleksi objek secara dinamis. Isi `Queue` antrian disalin ke `ArrayList` agar dapat diakses menggunakan perulangan indeks mundur, sehingga transaksi paling baru muncul di baris teratas tabel.

### (ui/RiwayatPanel.java)

```java
// 1. Array Primitif untuk mendefinisikan layout kolom tabel yang tetap (tidak berubah)
String[] kolom = {"ID Transaksi", "Keterangan Pesanan", "Metode", "Total Pendapatan", "Status"};

modelTabel = new DefaultTableModel(kolom, 0) {
    @Override
    public boolean isCellEditable(int r, int c) { return false; } // Mengunci sel agar tidak bisa diedit user
};

// 2. ArrayList untuk menampung salinan data transaksi dari Queue secara dinamis
List<Pesanan> listPesanan = new ArrayList<>(DataManager.getInstance().getQueue().getAntrian());

// Perulangan mundur (Looping dari indeks terakhir ke nol)
// Berfungsi agar transaksi paling baru (paling bawah di list) muncul di baris teratas JTable
for (int i = listPesanan.size() - 1; i >= 0; i--) {
    Pesanan p = listPesanan.get(i);

    // Meratakan karakter baris baru rincian menu menjadi satu kalimat dipisah koma
    String rincianLurus = p.getRincianMenu().replace("\n", ", ");

    modelTabel.addRow(new Object[]{
        p.getId(),
        rincianLurus,
        p.getMetodePembayaran(),
        "Rp " + String.format("%,.0f", (double) p.getTotalHarga()),
        p.getStatus()
    });
}
```

---

## 3. Queue (Antrian FIFO)

### Deskripsi & Penerapan

Mekanisme antrian pesanan pelanggan di kasir menggunakan prinsip **FIFO (First-In, First-Out)**. Pelanggan yang datang dan memesan terlebih dahulu akan masuk ke antrian paling depan untuk diproses pembayarannya dan diproduksi oleh barista terlebih dahulu. Selain operasi `offer` (Enqueue) dan `poll` (Dequeue), antrian ini juga dilengkapi fitur **pembatalan pesanan** menggunakan pencarian linier dan `removeIf`.

### (struktur/PesananQueue.java)

```java
package struktur;

import model.Pesanan;
import java.util.LinkedList;
import java.util.Queue;

/**
 * STRUKTUR DATA: QUEUE (FIFO - First In First Out)
 * Digunakan khusus untuk mengelola urutan pengerjaan produk oleh Barista di dapur.
 */
public class PesananQueue {
    // Memanfaatkan LinkedList bawaan Java untuk implementasi struktur antrian (Queue)
    private Queue<Pesanan> antrianBarista;

    public PesananQueue() {
        this.antrianBarista = new LinkedList<>();
    }

    // Operasi Enqueue: Memasukkan pesanan baru ke baris paling belakang antrian
    public boolean tambahKeAntrian(Pesanan pesanan) {
        return antrianBarista.offer(pesanan);
    }

    // Operasi Dequeue: Mengambil pesanan paling depan untuk segera diproses barista
    // Mengembalikan null jika antrian sedang kosong
    public Pesanan ambilPesananBerikutnya() {
        return antrianBarista.poll();
    }

    // Pembatalan Pesanan: Mencari pesanan berdasarkan ID, ubah status, lalu hapus dari antrian
    public void batalkanPesanan(String pesananId) {
        for (Pesanan p : antrianBarista) {
            if (p.getId().equals(pesananId)) {
                p.setStatus("BATAL");
                break;
            }
        }
        // Menghapus elemen dari antrian secara kondisional berdasarkan ID Pesanan
        antrianBarista.removeIf(p -> p.getId().equals(pesananId));
    }

    public Queue<Pesanan> getAntrian() { return antrianBarista; }
    public int getJumlahAntrian() { return antrianBarista.size(); }
}
```

---

## 4. Hash / Map (HashMap)

### Deskripsi & Penerapan

Program ini menggunakan **dua varian HashMap** untuk kebutuhan berbeda:

* **`MenuHashMap`** menggunakan `LinkedHashMap` — menyimpan objek `Menu` lengkap dengan urutan tampilan yang terjaga konsisten di GUI. Key = Kode Menu (misal: `M01`), Value = Objek Menu.
* **`AkunHashMap`** menggunakan `HashMap` standar — digunakan untuk autentikasi login dengan kompleksitas waktu **O(1)**. Key = Username, Value = Objek Akun.

Dengan menggunakan HashMap, pencarian data menu maupun validasi login tetap **instan dan stabil** meskipun jumlah data terus bertambah.

### (struktur/MenuHashMap.java)

```java
package struktur;

import model.Menu;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * STRUKTUR DATA: HASHMAP (Urutan Terpelihara)
 * Key = Kode Menu (misal: M01), Value = Objek Detail Menu
 * Pencarian data dengan performa O(1), ideal untuk proses scan/klik item kasir.
 */
public class MenuHashMap {
    private Map<String, Menu> menuMap;

    public MenuHashMap() {
        // LinkedHashMap dipilih agar urutan tampilan menu di GUI tetap konsisten
        this.menuMap = new LinkedHashMap<>();
    }

    public void tambahMenu(Menu menu) { menuMap.put(menu.getKode(), menu); }

    // Pencarian instan O(1): langsung menuju Key tanpa perulangan (looping)
    public Menu getMenu(String kode) { return menuMap.get(kode); }

    public boolean hapusMenu(String kode) { return menuMap.remove(kode) != null; }
    public Collection<Menu> getSemuaMenu() { return menuMap.values(); }
    public boolean isMenuValid(String kode) { return menuMap.containsKey(kode); }
    public Map<String, Menu> getMenuMap() { return menuMap; }

    // Memperbarui harga produk tertentu di dalam map
    public boolean updateHarga(String kode, long h) {
        Menu m = menuMap.get(kode);
        if (m != null) { m.setHarga(h); return true; }
        return false;
    }

    // Memperbarui nama item produk tertentu di dalam map
    public boolean updateNama(String kode, String n) {
        Menu m = menuMap.get(kode);
        if (m != null) { m.setNama(n); return true; }
        return false;
    }
}
```

### (struktur/AkunHashMap.java)

```java
package struktur;

import model.Akun;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * STRUKTUR DATA: HASHMAP (Key-Value)
 * Key = Username (String), Value = Objek Akun
 * Digunakan untuk autentikasi login dengan kompleksitas waktu O(1).
 */
public class AkunHashMap {
    private Map<String, Akun> akunMap;

    public AkunHashMap() {
        this.akunMap = new HashMap<>();
    }

    public void tambahAkun(Akun akun) { akunMap.put(akun.getUsername(), akun); }
    public Akun getAkun(String username) { return akunMap.get(username); }
    public boolean hapusAkun(String username) { return akunMap.remove(username) != null; }
    public Collection<Akun> getSemuaAkun() { return akunMap.values(); }

    // Memvalidasi kombinasi username dan password saat login
    // Menjaga null-safety untuk menghindari NullPointerException jika user tidak terdaftar
    public boolean validasiLogin(String username, String password) {
        Akun a = getAkun(username);
        return a != null && a.getPassword().equals(password);
    }
}
```

---

## 5. Linked List (Kustom)

### Deskripsi & Penerapan

Program ini mengimplementasikan **Singly Linked List secara mandiri** (bukan dari library Java) untuk mencatat seluruh log riwayat transaksi secara dinamis. Setiap `Node` menyimpan objek `Pesanan` dan pointer referensi ke node berikutnya. Struktur ini mendukung operasi **append**, **hapus** (dengan bypass pointer), dan **pencarian linier** berdasarkan ID maupun nama pelanggan.

> **Catatan:** Pada dokumentasi awal, bagian ini diposisikan sebagai *Stack (LIFO)* untuk navigasi halaman. Setelah ditelusuri kode yang sebenarnya berjalan, struktur data inti kelima yang diimplementasikan adalah **Linked List kustom** ini — digunakan sebagai backbone penyimpanan riwayat transaksi oleh `DataManager`.

### (struktur/PesananLinkedList.java)

```java
package struktur;

import model.Pesanan;
import java.util.ArrayList;
import java.util.List;

/**
 * STRUKTUR DATA: SINGLY LINKED LIST (Implementasi Mandiri/Kustom)
 * Mencatat seluruh log riwayat transaksi secara dinamis tanpa batas alokasi array statis.
 * Setiap node menyimpan objek data pesanan dan pointer referensi ke node berikutnya.
 */
public class PesananLinkedList {

    // Representasi internal pembungkus elemen data (Simpul/Node)
    private static class Node {
        Pesanan data;
        Node next;

        Node(Pesanan data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head; // Penunjuk elemen pertama di dalam list
    private int size;  // Menyimpan jumlah total simpul aktif saat ini

    public PesananLinkedList() {
        this.head = null;
        this.size = 0;
    }

    // Menambahkan pesanan baru di ujung akhir rantai Linked List (Append)
    public void tambah(Pesanan pesanan) {
        Node newNode = new Node(pesanan);
        if (head == null) {
            head = newNode; // Jika list kosong, node baru langsung menjadi Head
        } else {
            Node current = head;
            // Perulangan geser hingga menemukan simpul ekor terakhir (Tail)
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode; // Menyambungkan pointer ekor ke node baru
        }
        size++;
    }

    // Menghapus simpul berdasarkan ID dengan teknik Bypass Pointer
    public boolean hapus(String pesananId) {
        if (head == null) return false;

        // Kondisi jika data yang mau dihapus berada di simpul utama (Head)
        if (head.data.getId().equals(pesananId)) {
            head = head.next; // Memindahkan Head ke simpul kedua
            size--;
            return true;
        }

        Node current = head;
        while (current.next != null) {
            if (current.next.data.getId().equals(pesananId)) {
                current.next = current.next.next; // Memotong sambungan node target (Bypass pointer)
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // Pencarian linear (Linear Search) berdasarkan ID transaksi spesifik
    public Pesanan cariById(String pesananId) {
        Node current = head;
        while (current != null) {
            if (current.data.getId().equals(pesananId)) return current.data;
            current = current.next;
        }
        return null;
    }

    // Pencarian parsial case-insensitive berdasarkan nama pelanggan
    public List<Pesanan> cariByNama(String nama) {
        List<Pesanan> hasil = new ArrayList<>();
        Node current = head;
        while (current != null) {
            if (current.data.getNamaPelanggan().toLowerCase().contains(nama.toLowerCase())) {
                hasil.add(current.data);
            }
            current = current.next;
        }
        return hasil;
    }

    // Mengonversi Linked List kustom menjadi Java List standar untuk ditampilkan di GUI
    public List<Pesanan> getSemuaPesanan() {
        List<Pesanan> hasil = new ArrayList<>();
        Node current = head;
        while (current != null) {
            hasil.add(current.data);
            current = current.next;
        }
        return hasil;
    }

    public int getSize() { return size; }
}
```

---

## 6. Tree (Hierarki Kategori)

### Deskripsi & Penerapan

Digunakan untuk merepresentasikan struktur pengelompokan kategori produk secara bertingkat. Data diorganisir mulai dari *Root* (Akar) hingga *Leaf* (Daun/Produk Akhir) untuk mempermudah filter produk berdasarkan kategori dan sub-kategori pada sisi manajemen data admin. Pada kode program yang berjalan saat ini, data menu sudah memiliki atribut `kategori` (misal: `"Coffee"`, `"Non-Coffee"`) di kelas `Menu` yang menjadi fondasi logika pengelompokan ini.

### (model/KategoriNode.java) — *Roadmap / Rancangan Implementasi*

```java
import java.util.ArrayList;
import java.util.List;

public class KategoriNode {
    private String namaKategori;
    private List<KategoriNode> subKategori; // Cabang atau anak dari node ini

    public KategoriNode(String namaKategori) {
        this.namaKategori = namaKategori;
        this.subKategori = new ArrayList<>();
    }

    public void tambahAnakNode(KategoriNode anak) {
        this.subKategori.add(anak);
    }
}
```

### Atribut `kategori` pada (model/Menu.java)

```java
public class Menu {
    private String kode;
    private String nama;
    private String kategori; // Contoh nilai: "Coffee", "Non-Coffee"
    private long harga;
    private int stok;

    public Menu(String kode, String nama, String kategori, long harga, int stok) {
        this.kode = kode;
        this.nama = nama;
        this.kategori = kategori;
        this.harga = harga;
        this.stok = stok;
    }

    public String getKategori() { return kategori; }
    // ... getter & setter lainnya
}
```

### Data Menu Default di (struktur/DataManager.java)

```java
// Contoh data awal yang sudah mencerminkan hierarki kategori Tree
menuMap.tambahMenu(new Menu("M01", "Coffee Latte",  "Coffee",     20000, 122));
menuMap.tambahMenu(new Menu("M02", "Matcha Latte",  "Non-Coffee", 25000, 50));
menuMap.tambahMenu(new Menu("M03", "Americano",     "Coffee",     15000, 100));
```

*Visualisasi Representasi Logis Tree berdasarkan data aktual:*

```
Menu Utama (Root)
 ├── Coffee
 │    ├── Coffee Latte  (M01 - Rp 20.000)
 │    └── Americano     (M03 - Rp 15.000)
 └── Non-Coffee
      └── Matcha Latte  (M02 - Rp 25.000)
```

---
