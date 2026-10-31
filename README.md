# FLX4 Voice Chat Bridge

Client-Mod für **Minecraft Java 26.3 / Fabric / Windows**. Mixe mit deinem DDJ-FLX4 in Rekordbox und sende den Master-Mix über ein virtuelles Audiokabel in **Simple Voice Chat**. Die Musik kommt von deiner Spielfigur; das normale Mikrofon bleibt zusätzlich nutzbar.

**Status: Alpha – Hardware- und Multiplayer-Hörtest noch offen.** Der Build-Workflow prüft Kompilierung und Audio-Tests. Ein erfolgreicher Build ersetzt keinen Test mit Windows, Rekordbox und FLX4.

## Download und Installation

1. Unter [Actions → Build and test](https://github.com/terrabrightxD/flx4-voicechat/actions/workflows/build.yml) den neuesten **erfolgreichen** Lauf öffnen.
2. Unten das Artefakt **flx4-voicechat-26.3** herunterladen (GitHub-Anmeldung nötig) und entpacken.
3. Die enthaltene `flx4-voicechat-0.2.0-alpha.1.jar` in den `mods`-Ordner deiner Minecraft-Instanz legen.
4. Benötigt: Minecraft **26.3**, Fabric Loader **0.19.5 oder neuer**, Java **25**, Simple Voice Chat **2.6.27 oder neuer** für 26.3. Zusätzlich **Fabric API 0.160.5+26.3 oder neuer für 26.3** installieren.
5. Auf dem Server muss Simple Voice Chat funktionieren. Nur der DJ installiert diese Zusatzmod; Zuhörer benötigen Simple Voice Chat.

## Windows und Rekordbox einrichten

**Signalweg:** FLX4 steuert Rekordbox → Rekordbox Master → CABLE Input → CABLE Output → diese Mod → Simple Voice Chat.

1. [VB-CABLE vom Hersteller](https://vb-audio.com/Cable/) installieren und Windows neu starten.
2. In den klassischen Windows-Soundeinstellungen die Formate des virtuellen Kabels auf **48.000 Hz, 16 Bit, Stereo** setzen. Exklusiven Zugriff bei Problemen abschalten.
3. In Rekordbox das Audio-Gerät **DDJ-FLX4 WASAPI** beibehalten. Kopfhörer bleiben auf **DDJ-FLX4 PHONES**, damit Cue/Vorhören privat bleibt.
4. **PC MASTER OUT** aktivieren und den zusätzlichen Master-Ausgang auf **CABLE Input (VB-Audio Virtual Cable)** führen. Der genaue Auswahlweg hängt von der Rekordbox-Version ab. Prüfe im Master-Output-Feld, dass FLX4 MASTER **plus CABLE Input** steht. Wenn PC MASTER OUT nur das Windows-Standardgerät übernimmt: CABLE Input vor dem Start von Rekordbox als Standard-Wiedergabegerät wählen.
5. Falls du das Windows-Standardgerät geändert hast: Minecraft, Simple Voice Chat, Browser und Discord ausdrücklich auf echte Kopfhörer/Lautsprecher routen. Sonst landen fremde Stimmen oder Spielgeräusche wieder im Kabel. Windows-Systemklänge für den Auftritt abschalten. Das Kabel soll nur den gewünschten Mix enthalten.
6. In **Simple Voice Chat** dein echtes Mikrofon auswählen, **nicht CABLE Output**. Auch das Wiedergabegerät ausdrücklich auf deine Kopfhörer setzen. Ein funktionierender Voice-Chat-Mikrofoneingang ist für dessen Audiozyklus erforderlich.
7. Server betreten und **F8** drücken. Das eigene **FLX4 · DJ-Mixer**-Menü öffnet sich direkt in Minecraft. Unter **Optionen → Steuerung → Tastenbelegung → FLX4 Voice Chat** lässt sich die Taste ändern.
8. Dort mit dem obersten Button durch die Audioeingänge schalten und **CABLE Output** auswählen, einen Track in Rekordbox starten und **START** drücken. Die Anzeige sollte ausschlagen.
9. Andere Spieler hören die Musik in deiner Nähe. Für räumlichen Klang eine eventuell aktive Voice-Chat-Gruppe verlassen; in einer Gruppe gelten deren normalen Übertragungsregeln.

Die PC-MASTER-OUT-Funktion ist vom Hersteller dokumentiert. Das Routing auf VB-CABLE ist die hier vorgeschlagene Kombination und muss mit deiner Rekordbox-Version getestet werden. Falls Rekordbox das Kabel nicht übernimmt, zuerst den Windows-Aufnahmepegel von CABLE Output prüfen – ohne Signal dort kann auch die Mod nichts senden.

## Bedienung

- **START / STOPP**: Musikübertragung starten oder beenden. Kein automatischer Start nach Installation oder Wiederverbindung.
- **Lautstärke 0–200 %**: Start bei 70 %. Bei Übersteuerung in Rekordbox und/oder im DJ-Menü reduzieren. Die Begrenzung verhindert Zahlenüberlauf, ist aber kein Studio-Limiter.
- **Musik auch bei stummem Mikrofon senden**: standardmäßig aus. Aktivieren, wenn du das Mikrofon stummschalten, aber Musik weitersenden möchtest.
- **Push-to-Talk steuert nur das Mikrofon**. Musik läuft nach START dauerhaft, auch ohne gedrückte Sprechtaste. STOPP beendet sie.
- **Escape / Zurück zum Spiel** schließt nur das Menü: Musik läuft weiter. Mit F8 erneut öffnen und **Musik stoppen** drücken. Das Menü pausiert das Spiel nicht.
- **Disconnect / Voice Chat deaktivieren** stoppt die Aufnahme. Nach erneuter Verbindung START drücken.
- Einstellungen werden in dieser Alpha nicht gespeichert; du wählst Gerät und Start bei jeder Sitzung bewusst neu.

## Was integriert ist – und was nicht

Es wird der fertige Audiomix übernommen. Jogwheels, Fader, Effekte und Cue funktionieren weiterhin über Rekordbox. Die Mod liest keine Rekordbox-Datenbank, lädt keine Tracks und belegt keine MIDI-Regler.

Über den normalen Sprachkanal wird **Mono** übertragen (Stereo wird gemittelt); echte Stereoübertragung und ein eigener Lautsprecherblock sind nicht enthalten. Die Latenz hängt zusätzlich von Windows, Kabel, Voice Chat und Netzwerk ab. Zum Beatmatching deshalb direkt am FLX4 vorhören, nicht über den Minecraft-Rückweg.

## Fehlerbehebung

| Problem | Prüfen |
|---|---|
| Menü öffnet nicht | F8 im Spiel drücken; bei Tastenkonflikten unter Steuerung eine andere Taste zuweisen. Fabric API installiert? |
| Kein Gerät sichtbar | VB-CABLE installieren, neustarten, „Audiogeräte neu laden“. Windows muss CABLE Output als Aufnahmegerät zeigen. |
| Aufnahmefehler | Gerät auf 48 kHz/16 Bit stellen; exklusiven Zugriff anderer Programme beenden. Kein ASIO-Gerät direkt auswählen. |
| Pegel bewegt sich, niemand hört Musik | Voice Chat verbunden/aktiv? Mikrofon stumm? Option für Musik trotz Stummschaltung prüfen. Server-Sprechrechte, Gruppe und Entfernung prüfen. |
| Echo oder doppelte Musik | CABLE Output nicht zusätzlich als Voice-Chat-Mikrofon verwenden. Spiel/Voice-Chat-Wiedergabe niemals auf CABLE Input schicken. |
| Abgehackter Klang | Audioformate prüfen, CPU-Last reduzieren und Rekordbox-Puffer erhöhen. Windows-Geräte- und Multiplayer-Test steht noch aus. |
| Verzerrungen beim Sprechen über Musik | Beide Pegel reduzieren: Sprache und Musik werden im Voice-Chat-Kanal zusammengemischt. |

## Entwicklung

JDK 25 und Gradle 9.8.2 installieren, dann:

```sh
gradle clean build
```

Ausgabe: `build/libs/flx4-voicechat-0.2.0-alpha.1.jar`.

Das Menü verwendet native Minecraft-Screens, Buttons, Tooltips und Lautstärke-Slider. Schrift, Fokusrahmen und Widget-Texturen entsprechen Minecraft und reagieren auf Ressourcenpakete. Kein separates Desktop-Fenster und kein zusätzlicher Java-Prozess. Fabric Loom 1.17.21 kompiliert gegen Minecraft 26.3.

- Aufnahme: 48 kHz, signed PCM16 little endian; Stereo mit Mono-Fallback.
- Frames: 960 Samples / 20 ms. Maximal drei Frames im FIFO; alte Frames über 100 ms werden verworfen.
- Keine Datei-Aufzeichnung, keine zusätzlichen Netzwerkports und kein eigener Serverdienst.
- `gradle audioTest`: Tests für PCM-Dekodierung, Downmix, Übersteuerung, Pegel und Puffer.
- Siehe [TESTING.md](TESTING.md) für den noch erforderlichen Praxistest.

## Quellen

- [Simple Voice Chat – API](https://modrepo.de/minecraft/voicechat/api/getting_started)
- [MergeClientSoundEvent im 26.3-Quellcode](https://github.com/henkelmax/simple-voice-chat/blob/26.3/api/src/main/java/de/maxhenkel/voicechat/api/events/MergeClientSoundEvent.java)
- [AlphaTheta: FLX4 / Rekordbox / PC MASTER OUT unter Windows](https://support.alphatheta.com/en-US/articles/26128189455769)
- [VB-CABLE](https://vb-audio.com/Cable/)

Unabhängiges Community-Projekt; keine offizielle Integration von AlphaTheta/Pioneer DJ oder Mojang. Lizenz: MIT.
