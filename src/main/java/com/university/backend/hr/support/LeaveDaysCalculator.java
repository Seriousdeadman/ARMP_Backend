package com.university.backend.hr.support;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class LeaveDaysCalculator {

    private LeaveDaysCalculator() {
    }

    /**
     * Inclusive calendar days: same start and end = 1 day.
     */
    public static int inclusiveCalendarDays(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
