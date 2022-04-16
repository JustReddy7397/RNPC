package ga.justreddy.wiki.rnpc.utils;

import ga.justreddy.wiki.rnpc.RNPC;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Utility;


public class Utils {

    public static final String CHAT_LINE = "&m-----------------------------------------------------";
    public static final String CONSOLE_LINE ="*-----------------------------------------------------*";
    public static final String LORE_LINE ="&m--------------------------";

    public static String format(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void error(Throwable throwable, String description, boolean disable) {
        if (throwable != null) throwable.printStackTrace();

        if (disable) {
            sendConsole(
                    "&4%line%",
                    "&cAn internal error has occurred in " + RNPC.getPlugin().getDescription().getName() + "!",
                    "&cContact the plugin author if you cannot fix this error.",
                    "&cDescription: &6" + description,
                    "&cThe plugin will now disable.",
                    "&4%line%"
            );
        }else {
            sendConsole(
                    "&4%line%",
                    "&cAn internal error has occurred in " + RNPC.getPlugin().getDescription().getName() + "!",
                    "&cContact the plugin author if you cannot fix this error.",
                    "&cDescription: &6" + description,
                    "&4%line%"
            );
        }

        if (disable && Bukkit.getPluginManager().isPluginEnabled(RNPC.getPlugin())) {
            Bukkit.getPluginManager().disablePlugin(RNPC.getPlugin());
        }

    }

    public static void sendConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(format(message).replace("%line%", CONSOLE_LINE));
    }

    public static void sendConsole(String... message) {
        for(String line : message) {
            sendConsole(line);
        }
    }

}
