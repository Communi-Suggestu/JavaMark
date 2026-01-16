package com.communi.suggestu.javamark.doclet;

import com.communi.suggestu.javamark.doclet.utils.Option;
import jdk.javadoc.doclet.Doclet;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class Configuration
{
    private Path destDir;

    public Set<Doclet.Option> getSupportedOptions() {
        return Set.of(
            new Option("-d", 1, "", "The destination directory.") {
                @Override
                public boolean process(String opt, List<String> args) {
                    destDir = Path.of(args.getFirst());
                    return true;
                }
            }
        );
    }

    public boolean isValid() {
        return destDir != null;
    }

    public void prepare() throws IOException
    {
        FileUtils.deleteDirectory(getDestination().toFile());
    }

    public Path getDestination()
    {
        return destDir;
    }
}
