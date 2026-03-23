import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Queue;

public class LineChart {
    public DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    public JFreeChart createChartFloat(String chartTitle, String xLabel, String yLabel, String rowName, ArrayList<Float> values, ArrayList<String> timestamps) {
        dataset.clear();

        for (int i = 0; i < values.size(); i++) {
            float val = values.get(i);
            String time = timestamps.get(i);
            dataset.addValue(val, rowName, time);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                chartTitle,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        //autoscale settings
        CategoryPlot plot = chart.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeIncludesZero(false);

        return chart;
    }

    JFreeChart createSimpleChartFloat(String chartTitle, String xLabel, String yLabel, String rowKey, Queue<Float> data){
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

    JFreeChart createSimpleChartFloat(String chartTitle, String xLabel, String yLabel, String rowKey, ArrayList<Float> data){
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

    JFreeChart createSimpleChartInt(String chartTitle, String xLabel, String yLabel, String rowKey, Queue<Integer> data){
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

    JFreeChart createSimpleChartInt(String chartTitle, String xLabel, String yLabel, String rowKey, ArrayList<Integer> data){
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
