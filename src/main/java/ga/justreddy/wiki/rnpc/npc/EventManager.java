package ga.justreddy.wiki.rnpc.npc;

import ga.justreddy.wiki.rnpc.npc.versions.v_1_8_R3.NpcPipeLine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventManager implements Listener {

    private static final Map<UUID, Long> playerDelays = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        NpcUtils.getUtils().showNpcs(e.getPlayer());
        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_8_R3":
                NpcPipeLine.getPipeLine().inject(e.getPlayer());
                break;
            case "v1_9_R2":
                ga.justreddy.wiki.rnpc.npc.versions.v_1_9_R2.NpcPipeLine.getPipeLine().inject(e.getPlayer());
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
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        NpcUtils.getUtils().showNpcs(e.getPlayer());
        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_8_R3":
                NpcPipeLine.getPipeLine().inject(e.getPlayer());
                break;
            case "v1_9_R2":
                ga.justreddy.wiki.rnpc.npc.versions.v_1_9_R2.NpcPipeLine.getPipeLine().inject(e.getPlayer());
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
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        if (e.getPlayer() == null || e.getRightClicked() == null) return;
        Entity entity = e.getRightClicked();
        INpc iNpc;
        if ((iNpc = NpcUtils.getUtils().getNpcFromEntityId(entity.getEntityId())) != null){
            e.setCancelled(true);
            runCommands(e.getPlayer(), iNpc);
        }
    }


    private void runCommands(Player player, INpc npc) {
        if (!lastNpcInteractDelayis(player, 500)) return;
        npc.runNpcCommands(player);
    }

    private boolean lastNpcInteractDelayis(Player player, long delay) {
        if (playerDelays.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() - playerDelays.get(player.getUniqueId()) >= delay) {
                playerDelays.replace(player.getUniqueId(), System.currentTimeMillis());
                return true;
            }
            return false;
        }
        playerDelays.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }

}
