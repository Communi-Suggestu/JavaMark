package com.communi.suggestu.javamark.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.util.List;
import java.util.Map;

public class JavaMarkPlugin implements Plugin<Project>
{

    private static final String CONFIGURATION_NAME = "javaMarkDoclet";
    private static final String DOCLET_CLASS       = "com.communi.suggestu.javamark.doclet.JavaMarkDoclet";

    private static final List<String> EXPORTS = List.of(
        "jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED",
        "jdk.javadoc/jdk.javadoc.internal.doclets.formats.html=ALL-UNNAMED",
        "jdk.javadoc/jdk.javadoc.internal.doclets.formats.html.markup=ALL-UNNAMED",
        "jdk.javadoc/jdk.javadoc.internal.doclets.toolkit.util=ALL-UNNAMED",
        "jdk.javadoc/jdk.javadoc.internal.doclets.toolkit.taglets=ALL-UNNAMED",
        "jdk.javadoc/jdk.javadoc.internal.doclets.toolkit=ALL-UNNAMED"
    );

    private static final List<String> OPENS = List.of(
        "jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED",
        "jdk.javadoc/jdk.javadoc.internal.doclets.formats.html=ALL-UNNAMED",
        "jdk.javadoc/jdk.javadoc.internal.doclets.toolkit.builders=ALL-UNNAMED"
    );

    @Override
    public void apply(Project target)
    {
        var version = target.getProviders().gradleProperty("javaMarkVersion").orElse("latest.release");
        var configuration = target.getConfigurations().register(CONFIGURATION_NAME);
        target.getDependencies().addProvider(CONFIGURATION_NAME, version.map(v -> "com.communi-suggestu.javamark:doclet:" + v));
        target.getTasks().register("generateMarkdownDocs", JavaMarkTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Generates documentation in markdown format using the JavaMark doclet.");
            task.getOptions().doclet(DOCLET_CLASS);
            task.getOptions().setDocletpath(List.copyOf(configuration.get().getFiles()));
            task.setDestinationDir(target.getLayout().getBuildDirectory().dir("docs/markdown").get().getAsFile());
            task.options(opts -> {
                if (opts instanceof StandardJavadocDocletOptions sOpts)
                {
                    sOpts.addMultilineStringsOption("-add-exports")
                            .setValue(EXPORTS);
                    sOpts.addMultilineStringsOption("-add-opens")
                            .setValue(OPENS);
                }
                else
                {
                    throw new IllegalStateException("Options are not standard");
                }
            });
        });
    }

    public static abstract class JavaMarkTask extends Javadoc {}
}
