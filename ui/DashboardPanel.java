package ui;

import model.Menu;
import model.Pesanan;
import struktur.DataManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {
    private MainFrame frame;
    private JLabel lblCup, lblPendapatan, lblAntrian;
    private JTable tabelAntrian;
    private DefaultTableModel modelAntrian;
    private JPanel pnlStokList;

    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(Theme.pageHeader("Dashboard", "Ringkasan performa Rainafa Coffee."), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setOpaque(false);

        // 1. TIGA KARTU SUMMARY
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsRow.setOpaque(false);
        cardsRow.setPreferredSize(new Dimension(0, 110));

        lblCup = new JLabel("0"); lblPendapatan = new JLabel("Rp 0"); lblAntrian = new JLabel("0");
        cardsRow.add(buildSummaryCard("Cup Terjual", lblCup, new Color(40, 167, 69)));
        cardsRow.add(buildSummaryCard("Pendapatan", lblPendapatan, Theme.BLUE_PRIMARY));
        cardsRow.add(buildSummaryCard("Antrian", lblAntrian, Theme.TEXT_MUTED));
        centerPanel.add(cardsRow, BorderLayout.NORTH);

        // 2. KOTAK ANTRIAN & STOK MENU 
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomRow.setOpaque(false);

        // --- KOTAK KIRI: Tabel Antrian ---
        JPanel pnlAntrian = Theme.modernCard();
        pnlAntrian.setLayout(new BorderLayout(0, 15));
        JLabel titleAntrian = new JLabel("Antrian Pembayaran Aktif"); 
        titleAntrian.setFont(Theme.FONT_H2);
        pnlAntrian.add(titleAntrian, BorderLayout.NORTH);
        
        String[] kolomAntrian = {"ID Transaksi", "Pelanggan", "Pesanan", "Status"};
        modelAntrian = new DefaultTableModel(kolomAntrian, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        tabelAntrian = new JTable(modelAntrian);
        tabelAntrian.setFont(Theme.FONT_BODY);
        tabelAntrian.setRowHeight(38);
        tabelAntrian.setShowVerticalLines(false);
        tabelAntrian.setGridColor(Theme.BORDER_COLOR);
        tabelAntrian.setSelectionBackground(new Color(230, 240, 255));
        tabelAntrian.setSelectionForeground(Theme.TEXT_DARK);

        JTableHeader headerAntrian = tabelAntrian.getTableHeader();
        headerAntrian.setFont(Theme.FONT_SMALL);
        headerAntrian.setBackground(Color.WHITE);
        headerAntrian.setForeground(Theme.TEXT_MUTED);
        headerAntrian.setPreferredSize(new Dimension(0, 35));

        tabelAntrian.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.BG_MAIN); 
                setBorder(new EmptyBorder(0, 10, 0, 10));
                
                if (column == 3) { 
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(Theme.FONT_BODY_BOLD);
                    c.setForeground(new Color(133, 100, 4)); 
                } else if (column == 0) { 
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(Theme.FONT_BODY_BOLD);
                    c.setForeground(Theme.BLUE_PRIMARY);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(Theme.FONT_BODY);
                    c.setForeground(Theme.TEXT_DARK);
                }
                return c;
            }
        });

        JScrollPane scrollAntrian = new JScrollPane(tabelAntrian);
        scrollAntrian.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        scrollAntrian.getViewport().setBackground(Color.WHITE);
        pnlAntrian.add(scrollAntrian, BorderLayout.CENTER);
        
        // PERBAIKAN: Menggunakan this.frame agar peringatan "not used" hilang
        JButton btnKeAntrian = Theme.secondaryButton("Lihat Semua Antrian");
        btnKeAntrian.addActionListener(e -> this.frame.pindahKeMenu("Antrian"));
        pnlAntrian.add(btnKeAntrian, BorderLayout.SOUTH);

        // --- KOTAK KANAN: Peringatan Stok Menu ---
        JPanel pnlStok = Theme.modernCard();
        pnlStok.setLayout(new BorderLayout(0, 15));
        JLabel titleStok = new JLabel("Peringatan Stok Menu Terendah"); 
        titleStok.setFont(Theme.FONT_H2);
        pnlStok.add(titleStok, BorderLayout.NORTH);
        
        pnlStokList = new JPanel();
        pnlStokList.setLayout(new BoxLayout(pnlStokList, BoxLayout.Y_AXIS));
        pnlStokList.setOpaque(false);
        
        JScrollPane scrollStok = new JScrollPane(pnlStokList);
        scrollStok.setBorder(null);
        scrollStok.getViewport().setOpaque(false);
        scrollStok.setOpaque(false);
        pnlStok.add(scrollStok, BorderLayout.CENTER);

        // PERBAIKAN: Menggunakan this.frame agar peringatan "not used" hilang
        JButton btnKeMenu = Theme.secondaryButton("Kelola Menu");
        btnKeMenu.addActionListener(e -> this.frame.pindahKeMenu("Menu"));
        pnlStok.add(btnKeMenu, BorderLayout.SOUTH);

        bottomRow.add(pnlAntrian);
        bottomRow.add(pnlStok);
        centerPanel.add(bottomRow, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildSummaryCard(String title, JLabel valueLabel, Color color) {
        JPanel card = Theme.modernCard();
        card.setLayout(new BorderLayout());
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(color);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_BODY);
        lblTitle.setForeground(Theme.TEXT_MUTED);
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        DataManager dm = DataManager.getInstance();
        
        // PERBAIKAN: Mengonversi Queue menjadi ArrayList (List)
        List<Pesanan> semuaPesanan = new ArrayList<>(dm.getQueue().getAntrian());
        
        long totalPendapatan = 0;
        int totalCup = 0;
        int jumlahAntrian = 0;

        modelAntrian.setRowCount(0);
        
        // --- LOGIKA LIVE CALCULATION ---
        for (Pesanan p : semuaPesanan) {
            String status = p.getStatus() != null ? p.getStatus().toUpperCase() : "";
            
            if (status.equals("LUNAS")) {
                totalPendapatan += p.getTotalHarga();
                
                try {
                    String[] barisMenu = p.getRincianMenu().split("\n");
                    for (String baris : barisMenu) {
                        if (baris.contains("x ")) {
                            String qty = baris.substring(0, baris.indexOf("x")).trim();
                            totalCup += Integer.parseInt(qty);
                        }
                    }
                } catch (Exception e) {
                    // Abaikan jika format rincian tidak biasa
                }
            } 
            else if (status.equals("BELUM BAYAR")) {
                jumlahAntrian++;
                
                String rincian = p.getRincianMenu().replace("\n", ", ");
                if (rincian.length() > 30) {
                    rincian = rincian.substring(0, 27) + "...";
                }
                modelAntrian.addRow(new Object[]{ p.getId(), p.getNamaPelanggan(), rincian, "Belum Bayar" });
            }
        }
        
        lblCup.setText(String.valueOf(totalCup));
        lblPendapatan.setText(String.format("Rp %,.0f", (double) totalPendapatan));
        lblAntrian.setText(String.valueOf(jumlahAntrian));

        // --- REFRESH DAFTAR STOK MENU ---
        pnlStokList.removeAll();
        
        List<Menu> semuaMenu = new ArrayList<>(dm.getSemuaMenu());
        semuaMenu.sort((m1, m2) -> Integer.compare(m1.getStok(), m2.getStok()));
        
        int count = 0;
        for (Menu m : semuaMenu) {
            if (count >= 6) break; 
            
            String nama = m.getNama();
            int currentStok = m.getStok();
            
            int maxVisual = 50; 
            double percentage = Math.max(0, Math.min(1, (double) currentStok / maxVisual));

            JPanel rowWrapper = new JPanel(new BorderLayout());
            rowWrapper.setOpaque(false);
            rowWrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));

            JPanel row = new JPanel(new BorderLayout(0, 8)); 
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(12, 5, 12, 5));

            JPanel textRow = new JPanel(new BorderLayout()); 
            textRow.setOpaque(false);
            
            JLabel lblNama = new JLabel(nama); 
            lblNama.setFont(Theme.FONT_BODY_BOLD);
            lblNama.setForeground(Theme.TEXT_DARK);
            
            JLabel lblNilai = new JLabel(currentStok + " Porsi");
            lblNilai.setFont(Theme.FONT_SMALL);
            lblNilai.setForeground(Theme.TEXT_MUTED);
            
            textRow.add(lblNama, BorderLayout.WEST); 
            textRow.add(lblNilai, BorderLayout.EAST);
            row.add(textRow, BorderLayout.NORTH);

            Color barColor = currentStok < 10 ? Theme.DANGER : Theme.BLUE_PRIMARY;
            
            JPanel progressBar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int h = 6; 
                    int y = (getHeight() - h) / 2;
                    
                    g2.setColor(Theme.BORDER_COLOR);
                    g2.fill(new RoundRectangle2D.Float(0, y, getWidth(), h, h, h));
                    
                    g2.setColor(barColor);
                    int fillWidth = (int) (getWidth() * percentage);
                    g2.fill(new RoundRectangle2D.Float(0, y, fillWidth, h, h, h));
                    
                    g2.dispose();
                }
            };
            progressBar.setPreferredSize(new Dimension(100, 12));
            progressBar.setOpaque(false);
            row.add(progressBar, BorderLayout.CENTER);

            rowWrapper.add(row, BorderLayout.CENTER);
            pnlStokList.add(rowWrapper);
            count++;
        }

        revalidate(); repaint();
    }
}