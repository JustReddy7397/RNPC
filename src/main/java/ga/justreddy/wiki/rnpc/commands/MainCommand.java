package ga.justreddy.wiki.rnpc.commands;

import ga.justreddy.wiki.rnpc.RNPC;
import ga.justreddy.wiki.rnpc.npc.NpcUtils;
import ga.justreddy.wiki.rnpc.utils.Utils;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.*;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import java.util.List;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tellPlayersOnly());
            return true;
        }
        Player player = (Player) sender;
        try {
            switch (args[0]) {
                case "create":
                    if (!player.hasPermission("rnpc.create")) {
                        player.sendMessage(tellInvalidPerms("rnpc.create"));
                        return true;
                    }
                    createCommand(player, args);
                    break;
                case "delete":
                    if (!player.hasPermission("rnpc.delete")) {
                        player.sendMessage(tellInvalidPerms("rnpc.delete"));
                        return true;
                    }
                    deleteCommand(player, args);
                    break;
                case "reload":
                    if (!player.hasPermission("rnpc.reload")) {
                        player.sendMessage(tellInvalidPerms("rnpc.reload"));
                        return true;
                    }
                    reloadCommand(player, args);
                    break;
                case "setname":
                    if (!player.hasPermission("rnpc.setname")) {
                        player.sendMessage(tellInvalidPerms("rnpc.setname"));
                        return true;
                    }
                    setNameCommand(player, args);
                    break;
                case "setskin":
                    if (!player.hasPermission("rnpc.setskin")) {
                        player.sendMessage(tellInvalidPerms("rnpc.setskin"));
                        return true;
                    }
                    setSkinCommand(player, args);
                    break;
                case "showname":
                    if (!player.hasPermission("rnpc.showname")) {
                        player.sendMessage(tellInvalidPerms("rnpc.showname"));
                        return true;
                    }
                    setCustomNameVisibleCommand(player, args);
                    break;
                case "move":
                    if (!player.hasPermission("rnpc.move")) {
                        player.sendMessage(tellInvalidPerms("rnpc.move"));
                        return true;
                    }
                    moveCommand(player, args);
                    break;
                case "command":
                    if (!player.hasPermission("rnpc.command")) {
                        player.sendMessage(tellInvalidPerms("rnpc.command"));
                        return true;
                    }
                    addCommand(player, args);
                    break;
                case "list":
                    if (!player.hasPermission("rnpc.list")) {
                        player.sendMessage(tellInvalidPerms("rnpc.list"));
                        return true;
                    }
                    listCommand(player, args);
                    break;
                case "teleport":
                case "tp":
                    teleportCommand(player, args);
                    break;
                default:
                    sendHelpMessage(player);
                    break;
            }
        } catch (IndexOutOfBoundsException ex) {
            sendHelpMessage(player);
        }
        return true;
    }

    public String tellInvalidArguments(String usage) {
        return Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("invalid-arguments").replace("%usage%", usage));
    }

    public String tellInvalidPerms(String permission) {
        return Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("invalid-permissions").replace("%permission%", permission));
    }

    public String tellPlayersOnly() {
        return Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("players-only"));
    }

    private void sendHelpMessage(Player player) {
        Utils.sendMessage(player,
                "&a%line%",
                "&a/npc create <id> &7- &aCreates a NPC",
                "&a/npc delete <id> &7- &aDeletes a NPC",
                "&a/npc reload &7- &aReloads the configs",
                "&a/npc setname <id> <name> &7- &aSets the name of a NPC",
                "&a/npc setskin <id> <skin> &7- &aSets the skin of a NPC",
                "&a/npc showname <id> <true/false> &7- &aEnabled/Disables the NPC name",
                "&a/npc move <id> &7- &aMoves the NPC to your location",
                "&a/npc command <id> [-s(server), -p(layer), -m(essage)] <command/message/server>",
                "&a/npc list [page] &7- &aCheck all NPCs on the server",
                "&a/npc tp <id> &7- &aTeleport to a NPC",
                "&a%line%"
        );
    }

    private void createCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-already-exists").replace("%id%", id)));
                return;
            }
            NpcUtils.getUtils().create(player.getLocation(), id);
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-created")));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments("/npc create <id>"));
        }
    }

    private void deleteCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-not-exists").replace("%id%", id)));
                return;
            }
            NpcUtils.getUtils().delete(id, true);
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-deleted")));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidPerms("/npc delete <id>"));
        }
    }

    @SneakyThrows
    private void reloadCommand(Player player, String[] args) {
        NpcUtils.getUtils().hideNpcs();
        RNPC.getPlugin().getNpcConfig().reload();
        Bukkit.getScheduler().runTaskLater(RNPC.getPlugin(), () -> NpcUtils.getUtils().load(), 20L);
        player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("reload")));
    }

    private void setNameCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-not-exists").replace("%id%", id)));
                return;
            }

            String name = "";
            for (int i = 2; i < args.length; i++) {
                name = name + args[i] + " ";
            }

            NpcUtils.getUtils().setName(id, name);
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-name").replace("%name%", name)));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments("/npc setname <id> <name>"));
        }
    }

    private void setSkinCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-not-exists").replace("%id%", id)));
                return;
            }

            String skin = args[2];

            NpcUtils.getUtils().setSkin(id, skin);
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-skin").replace("%skin%", skin)));

        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments("/npc setskin <id> <skin>"));
        }
    }

    private void setCustomNameVisibleCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-not-exists").replace("%id%", id)));
                return;
            }
            boolean customName = Boolean.parseBoolean(args[2]);
            NpcUtils.getUtils().showCustomName(id, customName);
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-togglename").replace("%boolean%", (customName ? "enabled" : "disabled"))));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments("/npc setskin <id> <skin>"));
        }
    }

    private void moveCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-not-exists").replace("%id%", id)));
                return;
            }
            NpcUtils.getUtils().move(id, player.getLocation());
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-moved")));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments("/npc move <id>"));
        }
    }

    private void addCommand(Player player, String[] args) {
        try {

            String id = args[1];

            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-not-exists").replace("%id%", id)));
                return;
            }

            StringBuilder command = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                command.append(args[i]).append(" ");
            }

            String type = "CONSOLE";

            if (command.toString().startsWith("-s") || command.toString().startsWith("-server")) {
                command = new StringBuilder(command.toString().replace("-s ", "server:").replace("-server ", "server:"));
                type = "SERVER";
            }

            if (command.toString().startsWith("-p") || command.toString().startsWith("-player")) {
                command = new StringBuilder(command.toString().replace("-p ", "player:").replace("-player ", "player:"));
                type = "PLAYER";
            }

            if (command.toString().startsWith("-m") || command.toString().startsWith("-message")) {
                command = new StringBuilder(command.toString().replace("-m ", "message:").replace("-message ", "message:"));
                type = "MESSAGE";
            }

            NpcUtils.getUtils().addCommand(id, command.toString());
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-command-add").replace("%type", type)));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments("/npc move <id>"));
        }
    }
    int page;
    private void listCommand(Player player, String[] args) {
        try {
            page = Integer.parseInt(args[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            page = 1;
        }
        List<String> lines = RNPC.getPlugin().getNpcConfig().getConfig().getStringList("NpcList");
        ChatPaginator.ChatPage paginator = ChatPaginator.paginate(String.join("\n", lines), page, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH, 5);
        TextComponent next = new TextComponent(Utils.format("&e&l>>"));
        next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc list " + (page+1)));
        TextComponent previous = new TextComponent(Utils.getCenteredMessage("<center>&e&l<<</center>"));
        previous.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/npc list ") + (page-1)));
        if (page > paginator.getTotalPages()) return;
        Utils.sendMessage(player, "&6%line%");
        if (page == 1) {
            TextComponent main = new TextComponent(Utils.getCenteredMessage("<center> &aNPCS (Page %page%/%maxpage%)</center> ").replace("%page%", String.valueOf(page)).replace("%maxpage%", String.valueOf(paginator.getTotalPages())));
            player.spigot().sendMessage(main, next);
        } else if (page == paginator.getTotalPages()) {
            TextComponent main = new TextComponent(Utils.format(" &aNPCS (Page %page%/%maxpage%) ").replace("%page%", String.valueOf(page)).replace("%maxpage%", String.valueOf(paginator.getTotalPages())));
            player.spigot().sendMessage(previous, main);
        } else {
            TextComponent main = new TextComponent(Utils.format(" &aNPCS (Page %page%/%maxpage%) ").replace("%page%", String.valueOf(page)).replace("%maxpage%", String.valueOf(paginator.getTotalPages())));
            player.spigot().sendMessage(previous, main, next);
        }
        for (String line : paginator.getLines()) {
            Location location = (Location) RNPC.getPlugin().getNpcConfig().getConfig().get("npc." + ChatColor.stripColor(line) + ".location");
            line = ChatColor.stripColor(line);
            TextComponent textComponent = new TextComponent(Utils.format("&a* &6" + line));
            String finalLocation = "world: " + location.getWorld().getName() + " x: " + Math.round(location.getX()) + ", y: " + location.getY() + ", z: " + Math.round(location.getZ());
            TextComponent locationComponent = new TextComponent(Utils.format(" &7( " + finalLocation + " )"));
            locationComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Utils.format("&aClick to teleport to the NPC")).create()));
            locationComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc tp " + ChatColor.stripColor(line)));
            player.spigot().sendMessage(textComponent, locationComponent);
        }
        Utils.sendMessage(player, "&6%line%");
    }

    private void teleportCommand(Player player, String[] args) {
        try{
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-not-exists").replace("%id%", id)));
                return;
            }
            player.teleport((Location) RNPC.getPlugin().getNpcConfig().getConfig().get("npc." + id + ".location"));
        }catch (IndexOutOfBoundsException ex) {
            player.sendMessage(Utils.format(tellInvalidArguments("/npc teleport <id>")));
        }
    }

}
