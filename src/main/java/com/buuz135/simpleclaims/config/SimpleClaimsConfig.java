package com.buuz135.simpleclaims.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class SimpleClaimsConfig {

    public static final BuilderCodec<SimpleClaimsConfig> CODEC = BuilderCodec.builder(SimpleClaimsConfig.class, SimpleClaimsConfig::new)
            .append(new KeyedCodec<Integer>("DefaultPartyClaimsAmount", Codec.INTEGER),
                    (simpleClaimsConfig, aDouble, extraInfo) -> simpleClaimsConfig.DefaultPartyClaimsAmount = aDouble,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.DefaultPartyClaimsAmount).add()
            .append(new KeyedCodec<Boolean>("DefaultPartyBlockPlaceEnabled", Codec.BOOLEAN),
                    (simpleClaimsConfig, aDouble, extraInfo) -> simpleClaimsConfig.DefaultPartyBlockPlaceEnabled = aDouble,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.DefaultPartyBlockPlaceEnabled).add()
            .append(new KeyedCodec<Boolean>("DefaultPartyBlockBreakEnabled", Codec.BOOLEAN),
                    (simpleClaimsConfig, aDouble, extraInfo) -> simpleClaimsConfig.DefaultPartyBlockBreakEnabled = aDouble,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.DefaultPartyBlockBreakEnabled).add()
            .append(new KeyedCodec<Boolean>("DefaultPartyBlockInteractEnabled", Codec.BOOLEAN),
                    (simpleClaimsConfig, aDouble, extraInfo) -> simpleClaimsConfig.DefaultPartyBlockInteractEnabled = aDouble,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.DefaultPartyBlockInteractEnabled).add()
            .append(new KeyedCodec<Boolean>("ForceSimpleClaimsChunkWorldMap", Codec.BOOLEAN),
                    (simpleClaimsConfig, aDouble, extraInfo) -> simpleClaimsConfig.ForceSimpleClaimsChunkWorldMap = aDouble,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.ForceSimpleClaimsChunkWorldMap).add()
            .append(new KeyedCodec<Boolean>("CreativeModeBypassProtection", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.CreativeModeBypassProtection = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.CreativeModeBypassProtection).add()
            .append(new KeyedCodec<Boolean>("DefaultPartyPVPEnabled", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.DefaultPartyPVPEnabled = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.DefaultPartyPVPEnabled).add()
            .append(new KeyedCodec<Boolean>("DefaultPartyAllowEntry", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.DefaultPartyAllowEntry = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.DefaultPartyAllowEntry).add()
            .append(new KeyedCodec<Boolean>("AllowPartyPVPSettingChanges", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.AllowPartyPVPSetting = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.AllowPartyPVPSetting).add()
            .append(new KeyedCodec<Boolean>("AllowPartyPlaceBlockSettingChanges", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.AllowPartyPlaceBlockSetting = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.AllowPartyPlaceBlockSetting).add()
            .append(new KeyedCodec<Boolean>("AllowPartyBreakBlockSettingChanges", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.AllowPartyBreakBlockSetting = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.AllowPartyBreakBlockSetting).add()
            .append(new KeyedCodec<Boolean>("AllowPartyInteractBlockSettingChanges", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.AllowPartyInteractBlockSetting = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.AllowPartyInteractBlockSetting).add()
            .append(new KeyedCodec<Boolean>("AllowPartyAllowEntrySetting", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.AllowPartyAllowEntrySetting = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.AllowPartyAllowEntrySetting).add()
            .append(new KeyedCodec<String[]>("WorldClaimBlacklist", Codec.STRING_ARRAY),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.WorldNameBlacklistForClaiming = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.WorldNameBlacklistForClaiming).add()
            .append(new KeyedCodec<String>("TitleTopClaimTitleText", Codec.STRING),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.TitleTopClaimTitleText = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.TitleTopClaimTitleText).add()
            .append(new KeyedCodec<String[]>("FullWorldProtection", Codec.STRING_ARRAY),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.FullWorldProtection = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.FullWorldProtection).add()
            .append(new KeyedCodec<Boolean>("EXPERIMENTAL-EnableAlloyEntryTesting", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.EnableAlloyEntryTesting = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.EnableAlloyEntryTesting).add()
            .append(new KeyedCodec<Boolean>("EnableParticleBorders", Codec.BOOLEAN),
                    (simpleClaimsConfig, value, extraInfo) -> simpleClaimsConfig.EnableParticleBorders = value,
                    (simpleClaimsConfig, extraInfo) -> simpleClaimsConfig.EnableParticleBorders).add()
            .build();

    private int DefaultPartyClaimsAmount = 25;
    private boolean DefaultPartyBlockPlaceEnabled = false;
    private boolean DefaultPartyBlockBreakEnabled = false;
    private boolean DefaultPartyBlockInteractEnabled = false;
    private boolean DefaultPartyPVPEnabled = false;
    private boolean DefaultPartyAllowEntry = true;
    private boolean AllowPartyPVPSetting = true;
    private boolean AllowPartyPlaceBlockSetting = true;
    private boolean AllowPartyBreakBlockSetting = true;
    private boolean AllowPartyInteractBlockSetting = true;
    private boolean AllowPartyAllowEntrySetting = true;
    private String[] WorldNameBlacklistForClaiming = new String[0];
    private String TitleTopClaimTitleText = "Simple Claims";
    private String[] FullWorldProtection = new String[0];
    private boolean EnableAlloyEntryTesting = false;
    private boolean EnableParticleBorders = true;

    private boolean ForceSimpleClaimsChunkWorldMap = true;
    private boolean CreativeModeBypassProtection = false;

    public SimpleClaimsConfig() {

    }

    public int getDefaultPartyClaimsAmount() {
        return DefaultPartyClaimsAmount;
    }

    public boolean isDefaultPartyBlockPlaceEnabled() {
        return DefaultPartyBlockPlaceEnabled;
    }

    public boolean isDefaultPartyBlockBreakEnabled() {
        return DefaultPartyBlockBreakEnabled;
    }

    public boolean isDefaultPartyBlockInteractEnabled() {
        return DefaultPartyBlockInteractEnabled;
    }

    public boolean isForceSimpleClaimsChunkWorldMap() {
        return ForceSimpleClaimsChunkWorldMap;
    }

    public boolean isCreativeModeBypassProtection() {
        return CreativeModeBypassProtection;
    }

    public boolean isDefaultPartyPVPEnabled() {
        return DefaultPartyPVPEnabled;
    }

    public boolean isAllowPartyPVPSetting() {
        return AllowPartyPVPSetting;
    }

    public String[] getWorldNameBlacklistForClaiming() {
        return WorldNameBlacklistForClaiming;
    }

    public boolean isAllowPartyPlaceBlockSetting() {
        return AllowPartyPlaceBlockSetting;
    }

    public boolean isAllowPartyBreakBlockSetting() {
        return AllowPartyBreakBlockSetting;
    }

    public boolean isAllowPartyInteractBlockSetting() {
        return AllowPartyInteractBlockSetting;
    }

    public String getTitleTopClaimTitleText() {
        return TitleTopClaimTitleText;
    }

    public String[] getFullWorldProtection() {
        return FullWorldProtection;
    }

    public boolean isDefaultPartyAllowEntry() {
        return DefaultPartyAllowEntry;
    }

    public boolean isAllowPartyAllowEntrySetting() {
        return AllowPartyAllowEntrySetting;
    }

    public boolean isEnableAlloyEntryTesting() {
        return EnableAlloyEntryTesting;
    }

    public boolean isEnableParticleBorders() {
        return EnableParticleBorders;
    }
}
