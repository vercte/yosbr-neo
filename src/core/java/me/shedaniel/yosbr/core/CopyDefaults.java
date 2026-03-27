package me.shedaniel.yosbr.core;

import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CopyDefaults {
    public static final File RUN_DIR = FMLPaths.GAMEDIR.get().toFile();
    public static final File CONFIG_DIR = FMLPaths.CONFIGDIR.get().toFile();
    public static boolean initialized;

    public static void respectTheOptions() {
        if (initialized) return; // NeoForge constructs language loaders twice; first when counting and when actually loading
        initialized = true;

        YOSBRLanguageLoader.LOGGER.info("Applying default options... (YOSBR)");
        try {
            File yosbr = new File(CONFIG_DIR, "yosbr");
            if (!yosbr.exists() && !yosbr.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + yosbr.getAbsolutePath());
            }

            //noinspection ResultOfMethodCallIgnored
            new File(yosbr, "options.txt").createNewFile();
            File config = new File(yosbr, "config");
            if (!config.exists() && !config.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + config.getAbsolutePath());
            }
            Files.walk(yosbr.toPath()).forEach(path -> {
                File file = path.normalize().toAbsolutePath().normalize().toFile();
                if (!file.isFile()) return;
                try {
                    try {
                        Path configRelative = config.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize());
                        if (configRelative.startsWith("yosbr"))
                            throw new IllegalStateException("Illegal default config file: " + file);
                        applyDefaultOptions(new File(CONFIG_DIR, configRelative.normalize().toString()), file);
                    } catch (IllegalArgumentException e) {
                        System.out.println(yosbr.toPath().toAbsolutePath().normalize());
                        System.out.println(file.toPath().toAbsolutePath().normalize());
                        System.out.println(yosbr.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()));
                        applyDefaultOptions(new File(RUN_DIR, yosbr.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()).normalize().toString()), file);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            YOSBRLanguageLoader.LOGGER.error("Failed to apply default options.", e);
        }
    }
    
    private static void applyDefaultOptions(File file, File defaultFile) throws IOException {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + file.getParentFile().getAbsolutePath());
        }
        if (!defaultFile.getParentFile().exists() && !defaultFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + defaultFile.getParentFile().getAbsolutePath());
        }
        if (!defaultFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            defaultFile.createNewFile();
            return;
        }
        if (file.exists()) return;
        YOSBRLanguageLoader.LOGGER.info("Applying default options for {}{} from {}{}",
                File.separator, RUN_DIR.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()).normalize().toString(),
                File.separator, RUN_DIR.toPath().toAbsolutePath().normalize().relativize(defaultFile.toPath().toAbsolutePath().normalize()).normalize().toString());
        Files.copy(defaultFile.toPath(), file.toPath());
    }
}
