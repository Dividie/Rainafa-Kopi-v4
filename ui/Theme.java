package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Kelas Theme dikonfigurasi menggunakan keyword `final` serta Constructor privat (Utility Class).
 * Menyediakan palet kode warna standar, cetakan font tipografi, serta pabrik perakit komponen UI 
 * (Button, Card, Input) dengan desain UI/UX datar modern (Flat Design).
 */
public final class Theme {
    private Theme() {} // Menghalangi instansiasi object langsung dari luar kelas

    // --- PALET WARNA FLAT UI ---
    public static final Color BLUE_PRIMARY = new Color(0, 74, 173);
    public static final Color BLUE_HOVER   = new Color(0, 90, 200);
    public static final Color BG_MAIN      = new Color(248, 249, 250);
    public static final Color BG_WHITE     = Color.WHITE;
    public static final Color TEXT_DARK    = new Color(33, 37, 41);
    public static final Color TEXT_MUTED   = new Color(108, 117, 125);
    public static final Color BORDER_COLOR = new Color(222, 226, 230); 
    public static final Color DANGER       = new Color(220, 53, 69);

    // --- TIPOGRAFI HURUF STANDARD SEGOE UI ---
    public static final Font FONT_H1       = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_H2       = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD= new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);

    public static final int RADIUS = 12; // Ukuran sudut kelengkungan kotak (Corner Radius)

    /**
     * Membuat wadah panel komponen dengan sudut kelengkungan halus kustom bergaris tepi tipis elegan.
     */
    public static JPanel modernCard() {
        JPanel panel = new JPanel() {
            @Override 
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Menggambar bidang warna latar belakang putih polos
                g2.setColor(BG_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, RADIUS, RADIUS));
                
                // Menggambar garis luar border setebal 1px abu-abu
                g2.setColor(BORDER_COLOR);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, RADIUS, RADIUS));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    /**
     * Mesin internal perakit cetakan tombol kustom dengan dukungan efek transisi rollover kursor (Hover).
     */
    private static JButton customButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text) {
            @Override 
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Merubah warna background tombol menjadi lebih cerah saat kursor melintas (Hover state)
                g2.setColor(getModel().isRollover() ? hoverBg : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        return btn;
    }

    public static JButton primaryButton(String text) { return customButton(text, BLUE_PRIMARY, BLUE_HOVER, Color.WHITE); }
    public static JButton dangerButton(String text)  { return customButton(text, DANGER, DANGER.darker(), Color.WHITE); }
    
    public static JButton secondaryButton(String text) {
        JButton btn = customButton(text, BG_WHITE, BG_MAIN, TEXT_DARK);
        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(8, 15, 8, 15)));
        return btn;
    }

    /**
     * Membuat kolom kotak isian teks (JTextField) dengan gaya melengkung modern dan padding nyaman.
     */
    public static JTextField modernInput() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBackground(BG_MAIN);
        // CompoundBorder menggabungkan garis border tipis melengkung luar dan padding kosong menjorok dalam (8px kiri-kanan)
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(8, 12, 8, 12)));
        return tf;
    }

    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY_BOLD);
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Membuat komponen area header judul halaman terstandarisasi.
     */
    public static JPanel pageHeader(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title); lblTitle.setFont(FONT_H1); lblTitle.setForeground(TEXT_DARK);
        JLabel lblSub = new JLabel(subtitle); lblSub.setFont(FONT_BODY); lblSub.setForeground(TEXT_MUTED);
        
        p.add(lblTitle); 
        p.add(Box.createVerticalStrut(5)); // Memberikan jarak vertikal pemisah 5px
        p.add(lblSub);
        return p;
    }
}