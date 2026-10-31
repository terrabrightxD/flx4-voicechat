package de.terrabright.flx4;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

/** Keeps AWT out of Minecraft's potentially headless JVM. No sockets or public port. */
final class DesktopBridge {
    private final FlxPlugin plugin;
    private Process process;
    private ScheduledExecutorService statusWorker;
    private DataOutputStream output;
    private List<Capture.Device> devices = List.of();

    DesktopBridge(FlxPlugin plugin) { this.plugin = plugin; }
    synchronized void show() {
        if (process != null && process.isAlive()) return;
        close();
        try {
            String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
            String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();
            String classpath = Path.of(ControlPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            process = new ProcessBuilder(java, "-Djava.awt.headless=false", "-cp", classpath,
                    "de.terrabright.flx4.ControlPanel").redirectError(ProcessBuilder.Redirect.INHERIT).start();
            Process child = process;
            output = new DataOutputStream(child.getOutputStream());
            sendDevices();
            Thread commands = new Thread(() -> commands(child), "flx4-ui-commands");
            commands.setDaemon(true);
            commands.start();
            statusWorker = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "flx4-ui-status");
                thread.setDaemon(true);
                return thread;
            });
            statusWorker.scheduleAtFixedRate(() -> sendStatus(child), 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            close();
            System.err.println("[flx4] Could not open desktop controls: " + e);
        }
    }
    private synchronized void sendDevices() throws IOException {
        devices = Capture.devices();
        output.writeUTF("DEVICES");
        output.writeInt(devices.size());
        for (Capture.Device device : devices) output.writeUTF(device.toString());
        output.flush();
    }
    private synchronized void sendStatus(Process child) {
        if (process != child) return;
        try {
            output.writeUTF("STATUS");
            output.writeBoolean(plugin.connected);
            output.writeBoolean(plugin.capture.active());
            output.writeInt((int) Math.min(32768, plugin.capture.peak * plugin.gain));
            output.writeUTF(plugin.capture.status());
            output.writeUTF(plugin.transmission);
            output.flush();
        } catch (IOException e) { close(); }
    }
    private void commands(Process child) {
        try (DataInputStream input = new DataInputStream(child.getInputStream())) {
            while (true) handle(child, input.readUTF());
        } catch (IOException e) {
            synchronized (this) { if (process == child) close(); }
        }
    }
    private synchronized void handle(Process child, String command) throws IOException {
        if (process != child) return;
        String[] parts = command.split(" ", 2);
        try {
            switch (parts[0]) {
                case "STOP" -> plugin.capture.stop();
                case "REFRESH" -> { if (!plugin.capture.active()) sendDevices(); }
                case "START" -> {
                    int index = Integer.parseInt(parts[1]);
                    if (plugin.connected && index >= 0 && index < devices.size()) plugin.capture.start(devices.get(index));
                }
                case "GAIN" -> plugin.gain = Math.max(0, Math.min(200, Integer.parseInt(parts[1]))) / 100.0;
                case "MUTED" -> plugin.allowWhileMicMuted = Boolean.parseBoolean(parts[1]);
                default -> { }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            // Invalid private-pipe command must not interrupt the audio thread.
        }
    }
    synchronized void close() {
        plugin.capture.stop();
        if (statusWorker != null) statusWorker.shutdownNow();
        statusWorker = null;
        if (process != null) process.destroy();
        process = null;
        output = null;
    }
}
