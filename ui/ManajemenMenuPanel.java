package ui;

import model.Menu;
import struktur.DataManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;

/**
 * Kelas ManajemenMenuPanel memfasilitasi antarmuka CRUD (Create, Read, Update, Delete) data produk.
 * Admin dapat memanipulasi informasi nama, kategori, harga, kuantitas stok barang, serta melampirkan berkas foto menu.
 */
public class ManajemenMenuPanel extends JPanel {
    private JTable tabel;
    private DefaultTableModel modelTabel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private JTextField txtNama, txtHarga, txtStok, txtCari;
    private JComboBox<String> cbKategori;
    private String editKode = ""; // Menyimpan penanda Primary Key kode menu yang sedang disunting
    private JLabel lblStatusForm;
    
    private JLabel lblImagePreview;
    private File selectedImageFile = null;

    public ManajemenMenuPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(Theme.pageHeader("Manajemen Menu", "Kelola data produk, harga, ketersediaan stok, dan foto."), BorderLayout.WEST);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); 
        
        JLabel lblCari = new JLabel("Cari:");
        lblCari.setFont(Theme.FONT_BODY_BOLD);
        lblCari.setForeground(Theme.TEXT_MUTED);
        
        txtCari = Theme.modernInput();
        txtCari.setPreferredSize(new Dimension(250, 40)); 
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTabel(); }
            public void removeUpdate(DocumentEvent e) { filterTabel(); }
            public void changedUpdate(DocumentEvent e) { filterTabel(); }
        });
        
        searchPanel.add(lblCari);
        searchPanel.add(txtCari);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(initTablePanel(), BorderLayout.CENTER);
        centerPanel.add(initFormPanel(), BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
        refresh();
    }

    private JPanel initTablePanel() {
        JPanel tableCard = Theme.modernCard();
        tableCard.setLayout(new BorderLayout(0, 15));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Daftar Menu Aktif");
        lblTitle.setFont(Theme.FONT_H2);
        tableHeader.add(lblTitle, BorderLayout.WEST);

        String[] kolom = {"Kode", "Nama Menu", "Kategori", "Harga", "Stok"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        tabel = new JTable(modelTabel);
        sorter = new TableRowSorter<>(modelTabel);
        tabel.setRowSorter(sorter); 
        
        tabel.setFont(Theme.FONT_BODY);
        tabel.setRowHeight(40);
        tabel.setShowVerticalLines(false);
        tabel.setGridColor(Theme.BORDER_COLOR);
        tabel.setSelectionBackground(new Color(230, 240, 255));
        tabel.setSelectionForeground(Theme.TEXT_DARK);

        JTableHeader header = tabel.getTableHeader();
        header.setFont(Theme.FONT_BODY_BOLD);
        header.setBackground(Color.WHITE);
        header.setForeground(Theme.TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 45));

        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.BG_MAIN); 
                
                // Menyuntikkan warna teks merah bahaya jika kuantitas kapasitas stok menipis (< 10)
                if (column == 4) { 
                    int stok = Integer.parseInt(value.toString());
                    c.setForeground(stok < 10 ? Theme.DANGER : Theme.BLUE_PRIMARY);
                    setFont(Theme.FONT_BODY_BOLD);
                } else {
                    c.setForeground(Theme.TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Memicu pengisian isian form sisi kanan ketika ada salah satu baris tabel yang dipilih kursor user
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        
        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        return tableCard;
    }

    private void filterTabel() {
        String text = txtCari.getText().trim();
        if (text.length() == 0) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }

    /**
     * Merakit isian panel formulir data entitas menu baru / sunting data lama.
     */
    private JPanel initFormPanel() {
        JPanel formCard = Theme.modernCard();
        formCard.setPreferredSize(new Dimension(380, 0));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JPanel headerForm = new JPanel(new BorderLayout());
        headerForm.setOpaque(false);
        headerForm.setMaximumSize(new Dimension(350, 40));
        headerForm.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        lblStatusForm = new JLabel("Tambah Menu Baru");
        lblStatusForm.setFont(Theme.FONT_H2);
        lblStatusForm.setForeground(Theme.BLUE_PRIMARY);
        
        JButton btnReset = Theme.secondaryButton("Reset");
        btnReset.setFont(Theme.FONT_SMALL);
        btnReset.addActionListener(e -> resetForm());

        headerForm.add(lblStatusForm, BorderLayout.WEST);
        headerForm.add(btnReset, BorderLayout.EAST);
        formCard.add(headerForm);
        formCard.add(Box.createVerticalStrut(15));

        // Sub-Modul Unggah Gambar/Foto Produk
        JPanel photoPanel = new JPanel(new BorderLayout(15, 0));
        photoPanel.setOpaque(false);
        photoPanel.setMaximumSize(new Dimension(350, 80));
        photoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblImagePreview = new JLabel("Tak Ada Foto", SwingConstants.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(80, 80));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setBackground(Theme.BG_MAIN);
        lblImagePreview.setForeground(Theme.TEXT_MUTED);

        JButton btnUpload = Theme.secondaryButton("Pilih Foto...");
        btnUpload.addActionListener(e -> pilihFoto());

        photoPanel.add(lblImagePreview, BorderLayout.WEST);
        photoPanel.add(btnUpload, BorderLayout.CENTER);

        formCard.add(Theme.fieldLabel("Foto Menu (Opsional)"));
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(photoPanel);
        formCard.add(Box.createVerticalStrut(15));

        formCard.add(Theme.fieldLabel("Nama Menu"));
        formCard.add(Box.createVerticalStrut(5));
        txtNama = Theme.modernInput();
        txtNama.setMaximumSize(new Dimension(350, 40));
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(txtNama);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(Theme.fieldLabel("Kategori"));
        formCard.add(Box.createVerticalStrut(5));
        cbKategori = new JComboBox<>(new String[]{"Coffee", "Non-Coffee", "Signature Coffe"});
        cbKategori.setMaximumSize(new Dimension(350, 40));
        cbKategori.setBackground(Color.WHITE);
        cbKategori.setFont(Theme.FONT_BODY);
        cbKategori.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(cbKategori);
        formCard.add(Box.createVerticalStrut(10));

        JPanel rowDua = new JPanel(new GridLayout(1, 2, 15, 0));
        rowDua.setOpaque(false);
        rowDua.setMaximumSize(new Dimension(350, 65));
        rowDua.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel colHarga = new JPanel(new BorderLayout(0, 5)); colHarga.setOpaque(false);
        colHarga.add(Theme.fieldLabel("Harga (Rp)"), BorderLayout.NORTH);
        txtHarga = Theme.modernInput(); colHarga.add(txtHarga, BorderLayout.CENTER);
        
        JPanel colStok = new JPanel(new BorderLayout(0, 5)); colStok.setOpaque(false);
        colStok.add(Theme.fieldLabel("Jumlah Stok"), BorderLayout.NORTH);
        txtStok = Theme.modernInput(); colStok.add(txtStok, BorderLayout.CENTER);
        
        rowDua.add(colHarga); rowDua.add(colStok);
        formCard.add(rowDua);
        formCard.add(Box.createVerticalStrut(20));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0)); 
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(350, 45));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSimpan = Theme.primaryButton("Simpan Data");
        btnSimpan.addActionListener(e -> simpanMenu());

        JButton btnHapus = Theme.dangerButton("Hapus Menu");
        btnHapus.addActionListener(e -> hapusMenu());

        btnPanel.add(btnSimpan); btnPanel.add(btnHapus);
        formCard.add(btnPanel);
        formCard.add(Box.createVerticalGlue()); 

        return formCard;
    }

    /**
     * Memotong dan merapikan visual gambar preview agar pas dengan rasio boks form (80x80).
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
            System.out.println("Gagal memuat gambar: " + e.getMessage());
            return null;
        }
    }

    private void pilihFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            tampilkanPreview(selectedImageFile.getAbsolutePath());
        }
    }

    private void tampilkanPreview(String path) {
        Image img = autofitImage(path, 80, 80);
        if (img != null) {
            lblImagePreview.setIcon(new ImageIcon(img));
            lblImagePreview.setText("");
        } else {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Error Load");
        }
    }

    /**
     * Memindahkan isi baris tabel terpilih menuju kotak textfield isian form (Logika Edit Mode).
     */
    private void populateFormFromTable() {
        int viewRow = tabel.getSelectedRow();
        if (viewRow < 0) return;
        
        int modelRow = tabel.convertRowIndexToModel(viewRow);
        editKode = (String) modelTabel.getValueAt(modelRow, 0);
        Menu m = DataManager.getInstance().getMenuMap().getMenu(editKode);
        
        if (m != null) {
            lblStatusForm.setText("Edit: " + m.getKode());
            txtNama.setText(m.getNama());
            cbKategori.setSelectedItem(m.getKategori());
            txtHarga.setText(String.valueOf(m.getHarga()));
            txtStok.setText(String.valueOf(m.getStok()));
            
            File imgFile = new File("images/" + m.getKode() + ".jpg");
            if (imgFile.exists()) {
                tampilkanPreview(imgFile.getAbsolutePath());
            } else {
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Tak Ada Foto");
            }
            selectedImageFile = null; 
        }
    }

    private void resetForm() {
        tabel.clearSelection();
        editKode = "";
        lblStatusForm.setText("Tambah Menu Baru");
        txtNama.setText("");
        cbKategori.setSelectedIndex(0);
        txtHarga.setText("");
        txtStok.setText("");
        txtCari.setText(""); 
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("Tak Ada Foto");
        selectedImageFile = null;
    }

    /**
     * Membaca seluruh data input form, memvalidasi isian angka, 
     * lalu memutuskan operasi Create (Jika editKode kosong) atau Update (Jika berisi id menu).
     */
    private void simpanMenu() {
        try {
            String n = txtNama.getText().trim();
            String hStr = txtHarga.getText().trim().replaceAll("[^0-9]", "");
            String sStr = txtStok.getText().trim().replaceAll("[^0-9]", "");
            String kat = (String) cbKategori.getSelectedItem();

            if (n.isEmpty() || hStr.isEmpty() || sStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama, Harga, dan Stok wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            long h = Long.parseLong(hStr);
            int s = Integer.parseInt(sStr);
            String kodeFinal = editKode;

            if (editKode.isEmpty()) { 
                // Operasi Pembuatan Menu Baru (Create)
                kodeFinal = "M" + (System.currentTimeMillis() % 10000); // Generate kode acak unik berbasis timestamp 4 digit belakang
                DataManager.getInstance().getMenuMap().tambahMenu(new Menu(kodeFinal, n, kat, h, s));
            } else { 
                // Operasi Pembaruan Data Menu Lama (Update)
                Menu existing = DataManager.getInstance().getMenuMap().getMenu(editKode);
                existing.setNama(n);
                existing.setKategori(kat);
                existing.setHarga(h);
                existing.setStok(s);
            }

            // Menyimpan berkas salinan gambar/foto produk ke folder 'images/[Kode_Menu].jpg'
            if (selectedImageFile != null) {
                try {
                    File imgDir = new File("images");
                    if (!imgDir.exists()) imgDir.mkdir(); 
                    File dest = new File(imgDir, kodeFinal + ".jpg");
                    Files.copy(selectedImageFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ex) {
                    System.out.println("Gagal menyimpan foto: " + ex.getMessage());
                }
            }

            DataManager.getInstance().simpanSemua();
            resetForm();
            refresh();
            JOptionPane.showMessageDialog(this, "Data menu berhasil disimpan!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Input Harga dan Stok harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusMenu() {
        if (editKode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih menu dari tabel terlebih dahulu untuk dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus menu ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            File imgFile = new File("images/" + editKode + ".jpg");
            if (imgFile.exists()) imgFile.delete(); // Menghapus file gambar lokal produk bersangkutan

            DataManager.getInstance().getMenuMap().hapusMenu(editKode);
            DataManager.getInstance().simpanSemua();
            resetForm();
            refresh();
        }
    }

    public void refresh() {
        modelTabel.setRowCount(0);
        for (Menu m : DataManager.getInstance().getSemuaMenu()) {
            modelTabel.addRow(new Object[]{
                m.getKode(), m.getNama(), m.getKategori(), m.getHargaFormatted(), m.getStok()
            });
        }
    }
}