import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Queue;

public class LineChart {
    public DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    JFreeChart createChartFloat(String chartTitle, String xLabel, String yLabel, String rowKey, Queue<Float> data){
        //Pupulate the dataset with the data provided
        int key = 1;
        for (float d : data){
            dataset.addValue(d,rowKey, "T_"+key);
            key++;
        }

        //Create the chart
        return ChartFactory.createLineChart(
                chartTitle,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, //Include legend
                true, //Include tooltips
                false //Include URLs
        );
    }

    JFreeChart createChartInt(String chartTitle, String xLabel, String yLabel, String rowKey, Queue<Integer> data){
        //Pupulate the dataset with the data provided
        int key = 1;
        for (int d : data){
            dataset.addValue(d,rowKey, "T_"+key);
            key++;
        }

        //Create the chart
        return ChartFactory.createLineChart(
                chartTitle,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, //Include legend
                true, //Include tooltips
                false //Include URLs
        );
    }
}
