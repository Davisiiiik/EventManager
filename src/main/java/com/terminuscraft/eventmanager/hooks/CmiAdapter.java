package com.terminuscraft.eventmanager.hooks;

import org.bukkit.entity.Player;

import com.terminuscraft.eventmanager.miscellaneous.Utils;

import net.Zrips.CMILib.Container.CMILocation;
import com.Zrips.CMI.utils.SpawnUtil;

public class CmiAdapter extends Utils {

    public static CmiAdapter getInstance() {
        return new CmiAdapter();
    }

    @Override
    public void sendToSpawn(Player player) {
        CMILocation spawn = SpawnUtil.getSpawnPoint(player);
        player.teleport(spawn);
    }
}
