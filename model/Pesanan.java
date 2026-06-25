package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Kelas Pesanan merepresentasikan data transaksi atau orderan pelanggan.
 * Menyimpan informasi detail mengenai menu yang dipesan, total biaya, 
 * metode pembayaran, status pembayaran, serta waktu transaksi.
 */
public class Pesanan {
    private String id;
    private String namaPelanggan;
    private String rincianMenu;
    private long totalHarga;
    private int totalCup;
    private String metodePembayaran;
    private long uangTunai; // Jumlah uang tunai yang diberikan oleh pelanggan
    private LocalDateTime waktu;
    private String status;

    /**
     * Konstruktor default untuk membuat pesanan baru.
     * Status otomatis diset "BELUM BAYAR" dan waktu diset ke saat ini.
     */
    public Pesanan(String id, String namaPelanggan, String rincianMenu, long totalHarga, int totalCup, String metodePembayaran, long uangTunai) {
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

    /**
     * Konstruktor overloading untuk memuat ulang data pesanan yang sudah ada (misal dari database).
     */
    public Pesanan(String id, String namaPelanggan, String rincianMenu, long totalHarga, int totalCup, String metodePembayaran, long uangTunai, String status, LocalDateTime waktu) {
        this(id, namaPelanggan, rincianMenu, totalHarga, totalCup, metodePembayaran, uangTunai);
        this.status = status;
        this.waktu = waktu;
    }

    // --- Getter Methods ---
    
    public String getId() { return id; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public String getRincianMenu() { return rincianMenu; }
    public long getTotalHarga() { return totalHarga; }
    public int getTotalCup() { return totalCup; }
    public String getMetodePembayaran() { return metodePembayaran; }
    public long getUangTunai() { return uangTunai; }
    
    /**
     * Menghitung uang kembalian secara otomatis berdasarkan uang tunai dan total harga.
     * @return Nilai kembalian (bisa bernilai negatif jika uang kurang).
     */
    public long getKembalian() { 
        return uangTunai - totalHarga; 
    } 
    
    public String getStatus() { return status; }
    public LocalDateTime getWaktu() { return waktu; }

    // --- Setter Methods ---

    public void setStatus(String status) { this.status = status; }
    
    /**
     * Mengubah format objek LocalDateTime menjadi string tanggal dengan format dd/MM/yyyy.
     * @return String tanggal yang sudah diformat (Contoh: 25/06/2026).
     */
    public String getWaktuFormatted() { 
        return waktu.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); 
    }
}