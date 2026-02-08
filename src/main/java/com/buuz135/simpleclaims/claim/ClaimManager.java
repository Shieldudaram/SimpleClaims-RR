package com.buuz135.simpleclaims.claim;

import com.buuz135.simpleclaims.Main;
import com.buuz135.simpleclaims.claim.party.PartyInvite;
import com.buuz135.simpleclaims.commands.CommandMessages;
import com.buuz135.simpleclaims.ctf.CtfTeam;
import com.buuz135.simpleclaims.ctf.CtfTeamSpawn;
import com.buuz135.simpleclaims.files.*;
import com.buuz135.simpleclaims.util.FileUtils;
import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.buuz135.simpleclaims.claim.party.PartyInfo;
import com.buuz135.simpleclaims.claim.player_name.PlayerNameTracker;
import com.buuz135.simpleclaims.claim.tracking.ModifiedTracking;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.Locale;

public class ClaimManager {

    private static final ClaimManager INSTANCE = new ClaimManager();

    private final Map<UUID, UUID> adminUsageParty;
    private final Map<UUID, PartyInvite> partyInvites;
    private final Map<UUID, UUID> playerToParty;
    private final Map<UUID, Integer> partyClaimCounts;
    private final Map<UUID, UUID> realmRulerTeamParty;
    private final Map<CtfTeam, CtfTeamSpawn> ctfTeamSpawns;
    private Set<String> worldsNeedingUpdates;
    private HytaleLogger logger = HytaleLogger.getLogger().getSubLogger("SimpleClaims");
    private PlayerNameTracker playerNameTracker;
    private HashMap<String, PartyInfo> parties;
    private HashMap<String, HashMap<String, ChunkInfo>> chunks;
    private final Object partyMaintenanceLock = new Object();
    private Set<UUID> adminOverrides;
    private DatabaseManager databaseManager;
    private HashMap<String, LongSet> mapUpdateQueue;

    public static ClaimManager getInstance() {
        return INSTANCE;
    }

    private ClaimManager() {
        this.adminUsageParty = new ConcurrentHashMap<>();
        this.worldsNeedingUpdates = new HashSet<>();
        this.partyInvites = new ConcurrentHashMap<>();
        this.playerToParty = new ConcurrentHashMap<>();
        this.partyClaimCounts = new ConcurrentHashMap<>();
        this.realmRulerTeamParty = new ConcurrentHashMap<>();
        this.ctfTeamSpawns = new ConcurrentHashMap<>();
        this.parties = new HashMap<>();
        this.chunks = new HashMap<>();
        this.playerNameTracker = new PlayerNameTracker();
        this.adminOverrides = new HashSet<>();
        this.databaseManager = new DatabaseManager(logger);
        this.mapUpdateQueue = new HashMap<>();

        FileUtils.ensureMainDirectory();

        logger.at(Level.INFO).log("Loading simple claims data...");

        if (this.databaseManager.isMigrationNecessary()) {
            logger.at(Level.INFO).log("Migration needed, loading JSON files...");
            PartyBlockingFile partyBlockingFile = new PartyBlockingFile();
            ClaimedChunkBlockingFile claimedChunkBlockingFile = new ClaimedChunkBlockingFile();
            PlayerNameTrackerBlockingFile playerNameTrackerBlockingFile = new PlayerNameTrackerBlockingFile();
            AdminOverridesBlockingFile adminOverridesBlockingFile = new AdminOverridesBlockingFile();

            if (new File(FileUtils.PARTY_PATH).exists()) {
                FileUtils.loadWithBackup(partyBlockingFile::syncLoad, FileUtils.PARTY_PATH, logger);
            }
            if (new File(FileUtils.CLAIM_PATH).exists()) {
                FileUtils.loadWithBackup(claimedChunkBlockingFile::syncLoad, FileUtils.CLAIM_PATH, logger);
            }
            if (new File(FileUtils.NAMES_CACHE_PATH).exists()) {
                FileUtils.loadWithBackup(playerNameTrackerBlockingFile::syncLoad, FileUtils.NAMES_CACHE_PATH, logger);
            }
            if (new File(FileUtils.ADMIN_OVERRIDES_PATH).exists()) {
                FileUtils.loadWithBackup(adminOverridesBlockingFile::syncLoad, FileUtils.ADMIN_OVERRIDES_PATH, logger);
            }

            this.databaseManager.migrate(partyBlockingFile, claimedChunkBlockingFile, playerNameTrackerBlockingFile, adminOverridesBlockingFile);
        }

        logger.at(Level.INFO).log("Loading party data from DB...");
        this.parties.putAll(this.databaseManager.loadParties());
        for (PartyInfo party : this.parties.values()) {
            for (UUID member : party.getMembers()) {
                playerToParty.put(member, party.getId());
            }
        }

        logger.at(Level.INFO).log("Loading chunk data from DB...");
        this.chunks.putAll(this.databaseManager.loadClaims());
        for (HashMap<String, ChunkInfo> dimensionChunks : this.chunks.values()) {
            for (ChunkInfo chunk : dimensionChunks.values()) {
                partyClaimCounts.merge(chunk.getPartyOwner(), 1, Integer::sum);
            }
        }

        logger.at(Level.INFO).log("Loading name cache data from DB...");
        PlayerNameTracker tracker = this.databaseManager.loadNameCache();
        for (PlayerNameTracker.PlayerName name : tracker.getNames()) {
            this.playerNameTracker.setPlayerName(name.getUuid(), name.getName(), name.getLastSeen(), name.getPlayTime());
        }

        logger.at(Level.INFO).log("Loading admin overrides data from DB...");
        this.adminOverrides.addAll(this.databaseManager.loadAdminOverrides());

        logger.at(Level.INFO).log("Loading CTF team spawns from DB...");
        try {
            var loaded = this.databaseManager.loadCtfTeamSpawns();
            for (Map.Entry<String, CtfTeamSpawn> e : loaded.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                CtfTeam team = CtfTeam.fromString(e.getKey());
                if (team == null) continue;
                this.ctfTeamSpawns.put(team, e.getValue());
            }
        } catch (Throwable t) {
            logger.at(Level.WARNING).withCause(t).log("Failed to load CTF team spawns from DB");
        }

    }

    public void saveParty(PartyInfo partyInfo) {
        this.databaseManager.saveParty(partyInfo);
    }

    private void saveClaim(String dimension, ChunkInfo chunkInfo) {
        this.databaseManager.saveClaim(dimension, chunkInfo);
    }

    private void saveNameCache(UUID uuid, String name, long lastSeen, float playTime) {
        this.databaseManager.saveNameCache(uuid, name, lastSeen, playTime);
    }

    private void saveAdminOverride(UUID uuid) {
        this.databaseManager.saveAdminOverride(uuid);
    }

    public void addParty(PartyInfo partyInfo){
        this.parties.put(partyInfo.getId().toString(), partyInfo);
        this.saveParty(partyInfo);
    }

    public boolean isAllowedToInteract(UUID playerUUID, String dimension, int chunkX, int chunkZ, Predicate<PartyInfo> interactMethod, String permission) {
        if (playerUUID != null && adminOverrides.contains(playerUUID)) return true;

        var chunkInfo = getChunkRawCoords(dimension, chunkX, chunkZ);
        if (chunkInfo == null) return !Arrays.asList(Main.CONFIG.get().getFullWorldProtection()).contains(dimension);

        var chunkParty = getPartyById(chunkInfo.getPartyOwner());
        if (chunkParty == null) return true;
        
        // If playerUUID is null (e.g., explosion with unknown source), deny in claimed chunks
        if (playerUUID == null) return false;

        UUID rrTeamParty = realmRulerTeamParty.get(playerUUID);
        boolean isRealmRulerTeamMember = rrTeamParty != null && rrTeamParty.equals(chunkParty.getId());

        if (chunkParty.isOwnerOrMember(playerUUID) || chunkParty.isPlayerAllied(playerUUID) || isRealmRulerTeamMember) {
            if (isRealmRulerTeamMember) return true;
            if (chunkParty.hasPermission(playerUUID, permission)) return true;
            return false;
        }

        var partyId = playerToParty.get(playerUUID);
        if (partyId != null && chunkParty.isPartyAllied(partyId)) {
            if (chunkParty.hasPartyPermission(partyId, permission)) return true;
            return false;
        }

        //Interact check for players that arent party members or allies
        return interactMethod.test(chunkParty);
    }

    @Nullable
    public PartyInfo getPartyFromPlayer(UUID player) {
        UUID partyId = playerToParty.get(player);
        if (partyId == null) return null;
        return getPartyById(partyId);
    }

    @Nullable
    public PartyInfo getPartyById(UUID partyId){
        return this.parties.get(partyId.toString());
    }

    public PartyInfo createParty(Player owner, PlayerRef playerRef, boolean isAdmin) {
        var party = new PartyInfo(UUID.randomUUID(), playerRef.getUuid(), owner.getDisplayName() + "'s Party", owner.getDisplayName() + "'s Party Description", new UUID[0], Color.getHSBColor(new Random().nextFloat(), 1, 1).getRGB());
        party.addMember(playerRef.getUuid());
        party.setCreatedTracked(new ModifiedTracking(playerRef.getUuid(), owner.getDisplayName(), LocalDateTime.now().toString()));
        party.setModifiedTracked(new ModifiedTracking(playerRef.getUuid(), owner.getDisplayName(), LocalDateTime.now().toString()));
        this.parties.put(party.getId().toString(), party);
        if (!isAdmin) this.playerToParty.put(playerRef.getUuid(), party.getId());
        this.saveParty(party);
        return party;
    }

    public boolean canClaimInDimension(World world){
        if (world.getWorldConfig().isDeleteOnRemove()) return false;
        if (world.getName().contains("Gaia_Temple")) return false;
        if (Arrays.asList(Main.CONFIG.get().getWorldNameBlacklistForClaiming()).contains(world.getName())) return false;
        return true;
    }

    @Nullable
    public ChunkInfo getChunk(String dimension, int chunkX, int chunkZ){
        var chunkInfo = this.chunks.computeIfAbsent(dimension, k -> new HashMap<>());
        var formattedChunk = ChunkInfo.formatCoordinates(chunkX, chunkZ);
        return chunkInfo.getOrDefault(formattedChunk, null);
    }

    @Nullable
    public ChunkInfo getChunkRawCoords(String dimension, int blockX, int blockZ){
        return this.getChunk(dimension, ChunkUtil.chunkCoordinate(blockX), ChunkUtil.chunkCoordinate(blockZ));
    }

    public ChunkInfo claimChunkBy(String dimension, int chunkX, int chunkZ, PartyInfo partyInfo, Player owner, PlayerRef playerRef) {
        var chunkInfo = new ChunkInfo(partyInfo.getId(), chunkX, chunkZ);
        var chunkDimension = this.chunks.computeIfAbsent(dimension, k -> new HashMap<>());
        chunkDimension.put(ChunkInfo.formatCoordinates(chunkX, chunkZ), chunkInfo);
        chunkInfo.setCreatedTracked(new ModifiedTracking(playerRef.getUuid(), owner.getDisplayName(), LocalDateTime.now().toString()));
        partyClaimCounts.merge(partyInfo.getId(), 1, Integer::sum);
        this.databaseManager.saveClaim(dimension, chunkInfo);
        return chunkInfo;
    }

    public ChunkInfo claimChunkByRawCoords(String dimension, int blockX, int blockZ, PartyInfo partyInfo, Player owner, PlayerRef playerRef) {
        return this.claimChunkBy(dimension, ChunkUtil.chunkCoordinate(blockX), ChunkUtil.chunkCoordinate(blockZ), partyInfo, owner, playerRef);
    }

    public boolean hasEnoughClaimsLeft(PartyInfo partyInfo) {
        int maxAmount = partyInfo.getMaxClaimAmount();
        int currentAmount = partyClaimCounts.getOrDefault(partyInfo.getId(), 0);
        return currentAmount < maxAmount;
    }

    public int getAmountOfClaims(PartyInfo partyInfo) {
        return partyClaimCounts.getOrDefault(partyInfo.getId(), 0);
    }

    public void unclaim(String dimension, int chunkX, int chunkZ) {
        var chunkMap = this.chunks.get(dimension);
        if (chunkMap != null) {
            ChunkInfo removed = chunkMap.remove(ChunkInfo.formatCoordinates(chunkX, chunkZ));
            if (removed != null) {
                partyClaimCounts.computeIfPresent(removed.getPartyOwner(), (k, v) -> v > 1 ? v - 1 : null);
                databaseManager.deleteClaim(dimension, chunkX, chunkZ);
            }
        }
    }

    public void unclaimRawCoords(String dimension, int blockX, int blockZ){
        this.unclaim(dimension, ChunkUtil.chunkCoordinate(blockX), ChunkUtil.chunkCoordinate(blockZ));
    }

    public Set<String> getWorldsNeedingUpdates() {
        return worldsNeedingUpdates;
    }

    public void setNeedsMapUpdate(String world) {
        this.worldsNeedingUpdates.add(world);
    }

    public void setPlayerName(UUID uuid, String name, long lastSeen) {
        var existing = this.playerNameTracker.getNamesMap().get(uuid);
        float playTime = existing != null ? existing.getPlayTime() : 0;
        this.playerNameTracker.setPlayerName(uuid, name, lastSeen, playTime);
        this.databaseManager.saveNameCache(uuid, name, lastSeen, playTime);
    }

    public void setPlayerPlayTime(UUID uuid, float playTime) {
        var existing = this.playerNameTracker.getNamesMap().get(uuid);
        if (existing != null) {
            if (Math.abs(existing.getPlayTime() - playTime) < 0.01) return;
            this.playerNameTracker.setPlayerName(uuid, existing.getName(), existing.getLastSeen(), playTime);
            this.databaseManager.saveNameCache(uuid, existing.getName(), existing.getLastSeen(), playTime);
        }
    }

    public PlayerNameTracker getPlayerNameTracker() {
        return playerNameTracker;
    }

    public HashMap<String, PartyInfo> getParties() {
        return parties;
    }

    public HashMap<String, HashMap<String, ChunkInfo>> getChunks() {
        return this.chunks;
    }

    public Map<UUID, UUID> getAdminUsageParty() {
        return adminUsageParty;
    }

    public void invitePlayerToParty(PlayerRef recipient, PartyInfo partyInfo, PlayerRef sender) {
        this.partyInvites.put(recipient.getUuid(), new PartyInvite(recipient.getUuid(), sender.getUuid(), partyInfo.getId()));
    }

    public PartyInvite acceptInvite(PlayerRef player) {
        var invite = this.partyInvites.get(player.getUuid());
        if (invite == null) return null;
        var party = this.getPartyById(invite.party());
        if (party == null) return null;
        if (Main.CONFIG.get().getMaxPartyMembers() != -1 && party.getMembers().length >= Main.CONFIG.get().getMaxPartyMembers()) {
            this.partyInvites.remove(player.getUuid());
            return null;
        }
        party.addMember(player.getUuid());
        this.playerToParty.put(player.getUuid(), party.getId());
        this.partyInvites.remove(player.getUuid());
        databaseManager.saveParty(party);
        return invite;
    }

    public Map<UUID, PartyInvite> getPartyInvites() {
        return partyInvites;
    }

    public void leaveParty(PlayerRef player, PartyInfo partyInfo) {
        this.playerToParty.remove(player.getUuid());

        if (partyInfo.isOwner(player.getUuid())) {
            disbandParty(partyInfo);
            player.sendMessage(CommandMessages.PARTY_DISBANDED);
            return;
        } else {
            partyInfo.removeMember(player.getUuid());
            playerToParty.remove(player.getUuid());
            player.sendMessage(CommandMessages.PARTY_LEFT);
        }
        databaseManager.saveParty(partyInfo);
    }

    public void disbandParty(PartyInfo partyInfo) {
        if (partyInfo == null) return;
        synchronized (partyMaintenanceLock) {
            for (UUID member : partyInfo.getMembers()) {
                playerToParty.remove(member);
            }
            queueMapUpdateForParty(partyInfo);
            this.chunks.forEach((dimension, chunkInfos) -> chunkInfos.values().removeIf(chunkInfo -> {
                boolean matches = chunkInfo.getPartyOwner().equals(partyInfo.getId());
                if (matches) {
                    databaseManager.deleteClaim(dimension, chunkInfo.getChunkX(), chunkInfo.getChunkZ());
                }
                return matches;
            }));
            partyClaimCounts.remove(partyInfo.getId());

            this.parties.remove(partyInfo.getId().toString());
            databaseManager.deleteParty(partyInfo.getId());
        }
    }

    public void removeAdminOverride(UUID uuid) {
        if (this.adminOverrides.remove(uuid)) {
            databaseManager.deleteAdminOverride(uuid);
        }
    }

    public void addAdminOverride(UUID uuid) {
        if (this.adminOverrides.add(uuid)) {
            databaseManager.saveAdminOverride(uuid);
        }
    }

    public Set<UUID> getAdminClaimOverrides() {
        return adminOverrides;
    }

    public void queueMapUpdateForParty(PartyInfo partyInfo) {
        this.getChunks().forEach((dimension, chunkInfos) -> {
            var world = Universe.get().getWorlds().get(dimension);
            if (world != null) {
                for (ChunkInfo value : chunkInfos.values()) {
                    if (value.getPartyOwner().equals(partyInfo.getId())) {
                        queueMapUpdate(world, value.getChunkX(), value.getChunkZ());
                    }
                }
            }
        });
    }

    public void queueMapUpdate(World world, int chunkX, int chunkZ) {
        if (!mapUpdateQueue.containsKey(world.getName())) {
            mapUpdateQueue.put(world.getName(), new LongOpenHashSet());
        }
        mapUpdateQueue.get(world.getName()).add(ChunkUtil.indexChunk(chunkX, chunkZ));
        mapUpdateQueue.get(world.getName()).add(ChunkUtil.indexChunk(chunkX + 1, chunkZ));
        mapUpdateQueue.get(world.getName()).add(ChunkUtil.indexChunk(chunkX - 1, chunkZ));
        mapUpdateQueue.get(world.getName()).add(ChunkUtil.indexChunk(chunkX, chunkZ + 1));
        mapUpdateQueue.get(world.getName()).add(ChunkUtil.indexChunk(chunkX, chunkZ - 1));
        this.setNeedsMapUpdate(world.getName());
    }

    public HashMap<String, LongSet> getMapUpdateQueue() {
        return mapUpdateQueue;
    }

    public Map<UUID, UUID> getPlayerToParty() {
        return playerToParty;
    }

    public void setRealmRulerTeamParty(@Nullable UUID playerUuid, @Nullable UUID partyId) {
        if (playerUuid == null) return;
        if (partyId == null) {
            realmRulerTeamParty.remove(playerUuid);
        } else {
            realmRulerTeamParty.put(playerUuid, partyId);
        }
    }

    public void clearRealmRulerTeamParty(@Nullable UUID playerUuid) {
        if (playerUuid == null) return;
        realmRulerTeamParty.remove(playerUuid);
    }

    public void clearAllRealmRulerTeamParties() {
        realmRulerTeamParty.clear();
    }

    @Nullable
    public UUID getRealmRulerTeamParty(@Nullable UUID playerUuid) {
        if (playerUuid == null) return null;
        return realmRulerTeamParty.get(playerUuid);
    }

    @Nullable
    public CtfTeamSpawn getCtfTeamSpawn(@Nullable CtfTeam team) {
        if (team == null) return null;
        return ctfTeamSpawns.get(team);
    }

    public void setCtfTeamSpawn(@Nullable CtfTeam team, @Nullable CtfTeamSpawn spawn) {
        if (team == null || spawn == null) return;
        ctfTeamSpawns.put(team, spawn);
        this.databaseManager.saveCtfTeamSpawn(team.name().toLowerCase(Locale.ROOT), spawn.dimension(), spawn.x(), spawn.y(), spawn.z());
    }

    public void disbandInactiveParties() {
        synchronized (partyMaintenanceLock) {
            int inactivityHours = Main.CONFIG.get().getPartyInactivityHours();
            if (inactivityHours < 0) return;
            long inactivityMillis = inactivityHours * 60L * 60L * 1000L;
            long currentTime = System.currentTimeMillis();
            List<PartyInfo> toDisband = new ArrayList<>();
            for (PartyInfo party : parties.values()) {
                if (party.getOwner() == null && (party.getMembers() == null || party.getMembers().length == 0)) {
                    continue; // Ignore if no owner and no members
                }
                boolean allInactive = true;
                if (party.getOwner() != null) {
                    var playerName = playerNameTracker.getNamesMap().get(party.getOwner());
                    if (playerName == null || playerName.getLastSeen() <= 0 || Universe.get().getPlayer(party.getOwner()) != null) { //Check if online also
                        allInactive = false; // Ignore if lastSeen is missing
                    } else if (currentTime - playerName.getLastSeen() < inactivityMillis) {
                        allInactive = false;
                    }
                }
                if (allInactive && party.getMembers() != null) {
                    for (UUID member : party.getMembers()) {
                        var playerName = playerNameTracker.getNamesMap().get(member);
                        if (playerName == null || playerName.getLastSeen() <= 0 || Universe.get().getPlayer(member) != null) { //Check if online also
                            allInactive = false; // Ignore if lastSeen is missing
                            break;
                        } else if (currentTime - playerName.getLastSeen() < inactivityMillis) {
                            allInactive = false;
                            break;
                        }
                    }
                }
                if (allInactive) {
                    toDisband.add(party);
                }
            }
            for (PartyInfo party : toDisband) {
                logger.at(Level.INFO).log("Disbanding inactive party: " + party.getName() + " (" + party.getId() + ")");
                disbandParty(party);
            }
        }
    }
}
