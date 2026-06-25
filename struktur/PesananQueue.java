package struktur;

import model.Pesanan;
import java.util.LinkedList;
import java.util.Queue;

/**
 * STRUKTUR DATA: QUEUE (FIFO - First In First Out)
 * Digunakan khusus untuk mengelola urutan pengerjaan produk oleh Barista di dapur.
 * Menjamin pelanggan yang memesan lebih awal dilayani terlebih dahulu.
 */
public class PesananQueue {
    // Memanfaatkan LinkedList bawaan Java untuk implementasi struktur antrian (Queue)
    private Queue<Pesanan> antrianBarista;

    public PesananQueue() {
        this.antrianBarista = new LinkedList<>();
    }

    /**
     * Memasukkan pesanan baru ke baris paling belakang antrian (Enqueue).
     */
    public boolean tambahKeAntrian(Pesanan pesanan) {
        return antrianBarista.offer(pesanan);
    }

    /**
     * Mengambil pesanan di urutan paling depan untuk segera diproses barista (Dequeue).
     * @return Objek pesanan pertama, atau null jika antrian sedang kosong.
     */
    public Pesanan ambilPesananBerikutnya() {
        return antrianBarista.poll(); 
    }

    /**
     * Melakukan pencarian linier di dalam antrian untuk mengubah status transaksi
     * dan menghapusnya apabila pelanggan membatalkan pesanan di tengah jalan.
     */
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