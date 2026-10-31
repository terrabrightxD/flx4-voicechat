package de.terrabright.flx4;

import java.util.ArrayDeque;

/** Bounded FIFO: an interrupted voice connection must never replay old music. */
final class FrameBuffer {
    private record Frame(short[] audio, long time) {}
    private final ArrayDeque<Frame> frames = new ArrayDeque<>();
    synchronized void offer(short[] audio, long now) {
        while (frames.size() >= 3) frames.removeFirst();
        frames.addLast(new Frame(audio, now));
    }
    synchronized short[] poll(long now) {
        while (!frames.isEmpty()) {
            Frame frame = frames.removeFirst();
            if (now - frame.time <= 100_000_000L) return frame.audio;
        }
        return null;
    }
    synchronized void clear() { frames.clear(); }
}
