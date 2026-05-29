package audiometer;

import audiometer.AudiometerModel.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static audiometer.AudiometerModel.*;

/**
 * Pure functions implementing the Hughson–Westlake audiometric procedure.
 *
 * Rules (IEC 60645-1 compliant):
 *   - If the patient hears a tone  → decrease intensity by 10 dB  ("down 10")
 *   - If the patient misses a tone → increase intensity by  5 dB  ("up 5")
 *   - Threshold = lowest dB level at which ≥2 HEARD responses occur
 *     on ascending presentations.
 *
 * All methods are static and side-effect-free.
 * No I/O, no shared mutable state, no exceptions for control flow.
 */
public final class HughsonWestlake {

    private HughsonWestlake() {}

    // ── Step logic ─────────────────────────────────────────────────────────

    /**
     * Pure function: compute the next intensity level given the current
     * level and the patient's response.
     *
     * @param currentDb  current intensity in dB
     * @param response   patient's response
     * @return           next intensity before clamping
     */
    public static int nextIntensity(int currentDb, ResponseStatus response) {
        return switch (response) {
            case HEARD     -> currentDb - 10;   // "down 10"
            case NOT_HEARD -> currentDb + 5;    // "up 5"
        };
    }

    /**
     * Pure function: clamp an intensity value to the valid clinical range.
     *
     * @param db  unclamped dB value
     * @return    value guaranteed to be within [MIN_INTENSITY_DB, MAX_INTENSITY_DB]
     */
    public static int clampIntensity(int db) {
        return Math.max(MIN_INTENSITY_DB, Math.min(MAX_INTENSITY_DB, db));
    }

    // ── Threshold determination ────────────────────────────────────────────

    /**
     * Pure function: determine the hearing threshold from a list of trials.
     *
     * Returns the lowest intensity at which the patient gave ≥2 HEARD responses.
     * Returns Optional.empty() if no threshold has been established yet.
     *
     * @param trials  list of completed trials (may be empty)
     * @return        threshold in dB, or empty if not yet determined
     */
    public static Optional<Integer> determineThreshold(List<Trial> trials) {
        return trials.stream()
            .filter(t -> t.response() == ResponseStatus.HEARD)
            .collect(Collectors.groupingBy(Trial::intensityDb, Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() >= 2)          // ≥2 responses at this level
            .mapToInt(java.util.Map.Entry::getKey)
            .min()                                   // lowest qualifying level
            .stream()
            .boxed()
            .findFirst();
    }

    // ── State transitions (immutable) ──────────────────────────────────────

    /**
     * Pure function: apply one patient response to the current test state,
     * returning a BRAND-NEW TestState. The original state is never modified.
     *
     * @param state     current test state
     * @param response  patient's response to the tone just played
     * @return          new test state after the response
     */
    public static TestState applyResponse(TestState state, ResponseStatus response) {
        // Record this trial
        Trial newTrial = new Trial(
            state.frequencyHz(),
            state.currentIntensityDb(),
            state.ear(),
            response
        );

        // Build new (immutable) trial list
        List<Trial> updatedTrials = Stream.concat(
            state.trials().stream(),
            Stream.of(newTrial)
        ).toList();

        // Recompute threshold from all trials so far
        Optional<Integer> threshold = determineThreshold(updatedTrials);

        // Compute next intensity (clamped)
        int nextDb = clampIntensity(nextIntensity(state.currentIntensityDb(), response));

        return new TestState(
            state.frequencyHz(),
            state.ear(),
            nextDb,
            updatedTrials,
            threshold
        );
    }

    /**
     * Pure function: create a fresh initial test state for a given
     * frequency and ear, starting at the standard initial intensity.
     */
    public static TestState initialState(int frequencyHz, Ear ear) {
        return new TestState(
            frequencyHz,
            ear,
            INITIAL_INTENSITY_DB,
            List.of(),
            Optional.empty()
        );
    }

    /**
     * Pure function: check whether the test for a given frequency is complete.
     * A test is complete once a threshold has been confirmed.
     */
    public static boolean isTestComplete(TestState state) {
        return state.confirmedThreshold().isPresent();
    }
}
