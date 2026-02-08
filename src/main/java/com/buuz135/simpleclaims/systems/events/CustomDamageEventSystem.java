package com.buuz135.simpleclaims.systems.events;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class CustomDamageEventSystem extends DamageEventSystem {

    @Nullable
    private static final Field PROJECTILE_CREATOR_UUID_FIELD = findProjectileCreatorUuidField();

    @Nullable
    private static Field findProjectileCreatorUuidField() {
        try {
            Field field = ProjectileComponent.class.getDeclaredField("creatorUuid");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl Damage damage) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        PlayerRef victimPlayerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || transformComponent == null || victimPlayerRef == null) return;

        UUID attackerUuid = resolveAttackerPlayerUuid(damage, commandBuffer);
        if (attackerUuid == null) return;

        ClaimManager claimManager = ClaimManager.getInstance();
        Vector3d transform = transformComponent.getPosition();

        var chunk = claimManager.getChunkRawCoords(player.getWorld().getName(), (int) transform.getX(), (int) transform.getZ());
        if (chunk != null) {
            var partyInfo = claimManager.getPartyById(chunk.getPartyOwner());
            if (partyInfo != null && !partyInfo.isPVPEnabled()) {
                damage.setCancelled(true);
            }
        }
        if (damage.isCancelled()) return;

        UUID victimUuid = victimPlayerRef.getUuid();
        UUID attackerCtfParty = claimManager.getRealmRulerTeamParty(attackerUuid);
        UUID victimCtfParty = claimManager.getRealmRulerTeamParty(victimUuid);
        if (attackerCtfParty != null || victimCtfParty != null) {
            if (attackerCtfParty == null || victimCtfParty == null || attackerCtfParty.equals(victimCtfParty)) {
                damage.setCancelled(true);
            }
            return;
        }

        var attackerParty = claimManager.getPartyFromPlayer(attackerUuid);
        var victimParty = claimManager.getPartyFromPlayer(victimUuid);
        if (attackerParty != null && victimParty != null && attackerParty.getId().equals(victimParty.getId()) && !attackerParty.isFriendlyFireEnabled()) {
            damage.setCancelled(true);
        }
    }

    @Nullable
    private UUID resolveAttackerPlayerUuid(@NonNullDecl Damage damage, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) return null;

        UUID attackerUuid = getPlayerUuidFromRef(entitySource.getRef(), commandBuffer);
        if (attackerUuid != null) return attackerUuid;

        if (damage.getSource() instanceof Damage.ProjectileSource projectileSource) {
            UUID projectileRefUuid = getPlayerUuidFromRef(projectileSource.getProjectile(), commandBuffer);
            if (projectileRefUuid != null) return projectileRefUuid;

            return getProjectileCreatorUuid(projectileSource, commandBuffer);
        }

        return null;
    }

    @Nullable
    private UUID getPlayerUuidFromRef(@Nullable Ref<EntityStore> entityRef, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if (entityRef == null || !entityRef.isValid()) return null;
        PlayerRef playerRef = (PlayerRef) commandBuffer.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return null;
        return playerRef.getUuid();
    }

    @Nullable
    private UUID getProjectileCreatorUuid(@NonNullDecl Damage.ProjectileSource projectileSource, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if (PROJECTILE_CREATOR_UUID_FIELD == null) return null;

        Ref<EntityStore> projectileRef = projectileSource.getProjectile();
        if (projectileRef == null || !projectileRef.isValid()) return null;

        ProjectileComponent projectileComponent = (ProjectileComponent) commandBuffer.getComponent(projectileRef, ProjectileComponent.getComponentType());
        if (projectileComponent == null) return null;

        try {
            Object value = PROJECTILE_CREATOR_UUID_FIELD.get(projectileComponent);
            if (value instanceof UUID uuid) return uuid;
        } catch (IllegalAccessException ignored) {
            return null;
        }
        return null;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    @NonNullDecl
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Collections.singleton(RootDependency.first());
    }
}
