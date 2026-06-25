package struktur;

import model.Akun;
import model.Menu;
import model.Pesanan;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Kelas DataManager mengimplementasikan Design Pattern SINGLETON.
 * Berfungsi sebagai pusat kendali data (Data Access Object) yang mengatur sinkronisasi 
 * antara memori RAM (Struktur Data) dan penyimpanan permanen (File Teks .txt).
 */
public class DataManager {
    // Referensi ke berbagai struktur data yang digunakan aplikasi
    private PesananQueue queue;
    private PesananLinkedList linkedList;
    private MenuHashMap menuMap;
    private AkunHashMap akunMap; 

    // Satu-satunya instance dari DataManager (Singleton)
    private static DataManager instance;

    // Nama file penyimpanan data lokal
    private static final String FILE_PESANAN = "data_pesanan.txt";
    private static final String FILE_MENU = "data_menu.txt";
    private static final String FILE_AKUN = "data_akun.txt";

    /**
     * Konstruktor privat mencegah instansiasi langsung dari luar kelas.
     * Menginisialisasi semua objek struktur data dan memuat data dari file.
     */
    private DataManager() {
        queue = new PesananQueue();
        linkedList = new PesananLinkedList();
        menuMap = new MenuHashMap();
        akunMap = new AkunHashMap();
        
        // Data Stok Awal Bahan Baku menggunakan format: {stok_sekarang, maksimal_stok}
        stokBahan.put("Cup 16oz", new double[]{120, 200}); 
        stokBahan.put("Susu UHT", new double[]{3.2, 10.0});
        stokBahan.put("Biji kopi", new double[]{2.1, 5.0});
        
        // Memuat data dari file teks ke dalam struktur data memori saat aplikasi dijalankan
        muatData();
    }

    /**
     * Metode global untuk mendapatkan instance tunggal dari DataManager.
     */
    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    /**
     * Memanggil semua fungsi pemuatan data dari file eksternal.
     */
    private void muatData() {
        muatAkun();
        muatMenu();
        muatPesanan();
    }

    /**
     * Membaca berkas data_akun.txt dan memasukkannya ke dalam akunMap.
     */
    private void muatAkun() {
        File f = new File(FILE_AKUN);
        if (!f.exists()) {
            // Memberikan akun default jika file belum pernah dibuat sebelumnya
            akunMap.tambahAkun(new Akun("Admin", "123"));
            akunMap.tambahAkun(new Akun("Kasir", "1245"));
            simpanAkun();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|"); // Parsing data menggunakan pemisah pipa (|)
                if (parts.length == 2) {
                    akunMap.tambahAkun(new Akun(parts[0], parts[1]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Membaca berkas data_menu.txt dan memasukkannya ke dalam menuMap.
     */
    private void muatMenu() {
        File f = new File(FILE_MENU);
        if (!f.exists()) {
            // Data cetakan dasar produk menu cafe
            menuMap.tambahMenu(new Menu("M01", "Coffee Latte", "Coffee", 20000, 122));
            menuMap.tambahMenu(new Menu("M02", "Matcha Latte", "Non-Coffee", 25000, 50));
            menuMap.tambahMenu(new Menu("M03", "Americano", "Coffee", 15000, 100));
            simpanMenu();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 5) {
                    menuMap.tambahMenu(new Menu(p[0], p[1], p[2], Long.parseLong(p[3]), Integer.parseInt(p[4])));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Membaca riwayat transaksi dari berkas data_pesanan.txt.
     * Memasukkan data ke dalam LinkedList (riwayat) dan Queue (antrian aktif barista).
     */
    private void muatPesanan() {
        File f = new File(FILE_PESANAN);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 8) {
                    // Fitur Fallback: Mengantisipasi perbedaan format struktur data lama dan baru
                    long uang = p.length >= 9 ? Long.parseLong(p[8]) : Long.parseLong(p[3]);

                    // Mengembalikan karakter semicolon (;) menjadi baris baru (\n) untuk rincian menu
                    Pesanan pesanan = new Pesanan(p[0], p[1], p[2].replace(";", "\n"), Long.parseLong(p[3]),
                            Integer.parseInt(p[4]), p[5], uang, p[6], LocalDateTime.parse(p[7]));
                    
                    linkedList.tambah(pesanan); // Masuk ke log riwayat seluruh pesanan
                    
                    // Jika status pesanan belum dibayar/selesai, masukkan kembali ke antrian pembuatan
                    if (pesanan.getStatus().equals("BELUM BAYAR")) {
                        queue.tambahKeAntrian(pesanan);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Menyimpan seluruh struktur data memori kembali ke bentuk berkas teks secara berkala.
     */
    public void simpanSemua() {
        simpanPesanan();
        simpanMenu();
        simpanAkun();
    }

    private void simpanAkun() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_AKUN))) {
            for (Akun a : akunMap.getSemuaAkun()) {
                pw.println(a.getUsername() + "|" + a.getPassword());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void simpanPesanan() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PESANAN))) {
            for (Pesanan p : linkedList.getSemuaPesanan()) {
                // Mengubah baris baru rincian menu menjadi semicolon agar tidak merusak baris file teks
                String rincianSatuBaris = p.getRincianMenu().replace("\n", ";");
                pw.println(p.getId() + "|" + p.getNamaPelanggan() + "|" + rincianSatuBaris + "|" + p.getTotalHarga()
                        + "|" + p.getTotalCup() + "|" + p.getMetodePembayaran() + "|" + p.getStatus() + "|"
                        + p.getWaktu().toString() + "|" + p.getUangTunai());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void simpanMenu() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_MENU))) {
            for (Menu m : menuMap.getSemuaMenu()) {
                pw.println(m.getKode() + "|" + m.getNama() + "|" + m.getKategori() + "|" + m.getHarga() + "|"
                        + m.getStok());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Menambahkan data pesanan baru langsung ke dalam log riwayat transaksi dan antrian antarmuka barista.
     */
    public void tambahPesananBaru(Pesanan p) {
        linkedList.tambah(p);
        queue.tambahKeAntrian(p);
        simpanSemua(); // Auto-save ke penyimpanan lokal setiap ada perubahan data
    }

    /**
     * Menghapus pesanan berdasarkan ID dari linked list riwayat transaksi maupun antrian antrian aktif.
     */
    public void hapusPesanan(String id) {
        linkedList.hapus(id);
        queue.batalkanPesanan(id); 
        simpanSemua();
    }

    // --- LOGIKA AGREGASI DATA UNTUK HALAMAN DASHBOARD ---
    
    public int getTotalCupTerjual() {
        int total = 0;
        for (Pesanan p : linkedList.getSemuaPesanan()) {
            if (p.getStatus().equals("SELESAI"))
                total += p.getTotalCup();
        }
        return total;
    }

    public long getTotalPendapatan() {
        long total = 0;
        for (Pesanan p : linkedList.getSemuaPesanan()) {
            if (p.getStatus().equals("SELESAI"))
                total += p.getTotalHarga();
        }
        return total;
    }

    public int getJumlahAntrian() {
        return queue.getJumlahAntrian();
    }

    // --- Getters Akses Struktur Data ---
    
    public List<Pesanan> getSemuaPesanan() { return linkedList.getSemuaPesanan(); }
    public Collection<Menu> getSemuaMenu() { return menuMap.getSemuaMenu(); }
    public MenuHashMap getMenuMap() { return menuMap; }
    public PesananQueue getQueue() { return queue; }
    public AkunHashMap getAkunMap() { return akunMap; }

    // Objek Map lokal untuk manajemen kapasitas persediaan bahan dapur
    private java.util.Map<String, double[]> stokBahan = new java.util.LinkedHashMap<>();

    /**
     * Mengkalkulasi statistik menu paling laris menggunakan Stream API.
     * Mem-parsing string deskripsi pesanan untuk menghitung akumulasi kuantitas penjualan.
     * @return Map berisi nama menu dan total kuantitas yang berurutan dari paling laris.
     */
    public java.util.Map<String, Integer> getTopSellingMenu() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        for (Pesanan p : linkedList.getSemuaPesanan()) {
            if ("SELESAI".equals(p.getStatus())) {
                String[] items = p.getRincianMenu().split("\n");
                for (String item : items) {
                    try {
                        // Pola format String: "2x Nama Menu" -> Memisahkan Qty dan Nama Menu
                        String namaMenu = item.substring(item.indexOf("x ") + 2).trim();
                        int qty = Integer.parseInt(item.substring(0, item.indexOf("x")));
                        stats.put(namaMenu, stats.getOrDefault(namaMenu, 0) + qty);
                    } catch (Exception e) {
                        /* Mengabaikan baris string yang gagal diproses */ 
                    }
                }
            }
        }
        // Mengurutkan Map berdasarkan nilai Value (Kuantitas terjual) secara Descending
        return stats.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey, java.util.Map.Entry::getValue,
                        (e1, e2) -> e1, java.util.LinkedHashMap::new));
    }
    
    public java.util.Map<String, double[]> getStokBahan() { return stokBahan; }
    
    public void updateStokBahan(String nama, double jumlahBaru) {
        if (stokBahan.containsKey(nama)) {
            stokBahan.get(nama)[0] = jumlahBaru;
        }
    }
    
    // --- STRUKTUR DAN METODE BARU UNTUK MANAJEMEN AKUN MULTI-ROLE (KASIR/ADMIN) ---
    // Catatan: Ini melengkapi/mengganti fungsionalitas akunMap bawaan sebelumnya.
    private java.util.Map<String, String[]> dataAkun = new java.util.HashMap<>();

    /**
     * Mendapatkan daftar semua akun terdaftar dengan array nilai format [Password, Role].
     */
    public java.util.Map<String, String[]> getSemuaAkun() { 
        if(dataAkun.isEmpty()) {
            dataAkun.put("Admin", new String[]{"123", "Admin"}); // Inisialisasi default Akun Atasan
            dataAkun.put("Kasir", new String[]{"111", "Kasir"}); // Inisialisasi default Akun Pegawai Toko
        }
        return dataAkun; 
    }
    
    /**
     * Menyimpan atau memperbarui data kredensial akun user baru ke dalam sistem map lokal.
     */
    public void simpanAkun(String username, String password, String role) {
        dataAkun.put(username, new String[]{password, role});
    }
    
    /**
     * Menghapus hak akses akun user berdasarkan username unik.
     */
    public void hapusAkun(String username) {
        dataAkun.remove(username);
    }
}