package com.alibaba.qa.diffcoverage.utility;

import lombok.Getter;
import lombok.Setter;

import com.googlecode.charts4j.BarChart;
import com.googlecode.charts4j.BarChartPlot;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Plots;

public class PercentChart {
	@Getter @Setter
	private Double maxProgress = 100.0;
	@Getter @Setter
	private Double highPercent = 20.0;
	@Getter @Setter
	private Double mediumPercent = 50.0;
	@Getter @Setter
	private Color backgroundColor = Color.BLUEVIOLET;

	public String getChartUrl(Double now) {
		Double delta = maxProgress.doubleValue() - now.doubleValue();
		Color color = null;
		if (delta.doubleValue() <= highPercent.doubleValue())
			color = Color.GREEN;
		else if (delta.doubleValue() <= mediumPercent.doubleValue())
			color = Color.YELLOW;
		else
			color = Color.RED;
		BarChartPlot currentCoverage = Plots.newBarChartPlot(
			Data.newData(now.doubleValue()), color);
		BarChartPlot freeCoverage = Plots.newBarChartPlot(
			Data.newData(delta.doubleValue()), backgroundColor);
		BarChart barChart = GCharts.newBarChart(currentCoverage, freeCoverage);
		barChart.setSize(200, 30);
		barChart.setTransparency(100);
		barChart.setDataStacked(true);
		barChart.setHorizontal(true);
		return barChart.toURLString();
	}
}
