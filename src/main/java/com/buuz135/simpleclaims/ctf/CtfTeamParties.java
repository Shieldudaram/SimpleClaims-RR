package com.buuz135.simpleclaims.ctf;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.party.PartyInfo;
import com.buuz135.simpleclaims.claim.party.PartyOverride;
import com.buuz135.simpleclaims.claim.party.PartyOverrides;
import com.buuz135.simpleclaims.claim.tracking.ModifiedTracking;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public final class CtfTeamParties {

    public static final int MIN_TEAM_CLAIM_LIMIT = 100;

    private CtfTeamParties() {
    }

    public static PartyInfo ensureTeamParty(CtfTeam team) {
        if (team == null) return null;

        ClaimManager claimManager = ClaimManager.getInstance();

        PartyInfo party = findTeamPartyByName(claimManager, team.partyName());
        if (party == null) {
            // Deterministic ID if we need to create a fresh one, but prefer existing-by-name for backwards compatibility.
            UUID id = UUID.nameUUIDFromBytes(("simpleclaims:ctf:" + team.name().toLowerCase(Locale.ROOT)).getBytes(StandardCharsets.UTF_8));
            party = claimManager.getPartyById(id);
            if (party == null) {
                UUID systemOwner = UUID.nameUUIDFromBytes("simpleclaims:ctf:system".getBytes(StandardCharsets.UTF_8));
                party = new PartyInfo(id, systemOwner, team.partyName(), "CTF arena claims (managed by Realm Ruler)", new UUID[0], team.rgb());
                party.setCreatedTracked(new ModifiedTracking(systemOwner, "RealmRuler", LocalDateTime.now().toString()));
                party.setModifiedTracked(new ModifiedTracking(systemOwner, "RealmRuler", LocalDateTime.now().toString()));
                claimManager.getParties().put(party.getId().toString(), party);
            } else {
                // Ensure the name remains consistent even if the party was created earlier.
                party.setName(team.partyName());
                party.setColor(team.rgb());
            }
        }

        ensureClaimLimitAtLeast(party, MIN_TEAM_CLAIM_LIMIT);
        ClaimManager.getInstance().saveParty(party);
        return party;
    }

    private static PartyInfo findTeamPartyByName(ClaimManager claimManager, String name) {
        if (claimManager == null || name == null) return null;
        for (PartyInfo party : claimManager.getParties().values()) {
            if (party != null && name.equalsIgnoreCase(party.getName())) {
                return party;
            }
        }
        return null;
    }

    private static void ensureClaimLimitAtLeast(PartyInfo party, int min) {
        if (party == null) return;
        if (party.getMaxClaimAmount() >= min) return;
        party.setOverride(new PartyOverride(
                PartyOverrides.CLAIM_CHUNK_AMOUNT,
                new PartyOverride.PartyOverrideValue("integer", min)
        ));
    }
}
