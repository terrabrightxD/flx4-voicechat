package de.terrabright.flx4;

import java.io.*;

/** Desktop-side model. Communication is only through private subprocess pipes. */
final class DesktopState {
    volatile String[] devices = new String[0];
    volatile boolean devicesChanged;
    volatile boolean connected;
    volatile boolean active;
    volatile int peak;
    volatile String status = "Verbinde mit Minecraft …";
    volatile String transmission = " ";
    private final DataOutputStream output = new DataOutputStream(System.out);
    synchronized void command(String command) {
        try { output.writeUTF(command); output.flush(); }
        catch (IOException e) { System.exit(0); }
    }
    void read() {
        try (DataInputStream input = new DataInputStream(System.in)) {
            while (true) {
                String type = input.readUTF();
                if (type.equals("DEVICES")) {
                    int count = input.readInt();
                    if (count < 0 || count > 1024) throw new IOException("Invalid device count");
                    String[] next = new String[count];
                    for (int i = 0; i < count; i++) next[i] = input.readUTF();
                    devices = next;
                    devicesChanged = true;
                } else if (type.equals("STATUS")) {
                    connected = input.readBoolean();
                    active = input.readBoolean();
                    peak = input.readInt();
                    status = input.readUTF();
                    transmission = input.readUTF();
                } else throw new IOException("Unknown message");
            }
        } catch (IOException e) {
            // Parent process exited or closed its private pipe. Do not leave a ghost UI.
            System.exit(0);
        }
    }
}
