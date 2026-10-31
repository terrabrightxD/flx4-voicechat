# Validation

## Automated

`gradle clean build` compiles against the real published Simple Voice Chat API 2.6.27, runs the standalone `AudioTest` (13 checks), and creates the Fabric JAR. GitHub Actions is the authoritative build result; do not treat an uncompleted or failed run as verified.

The tests cover signed little-endian PCM, stereo overflow, downmix, saturating gain, zero gain, negative full-scale metering, invalid gain rejection, queue overflow ordering, stale-frame expiry, queue clearing, and frame length.

`xvfb-run -a gradle desktopTest` starts the actual Swing subprocess with synthetic device/status messages, checks that it remains alive, and verifies clean exit when the parent pipe closes. This checks UI startup and process lifecycle on Linux, not Windows audio capture.

## Manual acceptance – pending

Requires Windows, Java 25, Minecraft 26.3/Fabric, Simple Voice Chat 2.6.27+, VB-CABLE, Rekordbox, FLX4 and a second connected player.

- [ ] Client loads the JAR without missing dependency/API errors.
- [ ] Joining a Voice Chat server opens the control window; nothing transmits before START.
- [ ] Select CABLE Output; input meter follows the Rekordbox master.
- [ ] Another player hears music from the DJ's position, with normal distance attenuation outside groups.
- [ ] Cue a silent-to-master deck: listeners must not hear headphone cue.
- [ ] PTT microphone can be added while the music continues.
- [ ] Mute pauses music with the checkbox off; the explicit checkbox allows music while the mic is muted.
- [ ] STOP and window close stop music immediately; no queued music reappears after restart.
- [ ] Disconnect and reconnect do not resume music without START.
- [ ] Disable/re-enable Voice Chat requires START again.
- [ ] Changing devices after STOP works; unplugging an input reports an error and permits recovery.
- [ ] Run a 30-minute set; listen for buffer drift, gaps, feedback and clipping.
- [ ] Minecraft exits normally with the control window open.

Hardware capture, Swing UI in the Minecraft JVM, audible latency and end-to-end multiplayer behavior cannot be proven by the unit tests.
