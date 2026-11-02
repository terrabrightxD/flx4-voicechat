# Release Roadmap

This roadmap describes planned work, not features that already exist. Release numbers are proposed targets; scope may change after testing and feedback. There are no fixed release dates. Reliability of the Windows/Fabric audio bridge comes before additional integrations.

## Current baseline

- **Published:** `v0.2.0-alpha.1` — native Minecraft menu, F8 shortcut, audio-input selection, start/stop, volume and level meter. The release JAR uses German UI text.
- **Development build:** `0.2.0-alpha.2` — English default UI, German translation, English documentation and release description. This build is not yet a tagged release.
- **Verified:** compilation and 13 automated audio checks; translation keys and format placeholders checked for parity.
- **Still pending:** actual in-game UI testing and Windows/Rekordbox/FLX4 multiplayer listening tests. See [TESTING.md](TESTING.md).

## Planned releases

| Target | Priority | Main goal | Ready when |
|---|---|---|---|
| `0.2.0-alpha.3` | P0 | Validate the existing end-to-end workflow | Core manual acceptance checks pass with documented results |
| `0.3.0-alpha.1` | P1 | Make setup and everyday use easier | Settings survive restarts and device selection is clear and reliable |
| `0.4.0-alpha.1` | P1 | Improve capture recovery and diagnostics | Input failures are understandable and recoverable without restarting Minecraft |
| `1.0.0-beta.1` | P1 | Test longer sets and prepare distribution | Extended sessions pass and the feature set is frozen for stabilization |
| `1.0.0` | P0 release gate | Ship a dependable first stable version | Stable-release acceptance criteria below are met |

**P0:** required to establish confidence or ship stable. **P1:** next planned improvement after the previous release gate passes. Unfinished work moves to another prerelease rather than being hidden behind a stable label.

## 0.2.0-alpha.3 — Validate the foundation

- [ ] Test installation on Windows with Minecraft 26.3, Fabric, Fabric API and Simple Voice Chat; record exact versions.
- [ ] Verify the menu in-game at GUI scales 2, 3 and 4, including resizing, keyboard navigation, F8 rebinding and English/German labels.
- [ ] Run the complete audio route with Rekordbox and a real DDJ-FLX4. Confirm that headphone cue stays private.
- [ ] Check positional music playback with a second player, microphone mixing, push-to-talk, mute behavior and Voice Chat groups.
- [ ] Test Stop, disconnect/reconnect, disabling Voice Chat and unplugging the capture device. Confirm that music never resumes without an explicit Start action after a disconnect.
- [ ] Complete a 30-minute set and record audible gaps, clipping, latency observations and any drift.
- [ ] Fix blocking defects and publish a tagged prerelease containing the English UI.

**Release gate:** record the environment, results and known issues in TESTING.md. Installation failures, unintended transmission, leaked cue audio and broken start/stop behavior block this milestone.

## 0.3.0-alpha.1 — Easier setup

- [ ] Save the selected input, gain and music-while-muted preference in a versioned config file.
- [ ] Restore preferences without automatically starting transmission. A missing device must never silently switch to the default microphone.
- [ ] Replace the cycling device button with a scrollable selection screen that shows full device names.
- [ ] Add setup hints for CABLE Input versus CABLE Output, 48 kHz/16-bit capture and separate headphone cue.
- [ ] Show a clear warning when the selected input disappears; keep Start disabled until a valid input is selected.
- [ ] Add an optional in-game indicator showing whether music is transmitting or paused, plus a rebindable Stop shortcut.

**Release gate:** valid preferences survive a restart; missing or invalid configuration has a safe fallback. Closing the menu still leaves an explicitly started mix running, and Stop remains easy to reach.

## 0.4.0-alpha.1 — Capture recovery and diagnostics

- [ ] Replace error-string matching with explicit capture states and localized, actionable error messages.
- [ ] Handle device removal, unavailable formats and exclusive-access failures without freezing the render or audio thread.
- [ ] Allow a device refresh and explicit restart after reconnecting the cable or device.
- [ ] Show input level and transmission state separately so users can tell “signal received” from “audio submitted to Voice Chat.” Do not present either as proof that another player heard it.
- [ ] Add bounded diagnostic counters for buffer underruns, dropped frames and capture failures.
- [ ] Provide a user-triggered diagnostic export containing relevant versions and settings, without recording audio. Let users review device names before sharing.
- [ ] Add tests for capture lifecycle transitions and recovery where those can be simulated meaningfully.

**Release gate:** recovery works without restarting Minecraft, old buffered music is not replayed, and no restart or recovery path enables transmission automatically.

## 1.0.0-beta.1 — Longer sets and release preparation

- [ ] Test at least a 90-minute DJ set on Windows with two players and document the setup and observations.
- [ ] Check behavior under increased game load and network disruption; investigate sustained latency growth, dropouts and resource leaks.
- [ ] Measure capture/buffering overhead on the test machine before choosing performance targets or tuning buffer settings.
- [ ] Retest microphone-plus-music headroom and document practical gain settings. Evaluate a limiter only if listening tests demonstrate a need.
- [ ] Create a tested compatibility matrix for supported Minecraft, Fabric API and Simple Voice Chat versions.
- [ ] Replace the one-off first-release workflow with a reusable release process that attaches the exact tested JAR and its checksum to a versioned tag.
- [ ] Prepare a bug-report template, changelog, upgrade instructions and screenshots of the actual in-game menu.

**Release gate:** no known blocking defects in the core Windows/Fabric workflow. Freeze new features during beta; use additional betas or release candidates for fixes.

## 1.0.0 — Stable-release acceptance criteria

- [ ] All applicable checks in TESTING.md have recorded results for the advertised supported setup.
- [ ] Installation and upgrade instructions work with the published JAR, not only a development environment.
- [ ] The native menu, saved settings, device recovery and explicit start/stop behavior pass regression testing.
- [ ] At least one extended session passes without unresolved sustained drift, recurring unexplained dropouts or resource leaks attributable to the mod.
- [ ] English text is complete; the German translation has matching keys and usable layout.
- [ ] Remaining limitations are documented clearly, including mono transmission and the need for external audio routing.
- [ ] The release identifies the tested commit and dependency versions and includes a changelog and downloadable JAR.

Stable means the documented supported workflow has been tested. It does not promise zero latency, every audio device, or compatibility with untested Minecraft versions.

## Later ideas — investigation only

These ideas are outside the committed release sequence. Each needs a feasibility check before implementation.

| Idea | What needs to be established first |
|---|---|
| Placeable DJ booth or speaker block | Server-side routing, permissions, range limits and which players must install additional components |
| Stereo or a dedicated music channel | Whether the supported Voice Chat APIs can provide it and what client/server protocol changes would be required |
| Optional FLX4 MIDI shortcuts | Whether MIDI access can coexist with Rekordbox without stealing controller input; retain keyboard controls as a fallback |
| Track title, BPM or deck display | Availability of an official, supported Rekordbox data interface; audio capture alone does not provide metadata |
| Direct Windows audio capture without a virtual cable | Feasibility of process-specific loopback capture, native dependencies, driver behavior and maintenance cost |
| macOS/Linux support | Real hardware tests, platform-specific routing instructions and capture-device compatibility |
| Additional Minecraft versions or mod loaders | Demand, compatible dependencies and a maintainable build/test matrix |

## Feedback and scope changes

Report bugs and feature requests in [GitHub Issues](https://github.com/terrabrightxD/flx4-voicechat/issues). Include Minecraft/mod versions, Windows version, audio routing, steps to reproduce and relevant logs. For audio problems, distinguish what the local meter shows from what another player hears.

Update this roadmap when a milestone is completed or its scope changes. Checkboxes are only marked complete after implementation and the corresponding validation; a successful compile alone does not complete an in-game or hardware test.
