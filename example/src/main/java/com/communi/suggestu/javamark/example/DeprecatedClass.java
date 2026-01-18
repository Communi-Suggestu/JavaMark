package com.communi.suggestu.javamark.example;

/**
 * A deprecated class, just not for removal.
 * @deprecated Yes we are deprecated
 */
@Deprecated
public class DeprecatedClass
{

    /**
     * Some private constructor.
     */
    private DeprecatedClass()
    {
    }

    /**
     * We will really remove this crap.
     * @deprecated Removing it!
     */
    @Deprecated(forRemoval = true)
    public static class ReallyDeprecatedClass {

        /**
         * This will be yeeted
         */
        private ReallyDeprecatedClass()
        {
        }
    }
}
