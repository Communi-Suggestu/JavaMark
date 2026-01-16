package com.communi.suggestu.javamark.doclet;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaMarkIntegrationTest
{

    @Test
    void testJavadocIntegration() throws Exception {
        Method execute = Class.forName("jdk.javadoc.internal.tool.Main").getMethod("execute", String[].class);
        execute.setAccessible(true);
        String outputDirectory = "target/javadoc-output";
        deleteRecursively(outputDirectory);
        int result = (int) execute.invoke(null, (Object) new String[]{
            "--module-path", classpath(),
            "-doclet", "com.communi.suggestu.javamark.doclet.JavaMarkDoclet",
            "--source-path", "../example/src/main/java",
            "-d", outputDirectory,
            "-subpackages", "com.communi.suggestu.javamark.example",
        });
        assertThat(result).isEqualTo(0);
    }

    private void deleteRecursively(String outputDirectory) throws IOException
    {
        Path outputPath = Paths.get(outputDirectory);
        if (Files.exists(outputPath)) {
            Files.walkFileTree(outputPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private String classpath() {
        return Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
            .filter(s -> !s.contains("ideaIU")) // Filter out Intellij jar files.
            .collect(Collectors.joining(File.pathSeparator));
    }
}