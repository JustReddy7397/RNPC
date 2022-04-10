package ga.justreddy.wiki.rnpc.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface INpc {

    void setLocation(Location location);

    void hide();

    void show();

    void hide(Player player);

    void show(Player player);

    void refresh();

    int getEntityId();

    void runNpcCommands(Player player);
}
