package ch.framedev.essentialsmod.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

public class ChatUtils {

    /**
     * Creates a TextComponent with a specific color/style.
     *
     * @param message        The message to be displayed.
     * @param chatFormatting The color/style to apply.
     * @return A styled TextComponent.
     */
    public static TextComponent getColoredTextComponent(String message, ChatFormatting chatFormatting) {
        return (TextComponent) new TextComponent(message).withStyle(chatFormatting);
    }

    /**
     * Combines multiple messages into a single TextComponent, applying specified color codes.
     *
     * @param messages              The array of messages to combine.
     * @param patternToReplaceColor An array of color codes for each message.
     * @return A combined TextComponent with applied colors.
     * @throws IllegalArgumentException if arrays have mismatched lengths or invalid color codes.
     */
    public static TextComponent getTextComponent(String[] messages, String[] patternToReplaceColor) {
        if (messages.length != patternToReplaceColor.length) {
            throw new IllegalArgumentException("Pattern array must have the same length as the messages array.");
        }

        TextComponent combinedComponent = new TextComponent("");

        for (int i = 0; i < messages.length; i++) {
            ChatFormatting formatting = ChatColor.getByColorCode(patternToReplaceColor[i]);
            if (formatting == null) {
                throw new IllegalArgumentException("Invalid color code: " + patternToReplaceColor[i]);
            }
            combinedComponent.append(new TextComponent(messages[i]).withStyle(formatting));
            if (i < messages.length - 1) {
                combinedComponent.append(" "); // Add space between messages
            }
        }

        return combinedComponent;
    }

    private static TextComponent getTextComponentForPrefix(String[] messages, String[] patternToReplaceColor) {
        if (messages.length != patternToReplaceColor.length) {
            throw new IllegalArgumentException("Pattern array must have the same length as the messages array");
        }

        TextComponent combinedComponent = new TextComponent("");
        for (int i = 0; i < messages.length; i++) {
            // Create a new styled TextComponent for each message
            TextComponent part = new TextComponent(messages[i]);
            ChatFormatting chatFormatting = ChatColor.getByColorCode(patternToReplaceColor[i]);
            if (chatFormatting != null) {
                part.withStyle(chatFormatting); // Apply style
            }
            combinedComponent.append(part); // Append to a combined component
        }

        return combinedComponent;
    }


    public static TextComponent getPrefix() {
        String[] messages = new String[]{
                "[",
                "Essentials",
                "]",
                " » ",
                ""
        };
        String[] patternToReplaceColor = new String[]{
                "§a",
                "§6",
                "§a",
                "§c",
                "§f"
        };
        return getTextComponentForPrefix(messages, patternToReplaceColor);
    }

    /**
     * Enum representing chat colors and styles for Minecraft, including their respective codes.
     */
    public enum ChatColor {
        GRAY(ChatFormatting.GRAY, "§7"),
        RED(ChatFormatting.RED, "§c"),
        BLUE(ChatFormatting.BLUE, "§9"),
        GOLD(ChatFormatting.GOLD, "§6"),
        GREEN(ChatFormatting.GREEN, "§a"),
        YELLOW(ChatFormatting.YELLOW, "§e"),
        AQUA(ChatFormatting.AQUA, "§b"),
        WHITE(ChatFormatting.WHITE, "§f"),
        LIGHT_PURPLE(ChatFormatting.LIGHT_PURPLE, "§d"),
        DARK_PURPLE(ChatFormatting.DARK_PURPLE, "§5"),
        DARK_BLUE(ChatFormatting.DARK_BLUE, "§1"),
        DARK_GREEN(ChatFormatting.DARK_GREEN, "§2"),
        DARK_AQUA(ChatFormatting.DARK_AQUA, "§3"),
        DARK_RED(ChatFormatting.DARK_RED, "§4"),
        DARK_GRAY(ChatFormatting.DARK_GRAY, "§8"),
        BLACK(ChatFormatting.BLACK, "§0"),
        RESET(ChatFormatting.RESET, "§r"),
        ITALIC(ChatFormatting.ITALIC, "§o"),
        BOLD(ChatFormatting.BOLD, "§l"),
        STRIKETHROUGH(ChatFormatting.STRIKETHROUGH, "§m"),
        UNDERLINE(ChatFormatting.UNDERLINE, "§n"),
        OBFUSCATED(ChatFormatting.OBFUSCATED, "§k");

        private final ChatFormatting chatFormatting;
        private final String colorCode;

        ChatColor(ChatFormatting chatFormatting, String colorCode) {
            this.chatFormatting = chatFormatting;
            this.colorCode = colorCode;
        }

        /**
         * Gets the ChatFormatting enum associated with this color.
         *
         * @return The ChatFormatting enum.
         */
        public ChatFormatting getChatFormatting() {
            return chatFormatting;
        }

        /**
         * Gets the Minecraft color code string (e.g., §a).
         *
         * @return The color code string.
         */
        public String getColorCode() {
            return colorCode;
        }

        /**
         * Finds a ChatFormatting style by its color code.
         *
         * @param colorCode The color code to lookup (e.g., §a).
         * @return The corresponding ChatFormatting or null if not found.
         */
        public static ChatFormatting getByColorCode(String colorCode) {
            for (ChatColor chatColor : ChatColor.values()) {
                if (chatColor.getColorCode().equalsIgnoreCase(colorCode)) {
                    return chatColor.getChatFormatting();
                }
            }
            return null; // Return null if the color code is not found
        }
    }
}