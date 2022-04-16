package ga.justreddy.wiki.rnpc;

import ga.justreddy.wiki.rnpc.commands.MainCommand;
import ga.justreddy.wiki.rnpc.npc.EventManager;
import ga.justreddy.wiki.rnpc.npc.INpc;
import ga.justreddy.wiki.rnpc.npc.NpcUtils;
import ga.justreddy.wiki.rnpc.npc.versions.v_1_8_R3.NpcPipeLine;
import ga.justreddy.wiki.rnpc.npc.versions.v_1_8_R3.V1_8_R3;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class RNPC extends JavaPlugin {

    @Getter private static RNPC plugin;

    @Getter private YamlConfig npcConfig;

    private final int NPC_VERSION = 1;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        getCommand("npc").setExecutor(new MainCommand());
        if(!loadConfigs()) return;
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        NpcUtils.getUtils().load();
        addPlayersToPipeline();
    }

    private void addPlayersToPipeline() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
                case "v1_8_R3":
                    NpcPipeLine.getPipeLine().inject(p);
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
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        NpcUtils.getUtils().hideNpcs();
    }

    private boolean loadConfigs() {

        try {

            String currentlyLoading;

            currentlyLoading = "npcs.yml";
            npcConfig = new YamlConfig(currentlyLoading);
            if(npcConfig.isOutdated(NPC_VERSION)){
                System.out.println("Outdated ");
                return false;
            }


        }catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private void loadNpc() {

    }

}
