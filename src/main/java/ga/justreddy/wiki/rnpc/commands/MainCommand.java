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
        if (!(sender instanceof Player))
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', tellPlayersOnly()));
        Player player = (Player) sender;
        try {
            switch (args[0]) {
                case "create":
                    createCommand(player, args);
                    break;
                case "delete":
                    deleteCommand(player, args);
                    break;
                case "reload":
                    reloadCommand(player, args);
                    break;
                case "setname":
                    setNameCommand(player, args);
                    break;
                case "setskin":
                    setSkinCommand(player, args);
                    break;
                case "showname":
                    setCustomNameVisibleCommand(player, args);
                    break;
                case "move":
                    moveCommand(player, args);
                    break;
                case "command":
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

    public String tellInvalidArguments() {
        return "&cInvalid arguments! %syntax%";
    }

    public String tellInvalidPerms() {
        return "No Permissions";
    }

    public String tellPlayersOnly() {
        return "&cOnly players can use this command";
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
                player.sendMessage(id + " Already exists!");
                return;
            }
            NpcUtils.getUtils().create(player.getLocation(), id);
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid arguments! /npc create <id>"));
        }
    }

    private void deleteCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(id + " Doesn't exists!");
                return;
            }
            NpcUtils.getUtils().delete(id, true);
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage("owo");
        }
    }

    @SneakyThrows
    private void reloadCommand(Player player, String[] args) {
        player.sendMessage("Reloaded");
        NpcUtils.getUtils().hideNpcs();
        RNPC.getPlugin().getNpcConfig().reload();
        Bukkit.getScheduler().runTaskLater(RNPC.getPlugin(), () -> NpcUtils.getUtils().load(), 20L);
    }

    private void setNameCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(id + " Doesn't exists!");
                return;
            }

            String name = "";
            for (int i = 2; i < args.length; i++) {
                name = name + args[i] + " ";
            }

            NpcUtils.getUtils().setName(id, name);

        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid arguments! /npc setname <id> <name>"));
        }
    }

    private void setSkinCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(id + " Doesn't exists!");
                return;
            }

            String skin = args[2];

            NpcUtils.getUtils().setSkin(id, skin);

        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid arguments! /npc setskin <id> <skin>"));
        }
    }

    private void setCustomNameVisibleCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(id + " Doesn't exists!");
                return;
            }
            boolean customName = Boolean.parseBoolean(args[2]);
            NpcUtils.getUtils().showCustomName(id, customName);
            player.sendMessage(Utils.format("&aSuccessfully " + (customName ? "enabled" : "disabled") + " the showing of the name"));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid arguments! /npc setskin <id> <skin>"));
        }
    }

    private void moveCommand(Player player, String[] args) {
        try {
            String id = args[1];
            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(id + " Doesn't exists!");
                return;
            }
            NpcUtils.getUtils().move(id, player.getLocation());
            player.sendMessage(Utils.format("&aSuccessfully moved"));
        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid arguments! /npc move <id>"));
        }
    }

    private void addCommand(Player player, String[] args) {
        try {

            String id = args[1];

            if (!NpcUtils.getUtils().doesExist(id)) {
                player.sendMessage(id + " Doesn't exists!");
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

        } catch (IndexOutOfBoundsException ex) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid arguments! /npc move <id>"));
        }
    }

}
