package com.ilyabogdanovich.jelly.utils

/**
 * Helper to hold either one or another type in the same variable.
 * It is convenient to use for returning either result or error.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
sealed class Either<L, R> {
    data class Left<L, R>(val value: L) : Either<L, R>()
    data class Right<L, R>(val value: R) : Either<L, R>()

    inline fun <T> mapRight(mapper: (R) -> T): Either<L, T> = when (this) {
        is Left -> Left(value)
        is Right -> Right(mapper(value))
    }
}

fun <L, R> L.asLeft(): Either<L, R> = Either.Left(this)

fun <L, R> R.asRight(): Either<L, R> = Either.Right(this)

inline fun <L, R, T> Either<L, R>.mapEitherRight(mapper: (R) -> Either<L, T>): Either<L, T> =
    mapRight {
        return when (val result = mapper(it)) {
            is Either.Right -> result.value.asRight()
            is Either.Left -> result.value.asLeft()
        }
    }
