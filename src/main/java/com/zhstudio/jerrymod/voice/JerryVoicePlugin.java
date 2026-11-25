package com.zhstudio.jerrymod.voice;

import com.zhstudio.jerrymod.JerryForm;
import com.zhstudio.jerrymod.JerryMod;
import com.zhstudio.jerrymod.JerryState;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Only loaded when the Simple Voice Chat mod is installed.
 * - Players who pressed "Don't allow" are muted.
 * - Demonic Jerry gets a deep, growling voice. Skinwalker Jerry gets a warped, wrong-sounding voice.
 */
@ForgeVoicechatPlugin
public class JerryVoicePlugin implements VoicechatPlugin {
    private static final int SAMPLE_RATE = 48000;

    private final Map<UUID, Voice> voices = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return JerryMod.MODID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) {
            return;
        }
        UUID uuid = connection.getPlayer().getUuid();

        if (JerryState.VOICE_DENIED.contains(uuid)) {
            event.cancel();
            return;
        }

        UUID jerry = JerryState.serverJerry;
        JerryForm form = JerryState.serverForm;
        if (jerry == null || !jerry.equals(uuid) || (form != JerryForm.DEMON && form != JerryForm.SKINWALKER)) {
            return;
        }

        byte[] data = event.getPacket().getOpusEncodedData();
        Voice voice = voices.computeIfAbsent(uuid, id -> new Voice(event.getVoicechat()));
        if (data == null || data.length == 0) {
            voice.reset();
            return;
        }
        try {
            short[] pcm = voice.decoder.decode(data);
            short[] out = form == JerryForm.DEMON ? voice.demon(pcm) : voice.skinwalker(pcm);
            event.getPacket().setOpusEncodedData(voice.encoder.encode(out));
        } catch (Exception e) {
            voice.reset();
        }
    }

    private static final class Voice {
        final OpusDecoder decoder;
        final OpusEncoder encoder;
        double phase;

        Voice(VoicechatServerApi api) {
            decoder = api.createDecoder();
            encoder = api.createEncoder();
        }

        void reset() {
            decoder.resetState();
            encoder.resetState();
        }

        /** Pitch down + sub-octave + distortion = demon growl. */
        short[] demon(short[] in) {
            int n = in.length;
            short[] out = new short[n];
            for (int i = 0; i < n; i++) {
                double low = sample(in, i * 0.72);
                double sub = sample(in, i * 0.5);
                double v = (low * 0.8 + sub * 0.6) / 32768.0;
                v = Math.tanh(v * 2.2) * 0.85; // grit
                out[i] = clamp(v * 32767.0);
            }
            return out;
        }

        /** Slightly higher, ring-modulated and wobbling = something pretending to be a voice. */
        short[] skinwalker(short[] in) {
            int n = in.length;
            short[] out = new short[n];
            for (int i = 0; i < n; i++) {
                double pos = (i * 1.12) % n;
                double v = sample(in, pos) / 32768.0;
                phase += 2.0 * Math.PI * 38.0 / SAMPLE_RATE;
                double ring = Math.sin(phase) * 0.55 + 0.45;
                double wobble = 0.75 + 0.25 * Math.sin(phase * 0.09);
                out[i] = clamp(v * ring * wobble * 32767.0 * 1.3);
            }
            if (phase > 1000.0) {
                phase %= 2.0 * Math.PI;
            }
            return out;
        }

        private static double sample(short[] in, double pos) {
            int a = (int) pos;
            if (a >= in.length - 1) {
                return in[in.length - 1];
            }
            double t = pos - a;
            return in[a] * (1.0 - t) + in[a + 1] * t;
        }

        private static short clamp(double v) {
            return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v));
        }
    }
}
