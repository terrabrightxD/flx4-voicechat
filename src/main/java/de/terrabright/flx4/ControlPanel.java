package de.terrabright.flx4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Separate Windows control window: accessible while Rekordbox has focus. */
public final class ControlPanel {
    private final JFrame window = new JFrame("FLX4 → Minecraft | Voice Chat Bridge");
    ControlPanel(DesktopState plugin) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        body.add(new JLabel("Rekordbox-Mix → virtuelles Audiokabel → Voice Chat"));
        body.add(Box.createVerticalStrut(14));
        body.add(new JLabel("Aufnahmegerät: CABLE Output auswählen (nicht CABLE Input)"));
        JComboBox<String> devices = new JComboBox<>();
        JButton refresh = new JButton("Geräte neu laden");
        Runnable load = () -> {
            devices.removeAllItems();
            for (String device : plugin.devices) devices.addItem(device);
            devices.setSelectedIndex(-1); // Never silently pick a microphone.
        };
        load.run();
        refresh.addActionListener(e -> plugin.command("REFRESH"));
        body.add(devices);
        body.add(refresh);
        body.add(Box.createVerticalStrut(12));
        JLabel volumeLabel = new JLabel("Musiklautstärke: 70 %");
        JSlider volume = new JSlider(0, 200, 70);
        volume.addChangeListener(e -> {
            plugin.command("GAIN " + volume.getValue());
            volumeLabel.setText("Musiklautstärke: " + volume.getValue() + " %");
        });
        body.add(volumeLabel);
        body.add(volume);
        JProgressBar meter = new JProgressBar(0, 32768);
        meter.setStringPainted(true);
        body.add(meter);
        JCheckBox muted = new JCheckBox("Musik auch bei stummem Mikrofon senden", false);
        muted.addActionListener(e -> plugin.command("MUTED " + muted.isSelected()));
        body.add(muted);
        body.add(new JLabel("Push-to-Talk steuert das Mikrofon. Musik läuft nach START dauerhaft."));
        body.add(new JLabel("Für räumlichen Klang: Voice-Chat-Gruppe verlassen."));
        JButton toggle = new JButton("START");
        toggle.addActionListener(e -> {
            if (plugin.active) {
                plugin.command("STOP");
                plugin.transmission = "Musik gestoppt";
            } else if (plugin.connected && devices.getSelectedIndex() >= 0) {
                plugin.command("START " + devices.getSelectedIndex());
            }
        });
        body.add(Box.createVerticalStrut(12));
        body.add(toggle);
        JLabel status = new JLabel("Gestoppt");
        JLabel send = new JLabel(" ");
        body.add(status);
        body.add(send);
        Timer timer = new Timer(100, e -> {
            if (plugin.devicesChanged) {
                plugin.devicesChanged = false;
                load.run();
            }
            boolean active = plugin.active;
            toggle.setText(active ? "STOPP – Musik beenden" : "START");
            toggle.setEnabled(active || (plugin.connected && devices.getSelectedItem() != null));
            devices.setEnabled(!active);
            refresh.setEnabled(!active);
            int peak = plugin.peak;
            meter.setValue(peak);
            meter.setString(peak >= 32767 ? "ÜBERSTEUERT – Lautstärke senken" : "Pegel: " + Math.round(peak * 100.0 / 32768) + " %");
            status.setText(plugin.status);
            send.setText(active ? plugin.transmission : (plugin.connected ? "Verbunden – Musik gestoppt" : "Nicht verbunden"));
        });
        timer.start();
        window.setContentPane(body);
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                plugin.command("STOP");
                window.setState(Frame.ICONIFIED);
            }
        });
        window.pack();
        window.setMinimumSize(new Dimension(660, 430));
        window.setSize(760, 460);
        window.setLocationByPlatform(true);
    }
    void show() { window.setVisible(true); }
    public static void main(String[] args) {
        DesktopState state = new DesktopState();
        Thread reader = new Thread(state::read, "flx4-ui-status");
        reader.setDaemon(true);
        reader.start();
        SwingUtilities.invokeLater(() -> new ControlPanel(state).show());
    }
}
