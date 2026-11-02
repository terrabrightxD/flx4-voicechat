package de.terrabright.flx4;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.*;

/** Only public Simple Voice Chat API; no Minecraft mixins or internal classes. */
public final class FlxPlugin implements VoicechatPlugin {
    final Capture capture = new Capture();
    volatile boolean connected;
    volatile boolean allowWhileMicMuted;
    volatile double gain = 0.7;
    volatile String transmission = "gui.flx4.disconnected";
    static volatile FlxPlugin instance;

    public FlxPlugin() { instance = this; }

    @Override public String getPluginId() { return "flx4_voicechat"; }
    @Override public void registerEvents(EventRegistration events) {
        events.registerEvent(ClientVoicechatConnectionEvent.class, event -> {
            connected = event.isConnected();
            if (!connected) {
                capture.stop();
                transmission = "gui.flx4.reconnect";
            } else {
                transmission = "gui.flx4.ready";
            }
        });
        events.registerEvent(MergeClientSoundEvent.class, event -> {
            if (!capture.active()) return;
            // Always consume the live frame, even when transmission is paused.
            short[] frame = capture.poll();
            if (!connected || event.getVoicechat().isDisconnected() || event.getVoicechat().isDisabled()) {
                transmission = "gui.flx4.unavailable";
                return;
            }
            if (event.getVoicechat().isMuted() && !allowWhileMicMuted) {
                transmission = "gui.flx4.mic_muted";
                return;
            }
            if (frame != null) {
                event.mergeAudio(Pcm.gain(frame, gain));
                transmission = "gui.flx4.sending";
            } else transmission = "gui.flx4.waiting";
        });
        events.registerEvent(VoicechatDisableEvent.class, event -> {
            // Stop on both disable/enable transitions; never resume unexpectedly.
            capture.stop();
            transmission = "gui.flx4.changed";
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            capture.stop();
        }, "flx4-shutdown"));
    }
}
