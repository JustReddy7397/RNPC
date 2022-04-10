package ga.justreddy.wiki.rnpc.commands;

import com.github.helpfuldeer.commandlib.BaseCommand;
import com.github.helpfuldeer.commandlib.SuperCommand;
import ga.justreddy.wiki.rnpc.RNPC;
import ga.justreddy.wiki.rnpc.npc.NpcUtils;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)  {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            try{
                if(args[0].equals("create")) createCommand(player, args);
                else if(args[0].equals("delete")) deleteCommand(player, args);
                else if(args[0].equals("reload")) reloadCommand(player, args);
            }catch (IndexOutOfBoundsException ex){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "Invalid arguments!"));
            }
        }
        return false;
    }

    private void createCommand(Player player, String[] args) {
        try{
            String id = args[1];
            if(NpcUtils.getUtils().doesExist(id.toLowerCase())){
                player.sendMessage(id + " Already exists!");
                return;
            }
            NpcUtils.getUtils().create(player.getLocation().getBlock().getLocation().add(0.0, 0.0, 0.5), id);
        }catch (IndexOutOfBoundsException ex) {
            player.sendMessage("owo");
        }
    }

    private void deleteCommand(Player player, String[] args) {
        try{
            String id = args[1];
            if(!NpcUtils.getUtils().doesExist(id.toLowerCase())){
                player.sendMessage(id + " Doesn't exists!");
                return;
            }
            NpcUtils.getUtils().delete(id);
        }catch (IndexOutOfBoundsException ex) {
            player.sendMessage("owo");
        }
    }

    @SneakyThrows
    private void reloadCommand(Player player, String[] args) {
        player.sendMessage("Reloaded");
        RNPC.getPlugin().getNpcConfig().reload();
    }

}
