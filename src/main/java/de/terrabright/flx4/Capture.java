package de.terrabright.flx4;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

final class Capture {
    record Device(Mixer.Info info) {
        @Override public String toString() { return info.getName() + " — " + info.getDescription(); }
    }
    private static final class Session {
        final FrameBuffer frames = new FrameBuffer();
        volatile TargetDataLine line;
    }
    private volatile Session current;
    private volatile String status = "Gestoppt";
    volatile int peak;

    static List<Device> devices() {
        List<Device> result = new ArrayList<>();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.getTargetLineInfo().length > 0) result.add(new Device(info));
        }
        return result;
    }
    boolean active() { return current != null; }
    String status() { return status; }
    synchronized void start(Device device) {
        if (current != null) return;
        Session session = new Session();
        current = session;
        status = "Audioeingang wird geöffnet …";
        Thread worker = new Thread(() -> read(session, device), "flx4-audio-capture");
        worker.setDaemon(true);
        worker.start();
    }
    synchronized void stop() {
        Session session = current;
        current = null;
        peak = 0;
        status = "Gestoppt";
        if (session != null) {
            session.frames.clear();
            TargetDataLine line = session.line;
            if (line != null) line.close(); // Unblocks a pending read.
        }
    }
    short[] poll() {
        Session session = current;
        return session == null ? null : session.frames.poll(System.nanoTime());
    }
    private void read(Session session, Device device) {
        TargetDataLine line = null;
        try {
            Mixer mixer = AudioSystem.getMixer(device.info());
            int channels = 0;
            Exception last = null;
            for (int candidate : new int[]{2, 1}) {
                AudioFormat format = new AudioFormat(48000, 16, candidate, true, false);
                DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);
                if (!mixer.isLineSupported(lineInfo)) continue;
                TargetDataLine attempt = null;
                try {
                    attempt = (TargetDataLine) mixer.getLine(lineInfo);
                    attempt.open(format, Pcm.SAMPLES * candidate * 2 * 4);
                    line = attempt;
                    channels = candidate;
                    break;
                } catch (Exception e) {
                    last = e;
                    if (attempt != null) attempt.close();
                }
            }
            if (line == null) throw new IllegalStateException("48 kHz / 16 Bit nicht verfügbar. Windows-Audioformat prüfen.", last);
            synchronized (this) {
                if (current != session) return;
                session.line = line;
                line.start();
                status = "Audioeingang aktiv (" + channels + " Kanal/Kanäle)";
            }
            byte[] bytes = new byte[Pcm.SAMPLES * channels * 2];
            while (current == session) {
                int offset = 0;
                while (offset < bytes.length && current == session) {
                    int read = line.read(bytes, offset, bytes.length - offset);
                    if (read <= 0) throw new IllegalStateException("Audioeingang wurde getrennt.");
                    offset += read;
                }
                if (current != session) break;
                short[] frame = Pcm.decode(bytes, channels);
                peak = Pcm.peak(frame);
                session.frames.offer(frame, System.nanoTime());
            }
        } catch (Exception e) {
            synchronized (this) {
                if (current == session) {
                    current = null;
                    peak = 0;
                    status = "Fehler: " + e.getMessage();
                }
            }
        } finally {
            if (line != null) line.close();
            session.frames.clear();
        }
    }
}
