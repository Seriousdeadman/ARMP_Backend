package com.university.backend.hr.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveDaysCalculatorTest {

    @Test
    void inclusiveCalendarDays_sameDay_isOne() {
        LocalDate d = LocalDate.of(2025, 6, 10);
        assertThat(LeaveDaysCalculator.inclusiveCalendarDays(d, d)).isEqualTo(1);
    }

    @Test
    void inclusiveCalendarDays_weekSpan() {
        LocalDate start = LocalDate.of(2025, 6, 9);
        LocalDate end = LocalDate.of(2025, 6, 13);
        assertThat(LeaveDaysCalculator.inclusiveCalendarDays(start, end)).isEqualTo(5);
    }

    @Test
    void inclusiveCalendarDays_invalidRange_isZero() {
        LocalDate start = LocalDate.of(2025, 6, 10);
        LocalDate end = LocalDate.of(2025, 6, 9);
        assertThat(LeaveDaysCalculator.inclusiveCalendarDays(start, end)).isEqualTo(0);
    }
}
