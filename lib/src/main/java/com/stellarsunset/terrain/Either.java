package com.stellarsunset.terrain;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Simple class for working with one of two possible values in a return type.
 *
 * <p>This makes it easier to work with Go-like syntax in error handling, returning {@code Either<Value, Error>}. Often
 * the {@code Error} will be a sealed interface of known error conditions which can either be handled or down-converted
 * to {@link RuntimeException}s and thrown.
 */
public record Either<L, R>(Optional<L> left, Optional<R> right) {

    public static <L, R> Either<L, R> ofLeft(L left) {
        return new Either<>(Optional.of(left), Optional.empty());
    }

    public static <L, R> Either<L, R> ofRight(R right) {
        return new Either<>(Optional.empty(), Optional.of(right));
    }

    public Either {
        checkArgument(left.isEmpty() || right.isEmpty(), "Left and Right cannot both be present");
        checkArgument(left.isPresent() || right.isPresent(), "Left and Right cannot both be missing");
    }

    public <T> Either<T, R> mapLeft(Function<L, T> fn) {
        return new Either<>(left.map(fn), right);
    }

    public <T> Either<L, T> mapRight(Function<R, T> fn) {
        return new Either<>(left, right.map(fn));
    }

    public Either<R, L> swap() {
        return new Either<>(right, left);
    }

    public <T> T apply(Function<L, T> lFn, Function<R, T> rFn) {
        return left.map(lFn).or(() -> right.map(rFn)).orElseThrow();
    }

    /**
     * Return the non-null right side of the either or convert the left side to a {@link RuntimeException} and throw.
     */
    public <E extends RuntimeException> R orThrowLeft(Function<L, E> toException) {
        if (left.isPresent()) {
            throw toException.apply(left.get());
        }
        return right.orElseThrow();
    }

    /**
     * Return the non-null left side of the either or convert the right side to a {@link RuntimeException} and throw.
     */
    public <E extends RuntimeException> L orThrowRight(Function<R, E> toException) {
        if (right.isPresent()) {
            throw toException.apply(right.get());
        }
        return left.orElseThrow();
    }
}
