package ga.justreddy.wiki.rnpc.npc;

import ga.justreddy.wiki.rnpc.RNPC;
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
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        NpcUtils.getUtils().showNpcs(e.getPlayer());
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


    private boolean runCommands(Player player, INpc npc) {
        if (!lastNpcInteractDelayis(player, 500)) return false;
        npc.runNpcCommands(player);
        return true;
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
