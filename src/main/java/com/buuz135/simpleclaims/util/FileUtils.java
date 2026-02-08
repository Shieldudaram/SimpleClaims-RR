package com.buuz135.simpleclaims.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class FileUtils {

    public static String MAIN_PATH = Constants.UNIVERSE_PATH.resolve("SimpleClaims").toAbsolutePath().toString();
    public static String PARTY_PATH = MAIN_PATH + File.separator + "Parties.json";
    public static String CLAIM_PATH = MAIN_PATH + File.separator + "Claims.json";
    public static String NAMES_CACHE_PATH = MAIN_PATH + File.separator + "NameCache.json";
    public static String ADMIN_OVERRIDES_PATH = MAIN_PATH + File.separator + "AdminOverrides.json";
    public static String DATABASE_PATH = MAIN_PATH + File.separator + "SimpleClaims.db";
    private static final HytaleLogger LOGGER = HytaleLogger.getLogger().getSubLogger("SimpleClaims");

    public static void ensureDirectory(String path){
        var file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void ensureMainDirectory(){
        ensureDirectory(MAIN_PATH);
    }

    public static File ensureFile(String path, String defaultContent){
        return ensureFile(path, defaultContent, LOGGER);
    }

    public static File ensureFile(String path, String defaultContent, HytaleLogger logger) {
        var file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
                var writer = new FileWriter(file);
                writer.write(defaultContent);
                writer.close();
            } catch (Exception e) {
                logger.at(Level.SEVERE).withCause(e).log("Failed to create default file: " + path);
            }
        }
        return file;
    }

    public static void backupFile(String path) {
        backupFile(path, LOGGER);
    }

    public static void backupFile(String path, HytaleLogger logger) {
        var file = new File(path);
        if (file.exists()) {
            try {
                Files.copy(file.toPath(), Paths.get(path + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.at(Level.SEVERE).withCause(e).log("Failed to back up file: " + path);
            }
        }
    }

    public static boolean restoreFromBackup(String path) {
        return restoreFromBackup(path, LOGGER);
    }

    public static boolean restoreFromBackup(String path, HytaleLogger logger) {
        var backupFile = new File(path + ".bak");
        if (backupFile.exists()) {
            try {
                Files.copy(backupFile.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                logger.at(Level.SEVERE).withCause(e).log("Failed to restore backup file: " + path);
            }
        }
        return false;
    }

    public static void loadWithBackup(Runnable loadRunnable, String path, HytaleLogger logger) {
        try {
            loadRunnable.run();
        } catch (Exception e) {
            logger.at(Level.SEVERE).withCause(e).log("LOADING FILE ERROR: " + path + ", trying backup...");
            if (restoreFromBackup(path, logger)) {
                try {
                    loadRunnable.run();
                } catch (Exception ex) {
                    logger.at(Level.SEVERE).withCause(ex).log("LOADING BACKUP FILE ERROR: " + path);
                    throw ex;
                }
            } else {
                logger.at(Level.SEVERE).withCause(e).log("NO BACKUP FOUND FOR: " + path);
            }
        }
    }

}
