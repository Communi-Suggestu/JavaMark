package com.communi.suggestu.javamark.doclet.utils;

import jdk.javadoc.doclet.Doclet;

import java.util.Arrays;
import java.util.List;

public abstract class Option implements Doclet.Option, Comparable<Option>
{
    private final String[] names;
    private final String   parameters;
    private final String   description;
    private final int      argCount;

    protected Option(String name, int argCount, String parameters, String description)
    {
        this.names = name.trim().split("\\s+");
        this.argCount = argCount;
        this.parameters = parameters;
        this.description = description;
    }

    protected Option(String name, String parameters, String description)
    {
        this(name, 0, parameters, description);
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public Kind getKind()
    {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames()
    {
        return Arrays.asList(names);
    }

    @Override
    public String getParameters()
    {
        return parameters;
    }

    @Override
    public String toString()
    {
        return Arrays.toString(names);
    }

    @Override
    public int getArgumentCount()
    {
        return argCount;
    }

    public boolean matches(String option)
    {
        for (String name : names)
        {
            boolean matchCase = name.startsWith("--");
            if (option.startsWith("--") && option.contains("="))
            {
                return name.equals(option.substring(option.indexOf("=") + 1));
            }
            else if (matchCase)
            {
                return name.equals(option);
            }
            return name.equalsIgnoreCase(option);
        }
        return false;
    }

    @Override
    public int compareTo(Option that)
    {
        return this.getNames().getFirst().compareTo(that.getNames().getFirst());
    }
}