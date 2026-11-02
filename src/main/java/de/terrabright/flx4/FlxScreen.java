package de.terrabright.flx4;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.concurrent.CompletableFuture;

/** Native Minecraft screen: vanilla widgets, focus navigation and resource-pack styling. */
public final class FlxScreen extends Screen {
    private int left, top, panelWidth;
    private Button deviceButton, refreshButton, startButton, mutedButton;
    private boolean loading;
    private String deviceError;

    public FlxScreen() { super(Component.translatable("gui.flx4.title")); }
    private FlxPlugin plugin() { return FlxPlugin.instance; }
    private static Component text(String value) { return Component.literal(value); }

    private static String tr(String key, Object... args) {
        return Component.translatable("gui.flx4." + key, args).getString();
    }

    @Override protected void init() {
        panelWidth = Math.min(320, width - 20);
        left = (width - panelWidth) / 2;
        top = Math.max(0, (height - 234) / 2);
        deviceButton = addRenderableWidget(Button.builder(text(tr("select")), button -> {
            if (!FlxClient.devices.isEmpty())
                FlxClient.selected = (FlxClient.selected + 1) % FlxClient.devices.size();
            updateButtons();
        }).bounds(left, top + 32, panelWidth, 20).build());
        refreshButton = addRenderableWidget(Button.builder(text(tr("refresh")), button -> refreshDevices())
                .bounds(left, top + 56, panelWidth, 20).build());
        addRenderableWidget(new GainSlider(left, top + 80, panelWidth));
        mutedButton = addRenderableWidget(Button.builder(text(""), button -> {
            if (plugin() != null) plugin().allowWhileMicMuted = !plugin().allowWhileMicMuted;
            updateButtons();
        }).bounds(left, top + 104, panelWidth, 20).build());
        mutedButton.setTooltip(Tooltip.create(text(tr("muted_tip"))));
        startButton = addRenderableWidget(Button.builder(text(tr("start")), button -> {
            FlxPlugin p = plugin();
            if (p == null) return;
            if (p.capture.active()) p.capture.stop();
            else if (p.connected && FlxClient.selected >= 0 && FlxClient.selected < FlxClient.devices.size())
                p.capture.start(FlxClient.devices.get(FlxClient.selected));
            updateButtons();
        }).bounds(left, top + 128, panelWidth, 20).build());
        addRenderableWidget(Button.builder(text(tr("back")), button -> onClose())
                .bounds(left, top + 212, panelWidth, 20).build());
        updateButtons();
        if (FlxClient.devices.isEmpty() && !loading) refreshDevices();
    }

    private void refreshDevices() {
        if (loading || (plugin() != null && plugin().capture.active())) return;
        loading = true;
        deviceError = null;
        String previous = FlxClient.selected >= 0 && FlxClient.selected < FlxClient.devices.size()
                ? FlxClient.devices.get(FlxClient.selected).toString() : null;
        CompletableFuture.supplyAsync(Capture::devices).whenComplete((devices, failure) -> minecraft.execute(() -> {
            loading = false;
            if (failure != null) deviceError = tr("devices_error");
            else {
                FlxClient.devices = devices;
                FlxClient.selected = -1;
                for (int i = 0; i < devices.size(); i++)
                    if (devices.get(i).toString().equals(previous)) FlxClient.selected = i;
            }
            updateButtons();
        }));
        updateButtons();
    }

    private void updateButtons() {
        FlxPlugin p = plugin();
        boolean active = p != null && p.capture.active();
        boolean selected = FlxClient.selected >= 0 && FlxClient.selected < FlxClient.devices.size();
        String name = selected ? FlxClient.devices.get(FlxClient.selected).info().getName()
                : (loading ? tr("searching") : tr("select"));
        deviceButton.setMessage(text(font.plainSubstrByWidth(name, panelWidth - 24)));
        deviceButton.setTooltip(Tooltip.create(text(selected ? FlxClient.devices.get(FlxClient.selected).toString()
                : tr("device_tip"))));
        deviceButton.active = !active && !loading && !FlxClient.devices.isEmpty();
        refreshButton.active = !active && !loading;
        mutedButton.active = p != null;
        mutedButton.setMessage(text(tr("muted", tr(p != null && p.allowWhileMicMuted ? "on" : "off"))));
        startButton.setMessage(text(active ? tr("stop") : tr("start")));
        startButton.active = active || (p != null && p.connected && selected && !loading);
    }
    @Override public void tick() { super.tick(); updateButtons(); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.gui.setScreen(null); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        line(g, title.getString(), top + 5, 0xFFFFFFFF);
        line(g, "Rekordbox → Voice Chat", top + 19, 0xFFAAAAAA);
        FlxPlugin p = plugin();
        int peak = p == null ? 0 : (int) Math.min(32768, p.capture.peak * p.gain);
        g.fill(left, top + 155, left + panelWidth, top + 163, 0xFF151515);
        g.fill(left + 1, top + 156, left + 1 + (panelWidth - 2) * peak / 32768, top + 162,
                peak >= 32767 ? 0xFFFF5555 : peak > 26000 ? 0xFFFFFF55 : 0xFF55FF55);
        String status = p == null ? tr("loading") : !p.connected ? tr("disconnected")
                : p.capture.active() ? Component.translatable(p.transmission).getString() : tr("ready");
        line(g, status, top + 168, p != null && p.connected ? 0xFF55FF55 : 0xFFFFAA00);
        String detail = deviceError != null ? deviceError : p != null && p.capture.status().startsWith("Error:")
                ? p.capture.status() : peak >= 32767 ? tr("clipping")
                : FlxClient.devices.isEmpty() && !loading ? tr("no_devices") : tr("level", Math.round(peak * 100.0 / 32768));
        line(g, detail, top + 180, 0xFFAAAAAA);
        line(g, tr("close_hint"), top + 197, 0xFFAAAAAA);
    }
    private void line(GuiGraphicsExtractor g, String value, int y, int color) {
        String clipped = font.plainSubstrByWidth(value, panelWidth);
        g.text(font, clipped, (width - font.width(clipped)) / 2, y, color, true);
    }
    private final class GainSlider extends AbstractSliderButton {
        GainSlider(int x, int y, int width) {
            super(x, y, width, 20, Component.empty(), plugin() == null ? 0.35 : plugin().gain / 2);
            updateMessage();
        }
        @Override protected void updateMessage() { setMessage(text(tr("volume", Math.round(value * 200)))); }
        @Override protected void applyValue() { if (plugin() != null) plugin().gain = value * 2; }
    }
}
