package ru.nextupvamp.util;

import org.junit.jupiter.api.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsCalculatorTest {

    @Test
    public void testCompute95p() {
        Queue<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().collect(Collectors.toCollection(ConcurrentLinkedQueue<Long>::new));
        long p95 = StatisticsCalculator.calculate95Percentile(list);

        assertEquals(96, p95);
    }

    @Test
    public void testCompute95pOnEmptyList() {
        Queue<Long> list = new ConcurrentLinkedQueue<>();
        long p95 = StatisticsCalculator.calculate95Percentile(list);
        assertEquals(0, p95);
        assertThat(list).isEmpty();
    }

    @Test
    public void testComputeAverage() {
        Queue<Long> list = LongStream.iterate(1, it -> it + 1).limit(100).boxed().collect(Collectors.toCollection(ConcurrentLinkedQueue<Long>::new));

        assertEquals(50, StatisticsCalculator.calculateAverage(list));
    }

    @Test
    public void testComputeAverageOnEmptyList() {
        Queue<Long> list = new ConcurrentLinkedQueue<>();

        assertEquals(0, StatisticsCalculator.calculateAverage(list));
        assertThat(list).isEmpty();
    }
}
