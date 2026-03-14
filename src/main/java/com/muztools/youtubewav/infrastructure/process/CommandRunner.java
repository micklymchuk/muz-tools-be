package com.muztools.youtubewav.infrastructure.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CommandRunner {

    private static final Logger log = LoggerFactory.getLogger(CommandRunner.class);

    public void run(List<String> command, Path workingDirectory, Duration timeout) {
        runAndCapture(command, workingDirectory, timeout);
    }

    public String runAndCapture(List<String> command, Path workingDirectory, Duration timeout) {
        Process process = null;
        long startedAtNanos = System.nanoTime();
        try {
            log.info("Running command in {}: {}", workingDirectory, String.join(" ", command));
            process = new ProcessBuilder(command)
                    .directory(workingDirectory.toFile())
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

            if (!finished) {
                process.destroyForcibly();
                throw new CommandExecutionException("Command timed out: " + String.join(" ", command));
            }

            if (process.exitValue() != 0) {
                log.error("Command failed with exit code {} after {} ms: {}",
                        process.exitValue(),
                        elapsedMillis(startedAtNanos),
                        summarize(command, output));
                throw new CommandExecutionException(output.isBlank()
                        ? "Command failed: " + String.join(" ", command)
                        : output);
            }

            log.info("Command finished in {} ms: {}", elapsedMillis(startedAtNanos), String.join(" ", command));
            return output;
        } catch (IOException exception) {
            String detail = exception.getMessage() == null ? "unknown IO error" : exception.getMessage();
            log.error("Failed to execute command {}: {}", String.join(" ", command), detail, exception);
            throw new CommandExecutionException(
                    "Failed to execute command: " + String.join(" ", command) + " (" + detail + ")"
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Command execution interrupted: {}", String.join(" ", command), exception);
            throw new CommandExecutionException("Command execution interrupted");
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    private long elapsedMillis(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000;
    }

    private String summarize(List<String> command, String output) {
        if (output == null || output.isBlank()) {
            return String.join(" ", command);
        }
        String normalized = output.replaceAll("\\s+", " ").trim();
        return String.join(" ", command) + " :: " + normalized;
    }
}
