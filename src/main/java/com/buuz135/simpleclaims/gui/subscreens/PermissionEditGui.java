package com.buuz135.simpleclaims.gui.subscreens;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.party.PartyInfo;
import com.buuz135.simpleclaims.claim.party.PartyOverrides;
import com.buuz135.simpleclaims.files.DatabaseManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;

public class PermissionEditGui extends GuiWithParent<PermissionEditGui.PermissionData> {

    private final PartyInfo partyInfo;
    private final UUID targetUuid;
    private final boolean isOpEdit;

    public PermissionEditGui(@NonNullDecl PlayerRef playerRef, PartyInfo partyInfo, UUID targetUuid, InteractiveCustomUIPage<?> parent, boolean isOpEdit) {
        super(playerRef, CustomPageLifetime.CanDismiss, PermissionData.CODEC, parent);
        this.partyInfo = partyInfo;
        this.targetUuid = targetUuid;
        this.isOpEdit = isOpEdit;
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl PermissionData data) {
        super.handleDataEvent(ref, store, data);
        if (data.action != null) {
            String permission = null;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_PLACE_BLOCKS))
                permission = PartyOverrides.PARTY_PROTECTION_PLACE_BLOCKS;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_BREAK_BLOCKS))
                permission = PartyOverrides.PARTY_PROTECTION_BREAK_BLOCKS;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_INTERACT))
                permission = PartyOverrides.PARTY_PROTECTION_INTERACT;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_INTERACT_CHEST))
                permission = PartyOverrides.PARTY_PROTECTION_INTERACT_CHEST;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_INTERACT_DOOR))
                permission = PartyOverrides.PARTY_PROTECTION_INTERACT_DOOR;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_INTERACT_BENCH))
                permission = PartyOverrides.PARTY_PROTECTION_INTERACT_BENCH;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_INTERACT_CHAIR))
                permission = PartyOverrides.PARTY_PROTECTION_INTERACT_CHAIR;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_INTERACT_PORTAL))
                permission = PartyOverrides.PARTY_PROTECTION_INTERACT_PORTAL;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_MODIFY_INFO))
                permission = PartyOverrides.PARTY_PROTECTION_MODIFY_INFO;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_CLAIM_UNCLAIM))
                permission = PartyOverrides.PARTY_PROTECTION_CLAIM_UNCLAIM;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_INVITE_PLAYERS))
                permission = PartyOverrides.PARTY_PROTECTION_INVITE_PLAYERS;
            if (data.action.equals(PartyOverrides.PARTY_PROTECTION_ADD_ALLIES))
                permission = PartyOverrides.PARTY_PROTECTION_ADD_ALLIES;

            if (permission != null) {
                boolean currentValue = this.partyInfo.hasPermission(this.targetUuid, permission) || this.partyInfo.hasPartyPermission(this.targetUuid, permission);
                this.partyInfo.setPermission(this.targetUuid, permission, !currentValue);
                long time = System.currentTimeMillis();
                ClaimManager.getInstance().saveParty(this.partyInfo);
                System.out.println("Saved party at " + (System.currentTimeMillis() - time));
            }

            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.build(ref, commandBuilder, eventBuilder, store);
            this.sendUpdate(commandBuilder, eventBuilder, true);
        }
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        super.build(ref, uiCommandBuilder, uiEventBuilder, store);
        var playerCanModify = this.partyInfo.isOwner(this.playerRef.getUuid()) || this.isOpEdit;
        uiCommandBuilder.append("Pages/Buuz135_SimpleClaims_PartyPermissionConfig.ui");

        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#PlaceBlocks", PartyOverrides.PARTY_PROTECTION_PLACE_BLOCKS, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#BreakBlocks", PartyOverrides.PARTY_PROTECTION_BREAK_BLOCKS, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#InteractBlocks", PartyOverrides.PARTY_PROTECTION_INTERACT, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#InteractChest", PartyOverrides.PARTY_PROTECTION_INTERACT_CHEST, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#InteractDoor", PartyOverrides.PARTY_PROTECTION_INTERACT_DOOR, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#InteractBench", PartyOverrides.PARTY_PROTECTION_INTERACT_BENCH, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#InteractChair", PartyOverrides.PARTY_PROTECTION_INTERACT_CHAIR, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#InteractPortal", PartyOverrides.PARTY_PROTECTION_INTERACT_PORTAL, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#ModifyInfo", PartyOverrides.PARTY_PROTECTION_MODIFY_INFO, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#ClaimUnclaim", PartyOverrides.PARTY_PROTECTION_CLAIM_UNCLAIM, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#InvitePlayers", PartyOverrides.PARTY_PROTECTION_INVITE_PLAYERS, playerCanModify);
        addPermissionToggle(uiCommandBuilder, uiEventBuilder, "#AddAllies", PartyOverrides.PARTY_PROTECTION_ADD_ALLIES, playerCanModify);
    }

    private void addPermissionToggle(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder, String element, String permission, boolean playerCanModify) {
        boolean value = this.partyInfo.hasPermission(this.targetUuid, permission) || this.partyInfo.hasPartyPermission(this.targetUuid, permission);
        uiCommandBuilder.set(element + " #CheckBox.Value", value);
        uiCommandBuilder.set(element + " #CheckBox.Disabled", !playerCanModify);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, element + " #CheckBox", EventData.of("Action", permission), false);
    }

    public static class PermissionData extends GuiWithParentData {
        static final String KEY_ACTION = "Action";

        public static final BuilderCodec<PermissionData> CODEC = BuilderCodec.<PermissionData>builder(PermissionData.class, PermissionData::new)
                .addField(new KeyedCodec<>(KEY_ACTION, Codec.STRING), (data, s) -> data.action = s, data -> data.action)
                .addField(new KeyedCodec<>(GuiWithParentData.BACK_INTERACTION, Codec.STRING), (data, s) -> data.backInteraction = s, data -> data.backInteraction)
                .build();

        private String action;
    }
}
