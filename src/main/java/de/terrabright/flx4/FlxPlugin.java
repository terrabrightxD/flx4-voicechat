package de.terrabright.flx4;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.*;

/** Only public Simple Voice Chat API; no Minecraft mixins or internal classes. */
public final class FlxPlugin implements VoicechatPlugin {
    final Capture capture = new Capture();
    volatile boolean connected;
    volatile boolean allowWhileMicMuted;
    volatile double gain = 0.7;
    volatile String transmission = "Nicht verbunden";
    static volatile FlxPlugin instance;

    public FlxPlugin() { instance = this; }

    @Override public String getPluginId() { return "flx4_voicechat"; }
    @Override public void registerEvents(EventRegistration events) {
        events.registerEvent(ClientVoicechatConnectionEvent.class, event -> {
            connected = event.isConnected();
            if (!connected) {
                capture.stop();
                transmission = "Getrennt – zum Fortsetzen erneut starten";
            } else {
                transmission = "Verbunden – Musik gestoppt";
            }
        });
        events.registerEvent(MergeClientSoundEvent.class, event -> {
            if (!capture.active()) return;
            // Always consume the live frame, even when transmission is paused.
            short[] frame = capture.poll();
            if (!connected || event.getVoicechat().isDisconnected() || event.getVoicechat().isDisabled()) {
                transmission = "Voice Chat nicht verfügbar";
                return;
            }
            if (event.getVoicechat().isMuted() && !allowWhileMicMuted) {
                transmission = "Pausiert: Mikrofon ist stumm";
                return;
            }
            if (frame != null) {
                event.mergeAudio(Pcm.gain(frame, gain));
                transmission = "Musik wird eingespeist";
            } else transmission = "Warte auf Audio …";
        });
        events.registerEvent(VoicechatDisableEvent.class, event -> {
            // Stop on both disable/enable transitions; never resume unexpectedly.
            capture.stop();
            transmission = "Voice-Chat-Status geändert – erneut starten";
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            capture.stop();
        }, "flx4-shutdown"));
    }
}
