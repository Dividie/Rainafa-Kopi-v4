package struktur;

import model.Menu;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * STRUKTUR DATA: HASHMAP (Urutan Terpelihara)
 * Key = Kode Menu (misal: M01), Value = Objek Detail Menu
 * Melakukan pencarian data dengan performa O(1), sangat ideal untuk pencarian kilat
 * oleh sistem kasir sewaktu memproses scan/klik item belanjaan.
 */
public class MenuHashMap {
    private Map<String, Menu> menuMap;

    public MenuHashMap() {
        // LinkedHashMap dipilih agar urutan tampilan menu pada user interface (GUI) 
        // tetap teratur konsisten sesuai waktu data tersebut dimasukkan pertama kali.
        this.menuMap = new LinkedHashMap<>();
    }

    public void tambahMenu(Menu menu) { menuMap.put(menu.getKode(), menu); }
    public Menu getMenu(String kode) { return menuMap.get(kode); }
    public boolean hapusMenu(String kode) { return menuMap.remove(kode) != null; }
    public Collection<Menu> getSemuaMenu() { return menuMap.values(); }
    public boolean isMenuValid(String kode) { return menuMap.containsKey(kode); }
    public Map<String, Menu> getMenuMap() { return menuMap; }

    /**
     * Memperbarui informasi harga produk tertentu di dalam map.
     */
    public boolean updateHarga(String kode, long h) { 
        Menu m = menuMap.get(kode); 
        if (m != null) { 
            m.setHarga(h); 
            return true; 
        } 
        return false; 
    }
    
    /**
     * Memperbarui nama item produk tertentu di dalam map.
     */
    public boolean updateNama(String kode, String n) { 
        Menu m = menuMap.get(kode); 
        if (m != null) { 
            m.setNama(n); 
            return true; 
        } 
        return false; 
    }
}