package ca.pfv.spmf.gui.clusterviewer;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.gui.PreferencesManager;
import ca.pfv.spmf.gui.plot.Plot;
import ca.pfv.spmf.gui.plot.Plot.Data;
import ca.pfv.spmf.gui.plot.Plot.LegendFormat;
import ca.pfv.spmf.gui.plot.Plot.Line;
import ca.pfv.spmf.gui.plot.Plot.Marker;
import ca.pfv.spmf.gui.timeseriesviewer.TimeSeriesViewerPanelListener;
import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ca.pfv.spmf.test.MainTestApriori_saveToFile;

/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * This is the panel in the Cluster viewer window
 * @author Philippe Fournier-Viger, 2016
 *
 */
public class ClusterViewerPanel extends JPanel {
	
	/**
	 * static UID
	 */
	private static final long serialVersionUID = 1L;

	/** The object used to draw plots **/
	Plot plot = null;
	
	/** This indicates the level of zoom */
	double scaleLevel = 1.0;
	
	/**  The height **/
	int height;
	
	/** The width **/
	int width;
	
	/** the heigth after scaling **/
	int originalHeigth;
	
	/** the width after scaling **/
	int originalWidth;

	/** The time series to be displayed **/
	private List<Cluster> multipleClusters;
	
	/** The color of each time series **/
	private Color multipleClustersColors[];
	
	/** min and max values on the X axis **/
    double minX = 0;
    double maxX = Double.MIN_VALUE;
	
	/** min and max values on the Y axis **/
    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;
    
    /**  If true, the grid will be drawn **/
    boolean drawTheGrid = false;
	
    /**  The size of the markers in points**/
	private int markerSize = 5;

    /**  If true, the legend will be drawn **/
    LegendFormat legendFormat = LegendFormat.BOTTOM;
    
    /** The listeners **/
    List<ClusterViewerPanelListener> listeners = new ArrayList<ClusterViewerPanelListener>();

	/** The set of basic colors */
	Color[] colors = new Color[]{Color.blue, Color.green, Color.red, Color.yellow, Color.magenta, Color.orange, Color.cyan, Color.pink,  Color.darkGray, Color.gray, Color.lightGray, };
	
    /** The selected attribute for the X axis */
	private int attributeSelectedX;
	
	 /** The selected attribute for the Y axis */
	private int attributeSelectedY;

	
    /**
     * Constructor
     * @param clusters a list of cluster to display
	 * @param attributeSelectedX the attribute selected on the X axis
	 * @param attributeSelectedY the attribute selected on the Y axis
     */
	public ClusterViewerPanel(List<Cluster> clusters, int attributeSelectedX, int attributeSelectedY){
		this.attributeSelectedX = attributeSelectedX;
		this.attributeSelectedY = attributeSelectedY;
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// Get the coordinates of the chart area
				Rectangle area = plot.getPlotAreaRectangle();
				
				// Get the relative X and Y position in the chart area
				double x = e.getX()/scaleLevel - area.getX();
				double y = area.getHeight() - (e.getY()/scaleLevel - area.getY());
				
				// Calculate the location of the mouse on the X axis of the chart
				double chartMouseX = ((x / area.width) * (maxX - minX)) + minX;
				// Calculate the location of the mouse on the Y axis of the chart
				double chartMouseY = ((y / area.height) * (maxY - minY)) + minY;
				
				// The color under the mouse
				Color colorUnderMouse = Color.WHITE;
				
				// The current time series under the mouse
				String timeSeriesUnderMouse = "";

				
				// If the mouse is not in the chart area
				if(chartMouseX < minX  ||chartMouseX > maxX ||
						chartMouseY < minY  ||chartMouseY > maxY){
					// we use the default cursor
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					
					// inform all listeners that the cursor position has changed
					for(ClusterViewerPanelListener listener : listeners){
						listener.notifyMouseOutOfChart();
					}
				}else{
					// otherwise, we use the crosshair cursor
					setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
//					System.out.println(chartMouseX + "     " + chartMouseY);
					
					// Get the color under the mouse
					try {
						Robot robot = new Robot();
						PointerInfo pi = MouseInfo.getPointerInfo();
						 colorUnderMouse = robot.getPixelColor(pi.getLocation().x, pi.getLocation().y);
					} catch (AWTException e1) {
						e1.printStackTrace();
					}
					
					// Get the time series corresponding to that color
					// For each time series
					for(int i = 0; i < multipleClustersColors.length; i++){
						// if the color of the time series and the color of the pixel under the mouse match
						if(colorUnderMouse.equals(multipleClustersColors[i])){
							// we found the time series and stop searching
							timeSeriesUnderMouse = "Cluster" + (i+1);
							break;
						}
					}
					
					// inform all listeners that the cursor position has changed
					for(ClusterViewerPanelListener listener : listeners){
						listener.notifyOfNewMousePosition(chartMouseX, chartMouseY, colorUnderMouse, timeSeriesUnderMouse);
					}
				}	

			}
		});
		this.multipleClusters = clusters;
		
		//  ========  Assign a different color to each time series ======== 

		multipleClustersColors = new Color[multipleClusters.size()];
		for(int i = 0; i< multipleClusters.size(); i++){
        	Color color = colors[i%colors.length];
        	// To make sure that each color is unique, we  slightly alter the color
        	// by adding 1 to the red, green and blue components of the color
        	// This should not be visible to the eye but it will help to detect what the mouse of the user is pointing to.
        	int newBlue = color.getBlue() == 255 ? color.getBlue() : color.getBlue()+1;
        	int newGreen = color.getGreen() == 255 ? color.getGreen() : color.getGreen()+1;
        	int newRed = color.getRed() == 255 ? color.getRed() : color.getRed()+1;
        	Color alteredColor = new Color(newRed, newGreen, newBlue);
        	multipleClustersColors[i] = alteredColor;
		}
		
//		// calculate the size of the panel
//		int maxTimeSeriesLength = 0;
//		for(TimeSeries singleTimeSeries : timeSeries){
//			if(singleTimeSeries.size() > maxTimeSeriesLength){
//				maxTimeSeriesLength = singleTimeSeries.size();
//			}
//		}
		
		originalWidth = width = 600;
		originalHeigth = height = 500;
		
		setPreferredSize(new Dimension(width,height));
		
		plot = 	null;		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		width = this.width;
		height = this.height;
		
        super.paintComponent(g);
        
        Graphics2D g2 = ( Graphics2D ) g; // cast g to Graphics2D  
        
        // Adjust the zoom
        g2.scale(scaleLevel, scaleLevel);

        // Draw the plot
        Image image = drawThePlot();
        g2.drawImage(image,0,0, this);
	}

	private Image drawThePlot() {
        Image image = createImage(width, height);
        
        // if nothing to draw
        if(multipleClusters.size() == 0){
           return image;
        }
        
        // grid color
        Color gridColor = (drawTheGrid) ? Color.BLACK : Color.WHITE;
        
        // Choose the type of legend
        
        plot = 	Plot.plot(Plot.plotOpts().
//				title("Time series").
//				titleFont(new Font("Arial", Font.ITALIC, 16)).
				width(width).
				height(height).
				bgColor(Color.WHITE).
				fgColor(Color.BLACK).
			//	padding(50). // padding for the whole image
//				plotPadding(30). // padding between plot border and the very plot
//				labelPadding(20). // padding between label and other elements
				labelFont(new Font("Arial", Font.BOLD, 12)).
			//	grids(5, 5).
				gridColor(gridColor).
			//	gridStroke(new BasicStroke(1)).
				//tickSize(10).
				legend(legendFormat));

        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        
        // For each time series
        int seriesCount = 0;
        for(int i = 0; i < multipleClusters.size(); i++){

            int count = 0;
        	Cluster cluster = multipleClusters.get(i);
        	Data data = plot.data();
        	
        	// For each data point
        	for(int j = 0; j < cluster.getVectors().size(); j++){
        		DoubleArray point = cluster.getVectors().get(j);

        		data.xy(point.data[attributeSelectedX], point.data[attributeSelectedY]);
        		
        		if(point.data[1] < minY){
        			minY = point.data[1];
        		}
        		if(point.data[1] > maxY){
        			maxY = point.data[1];
        		}
        		
        		if(point.data[0] < minX){
        			minX = point.data[0];
        		}
        		if(point.data[0] > maxX){
        			maxX = point.data[0];
        		}
        	}  	
        	
        	// automatically set the color of the time series
//        	Color color = colors[i%colors.length];
        	Color color = multipleClustersColors[i];
        	
//        	public enum Line { NONE, SOLID, DASHED };
        	
//        	Marker markersType = (markerSize) ? Marker.DIAMOND : Marker.NONE;
//        	markerTypes
        	
            // Automatically assign the marker type if we run out of colors
            Marker[] markerTypes = new Marker[]{Marker.CIRCLE, Marker.DIAMOND, Marker.SQUARE};
            int numberOfBasicColors = colors.length;
            int markerIndex = ((i) / numberOfBasicColors) % markerTypes.length;
        	Marker markerType = markerTypes[markerIndex];
        	
        	// Code for debugging the colors and marker types
//            for(int j = 0; j < 100; j++){
//	            int markerIndex = ((j) / numberOfBasicColors) % markerTypes.length;
//	        	Marker markerType = markerTypes[markerIndex];
//	        	System.out.println(j + " " + "markerType " + markerType.toString() + " color: " + j%colors.length);
//            }
        	
        	
        	plot.series("Cluster" + (i+1), data,
    				Plot.seriesOpts().
    					color(color).
    					line(Line.NONE).
    					marker(markerType).
    					markerColor(color).
    					markerSize(markerSize)
    					);
        }
        

		// configuring axes
        // Automatically choosing the maximum on the X axis and Y axis
//        int TICKCOUNT = 5;
//        {
//	    	double rangeX = Math.abs(maxX - minX);
//	    	double unroundedTickSize = rangeX/(TICKCOUNT-1);
//	    	double x = Math.ceil(Math.log10(unroundedTickSize)-1);
//	    	double pow10x = Math.pow(10, x);
//	    	double roundedTickRange = Math.ceil(unroundedTickSize / pow10x) * pow10x;
//	    	maxX = roundedTickRange*TICKCOUNT;
//        }
//        // Automatically choosing the maximum on the Y axis
//        {
//	    	double rangeY = Math.abs(maxY - minY);
//	    	double unroundedTickSize = rangeY/(TICKCOUNT-1);
//	    	double x = Math.ceil(Math.log10(unroundedTickSize)-1);
//	    	double pow10x = Math.pow(10, x);
//	    	double roundedTickRange = Math.ceil(unroundedTickSize / pow10x) * pow10x;
//	    	maxY = roundedTickRange*TICKCOUNT;
//        }
    	
        plot.
		xAxis("x", Plot.axisOpts().format(Plot.AxisFormat.NUMBER)/*.range(minX, maxX)*/).
		yAxis("y", Plot.axisOpts()/*.range(minY, maxY)*/);
        

        plot.drawChartOnGraphics2D((Graphics2D)image.getGraphics());
		return image;
	}
	
	@Override
	public void update(Graphics g) {
		super.update(g);
		
		paintComponent(g);
	}
	
//	@Override
	public Dimension getPreferredSize() {
		return new Dimension((int)scaleLevel*width, (int)scaleLevel*height);
	}

	public void increaseZoom() {
		//scaleLevel *= 2.0;
		width *= 2.0;
		height *= 2.0;

//		System.out.println(scaleLevel);
		// necessary to update the scroll bar(s) if this panel is in a JScrollPane
		revalidate();
		// necessary to redraw the image correctly
		repaint();
	}
	
	public void decreaseZoom() {
		// Adjust the width and height		
		// If the width and height are not less than 50 % of the original value, we zoom out
		if(width >= originalWidth){
			width /= 2;
			height /= 2;
//			System.out.println(scaleLevel);
			// necessary to update the scroll bar(s) if this panel is in a JScrollPane
			revalidate();
			// necessary to redraw the image correctly
			repaint();
		}

	}

	/**
	 * This method is called when the user click on the button to export the current plot to a file
	 * @throws IOException if an error occurs
	 */
	protected void export() {
		
		// ask the user to choose the filename and path
		String outputFilePath = null;
		try {
		    File path;
		    // Get the last path used by the user, if there is one
		    String previousPath = PreferencesManager.getInstance().getOutputFilePath();
		    // If there is no previous path (first time user), 
		    // show the files in the "examples" package of
		    // the spmf distribution.
		    if (previousPath == null) {
		        URL main = MainTestApriori_saveToFile.class.getResource("MainTestApriori_saveToFile.class");
		        if (!"file".equalsIgnoreCase(main.getProtocol())) {
		            path = null;
		        } else {
		            path = new File(main.getPath());
		        }
		    } else {
		        // Otherwise, use the last path used by the user.
		        path = new File(previousPath);
		    }

		    // ASK THE USER TO CHOOSE A FILE
		    final JFileChooser fc;
		    if (path != null) {
		        fc = new JFileChooser(path.getAbsolutePath());
		    } else {
		        fc = new JFileChooser();
		    }
		    int returnVal = fc.showSaveDialog(ClusterViewerPanel.this);

		    // If the user chose a file
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		        File file = fc.getSelectedFile();
		        outputFilePath = file.getPath(); // save the file path
		        // save the path of this folder for next time.
		        if (fc.getSelectedFile() != null) {
		            PreferencesManager.getInstance().setOutputFilePath(fc.getSelectedFile().getParent());
		        }
		    }else{
		    	// the user did not choose so we return
		    	return;
		    }

		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
		            "An error occured while opening the save plot dialog. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}

		try{
			
			// add the .png extension
			if(outputFilePath.endsWith("png") == false){
				outputFilePath = outputFilePath + ".png";
			}
			File outputFile = new File(outputFilePath);
			BufferedImage image = (BufferedImage)drawThePlot();
			ImageIO.write(image, "png", outputFile);

		}catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
		            "An error occured while attempting to save the plot. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}
	}

	
	/**
	 * This method is for printing the panel
	 */
	public void doPrint() {
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setJobName(" SPMF print time series");

		pj.setPrintable(new Printable() {
			@Override
			public int print(Graphics pg, PageFormat pageFormat, int pageNum)
					throws PrinterException {
			      if(pageNum > 0){
					return Printable.NO_SUCH_PAGE;
				}

		        Image image = drawThePlot();
		        
		        Graphics2D g2 = (Graphics2D) pg;
		        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		        g2.scale(pageFormat.getImageableWidth()/width, pageFormat.getImageableHeight()/ height);
		        g2.drawImage(image,0,0, null);
		        return Printable.PAGE_EXISTS;
			}
		}); 
		if (pj.printDialog() == false)
			return;

		try {
			pj.print();
		} catch (PrinterException ex) {
			// handle exception
		}
	}

	
	/**
	 * Add a listener to the changes to this panel
	 * @param listener a listener
	 */
	public void addListener(ClusterViewerPanelListener listener){
		listeners.add(listener);
	}

	/**
	 * Set the option to draw the grid of the chart to true or false
	 * @param drawTheGrid a boolean indicating whether the grid should be drawn
	 */
	public void setDrawGrid(boolean drawTheGrid) {
		this.drawTheGrid = drawTheGrid;
		
		// necessary to update the scroll bar(s) if this panel is in a JScrollPane
		revalidate();
		// necessary to redraw the image correctly
		repaint();
	}
	

	/**
	 * Set the option to draw the legend and where
	 * @param selection a LegendFormat object indicating where the legend should be drawn
	 */
	public void setDrawLegend(LegendFormat selection) {
		this.legendFormat = selection;
		
		// necessary to update the scroll bar(s) if this panel is in a JScrollPane
		revalidate();
		// necessary to redraw the image correctly
		repaint();
	}

	/**
	 * 
	 * Set the size of the markers
	 * @param selection true will draw the markers, otherwise not
	 */
	public void setMarkersSize(int selection) {
		this.markerSize = selection;
		
		// necessary to update the scroll bar(s) if this panel is in a JScrollPane
		revalidate();
		// necessary to redraw the image correctly
		repaint();
	}

	/**
	 * Set the selection of atributes
	 * @param attributeSelectedX the attribute selected on the X axis
	 * @param attributeSelectedY the attribute selected on the Y axis
	 */
	public void setAttributeSelection(int attributeSelectedX, int attributeSelectedY) {
		this.attributeSelectedX = attributeSelectedX;
		this.attributeSelectedY = attributeSelectedY;
		
		// necessary to update the scroll bar(s) if this panel is in a JScrollPane
		revalidate();
		// necessary to redraw the image correctly
		repaint();
	}
}
