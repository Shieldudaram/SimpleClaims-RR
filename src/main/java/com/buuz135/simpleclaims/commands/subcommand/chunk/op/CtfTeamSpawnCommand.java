package com.buuz135.simpleclaims.commands.subcommand.chunk.op;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.commands.CommandMessages;
import com.buuz135.simpleclaims.ctf.CtfTeam;
import com.buuz135.simpleclaims.ctf.CtfTeamParties;
import com.buuz135.simpleclaims.ctf.CtfTeamSpawn;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public class CtfTeamSpawnCommand extends AbstractAsyncCommand {

    private final CtfTeam team;

    public CtfTeamSpawnCommand(CtfTeam team) {
        super("spawn", "Sets the spawn point for this CTF team (and claims the current chunk for the team if needed)");
        this.team = team;
        this.requirePermission(CommandMessages.ADMIN_PERM + "admin-chunk");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        CommandSender sender = commandContext.sender();
        if (sender instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                return CompletableFuture.runAsync(() -> {
                    PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef == null) return;
                    if (!ClaimManager.getInstance().canClaimInDimension(world)) {
                        player.sendMessage(CommandMessages.CANT_CLAIM_IN_THIS_DIMENSION);
                        return;
                    }

                    var party = CtfTeamParties.ensureTeamParty(team);
                    if (party == null) {
                        playerRef.sendMessage(Message.raw("[SimpleClaims] Failed to resolve CTF team party."));
                        return;
                    }

                    var transform = store.getComponent(ref, TransformComponent.getComponentType());
                    if (transform == null) return;

                    double x = transform.getPosition().getX();
                    double y = transform.getPosition().getY();
                    double z = transform.getPosition().getZ();

                    String dimension = player.getWorld().getName();
                    var chunk = ClaimManager.getInstance().getChunkRawCoords(dimension, (int) x, (int) z);
                    if (chunk != null) {
                        if (!chunk.getPartyOwner().equals(party.getId())) {
                            player.sendMessage(Message.raw("[SimpleClaims] This chunk is already claimed by another party; can't set the " + team.displayName() + " spawn here."));
                            return;
                        }
                    } else {
                        if (!ClaimManager.getInstance().hasEnoughClaimsLeft(party)) {
                            player.sendMessage(CommandMessages.NOT_ENOUGH_CHUNKS);
                            return;
                        }
                        var claimed = ClaimManager.getInstance().claimChunkByRawCoords(dimension, (int) x, (int) z, party, player, playerRef);
                        ClaimManager.getInstance().queueMapUpdate(player.getWorld(), claimed.getChunkX(), claimed.getChunkZ());
                    }

                    ClaimManager.getInstance().setCtfTeamSpawn(team, new CtfTeamSpawn(dimension, x, y, z));

                    int chunkX = ChunkUtil.chunkCoordinate((int) x);
                    int chunkZ = ChunkUtil.chunkCoordinate((int) z);
                    player.sendMessage(Message.raw("[SimpleClaims] Set " + team.partyName() + " spawn: " + dimension + " @ (" +
                            String.format("%.2f", x) + ", " + String.format("%.2f", y) + ", " + String.format("%.2f", z) +
                            ") chunk=(" + chunkX + "," + chunkZ + ")"));
                }, world);
            } else {
                commandContext.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                return CompletableFuture.completedFuture(null);
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}

