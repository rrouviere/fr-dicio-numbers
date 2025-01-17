package org.dicio.numbers.lang.en;

import static org.dicio.numbers.test.TestUtils.DAY;
import static org.dicio.numbers.test.TestUtils.F;
import static org.dicio.numbers.test.TestUtils.HOUR;
import static org.dicio.numbers.test.TestUtils.MICROS;
import static org.dicio.numbers.test.TestUtils.MILLIS;
import static org.dicio.numbers.test.TestUtils.MINUTE;
import static org.dicio.numbers.test.TestUtils.MONTH;
import static org.dicio.numbers.test.TestUtils.T;
import static org.dicio.numbers.test.TestUtils.WEEK;
import static org.dicio.numbers.test.TestUtils.YEAR;
import static org.dicio.numbers.test.TestUtils.t;
import static org.junit.Assert.assertTrue;

import org.dicio.numbers.NumberParserFormatter;
import org.dicio.numbers.parser.lexer.TokenStream;
import org.dicio.numbers.test.DurationExtractorUtilsTestBase;
import org.dicio.numbers.util.DurationExtractorUtils;
import org.junit.Test;

import java.time.Duration;

public class DurationExtractorUtilsTest extends DurationExtractorUtilsTestBase {
    @Override
    public String configFolder() {
        return "config/en-us";
    }

    @Override
    public Duration extractDuration(final TokenStream ts, final boolean shortScale) {
        final EnglishNumberExtractor numberExtractor
                = new EnglishNumberExtractor(ts, shortScale, false);
        return DurationExtractorUtils.extractDuration(ts,
                numberExtractor::extractOneNumberNoOrdinal);
    }

    @Test
    public void testDurationNumberAndUnit() {
        assertDuration("one billion nanoseconds",   F, t(1000));
        assertDuration("one billion nanoseconds",   T, t(1));
        assertDuration("seventeen twenty eight μs", F, t(0, 1728 * MICROS));
        assertDuration("one tenth millisecond",     T, t(0, 100 * MICROS));
        assertDuration("18s",                       F, t(18));
        assertDuration("give me one sec",           F, t(1));
        assertDuration("in 59 minute s",            T, t(59 * MINUTE));
        assertDuration("twenty three hours",        F, t(23 * HOUR));
        assertDuration("it's been half an hour",    T, t(HOUR / 2));
        assertDuration("one point 2 day",           T, t(1.2 * DAY));
        assertDuration("half a day",                F, t(DAY / 2));
        assertDuration("and ten and weeks and",     F, t(10 * WEEK));
        assertDuration("6 mo",                      T, t(6 * MONTH));
        assertDuration("three billion years ago",   T, t(3e9 * YEAR));
        assertDuration("fifteen decades",           T, t(150 * YEAR));
        assertDuration("one billionth century",     F, t(1e-12 * 100 * YEAR));
        assertDuration("one billionth century",     T, t(1e-9 * 100 * YEAR));
        assertDuration("1 millennium",              F, t(1000 * YEAR));
        assertDuration("four three millenia four",  T, t(3000 * YEAR));
        assertNoDuration("one hundred tests",      F);
        assertNoDuration("point three four grams", T);
    }

    @Test
    public void testDurationOnlyUnit() {
        assertDuration("hour minute millenium",                     T, t(1000 * YEAR + HOUR + MINUTE));
        assertDuration("hello millisecond and second, microsecond", F, t(1, MILLIS + MICROS));
        assertDuration("seconds second s",                          T, t(2));
        assertDuration("minute hours yr",                           F, t(MINUTE + HOUR));
        assertNoDuration("it's good",                T);
        assertNoDuration("ns μs ms s m h d w mo yr", F);
    }

    @Test
    public void testDurationOf() {
        assertDuration("two tenths of a second", F, t(0, 200 * MILLIS));
        assertDuration("a couple of hours",      F, t(2 * HOUR));
        assertDuration("plenty of seconds",      F, t(1));
        assertNoDuration("tens of lines of tests",     T);
        assertNoDuration("hello two hundred of hello", F);
        assertNoDuration("hello of s",                 F);
    }

    @Test
    public void testMultipleDurationGroups() {
        assertDuration("twenty minutes and thirty six and seconds because", T, t(20 * MINUTE + 36));
        assertDuration("it took us seven days, 21 hours and twelve minutes to reach you", F, t(7 * DAY + 21 * HOUR + 12 * MINUTE));
        assertDuration("testing minute, seconds and millisecond, microseconds nanosecond test", T, t(MINUTE + 1, MILLIS + MICROS + 1));
        assertDuration("ms 5 ns ns", F, t(0, 5));
    }

    @Test(timeout = 4000) // 1024 formats + parses take <2s, use 4s timeout just for slower PCs
    public void testPerformanceWithFormatter() {
        // TODO there are no fractions of second here since the formatter does not support them
        final Duration[] alternatives = {
                t(1), t(5 * MINUTE), t(2 * HOUR), t(16 * DAY), t(WEEK), t(3 * MONTH), t(5 * YEAR),
                t(1e9 * YEAR), t(17 * WEEK), t(45)
        };

        final NumberParserFormatter npf = new NumberParserFormatter(new EnglishFormatter(), null);
        for (int i = 0; i < (1 << alternatives.length); ++i) {
            Duration durationToTest = Duration.ZERO;
            for (int j = 0; j < alternatives.length; ++j) {
                if ((i & (1 << j)) != 0) {
                    durationToTest = durationToTest.plus(alternatives[j]);
                }
            }

            // the formatter only supports short scale (TODO maybe allow customizing?)
            final String formatted = npf.niceDuration(durationToTest).get();
            final TokenStream ts = new TokenStream(tokenizer.tokenize(formatted));
            assertDuration(formatted, ts, T, durationToTest);
            assertTrue(ts.finished());
        }
    }
}
