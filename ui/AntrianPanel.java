package ui;

import model.Pesanan;
import struktur.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder; 
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Kelas AntrianPanel menangani halaman pengolahan antrian pesanan berkategori "BELUM BAYAR".
 * Menyediakan alur validasi upload struk gambar untuk metode QRIS serta terintegrasi otomatis 
 * ke API web WhatsApp untuk pelaporan admin toko.
 */
public class AntrianPanel extends JPanel {
    private JPanel listPanel;
    // Nomor tujuan pengiriman konfirmasi teks nota transfer QRIS
    private final String WA_ADMIN = "62882008254508"; 

    public AntrianPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(Theme.pageHeader("Antrian Pembayaran", "Urutan FIFO - Pelanggan pertama dilayani"), BorderLayout.WEST);
        
        JButton btnKosongkan = Theme.dangerButton("Kosongkan Antrian");
        btnKosongkan.setPreferredSize(new Dimension(180, 45));
        btnKosongkan.addActionListener(e -> kosongkanAntrian());
        
        JPanel pnlKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        pnlKanan.setOpaque(false);
        pnlKanan.add(btnKosongkan);
        headerPanel.add(pnlKanan, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- CONTAINER SCROLLABLE UNTUK LIST KARTU ANTRIAN ---
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.getVerticalScrollBar().setUnitIncrement(16); // Menyeimbangkan kecepatan gulir mouse
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    /**
     * Memperbarui daftar tumpukan kartu antrian aktif yang disaring berdasarkan status "BELUM BAYAR".
     */
    public void refresh() {
        listPanel.removeAll();
        boolean adaAntrian = false;

        for (Pesanan p : DataManager.getInstance().getQueue().getAntrian()) {
            if ("BELUM BAYAR".equals(p.getStatus())) {
                listPanel.add(createAntrianCard(p));
                listPanel.add(Box.createVerticalStrut(15)); // Pemisah jarak antar kartu
                adaAntrian = true;
            }
        }

        // State visual jika antrian toko sedang dalam kondisi kosong
        if (!adaAntrian) {
            JLabel lblKosong = new JLabel("Tidak ada antrian pembayaran saat ini.");
            lblKosong.setFont(Theme.FONT_BODY);
            lblKosong.setForeground(Theme.TEXT_MUTED);
            lblKosong.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(50));
            listPanel.add(lblKosong);
        }

        revalidate();
        repaint();
    }

    /**
     * Merakit panel komponen berbentuk kartu (Card UI) untuk merepresentasikan informasi satu pesanan antrian.
     */
    private JPanel createAntrianCard(Pesanan p) {
        JPanel card = Theme.modernCard();
        card.setLayout(new BorderLayout(15, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        // SISI KIRI: Penanda ID Transaksi & Nama Pelanggan
        JPanel pnlKiri = new JPanel(new BorderLayout(0, 5));
        pnlKiri.setOpaque(false);
        pnlKiri.setPreferredSize(new Dimension(200, 0));

        JLabel lblId = new JLabel(p.getId(), SwingConstants.CENTER);
        lblId.setFont(Theme.FONT_BODY_BOLD);
        lblId.setForeground(Color.WHITE);
        lblId.setOpaque(true);
        lblId.setBackground(Theme.BLUE_PRIMARY);
        lblId.setBorder(new EmptyBorder(4, 10, 4, 10));
        
        JLabel lblNama = new JLabel(p.getNamaPelanggan());
        lblNama.setFont(Theme.FONT_H2);
        
        pnlKiri.add(lblId, BorderLayout.NORTH);
        pnlKiri.add(lblNama, BorderLayout.CENTER);

        // SISI TENGAH: Area Teks Deskripsi Item-item Menu Belanjaan
        JTextArea txtRincian = new JTextArea(p.getRincianMenu());
        txtRincian.setFont(Theme.FONT_SMALL);
        txtRincian.setForeground(Theme.TEXT_MUTED);
        txtRincian.setOpaque(false);
        txtRincian.setEditable(false);
        txtRincian.setFocusable(false);
        txtRincian.setLineWrap(true);
        txtRincian.setWrapStyleWord(true);

        // SISI KANAN: Detail Informasi Harga Tagihan & Tombol Eksekusi Kasir
        JPanel pnlKanan = new JPanel(new BorderLayout(15, 0));
        pnlKanan.setOpaque(false);
        
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlInfo.setOpaque(false);
        pnlInfo.add(new JLabel("<html><font size='3'>Metode: </font><b>" + p.getMetodePembayaran() + "</b></html>"));
        pnlInfo.add(new JLabel("<html><font size='3'>Tagihan: </font><b>Rp " + String.format("%,.0f", (double) p.getTotalHarga()) + "</b></html>"));
        
        JPanel pnlAksi = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlAksi.setOpaque(false);
        
        JButton btnBayar = Theme.primaryButton("Bayar Sekarang");
        btnBayar.addActionListener(e -> prosesPembayaran(p));
        
        JButton btnBatal = new JButton("Batalkan");
        btnBatal.setFont(Theme.FONT_BODY_BOLD);
        btnBatal.setForeground(Theme.DANGER);
        btnBatal.setContentAreaFilled(false);
        btnBatal.setBorderPainted(false);
        btnBatal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBatal.addActionListener(e -> batalkanPesanan(p));
        
        pnlAksi.add(btnBayar);
        pnlAksi.add(btnBatal);

        pnlKanan.add(pnlInfo, BorderLayout.CENTER);
        pnlKanan.add(pnlAksi, BorderLayout.EAST);

        card.add(pnlKiri, BorderLayout.WEST);
        card.add(txtRincian, BorderLayout.CENTER);
        card.add(pnlKanan, BorderLayout.EAST);

        return card;
    }

    /**
     * Menyaring alur penyelesaian transaksi kasir berdasarkan jenis instrumen pembayaran.
     */
    private void prosesPembayaran(Pesanan p) {
        if (p.getMetodePembayaran().equalsIgnoreCase("QRIS")) {
            tampilkanDialogUploadQRIS(p); // Jika QRIS, kasir wajib upload foto bukti bayar terlebih dahulu
        } else {
            selesaikanTransaksi(p); // Jika cash, bisa langsung lunas
        }
    }

    /**
     * Membuka modal pop-up (JDialog) untuk keperluan simulasi upload berkas gambar bukti bayar QRIS.
     */
    private void tampilkanDialogUploadQRIS(Pesanan p) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Upload Bukti QRIS: " + p.getId(), true);
        dialog.setSize(400, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Upload Bukti Transfer QRIS");
        lblTitle.setFont(Theme.FONT_H2);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(20));

        JLabel lblPreview = new JLabel("Belum ada bukti", SwingConstants.CENTER);
        lblPreview.setPreferredSize(new Dimension(200, 280));
        lblPreview.setMaximumSize(new Dimension(200, 280));
        lblPreview.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        lblPreview.setOpaque(true);
        lblPreview.setBackground(Theme.BG_MAIN);
        lblPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblPreview);
        panel.add(Box.createVerticalStrut(15));

        final File[] selectedFile = new File[1]; // Variabel penampung file gambar pilihan kasir

        JButton btnPilih = Theme.secondaryButton("Pilih Foto Bukti...");
        btnPilih.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPilih.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "jpeg", "png"));
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(selectedFile[0].getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(200, 280, Image.SCALE_SMOOTH);
                lblPreview.setIcon(new ImageIcon(img));
                lblPreview.setText("");
            }
        });
        panel.add(btnPilih);
        panel.add(Box.createVerticalStrut(25));

        JButton btnSelesai = Theme.primaryButton("Selesai & Kirim WA");
        btnSelesai.setMaximumSize(new Dimension(300, 45));
        btnSelesai.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSelesai.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Harap pilih foto bukti transfer terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                File dir = new File("proofs");
                if (!dir.exists()) dir.mkdir();
                
                // Menyalin file gambar pilihan kasir ke folder lokal aplikasi 'proofs/[ID].jpg'
                File tujuan = new File(dir, p.getId() + ".jpg");
                Files.copy(selectedFile[0].toPath(), tujuan.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan foto bukti: " + ex.getMessage());
            }

            dialog.dispose();
            selesaikanTransaksi(p);
            bukaWhatsApp(p.getId(), p.getTotalHarga()); // Redirect otomatis ke browser WA
        });
        panel.add(btnSelesai);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void selesaikanTransaksi(Pesanan p) {
        p.setStatus("LUNAS");
        DataManager.getInstance().simpanSemua();
        JOptionPane.showMessageDialog(this, "Pembayaran untuk " + p.getId() + " berhasil diproses!");
        refresh();
    }

    private void batalkanPesanan(Pesanan p) {
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin membatalkan pesanan " + p.getId() + "?\nStok yang sudah terpotong tidak akan kembali otomatis.", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            p.setStatus("BATAL");
            DataManager.getInstance().simpanSemua();
            refresh();
        }
    }

    /**
     * Membatalkan seluruh pesanan antrian toko berstatus "BELUM BAYAR" secara massal.
     */
    private void kosongkanAntrian() {
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin mengosongkan SELURUH antrian yang belum dibayar?", "Peringatan Bahaya", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            for (Pesanan p : DataManager.getInstance().getQueue().getAntrian()) {
                if ("BELUM BAYAR".equals(p.getStatus())) {
                    p.setStatus("BATAL");
                }
            }
            DataManager.getInstance().simpanSemua();
            refresh();
        }
    }

    /**
     * Mengonversi string pelaporan menjadi kode URL web yang valid, lalu memicu 
     * aplikasi default browser komputer untuk membuka ruang chat WhatsApp Admin Pusat.
     */
    private void bukaWhatsApp(String idTrx, long nominal) {
        try {
            String pesan = "Halo Admin, berikut adalah pelaporan bukti transfer QRIS untuk kasir.\n\n" +
                           "*ID Transaksi:* " + idTrx + "\n" +
                           "*Nominal:* Rp " + nominal + "\n\n" +
                           "Mohon konfirmasinya. Bukti foto akan saya lampirkan di bawah ini.";
                           
            // Melakukan encoding string (mengonversi spasi jadi %20, enter jadi %0A, dll) agar aman dibaca browser
            String pesanEncoded = URLEncoder.encode(pesan, "UTF-8");
            String url = "https://wa.me/" + WA_ADMIN + "?text=" + pesanEncoded;
            
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                
                JOptionPane.showMessageDialog(this, 
                    "WhatsApp telah dibuka di browser Anda.\n\nFoto bukti transfer telah disimpan di folder:\n'proofs/" + idTrx + ".jpg'\n\nSilakan 'Attach/Kirim Gambar' pada chat WhatsApp.", 
                    "Instruksi Pengiriman", JOptionPane.INFORMATION_MESSAGE);
            } else {
                throw new Exception("Browser desktop tidak didukung.");
            }
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuka WhatsApp (" + e.getMessage() + ").\nSilakan buka WhatsApp secara manual.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}