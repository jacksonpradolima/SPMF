package ca.pfv.spmf.gui.clusterviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import ca.pfv.spmf.gui.plot.Plot.LegendFormat;
import ca.pfv.spmf.patterns.cluster.Cluster;

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
 * This is the SPMF cluster viewer.
 * It can only display clusters of 2D points.
 * @author Philippe Fournier-Viger, 2016
 */
public class ClusterViewer extends JFrame implements ClusterViewerPanelListener{
	
	/** Title of this panel */
	String title = "SPMF Cluster Viewer 2.09";
	
	/** Serial ID */
	private static final long serialVersionUID = 1L;
	
	/** This panel is used to draw the clusters	 */
	ClusterViewerPanel panelChart = null;

	private JLabel labelX;

	private JLabel labelY;

	private JLabel labelName;


	/** The list of attribute names */
	String[] attributeNamesArray = null;

	/** The combo box for selecting the attribute on the X axis */
	private JComboBox comboBoxX;

	/** The combo box for selecting the attribute on the Y axis */
	private JComboBox comboBoxY;

	
	/**
	 * Constructor
	 * @param clusters a list of clusters to display
	 */
	public ClusterViewer(List<Cluster> clusters, List<String> attributeNames) {
		if(clusters.size() == 0){
			JOptionPane.showMessageDialog(null,
		            "The file is empty. The instance viewer has nothing to display", "Error",
		            JOptionPane.ERROR_MESSAGE);
		}
		
		attributeNamesArray = attributeNames.toArray(new String[0]);
		
		// If no attribute names are provided, we will generate some names
		if(attributeNames == null || attributeNames.size() == 0 ){
			int dimensionCount = clusters.get(0).getVectors().get(0).data.length;
			attributeNamesArray = new String[dimensionCount];
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute "+i);
			}
		}
		
//		setResizable(false);
		
		// Initialize this JFrame
		setTitle(title);
		setSize(900,687);
		setMinimumSize(new Dimension(884,648));
		
        // Initialize the panel to display clusters
		panelChart = new ClusterViewerPanel(clusters,0,1);
		panelChart.setForeground(Color.WHITE);
		panelChart.addListener(this);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		// Put the panel inside a scrollpane to have scrollbars.
		JScrollPane scrollPane = new JScrollPane(panelChart);
		scrollPane.setAutoscrolls(true);
		this.getContentPane().add(scrollPane);
		
		JPanel panelTools = new JPanel();
//		panelTools.setBounds(0, 0, 800, 100);
		panelTools.setMinimumSize(new Dimension(900, 120));
		panelTools.setPreferredSize(new Dimension(900, 120));
		panelTools.setMaximumSize(new Dimension(900, 120));
		getContentPane().add(panelTools);
		panelTools.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(15, 16, 199, 55);
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
		buttonZoomIn.setIcon(new ImageIcon(ClusterViewer.class.getResource("/ca/pfv/spmf/gui/icons/zoomin.gif")));
		
		JButton buttonZoomOut = new JButton("");
		buttonZoomOut.setBounds(270, 16, 50, 29);
		panelTools.add(buttonZoomOut);
		buttonZoomOut.setIcon(new ImageIcon(ClusterViewer.class.getResource("/ca/pfv/spmf/gui/icons/zoomout.gif")));
		
		JLabel lblLegend = new JLabel("Legend:");
		lblLegend.setBounds(353, 25, 59, 20);
		panelTools.add(lblLegend);

		
		JComboBox comboBoxLegend = new JComboBox(LegendFormat.values());
		comboBoxLegend.setBounds(416, 19, 87, 26);
		panelTools.add(comboBoxLegend);
		comboBoxLegend.setSelectedIndex(3);
		
		JComboBox comboBoxGrid = new JComboBox(new String[]{"VISIBLE", "NONE"});
		comboBoxGrid.setBounds(592, 19, 87, 26);
		panelTools.add(comboBoxGrid);
		comboBoxGrid.setSelectedIndex(1);
		
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
		buttonSaveAsPng.setIcon(new ImageIcon(ClusterViewer.class.getResource("/ca/pfv/spmf/gui/icons/save.gif")));
		
		JButton buttonPrint = new JButton("Print");
		buttonPrint.setBounds(694, 16, 163, 29);
		panelTools.add(buttonPrint);
		buttonPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelChart.doPrint();
			}
		});
		buttonPrint.setIcon(new ImageIcon(ClusterViewer.class.getResource("/ca/pfv/spmf/gui/icons/print.gif")));
		
		comboBoxX = new JComboBox(attributeNamesArray);
		comboBoxX.setSelectedIndex(0);
		comboBoxX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int attributeSelectedX = comboBoxX.getSelectedIndex();
				int attributeSelectedY = comboBoxY.getSelectedIndex();
				panelChart.setAttributeSelection(attributeSelectedX, attributeSelectedY);
			}
		});
		comboBoxX.setBounds(92, 82, 121, 20);
		panelTools.add(comboBoxX);
		
		comboBoxY = new JComboBox(attributeNamesArray);
		comboBoxY.setSelectedIndex(1);
		comboBoxY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int attributeSelectedX = comboBoxX.getSelectedIndex();
				int attributeSelectedY = comboBoxY.getSelectedIndex();
				panelChart.setAttributeSelection(attributeSelectedX, attributeSelectedY);
			}
		});
		comboBoxY.setBounds(299, 82, 121, 20);
		panelTools.add(comboBoxY);
		
		JLabel label = new JLabel("X attribute:");
		label.setBounds(25, 85, 75, 14);
		panelTools.add(label);
		
		JLabel label_1 = new JLabel("Y attribute:");
		label_1.setBounds(223, 85, 70, 14);
		panelTools.add(label_1);
		


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
	public void notifyOfNewMousePosition(double x, double y, Color colorUnderMouse, String objectUnderMouse) {
		
		// display the mouse position
		// we will only show two decimals
		DecimalFormat df = new DecimalFormat("0.00");
		df.setMaximumFractionDigits(2);
		String stringX = df.format(x);
		String stringY = df.format(y);
		labelX.setText("x = " + stringX);
		labelY.setText("y = " + stringY);
		
		// display the selection name
		labelName.setText("Name = "  + objectUnderMouse);
		if("".equals(objectUnderMouse) == false){
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
