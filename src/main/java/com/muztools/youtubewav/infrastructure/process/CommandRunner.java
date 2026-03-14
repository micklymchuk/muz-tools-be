package com.muztools.youtubewav.infrastructure.process;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CommandRunner {

    public void run(List<String> command, Path workingDirectory, Duration timeout) {
        runAndCapture(command, workingDirectory, timeout);
    }

    public String runAndCapture(List<String> command, Path workingDirectory, Duration timeout) {
        Process process = null;
        try {
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
                throw new CommandExecutionException(output.isBlank()
                        ? "Command failed: " + String.join(" ", command)
                        : output);
            }

            return output;
        } catch (IOException exception) {
            String detail = exception.getMessage() == null ? "unknown IO error" : exception.getMessage();
            throw new CommandExecutionException(
                    "Failed to execute command: " + String.join(" ", command) + " (" + detail + ")"
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CommandExecutionException("Command execution interrupted");
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }
}
