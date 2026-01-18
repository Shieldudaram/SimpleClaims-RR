package com.buuz135.simpleclaims.systems.tick;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.buuz135.simpleclaims.claim.party.PartyInfo;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkBordersTickingSystem extends DelayedEntitySystem<EntityStore> {

    private final Random random = new Random();

    public ChunkBordersTickingSystem() {
        super(0.5f);
    }

    @Override
    public void tick(float v, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (!ref.isValid()) return;
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        Vector3d pos = playerRef.getTransform().getPosition();
        int chunkX = ChunkUtil.chunkCoordinate((int) pos.getX());
        int chunkZ = ChunkUtil.chunkCoordinate((int) pos.getZ());
        String dimension = player.getWorld().getName();
        for (int x = chunkX - 1; x <= chunkX + 1; ++x) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; ++z) {
                ChunkInfo chunkInfo = ClaimManager.getInstance().getChunk(dimension, x, z);
                if (chunkInfo != null) {
                    PartyInfo partyInfo = ClaimManager.getInstance().getPartyById(chunkInfo.getPartyOwner());
                    if (partyInfo != null && isNearBorder(pos, x, z)) {
                        spawnChunkBorderParticles(x, z, partyInfo, store, pos, dimension);
                    }
                }
            }
        }
    }

    private boolean isNearBorder(Vector3d pos, int chunkX, int chunkZ) {
        int minX = ChunkUtil.minBlock(chunkX);
        int minZ = ChunkUtil.minBlock(chunkZ);
        int maxX = ChunkUtil.maxBlock(chunkX) + 1;
        int maxZ = ChunkUtil.maxBlock(chunkZ) + 1;

        double distMinX = Math.abs(pos.getX() - minX);
        double distMaxX = Math.abs(pos.getX() - maxX);
        double distMinZ = Math.abs(pos.getZ() - minZ);
        double distMaxZ = Math.abs(pos.getZ() - maxZ);

        double threshold = 5.0;
        return distMinX < threshold || distMaxX < threshold || distMinZ < threshold || distMaxZ < threshold;
    }

    private void spawnChunkBorderParticles(int chunkX, int chunkZ, PartyInfo partyInfo, Store<EntityStore> store, Vector3d playerPos, String dimension) {
        int minX = ChunkUtil.minBlock(chunkX);
        int minZ = ChunkUtil.minBlock(chunkZ);
        int maxX = ChunkUtil.maxBlock(chunkX) + 1;
        int maxZ = ChunkUtil.maxBlock(chunkZ) + 1;

        double threshold = 5.0;
        double playerY = playerPos.getY();
        var particleName = "Buuz135_SimpleClaims_Spawn";
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = (SpatialResource) store.getResource(EntityModule.get().getPlayerSpatialResourceType());
        int colorInt = partyInfo.getColor();
        Color particleColor = new Color((byte) ((colorInt >> 16) & 0xFF), (byte) ((colorInt >> 8) & 0xFF), (byte) (colorInt & 0xFF));
        if (Math.abs(playerPos.getX() - minX) < threshold) {
            ChunkInfo adjacent = ClaimManager.getInstance().getChunk(dimension, chunkX - 1, chunkZ);
            if (adjacent == null || !adjacent.getPartyOwner().equals(partyInfo.getId())) {
                for (double z = minZ; z <= maxZ; z += 0.5) {
                    if (Math.abs(playerPos.getZ() - z) < threshold && random.nextBoolean()) {
                        var pos = new Vector3d(minX, playerY, z + randomOffset());
                        ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
                        playerSpatialResource.getSpatialStructure().collect(playerPos, (double) 75.0F, playerRefs);
                        ParticleUtil.spawnParticleEffect(particleName, pos
                                , 0f, 0f, 0f, 1f, particleColor, playerRefs,
                                store);
                    }
                }
            }
        }
        if (Math.abs(playerPos.getX() - maxX) < threshold) {
            ChunkInfo adjacent = ClaimManager.getInstance().getChunk(dimension, chunkX + 1, chunkZ);
            if (adjacent == null || !adjacent.getPartyOwner().equals(partyInfo.getId())) {
                for (double z = minZ; z <= maxZ; z += 0.5) {
                    if (Math.abs(playerPos.getZ() - z) < threshold && random.nextBoolean()) {
                        var pos = new Vector3d(maxX, playerY, z + randomOffset());
                        ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
                        playerSpatialResource.getSpatialStructure().collect(playerPos, (double) 75.0F, playerRefs);
                        ParticleUtil.spawnParticleEffect(particleName, pos
                                , 0f, 0f, 0f, 1f, particleColor, playerRefs,
                                store);
                    }
                }
            }
        }
        if (Math.abs(playerPos.getZ() - minZ) < threshold) {
            ChunkInfo adjacent = ClaimManager.getInstance().getChunk(dimension, chunkX, chunkZ - 1);
            if (adjacent == null || !adjacent.getPartyOwner().equals(partyInfo.getId())) {
                for (double x = minX; x <= maxX; x += 0.5) {
                    if (Math.abs(playerPos.getX() - x) < threshold && random.nextBoolean()) {
                        var pos = new Vector3d(x + randomOffset(), playerY, minZ);
                        ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
                        playerSpatialResource.getSpatialStructure().collect(playerPos, (double) 75.0F, playerRefs);
                        ParticleUtil.spawnParticleEffect(particleName, pos
                                , 0f, 0f, 0f, 1f, particleColor, playerRefs,
                                store);
                    }
                }
            }
        }
        if (Math.abs(playerPos.getZ() - maxZ) < threshold) {
            ChunkInfo adjacent = ClaimManager.getInstance().getChunk(dimension, chunkX, chunkZ + 1);
            if (adjacent == null || !adjacent.getPartyOwner().equals(partyInfo.getId())) {
                for (double x = minX; x <= maxX; x += 0.5) {
                    if (Math.abs(playerPos.getX() - x) < threshold && random.nextBoolean()) {
                        var pos = new Vector3d(x + randomOffset(), playerY, maxZ);
                        ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
                        playerSpatialResource.getSpatialStructure().collect(playerPos, (double) 75.0F, playerRefs);
                        ParticleUtil.spawnParticleEffect(particleName, pos
                                , 0f, 0f, 0f, 1f, particleColor, playerRefs,
                                store);
                    }
                }
            }
        }
    }

    private double randomOffset() {
        return (random.nextDouble() - 0.5);
    }


    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
