package de.terrabright.flx4;

import java.io.DataOutputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Smoke test for the real desktop subprocess and private status pipe. Run under Xvfb on Linux. */
public final class DesktopTest {
    public static void main(String[] args) throws Exception {
        Process child = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Djava.awt.headless=false", "-cp", args[0], "de.terrabright.flx4.ControlPanel")
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try {
            DataOutputStream status = new DataOutputStream(child.getOutputStream());
            status.writeUTF("DEVICES");
            status.writeInt(1);
            status.writeUTF("Test CABLE Output");
            status.writeUTF("STATUS");
            status.writeBoolean(true);
            status.writeBoolean(false);
            status.writeInt(0);
            status.writeUTF("Gestoppt");
            status.writeUTF("Verbunden");
            status.flush();
            if (child.waitFor(2, TimeUnit.SECONDS)) throw new AssertionError("Desktop exited before pipe closed: " + child.exitValue());
            status.close();
            if (!child.waitFor(5, TimeUnit.SECONDS)) throw new AssertionError("Desktop did not exit after parent pipe closed");
            if (child.exitValue() != 0) throw new AssertionError("Desktop failed: " + child.exitValue());
            System.out.println("Desktop subprocess opened and exited cleanly after pipe closure");
        } finally { child.destroyForcibly(); }
    }
}
