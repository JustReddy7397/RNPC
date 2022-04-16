package ga.justreddy.wiki.rnpc;

import ga.justreddy.wiki.rnpc.commands.MainCommand;
import ga.justreddy.wiki.rnpc.npc.EventManager;
import ga.justreddy.wiki.rnpc.npc.NpcUtils;
import ga.justreddy.wiki.rnpc.npc.versions.v_1_8_R3.NpcPipeLine;
import ga.justreddy.wiki.rnpc.utils.Utils;
import lombok.Getter;
import net.minecraft.server.v1_9_R2.BiomeBase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class RNPC extends JavaPlugin {

    @Getter
    private static RNPC plugin;

    @Getter
    private YamlConfig npcConfig;
    @Getter
    private YamlConfig messagesConfig;

    @Getter
    private final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private final int NPC_VERSION = 1;
    private final int MESSAGES_VERSION = 1;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        if(!start()) return;
        getCommand("npc").setExecutor(new MainCommand());
        if (!loadConfigs()) return;
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        NpcUtils.getUtils().load();
        addPlayersToPipeline();
    }

    private void addPlayersToPipeline() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            switch (getVersion()) {
                case "v1_8_R3":
                    NpcPipeLine.getPipeLine().inject(p);
                    break;
                case "v1_9_R2":
                    ga.justreddy.wiki.rnpc.npc.versions.v_1_9_R2.NpcPipeLine.getPipeLine().inject(p);
                    break;
                case "v1_10_R1":
                    ga.justreddy.wiki.rnpc.npc.versions.v_1_10_R1.NpcPipeLine.getPipeLine().inject(p);
                    break;
                case "v1_11_R1":
                    ga.justreddy.wiki.rnpc.npc.versions.v_1_11_R1.NpcPipeLine.getPipeLine().inject(p);
                    break;
                case "v1_12_R1":
                    ga.justreddy.wiki.rnpc.npc.versions.v_1_12_R1.NpcPipeLine.getPipeLine().inject(p);
                    break;
                case "v1_13_R2":
                    break;
                case "v1_14_R1":
                    break;
                case "v1_15_R1":
                    break;
                default:
                    Utils.error(null, "Failed to find NMS version for " + getVersion(), true);
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
            if (npcConfig.isOutdated(NPC_VERSION)) {
                Utils.error(null, "The " + currentlyLoading + " is outdated!", true);
                return false;
            }

            currentlyLoading = "messages.yml";
            messagesConfig = new YamlConfig(currentlyLoading);
            if(messagesConfig.isOutdated(MESSAGES_VERSION)) {
                Utils.error(null, "The " + currentlyLoading + " is outdated!", true);
                return false;
            }


        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean start() {
        switch (getVersion()) {
            case "v1_8_R3":
            case "v1_10_R1":
            case "v1_9_R2":
            case "v1_11_R1":
            case "v1_12_R1":
            case "v1_13_R2":
            case "v1_14_R1":
            case "v1_15_R1":
                getLogger().log(Level.INFO, "Found NMS version " + getVersion() + ", using it!");
                return true;
            default:
                Utils.error(null, "Failed to find NMS version for " + getVersion(), true);
                return false;

        }
    }

}
