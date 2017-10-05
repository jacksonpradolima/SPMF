package ca.pfv.spmf.gui.instanceviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

import ca.pfv.spmf.patterns.cluster.DoubleArray;

import javax.swing.JTextField;

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
 * This is the SPMF instance viewer.
 * It can only display instances of 2D points.
 * @author Philippe Fournier-Viger, 2016
 */
public class InstanceViewer extends JFrame implements InstanceViewerPanelListener{
	
	/** Title of this panel */
	String title = "SPMF Instance Viewer 2.09";
	
	/** Serial ID */
	private static final long serialVersionUID = 1L;
	
	/** This panel is used to draw the instances	 */
	InstancesViewerPanel panelChart = null;

	private JLabel labelX;

	private JLabel labelY;
	
	/** The list of attribute names */
	String[] attributeNamesArray = null;

	/** The combo box for selecting the attribute on the X axis */
	private JComboBox comboBoxX;

	/** The combo box for selecting the attribute on the Y axis */
	private JComboBox comboBoxY;

	
	/**
	 * Constructor
	 * @param instances a list of instances to display
	 * @param attributeNames 
	 */
	public InstanceViewer(List<DoubleArray> instances, List<String> attributeNames) {
		if(instances.size() == 0){
			JOptionPane.showMessageDialog(null,
		            "The file is empty. The instance viewer has nothing to display", "Error",
		            JOptionPane.ERROR_MESSAGE);
		}
		
		attributeNamesArray = attributeNames.toArray(new String[0]);
		
		// If no attribute names are provided, we will generate some names
		if(attributeNames == null || attributeNames.size() == 0 ){
			int dimensionCount = instances.get(0).data.length;
			attributeNamesArray = new String[dimensionCount];
			for(int i = 0; i < dimensionCount; i++){
				attributeNames.add("Attribute "+i);
			}
		}
		
//		setResizable(false);
		
		// Initialize this JFrame
		setTitle("SPMF Instance Viewer 2.09");
		setSize(900,684);
		setMinimumSize(new Dimension(884,648));
		
        // Initialize the panel to display instances
		panelChart = new InstancesViewerPanel(instances, 0, 1);
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
		panel.setBounds(10, 11, 199, 40);
		panelTools.add(panel);
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setVisible(true);
		panel.setLayout(null);
		
		labelX = new JLabel("x = ");
		labelX.setBounds(15, 7, 61, 20);
		panel.add(labelX);
		
		labelY = new JLabel("y = ");
		labelY.setBounds(106, 7, 51, 20);
		panel.add(labelY);
		
		JButton buttonZoomIn = new JButton("");
		buttonZoomIn.setBounds(313, 11, 50, 29);
		panelTools.add(buttonZoomIn);
		buttonZoomIn.setIcon(new ImageIcon(InstanceViewer.class.getResource("/ca/pfv/spmf/gui/icons/zoomin.gif")));
		
		JButton buttonZoomOut = new JButton("");
		buttonZoomOut.setBounds(366, 11, 50, 29);
		panelTools.add(buttonZoomOut);
		buttonZoomOut.setIcon(new ImageIcon(InstanceViewer.class.getResource("/ca/pfv/spmf/gui/icons/zoomout.gif")));
		
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
		buttonSaveAsPng.setIcon(new ImageIcon(InstanceViewer.class.getResource("/ca/pfv/spmf/gui/icons/save.gif")));
		
		JButton buttonPrint = new JButton("Print");
		buttonPrint.setBounds(694, 11, 163, 29);
		panelTools.add(buttonPrint);
		buttonPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelChart.doPrint();
			}
		});
		buttonPrint.setIcon(new ImageIcon(InstanceViewer.class.getResource("/ca/pfv/spmf/gui/icons/print.gif")));
		
		JLabel lblXAttribute = new JLabel("X attribute:");
		lblXAttribute.setBounds(10, 75, 75, 14);
		panelTools.add(lblXAttribute);
		
		comboBoxX = new JComboBox(attributeNamesArray);
		comboBoxX.setSelectedIndex(0);
		comboBoxX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int attributeSelectedX = comboBoxX.getSelectedIndex();
				int attributeSelectedY = comboBoxY.getSelectedIndex();
				panelChart.setAttributeSelection(attributeSelectedX, attributeSelectedY);
			}
		});
		comboBoxX.setBounds(77, 72, 121, 20);
		panelTools.add(comboBoxX);
		
		JLabel lblYAttribute = new JLabel("Y attribute:");
		lblYAttribute.setBounds(208, 75, 70, 14);
		panelTools.add(lblYAttribute);
		
		comboBoxY = new JComboBox(attributeNamesArray);
		comboBoxY.setSelectedIndex(1);
		comboBoxY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int attributeSelectedX = comboBoxX.getSelectedIndex();
				int attributeSelectedY = comboBoxY.getSelectedIndex();
				panelChart.setAttributeSelection(attributeSelectedX, attributeSelectedY);
			}
		});
		comboBoxY.setBounds(284, 72, 121, 20);
		panelTools.add(comboBoxY);

		comboBoxGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selection = (String)comboBoxGrid.getSelectedItem();
				panelChart.setDrawGrid("VISIBLE".equals(selection));
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
	public void notifyOfNewMousePosition(double x, double y) {
		
		// display the mouse position
		// we will only show two decimals
		DecimalFormat df = new DecimalFormat("0.00");
		df.setMaximumFractionDigits(2);
		String stringX = df.format(x);
		String stringY = df.format(y);
		labelX.setText("x = " + stringX);
		labelY.setText("y = " + stringY);
	}

	@Override
	public void notifyMouseOutOfChart() {
		labelX.setText("x = ");
		labelY.setText("y = ");
	}
}
