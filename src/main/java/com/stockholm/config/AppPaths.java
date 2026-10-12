package com.stockholm.config;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {
    private AppPaths() {
    }

    public static Path resolveApplicationDirectory() {
        try {
            File codeSource = Paths.get(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
            File base = codeSource.isFile() ? codeSource.getParentFile() : codeSource;
            if (base != null) {
                return base.toPath().toAbsolutePath().normalize();
            }
        } catch (URISyntaxException ignored) {
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    public static Path resolveDatabaseFile() {
        return resolveApplicationDirectory().resolve("stockholm.db");
    }

    public static Path resolveBannerFile() {
        Path applicationBanner = resolveApplicationDirectory().resolve("banniere.jpg");
        if (java.nio.file.Files.isRegularFile(applicationBanner)) {
            return applicationBanner;
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize().resolve("banniere.jpg");
    }
}
