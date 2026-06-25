package ui;

import struktur.DataManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Kelas LoginPanel mengelola form gerbang otentikasi masuk karyawan cafe.
 * Menyediakan isian username, sandi masking, serta dialog modal pop-up kustom 
 * untuk pendaftaran (registrasi) akun baru karyawan berklasifikasi hak akses Multi-Role.
 */
public class LoginPanel extends JPanel {
    private MainFrame frame;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout()); // Memanfaatkan GridBagLayout agar posisi kotak login sentral di tengah monitor
        setBackground(Theme.BLUE_PRIMARY); 

        // Komponen Panel berbentuk wadah kartu melengkung (Card UI Container)
        JPanel card = new JPanel() {
            @Override 
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 500));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel lblLogo = new JLabel("<html><div style='text-align: center;'><b style='color:#004AAD; font-size:32px;'>RAINAFA</b><br><b style='color:#004AAD; font-size:32px;'>COFFEE</b></div></html>", SwingConstants.CENTER);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setMaximumSize(new Dimension(320, 100)); 
        card.add(lblLogo);
        card.add(Box.createVerticalStrut(30));

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(Theme.FONT_BODY_BOLD);
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblUser);
        card.add(Box.createVerticalStrut(5));

        txtUsername = Theme.modernInput();
        txtUsername.setMaximumSize(new Dimension(320, 42));
        txtUsername.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(15));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(Theme.FONT_BODY_BOLD);
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblPass);
        card.add(Box.createVerticalStrut(5));

        txtPassword = new JPasswordField();
        txtPassword.setFont(Theme.FONT_BODY);
        txtPassword.setHorizontalAlignment(SwingConstants.CENTER);
        txtPassword.setMaximumSize(new Dimension(320, 42));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR), new EmptyBorder(8, 12, 8, 12)
        ));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(30));

        JButton btnLogin = Theme.primaryButton("Masuk");
        btnLogin.setMaximumSize(new Dimension(320, 45));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> prosesLogin());
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(15));

        JLabel lblLupa = new JLabel("Lupa Password? Hubungi Admin");
        lblLupa.setFont(Theme.FONT_SMALL); lblLupa.setForeground(Theme.TEXT_MUTED);
        lblLupa.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblLupa);
        card.add(Box.createVerticalStrut(10));

        JButton btnRegister = new JButton("<html><u>Daftar Akun Baru</u></html>");
        btnRegister.setFont(Theme.FONT_BODY_BOLD); btnRegister.setForeground(Theme.BLUE_PRIMARY);
        btnRegister.setContentAreaFilled(false); btnRegister.setBorderPainted(false); btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> tampilkanPopUpRegister());
        card.add(btnRegister);

        add(card);
    }

    /**
     * Membaca masukan kredensial user, membandingkannya dengan data akun terdaftar, 
     * lalu melempar parameter hak akses menuju kendali utama MainFrame jika valid.
     */
    private void prosesLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.util.Map<String, String[]> data = DataManager.getInstance().getSemuaAkun();
        
        // Memeriksa keabsahan password terenkripsi dari map lokal
        if (data.containsKey(user) && data.get(user)[0].equals(pass)) {
            String role = data.get(user)[1]; // Ekstraksi data kolom Role pengguna (Index 1)
            txtUsername.setText("");
            txtPassword.setText("");
            frame.loginSukses(role); // Oper kendali kemudi navigasi ke MainFrame
        } else {
            JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Gagal Login", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Memunculkan modal dialog bertingkat (JDialog Modal) untuk meregistrasikan 
     * data identitas kredensial karyawan baru tanpa menutup frame aplikasi utama.
     */
    private void tampilkanPopUpRegister() {
        JDialog dialog = new JDialog(frame, "Pendaftaran Akun Karyawan", true);
        dialog.setSize(380, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(25, 30, 25, 30));
        p.setBackground(Theme.BG_WHITE);

        JLabel title = new JLabel("Registrasi Akun");
        title.setFont(Theme.FONT_H1); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title); p.add(Box.createVerticalStrut(20));

        p.add(Theme.fieldLabel("Username Baru"));
        JTextField txtU = Theme.modernInput(); txtU.setMaximumSize(new Dimension(320, 40));
        p.add(txtU); p.add(Box.createVerticalStrut(15));

        p.add(Theme.fieldLabel("Password"));
        JPasswordField txtP = new JPasswordField(); txtP.setMaximumSize(new Dimension(320, 40));
        txtP.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR), new EmptyBorder(8, 12, 8, 12)));
        p.add(txtP); p.add(Box.createVerticalStrut(15));

        p.add(Theme.fieldLabel("Hak Akses (Role)"));
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"Kasir", "Admin"});
        cbRole.setMaximumSize(new Dimension(320, 40)); cbRole.setBackground(Color.WHITE);
        p.add(cbRole); p.add(Box.createVerticalStrut(30));

        JButton btnSimpan = Theme.primaryButton("Daftar Sekarang");
        btnSimpan.setMaximumSize(new Dimension(320, 45));
        btnSimpan.addActionListener(e -> {
            String u = txtU.getText().trim();
            String pwd = new String(txtP.getPassword());
            String r = (String) cbRole.getSelectedItem();

            if(u.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Semua field wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validasi duplikasi key username terdaftar untuk mencegah tabrakan data (Data Collision)
            if(DataManager.getInstance().getSemuaAkun().containsKey(u)) {
                JOptionPane.showMessageDialog(dialog, "Username sudah terdaftar! Pilih yang lain.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DataManager.getInstance().simpanAkun(u, pwd, r); // Menyimpan permanen ke file teks via DataManager
            JOptionPane.showMessageDialog(dialog, "Akun " + r + " berhasil didaftarkan!\nSilakan login menggunakan akun tersebut.");
            dialog.dispose();
        });
        p.add(btnSimpan);

        dialog.add(p, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}