package com.buuz135.simpleclaims.util;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Permissions {

    public static final String CLAIM_CHUNK_AMOUNT = "simpleclaims.party.claim_chunk_amount";
    public static final String MAX_ADD_CHUNK_AMOUNT = "simpleclaims.admin.max_add_chunk_amount";
    public static final String CLAIM_CHUNK_GAIN_MINUTES = "simpleclaims.player.claim_chunk_gain_minutes";

    public static int getPermissionClaimAmount(UUID uuid) { //TODO Check of admin parties
        return getIntPermission(uuid, CLAIM_CHUNK_AMOUNT);
    }

    public static int getPermissionMaxAddChunkAmount(UUID uuid) {
        return getIntPermission(uuid, MAX_ADD_CHUNK_AMOUNT);
    }

    public static int getPermissionClaimChunkGainMinutes(UUID uuid) {
        return getIntPermission(uuid, CLAIM_CHUNK_GAIN_MINUTES);
    }

    private static int getIntPermission(UUID uuid, String permissionNode) {
        int amount = -1;
        for (PermissionProvider provider : PermissionsModule.get().getProviders()) {
            if (provider.getName().equals("LuckPerms")) {
                for (String perm : LuckPermsHelper.getPerms(uuid)) {
                    if (perm.startsWith(permissionNode + ".")) {
                        try {
                            var parsed = Integer.parseInt(perm.replace(permissionNode + ".", ""));
                            if (parsed > amount) amount = parsed;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                return amount;
            }
            var userNodes = new HashSet<String>();
            userNodes.addAll(provider.getUserPermissions(uuid));
            for (String s : provider.getGroupsForUser(uuid)) {
                userNodes.addAll(provider.getGroupPermissions(s));
            }
            for (String node : userNodes) {
                if (node.startsWith(permissionNode + ".")) {
                    try {
                        var parsed = Integer.parseInt(node.replace(permissionNode + ".", ""));
                        if (parsed > amount) amount = parsed;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return amount;
    }
}
