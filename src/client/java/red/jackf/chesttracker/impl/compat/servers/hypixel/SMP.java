package red.jackf.chesttracker.impl.compat.servers.hypixel;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

interface SMP {
    static boolean isSMPJoinMessage(Component message) {
        return message.getString().startsWith("SMP ID: ")
                && message.getStyle().getClickEvent() != null
                && message.getStyle().getClickEvent().getAction() == ClickEvent.Action.SUGGEST_COMMAND;
    }
}
