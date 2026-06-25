package struktur;

import model.Pesanan;
import java.util.ArrayList;
import java.util.List;

/**
 * STRUKTUR DATA: SINGLY LINKED LIST (Implementasi Mandiri/Kustom)
 * Berfungsi mencatat seluruh log riwayat transaksi secara dinamis tanpa batas alokasi array statis.
 * Setiap node menyimpan objek data pesanan dan alamat pointer referensi ke node berikutnya.
 */
public class PesananLinkedList {

    /**
     * Representasi internal pembungkus elemen data (Simpul/Node) pada Linked List.
     */
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

    /**
     * Menambahkan pesanan baru di ujung akhir rantai Linked List (Append).
     */
    public void tambah(Pesanan pesanan) {
        Node newNode = new Node(pesanan);
        if (head == null) {
            head = newNode; // Jika list kosong, node baru langsung menjadi Head
        } else {
            Node current = head;
            // Melakukan perulangan geser hingga menemukan simpul ekor terakhir (Tail)
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode; // Menyambungkan pointer ekor ke node baru
        }
        size++;
    }

    /**
     * Menghapus simpul pesanan tertentu dari rantai pointer berdasarkan ID.
     * @return true jika proses penghapusan mata rantai memori berhasil dilakukan.
     */
    public boolean hapus(String pesananId) {
        if (head == null) return false;
        
        // Kondisi jika data yang mau dihapus berada di simpul utama (Head)
        if (head.data.getId().equals(pesananId)) {
            head = head.next; // Memindahkan Head ke simpul kedua
            size--;
            return true;
        }
        
        Node current = head;
        // Mencari simpul yang memiliki anak simpul dengan ID target yang cocok
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

    /**
     * Pencarian linear (Linear Search) pesanan berdasarkan ID transaksi spesifik.
     */
    public Pesanan cariById(String pesananId) {
        Node current = head;
        while (current != null) {
            if (current.data.getId().equals(pesananId)) return current.data;
            current = current.next;
        }
        return null;
    }

    /**
     * Mencari transaksi pelanggan berdasarkan nama (Mendukung pencarian parsial case-insensitive).
     */
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

    /**
     * Mengonversi isi data berantai dari Linked List kustom menjadi struktur Java List Standard 
     * agar mudah diolah dan ditampilkan di komponen tabel GUI.
     */
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