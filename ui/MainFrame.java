package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * Kelas MainFrame bertindak sebagai Container/Window utama aplikasi (JFrame).
 * Menggunakan CardLayout untuk mengatur perpindahan antar halaman utama (Login vs Aplikasi Utama),
 * serta mengontrol hak akses menu berdasarkan role pengguna (Admin / Kasir).
 */
public class MainFrame extends JFrame {
    private JPanel rootPanel;
    private CardLayout rootLayout;
    
    private JPanel kontenPanel;
    private ArrayList<JButton> menuButtons = new ArrayList<>();
    private String currentRole = "";

    // Deklarasi komponen halaman (Panel)
    private DashboardPanel dashboardPanel;
    private ManajemenMenuPanel manajemenMenuPanel;
    private KasirPanel kasirPanel;
    private AntrianPanel antrianPanel;
    private RiwayatPanel riwayatPanel;
    private AkunPanel akunPanel;

    /**
     * Konstruktor utama MainFrame. Mengonfigurasi properti dasar window 
     * dan mendaftarkan panel Login serta panel Utama ke CardLayout.
     */
    public MainFrame() {
        super("Rainafa Coffee - POS Modern");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null); // Memosisikan window di tengah layar komputer
        
        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);

        // Mendaftarkan halaman ke dalam CardLayout root
        rootPanel.add(new LoginPanel(this), "LOGIN");
        rootPanel.add(buatMainApp(), "APP");

        setContentPane(rootPanel);
        setVisible(true);
        rootLayout.show(rootPanel, "LOGIN"); // Tampilan awal adalah halaman login
    }

    /**
     * Dipanggil oleh LoginPanel ketika otentikasi user berhasil.
     * Mengatur hak akses sidebar dan mengarahkan user ke halaman default role-nya.
     * @param role Hak akses pengguna ("Admin" atau "Kasir").
     */
    public void loginSukses(String role) {
        this.currentRole = role;
        aturAksesSidebar(); // Sembunyikan atau tampilkan tombol menu sesuai hak akses
        rootLayout.show(rootPanel, "APP");
        
        // Pengalihan halaman default setelah login
        if (role.equals("Admin")) {
            pindahKeMenu("Dashboard");
        } else {
            pindahKeMenu("Kasir"); 
        }
    }

    /**
     * Mengembalikan tampilan aplikasi ke halaman Login awal.
     */
    public void logout() {
        rootLayout.show(rootPanel, "LOGIN");
    }

    /**
     * Berpindah kartu halaman (Card) di dalam konten utama sekaligus memicu 
     * fungsi refresh data agar informasi yang ditampilkan selalu yang terbaru.
     * @param namaMenu Nama ID kartu tujuan (Contoh: "Dashboard", "Kasir").
     */
    public void pindahKeMenu(String namaMenu) {
        setActiveMenu(namaMenu); // Mengubah visual tombol aktif di sidebar
        ((CardLayout) kontenPanel.getLayout()).show(kontenPanel, namaMenu);
        
        // Trigger penyegaran data otomatis pada panel tujuan
        if (namaMenu.equals("Dashboard")) dashboardPanel.refresh();
        if (namaMenu.equals("Menu")) manajemenMenuPanel.refresh();
        if (namaMenu.equals("Kasir")) kasirPanel.refreshMenu();
        if (namaMenu.equals("Antrian")) antrianPanel.refresh();
        if (namaMenu.equals("History")) riwayatPanel.refresh();
        if (namaMenu.equals("Akun")) akunPanel.refresh();
    }

    /**
     * Menyaring tombol menu mana saja yang boleh diklik/dilihat oleh pengguna.
     * Kasir dibatasi hanya bisa mengakses Menu, Kasir, dan Antrian.
     */
    private void aturAksesSidebar() {
        for (JButton btn : menuButtons) {
            String menuName = btn.getActionCommand();
            if (currentRole.equals("Admin")) {
                btn.setVisible(true); // Admin memiliki akses mutlak ke semua fitur
            } else if (currentRole.equals("Kasir")) {
                if (menuName.equals("Menu") || menuName.equals("Kasir") || menuName.equals("Antrian")) {
                    btn.setVisible(true);
                } else {
                    btn.setVisible(false); // Sembunyikan Dashboard, History, dan Manajemen Akun dari Kasir
                }
            }
        }
    }

    /**
     * Merakit tata letak utama aplikasi (Sidebar di sebelah kiri, Konten kartu di sebelah kanan).
     */
    private JPanel buatMainApp() {
        JPanel appPanel = new JPanel(new BorderLayout());
        appPanel.setBackground(Theme.BG_MAIN);

        // --- KONFIGURASI SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BG_WHITE);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // Pengolahan Komponen Gambar Logo Cafe
        JLabel logo = new JLabel("", SwingConstants.CENTER);
        try {
            File logoFile = new File("images/logo.jpg");
            if (!logoFile.exists()) {
                logoFile = new File("images/logo.png"); 
            }

            if (logoFile.exists()) {
                Image img = ImageIO.read(logoFile);
                // Skala gambar diperkecil menjadi lebar 160px dengan aspect ratio terkunci (-1)
                Image scaledImg = img.getScaledInstance(160, -1, Image.SCALE_SMOOTH);
                logo.setIcon(new ImageIcon(scaledImg));
            } else {
                logo.setText("<html><center><b style='color:#004AAD; font-size:18px;'>RAINAFA</b><br><b style='color:#004AAD; font-size:18px;'>COFFEE</b></center></html>");
            }
        } catch (Exception ex) {
            logo.setText("<html><center><b style='color:#004AAD; font-size:18px;'>RAINAFA</b><br><b style='color:#004AAD; font-size:18px;'>COFFEE</b></center></html>");
        }
        
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(new EmptyBorder(40, 0, 40, 0));
        sidebar.add(logo);

        // --- INISIALISASI KONTEN PANEL (CARD LAYOUT) ---
        kontenPanel = new JPanel(new CardLayout());
        kontenPanel.setBackground(Theme.BG_MAIN);

        dashboardPanel = new DashboardPanel(this);
        manajemenMenuPanel = new ManajemenMenuPanel();
        kasirPanel = new KasirPanel(this);
        antrianPanel = new AntrianPanel();
        riwayatPanel = new RiwayatPanel();
        akunPanel = new AkunPanel();

        // Mendaftarkan semua panel kerja ke dalam kontenPanel utama
        kontenPanel.add(dashboardPanel, "Dashboard");
        kontenPanel.add(manajemenMenuPanel, "Menu");
        kontenPanel.add(kasirPanel, "Kasir");
        kontenPanel.add(antrianPanel, "Antrian");
        kontenPanel.add(riwayatPanel, "History");
        kontenPanel.add(akunPanel, "Akun");

        // Membuat tombol navigasi menu secara dinamis menggunakan perulangan
        String[] menus = {"Dashboard", "Menu", "Kasir", "Antrian", "History", "Akun"};
        for (String m : menus) {
            JButton btn = createSidebarButton(m, m);
            menuButtons.add(btn);
            sidebar.add(btn);
        }

        // Perekat otomatis untuk mendorong tombol logout berada di posisi paling bawah sidebar
        sidebar.add(Box.createVerticalGlue());
        
        JButton btnLogout = createSidebarButton("Log out", "Logout");
        btnLogout.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Yakin log out?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                logout();
            }
        });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(30)); // Jarak margin terbawah

        appPanel.add(sidebar, BorderLayout.WEST);
        appPanel.add(kontenPanel, BorderLayout.CENTER);
        return appPanel;
    }

    /**
     * Kustomisasi pembuatan objek tombol sidebar dengan efek penggambaran background 2D (Hover & Aktif).
     */
    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Boolean aktif = (Boolean) getClientProperty("aktif");
                
                // Logika pewarnaan background tombol
                if (aktif != null && aktif) {
                    g2.setColor(Theme.BLUE_PRIMARY); // Biru pekat jika sedang aktif dibuka
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 0, 0, 10)); // Transparan gelap tipis saat kursor melintas (Hover)
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setActionCommand(cardName);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
        btn.setForeground(Theme.TEXT_DARK);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
        btn.setMaximumSize(new Dimension(240, 48)); 
        btn.setPreferredSize(new Dimension(240, 48));
        btn.setBorder(new EmptyBorder(0, 40, 0, 0)); // Padding teks agar menjorok ke dalam
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            if (!cardName.equals("Logout")) pindahKeMenu(cardName);
        });
        return btn;
    }

    /**
     * Memperbarui visual seluruh tombol menu di sidebar untuk menegaskan halaman yang sedang aktif.
     */
    private void setActiveMenu(String activeName) {
        for (JButton btn : menuButtons) {
            boolean isAktif = btn.getActionCommand().equals(activeName);
            btn.putClientProperty("aktif", isAktif); // Menyisipkan flag custom ke properti komponen swing
            
            if (isAktif) {
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            } else {
                btn.setForeground(Theme.TEXT_DARK);
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            }
            btn.repaint(); // Paksa penggambaran ulang komponen
        }
    }
}