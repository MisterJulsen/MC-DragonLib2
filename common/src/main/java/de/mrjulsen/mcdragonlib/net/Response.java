package de.mrjulsen.mcdragonlib.net;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The outcome of an exchange: either a value or a {@link NetworkError}, never both and never
 * neither.
 *
 * <p>This keeps failures out of the payload type, so a handler cannot mistake an error for a
 * result by forgetting to check a status field.
 *
 * @param <T> the type of a successful result
 */
public final class Response<T> {

    private final T value;
    private final NetworkError error;

    private Response(T value, NetworkError error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Creates a successful outcome.
     *
     * @param value the result, which may be {@code null} if the packet allows it
     * @param <T> the result type
     * @return a successful response
     */
    public static <T> Response<T> success(T value) {
        return new Response<>(value, null);
    }

    /**
     * Creates a failed outcome.
     *
     * @param error the reason the exchange failed
     * @param <T> the result type that was expected
     * @return a failed response
     */
    public static <T> Response<T> failure(NetworkError error) {
        return new Response<>(null, Objects.requireNonNull(error, "error"));
    }

    /**
     * Creates a failed outcome from a reason alone.
     *
     * @param reason the category of the failure
     * @param <T> the result type that was expected
     * @return a failed response
     */
    public static <T> Response<T> failure(NetworkError.Reason reason) {
        return failure(NetworkError.of(reason));
    }

    /**
     * Returns whether the exchange produced a value.
     *
     * @return {@code true} if this is a success
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Returns whether the exchange failed.
     *
     * @return {@code true} if this is a failure
     */
    public boolean isFailure() {
        return error != null;
    }

    /**
     * Returns the result.
     *
     * @return the successful result
     * @throws NoSuchElementException if this is a failure
     */
    public T get() {
        if (error != null) {
            throw new NoSuchElementException("Response failed: " + error);
        }
        return value;
    }

    /**
     * Returns the result or a fallback if the exchange failed.
     *
     * @param fallback the value to use on failure
     * @return the result or the fallback
     */
    public T orElse(T fallback) {
        return error == null ? value : fallback;
    }

    /**
     * Returns the result if present.
     *
     * @return the result, or an empty optional on failure or for a {@code null} result
     */
    public Optional<T> value() {
        return error == null ? Optional.ofNullable(value) : Optional.empty();
    }

    /**
     * Returns the failure reason if the exchange failed.
     *
     * @return the error, or an empty optional on success
     */
    public Optional<NetworkError> error() {
        return Optional.ofNullable(error);
    }

    /**
     * Runs one of two actions depending on the outcome.
     *
     * @param onSuccess called with the result if the exchange succeeded
     * @param onError called with the error if the exchange failed
     */
    public void accept(Consumer<T> onSuccess, Consumer<NetworkError> onError) {
        if (error == null) {
            onSuccess.accept(value);
        } else {
            onError.accept(error);
        }
    }

    /**
     * Converts a successful result, leaving a failure untouched.
     *
     * @param mapper converts the result
     * @param <R> the converted result type
     * @return a response holding the converted value or the original error
     */
    public <R> Response<R> map(Function<T, R> mapper) {
        return error == null ? success(mapper.apply(value)) : failure(error);
    }

    @Override
    public String toString() {
        return error == null ? "Response[" + value + "]" : "Response[failed: " + error + "]";
    }
}
