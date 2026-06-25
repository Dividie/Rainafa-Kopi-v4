import ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Mengubah visual UI agar mengikuti tema Sistem Operasi asli (Native OS)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Jika gagal, otomatis kembali ke tema standar Java
        }

        // Menjalankan Frame Utama di thread khusus GUI (EDT) agar aplikasi stabil
        SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
}