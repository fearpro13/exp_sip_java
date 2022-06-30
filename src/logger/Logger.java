package logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Logger {
    private final File file;

    public Logger(String logFileName) {
        String logFileDir = System.getProperty("user.dir");
        String logFilePath = String.format("%s/%s", logFileDir, logFileName);
        this.file = new File(logFilePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.printf("Could not initiate log file %s \n", logFilePath);
            }
        }
        System.out.printf("Logger started writing to %s \n", logFilePath);
    }

    public void write(String message) {
        try {
            Files.write(Paths.get(this.file.getPath()), message.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("Could not write to log file %s \n", this.file.getAbsolutePath());
        }
    }
}
