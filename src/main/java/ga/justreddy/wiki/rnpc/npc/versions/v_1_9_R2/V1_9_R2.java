package ga.justreddy.wiki.rnpc.npc.versions.v_1_9_R2;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import ga.justreddy.wiki.rnpc.RNPC;
import ga.justreddy.wiki.rnpc.npc.INpc;
import ga.justreddy.wiki.rnpc.npc.ProfileClass;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class V1_9_R2 implements INpc {

    private final FileConfiguration config = RNPC.getPlugin().getNpcConfig().getConfig();

    private Location location;
    private final String id;
    private String name;
    private String skinOwner;
    private EntityType npcType;
    private LivingEntity npcAsEntity;
    private EntityPlayer npcAsPlayer;
    private ProfileClass profile;
    private List<String> npcCommands;
    private boolean customName;

    public V1_9_R2(String id, Location location) {
        this.id = id;
        setLocation(location);
        initVariables();
        createNewNpc();
    }

    private void initVariables() {
        if (checkIfNpcHasData()) initVariablesFromExisting();
        else initNewVariables();
    }

    private void initVariablesFromExisting() {
        name = RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id.toLowerCase() + ".name");
        skinOwner = RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id.toLowerCase() + ".skinOwner");
        customName = RNPC.getPlugin().getNpcConfig().getConfig().getBoolean("npc." + id.toLowerCase() + ".customName");
        npcCommands = config.getStringList("npc." + id + ".commands");
        if (name == null || name.equals("")) name = " ";
        if (skinOwner == null || skinOwner.equals("")) skinOwner = " ";
    }

    private void initNewVariables() {
        name = id;
        skinOwner = id;
        customName = true;
        npcCommands = new ArrayList<>();
    }

    private boolean checkIfNpcHasData() {
        return RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id) != null;
    }

    @SneakyThrows
    private void save() {
        config.set("npc." + id.toLowerCase() + ".name", name);
        config.set("npc." + id.toLowerCase() + ".skinOwner", skinOwner);
        config.set("npc." + id.toLowerCase() + ".type", npcType.toString().toLowerCase());
        config.set("npc." + id.toLowerCase() + ".location", location);
        config.set("npc." + id.toLowerCase() + ".customName", customName);
        config.set("npc." + id.toLowerCase() + ".commands", npcCommands);
        RNPC.getPlugin().getNpcConfig().save();
    }

    private void createNewNpc() {
        profile = new ProfileClass();
        if (checkIfNpcHasData()) initNpcType();
        else npcType = EntityType.PLAYER;
        loadNewNpc();
        killNearbyMatchingEntities();
    }

    private void killNearbyMatchingEntities() {
        if (npcType == EntityType.PLAYER) return;
        for (Entity entity : npcAsEntity.getNearbyEntities(0.0, 0.5, 0.0)) {
            if (entity.getCustomName() != null && entity.getCustomName().equals(name) && entity.getType() == npcType) {
                entity.remove();
            }
        }
    }


    private void initNpcType() {
        String typeAsString = RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id + ".type");
        npcType = null;
        try {
            if (typeAsString == null ||
                    typeAsString.trim().equals("") ||
                    (npcType = EntityType.valueOf(typeAsString.toLowerCase().replace(" ", "_"))) == null)
                npcType = EntityType.PLAYER;
        } catch (Exception ex) {
            npcType = EntityType.PLAYER;
        }
    }

    private void loadNewNpc() {
        if (npcType == EntityType.PLAYER) loadNewPlayerNpc();
        else loadNewEntityNpc();
        save();
    }

    private void loadNewPlayerNpc() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        npcAsPlayer = new EntityPlayer(server, world, profile.getProfile(), new PlayerInteractManager(world));
        npcAsPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        npcAsPlayer.setCustomNameVisible(true);
        setNpcName();
        setNpcSkin();
        hide();
        show();
    }

    @SneakyThrows
    private void setNpcName() {
        boolean nameValid = true;
        if (name == null || name.trim().equals("")) name = " ";
        if (name.length() > 16) nameValid = false;
        try {
            Field field = profile.getProfile().getClass().getDeclaredField("name");
            field.setAccessible(true);
            field.set(profile.getProfile(), ChatColor.translateAlternateColorCodes('&', nameValid ? name : name.substring(0, 16)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        config.set("npc." + id.toLowerCase() + ".name", name);
        RNPC.getPlugin().getNpcConfig().save();
    }

    private void setNpcSkin() {
        if (skinOwner == null || skinOwner.trim().equals("")) return;
        profile.updateSkinOwner(skinOwner);

    }

    private void loadNewEntityNpc() {
        if (!checkIfNpcIsDead()) return;
        npcAsEntity = getNewEntityNpc();
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () -> {
            if (!checkIfNpcIsDead()) {
                NBTEntity entity = new NBTEntity(npcAsEntity);
                entity.setInteger("Invulnerable", 1);
            }
        }, 2);
    }

    private LivingEntity getNewEntityNpc() {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, npcType);
        NBTEntity nbtEntity = new NBTEntity(entity);
        nbtEntity.setInteger("Silent", 1);
        nbtEntity.setInteger("NoAI", 1);
        nbtEntity.setInteger("NoGravity", 1);
        entity.setCustomNameVisible(customName);
        entity.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        return entity;
    }

    private boolean checkIfNpcIsDead() {
        return npcAsEntity == null || npcAsEntity.isDead();
    }

    @Override
    public void setLocation(Location location) {
        if (location == null) return;
        this.location = location;
        this.location.setYaw(location.getYaw());
        this.location.setPitch(location.getPitch());
        refreshNpc();
    }

    private void refreshNpc() {
        if (npcType == EntityType.PLAYER) refreshPlayerNpc();
        else if (npcAsEntity != null && !checkIfNpcIsDead()) npcAsEntity.teleport(location);
    }

    private void refreshPlayerNpc() {
        if (npcAsPlayer == null) return;
        hide();
        npcAsPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        show();
    }

    private void showPlayerNpc(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npcAsPlayer));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npcAsPlayer));
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () -> {
            if (skinOwner != null && !skinOwner.trim().equals("")) sendNpcSkinPackets(connection);
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npcAsPlayer));
            ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), npcAsPlayer.getName());
            if(!customName) {
                team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
                connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
                connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
                connection.sendPacket(new PacketPlayOutScoreboardTeam(team, new ArrayList<String>(){{add(npcAsPlayer.getName());}}, 3));
            }else{
                team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
            }
        }, 50);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () ->
                        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npcAsPlayer, (byte) ((location.getYaw() * 256.0F) / 360.0F))),
                5);
    }

    private void sendNpcSkinPackets(PlayerConnection connection) {
        DataWatcher watcher = npcAsPlayer.getDataWatcher();
        byte bytes = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        watcher.set(new DataWatcherObject<>(12, DataWatcherRegistry.a), bytes);
        connection.sendPacket(new PacketPlayOutEntityMetadata(npcAsPlayer.getId(), watcher, true));
    }


    @Override
    public void hide(Player player) {
        if (npcType == EntityType.PLAYER) hidePlayerNpc(player);
        else if (!checkIfNpcIsDead()) hideEntityNpc();
    }

    private void hidePlayerNpc(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(npcAsPlayer.getId()));
    }

    private void hideEntityNpc() {
        npcAsEntity.remove();
        npcAsEntity.setHealth(0.0);
    }

    @Override
    public void hide() {
        for (Player p : location.getWorld().getPlayers()) {
            hide(p);
        }
    }

    @Override
    public void show(Player player) {
        if (!player.getWorld().equals(location.getWorld())) return;
        if (npcType == EntityType.PLAYER) showPlayerNpc(player);
        else showNpcEntity(player);
    }

    private void showNpcEntity(Player player) {
        if (!player.getWorld().equals(location.getWorld())) return;
        loadNewEntityNpc();
    }

    @Override
    public void show() {
        for (Player p : location.getWorld().getPlayers()) {
            show(p);
        }
    }

    @Override
    public void refresh() {
        hide();
        if (getLocation() != null) location = getLocation();
        initNewVariables();
        createNewNpc();
    }

    @Override
    public int getEntityId() {
        return npcType == EntityType.PLAYER ? npcAsPlayer.getId() : npcAsEntity.getEntityId();
    }

    @Override
    public void runNpcCommands(Player player) {
        if (player == null || npcCommands.isEmpty()) return;
        for (String cmd : npcCommands) {
            if (cmd.startsWith("/")) cmd = cmd.replaceFirst("/", "");
            boolean playerCmd = checkIfCommandIsPlayer(cmd);
            boolean serverCmd = cmd.startsWith("server:");
            boolean messageCmd = cmd.startsWith("message:");
            if (playerCmd) {
                cmd = cmd.replaceFirst("player:", "");
                if (cmd.startsWith("/")) cmd = cmd.replaceFirst("/", "");
            }

            if(serverCmd) {
                cmd = cmd.replace("server:", "");
                sendServer(player, cmd);
            }

            if(messageCmd) {
                cmd = cmd.replace("message:", "");
                sendMessage(player, cmd);
            }

            if (checkIfCommandIsValid(cmd.split(" ")[0])) runCommand(player, cmd, playerCmd);

        }
    }

    private void runCommand(Player player, String command, boolean playerCmd) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () -> {
            String cmd = ChatColor.translateAlternateColorCodes('&', command).replace("%player%", player.getName());
            if (playerCmd) player.performCommand(cmd);
            else Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }, 2);
    }

    private void sendServer(Player player, String serverName) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ConnectOther");
            out.writeUTF(player.getName());
            out.writeUTF(serverName);
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(RNPC.getPlugin(), "BungeeCord");
            player.sendPluginMessage(RNPC.getPlugin(), "BungeeCord", out.toByteArray());
        }, 2);
    }

    private void sendMessage(Player player, String command) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', command).replace("%player%", player.getName()));
    }

    private boolean checkIfCommandIsPlayer(String cmd) {
        return cmd.startsWith("player:");
    }

    @SneakyThrows
    private boolean checkIfCommandIsValid(String cmd) {
        return cmd != null && !cmd.trim().equals("") && getCommandMap().getCommand(cmd) != null;
    }

    private List<String> getStringList(String input) {
        return config.getStringList("npc." + id.toLowerCase() + "." + input);
    }

    private Location getLocation() {
        return (Location) config.get("npc." + id.toLowerCase() + ".location");
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(Bukkit.getServer());
    }

}
