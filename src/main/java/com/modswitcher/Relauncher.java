package com.modswitcher;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tiny helper that runs in its own JVM after Minecraft has been told to close.
 * It waits for the game process to end (so the jars are unlocked), renames the queued mod files,
 * then starts the game again with the same command line. Uses only JDK classes.
 */
public final class Relauncher {
    private Relauncher() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            return;
        }
        Path plan = Path.of(args[0]);
        List<String> lines = Files.readAllLines(plan, StandardCharsets.UTF_8);
        long pid = Long.parseLong(lines.get(0).trim());
        File workDir = new File(lines.get(1));

        List<Path[]> moves = new ArrayList<>();
        List<String> command = new ArrayList<>();
        for (int i = 2; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t", 3);
            if (parts.length >= 3 && parts[0].equals("MOVE")) {
                moves.add(new Path[]{Path.of(parts[1]), Path.of(parts[2])});
            } else if (parts.length >= 2 && parts[0].equals("CMD")) {
                command.add(parts[1]);
            }
        }

        ProcessHandle.of(pid).ifPresent(handle -> {
            try {
                handle.onExit().get(180, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // timed out or interrupted: carry on anyway
            }
        });
        Thread.sleep(1500);

        for (Path[] move : moves) {
            for (int attempt = 0; attempt < 60; attempt++) {
                try {
                    if (!Files.exists(move[0]) || Files.exists(move[1])) {
                        break;
                    }
                    Files.move(move[0], move[1]);
                    break;
                } catch (Exception e) {
                    Thread.sleep(500);
                }
            }
        }

        try {
            Files.deleteIfExists(plan);
        } catch (Exception ignored) {
            // temp file, harmless if it stays
        }

        if (!command.isEmpty()) {
            new ProcessBuilder(command)
                    .directory(workDir)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
        }
    }
}
