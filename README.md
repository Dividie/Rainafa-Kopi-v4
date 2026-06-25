# Rainafa Coffee POS

Implementasi konsep **Object-Oriented Programming (OOP)** dan berbagai **Struktur Data** yang digunakan dalam pengembangan aplikasi *Point of Sale (POS) Modern Rainafa Coffee*. 

---

## Daftar Isi
1. [Object-Oriented Programming (OOP)](#1-object-oriented-programming-oop)
2. [Array / ArrayList](#2-array--arraylist)
3. [Queue (Antrian FIFO)](#3-queue-antrian-fifo)
4. [Hash / Map (HashMap)](#4-hash--map-hashmap)
5. [Stack (LIFO)](#5-stack-lifo)
6. [Tree (Hierarki Kategori)](#6-tree-hierarki-kategori)

---

## 1. Object-Oriented Programming (OOP)

###  Deskripsi & Penerapan
Pendekatan OOP digunakan untuk fondasi utama program ini. Kita membungkus data dan fungsi ke dalam objek nyata.

###  (model/Pesanan.java)
```java
package model;

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
```
---

## 2. Array / ArrayList

###  Deskripsi & Penerapan

* **Array Primitif:** Digunakan untuk menyimpan data statis yang ukurannya sudah pasti sejak awal program berjalan, seperti nama-nama kolom pada tabel.
* **ArrayList:** Digunakan untuk menampung koleksi objek secara dinamis. Ukuran `ArrayList` akan bertambah atau berkurang secara otomatis di memori (RAM) seiring berjalannya transaksi baru.

###  (ui/RiwayatPanel.java)

```java
// 1. Array Primitif untuk mendefinisikan layout tabel tetap
String[] kolom = {"ID Transaksi", "Keterangan Pesanan", "Metode", "Total Pendapatan", "Status"};

// 2. ArrayList untuk menampung data transaksi yang dinamis
List<Pesanan> listPesanan = new ArrayList<>(DataManager.getInstance().getQueue().getAntrian());

// Perulangan mundur (Looping dari indeks terakhir)
// Berfungsi agar transaksi paling baru (paling bawah di list) muncul di baris teratas JTable
for (int i = listPesanan.size() - 1; i >= 0; i--) {
    Pesanan p = listPesanan.get(i);
    modelTabel.addRow(new Object[]{
        p.getId(),
        p.getRincianMenu().replace("\n", ", "),
        p.getMetodePembayaran(),
        "Rp " + String.format("%,.0f", (double) p.getTotalHarga()),
        p.getStatus()
    });
}

```

---

## 3. Queue (Antrian FIFO)

###  Deskripsi & Penerapan

Mekanisme antrian pesanan pelanggan di kasir menggunakan prinsip **FIFO (First-In, First-Out)**. Pelanggan yang datang dan memesan terlebih dahulu akan masuk ke antrian paling depan untuk diproses pembayarannya dan diproduksi oleh barista terlebih dahulu.

###  (struktur/AntrianSistem.java)

```java
import java.util.LinkedList;
import java.util.Queue;
import model.Pesanan;

public class AntrianSistem {
    // Menggunakan antrian berbasis LinkedList bawaan Java
    private Queue<Pesanan> daftarAntrian = new LinkedList<>();

    // Operasi Enqueue: Menambahkan pesanan baru ke baris paling belakang
    public void tambahKeAntrian(Pesanan pesananBaru) {
        daftarAntrian.add(pesananBaru);
    }

    // Operasi Dequeue: Mengambil dan menghapus pesanan paling depan setelah lunas/selesai
    public Pesanan prosesSelesai() {
        return daftarAntrian.poll(); 
    }
}

```

---

## 4. Hash / Map (HashMap)

###  Deskripsi & Penerapan

Digunakan pada proses pencarian (*searching*) data menu makanan dan minuman secara instan berdasarkan kode produk unik. Dengan menggunakan `HashMap`, kompleksitas waktu pencariannya adalah $O(1)$ (konstan), sehingga performa aplikasi tetap stabil dan cepat meskipun kafe memiliki ratusan menu.

###  (struktur/ManajemenMenu.java)

```java
import java.util.HashMap;

public class ManajemenMenu {
    // Key: Kode Menu (String), Value: Harga Menu (Double)
    private HashMap<String, Double> mapHargaMenu = new HashMap<>();

    public void inisialisasiMenu() {
        // Mapping Kode Produk ke Data Aslinya
        mapHargaMenu.put("RNF", 15000.0); // Rainafa Coffee
        mapHargaMenu.put("MTC", 18000.0); // Matcha
        mapHargaMenu.put("CKL", 22000.0); // Cokelat
    }

    // Pencarian instan langsung menuju Key tanpa perlu perulangan (looping)
    public double cariHargaMenu(String kodeMenu) {
        if (mapHargaMenu.containsKey(kodeMenu)) {
            return mapHargaMenu.get(kodeMenu);
        }
        return 0.0;
    }
}

```

---

## 5. Stack (LIFO)

###  Deskripsi & Penerapan

*(Diusulkan/Roadmap Fitur)* Struktur data Stack dirancang untuk memfasilitasi sistem navigasi riwayat aktivitas (fitur **Undo / Back**). Setiap kali pengguna membuka panel baru, halaman tersebut akan ditumpuk (*Push*). Ketika tombol "Kembali" ditekan, halaman teratas akan dibuang (*Pop*) menggunakan prinsip **LIFO (Last-In, First-Out)**.

###  (struktur/NavigasiSistem.java)

```java
import java.util.Stack;

public class NavigasiSistem {
    private Stack<String> riwayatHalaman = new Stack<>();

    // Menyimpan rekam jejak halaman baru yang diklik user
    public void bukaHalaman(String namaHalaman) {
        riwayatHalaman.push(namaHalaman);
    }

    // Mekanisme tombol 'Kembali' pintar
    public String kembaliKeHalamanSebelumnya() {
        if (riwayatHalaman.size() > 1) {
            riwayatHalaman.pop(); // Menghapus halaman aktif saat ini
            return riwayatHalaman.peek(); // Mengambil halaman tepat di bawahnya
        }
        return "Dashboard"; // Fallback default
    }
}

```

---

## 6. Tree (Hierarki Kategori)

###  Deskripsi & Penerapan

Digunakan untuk merepresentasikan struktur pengelompokan kategori produk secara bertingkat. Data diorganisir mulai dari *Root* (Akar) hingga *Leaf* (Daun/Produk Akhir) untuk mempermudah filter produk berdasarkan kategori dan sub-kategori pada sisi manajemen data admin.

###  (model/KategoriNode.java)

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

/*
Visualisasi Representasi Logis Tree:
Menu Utama (Root)
 ├── Minuman
 │    ├── Coffee (Rainafa Coffee, Americano)
 │    └── Non-Coffee (Matcha, Chocomint)
 └── Makanan
      └── Snack (Kentang Goreng)
*/

```
