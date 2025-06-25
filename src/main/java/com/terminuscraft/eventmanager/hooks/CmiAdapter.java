package com.terminuscraft.eventmanager.hooks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.terminuscraft.eventmanager.miscellaneous.Utils;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.utils.SpawnUtil;

public class CmiAdapter extends Utils {

    public static CmiAdapter getInstance() {
        return new CmiAdapter();
    }

    @Override
    public void sendToSpawn(Player player) {
        Location spawn = SpawnUtil.getSpawnPoint(player).getBukkitLoc();
        player.teleport(spawn);
    }

    @Override
    public void refreshHolograms() {
        CMI.getInstance().getHologramManager().reload();
    }
}
