package com.buuz135.simpleclaims.systems.tick;

import com.buuz135.simpleclaims.Main;
import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.party.PartyOverride;
import com.buuz135.simpleclaims.claim.party.PartyOverrides;
import com.buuz135.simpleclaims.util.Permissions;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

public class PlayerPlayTimeSystem extends DelayedEntitySystem<EntityStore> {

    public PlayerPlayTimeSystem() {
        super(60);
    }

    @Override
    public void tick(float dt, int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID uuid = playerRef.getUuid();

        var party = ClaimManager.getInstance().getPartyFromPlayer(uuid);
        if (party == null || !party.isOwner(uuid)) return;

        int gainMinutes = Main.CONFIG.get().getClaimChunkGainInMinutes();
        int permissionGainMinutes = Permissions.getPermissionClaimChunkGainMinutes(uuid);
        if (permissionGainMinutes > 0) gainMinutes = permissionGainMinutes;

        if (gainMinutes <= 0) return;

        var nameEntry = ClaimManager.getInstance().getPlayerNameTracker().getNamesMap().get(uuid);
        float currentTime = nameEntry != null ? nameEntry.getPlayTime() : 0f;
        currentTime += 60;

        float targetSeconds = gainMinutes * 60f;
        while (currentTime >= targetSeconds) {
            currentTime -= targetSeconds;

            int currentMax = party.getMaxClaimAmount();
            int maxGain = Main.CONFIG.get().getMaxAddChunkAmount();

            if (currentMax < maxGain) {
                party.setOverride(new PartyOverride(PartyOverrides.CLAIM_CHUNK_AMOUNT, new PartyOverride.PartyOverrideValue("integer", currentMax + 1)));
                ClaimManager.getInstance().saveParty(party);
            } else {
                break; // reached max gain, no point in continuing the loop
            }
        }
        ClaimManager.getInstance().setPlayerPlayTime(uuid, currentTime);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
