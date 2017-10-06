package ca.pfv.spmf.gui.patternvizualizer;

import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import ca.pfv.spmf.gui.PreferencesManager;
import ca.pfv.spmf.gui.patternvizualizer.filters.AbstractFilter;
import ca.pfv.spmf.test.MainTestApriori_saveToFile;
/*
 * Copyright (c) 2008-2015 Philippe Fournier-Viger
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
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This is a simple user interface to vizualize patterns found by algorithms in SPMF.
 * 
 * @author Philippe Fournier-Viger
 */
public class PatternVizualizer extends JFrame{
	
	/** title **/
	String title = "SPMF - Pattern vizualization tool 2.05";
	
	/** Generated serial ID*/
	private static final long serialVersionUID = -2012129335077139428L;
	
	/** The table for showing the patterns to the user */
	JTable table;
	/** The label indicating the number of patterns currently shown in the Jtable */
	private JLabel labelNumberOfPatterns;
	
	/** Variables for storing the data from the TableModel used in the Jtable */
	Vector<List<Object>> data = null;
	/** List of table column names */
	Vector<String> columnNames = null;
	/** List of table column classes (Integer, Double, String) */
	Vector<Class>  columnClasses = null;
	
	/** The JList showing the current filters that are applied on the Jtable*/
	private JList listFilters;
	/** The list model for the JList showing the filters */
	private DefaultListModel<String> listModelFilters;

	/** The "Remove selected filter" button */
	private JButton btnRemoveFilter;
	/** The "Remove all filters" button */
	private JButton btnRemoveAllFilters;

	/** The list of current filters, used for filtering the JTable **/
	PatternTableRowFilters  rowFilters = new PatternTableRowFilters();

	/** The TableRowSorter used by the JTable */
    private TableRowSorter<PatternTableModel> sorter;

    /**  The TableModel used by the JTable */
    private PatternTableModel model;
    private JTextField textFieldSearch;

	private JComboBox comboBoxExport;
	
	/**
	 * Method to initialize the windows for vizualizing patterns, and diplay patterns from a file in SPMF format.
	 * @param patternFilePath the path to a file containing patterns, in SPMF format.
	 * @throws IOException if error while reading file
	 */
	public PatternVizualizer(String patternFilePath) throws ParseException, IOException {

		// set the size of the window, and make it non-resizeable
		setSize(800, 600);
		setResizable(false);
		
		// get the file name 
		File file = new File(patternFilePath);
		String fileName = file.getName();
		
		// get the file last modification date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
		//printing value of Date System.out.println("current Date: " + currentDate); DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss"); //formatted value of current Date System.out.println("Milliseconds to Date: " + df.format(currentDate)); - See more at: http://javac.in/?p=402#sthash.ziDUnonk.dpuf
		String modificationDate =  sdf.format(new Date(file.lastModified()));
		
		// set the window title and layout
		setTitle(title);
		getContentPane().setLayout(null);
		
		// add the scroll pane for the Jtable
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(22, 41, 572, 462);
		getContentPane().add(scrollPane);
		
		// Create the Jtable for showing patterns
		table = new JTable();
		// let the user sort the columns in the table by clicking on the column headers
		table.setAutoCreateRowSorter(true);
		table.setCellSelectionEnabled(true);
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		scrollPane.setViewportView(table);
		// set the horizontal and vertical scrollbars for the table
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		// Create the label for showing the number of patterns currently displayed
		labelNumberOfPatterns = new JLabel();
		labelNumberOfPatterns.setBounds(22, 514, 189, 14);
		getContentPane().add(labelNumberOfPatterns);
		
		// Show the patern file size
		double size = file.length() / 1024d / 1024d;
		String fileSize = String.format("%.4f", size);
		JLabel lblFileSizemb = new JLabel("File size (MB): " + fileSize);
		lblFileSizemb.setBounds(221, 535, 174, 14);
		getContentPane().add(lblFileSizemb);
		
		// Show the file name
		JLabel lblFileName = new JLabel("File name: " + fileName);
		lblFileName.setBounds(22, 535, 224, 14);
		getContentPane().add(lblFileName);
		
		// Show the date of last modification
		JLabel lblLastModified = new JLabel("Last modified: " + modificationDate);
		lblLastModified.setBounds(405, 535, 217, 14);
		getContentPane().add(lblLastModified);
		
		// Label "Patterns:"
		JLabel lblPatterns = new JLabel("Patterns:");
		lblPatterns.setBounds(22, 16, 77, 14);
		getContentPane().add(lblPatterns);
		
		// Label "Filters"
		JLabel lblApplyFilters = new JLabel("Apply filter(s):");
		lblApplyFilters.setBounds(604, 104, 138, 14);
		getContentPane().add(lblApplyFilters);
		
		// Create the JList for showing the current filters
		// and create the underlying ListModel
		listModelFilters = new DefaultListModel();
		

		// ****** read the file containing patterns to fill the JTable  *********
		readFile(patternFilePath);
		
		// Set the window as modal
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		// Create the button for adding a filter
		JButton btnAddAFilter = new JButton("Add a filter");
		btnAddAFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// When the user click on the button,
				// a window is created for letting the user create the filter
				FilterSelectionWindow window = new FilterSelectionWindow(columnNames, columnClasses, rowFilters, PatternVizualizer.this);

			}
		});
		btnAddAFilter.setBounds(609, 284, 174, 23);
		getContentPane().add(btnAddAFilter);
		
		// Create the "remove selected filter" button
		btnRemoveFilter = new JButton("Remove selected filter");
		btnRemoveFilter.setBounds(609, 308, 174, 23);
		btnRemoveFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeSelectedFilter();
			}
		});
		btnRemoveFilter.setEnabled(false);
		getContentPane().add(btnRemoveFilter);

		// Create the "remove all filters" button
		btnRemoveAllFilters = new JButton("Remove all filters");
		btnRemoveAllFilters.setBounds(609, 333, 174, 23);
		btnRemoveAllFilters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				System.out.println("remove all filters");
				removeAllFilters();
			}

		});
		btnRemoveAllFilters.setEnabled(false);
		getContentPane().add(btnRemoveAllFilters);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(609, 124, 174, 155);
		getContentPane().add(scrollPane_1);
		listFilters = new JList(listModelFilters);
		scrollPane_1.setViewportView(listFilters);
		
		JLabel lblNewLabel = new JLabel("Search:");
		lblNewLabel.setBounds(604, 39, 69, 20);
		getContentPane().add(lblNewLabel);
		
		textFieldSearch = new JTextField();
		textFieldSearch.setBounds(609, 59, 128, 26);
		getContentPane().add(textFieldSearch);
		textFieldSearch.setColumns(10);
		
		JButton btnSearch = new JButton("");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				search();
			}
		});
		btnSearch.setIcon(new ImageIcon(PatternVizualizer.class.getResource("/ca/pfv/spmf/gui/patternvizualizer/find.gif")));
		btnSearch.setBounds(738, 59, 30, 26);
		getContentPane().add(btnSearch);
		
		JLabel lblExportTo = new JLabel("Export current view to:");
		lblExportTo.setBounds(604, 372, 138, 20);
		getContentPane().add(lblExportTo);
		
		comboBoxExport = new JComboBox();
		comboBoxExport.setBounds(609, 396, 138, 26);
		getContentPane().add(comboBoxExport);
		comboBoxExport.addItem("SPMF format");
		comboBoxExport.addItem("TSV format");
		comboBoxExport.addItem("CSV format");
		
		JButton buttonExport = new JButton("");
		buttonExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		buttonExport.setIcon(new ImageIcon(PatternVizualizer.class.getResource("/ca/pfv/spmf/gui/patternvizualizer/save.gif")));
		buttonExport.setBounds(753, 396, 30, 26);
		getContentPane().add(buttonExport);
		listFilters.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				selectFilter(arg0);
			}
		});
		
		// set this window as visible
		setVisible(true);
	}





	/**
	 * Method to read a file containing patterns and show the patterns in the table
	 * @param patternFilePath the path to the file containing patterns, in SPMF format.
	 * @throws IOException if error while reading file
	 */
	private void readFile(String patternFilePath) throws IOException {
		// variable to count the number of patterns
		int numberOfPatterns = 0;
		
		// initialize the variables used by the JTable
		data = new Vector<List<Object>>();
		columnNames = new Vector<String>();
		columnClasses = new Vector<Class>();
		columnClasses.add(String.class);
		// add a first column named "Pattern" to the list of columns
		columnNames.add("Pattern");
		
		// Create a buffered reader for reading the file containing the patterns
		BufferedReader br = new BufferedReader(new FileReader(patternFilePath));
        String line;
        while((line = br.readLine()) != null) { // iterate over the lines to build the transaction
			// if the line is  a comment, is  empty or is  metadata
			if (line.isEmpty() == true ||line.charAt(0) == '#' 
					|| line.charAt(0) == '%' || line.charAt(0) == '@') {
				
				//...
			}
			else{
				// Create a list of Object for storing the values
				List<Object> lineData = new ArrayList<Object>();
				
				// Find the position of the next "#" symbol indicating the end of the first attribute value
				int positionFirstDelimiter = line.indexOf(" #");
				
				// If there is no "#", we will consider that the line has a single attribute
				if(positionFirstDelimiter == -1){
					// add the first attribute value to the data of this line
					lineData.add(line.substring(0, line.length()));
				}else{
					// Otherwise, we will break down the line into several attributes using the "#"
						
					// add the first attribute value to the data of this line
					lineData.add(line.substring(0, positionFirstDelimiter));
					
					String cutLine = line;
					 
					// Then we will process the next attributes one by one
					while(positionFirstDelimiter >=0){
						// We will first remove what has been already processed
						cutLine = cutLine.substring(positionFirstDelimiter+1,cutLine.length());
						// We will find the first space and the position of the next attriute
						int positionFirstSpace = cutLine.indexOf(' ',1);
						int positionNextDelimiter = cutLine.indexOf(" #",1);
						
						// The name of the current attribute is the string between the 
						// first position and the first space
						String attributeName = cutLine.substring(0, positionFirstSpace);
						
						// The attribute value is the String from the first space until the next " #" indicating
						// the start of the next attriute.
						String attributeValue;
						if(positionNextDelimiter == -1){
							attributeValue = cutLine.substring(positionFirstSpace+1, cutLine.length());
						}else{
							attributeValue = cutLine.substring(positionFirstSpace+1, positionNextDelimiter);
						}
						
						// if it is the first line
						if(numberOfPatterns == 0){
							// add that attribute name to the list of column
							columnNames.add(attributeName);
						}
						
						 // Then we add the attribute value to the data for this line according to its type
						Double doubleValue = isDouble(attributeValue);
						if(doubleValue != null){
							// If first time, we remember the class for this column
							if(numberOfPatterns == 0){
								columnClasses.add(Double.class);
							}
							// if it is a double value, we add that value the list of attribute value
							lineData.add(doubleValue);
							
							positionFirstDelimiter = positionNextDelimiter;
							continue; 
						}
						
						// we check if it is an integer value
						Integer integerValue = isInteger(attributeValue);
						if(integerValue != null){
							// If first time, we remember the class for this column
							if(numberOfPatterns == 0){
								columnClasses.add(Double.class);
							}
							// if it is an integer value, we add that value the list of attribute value
							columnClasses.add(Integer.class);
							lineData.add(integerValue);
							
							positionFirstDelimiter = positionNextDelimiter;
							continue;
						}
						
						// we check if it is a boolean value
						Boolean booleanValue = isBoolean(attributeValue);
						if(booleanValue != null){
							// If first time, we remember the class for this column
							if(numberOfPatterns == 0){
								columnClasses.add(Boolean.class);
							}
							// if it is a boolean value, we add that value the list of attribute value
							columnClasses.add(Boolean.class);
							lineData.add(booleanValue);
							
							positionFirstDelimiter = positionNextDelimiter;
							continue;
						}
						
						// else we assume that it is a string value
						columnClasses.add(String.class);
						lineData.add(attributeValue);
						
						
						positionFirstDelimiter = positionNextDelimiter;
						
					}

				}
				
				// add the line to the model
				data.add(lineData);
				
				// count the number of patterns
				numberOfPatterns++;
			}
        }
        // close the file
        br.close();
        
        // if the file is empty, do nothing
        if(numberOfPatterns == 0){
        	return;
        }

        // We have filled the table model, so now we set it as the table model for the JTable
        model = new PatternTableModel(data, columnNames, columnClasses);
        table.setModel(model);
        
		// We set the table sorter of the JTable
        sorter = new TableRowSorter<PatternTableModel>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(rowFilters);
        
        // We auto adjust column widths of tables so that the values are fully displayed
        TableColumnAdjuster tca = new TableColumnAdjuster(table);
        tca.adjustColumns();
		
        // We update the number of patterns shown in the window
        refreshNumberOfPatternsDisplayed();
	}


	
	/**
	 * Check if a string value is a doule value
	 * 
	 * @param token
	 *            the string value
	 * @return the value as a double, or null if it is not a double value
	 */
    private Double isDouble(String token) {
    	Double result = null;
    	try{
    		result = Double.valueOf(token);
    	}catch(Exception e){ 		
    	}
		return result;
	}
    
	/**
	 * Check if a string value is an integer value
	 * 
	 * @param token
	 *            the string value
	 * @return the value as an integer, or null if it is not an integer value
	 */
    private Integer isInteger(String token) {
    	Integer result = null;
    	try{
    		result = Integer.valueOf(token);
    	}catch(Exception e){
    	}
		return result;
	}

	/**
	 * Check if a string value is a boolean value
	 * 
	 * @param token
	 *            the string value
	 * @return the value as a boolean, or null if it is not a boolean value
	 */
    private Boolean isBoolean(String token) {
    	if("true".equals(token)){
    		return Boolean.TRUE;
    	}else if("false".equals(token)){
    		return Boolean.FALSE;
    	}
		return null;
	}

    /**
     * This method is called when the user has added a new filter.
     * It updates the list of filters, the buttons and also the JTable.
     */
	public void filtersHaveBeenUpdated() {
		//remove all filters
		listModelFilters.clear();
		// fill the JList of filters
		for(int i =0; i < rowFilters.filters.size(); i++){
			AbstractFilter filter = (AbstractFilter) rowFilters.filters.get(i);
			listModelFilters.addElement(filter.getFilterWithParameterName());				 
		}
		// If there are some filters, then enable the bubtton "Remove all filters"
		if(rowFilters.filters.size() >=0){
			btnRemoveAllFilters.setEnabled(true);
		}
		
		// Notifiy all listeners that the filters have been updated
		// This will refresh the Jtable using the new filters
		for(TableModelListener listener: model.listeners){
			listener.tableChanged(new TableModelEvent(model));
		}
		// Refresh the number of patterns displayed in the window
		refreshNumberOfPatternsDisplayed();
	}
	
	/**
	 * This method is called when the user selects a filter from the list of filters
	 * @param arg0 a list selection event from the JList of filters
	 */
	protected void selectFilter(ListSelectionEvent arg0) {
		// if a filter is selected
		if(listFilters.getSelectedIndex()> -1){
			// Enable the button "Remove selected filter"
			btnRemoveFilter.setEnabled(true);
		}
	}
	

	/**
	 * Method to remove the current selected filter in the list of filters.
	 */
	private void removeSelectedFilter() {
		// Get the index of the selected filter in the list of filter
		int index = listFilters.getSelectedIndex();
		// Remove the filter from the JList model
		listModelFilters.remove(index);
		//Remove the filter from the filters used by the JTable
		rowFilters.filters.remove(index);
		// Disable the button for removing the selected filter
		btnRemoveFilter.setEnabled(false);
		// if there are no filters left, then we also disable the "remove all filters" button
		if(rowFilters.filters.size() == 0){
			btnRemoveAllFilters.setEnabled(false);
		}
		// Notifiy all listeners that the filters have been updated
		// This will refresh the Jtable using the new filters
		for(TableModelListener listener: model.listeners){
			listener.tableChanged(new TableModelEvent(model));
		}
		// Refresh the number of patterns displayed in the window
		refreshNumberOfPatternsDisplayed();
	}

	/**
	 * This method is called when the user removes all filters.
	 * It clear the JList of filters and also removes all filters from the Jtable, and update
	 * the user interface accordingly.
	 */
	private void removeAllFilters() {
		// Delete all the filters in the JList of filters
		listModelFilters.clear();
		// Delete all the filters in the Jtable
		rowFilters.filters.clear();
		//Disable the button for removing filters
		btnRemoveAllFilters.setEnabled(false);
		btnRemoveFilter.setEnabled(false);
		
		// Notifiy all listeners that the filters have been updated
		// This will refresh the Jtable using the new filters
		for(TableModelListener listener: model.listeners){
			listener.tableChanged(new TableModelEvent(model));
		}
		// Refresh the number of patterns displayed in the window
		refreshNumberOfPatternsDisplayed();
	}
	

	/**
	 * Refresh the number of patterns displayed in the window
	 */
	private void refreshNumberOfPatternsDisplayed() {
		labelNumberOfPatterns.setText("Number of patterns: " + table.getRowCount());
	}
	

	/**
	 * This method is called when the user click on the button to export the current patters to 
	 * a file format.
	 * @throws IOException if an error occurs
	 */
	protected void export() {
		String selection = (String)comboBoxExport.getSelectedItem();
		
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
		    int returnVal = fc.showSaveDialog(PatternVizualizer.this);

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
		            "An error occured while opening the output file dialog. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}

		try{
			// if the user wants to save in CSV format
			if("CSV format".equals(selection)){
				exportToCSV(table, outputFilePath);
			}else if("TSV format".equals(selection)){
				// if the user wants to save in TSV format
				exportToTSV(table, outputFilePath);
			}else if("SPMF format".equals(selection)){
				// if the user wants to save in SPMF format
				exportToSPMFFormat(table, outputFilePath);
			}
		}catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
		            "An error occured while attempting to save the file. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Save content of JTable to the SPMF format
	 * @param table  a JTable
	 * @param filepath  the file path where the file should be saved
	 * @throws IOException exception if error writing to file
	 */
	private void exportToSPMFFormat(JTable table2, String outputFilePath) throws IOException {
		  BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

        // For each row
        for(int i=0; i< table.getRowCount(); i++) {
        	
        	// Write the data in that row for each column
            for(int j=0; j < table.getColumnCount(); j++) {
            	//if the first column
            	if(j==0){
            		// write the value
            		writer.write(table.getValueAt(i,j).toString());
            	}
            	// if not the first column
            	else{
            		// write the column name + space
            		writer.write(columnNames.get(j));
            		writer.write(' ');
            		// then write the value
            		writer.write(table.getValueAt(i,j).toString());
            	}
            	// if not the last element on this line, we put a "," after
            	if(j != table.getColumnCount() -1){
            		writer.write(' ');
            	}
            }
            // Write the end of line
            writer.newLine();
        }
        // Close the file
        writer.close();
	}





	/**
	 * Save content of JTable to tab-separated format compatible with Excel and other software
	 * @param table  a JTable
	 * @param filepath  the file path where the file should be saved
	 * @throws IOException exception if error writing to file
	 */
	public void exportToTSV(JTable table, String filepath) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
        // for each column
        for(int i = 0; i < table.getColumnCount(); i++){
        	// write the column name followed by a tab
        	writer.write(table.getColumnName(i));
        	// if not the last element on this line, we put a ","
        	if(i!= table.getColumnCount() -1){
        		writer.write('\t');
        	}
        }
        // then write the end of line
        writer.newLine();

        // For each row
        for(int i=0; i< table.getRowCount(); i++) {
        	
        	// Write the data in that row for each column
            for(int j=0; j < table.getColumnCount(); j++) {
            	writer.write(table.getValueAt(i,j).toString());
            	// if not the last element on this line, we put a "," after
            	if(j != table.getColumnCount() -1){
            		writer.write('\t');
            	}
            }
            // Write the end of line
            writer.newLine();
        }
        // Close the file
        writer.close();
	}
	
	/**
	 * Save content of JTable to comma-separated format compatible with Excel and other software
	 * @param table  a JTable
	 * @param filepath  the file path where the file should be saved
	 * @throws IOException exception if error writing to file
	 */
	public void exportToCSV(JTable table, String filepath) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
        // for each column
        for(int i = 0; i < table.getColumnCount(); i++){
        	// write the column name followed by a tab
        	String string = table.getColumnName(i);
        	// if the , character appears, we should add quotes according to CSV format
        	if(string.indexOf(',') != -1){
            	string = '\"' + string + '\"';
        	}
        	writer.write(string);
        	// if not the last element on this line, we put a ","
        	if(i!= table.getColumnCount() -1){
        		writer.write(',');
        	}
        }
        // then write the end of line
        writer.newLine();

        // For each row
        for(int i=0; i< table.getRowCount(); i++) {
        	
        	// Write the data in that row for each column
            for(int j=0; j < table.getColumnCount(); j++) {
            	String string = table.getValueAt(i,j).toString();
            	// if the , character appears, we should add quotes according to CSV format
            	if(string.indexOf(',') != -1){
                	string = '\"' + string + '\"';
            	}
            	writer.write(string);
            	// if not the last element on this line, we put a "," after
            	if(j != table.getColumnCount() -1){
            		writer.write(',');
            	}
            }
            // Write the end of line
            writer.newLine();
        }
        // Close the file
        writer.close();
	}
	


	/**
	 * This method is called when the user click on the "search" button
	 */
	protected void search() {
		String text = textFieldSearch.getText();
		// if the user did not enter any text, then we do nothing
		if(text.length() == 0){
			return;
		}
		// We search from the position that is next to the current selected position in the table
		// except if nothing is selected, then we will search from the current position (0,0).
		int currentRow = table.getSelectedRow();
		int currentColumn = table.getSelectedColumn();
//		System.out.println("before" + currentRow + " " + currentColumn);
		
		currentColumn++;
		
		if(currentRow == -1){
			currentRow = 0;
		}
		
//		System.out.println(currentRow + " " + currentColumn);
		
		// For each row
        for(; currentRow< table.getRowCount(); currentRow++) {
        	
        	// Write the data in that row for each column
            for(; currentColumn < table.getColumnCount(); currentColumn++) {
            	// if we have found the searched text in the current cell
            	if(table.getValueAt(currentRow, currentColumn).toString().contains(text)){
            		// select that cell
            		table.changeSelection(currentRow, currentColumn, false, false);
//            		System.out.println("FOUND");
            		return;
            	}
            }
            currentColumn = 0;
        }
	}
}
