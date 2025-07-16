package de.paul2708.cs2stats.chart;

import java.util.Date;
import java.util.Map;

public record RankHistory(String name, Map<Date, Integer> ranks) {

}