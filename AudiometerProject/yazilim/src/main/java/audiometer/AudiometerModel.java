package audiometer;

import java.util.List;
import java.util.Optional;


public final class AudiometerModel {

    private AudiometerModel() {}

    // ── Enums ──────────────────────────────────────────────────────────────

    public enum Ear { LEFT, RIGHT }

    public enum ResponseStatus { HEARD, NOT_HEARD }


    public record Trial(
        int frequencyHz,
        int intensityDb,
        Ear ear,
        ResponseStatus response
    ) {}

    public record ThresholdResult(
        int frequencyHz,
        Ear ear,
        Optional<Integer> thresholdDb
    ) {}

    public record Audiogram(
        List<ThresholdResult> rightEar,
        List<ThresholdResult> leftEar
    ) {
        public Audiogram {
            rightEar = List.copyOf(rightEar);
            leftEar  = List.copyOf(leftEar);
        }
    }

    public record TestState(
        int frequencyHz,
        Ear ear,
        int currentIntensityDb,
        List<Trial> trials,
        Optional<Integer> confirmedThreshold
    ) {
        public TestState {
            trials = List.copyOf(trials); // defensive copy
        }
    }

    public static final List<Integer> STANDARD_FREQUENCIES =
        List.of(250, 500, 1000, 2000, 3000, 4000, 6000, 8000);

    public static final int MIN_INTENSITY_DB  = -10;
    public static final int MAX_INTENSITY_DB  = 120;
    public static final int INITIAL_INTENSITY_DB = 40;
}
