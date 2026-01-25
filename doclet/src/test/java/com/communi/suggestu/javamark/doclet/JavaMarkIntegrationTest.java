package com.communi.suggestu.javamark.doclet;

import org.junit.jupiter.api.Test;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaMarkIntegrationTest
{

    private static final String COMPARISON_OPTIONS_FILE = "../example/build/tmp/javadoc/javadoc.options";
    private static final String COMPARISON_DEFAULT_OUTPUT = "../example/build/docs";

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

    @Test
    void testDocToolIntegration() throws Exception {
        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();

        String outputDirectory = "target/javadoc-output";
        deleteRecursively(outputDirectory);
        int result = (int) docTool.run(System.in, System.out, System.err, new String[]{
            "--module-path", classpath(),
            "-doclet", "com.communi.suggestu.javamark.doclet.JavaMarkDoclet",
            "--source-path", "../example/src/main/java",
            "-d", outputDirectory,
            "-subpackages", "com.communi.suggestu.javamark.example",
        });
        assertThat(result).isEqualTo(0);
    }


    @Test
    void testHtmlJavadocIntegration() throws Exception {
        Method execute = Class.forName("jdk.javadoc.internal.tool.Main").getMethod("execute", String[].class);
        execute.setAccessible(true);
        deleteRecursively(COMPARISON_DEFAULT_OUTPUT);
        int result = (int) execute.invoke(null, (Object) processDefaultOptions(Files.readAllLines(Path.of(COMPARISON_OPTIONS_FILE)).toArray(String[]::new)));
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

    private String[] processDefaultOptions(String[] input) {
        final List<String> results = new ArrayList<>();
        for (String s : input)
        {
            if (s.startsWith("-classpath")) {
                s = s.replace("-classpath", "--module-path");
            }

            if (s.contains(" ")) {
                var firstPart = s.substring(0, s.indexOf(" "));
                var remainder = s.substring(firstPart.length() + 1);

                if (remainder.startsWith("'") && remainder.endsWith("'")) {
                    remainder = remainder.substring(1, remainder.length() - 1);
                }

                results.add(firstPart);
                results.add(remainder);
                continue;
            }

            if (s.startsWith("'") && s.endsWith("'")) {
                s = s.substring(1, s.length() - 1);
            }
            results.add(s);
        }

        return results.stream().filter(s -> !s.isBlank()).toArray(String[]::new);
    }
}