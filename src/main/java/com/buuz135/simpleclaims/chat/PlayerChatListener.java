package com.buuz135.simpleclaims.chat;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PlayerChatListener implements Function<CompletableFuture<PlayerChatEvent>, CompletableFuture<PlayerChatEvent>> {

    @Override
    public CompletableFuture<PlayerChatEvent> apply(final CompletableFuture<PlayerChatEvent> playerChatEventCompletableFuture) {
        return playerChatEventCompletableFuture.thenApply((event) -> {
            if (!PartyChatManager.getInstance().isPlayerToggledPartyChat(event.getSender().getUuid())) {
                return event;
            }

            final var sender = event.getSender();
            final var partyInfo = ClaimManager.getInstance().getPartyFromPlayer(sender.getUuid());
            if (partyInfo == null) return event;

            final var content = event.getContent();
            final var partyName = partyInfo.getName();
            event.setContent("[" + partyName + "] " + content);

            final var members = ClaimManager.getInstance().getParties().get(partyInfo.getId().toString()).getMembers();
            final List<PlayerRef> targets = new ArrayList<>();

            for (final UUID uuid : members) {
                final var player = Universe.get().getPlayer(uuid);
                if (player != null && player.isValid()) {
                    targets.add(player);
                }
            }

            event.setTargets(targets);
            return event;
        });
    }
}
