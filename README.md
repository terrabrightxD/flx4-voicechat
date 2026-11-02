# FLX4 Voice Chat Bridge

A client-side mod for **Minecraft Java 26.3 / Fabric / Windows**. Mix with your DDJ-FLX4 in Rekordbox and send the master mix through a virtual audio cable into **Simple Voice Chat**. Music comes from your player character, and you can still use your microphone.

**Status: alpha.** Compilation and automated audio checks are covered by CI. In-game UI testing and end-to-end testing with Windows, Rekordbox, an FLX4 and another player are still pending.

## Project roadmap

See [ROADMAP.md](ROADMAP.md) for planned releases, priorities, acceptance criteria and longer-term ideas. The next priority is validating the existing Windows/FLX4 workflow before adding features.

## Downloads and requirements

- [First release: v0.2.0-alpha.1](https://github.com/terrabrightxD/flx4-voicechat/releases/tag/v0.2.0-alpha.1) — includes the original German-language menu.
- [Latest development build](https://github.com/terrabrightxD/flx4-voicechat/actions/workflows/build.yml) — open the latest successful run and download **flx4-voicechat-26.3** under Artifacts. GitHub sign-in is required. Version **0.2.0-alpha.2** adds English as the default UI language and a German translation.

Extract the ZIP if necessary and put the JAR in your Minecraft instance's `mods` folder. Remove older versions of this mod first.

Required:

| Dependency | Version |
|---|---|
| Minecraft Java Edition | 26.3 |
| Java | 25 |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | 0.160.5+26.3 or newer **for Minecraft 26.3** |
| Simple Voice Chat | 2.6.27 or newer **for Minecraft 26.3** |

The server must have a working Simple Voice Chat installation. Only the DJ needs this add-on; listeners need Simple Voice Chat.

## Windows and Rekordbox setup

**Audio path:** FLX4 controls Rekordbox → Rekordbox master → CABLE Input → CABLE Output → this mod → Simple Voice Chat.

1. Install [VB-CABLE from its developer](https://vb-audio.com/Cable/) and restart Windows.
2. In the classic Windows sound settings, set the virtual cable's playback and recording formats to **48,000 Hz, 16-bit, stereo**. Disable exclusive access if another application prevents capture.
3. Keep **DDJ-FLX4 WASAPI** as Rekordbox's audio device. Keep the headphones output on **DDJ-FLX4 PHONES** so headphone cue stays private.
4. Enable **PC MASTER OUT** and route the additional master output to **CABLE Input (VB-Audio Virtual Cable)**. The exact selection method depends on your Rekordbox version. Check that Master Output lists FLX4 MASTER **plus CABLE Input**. If PC MASTER OUT follows the Windows default output, select CABLE Input as the default playback device before starting Rekordbox.
5. If you changed the Windows default output, explicitly route Minecraft, Simple Voice Chat playback, your browser and Discord to real headphones or speakers. Otherwise their audio may feed back into the cable. Disable Windows system sounds during a set. The cable should carry only the intended mix.
6. In **Simple Voice Chat**, select your real microphone, **not CABLE Output**. Explicitly choose your headphones as the playback device. Simple Voice Chat needs a working microphone input for its audio processing cycle.
7. Join a server and press **F8** to open the **FLX4 · DJ Mixer** menu. Change the shortcut under **Options → Controls → Key Binds → FLX4 Voice Chat** if needed.
8. Click the top button to cycle through audio inputs and select **CABLE Output**. Play a track in Rekordbox and click **Start music**. The level meter should respond.
9. Nearby players should hear the mix. Leave any active Voice Chat group for positional playback; while in a group, its normal transmission rules apply.

The manufacturer documents PC MASTER OUT. Routing it through VB-CABLE is the proposed combination here and still needs testing with your Rekordbox version. If Rekordbox does not route to the cable, first check the Windows recording meter for CABLE Output: the mod cannot transmit a signal that never reaches that input.

## Controls

- **Start music / Stop music:** explicitly start or stop transmission. There is no automatic start after installation or reconnection.
- **Music volume, 0–200%:** starts at 70%. Lower the Rekordbox and/or mod volume if the signal clips. Sample saturation prevents numeric overflow; it is not a studio limiter.
- **Music while mic is muted:** off by default. Enable it to keep playing music while your microphone is muted.
- **Push-to-talk controls only your microphone.** After Start, music continues without holding the speak key. Use Stop music to end it.
- **Escape / Back to game:** closes the menu while music continues. Press F8 to reopen it. The menu does not pause the game.
- **Disconnecting or disabling Voice Chat:** stops capture. Press Start again after reconnecting.
- Device selection and gain survive closing and reopening the menu within a session. Settings are not saved across game restarts.

The menu uses Minecraft's buttons, slider, tooltips, font and keyboard focus. Widget textures respond to resource packs. There is no separate desktop window or Java subprocess.

## Language support

The menu follows Minecraft's selected language. English (`en_us`) is the fallback; German (`de_de`) is also included. Audio-device names come from Windows, and technical capture errors are reported in English.

To add a translation, copy `src/main/resources/assets/flx4_voicechat/lang/en_us.json` to the appropriate locale filename and translate its values. Keep the same keys and `%s` placeholders; `%%` represents a literal percent sign.

## Scope and limitations

The mod captures the finished audio mix. Jog wheels, faders, effects and headphone cue continue to work through Rekordbox. It does not read the Rekordbox database, load tracks or take over MIDI controls.

The normal voice channel carries **mono audio**: stereo input is averaged down to mono. Stereo transmission and a placeable speaker block are not included. Total latency also depends on Windows, the virtual cable, Voice Chat and the network. Monitor directly through the FLX4 for beatmatching rather than listening to the delayed Minecraft signal.

## Troubleshooting

| Problem | What to check |
|---|---|
| Menu does not open | Press F8 while in the game. Rebind it if another mod uses the same key. Make sure Fabric API is installed. |
| No devices appear | Install VB-CABLE, restart Windows and click Refresh audio devices. Windows must list CABLE Output as a recording device. |
| Capture error | Set the device to 48 kHz/16-bit and stop other applications using it exclusively. Do not select an ASIO device directly. |
| Meter moves but nobody hears music | Check the Voice Chat connection, microphone mute state, music-while-muted option, server speaking permissions, group membership and distance. |
| Echo or duplicated music | Do not also select CABLE Output as the Voice Chat microphone. Never route game or Voice Chat playback to CABLE Input. |
| Choppy audio | Check audio formats, reduce CPU load and increase the Rekordbox buffer if needed. Windows device and multiplayer testing remain pending. |
| Distortion while speaking over music | Reduce both levels: microphone audio and music share the voice channel. |

## Development

Install JDK 25 and Gradle 9.8.2, then run:

```sh
gradle clean build
```

Output: `build/libs/flx4-voicechat-0.2.0-alpha.2.jar`.

Fabric Loom 1.17.21 compiles the native menu against Minecraft 26.3. Audio integration uses the public Simple Voice Chat API and Java Sound.

- Capture: 48 kHz, signed PCM16 little endian; stereo with mono fallback.
- Frames: 960 samples / 20 ms. The FIFO holds at most three frames and discards frames older than 100 ms.
- No audio recording to files, additional network ports or separate server service.
- `gradle audioTest` checks PCM decoding, downmixing, saturation, metering and buffering.
- See [TESTING.md](TESTING.md) for the remaining manual acceptance checks.

## References

- [Simple Voice Chat API setup](https://modrepo.de/minecraft/voicechat/api/getting_started)
- [MergeClientSoundEvent source for 26.3](https://github.com/henkelmax/simple-voice-chat/blob/26.3/api/src/main/java/de/maxhenkel/voicechat/api/events/MergeClientSoundEvent.java)
- [AlphaTheta: FLX4 / Rekordbox / PC MASTER OUT on Windows](https://support.alphatheta.com/en-US/articles/26128189455769)
- [VB-CABLE](https://vb-audio.com/Cable/)

Independent community project, not an official AlphaTheta/Pioneer DJ or Mojang integration. Licensed under the MIT License.
