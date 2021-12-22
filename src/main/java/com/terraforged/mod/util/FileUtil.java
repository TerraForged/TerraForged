/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.util;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {
    public static void walk(Path root, String path, FileSystemVisitor visitor) throws IOException {
        if (Files.isDirectory(root)) {
            walkDir(root, path, visitor);
        } else {
            walkSystem(root, path, visitor);
        }
    }

    public static void walkDir(Path root, String path, FileSystemVisitor visitor) throws IOException {
        root = root.resolve(path);
        walk(FileSystems.getDefault(), root, root, visitor);
    }

    public static void walkSystem(Path root, String path, FileSystemVisitor visitor) throws IOException {
        try (var fs = FileSystems.newFileSystem(root)) {
            root = fs.getPath(path);
            walk(fs, root, root, visitor);
        }
    }

    public static void walk(FileSystem fs, Path root, Path path, FileSystemVisitor visitor) throws IOException {
        var file = fs.getPath(path.toString());
        if (Files.isDirectory(file)) {
            try (var stream = fs.provider().newDirectoryStream(file, entry -> true)) {
                stream.forEach(f -> {
                    try {
                        walk(fs, root, f, visitor);
                    } catch (IOException e) {
                        throw new Error(e);
                    }
                });
            }
        } else {
            visitor.visit(fs, root, file);
        }
    }

    public static void createDirCopy(Path fromRoot, String fromPath, Path to) throws IOException {
        walk(fromRoot, fromPath, (fs, root, file) -> {
            var relative = root.relativize(file);
            var dest = resolve(to, relative);
            if (Files.exists(dest) || Files.isDirectory(file)) {
                return;
            }

            var parent = dest.getParent();
            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Files.copy(file, dest);
        });
    }

    public static void createZipCopy(Path from, String path, Path to) throws IOException {
        try (var output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(to)))) {
            walk(from, path, (fs, root, file) -> {
                var name = root.relativize(file).toString().replace('\\', '/');
                if (Files.isDirectory(file)) {
                    if (!name.endsWith("/")) {
                        name += "'/";
                    }

                    var entry = new ZipEntry(name);
                    entry.setTime(System.currentTimeMillis());
                    output.putNextEntry(entry);
                    output.closeEntry();
                } else {
                    var entry = new ZipEntry(name);
                    entry.setTime(System.currentTimeMillis());
                    output.putNextEntry(entry);
                    try (var input = new InputStreamReader(fs.provider().newInputStream(file))) {
                        IOUtils.copy(input, output, Charset.defaultCharset());
                    }
                    output.closeEntry();
                }
            });
        }
    }

    public static void delete(Path path) {
        if (!Files.exists(path)) return;

        if (Files.isDirectory(path)) {
            try (var files = Files.list(path)) {
                files.forEach(FileUtil::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path resolve(Path base, Path path) {
        var result = base;
        for (var part : path) {
            result = result.resolve(part.getFileName().toString());
        }
        return result;
    }

    public interface FileSystemVisitor {
        void visit(FileSystem fs, Path root, Path path) throws IOException;
    }
}
