package com.terminuscraft.eventmanager.communication;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PaginationUtil {

    public static void sendPaginatedList(
        CommandSender player, List<String> items, int page, String cmd, String headerName
    ) {
        int itemsPerPage = 8;
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

        if (page < 1 || page > totalPages) {
            if (page == 1) {
                player.sendMessage(Lang.get("paging.empty"));
            } else {
                player.sendMessage(Lang.get("paging.invalid_page"));
            }
            return;
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // Header
        player.sendMessage("");     /* Empty line for better visual separation */
        player.sendMessage(Lang.get("paging.header", Map.of("header", headerName), false));

        // Items
        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(items.get(i));
        }

        /* When CommandSender is not player, we dont need a navigation footer */
        if (!(player instanceof Player)) {
            return;
        }

        // Navigation arrows
        Component navigation = Component.empty();

        if (page > 1) {
            navigation = navigation.append(
                Component.text(Lang.get("paging.prev", false), NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/" + cmd + " " + (page - 1)))
                    .hoverEvent(
                        Component.text(
                            Lang.get(
                                "paging.page",
                                Map.of("page", Integer.toString(page - 1)),
                                false
                            )
                        )
                    )
                );
        } else {
            navigation = navigation.append(
                Component.text(Lang.get("paging.prev", false), NamedTextColor.DARK_GRAY)
            );
        }
        navigation = navigation.append(
            Component.text(
                Lang.get(
                    "paging.footer",
                    Map.of("page", Integer.toString(page), "total", Integer.toString(totalPages)),
                    false
                ),
                NamedTextColor.GOLD)
            );

        if (page < totalPages) {
            navigation = navigation.append(
                Component.text(Lang.get("paging.next", false), NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/" + cmd + " " + (page + 1)))
                    .hoverEvent(
                        Component.text(
                            Lang.get(
                                "paging.page",
                                Map.of("page", Integer.toString(page + 1)),
                                false
                            )
                        )
                    )
                );
        } else {
            navigation = navigation.append(
                Component.text(Lang.get("paging.next", false), NamedTextColor.DARK_GRAY)
            );
        }

        player.sendMessage(navigation);
    }
}

