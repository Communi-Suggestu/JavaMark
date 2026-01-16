package com.communi.suggestu.javamark.example;

import java.util.List;

/**
 * This is a test class
 */
public class SimpleTestClass
{

    /**
     * A public field
     */
    public final String finalStringField = "test";

    /**
     * Some constructor
     */
    public SimpleTestClass() {}

    /**
     * Another constructor with a parameter
     *
     * @param numericalParameter The parameter
     */
    public SimpleTestClass(final int numericalParameter) {}

    /**
     * Protected methods work
     */
    protected void someProtectedMethod() {}

    /**
     * Public methods work
     */
    public void somePublicMethod() {}

    /**
     * Methods returning generics work
     *
     * @return The generics.
     */
    public List<String> methodsReturningGenerics() {return null;}

    /**
     * Methods returning internal type work
     *
     * @return The internal type
     */
    public SimpleTestClass methodsReturningInternalType() {return null;}

    /**
     * A static inner class
     */
    public static class StaticInnerClass {

        /**
         * Comment on constructor with public inner class.
         */
        public StaticInnerClass()
        {
        }
    }

    /**
     * An inner class
     */
    public class InnerClass {

        /**
         * Private constructor with super call.
         */
        private InnerClass()
        {
            super();
        }
    }

    /**
     * A simple inner record
     */
    public record InnerRecord() {}

    /**
     * A record with a component
     *
     * @param component a component.
     */
    public record InnerRecordWithComponent(String component) {}

    /**
     * An enum without values
     */
    public enum InnerEnum {
    }

    /**
     * An enum with values
     */
    public enum InnerEnumWithValues {
        /**
         * Comment on value.
         */
        VALUE;
    }

    /**
     * An interface
     */
    public interface InnerInterface {

        /**
         * Apply the current value
         */
        void apply();

        /**
         * {@return the underlying value with inline return}
         */
        Object value();
    }

    /**
     * Interface with extension and inherited doc
     */
    public interface ExtendedInterface extends InnerInterface {

        /**
         * {@inheritDoc}
         */
        void apply();
    }

    /**
     * Inheriting class.
     */
    public final static class InheritingClass extends SimpleTestClass {

        /**
         * Public constructor without super call.
         */
        public InheritingClass()
        {
        }
    }

    /**
     * Complex class inheritance with generic
     * @param <G> The generic parameter
     */
    public static class ComplexClass<G> extends SimpleTestClass implements ExtendedInterface {

        /**
         * Public default constructor with comment.
         */
        public ComplexClass()
        {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void apply()
        {
        }

        /**
         * Different value comment
         * @return The value.
         */
        @Override
        public Object value()
        {
            return null;
        }
    }
}
