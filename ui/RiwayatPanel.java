package ui;

import model.Pesanan;
import struktur.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList; 
import java.util.List;      

/**
 * Kelas RiwayatPanel mengelola tampilan tabel log arsip seluruh data transaksi penjualan.
 * Menyediakan komponen pencarian teks (Real-time Filter) dan filter kategori status (Lunas / Batal).
 */
public class RiwayatPanel extends JPanel {
    private JTable tabel;
    private DefaultTableModel modelTabel;
    private TableRowSorter<DefaultTableModel> sorter; // Penanggung jawab filter baris tabel
    
    private JTextField txtCari;
    private JComboBox<String> cbFilterStatus;

    public RiwayatPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- MERAKIT HEADER PANEL ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(Theme.pageHeader("Riwayat Transaksi", "Daftar seluruh laporan rekam jejak penjualan."), BorderLayout.WEST);
        
        JPanel kananHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        kananHeaderPanel.setOpaque(false);
        kananHeaderPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(Theme.FONT_BODY_BOLD);
        lblFilter.setForeground(Theme.TEXT_MUTED);
        
        cbFilterStatus = new JComboBox<>(new String[]{"Semua", "Lunas", "Batal"});
        cbFilterStatus.setPreferredSize(new Dimension(110, 40));
        cbFilterStatus.setBackground(Color.WHITE);
        cbFilterStatus.setFont(Theme.FONT_BODY);
        cbFilterStatus.addActionListener(e -> filterData());

        JLabel lblCari = new JLabel("🔍 Cari:");
        lblCari.setFont(Theme.FONT_BODY_BOLD);
        lblCari.setForeground(Theme.TEXT_MUTED);
        
        txtCari = Theme.modernInput();
        txtCari.setPreferredSize(new Dimension(220, 40));
        
        // Listener untuk mendeteksi setiap ketikan huruf di kolom cari secara real-time
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        
        kananHeaderPanel.add(lblFilter);
        kananHeaderPanel.add(cbFilterStatus);
        kananHeaderPanel.add(lblCari);
        kananHeaderPanel.add(txtCari);
        
        topPanel.add(kananHeaderPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        add(initTableCard(), BorderLayout.CENTER);
        refresh();
    }

    /**
     * Menginisialisasi komponen JTable dengan desain custom zebra-row dan pewarnaan status.
     */
    private JPanel initTableCard() {
        JPanel tableCard = Theme.modernCard();
        tableCard.setLayout(new BorderLayout());

        String[] kolom = {"ID Transaksi", "Keterangan Pesanan", "Metode", "Total Pendapatan", "Status"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) { return false; } // Mengunci sel agar tidak bisa diedit user
        };
        
        tabel = new JTable(modelTabel);
        sorter = new TableRowSorter<>(modelTabel);
        tabel.setRowSorter(sorter);
        
        tabel.setFont(Theme.FONT_BODY);
        tabel.setRowHeight(42); 
        tabel.setShowVerticalLines(false);
        tabel.setGridColor(Theme.BORDER_COLOR);
        tabel.setSelectionBackground(new Color(230, 240, 255));
        tabel.setSelectionForeground(Theme.TEXT_DARK);

        JTableHeader header = tabel.getTableHeader();
        header.setFont(Theme.FONT_BODY_BOLD);
        header.setBackground(Color.WHITE);
        header.setForeground(Theme.TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 45));

        // Custom Cell Renderer untuk mengatur perataan kolom dan warna teks baris secara kondisional
        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Efek Zebra: Mewarnai abu-abu tipis pada indeks baris ganjil
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.BG_MAIN); 
                }
                
                // Styling spesifik kolom
                if (column == 0) { 
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(Theme.FONT_BODY_BOLD);
                    c.setForeground(Theme.BLUE_PRIMARY);
                } else if (column == 3) { 
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setFont(Theme.FONT_BODY_BOLD);
                    c.setForeground(Theme.TEXT_DARK);
                } else if (column == 4) { 
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(Theme.FONT_BODY_BOLD);
                    String status = value.toString().toUpperCase();
                    if (status.equals("LUNAS")) {
                        c.setForeground(new Color(40, 167, 69)); // Hijau untuk transaksi sukses
                    } else if (status.equals("BATAL")) {
                        c.setForeground(Theme.DANGER); // Merah untuk pembatalan
                    } else {
                        c.setForeground(new Color(133, 100, 4)); // Cokelat/Kuning untuk Belum Bayar
                    }
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(Theme.FONT_BODY);
                    c.setForeground(Theme.TEXT_DARK);
                }
                
                setBorder(new EmptyBorder(0, 15, 0, 15)); 
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        return tableCard;
    }

    /**
     * Memproses logika penyaringan gabungan (AndFilter) antara keyword teks dan JComboBox.
     */
    private void filterData() {
        String keyword = txtCari.getText().trim();
        String statusFilter = (String) cbFilterStatus.getSelectedItem();
        
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        
        // Menambahkan filter teks jika tidak kosong (case-insensitive)
        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + keyword));
        }
        
        // Menambahkan filter status pada kolom ke-4 jika bernilai selain "Semua"
        if (!statusFilter.equals("Semua")) {
            filters.add(RowFilter.regexFilter("(?i)^" + statusFilter + "$", 4)); 
        }
        
        if (filters.isEmpty()) {
            sorter.setRowFilter(null); // Membersihkan filter jika kosong
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    /**
     * Membaca ulang seluruh data transaksi dari struktur memori DataManager, 
     * lalu menampilkannya dengan urutan terbalik (Transaksi terbaru berada di baris teratas).
     */
    public void refresh() {
        modelTabel.setRowCount(0);
        
        // Menyalin isi Queue antrian ke ArrayList agar dapat dieksplorasi menggunakan perulangan indeks mundur
        List<Pesanan> listPesanan = new ArrayList<>(DataManager.getInstance().getQueue().getAntrian());
        
        for (int i = listPesanan.size() - 1; i >= 0; i--) {
            Pesanan p = listPesanan.get(i);
            
            // Meratakan karakter baris baru rincian menu menjadi satu kalimat lurus dipisah koma
            String rincianLurus = p.getRincianMenu().replace("\n", ", ");
            
            modelTabel.addRow(new Object[]{
                p.getId(),
                rincianLurus,
                p.getMetodePembayaran(),
                "Rp " + String.format("%,.0f", (double) p.getTotalHarga()),
                p.getStatus() 
            });
        }
    }
}