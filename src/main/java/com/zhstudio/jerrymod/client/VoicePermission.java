package com.zhstudio.jerrymod.client;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Remembers the player's voice chat answer in config/jerrymod-voice.txt. */
public final class VoicePermission {
    /** null = never asked, true = allowed, false = denied */
    private static Boolean answer;
    private static boolean loaded;

    private VoicePermission() {
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("jerrymod-voice.txt");
    }

    public static Boolean get() {
        if (!loaded) {
            loaded = true;
            try {
                Path f = file();
                if (Files.exists(f)) {
                    String s = Files.readString(f, StandardCharsets.UTF_8).trim();
                    if (s.equalsIgnoreCase("allow")) {
                        answer = true;
                    } else if (s.equalsIgnoreCase("deny")) {
                        answer = false;
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return answer;
    }

    public static void set(boolean allowed) {
        answer = allowed;
        loaded = true;
        try {
            Files.writeString(file(), allowed ? "allow" : "deny", StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
