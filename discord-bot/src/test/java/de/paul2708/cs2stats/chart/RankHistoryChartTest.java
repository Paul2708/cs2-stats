package de.paul2708.cs2stats.chart;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RankHistoryChartTest {

    @Test
    void generateSimplePlot() {
        byte[] data = RankHistoryChart.plot(List.of(
                new RankHistory("Player 1", Map.of(at(1, 1, 10), 1000))
        ));

        saveToFile(data, "01-simple.png");
    }

    @Test
    void generateEmptyPlot() {
        byte[] data = RankHistoryChart.plot(List.of(
                new RankHistory("Player 1", Map.of())
        ));

        saveToFile(data, "02-empty.png");
    }

    @Test
    void generateComplexScenario() {
        byte[] data = RankHistoryChart.plot(List.of(
                new RankHistory("Player 1", Map.of(
                        at(1, 1, 10), 1000,
                        at(1, 1, 11), 1025,
                        at(1, 2, 8), 900,
                        at(1, 2, 10), 1000,
                        at(2, 1, 10), 1150
                )),
                new RankHistory("Player 2", Map.of(
                        at(1, 1, 6), 800,
                        at(1, 1, 10), 850,
                        at(1, 2, 8), 900,
                        at(1, 3, 10), 950,
                        at(2, 5, 10), 850
                ))
        ));

        saveToFile(data, "03-complex.png");
    }

    @Test
    void generateMultipleDays() {
        Map<Date, Integer> ranks = new HashMap<>();

        for (int i = 1; i <= 31; i++) {
            ranks.put(at(8, i, 10), 1000 + ((i % 2 == 0 ? 1 : -1) * (i * 10)));
        }

        byte[] data = RankHistoryChart.plot(List.of(
                new RankHistory("Player 1", ranks)
        ));

        saveToFile(data, "04-one_month.png");
    }

    @Test
    void generateOneYearDays() {
        Map<Date, Integer> ranks = new HashMap<>();

        List<Integer> integers = generateRandomSequence(12 * 28);
        int i = 0;

        for (int month = 1; month <= 12; month++) {
            for (int day = 1; day <= 28; day++) {
                ranks.put(at(month, day, 10), integers.get(i));
                i++;
            }
        }


        byte[] data = RankHistoryChart.plot(List.of(
                new RankHistory("Player 1", ranks)
        ));

        saveToFile(data, "05-one_year.png");
    }

    private List<Integer> generateRandomSequence(int n) {
        int min = 1000;
        int max = 10000;
        int maxDiff = 500;

        Random rand = new Random();
        List<Integer> sequence = new ArrayList<>();

        int current = min + rand.nextInt(max - min + 1);
        sequence.add(current);

        for (int i = 1; i < n; i++) {
            int lower = Math.max(min, current - maxDiff);
            int upper = Math.min(max, current + maxDiff);

            current = lower + rand.nextInt(upper - lower + 1);
            sequence.add(current);
        }

        return sequence;
    }

    private Date at(int month, int day, int hours) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hours);

        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private void saveToFile(byte[] imageData, String fileName) {
        Path path = Paths.get("./build/test/%s".formatted(fileName));

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
