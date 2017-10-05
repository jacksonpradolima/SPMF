package ca.pfv.spmf.gui.timeseriesviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ComboBox;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JTextField;

import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.gui.plot.Plot.LegendFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.BoxLayout;

import java.awt.BorderLayout;

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
 * This is the SPMF time series viewer
 * @author Philippe Fournier-Viger, 2016
 */
public class TimeSeriesViewer extends JFrame implements TimeSeriesViewerPanelListener{
	
	/** Title of this panel */
	String title = "SPMF Time Series Viewer 2.08";
	
	/** Serial ID */
	private static final long serialVersionUID = 1L;
	
	/** This panel is used to draw the time series	 */
	TimeSeriesViewerPanel panelChart = null;

	private JLabel labelX;

	private JLabel labelY;

	private JLabel labelName;

	
	public TimeSeriesViewer(List<TimeSeries> timeSeries) {
//		setResizable(false);
		
		// Initialize this JFrame
		setTitle(title);
		setSize(900,600);
		setMinimumSize(new Dimension(884,648));
		
        // Initialize the panel to display time series
		panelChart = new TimeSeriesViewerPanel(timeSeries);
		panelChart.setForeground(Color.WHITE);
		panelChart.addListener(this);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		// Put the panel inside a scrollpane to have scrollbars.
		JScrollPane scrollPane = new JScrollPane(panelChart);
		scrollPane.setAutoscrolls(true);
		this.getContentPane().add(scrollPane);
		
		JPanel panelTools = new JPanel();
//		panelTools.setBounds(0, 0, 800, 100);
		panelTools.setMinimumSize(new Dimension(900,100));
		panelTools.setPreferredSize(new Dimension(900,100));
		panelTools.setMaximumSize(new Dimension(900,100));
		getContentPane().add(panelTools);
		panelTools.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(15, 16, 199, 63);
		panelTools.add(panel);
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setVisible(true);
		panel.setLayout(null);
		
		labelX = new JLabel("x = ");
		labelX.setBounds(15, 7, 61, 20);
		panel.add(labelX);
		
		labelY = new JLabel("y = ");
		labelY.setBounds(104, 7, 51, 20);
		panel.add(labelY);
		
		labelName = new JLabel("name = ");
		labelName.setBounds(15, 27, 169, 20);
		panel.add(labelName);
		
		JButton buttonZoomIn = new JButton("");
		buttonZoomIn.setBounds(218, 16, 50, 29);
		panelTools.add(buttonZoomIn);
		buttonZoomIn.setIcon(new ImageIcon(TimeSeriesViewer.class.getResource("/ca/pfv/spmf/gui/icons/zoomin.gif")));
		
		JButton buttonZoomOut = new JButton("");
		buttonZoomOut.setBounds(270, 16, 50, 29);
		panelTools.add(buttonZoomOut);
		buttonZoomOut.setIcon(new ImageIcon(TimeSeriesViewer.class.getResource("/ca/pfv/spmf/gui/icons/zoomout.gif")));
		
		JLabel lblLegend = new JLabel("Legend:");
		lblLegend.setBounds(353, 25, 59, 20);
		panelTools.add(lblLegend);
		
		JLabel lblLines = new JLabel("Series lines:");
		lblLines.setBounds(325, 59, 87, 20);
		panelTools.add(lblLines);

		
		JComboBox comboBoxLegend = new JComboBox(LegendFormat.values());
		comboBoxLegend.setBounds(416, 19, 87, 26);
		panelTools.add(comboBoxLegend);
		comboBoxLegend.setSelectedIndex(3);
		
		JComboBox comboBoxSeriesLine = new JComboBox(new String[]{"NONE", "1 pts", "2 pts", "3 pts", "4 pts", "5 pts", "6 pts", "7 pts", "8 pts", "9 pts", "10 pts"});
		comboBoxSeriesLine.setBounds(416, 56, 87, 26);
		comboBoxSeriesLine.setSelectedIndex(1);
		panelTools.add(comboBoxSeriesLine);
		comboBoxSeriesLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selection = comboBoxSeriesLine.getSelectedIndex();
				panelChart.setSeriesLineWidth(selection);
				
			}
		});
		
		JComboBox comboBoxGrid = new JComboBox(new String[]{"VISIBLE", "NONE"});
		comboBoxGrid.setBounds(592, 19, 87, 26);
		panelTools.add(comboBoxGrid);
		
		JLabel lblGrid = new JLabel("Grid:");
		lblGrid.setBounds(533, 25, 44, 20);
		panelTools.add(lblGrid);
		
		JLabel lblMarkers = new JLabel("Markers:");
		lblMarkers.setBounds(517, 59, 70, 20);
		panelTools.add(lblMarkers);
		
		JComboBox comboBoxMarkers = new JComboBox(new String[]{"NONE", "1 pts", "2 pts", "3 pts", "4 pts", "5 pts", "6 pts", "7 pts", "8 pts", "9 pts", "10 pts"});
		comboBoxMarkers.setSelectedIndex(5);
		comboBoxMarkers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selection = comboBoxMarkers.getSelectedIndex();
				panelChart.setMarkersSize(selection);
			}
		});
		comboBoxMarkers.setBounds(592, 53, 87, 26);
		panelTools.add(comboBoxMarkers);
		
		JButton buttonSaveAsPng = new JButton("Save as PNG");
		buttonSaveAsPng.setBounds(694, 50, 163, 29);
		panelTools.add(buttonSaveAsPng);
		buttonSaveAsPng.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelChart.export();
			}
		});
		buttonSaveAsPng.setIcon(new ImageIcon(TimeSeriesViewer.class.getResource("/ca/pfv/spmf/gui/icons/save.gif")));
		
		JButton buttonPrint = new JButton("Print");
		buttonPrint.setBounds(694, 16, 163, 29);
		panelTools.add(buttonPrint);
		buttonPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelChart.doPrint();
			}
		});
		buttonPrint.setIcon(new ImageIcon(TimeSeriesViewer.class.getResource("/ca/pfv/spmf/gui/icons/print.gif")));

		comboBoxGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selection = (String)comboBoxGrid.getSelectedItem();
				panelChart.setDrawGrid("VISIBLE".equals(selection));
			}
		});

		
		comboBoxLegend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LegendFormat selection = (LegendFormat) comboBoxLegend.getSelectedItem();
				panelChart.setDrawLegend( selection);
			}
		});
		buttonZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelChart.decreaseZoom();
			}
		});
		buttonZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelChart.increaseZoom();
			}
		});
	}

	@Override
	public void notifyOfNewMousePosition(double x, double y, Color colorUnderMouse, String timeSeriesUnderMouse) {
		
		// display the mouse position
		// we will only show two decimals
		DecimalFormat df = new DecimalFormat("0.00");
		df.setMaximumFractionDigits(2);
		String stringX = df.format(x);
		String stringY = df.format(y);
		labelX.setText("x = " + stringX);
		labelY.setText("y = " + stringY);
		
		// display the time series name
		labelName.setText("Name = "  + timeSeriesUnderMouse);
		if("".equals(timeSeriesUnderMouse) == false){
			labelName.setForeground(colorUnderMouse);
		}else{
			labelName.setForeground(Color.black);
		}
	}

	@Override
	public void notifyMouseOutOfChart() {
		labelX.setText("x = ");
		labelY.setText("y = ");
	}
}
