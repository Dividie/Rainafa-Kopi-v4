package ui;

import struktur.DataManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Map;

/**
 * Kelas AkunPanel menyediakan fungsi tata kelola data user terdaftar khusus bagi Atasan/Admin.
 * Dilengkapi visualisasi indikator baris warna khusus pembeda hierarki pangkat otoritas (Admin Merah vs Kasir Hijau).
 */
public class AkunPanel extends JPanel {
    private JTable tabel;
    private DefaultTableModel modelTabel;
    
    private JTextField txtUsername, txtPassword;
    private JComboBox<String> cbRole; 
    private JLabel lblStatusForm;
    private String editUsernameLama = ""; 

    public AkunPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(Theme.pageHeader("Manajemen Akun", "Kelola daftar akun pengguna dan hak akses (Role) sistem POS."), BorderLayout.NORTH);

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
        JLabel lblTitle = new JLabel("Daftar Akun Terdaftar");
        lblTitle.setFont(Theme.FONT_H2);
        tableHeader.add(lblTitle, BorderLayout.WEST);

        String[] kolom = {"Username", "Hak Akses (Role)", "Password"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        tabel = new JTable(modelTabel);
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
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : Theme.BG_MAIN); 
                }
                
                if (column == 0) {
                    c.setForeground(Theme.BLUE_PRIMARY);
                    setFont(Theme.FONT_BODY_BOLD);
                } else if (column == 1) { 
                    // Logika pewarnaan khusus kolom hak akses
                    String role = value.toString();
                    c.setForeground(role.equals("Admin") ? Theme.DANGER : new Color(40, 167, 69)); // Merah tebal untuk Admin, Hijau segar untuk Kasir
                    setFont(Theme.FONT_BODY_BOLD);
                } else {
                    c.setForeground(Theme.TEXT_MUTED);
                    setFont(Theme.FONT_BODY);
                }
                setBorder(new EmptyBorder(0, 15, 0, 15));
                return c;
            }
        });

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

    private JPanel initFormPanel() {
        JPanel formCard = Theme.modernCard();
        formCard.setPreferredSize(new Dimension(350, 0));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JPanel headerForm = new JPanel(new BorderLayout());
        headerForm.setOpaque(false);
        headerForm.setMaximumSize(new Dimension(320, 40));
        headerForm.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        lblStatusForm = new JLabel("Tambah Akun Baru");
        lblStatusForm.setFont(Theme.FONT_H2);
        lblStatusForm.setForeground(Theme.BLUE_PRIMARY);
        
        JButton btnReset = Theme.secondaryButton("Reset");
        btnReset.setFont(Theme.FONT_SMALL);
        btnReset.addActionListener(e -> resetForm());

        headerForm.add(lblStatusForm, BorderLayout.WEST);
        headerForm.add(btnReset, BorderLayout.EAST);
        formCard.add(headerForm);
        formCard.add(Box.createVerticalStrut(25));

        formCard.add(Theme.fieldLabel("Username"));
        formCard.add(Box.createVerticalStrut(5));
        txtUsername = Theme.modernInput();
        txtUsername.setMaximumSize(new Dimension(320, 40));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(txtUsername);
        formCard.add(Box.createVerticalStrut(15));

        formCard.add(Theme.fieldLabel("Password"));
        formCard.add(Box.createVerticalStrut(5));
        txtPassword = Theme.modernInput();
        txtPassword.setMaximumSize(new Dimension(320, 40));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(txtPassword);
        formCard.add(Box.createVerticalStrut(15));

        formCard.add(Theme.fieldLabel("Hak Akses (Role)"));
        formCard.add(Box.createVerticalStrut(5));
        cbRole = new JComboBox<>(new String[]{"Kasir", "Admin"});
        cbRole.setMaximumSize(new Dimension(320, 40));
        cbRole.setBackground(Color.WHITE);
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(cbRole);
        formCard.add(Box.createVerticalStrut(30));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0)); 
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(320, 45));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSimpan = Theme.primaryButton("Simpan Data");
        btnSimpan.addActionListener(e -> aksiSimpanAkun());

        JButton btnHapus = Theme.dangerButton("Hapus Akun");
        btnHapus.addActionListener(e -> aksiHapusAkun());

        btnPanel.add(btnSimpan); btnPanel.add(btnHapus);
        formCard.add(btnPanel);
        formCard.add(Box.createVerticalGlue()); 

        return formCard;
    }

    private void populateFormFromTable() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) return;
        
        String user = (String) modelTabel.getValueAt(baris, 0);
        String[] data = DataManager.getInstance().getSemuaAkun().get(user); 
        
        if (data != null) {
            editUsernameLama = user;
            lblStatusForm.setText("Edit Akun: " + user);
            txtUsername.setText(user);
            txtPassword.setText(data[0]); // Index 0 = Nilai teks Password murni
            cbRole.setSelectedItem(data[1]); // Index 1 = String Klasifikasi Hak Akses Role
        }
    }

    private void resetForm() {
        tabel.clearSelection();
        editUsernameLama = "";
        lblStatusForm.setText("Tambah Akun Baru");
        txtUsername.setText("");
        txtPassword.setText("");
        cbRole.setSelectedIndex(0);
    }

    private void aksiSimpanAkun() {
        String u = txtUsername.getText().trim();
        String p = txtPassword.getText().trim();
        String r = (String) cbRole.getSelectedItem(); 

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, String[]> data = DataManager.getInstance().getSemuaAkun();

        if (!editUsernameLama.isEmpty() && !editUsernameLama.equals(u)) {
            // Jika user merubah string username utama, hapus key nama lama untuk menghindari redundansi ganda
            DataManager.getInstance().hapusAkun(editUsernameLama);
        } else if (editUsernameLama.isEmpty() && data.containsKey(u)) {
            JOptionPane.showMessageDialog(this, "Username sudah digunakan! Pilih nama lain.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DataManager.getInstance().simpanAkun(u, p, r);
        resetForm();
        refresh();
        JOptionPane.showMessageDialog(this, "Data akun berhasil disimpan!");
    }

    private void aksiHapusAkun() {
        if (editUsernameLama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih akun dari tabel terlebih dahulu untuk dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Aturan Proteksi Kritis: Mencegah sistem dari kondisi tanpa akun sama sekali (Lockout State)
        if (DataManager.getInstance().getSemuaAkun().size() <= 1) {
            JOptionPane.showMessageDialog(this, "Aplikasi minimal harus memiliki 1 akun untuk login!", "Tidak Dapat Menghapus", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus akses untuk akun '" + editUsernameLama + "'?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            DataManager.getInstance().hapusAkun(editUsernameLama);
            resetForm();
            refresh();
        }
    }

    /**
     * Memperbarui muatan data baris tabel akun, menyamarkan kolom sandi dengan karakter bullet (masking) 
     * demi alasan privasi keamanan data toko.
     */
    public void refresh() {
        modelTabel.setRowCount(0);
        Map<String, String[]> data = DataManager.getInstance().getSemuaAkun();
        for (Map.Entry<String, String[]> entry : data.entrySet()) {
            String username = entry.getKey();
            String role = entry.getValue()[1]; 
            
            // Menyisipkan visual penyamar sandi (bullet mask) daripada teks password murni polos
            modelTabel.addRow(new Object[]{ username, role, "••••••••" });
        }
    }
}