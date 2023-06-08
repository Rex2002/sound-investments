package audio.synth.devgraphing;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class FrequencyChart extends JFrame {

    double[] data;
    Complex[] complexData;
    final int graphDefinition;
    final String title;

    boolean useFFT = false;

    public FrequencyChart(byte[] data, int graphDefinition, String title){
        this.data = new double[data.length];
        for(int i = 0; i< data.length; i++){
            this.data[i] = (data[i] * 127);
        }
        this.title = title;
        this.graphDefinition = graphDefinition;
        initUI();
    }

    public FrequencyChart(short[] data, int graphDefinition, String title){
        this.data = new double[data.length];
        for(int i = 0; i<data.length; i++){
            this.data[i] = data[i];
        }
        this.title = title;
        this.graphDefinition = graphDefinition;
        initUI();
    }

    public FrequencyChart(Complex[] data, int graphDefinition, String title){
        useFFT = true;
        this.complexData = new Complex[data.length];
        for (int i = 0; i < data.length; i++) {
            this.complexData[i] = data[i];
        }
        this.title = title;
        this.graphDefinition = graphDefinition;
        initUI();
    }

    public FrequencyChart(double[] data, int graphDefinition, String title){
        this.data = new double[data.length];
        for(int i = 0; i< data.length; i++){
            this.data[i] = (short) (data[i] * 16383);
        }
        this.title = title;
        this.graphDefinition = graphDefinition;
        initUI();
    }
    private void initUI(){
        XYDataset dataset = createDataSet();
        JFreeChart chart = createChart(dataset);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        chartPanel.setBackground(Color.WHITE);
        add(chartPanel);

        pack();
        setTitle(title);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private XYDataset createDataSet(){
        var series = new XYSeries("Data");
        if(useFFT){
            double df = 44100 / complexData.length;
            for( int i = -complexData.length/4; i < complexData.length/4 - 1; i ++){
                double f = i * df;
                double v = Math.sqrt(Math.pow(complexData[i + complexData.length/2].real, 2) + Math.pow(complexData[i + complexData.length/2].imaginary, 2));
                series.add(2 * f, v);
            }
        }
        else {
            for (int i = 0; i < data.length; i += graphDefinition) {
                series.add(i, data[i]);
            }
        }
        var dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }

    public JFreeChart createChart(XYDataset dataset){
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Data",
                "Time or Frequency",
                "Amplitude or Occurrences",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        var renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        chart.getLegend().setFrame(BlockBorder.NONE);

        chart.setTitle(new TextTitle("Data", new Font("Serif", java.awt.Font.BOLD, 18)));

        return chart;
    }

}