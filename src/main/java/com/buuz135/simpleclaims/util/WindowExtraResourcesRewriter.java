package com.buuz135.simpleclaims.util;

import com.hypixel.hytale.protocol.ExtraResources;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WindowExtraResourcesRewriter extends ChannelDuplexHandler {

    public static final String HANDLER_NAME = "simpleclaims_window_extra_rewriter";

    // per-channel map: windowId -> ExtraResources
    public static final AttributeKey<Map<Integer, ExtraResources>> EXTRA_BY_WINDOW_ID = AttributeKey.valueOf("simpleclaims_extra_by_window_id");

    // Reflection cached for UpdateWindow fields
    private static volatile Field UPDATEWINDOW_ID_FIELD;
    private static volatile Field UPDATEWINDOW_EXTRA_FIELD;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // handle Packet[] writes (PacketHandler.write(Packet...) uses channel.write(Packet[]))
        if (msg != null && msg.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(msg);
            for (int i = 0; i < len; i++) {
                Object elem = java.lang.reflect.Array.get(msg, i);
                rewriteIfUpdateWindow(ctx, elem);
            }
        } else {
            rewriteIfUpdateWindow(ctx, msg);
        }
        super.write(ctx, msg, promise);
    }

    private static void rewriteIfUpdateWindow(ChannelHandlerContext ctx, Object msg) {
        if (msg == null) return;

        Class<?> cls = msg.getClass();
        if (!cls.getName().equals("com.hypixel.hytale.protocol.packets.window.UpdateWindow")) return;

        Map<Integer, ExtraResources> map = ctx.channel().attr(EXTRA_BY_WINDOW_ID).get();
        if (map == null || map.isEmpty()) return;

        try {
            Field idF = getIdField(cls);
            Field extraF = getExtraField(cls);

            int windowId = (int) idF.get(msg);
            ExtraResources forced = map.get(windowId);
            if (forced == null) return;

            extraF.set(msg, forced);

        } catch (Throwable ignored) {
            // If reflection fails, we just don't rewrite.
        }
    }

    private static Field getIdField(Class<?> updateWindowClass) throws NoSuchFieldException {
        Field f = UPDATEWINDOW_ID_FIELD;
        if (f != null) return f;

        f = updateWindowClass.getDeclaredField("id");
        f.setAccessible(true);
        UPDATEWINDOW_ID_FIELD = f;
        return f;
    }

    private static Field getExtraField(Class<?> updateWindowClass) throws NoSuchFieldException {
        Field f = UPDATEWINDOW_EXTRA_FIELD;
        if (f != null) return f;

        for (Field fld : updateWindowClass.getDeclaredFields()) {
            if (fld.getType().getName().equals("com.hypixel.hytale.protocol.ExtraResources")) {
                fld.setAccessible(true);
                UPDATEWINDOW_EXTRA_FIELD = fld;
                return fld;
            }
        }
        throw new NoSuchFieldException("No ExtraResources field found on UpdateWindow");
    }

    public static Map<Integer, ExtraResources> getOrCreateMap(io.netty.channel.Channel ch) {
        Map<Integer, ExtraResources> m = ch.attr(EXTRA_BY_WINDOW_ID).get();
        if (m == null) {
            m = new ConcurrentHashMap<>();
            ch.attr(EXTRA_BY_WINDOW_ID).set(m);
        }
        return m;
    }
}
