package mu.rekolt.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//  One timestamped line appended to output/run-log.txt per generation
public final class WriteRunLog {

    private WriteRunLog() { }

    private static final DateTimeFormatter Stamp =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //  APPEND is what makes the file a history. Without it every run erases the
//  last one. CREATE makes the file on the first run instead of failing.
    public static void append(Path logFile, String message) {
        try {
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }
//          try-with-resources closes the writer whether the write succeeds or throws
            try (BufferedWriter writer = Files.newBufferedWriter(
                    logFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                writer.write(LocalDateTime.now().format(Stamp) + "  " + message);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("  Note: the run log could not be updated (" + e.getMessage()
                    + "). The report itself is unaffected.");
        }
    }
}