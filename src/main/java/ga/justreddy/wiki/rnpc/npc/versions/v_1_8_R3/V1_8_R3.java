package ga.justreddy.wiki.rnpc.npc.versions.v_1_8_R3;

import de.tr7zw.changeme.nbtapi.NBTEntity;
import ga.justreddy.wiki.rnpc.RNPC;
import ga.justreddy.wiki.rnpc.npc.INpc;
import ga.justreddy.wiki.rnpc.npc.ProfileClass;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class V1_8_R3 implements INpc {

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

    public V1_8_R3(String id, Location location) {
        this.id = id;
        setLocation(location);
        initVariables();
        createNewNpc();
    }

    private void initVariables() {
        if(checkIfNpcHasData()) initVariablesFromExisting();
        else initNewVariables();
    }

    private void initVariablesFromExisting() {
        name = RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id + ".name");
        skinOwner = RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id + ".skullOwner");
        npcCommands = config.getStringList("npc." + id + ".commands");
        if (name == null || name.equals("")) name = " ";
        if (skinOwner == null || skinOwner.equals("")) skinOwner = " ";
    }

    private void initNewVariables() {
        name = " ";
        skinOwner = " ";
        npcCommands = new ArrayList<>();
    }

    private boolean checkIfNpcHasData() {
        return RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id) != null;
    }

    @SneakyThrows
    private void save() {
        config.set("npc." + id + ".name", name);
        config.set("npc." + id + ".skinOwner", skinOwner);
        config.set("npc." + id + ".type", npcType.toString().toLowerCase());
        config.set("npc." + id + ".location", location);
        config.set("npc." + id + ".commands", npcCommands);
        RNPC.getPlugin().getNpcConfig().save();
    }

    private void createNewNpc() {
        profile = new ProfileClass();
        if(checkIfNpcHasData()) initNpcType();
        else npcType = EntityType.PLAYER;
        loadNewNpc();
        killNearbyMatchingEntities();

    }

    private void killNearbyMatchingEntities() {
        if(npcType == EntityType.PLAYER) return;
        for(Entity entity : npcAsEntity.getNearbyEntities(0.0, 0.5, 0.0)) {
            if(entity.getCustomName() != null && entity.getCustomName().equals(name) && entity.getType() == npcType) {
                entity.remove();
            }
        }
    }


    private void initNpcType() {
        String typeAsString = RNPC.getPlugin().getNpcConfig().getConfig().getString("npc." + id + ".type");
        npcType = null;
        try{
            if(typeAsString == null ||
                    typeAsString.trim().equals("") ||
                    (npcType = EntityType.valueOf(typeAsString.toUpperCase().replace(" ", "_"))) == null) npcType = EntityType.PLAYER;
        }catch (Exception ex) {
            npcType = EntityType.PLAYER;
        }
    }

    private void loadNewNpc() {
        if(npcType == EntityType.PLAYER) loadNewPlayerNpc();
        else loadNewEntityNpc();
        save();
    }

    private void loadNewPlayerNpc() {
        MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld)location.getWorld()).getHandle();
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
        if(name == null || name.trim().equals("")) name = " ";
        if(name.length() > 16) nameValid = false;
        try{
            Field field = profile.getProfile().getClass().getDeclaredField("name");
            field.setAccessible(true);
            field.set(profile.getProfile(), ChatColor.translateAlternateColorCodes('&', nameValid ? name : name.substring(0, 16)));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        config.set("npc." + id + ".name", name);
        RNPC.getPlugin().getNpcConfig().save();
    }

    private void setNpcSkin() {
        if(skinOwner == null || skinOwner.trim().equals("")) return;
        profile.updateSkinOwner(skinOwner);

    }

    private void loadNewEntityNpc() {
        if(!checkIfNpcIsDead()) return;
        npcAsEntity = getNewEntityNpc();
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () -> {
            if(!checkIfNpcIsDead()) {
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
        entity.setCustomNameVisible(true);
        entity.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        return entity;
    }

    private boolean checkIfNpcIsDead() {
        return npcAsEntity == null || npcAsEntity.isDead();
    }

    @Override
    public void setLocation(Location location) {
        if(location == null) return;

        this.location = location;
        this.location.setYaw(0f);
        this.location.setPitch(0f);

        refreshNpc();
    }

    private void refreshNpc() {
        if(npcType == EntityType.PLAYER) refreshPlayerNpc();
        else if (npcAsEntity != null && !checkIfNpcIsDead()) npcAsEntity.teleport(location);
    }

    private void refreshPlayerNpc() {
        if(npcAsPlayer == null) return;
        hide();
        npcAsPlayer.setLocation(location.getX(), location.getY(),location.getZ(), 0f, 0f);
        show();
    }

    private void showPlayerNpc(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npcAsPlayer));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npcAsPlayer));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npcAsPlayer, (byte) 0));
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () -> {
            if(skinOwner != null && !skinOwner.trim().equals("")) sendNpcSkinPackets(connection);
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npcAsPlayer));
        }, 50);
    }

    private void sendNpcSkinPackets(PlayerConnection connection) {
        DataWatcher watcher = npcAsPlayer.getDataWatcher();
        byte bytes = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        watcher.watch(10,  bytes);
        connection.sendPacket(new PacketPlayOutEntityMetadata(npcAsPlayer.getId(), watcher, true));
    }


    @Override
    public void hide(Player player) {
        if(npcType == EntityType.PLAYER) hidePlayerNpc(player);
        else if(!checkIfNpcIsDead()) hideEntityNpc();
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
        for(Player p : location.getWorld().getPlayers()) {
            hide(p);
        }
    }

    @Override
    public void show(Player player) {
        if(!player.getWorld().equals(location.getWorld())) return;
        if(npcType == EntityType.PLAYER) showPlayerNpc(player);
        else showNpcEntity(player);
    }

    private void showNpcEntity(Player player) {
        if(!player.getWorld().equals(location.getWorld())) return;
        loadNewEntityNpc();
    }

    @Override
    public void show() {
        for(Player p : location.getWorld().getPlayers()) {
            show(p);
        }
    }

    @Override
    public void refresh() {
        hide();
        if(getLocation() != null) location = getLocation();
        initNewVariables();
        createNewNpc();
    }

    @Override
    public int getEntityId() {
        return npcType == EntityType.PLAYER ? npcAsPlayer.getId() : npcAsEntity.getEntityId();
    }

    @Override
    public void runNpcCommands(Player player) {
        if(player == null || npcCommands.isEmpty()) return;
        for(String cmd : npcCommands) {
            if(cmd.startsWith("/")) cmd = cmd.replaceFirst("/", "");
            boolean playerCmd = checkIfCommandIsPlayer(cmd);
            if(playerCmd) {
                cmd = cmd.replaceFirst("player:", "");
                if(cmd.startsWith("/")) cmd = cmd.replaceFirst("/", "");
            }

            if(checkIfCommandIsValid(cmd.split(" ")[0])) runCommand(player, cmd, playerCmd);

        }
    }
    
    private void runCommand(Player player, String command, boolean playerCmd) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RNPC.getPlugin(), () -> {
            String cmd = ChatColor.translateAlternateColorCodes('&', command).replace("%player%", player.getName());
            if(playerCmd) player.performCommand(cmd);
            else Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }, 2);
    }

    private boolean checkIfCommandIsPlayer(String cmd) {
        return cmd.startsWith("player:");
    }

    @SneakyThrows
    private boolean checkIfCommandIsValid(String cmd) {
        return cmd != null && !cmd.trim().equals("") && getCommandMap().getCommand(cmd) != null;
    }

    private List<String> getStringList(String input) {
        return config.getStringList("npc." + id + "." + input);
    }

    private Location getLocation() {
        return (Location) config.get("npc." + id + ".location");
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(Bukkit.getServer());
    }

}
