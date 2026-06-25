package model;

/**
 * Kelas Akun mengelola data kredensial pengguna (user) untuk keperluan autentikasi sistem.
 */
public class Akun {
    private String username;
    private String password;

    /**
     * Konstruktor untuk mendaftarkan akun baru dengan username dan password tertentu.
     */
    public Akun(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getter Methods ---

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // --- Setter Methods ---

    /**
     * Digunakan apabila pengguna ingin memperbarui atau mengganti kata sandi mereka.
     */
    public void setPassword(String password) { this.password = password; }
}