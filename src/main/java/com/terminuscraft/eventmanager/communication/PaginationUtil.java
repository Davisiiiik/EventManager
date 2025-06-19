package com.terminuscraft.eventmanager.communication;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class PaginationUtil {

    public static void sendPaginatedList(CommandSender player, List<String> items, int page, String cmd) {
        int itemsPerPage = 6;
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

        if (page < 1 || page > totalPages) {
            if (page == 1) {
                player.sendMessage(Lang.get("cmd.list.empty"));
            } else {
                player.sendMessage(Lang.get("cmd.list.invalid_page"));
            }
            return;
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // Header
        player.sendMessage(Lang.get("cmd.list.header", false));

        // Items
        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(
                Lang.get("cmd.list.item", Map.of("event", items.get(i)), false)
            );
        }

        // Navigation arrows
        Component navigation = Component.empty();

        if (page > 1) {
            navigation = navigation.append(
                Component.text(Lang.get("cmd.list.prev", false), NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/" + cmd + " " + (page - 1)))
                    .hoverEvent(
                        Component.text(
                            Lang.get(
                                "cmd.list.page",
                                Map.of("page", Integer.toString(page - 1)),
                                false
                            )
                        )
                    )
                );
        } else {
            navigation = navigation.append(
                Component.text(Lang.get("cmd.list.prev", false), NamedTextColor.DARK_GRAY)
            );
        }
        navigation = navigation.append(
            Component.text(
                Lang.get(
                    "cmd.list.footer",
                    Map.of("page", Integer.toString(page), "total", Integer.toString(totalPages)),
                    false
                ),
                NamedTextColor.GRAY)
            );

        if (page < totalPages) {
            navigation = navigation.append(
                Component.text(Lang.get("cmd.list.next", false), NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/" + cmd + " " + (page + 1)))
                    .hoverEvent(
                        Component.text(
                            Lang.get(
                                "cmd.list.page",
                                Map.of("page", Integer.toString(page + 1)),
                                false
                            )
                        )
                    )
                );
        } else {
            navigation = navigation.append(
                Component.text(Lang.get("cmd.list.next", false), NamedTextColor.DARK_GRAY)
            );
        }

        player.sendMessage(navigation);
    }
}

