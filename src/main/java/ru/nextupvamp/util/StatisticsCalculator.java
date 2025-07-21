package ru.nextupvamp.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@UtilityClass
public class StatisticsCalculator {

    private static final double THE_95_TH_PERCENTILE = 0.95;


    public static double calculateAverage(Queue<Long> values) {
        double average = 0;

        if (!values.isEmpty()) {
            average = (double) values.stream().reduce(0L, Long::sum) / values.size();
        }

        return average;
    }

    public static long calculate95Percentile(Queue<Long> values) {
        long count = 0;

        if (!values.isEmpty()) {

            List<Long> converted = new ArrayList<>(values);
            converted.sort(Long::compareTo);

            count = converted.get((int) (values.size() * THE_95_TH_PERCENTILE));
        }

        return count;
    }
}
