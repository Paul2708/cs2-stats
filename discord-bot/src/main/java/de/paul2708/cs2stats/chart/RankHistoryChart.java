package de.paul2708.cs2stats.chart;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.Styler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public final class RankHistoryChart {

    private static final Logger logger = LoggerFactory.getLogger(RankHistoryChart.class);

    private RankHistoryChart() {

    }

    public static byte[] plot(Collection<RankHistory> rankHistories) {
        // Style chart
        XYChart chart = new XYChart(600, 400, Styler.ChartTheme.GGPlot2);

        chart.getStyler().setDatePattern("dd-MMM");
        chart.getStyler().setLocale(Locale.GERMAN);

        // Generate data
        for (RankHistory history : rankHistories) {
            if (history.ranks().isEmpty()) {
                continue;
            }

            List<Date> xData = history.ranks().keySet().stream()
                    .sorted()
                    .toList();
            List<Number> yData = new ArrayList<>();
            for (Date date : xData) {
                yData.add(history.ranks().get(date));
            }

            chart.addSeries(history.name(), xData, yData);
        }

        // Get image bytes
        try {
            return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            logger.error("Failed to generate plot", e);
            throw new RuntimeException(e);
        }
    }
}
