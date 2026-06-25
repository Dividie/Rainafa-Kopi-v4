package struktur;

import model.Akun;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * STRUKTUR DATA: HASHMAP (Key-Value)
 * Key = Username (String), Value = Objek Akun
 * Digunakan untuk menangani otentikasi login dengan kompleksitas waktu O(1) (Konstan).
 */
public class AkunHashMap {
    private Map<String, Akun> akunMap;

    public AkunHashMap() {
        this.akunMap = new HashMap<>();
    }

    public void tambahAkun(Akun akun) { 
        akunMap.put(akun.getUsername(), akun); 
    }
    
    public Akun getAkun(String username) { 
        return akunMap.get(username); 
    }
    
    public boolean hapusAkun(String username) { 
        return akunMap.remove(username) != null; 
    }
    
    public Collection<Akun> getSemuaAkun() { 
        return akunMap.values(); 
    }
    
    /**
     * Memvalidasi apakah kombinasi username dan password yang dimasukkan saat login cocok.
     * @return true jika data valid, false jika salah atau user tidak terdaftar.
     */
    public boolean validasiLogin(String username, String password) {
        Akun a = getAkun(username);
        // Validasi null-safety untuk menghindari NullPointerException jika user tidak ditemukan
        return a != null && a.getPassword().equals(password);
    }
}