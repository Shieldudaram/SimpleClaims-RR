# Simple Claims Configuration

This document explains all the configuration options available in the `SimpleClaims` mod.

## Integration Ownership and Test Scope

Use this matrix when validating behavior across SimpleClaims and RealmRuler integrations.

| Concern | Owner | Validation Scope |
| --- | --- | --- |
| Block break/place/interact claim protection | SimpleClaims | Validate directly in SimpleClaims first. |
| PvP and friendly-fire enforcement in claims | SimpleClaims | Validate directly in SimpleClaims first. |
| CTF team assignment lifecycle | RealmRuler via SimpleClaims bridge | Validate after core SimpleClaims checks. |
| `rr*` bridge method contract | Shared contract (do not rename/remove in SimpleClaims) | Validate with RealmRuler integration run. |

Recommended validation order:
1. Run SimpleClaims-only checks (claim assignment + block protection + PvP behavior).
2. Run integration checks with RealmRuler (team assignment/clear, then repeat CTF combat checks).

## Default Party Settings

These settings define the initial permissions and limits for newly created parties.

- **DefaultPartyClaimsAmount** (Integer, Default: `25`): The maximum number of chunks a party can claim by default.
- **MaxAddChunkAmount** (Integer, Default: `100`): The maximum amount of chunks a party can have when using the
  `add-chunk-amount` command or gain by playtime.
- **ClaimChunkGainInMinutes** (Integer, Default: `-1`): How often (in minutes) a player will gain another claim chunk by
  play time. Use `-1` to disable this feature.
- **MaxPartyMembers** (Integer, Default: `-1`): The maximum number of members a party can have. Use `-1` for no limit.
- **MaxPartyAllies** (Integer, Default: `-1`): The maximum number of allies (both players and other parties) a party can
  have. Use `-1` for no limit.
- **DefaultPartyBlockPlaceEnabled** (Boolean, Default: `false`): Whether block placement is allowed for non-members in
  claimed chunks by default.
- **DefaultPartyBlockBreakEnabled** (Boolean, Default: `false`): Whether block breaking is allowed for non-members in
  claimed chunks by default.
- **DefaultPartyBlockInteractEnabled** (Boolean, Default: `false`): Whether general block interaction is allowed for
  non-members in claimed chunks by default.
- **DefaultPartyPVPEnabled** (Boolean, Default: `false`): Whether PVP is enabled within claimed chunks by default.
- **DefaultPartyFriendlyFireEnabled** (Boolean, Default: `false`): Whether PVP between members of the same party is
  enabled by default.
- **DefaultPartyAllowEntry** (Boolean, Default: `true`): Whether non-members are allowed to enter claimed chunks by
  default.
- **DefaultPartyInteractChest** (Boolean, Default: `false`): Whether non-members can interact with chests in claimed
  chunks by default.
- **DefaultPartyInteractDoor** (Boolean, Default: `false`): Whether non-members can interact with doors in claimed
  chunks by default.
- **DefaultPartyInteractBench** (Boolean, Default: `false`): Whether non-members can interact with benches in claimed
  chunks by default.
- **DefaultPartyInteractChair** (Boolean, Default: `false`): Whether non-members can interact with chairs in claimed
  chunks by default.
- **DefaultPartyInteractPortal** (Boolean, Default: `false`): Whether non-members can interact with portals in claimed
  chunks by default.

## Permission Settings (Allow Changes)

These settings control whether party owners are allowed to change specific permissions in their own party settings.

- **AllowPartyPVPSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle PVP settings for their
  claims.
- **AllowPartyFriendlyFireSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle friendly fire
  settings for their party.
- **AllowPartyPlaceBlockSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle block placement
  permissions.
- **AllowPartyBreakBlockSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle block breaking
  permissions.
- **AllowPartyInteractBlockSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle general block
  interaction permissions.
- **AllowPartyAllowEntrySettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle entry
  permissions.
- **AllowPartyInteractChestSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle chest
  interaction permissions.
- **AllowPartyInteractDoorSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle door
  interaction permissions.
- **AllowPartyInteractBenchSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle bench
  interaction permissions.
- **AllowPartyInteractChairSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle chair
  interaction permissions.
- **AllowPartyInteractPortalSettingChanges** (Boolean, Default: `true`): If `true`, party owners can toggle portal
  interaction permissions.

## World & Protection Settings

Global settings that affect claiming and protection across the server.

- **WorldClaimBlacklist** (String Array, Default: `[]`): A list of world names where claiming chunks is disabled.
- **FullWorldProtection** (String Array, Default: `[]`): A list of world names where the entire world is protected as if
  it were claimed.
- **TitleTopClaimTitleText** (String, Default: `"Simple Claims"`): The text displayed at the top of the claim UI.
- **WildernessName** (String, Default: `"Wilderness"`): The text displayed when a player is in an unclaimed area.
- **CreativeModeBypassProtection** (Boolean, Default: `false`): If `true`, players in Creative Mode will bypass all
  claim protections.
- **BlocksThatIgnoreInteractRestrictions** (String Array, Default: `["gravestone"]`): A list of block IDs that can
  always be interacted with, even in claimed chunks where interactions are otherwise restricted.
- **PartyInactivityHours** (Integer, Default: `-1`): The number of hours a party can be inactive before it is
  automatically disbanded. Use `-1` to disable this feature. All members of the party must be inactive for this to
  trigger.

## Visual & Map Settings

Settings related to the user interface and visual feedback.

- **EnableParticleBorders** (Boolean, Default: `true`): If `true`, particles will be used to show the boundaries of
  claimed chunks.
- **ForceSimpleClaimsChunkWorldMap** (Boolean, Default: `true`): If `true`, forces the use of the Simple Claims chunk
  map.
- **RenderClaimNamesOnWorldMap** (Boolean, Default: `false`): If `true`, the name of the party that owns a chunk and a
  colored border will be rendered directly on the world map.
- **NotifyPartyChatToggling** (Boolean, Default: `true`): If `true`, all party members will be notified when someone
  toggles their party chat.

## Experimental Settings

Use these with caution as they might still be in development.

- **EXPERIMENTAL-EnableAlloyEntryTesting** (Boolean, Default: `false`): Enables experimental entry testing logic. This
  is a workaround for Hytale's interaction bugs.
- **EXPERIMENTAL-RenderMapInClaimUI** (Boolean, Default: `true`): If `true`, renders a map showing claimed chunks inside
  the claiming GUI.
