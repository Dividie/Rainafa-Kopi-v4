package ui;

import model.Menu;
import model.Pesanan;
import struktur.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Kelas KasirPanel mengelola modul Point of Sales (POS) utama.
 * Memuat grid catalog produk menu, filter tab kategori, keranjang belanjaan interaktif, 
 * hingga kalkulasi kalkulator uang kembalian pelanggan otomatis.
 */
public class KasirPanel extends JPanel {
    private JPanel gridProduk;
    private JPanel cartItemsPanel;
    private JTextField txtUang, txtCari;
    private JLabel lblTotal, lblKembali;
    private JComboBox<String> cbMetode;
    
    // --- ATRIBUT UNTUK PENOMORAN NOTA OTOMATIS ---
    private JLabel lblKodeTransaksi;
    private String currentTrxId; 
    private static int trxSequence = 1; // Konter urutan nomor transaksi toko
    
    private String kategoriAktif = "Semua";
    private List<JButton> btnKategoriList = new ArrayList<>();
    
    // Menggunakan LinkedHashMap agar urutan menu yang masuk ke keranjang belanja tetap teratur konsisten
    private Map<Menu, Integer> keranjang = new LinkedHashMap<>();

    public KasirPanel(MainFrame frame) { 
        setLayout(new BorderLayout(20, 0));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        generateNewTrxId(); // Membuat kode identitas nota awal saat panel diinisialisasi

        // --- PANEL PENCARIAN ATAS ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(Theme.pageHeader("Kasir", "Pilih menu untuk ditambahkan ke pesanan."), BorderLayout.WEST);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); 
        
        JLabel lblCari = new JLabel("🔍 Cari Menu:");
        lblCari.setFont(Theme.FONT_BODY_BOLD);
        lblCari.setForeground(Theme.TEXT_MUTED);
        
        txtCari = Theme.modernInput();
        txtCari.setPreferredSize(new Dimension(250, 40));
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshMenu(); }
            public void removeUpdate(DocumentEvent e) { refreshMenu(); }
            public void changedUpdate(DocumentEvent e) { refreshMenu(); }
        });
        
        searchPanel.add(lblCari);
        searchPanel.add(txtCari);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- WRAPPER TAB KATEGORI DAN GRID KATALOG ---
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);

        JPanel topFilterBar = new JPanel(new BorderLayout());
        topFilterBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);
        String[] categories = {"Semua", "Coffee", "Non-Coffee", "Snack", "Food", "Dessert"};
        for (String cat : categories) {
            JButton btnCat = createCategoryButton(cat);
            btnKategoriList.add(btnCat);
            filterPanel.add(btnCat);
        }
        topFilterBar.add(filterPanel, BorderLayout.WEST);
        centerWrapper.add(topFilterBar, BorderLayout.NORTH);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        
        gridProduk = new JPanel(new GridLayout(0, 3, 15, 15)); // Grid catalog berisi 3 kolom item kesamping
        gridProduk.setOpaque(false);
        gridWrapper.add(gridProduk, BorderLayout.NORTH); 
        
        JScrollPane scrollGrid = new JScrollPane(gridWrapper);
        scrollGrid.setBorder(null);
        scrollGrid.getViewport().setBackground(Theme.BG_MAIN);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);
        scrollGrid.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        centerWrapper.add(scrollGrid, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        initCartPanel(); // Merakit sidebar ringkasan keranjang belanja sisi kanan
        refreshMenu(); 
    }

    /**
     * Memformat bilangan urutan konter kasir menjadi string terisi padding 3 digit (Contoh: TRX001).
     */
    private void generateNewTrxId() {
        currentTrxId = String.format("TRX%03d", trxSequence);
        if (lblKodeTransaksi != null) {
            lblKodeTransaksi.setText("#" + currentTrxId); 
        }
    }

    /**
     * Membuat tombol filter kategori dengan bentuk melengkung kustom.
     */
    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isAktif = text.equals(kategoriAktif);
                
                if (isAktif) {
                    g2.setColor(Theme.BLUE_PRIMARY); 
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.BG_MAIN);      
                } else {
                    g2.setColor(Color.WHITE);        
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                if (!isAktif) {
                    g2.setColor(Theme.BORDER_COLOR);
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(text.equals(kategoriAktif) ? Color.WHITE : Theme.TEXT_DARK);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        btn.addActionListener(e -> {
            kategoriAktif = text;          
            updateCategoryButtonStyles();  
            refreshMenu();                 
        });
        
        return btn;
    }

    private void updateCategoryButtonStyles() {
        for (JButton btn : btnKategoriList) {
            btn.setForeground(btn.getText().equals(kategoriAktif) ? Color.WHITE : Theme.TEXT_DARK);
            btn.repaint(); 
        }
    }

    /**
     * Memotong gambar katalog produk agar proporsional dan tidak mengalami distorsi/gepeng saat dimuat ke card.
     */
    private Image autofitImage(String imagePath, int targetWidth, int targetHeight) {
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            if (img == null) return null;
            
            int width = img.getWidth();
            int height = img.getHeight();
            
            double ratioX = (double) targetWidth / width;
            double ratioY = (double) targetHeight / height;
            double ratio = Math.max(ratioX, ratioY); 
            
            int finalWidth = (int) (width * ratio);
            int finalHeight = (int) (height * ratio);
            
            Image scaledImg = img.getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH);
            BufferedImage cropped = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = cropped.createGraphics();
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            int x = (targetWidth - finalWidth) / 2;
            int y = (targetHeight - finalHeight) / 2;
            
            g2.drawImage(scaledImg, x, y, null);
            g2.dispose();
            
            return cropped;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Membaca ulang daftar menu dari gudang memori, menyaringnya berdasarkan ketikan keyword 
     * pencarian serta kategori aktif, kemudian menggambar ulang seluruh card produk kasir.
     */
    public void refreshMenu() {
        gridProduk.removeAll();
        String keyword = (txtCari != null) ? txtCari.getText().toLowerCase().trim() : "";

        for (Menu m : DataManager.getInstance().getSemuaMenu()) {
            boolean matchKategori = kategoriAktif.equals("Semua") || m.getKategori().equalsIgnoreCase(kategoriAktif);
            boolean matchKeyword = keyword.isEmpty() || m.getNama().toLowerCase().contains(keyword);

            if (matchKategori && matchKeyword) {
                gridProduk.add(createProductCard(m));
            }
        }
        gridProduk.revalidate(); 
        gridProduk.repaint();
    }

    /**
     * Membuat visual card item produk individual dilengkapi info harga, stok, dan tombol tambah keranjang.
     */
    private JPanel createProductCard(Menu menu) {
        JPanel card = Theme.modernCard();
        card.setPreferredSize(new Dimension(160, 240)); 
        card.setLayout(new BorderLayout(0, 10));
        
        JLabel lblFoto = new JLabel("", SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(140, 110));
        
        File imgFile = new File("images/" + menu.getKode() + ".jpg");
        if (imgFile.exists()) {
            Image img = autofitImage(imgFile.getAbsolutePath(), 140, 110);
            if (img != null) lblFoto.setIcon(new ImageIcon(img));
            else lblFoto.setText("Error Image");
        } else {
            lblFoto.setText("No Image");
            lblFoto.setOpaque(true);
            lblFoto.setBackground(Theme.BG_MAIN);
            lblFoto.setForeground(Theme.TEXT_MUTED);
            lblFoto.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        }
        card.add(lblFoto, BorderLayout.NORTH);
        
        JPanel info = new JPanel(new GridLayout(3, 1, 0, 3)); 
        info.setOpaque(false);
        
        JLabel lblNama = new JLabel("<html><div style='text-align: center;'>" + menu.getNama() + "</div></html>", SwingConstants.CENTER); 
        lblNama.setFont(Theme.FONT_BODY_BOLD);
        
        JLabel lblKat = new JLabel(menu.getKategori(), SwingConstants.CENTER); 
        lblKat.setFont(Theme.FONT_SMALL); lblKat.setForeground(Theme.TEXT_MUTED);
        
        JLabel lblHarga = new JLabel(menu.getHargaFormatted(), SwingConstants.CENTER); 
        lblHarga.setFont(Theme.FONT_BODY_BOLD); lblHarga.setForeground(Theme.BLUE_PRIMARY);
        
        info.add(lblNama); info.add(lblKat); info.add(lblHarga);
        card.add(info, BorderLayout.CENTER);
        
        JButton btnAdd = Theme.primaryButton("+ Tambah");
        btnAdd.setFont(Theme.FONT_SMALL);
        btnAdd.addActionListener(e -> tambahKeKeranjang(menu));
        
        // Mematikan tombol klik apabila stok komoditas menu tercatat habis di backend
        if (menu.getStok() <= 0) {
            btnAdd.setText("Habis");
            btnAdd.setEnabled(false);
            btnAdd.setBackground(Theme.BORDER_COLOR);
        }
        card.add(btnAdd, BorderLayout.SOUTH);
        
        return card;
    }

    /**
     * Merakit panel kalkulator struk belanja dan isian uang pembayaran kasir (Sisi Kanan).
     */
    private void initCartPanel() {
        JPanel cartPanel = Theme.modernCard();
        cartPanel.setPreferredSize(new Dimension(400, 0));
        cartPanel.setLayout(new BorderLayout(0, 15));

        JPanel cartHeader = new JPanel(new BorderLayout(0, 15)); 
        cartHeader.setOpaque(false);
        
        JLabel titleCart = new JLabel("Keranjang Pesanan");
        titleCart.setFont(Theme.FONT_H2);
        cartHeader.add(titleCart, BorderLayout.NORTH);
        
        JPanel inputGroup = new JPanel(new BorderLayout(10, 0)); 
        inputGroup.setOpaque(false);
        inputGroup.add(Theme.fieldLabel("Kode Transaksi:"), BorderLayout.WEST);
        
        lblKodeTransaksi = new JLabel("#" + currentTrxId, SwingConstants.RIGHT); 
        lblKodeTransaksi.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblKodeTransaksi.setForeground(Theme.BLUE_PRIMARY);
        inputGroup.add(lblKodeTransaksi, BorderLayout.CENTER);
        cartHeader.add(inputGroup, BorderLayout.SOUTH);
        cartPanel.add(cartHeader, BorderLayout.NORTH);

        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setOpaque(false);
        
        JScrollPane scrollCart = new JScrollPane(cartItemsPanel);
        scrollCart.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER_COLOR));
        scrollCart.getViewport().setBackground(Color.WHITE);
        cartPanel.add(scrollCart, BorderLayout.CENTER);

        JPanel cartBottom = new JPanel(new BorderLayout(0, 15)); 
        cartBottom.setOpaque(false);
        
        JPanel summaryBox = new JPanel(new GridLayout(4, 1, 0, 8));
        summaryBox.setBackground(Theme.BG_MAIN);
        summaryBox.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        lblTotal = new JLabel("Rp 0", SwingConstants.RIGHT); 
        lblTotal.setFont(Theme.FONT_H1); lblTotal.setForeground(Theme.BLUE_PRIMARY);
        summaryBox.add(createRow("Total Tagihan", lblTotal));
        
        txtUang = Theme.modernInput(); 
        txtUang.addCaretListener(e -> hitungKembalian()); // Setiap kasir mengubah isian angka nominal, kembalian re-kalkulasi otomatis
        JPanel rowUang = new JPanel(new BorderLayout(10, 0)); rowUang.setOpaque(false);
        rowUang.add(Theme.fieldLabel("Uang Tunai"), BorderLayout.WEST); 
        rowUang.add(txtUang, BorderLayout.CENTER);
        summaryBox.add(rowUang);
        
        lblKembali = new JLabel("Rp 0", SwingConstants.RIGHT); 
        lblKembali.setFont(Theme.FONT_BODY_BOLD);
        summaryBox.add(createRow("Kembalian", lblKembali));

        JPanel rowMetode = new JPanel(new BorderLayout(10, 0)); rowMetode.setOpaque(false);
        cbMetode = new JComboBox<>(new String[]{"Cash", "QRIS"});
        cbMetode.setBackground(Color.WHITE);
        cbMetode.setFont(Theme.FONT_BODY_BOLD); 
        cbMetode.setPreferredSize(new Dimension(140, 38)); 
        ((JLabel)cbMetode.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        rowMetode.add(Theme.fieldLabel("Pembayaran"), BorderLayout.WEST); 
        rowMetode.add(cbMetode, BorderLayout.EAST);
        summaryBox.add(rowMetode);

        cartBottom.add(summaryBox, BorderLayout.CENTER);
        
        JButton btnCheckout = Theme.primaryButton("Proses Pembayaran");
        btnCheckout.setFont(Theme.FONT_H2);
        btnCheckout.setPreferredSize(new Dimension(0, 50));
        btnCheckout.addActionListener(e -> checkout());
        cartBottom.add(btnCheckout, BorderLayout.SOUTH);

        cartPanel.add(cartBottom, BorderLayout.SOUTH);
        add(cartPanel, BorderLayout.EAST);
    }

    private JPanel createRow(String title, Component comp) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel lbl = new JLabel(title); lbl.setFont(Theme.FONT_BODY); lbl.setForeground(Theme.TEXT_MUTED);
        p.add(lbl, BorderLayout.WEST); p.add(comp, BorderLayout.EAST);
        return p;
    }

    /**
     * Menambahkan item menu ke dalam struktur Map keranjang belanja dengan validasi kapasitas stok.
     */
    private void tambahKeKeranjang(Menu menu) {
        int qtySaatIni = keranjang.getOrDefault(menu, 0);
        
        if (qtySaatIni >= menu.getStok()) {
            JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Sisa stok " + menu.getNama() + " hanya " + menu.getStok() + ".", "Peringatan Stok", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        keranjang.put(menu, qtySaatIni + 1);
        updateCartUI();
    }

    private void kurangiDariKeranjang(Menu menu) {
        int qty = keranjang.getOrDefault(menu, 0);
        if (qty > 1) {
            keranjang.put(menu, qty - 1);
        } else {
            keranjang.remove(menu); // Bersihkan objek dari map jika qty tersisa bernilai 0
        }
        updateCartUI();
    }

    /**
     * Memperbarui daftar list baris belanjaan di dalam komponen keranjang (Sisi Kanan).
     */
    private void updateCartUI() {
        cartItemsPanel.removeAll();
        long total = 0;

        if (keranjang.isEmpty()) {
            JLabel empty = new JLabel("Keranjang masih kosong");
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));
            cartItemsPanel.add(empty);
        } else {
            for (Map.Entry<Menu, Integer> entry : keranjang.entrySet()) {
                Menu m = entry.getKey(); int qty = entry.getValue();
                long subtotal = m.getHarga() * qty;
                total += subtotal;

                JPanel itemRow = new JPanel(new BorderLayout(10, 0)); 
                itemRow.setOpaque(false); 
                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                itemRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BG_MAIN),
                    new EmptyBorder(10, 5, 10, 5)
                ));
                
                JLabel info = new JLabel("<html><b>" + m.getNama() + "</b><br><font color='#6c757d'>" + String.format("Rp %,.0f", (double) subtotal) + "</font></html>");
                itemRow.add(info, BorderLayout.CENTER);
                
                JPanel qtyControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                qtyControl.setOpaque(false);
                
                JButton btnMin = new JButton("-"); btnMin.setMargin(new Insets(2, 6, 2, 6)); btnMin.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnMin.addActionListener(e -> kurangiDariKeranjang(m));
                
                JLabel lblQty = new JLabel(String.valueOf(qty), SwingConstants.CENTER);
                lblQty.setFont(Theme.FONT_BODY_BOLD); lblQty.setPreferredSize(new Dimension(25, 25));
                
                JButton btnPlus = new JButton("+"); btnPlus.setMargin(new Insets(2, 6, 2, 6)); btnPlus.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnPlus.addActionListener(e -> tambahKeKeranjang(m));
                
                qtyControl.add(btnMin); qtyControl.add(lblQty); qtyControl.add(btnPlus);
                itemRow.add(qtyControl, BorderLayout.EAST);

                cartItemsPanel.add(itemRow);
            }
        }

        lblTotal.setText(String.format("Rp %,.0f", (double) total));
        hitungKembalian();
        cartItemsPanel.revalidate(); cartItemsPanel.repaint();
    }

    private long getTotalHarga() {
        return keranjang.entrySet().stream().mapToLong(e -> e.getKey().getHarga() * e.getValue()).sum();
    }

    /**
     * Logika hitung nominal kembalian dengan pengaman String Regex dari input non-angka.
     */
    private void hitungKembalian() {
        try {
            long uang = Long.parseLong(txtUang.getText().replaceAll("[^0-9]", ""));
            long total = getTotalHarga();
            long kembali = uang - total;
            
            if (kembali < 0) {
                lblKembali.setText("Kurang!");
                lblKembali.setForeground(Theme.DANGER);
            } else {
                lblKembali.setText(String.format("Rp %,.0f", (double) kembali));
                lblKembali.setForeground(new Color(40, 167, 69)); 
            }
        } catch (Exception e) { 
            lblKembali.setText("Rp 0"); 
            lblKembali.setForeground(Theme.TEXT_DARK);
        }
    }

    /**
     * Validasi kelayakan sebelum menyimpan data transaksi ke database lokal.
     */
    private void checkout() {
        if (keranjang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang pesanan masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE); return;
        }

        String metode = (String) cbMetode.getSelectedItem();
        long uangT = 0;
        
        if (metode.equals("Cash")) {
            try { uangT = Long.parseLong(txtUang.getText().replaceAll("[^0-9]", "")); } catch (Exception e) {}
            if (uangT < getTotalHarga()) {
                JOptionPane.showMessageDialog(this, "Uang tunai kurang dari total tagihan!", "Pembayaran Gagal", JOptionPane.ERROR_MESSAGE); return;
            }
            prosesSimpanPesanan(metode, uangT);
        } else if (metode.equals("QRIS")) {
            prosesSimpanPesanan(metode, getTotalHarga()); // QRIS otomatis terbayar pas sesuai jumlah tagihan
        }
    }

    /**
     * Memotong jumlah kapasitas stok produk, merangkai string ringkasan, 
     * dan mendaftarkan object transaksi baru ke DataManager.
     */
    private void prosesSimpanPesanan(String metode, long uangT) {
        StringBuilder rincian = new StringBuilder(); 
        int totalCup = 0;
        
        for (Map.Entry<Menu, Integer> entry : keranjang.entrySet()) {
            Menu m = entry.getKey();
            int qtyYangDibeli = entry.getValue();
            
            rincian.append(qtyYangDibeli).append("x ").append(m.getNama()).append("\n");
            totalCup += qtyYangDibeli;
            
            // Mengurangi kapasitas stok fisik komoditas produk cafe di database memori
            int stokBaru = m.getStok() - qtyYangDibeli;
            m.setStok(Math.max(0, stokBaru)); 
        }

        String namaPelangganOtomatis = "Order #" + currentTrxId;
        Pesanan p = new Pesanan(currentTrxId, namaPelangganOtomatis, rincian.toString().trim(), getTotalHarga(), totalCup, metode, uangT);
        
        DataManager.getInstance().tambahPesananBaru(p);
        DataManager.getInstance().simpanSemua(); 

        JOptionPane.showMessageDialog(this, "Pesanan #" + currentTrxId + " sukses dibuat!\nSilakan cek tab Antrian.", "Checkout Berhasil", JOptionPane.INFORMATION_MESSAGE);
        
        keranjang.clear(); 
        txtUang.setText(""); 
        
        // --- PROSES PERPINDAHAN KONTER URUTAN NOTA ---
        trxSequence++;
        if (trxSequence > 999) trxSequence = 1; // Melakukan reset kembali ke angka 001 jika menyentuh angka 999
        
        generateNewTrxId(); 
        updateCartUI();
        refreshMenu(); 
    }
}