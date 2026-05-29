package audiometer;

import audiometer.AudiometerModel.*;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Pure functional processing of serial RESPONSE messages from the hardware.
 *
 * Responsibilities:
 *  - Parse raw serial strings into typed ResponseStatus values (Maybe/Optional pattern)
 *  - Filter and transform trial lists using map / filter / reduce chains
 *  - Build the final Audiogram from completed test states
 *
 * No side effects. No exceptions thrown for bad input — Optional is used instead.
 */
public final class ResponseProcessor {

    private ResponseProcessor() {}

    // ── Parsing (Optional / Maybe pattern) ────────────────────────────────

    /**
     * Parse a raw serial message string into a ResponseStatus.
     *
     * Uses the Optional / Maybe pattern:
     *   - Valid message  → Optional.of(status)
     *   - Null / unknown → Optional.empty()
     *
     * No exception is ever thrown; invalid input is silently discarded downstream.
     *
     * @param rawMessage  raw string received from serial port (e.g. "RESPONSE")
     * @return            parsed status, or empty if the message was unrecognised
     */
    public static Optional<ResponseStatus> parseMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) return Optional.empty();
        return switch (rawMessage.trim().toUpperCase()) {
            case "RESPONSE"    -> Optional.of(ResponseStatus.HEARD);
            case "NO_RESPONSE",
                 "TIMEOUT"     -> Optional.of(ResponseStatus.NOT_HEARD);
            default            -> Optional.empty(); // malformed / noise — discard
        };
    }

    // ── Bulk processing (map → filter → collect) ──────────────────────────

    /**
     * Convert a list of raw serial strings into valid ResponseStatus values.
     *
     * Pipeline: map(parse) → filter(present) → map(unwrap) → collect
     * Malformed messages are silently dropped — no exceptions, no nulls.
     *
     * @param rawMessages  raw strings from the serial port
     * @return             ordered list of valid responses
     */
    public static List<ResponseStatus> processMessages(List<String> rawMessages) {
        return rawMessages.stream()
            .map((String s) -> ResponseProcessor.parseMessage(s))   // String → Optional<ResponseStatus>
            .filter(Optional::isPresent)             // drop empties (malformed input)
            .map(Optional::get)                      // unwrap safely
            .collect(Collectors.toUnmodifiableList());
    }

    // ── Trial analytics ────────────────────────────────────────────────────

    /**
     * Count how many HEARD responses are in a list of trials.
     * Uses a reduce-style terminal operation (count).
     */
    public static long countHeardResponses(List<Trial> trials) {
        return trials.stream()
            .filter(t -> t.response() == ResponseStatus.HEARD)
            .count();
    }

    /**
     * Filter trials to only those matching a specific frequency and ear.
     */
    public static List<Trial> filterTrials(List<Trial> trials, int frequencyHz, Ear ear) {
        return trials.stream()
            .filter(t -> t.frequencyHz() == frequencyHz && t.ear() == ear)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Compute the average intensity of all HEARD trials.
     * Returns Optional.empty() if there are no HEARD trials.
     */
    public static Optional<Double> averageHeardIntensity(List<Trial> trials) {
        OptionalDouble avg = trials.stream()
            .filter(t -> t.response() == ResponseStatus.HEARD)
            .mapToInt(Trial::intensityDb)
            .average();
        return avg.isPresent() ? Optional.of(avg.getAsDouble()) : Optional.empty();
    }

    /**
     * Compute the minimum (best) intensity among all HEARD trials.
     * Returns Optional.empty() if none heard.
     */
    public static Optional<Integer> minimumHeardIntensity(List<Trial> trials) {
        return trials.stream()
            .filter(t -> t.response() == ResponseStatus.HEARD)
            .map(Trial::intensityDb)
            .reduce(Integer::min);
    }

    // ── Audiogram construction ─────────────────────────────────────────────

    /**
     * Build a complete Audiogram from a list of completed TestStates.
     *
     * Uses map + filter to split states by ear and project to ThresholdResult.
     *
     * @param completedStates  list of finished test states (one per frequency/ear)
     * @return                 immutable Audiogram record
     */
    public static Audiogram buildAudiogram(List<TestState> completedStates) {
        List<ThresholdResult> right = completedStates.stream()
            .filter(s -> s.ear() == Ear.RIGHT)
            .map(s -> new ThresholdResult(s.frequencyHz(), s.ear(), s.confirmedThreshold()))
            .collect(Collectors.toUnmodifiableList());

        List<ThresholdResult> left = completedStates.stream()
            .filter(s -> s.ear() == Ear.LEFT)
            .map(s -> new ThresholdResult(s.frequencyHz(), s.ear(), s.confirmedThreshold()))
            .collect(Collectors.toUnmodifiableList());

        return new Audiogram(right, left);
    }
}
