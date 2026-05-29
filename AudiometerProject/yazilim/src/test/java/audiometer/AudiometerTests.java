package audiometer;

import audiometer.AudiometerModel.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static audiometer.AudiometerModel.*;
import static org.junit.jupiter.api.Assertions.*;

class AudiometerTests {

    @Nested
    @DisplayName("HughsonWestlake: intensity stepping")
    class IntensitySteppingTests {

        @Test
        @DisplayName("HEARD response decreases intensity by 10 dB ('down 10')")
        void heard_decreases10dB() {
            assertEquals(30, HughsonWestlake.nextIntensity(40, ResponseStatus.HEARD));
        }

        @Test
        @DisplayName("NOT_HEARD response increases intensity by 5 dB ('up 5')")
        void notHeard_increases5dB() {
            assertEquals(45, HughsonWestlake.nextIntensity(40, ResponseStatus.NOT_HEARD));
        }

        @ParameterizedTest(name = "nextIntensity({0}, HEARD) == {1}")
        @CsvSource({"0,-10", "10,0", "40,30", "120,110"})
        @DisplayName("'down 10' is consistent across levels")
        void heard_alwaysDecreasesBy10(int input, int expected) {
            assertEquals(expected, HughsonWestlake.nextIntensity(input, ResponseStatus.HEARD));
        }

        @ParameterizedTest(name = "nextIntensity({0}, NOT_HEARD) == {1}")
        @CsvSource({"0,5", "10,15", "40,45", "115,120"})
        @DisplayName("'up 5' is consistent across levels")
        void notHeard_alwaysIncreasesBy5(int input, int expected) {
            assertEquals(expected, HughsonWestlake.nextIntensity(input, ResponseStatus.NOT_HEARD));
        }
    }

    @Nested
    @DisplayName("HughsonWestlake: intensity clamping")
    class ClampingTests {

        @Test
        @DisplayName("Values above max are clamped to MAX_INTENSITY_DB")
        void clamp_aboveMax() {
            assertEquals(MAX_INTENSITY_DB, HughsonWestlake.clampIntensity(200));
            assertEquals(MAX_INTENSITY_DB, HughsonWestlake.clampIntensity(MAX_INTENSITY_DB + 1));
        }

        @Test
        @DisplayName("Values below min are clamped to MIN_INTENSITY_DB")
        void clamp_belowMin() {
            assertEquals(MIN_INTENSITY_DB, HughsonWestlake.clampIntensity(-100));
            assertEquals(MIN_INTENSITY_DB, HughsonWestlake.clampIntensity(MIN_INTENSITY_DB - 1));
        }

        @Test
        @DisplayName("Values within range are unchanged")
        void clamp_withinRange() {
            assertEquals(0,  HughsonWestlake.clampIntensity(0));
            assertEquals(40, HughsonWestlake.clampIntensity(40));
            assertEquals(MAX_INTENSITY_DB, HughsonWestlake.clampIntensity(MAX_INTENSITY_DB));
        }

        /** Property: clamped output always lies within [MIN, MAX] for any integer input. */
        @Test
        @DisplayName("Property: output is always in valid range for any input")
        void property_clampAlwaysInRange() {
            for (int i = -500; i <= 500; i++) {
                int clamped = HughsonWestlake.clampIntensity(i);
                assertTrue(
                    clamped >= MIN_INTENSITY_DB && clamped <= MAX_INTENSITY_DB,
                    "Clamped value " + clamped + " out of range for input " + i
                );
            }
        }
    }

    @Nested
    @DisplayName("HughsonWestlake: threshold determination")
    class ThresholdTests {

        @Test
        @DisplayName("No trials → no threshold")
        void emptyTrials_returnsEmpty() {
            assertTrue(HughsonWestlake.determineThreshold(List.of()).isEmpty());
        }

        @Test
        @DisplayName("Only one HEARD at a level → no threshold yet")
        void oneHeared_notEnough() {
            List<Trial> trials = List.of(
                new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD)
            );
            assertTrue(HughsonWestlake.determineThreshold(trials).isEmpty());
        }

        @Test
        @DisplayName("Two HEARD at same level → threshold confirmed")
        void twoHeardAtSameLevel_confirmsThreshold() {
            List<Trial> trials = List.of(
                new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD),
                new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD)
            );
            assertEquals(Optional.of(30), HughsonWestlake.determineThreshold(trials));
        }

        @Test
        @DisplayName("Threshold picks the LOWEST qualifying level")
        void lowestQualifyingLevel_isChosen() {
            List<Trial> trials = List.of(
                new Trial(1000, 25, Ear.RIGHT, ResponseStatus.HEARD),
                new Trial(1000, 25, Ear.RIGHT, ResponseStatus.HEARD),
                new Trial(1000, 35, Ear.RIGHT, ResponseStatus.HEARD),
                new Trial(1000, 35, Ear.RIGHT, ResponseStatus.HEARD)
            );
            assertEquals(Optional.of(25), HughsonWestlake.determineThreshold(trials));
        }

        @Test
        @DisplayName("NOT_HEARD responses are ignored in threshold calculation")
        void notHeard_ignored() {
            List<Trial> trials = List.of(
                new Trial(1000, 20, Ear.RIGHT, ResponseStatus.NOT_HEARD),
                new Trial(1000, 20, Ear.RIGHT, ResponseStatus.NOT_HEARD),
                new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD),
                new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD)
            );
            assertEquals(Optional.of(30), HughsonWestlake.determineThreshold(trials));
        }
    }

    @Nested
    @DisplayName("HughsonWestlake: immutable state transitions")
    class StateTransitionTests {

        @Test
        @DisplayName("initialState starts at INITIAL_INTENSITY_DB with no trials")
        void initialState_isCorrect() {
            TestState s = HughsonWestlake.initialState(1000, Ear.RIGHT);
            assertEquals(1000, s.frequencyHz());
            assertEquals(Ear.RIGHT, s.ear());
            assertEquals(INITIAL_INTENSITY_DB, s.currentIntensityDb());
            assertTrue(s.trials().isEmpty());
            assertTrue(s.confirmedThreshold().isEmpty());
        }

        @Test
        @DisplayName("applyResponse does NOT mutate original state (immutability)")
        void applyResponse_isImmutable() {
            TestState original = HughsonWestlake.initialState(1000, Ear.RIGHT);
            HughsonWestlake.applyResponse(original, ResponseStatus.HEARD);
            // Original must be unchanged
            assertEquals(0, original.trials().size());
            assertEquals(INITIAL_INTENSITY_DB, original.currentIntensityDb());
        }

        @Test
        @DisplayName("applyResponse adds exactly one trial per call")
        void applyResponse_addsOneTrial() {
            TestState state = HughsonWestlake.initialState(1000, Ear.RIGHT);
            state = HughsonWestlake.applyResponse(state, ResponseStatus.NOT_HEARD);
            assertEquals(1, state.trials().size());
            state = HughsonWestlake.applyResponse(state, ResponseStatus.HEARD);
            assertEquals(2, state.trials().size());
        }

        @Test
        @DisplayName("Intensity decreases by 10 after HEARD")
        void intensity_decreasesAfterHeard() {
            TestState s = HughsonWestlake.initialState(1000, Ear.RIGHT); // 40 dB
            TestState next = HughsonWestlake.applyResponse(s, ResponseStatus.HEARD);
            assertEquals(30, next.currentIntensityDb());
        }

        @Test
        @DisplayName("Intensity increases by 5 after NOT_HEARD")
        void intensity_increasesAfterNotHeard() {
            TestState s = HughsonWestlake.initialState(1000, Ear.RIGHT); // 40 dB
            TestState next = HughsonWestlake.applyResponse(s, ResponseStatus.NOT_HEARD);
            assertEquals(45, next.currentIntensityDb());
        }

        @Test
        @DisplayName("isTestComplete returns false before threshold, true after")
        void isTestComplete_behavior() {
            TestState s = HughsonWestlake.initialState(1000, Ear.RIGHT);
            assertFalse(HughsonWestlake.isTestComplete(s));

            // Simulate getting 2 HEARD responses at the same level
            s = new TestState(1000, Ear.RIGHT, 30,
                List.of(
                    new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD),
                    new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD)
                ),
                Optional.of(30)
            );
            assertTrue(HughsonWestlake.isTestComplete(s));
        }

        @Test
        @DisplayName("Property: trial count grows by exactly 1 per response")
        void property_trialCountMonotonicallyIncreases() {
            TestState state = HughsonWestlake.initialState(1000, Ear.RIGHT);
            List<ResponseStatus> sequence = List.of(
                ResponseStatus.NOT_HEARD, ResponseStatus.HEARD,
                ResponseStatus.NOT_HEARD, ResponseStatus.HEARD,
                ResponseStatus.NOT_HEARD
            );
            int expected = 0;
            for (ResponseStatus r : sequence) {
                state = HughsonWestlake.applyResponse(state, r);
                expected++;
                assertEquals(expected, state.trials().size());
            }
        }
    }

    @Nested
    @DisplayName("IEC 60645-1: standard frequencies")
    class FrequencyTests {

        @ParameterizedTest(name = "{0} Hz is a standard audiometric frequency")
        @ValueSource(ints = {250, 500, 1000, 2000, 3000, 4000, 6000, 8000})
        void standardFrequencies_allPresent(int freq) {
            assertTrue(STANDARD_FREQUENCIES.contains(freq));
        }

        @Test
        @DisplayName("Exactly 8 standard test frequencies are defined")
        void exactlyEightFrequencies() {
            assertEquals(8, STANDARD_FREQUENCIES.size());
        }
    }

    @Nested
    @DisplayName("ResponseProcessor: message parsing")
    class ParsingTests {

        @Test
        @DisplayName("'RESPONSE' parses to HEARD")
        void response_parsesToHeard() {
            assertEquals(Optional.of(ResponseStatus.HEARD),
                ResponseProcessor.parseMessage("RESPONSE"));
        }

        @Test
        @DisplayName("'TIMEOUT' parses to NOT_HEARD")
        void timeout_parsesToNotHeard() {
            assertEquals(Optional.of(ResponseStatus.NOT_HEARD),
                ResponseProcessor.parseMessage("TIMEOUT"));
        }

        @Test
        @DisplayName("'NO_RESPONSE' parses to NOT_HEARD")
        void noResponse_parsesToNotHeard() {
            assertEquals(Optional.of(ResponseStatus.NOT_HEARD),
                ResponseProcessor.parseMessage("NO_RESPONSE"));
        }

        @Test
        @DisplayName("null / blank / garbage → Optional.empty() (no exception)")
        void invalid_returnsEmpty() {
            assertTrue(ResponseProcessor.parseMessage(null).isEmpty());
            assertTrue(ResponseProcessor.parseMessage("").isEmpty());
            assertTrue(ResponseProcessor.parseMessage("   ").isEmpty());
            assertTrue(ResponseProcessor.parseMessage("GARBAGE_DATA").isEmpty());
            assertTrue(ResponseProcessor.parseMessage("123").isEmpty());
        }

        @Test
        @DisplayName("Parsing is case-insensitive")
        void parsing_isCaseInsensitive() {
            assertEquals(Optional.of(ResponseStatus.HEARD),
                ResponseProcessor.parseMessage("response"));
            assertEquals(Optional.of(ResponseStatus.HEARD),
                ResponseProcessor.parseMessage("Response"));
        }
    }


    @Nested
    @DisplayName("ResponseProcessor: bulk message processing")
    class BulkProcessingTests {

        @Test
        @DisplayName("Invalid messages are filtered out silently")
        void processMessages_filtersInvalid() {
            List<String> raw = List.of("RESPONSE", "BAD", "TIMEOUT", "???", "RESPONSE");
            List<ResponseStatus> result = ResponseProcessor.processMessages(raw);
            assertEquals(3, result.size());
            assertEquals(ResponseStatus.HEARD,     result.get(0));
            assertEquals(ResponseStatus.NOT_HEARD, result.get(1));
            assertEquals(ResponseStatus.HEARD,     result.get(2));
        }

        @Test
        @DisplayName("Empty input produces empty output")
        void processMessages_emptyInput() {
            assertTrue(ResponseProcessor.processMessages(List.of()).isEmpty());
        }

        @Test
        @DisplayName("All-invalid input produces empty output")
        void processMessages_allInvalid() {
            List<String> raw = List.of("NOISE", "JUNK", "", "XYZ");
            assertTrue(ResponseProcessor.processMessages(raw).isEmpty());
        }
    }


    @Nested
    @DisplayName("ResponseProcessor: trial analytics")
    class AnalyticsTests {

        private final List<Trial> sampleTrials = List.of(
            new Trial(1000, 30, Ear.RIGHT, ResponseStatus.HEARD),
            new Trial(1000, 40, Ear.RIGHT, ResponseStatus.NOT_HEARD),
            new Trial(2000, 35, Ear.LEFT,  ResponseStatus.HEARD),
            new Trial(1000, 25, Ear.RIGHT, ResponseStatus.HEARD)
        );

        @Test
        @DisplayName("countHeardResponses counts only HEARD trials")
        void countHeard_isCorrect() {
            assertEquals(3, ResponseProcessor.countHeardResponses(sampleTrials));
        }

        @Test
        @DisplayName("filterTrials selects by frequency AND ear")
        void filterTrials_byFreqAndEar() {
            List<Trial> filtered = ResponseProcessor.filterTrials(sampleTrials, 1000, Ear.RIGHT);
            assertEquals(3, filtered.size());
            assertTrue(filtered.stream().allMatch(
                t -> t.frequencyHz() == 1000 && t.ear() == Ear.RIGHT));
        }

        @Test
        @DisplayName("averageHeardIntensity computes correctly")
        void averageHeardIntensity_isCorrect() {
            // HEARD at 30, 35, 25 → average = 30.0
            Optional<Double> avg = ResponseProcessor.averageHeardIntensity(sampleTrials);
            assertTrue(avg.isPresent());
            assertEquals(30.0, avg.get(), 0.001);
        }

        @Test
        @DisplayName("averageHeardIntensity returns empty when no HEARD trials")
        void averageHeardIntensity_emptyWhenNone() {
            List<Trial> noHeard = List.of(
                new Trial(1000, 40, Ear.RIGHT, ResponseStatus.NOT_HEARD)
            );
            assertTrue(ResponseProcessor.averageHeardIntensity(noHeard).isEmpty());
        }

        @Test
        @DisplayName("minimumHeardIntensity returns lowest HEARD level")
        void minimumHeard_isCorrect() {
            // HEARD at 30, 35, 25 → minimum = 25
            assertEquals(Optional.of(25),
                ResponseProcessor.minimumHeardIntensity(sampleTrials));
        }
    }


    @Nested
    @DisplayName("ResponseProcessor: audiogram construction")
    class AudiogramTests {

        @Test
        @DisplayName("buildAudiogram correctly separates ears")
        void buildAudiogram_separatesEars() {
            TestState right = new TestState(1000, Ear.RIGHT, 30, List.of(), Optional.of(30));
            TestState left  = new TestState(1000, Ear.LEFT,  40, List.of(), Optional.of(40));

            Audiogram gram = ResponseProcessor.buildAudiogram(List.of(right, left));

            assertEquals(1, gram.rightEar().size());
            assertEquals(1, gram.leftEar().size());
            assertEquals(Optional.of(30), gram.rightEar().get(0).thresholdDb());
            assertEquals(Optional.of(40), gram.leftEar().get(0).thresholdDb());
        }

        @Test
        @DisplayName("buildAudiogram from empty states gives empty audiogram")
        void buildAudiogram_empty() {
            Audiogram gram = ResponseProcessor.buildAudiogram(List.of());
            assertTrue(gram.rightEar().isEmpty());
            assertTrue(gram.leftEar().isEmpty());
        }

        @Test
        @DisplayName("Audiogram is immutable — modification attempt throws")
        void audiogram_isImmutable() {
            Audiogram gram = ResponseProcessor.buildAudiogram(List.of());
            assertThrows(UnsupportedOperationException.class, () ->
                gram.rightEar().add(new ThresholdResult(1000, Ear.RIGHT, Optional.of(30))));
        }
    }
}
