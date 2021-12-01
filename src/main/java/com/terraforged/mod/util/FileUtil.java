package com.terraforged.mod.util;

import com.terraforged.mod.TerraForged;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {
    public static Stream<Path> listFiles(Path dir) throws IOException {
        return Files.list(dir).flatMap(path -> {
            if (Files.isDirectory(path)) {
                try {
                    return listFiles(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return Stream.of(path);
        });
    }

    public static void copy(Path from, Path to) throws IOException {
        FileUtil.listFiles(from).parallel().forEach(file -> {
            try {
                var path = from.relativize(file);
                var dest = to.resolve(path);
                var dir = dest.getParent();

                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }

                TerraForged.LOG.info("Copying file: {} -> {}", file, dest);
                Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void createZipCopy(Path from, Path to) throws IOException {
        try (var output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(to)))) {
            ZipUtils.copyDirToZip(from, from, output);
        }
    }

    protected static class ZipUtils {
        protected static void copyToZip(Path file, Path from, ZipOutputStream output) throws IOException {
            var path = from.relativize(file);
            if (Files.isDirectory(file)) {
                output.putNextEntry(createEntry(file, path, true));
                output.closeEntry();
                copyDirToZip(file, from, output);
            } else {
                output.putNextEntry(createEntry(file, path, false));
                copyFileToZip(file, output);
                output.closeEntry();
            }
        }

        protected static void copyDirToZip(Path dir, Path from, ZipOutputStream output) throws IOException {
            FileUtil.listFiles(dir).forEach(file -> {
                try {
                    copyToZip(file, from, output);
                } catch (IOException e) {
                    throw new Error(e);
                }
            });
        }

        protected static void copyFileToZip(Path file, ZipOutputStream output) throws IOException {
            try (var input = Files.newBufferedReader(file)) {
                IOUtils.copy(input, output, Charset.defaultCharset());
            }
        }

        protected static ZipEntry createEntry(Path file, Path path, boolean dir) throws IOException {
            var name = path.toString().replace('\\', '/');

            if (dir && !name.endsWith("/")) {
                name += "'/";
            }

            var entry = new ZipEntry(name);
            entry.setTime(Files.getLastModifiedTime(file).toMillis());

            return entry;
        }
    }
}
