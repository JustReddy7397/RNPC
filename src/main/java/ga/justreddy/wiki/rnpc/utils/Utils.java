package ga.justreddy.wiki.rnpc.utils;

import ga.justreddy.wiki.rnpc.RNPC;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Utility;
import org.bukkit.entity.Player;

import java.util.Arrays;


public class Utils {

    private static final int CENTER_PX = 154;
    public static final String CHAT_LINE = "&m-----------------------------------------------------";
    public static final String CONSOLE_LINE = "*-----------------------------------------------------*";
    public static final String LORE_LINE = "&m--------------------------";

    public static String format(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%line%", CHAT_LINE)));
    }

    public static void sendMessage(Player player, String... message) {
        for (String line : message) {
            sendMessage(player, line);
        }
    }

    public static String getCenteredMessage(String message) {
        if (message == null || message.equals("")) return "";

        message = format(message);
        message = message.replace("<center>", "").replace("</center>", "");

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '�') {
                previousCode = true;

            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';

            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + message;

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
        } else {
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
        Bukkit.getConsoleSender().sendMessage(format(message.replace("%line%", CONSOLE_LINE)));
    }

    public static void sendConsole(String... message) {
        for (String line : message) {
            sendConsole(line);
        }
    }

}
