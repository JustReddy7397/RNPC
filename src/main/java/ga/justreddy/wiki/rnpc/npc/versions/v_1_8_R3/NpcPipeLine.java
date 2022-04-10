package ga.justreddy.wiki.rnpc.npc.versions.v_1_8_R3;

import ga.justreddy.wiki.rnpc.npc.INpc;
import ga.justreddy.wiki.rnpc.npc.NpcUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NpcPipeLine {

    private static NpcPipeLine npcPipeLine;

    private NpcPipeLine() {}

    private static final Map<UUID, Channel> channels = new HashMap<>();
    private static final Map<UUID, Long> lastInteraction = new HashMap<>();

    public void inject(Player player) {
        final CraftPlayer craftPlayer = (CraftPlayer) player;
        final Channel channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
        channels.put(player.getUniqueId(), channel);
        if(player.isOnline()) initPipeLine(player, channel);
    }

    private void initPipeLine(Player player, Channel channel) {
        if(channel.pipeline().get("NpcPipeLine") != null) channel.pipeline().remove("NpcPipeLine");
        addPacketDecoder(player, channel);

    }



    @SneakyThrows
    private void addPacketDecoder(Player player, Channel channel) {
        channel.pipeline().addAfter("decoder", "NpcPipeLine", new MessageToMessageDecoder<Packet<?>>() {

            @Override
            protected void decode(ChannelHandlerContext channel, Packet<?> packet, List<Object> args) throws Exception {
                args.add(packet);
                readPacket(player, packet);
            }
        });
    }


    private void readPacket(Player player, Packet<?> packet) {
        if(packet != null && packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) handlePacketPlayInUse(player, packet);
    }

    @SneakyThrows
    private void handlePacketPlayInUse(Player player, Packet<?> packet) {
        int entityId = (int) getObjectFromPacket(packet, "a");
        String action = getObjectFromPacket(packet, "action").toString();
        if(action.equals("INTERACT") || action.equals("INTERACT_AT")) {
            INpc iNpc;
            if(!checkAndUpdateLastClick(player.getUniqueId())) return;
            else if((iNpc = NpcUtils.getUtils().getNpcFromEntityId(entityId)) != null){
                iNpc.runNpcCommands(player);
            }
        }
    }

    private boolean checkAndUpdateLastClick(UUID uuid) {
        long lastInteract = lastInteraction.isEmpty() ? 0L : lastInteraction.getOrDefault(uuid, 0L);
        if(lastInteraction.isEmpty() || System.currentTimeMillis()-lastInteract < 500) return false;
        boolean isInMap = lastInteract > 0;
        lastInteract = System.currentTimeMillis();
        if(isInMap) lastInteraction.replace(uuid, lastInteract);
        else lastInteraction.put(uuid, lastInteract);
        return true;
    }

    @NotNull
    private Object getObjectFromPacket(Packet<?> packet, String object) {
        try{
            Field field = packet.getClass().getDeclaredField(object);
            field.setAccessible(true);
            return field.get(packet);
        }catch (Exception ex) {
            return null;
        }
    }

    public static NpcPipeLine getPipeLine() {
        if(npcPipeLine == null) npcPipeLine = new NpcPipeLine();
        return npcPipeLine;
    }


}
