package com.antcolonywars.desktop;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class ClassPathResolver {
    private ClassPathResolver() {
    }

    static String forClasses(Class<?>... classes) {
        Set<Path> paths = Arrays.stream(classes)
                .map(ClassPathResolver::locationOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return paths.stream()
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private static Path locationOf(Class<?> type) {
        try {
            return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException | NullPointerException exception) {
            throw new IllegalStateException("Cannot resolve classpath location for " + type.getName(), exception);
        }
    }
}
