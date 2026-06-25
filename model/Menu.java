package model;

/**
 * Kelas Menu merepresentasikan item produk yang dijual dalam sistem kasir/restoran.
 * Kelas ini juga menangani informasi stok serta kategori menu.
 */
public class Menu {
    private String kode;
    private String nama;
    private String kategori; 
    private long harga;
    private int stok; // Menyimpan sisa ketersediaan barang di gudang/dapur

    /**
     * Konstruktor untuk membuat objek Menu baru.
     */
    public Menu(String kode, String nama, String kategori, long harga, int stok) {
        this.kode = kode;
        this.nama = nama;
        this.kategori = kategori;
        this.harga = harga;
        this.stok = stok;
    }

    // --- Getter Methods ---

    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public String getKategori() { return kategori; }
    public long getHarga() { return harga; }
    public int getStok() { return stok; }

    // --- Setter Methods ---

    public void setNama(String nama) { this.nama = nama; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setHarga(long harga) { this.harga = harga; }
    public void setStok(int stok) { this.stok = stok; }

    /**
     * Mengurangi stok barang ketika ada transaksi atau pemesanan.
     * @param jumlah Jumlah stok yang ingin dikurangi.
     */
    public void kurangiStok(int jumlah) { 
        this.stok -= jumlah; 
    }

    /**
     * Memformat harga ke dalam bentuk mata uang Rupiah standard (tanpa simbol desimal).
     * @return String harga terformat (Contoh: Rp 15.000).
     */
    public String getHargaFormatted() { 
        return String.format("Rp %,.0f", (double) harga); 
    }
}