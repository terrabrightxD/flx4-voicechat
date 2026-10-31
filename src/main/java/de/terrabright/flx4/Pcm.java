package de.terrabright.flx4;

final class Pcm {
    static final int SAMPLES = 960; // 20 ms at 48 kHz
    private Pcm() {}
    static short[] decode(byte[] bytes, int channels) {
        if ((channels != 1 && channels != 2) || bytes.length % (2 * channels) != 0)
            throw new IllegalArgumentException("Invalid PCM frame");
        short[] out = new short[bytes.length / (2 * channels)];
        for (int i = 0; i < out.length; i++) {
            int sum = 0;
            for (int c = 0; c < channels; c++) {
                int p = (i * channels + c) * 2;
                sum += (short) ((bytes[p] & 255) | (bytes[p + 1] << 8));
            }
            out[i] = (short) (sum / channels);
        }
        return out;
    }
    static short[] gain(short[] input, double gain) {
        if (!Double.isFinite(gain) || gain < 0 || gain > 2)
            throw new IllegalArgumentException("Gain must be 0..2");
        short[] out = new short[input.length];
        for (int i = 0; i < out.length; i++)
            out[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(input[i] * gain)));
        return out;
    }
    static int peak(short[] frame) {
        int peak = 0;
        for (short sample : frame) peak = Math.max(peak, Math.abs((int) sample));
        return peak;
    }
}
