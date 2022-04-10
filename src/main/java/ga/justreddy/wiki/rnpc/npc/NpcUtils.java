package ga.justreddy.wiki.rnpc.npc;

import ga.justreddy.wiki.rnpc.RNPC;
import ga.justreddy.wiki.rnpc.npc.versions.v_1_8_R3.V1_8_R3;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcUtils {

    private static NpcUtils utils;

    private final Map<String, INpc> npcIdListByString = new HashMap<>();
    private final Map<Integer, INpc> npcIdListById = new HashMap<>();
    private final List<String> npcIdList = new ArrayList<>();

    public void load() {
        npcIdListByString.clear();
        npcIdListById.clear();
        npcIdList.clear();
        List<String> npcs = RNPC.getPlugin().getNpcConfig().getConfig().getStringList("NpcList");
        if(npcs.isEmpty()) return;
        for(String id : npcs) {
            Location location = (Location) RNPC.getPlugin().getNpcConfig().getConfig().get("npc." + id + ".location");
            if(location != null) create(location, id);
        }
    }

    public void create(Location location, String id ) {
        INpc iNpc = null;
        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_8_R3":
                iNpc = new V1_8_R3(id, location);
                break;
            case "v1_9_R2":
                break;
            case "v1_10_R1":
                break;
            case "v1_11_R1":
                break;
            case "v1_12_R1":
                break;
            case "v1_13_R2":
                break;
            case "v1_14_R1":
                break;
            case "v1_15_R1":
                break;
        }
        npcIdListByString.put(id, iNpc);
        npcIdListById.put(iNpc.getEntityId(), iNpc);
        npcIdList.add(id);
        saveNpcList();
    }

    @SneakyThrows
    private void saveNpcList() {
        RNPC.getPlugin().getNpcConfig().getConfig().set("NpcList", npcIdList);
        RNPC.getPlugin().getNpcConfig().save();
    }

    @SneakyThrows
    public void delete(String id) {
        INpc iNpc = npcIdListByString.get(id);
        if(iNpc == null) return;
        iNpc.hide();
        npcIdListByString.remove(id);
        npcIdListById.remove(iNpc.getEntityId());
        npcIdList.remove(id);
        saveNpcList();
        RNPC.getPlugin().getNpcConfig().getConfig().set("npc." + id, null);
        RNPC.getPlugin().getNpcConfig().save();
    }

    public boolean doesExist(String id) {
        return npcIdListByString.get(id) != null;
    }

    public void showNpcs(Player player) {
        for(INpc iNpc : npcIdListByString.values()) {
            iNpc.show(player);
        }
    }

    public void hideNpcs(Player player) {
        for(INpc iNpc : npcIdListByString.values()) {
            iNpc.hide(player);
        }
    }

    public void showNpcs() {
        for (Player player : Bukkit.getOnlinePlayers()) showNpcs(player);
    }

    public void hideNpcs() {
        for(Player player : Bukkit.getOnlinePlayers()) hideNpcs(player);
    }


    public INpc getNpcFromEntityId(int entityId) {
        return npcIdListById.get(entityId);
    }

    public static NpcUtils getUtils() {
        if(utils == null) utils = new NpcUtils();
        return utils;
    }
}
