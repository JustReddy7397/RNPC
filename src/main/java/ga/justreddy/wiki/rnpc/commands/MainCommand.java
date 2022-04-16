package ga.justreddy.wiki.rnpc.commands;

import ga.justreddy.wiki.rnpc.RNPC;
import ga.justreddy.wiki.rnpc.npc.NpcUtils;
import ga.justreddy.wiki.rnpc.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                    if(!player.hasPermission("rnpc.create")){
                        player.sendMessage(tellInvalidPerms("rnpc.create"));
                        return true;
                    }
                    createCommand(player, args);
                    break;
                case "delete":
                    if(!player.hasPermission("rnpc.delete")){
                        player.sendMessage(tellInvalidPerms("rnpc.delete"));
                        return true;
                    }
                    deleteCommand(player, args);
                    break;
                case "reload":
                    if(!player.hasPermission("rnpc.reload")){
                        player.sendMessage(tellInvalidPerms("rnpc.reload"));
                        return true;
                    }
                    reloadCommand(player, args);
                    break;
                case "setname":
                    if(!player.hasPermission("rnpc.setname")){
                        player.sendMessage(tellInvalidPerms("rnpc.setname"));
                        return true;
                    }
                    setNameCommand(player, args);
                    break;
                case "setskin":
                    if(!player.hasPermission("rnpc.setskin")){
                        player.sendMessage(tellInvalidPerms("rnpc.setskin"));
                        return true;
                    }
                    setSkinCommand(player, args);
                    break;
                case "showname":
                    if(!player.hasPermission("rnpc.showname")){
                        player.sendMessage(tellInvalidPerms("rnpc.showname"));
                        return true;
                    }
                    setCustomNameVisibleCommand(player, args);
                    break;
                case "move":
                    if(!player.hasPermission("rnpc.move")){
                        player.sendMessage(tellInvalidPerms("rnpc.move"));
                        return true;
                    }
                    moveCommand(player, args);
                    break;
                case "command":
                    if(!player.hasPermission("rnpc.command")){
                        player.sendMessage(tellInvalidPerms("rnpc.command"));
                        return true;
                    }
                    addCommand(player, args);
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
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-togglename").replace("%boolean%", (customName ? "enabled" : "disabled") )));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments( "/npc setskin <id> <skin>"));
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

            if (command.toString().startsWith("-s") || command.toString().startsWith("-server")) {
                command = new StringBuilder(command.toString().replace("-s ", "server:").replace("-server ", "server:"));
            }

            if (command.toString().startsWith("-p") || command.toString().startsWith("-player")) {
                command = new StringBuilder(command.toString().replace("-p ", "player:").replace("-player ", "player:"));
            }

            if (command.toString().startsWith("-m") || command.toString().startsWith("-message")) {
                command = new StringBuilder(command.toString().replace("-m ", "message:").replace("-message ", "message:"));
            }

            NpcUtils.getUtils().addCommand(id, command.toString());
            player.sendMessage(Utils.format(RNPC.getPlugin().getMessagesConfig().getConfig().getString("npc-command-add")));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(tellInvalidArguments("/npc move <id>"));
        }
    }

}
