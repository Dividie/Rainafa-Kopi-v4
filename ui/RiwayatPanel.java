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
 * Menyediakan komponen pencarian teks (Real-time Filter).
 */
public class RiwayatPanel extends JPanel {
    private JTable tabel;
    private DefaultTableModel modelTabel;
    private TableRowSorter<DefaultTableModel> sorter; // Penanggung jawab filter baris tabel
    
    private JTextField txtCari;

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

        JLabel lblCari = new JLabel("Cari:");
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
     * Memproses logika penyaringan data berdasarkan keyword teks di kolom Cari.
     */
    private void filterData() {
        String keyword = txtCari.getText().trim();
        
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null); // Membersihkan filter jika kolom pencarian kosong
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword)); // Filter case-insensitive
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