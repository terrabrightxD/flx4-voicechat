package de.terrabright.flx4;

import java.util.Arrays;

public final class AudioTest {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(Arrays.equals(Pcm.decode(new byte[]{0, -128, -1, 127}, 1), new short[]{-32768, 32767}), "signed little endian");
        check(Pcm.decode(new byte[]{-1, 127, -1, 127}, 2)[0] == 32767, "stereo sum must not overflow");
        check(Pcm.decode(new byte[]{0, -128, 0, -128}, 2)[0] == -32768, "negative stereo must not overflow");
        check(Pcm.decode(new byte[]{16, 39, -16, -40}, 2)[0] == 0, "stereo downmix");
        check(Arrays.equals(Pcm.gain(new short[]{30000, -30000}, 2), new short[]{32767, -32768}), "gain saturation");
        check(Pcm.gain(new short[]{123}, 0)[0] == 0, "zero gain");
        check(Pcm.peak(new short[]{-32768}) == 32768, "peak handles minimum short");
        boolean rejected = false;
        try { Pcm.gain(new short[1], Double.NaN); } catch (IllegalArgumentException e) { rejected = true; }
        check(rejected, "reject NaN gain");
        FrameBuffer queue = new FrameBuffer();
        for (short i = 0; i < 5; i++) queue.offer(new short[]{i}, 0);
        check(queue.poll(0)[0] == 2, "overflow drops oldest frames");
        check(queue.poll(0)[0] == 3, "preserves frame order");
        check(queue.poll(100_000_001L) == null, "drops stale audio");
        queue.offer(new short[]{1}, 0); queue.clear();
        check(queue.poll(0) == null, "stop clears queued music");
        check(Pcm.decode(new byte[3840], 2).length == 960, "20 ms frame size");
        System.out.println(checks + " audio checks passed");
    }
}
