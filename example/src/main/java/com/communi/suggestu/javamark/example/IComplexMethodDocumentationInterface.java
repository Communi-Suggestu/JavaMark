package com.communi.suggestu.javamark.example;

import java.util.Optional;

/**
 * This is an interface with a complex method documantation.
 */
public interface IComplexMethodDocumentationInterface
{

    /**
     * Returns the current {@link Object} if there is one.
     * If a new chiseling operation is started no {@link Object} is available,
     * as such an empty {@link Optional} will be returned in that case.
     * <p>
     * Only after the primary call to something or something
     * the returned {@link Optional} can contain a {@link Object}.
     * <p>
     * @return The {@link Optional} containing the {@link Object}.
     */
    Optional<Object> getMutator();
}
