import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class DynamicDataDemo extends ApplicationFrame  {
    /** The time series data. */
    private TimeSeries seriesCpu;
    private TimeSeries seriesMemory;
    
    private TimeSeries seriesCpuPid;
    private TimeSeries seriesMemoryPid;
    
    /** The most recent value added. */
    private double lastValueCpu = 0.0;
    private double lastValueMemory = 0.0;
    private double lastValueCpuPid = 0.0;
    private double lastValueMemoryPid = 0.0;
    
    
    private HashMap tempServerInfo;
    private CircularBuffer dataReceiver;
    private int failCount = 0;
    private int oldPointerCount = 0;
public DynamicDataDemo(final String title, CircularBuffer bufferC, HashMap tempHash) {

    super(title);
    
    this.dataReceiver = bufferC;
    this.tempServerInfo = tempHash;
    //this.tempServerInfo.put("QueuePointer", 0);
    
    this.seriesCpu = new TimeSeries("Runtime SysetemCPULoad Monitor", Millisecond.class);
    this.seriesCpuPid = new TimeSeries("Runtime SysetemCPUProcessId Monitor", Millisecond.class);
    this.seriesMemory = new TimeSeries("Runtime SysetemMemoryLoad Monitor", Millisecond.class);
    this.seriesMemoryPid =  new TimeSeries("Runtime SysetemMemory ProcessId Load Monitor", Millisecond.class);
    
    
    final TimeSeriesCollection datasetCpuUsage = new TimeSeriesCollection(this.seriesCpu);
    final TimeSeriesCollection datasetCPUPidUsage = new TimeSeriesCollection(this.seriesCpuPid);
    final TimeSeriesCollection datasetMemoryUsage = new TimeSeriesCollection(this.seriesMemory);
    final TimeSeriesCollection datasetMemoryPidUsage = new TimeSeriesCollection(this.seriesMemoryPid);
    
   
     
    
    final JFreeChart chartCpu = createChart(datasetCpuUsage,100.0); //CPU usage
    final JFreeChart chartCpuPid = createChart(datasetCPUPidUsage,20.0); //process Id usage
    final JFreeChart chartMemory = createChart(datasetMemoryUsage,100.0); // Memory usage
    final JFreeChart chartMemoryPid = createChart(datasetMemoryPidUsage,10.0); //Memory of process Id usage
    
    
    JPanel contents;
    final ChartPanel chartPanelCpu = new ChartPanel(chartCpu);
    final ChartPanel chartPanelCpuPid = new ChartPanel(chartCpuPid);
    final ChartPanel chartPanelMemory = new ChartPanel(chartMemory);
    final ChartPanel chartPanelMemoryPid = new ChartPanel(chartMemoryPid);
    
    
    contents = new JPanel();
    
     contents = new JPanel(new BorderLayout());
     //contents[1] = new JPanel(new BorderLayout());
    
    contents.add(chartPanelCpu,BorderLayout.NORTH);
    contents.add(chartPanelCpuPid,BorderLayout.WEST);
    contents.add(chartPanelMemory,BorderLayout.SOUTH);
    contents.add(chartPanelMemoryPid,BorderLayout.EAST);
    
    chartPanelCpu.setPreferredSize(new java.awt.Dimension(500, 270));
    chartPanelCpuPid.setPreferredSize(new java.awt.Dimension(500, 270));
    chartPanelMemory.setPreferredSize(new java.awt.Dimension(500, 270));
    chartPanelMemoryPid.setPreferredSize(new java.awt.Dimension(500, 270));
    
    
    setContentPane(contents);
    //setContentPane(contentMemory);
}


private JFreeChart createChart(final XYDataset dataset,double range) {
    final JFreeChart result = ChartFactory.createTimeSeriesChart(
        "Runtime SysetemLoad Monitor", 
        "Time", 
        "Value",
        dataset, 
        true, 
        true, 
        false
    );
    final XYPlot plot = result.getXYPlot();
    ValueAxis axis = plot.getDomainAxis();
    axis.setAutoRange(true);
    axis.setFixedAutoRange(60000.0);  // 60 seconds
    axis = plot.getRangeAxis();
    axis.setRange(0.0, range); 
    return result;
}


public  void summarize() {
	
	
	 double tempCpu         = 0.0;
	 double tempCpuPid      = 0.0;
	 double tempMemory  	= 0.0;
	 double tempMemoryPid 	= 0.0;
     int range              = 8;
     int init = dataReceiver.forwardPointer();
     
	 try {
		 
		 
		 //i want to wait for 5 values
		 if (init > 10) {
			 //----------------------- HANDLING WHEN PRODUCER IS DOWN----------------------------------
			 
			 if ( this.oldPointerCount == init ) {
			// We will try for 3 times , if same response then drop to zero
				 this.failCount ++;
				 if ( this.failCount == 3) {
					 this.lastValueCpu = this.lastValueCpuPid = this.lastValueMemory = this.lastValueMemoryPid = 0.0;
				 }
				 else{
					 this.failCount = 0;
				 }
			 } //----------------------------HANDLING OF PRODUCER IS DOWN CHECK COMPLETE-------------------
			 else {
			 //put Queue pointer value in hash
			 long timestampLatest =  dataReceiver.dequeue(init).timestamp;
			 long timestampMy = 0 ;
			// for(int count = 0; count < range ;init -- , count++) {
				 int count = 0;
				
				do  {
					//System.out.println(timestampMy);
					 timestampMy = dataReceiver.dequeue(init).timestamp;
					 tempCpu       += dataReceiver.dequeue(init).cpuUsage;
					 tempMemory    += dataReceiver.dequeue(init).memoryUsed;
					 tempCpuPid    += dataReceiver.dequeue(init).cpuPidUsage;
					 tempMemoryPid += dataReceiver.dequeue(init).memoryPidUsed;
        		     count ++;
        		     init--;
				 	} while(timestampLatest - timestampMy <= 1000 && count < 10);
        		        		 
				 this.lastValueCpu     = tempCpu / count;
				 this.lastValueCpuPid   = tempCpuPid / count;
				 this.lastValueMemory   = tempMemory /count;
				 this.lastValueMemoryPid = tempMemoryPid / count;
			 //System.out.println(tempCpu + " :" + tempMemory + " : " + tempCpuPid + " : " + tempMemoryPid);
			 }	 
			
			 
		 }
		 else {			 
			 //this.lastValueCpu = this.lastValueCpuPid = this.lastValueMemory = this.lastValueMemoryPid = 0.0;
		 }
	 }
	 catch (Exception e) {
		 
	 }
	 this.oldPointerCount = init;
	 this.seriesCpu.add(new Millisecond(), this.lastValueCpu);
     this.seriesMemory.add(new Millisecond(), this.lastValueMemory);
     this.seriesCpuPid.add(new Millisecond(),this.lastValueCpuPid);
     this.seriesMemoryPid.add(new Millisecond(),this.lastValueMemoryPid);
 }
}