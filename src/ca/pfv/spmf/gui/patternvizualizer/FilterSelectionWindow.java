package ca.pfv.spmf.gui.patternvizualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.pfv.spmf.gui.patternvizualizer.filters.AbstractFilter;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterEqualBoolean;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterEqualDouble;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterEqualInteger;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterGreaterThanDouble;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterGreaterThanInteger;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterLessThanDouble;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterLessThanInteger;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterStringContains;
import ca.pfv.spmf.gui.patternvizualizer.filters.FilterStringNotContains;

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
 * This is a simple user interface for filtering patterns shown by the pattern
 * vizualizer window.
 * 
 * @author Philippe Fournier-Viger
 */
public class FilterSelectionWindow extends JDialog {

	/** The text field for entering a value for the current filter */
	private JTextField textField;

	/** List of table column names */
	Vector<String> columnNames = null;

	/** List of table column classes (Integer, Double, String) */
	Vector<Class> columnClasses = null;

	/** List of current filters **/
	PatternTableRowFilters rowfilters;

	/** The list of available filters for showing in the combo box */
	List<Class> listComboBoxFilters = new ArrayList<Class>();

	/** the combo box to let the user choose a filter*/
	private JComboBox comboBoxFilters;

	/** the combo box for letting the user choose a column */
	private JComboBox comboBoxColumns; 
	
	/** the pattern vizualizer window that has created that filter section window */
	private PatternVizualizer patternVisualizer;

	/**
	 * Constructor of the filter selection window
	 * @param columnNames a vector of column names
	 * @param columnClasses a vector of classes corresponding to the columns (e.g. Integer.class)
	 * @param rowFilters the list of filters currently applied on the patterns
	 * @param patternVisualizer the pattern vizualization window that has created that filter selection window
	 */
	public FilterSelectionWindow(Vector<String> columnNames,
			Vector<Class> columnClasses, PatternTableRowFilters rowFilters, PatternVizualizer patternVisualizer) {
		super(patternVisualizer);

		// save the variables
		this.columnNames = columnNames;
		this.columnClasses = columnClasses;
		this.rowfilters = rowFilters;
		this.patternVisualizer = patternVisualizer;

		// set this window as visible
		setVisible(true);
		// show this window as modal
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		// set title and layout
		setTitle("Add a filter...");
		getContentPane().setLayout(null);

		// Create the Cancel button
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				dispose();
			}
		});
		btnCancel.setBounds(500, 143, 89, 23);
		getContentPane().add(btnCancel);

		// Create the Add Filter button
		JButton btnAddFilter = new JButton("Add filter");
		btnAddFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addFilter();
			}
		});
		btnAddFilter.setBounds(401, 143, 89, 23);
		getContentPane().add(btnAddFilter);
		btnAddFilter.setEnabled(false);


		// Create the labels
		JLabel lblSelectAColumn = new JLabel("Choose a column:");
		lblSelectAColumn.setBounds(10, 22, 258, 14);
		getContentPane().add(lblSelectAColumn);

		JLabel lblSelectAConstraint = new JLabel("Choose a constraint:");
		lblSelectAConstraint.setBounds(10, 74, 149, 14);
		getContentPane().add(lblSelectAConstraint);

		// Create the combo boxes for filters
		comboBoxFilters = new JComboBox();
		comboBoxFilters.setBounds(68, 99, 323, 23);
		getContentPane().add(comboBoxFilters);

		// Create the combo boxes for columns
		comboBoxColumns = new JComboBox(columnNames);
		comboBoxColumns.setBounds(68, 42, 200, 23);
		getContentPane().add(comboBoxColumns);

		// Add a listener to detect when the user select a column
		comboBoxColumns.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				//Refresh the como box of filters
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					refreshComboBoxFilters(columnNames, columnClasses,
							comboBoxFilters, comboBoxColumns);
				} else {
					// do nothing (shoud not happen)...
				}
			}
		});

		// Fill the combo box of filters  for the first time
		refreshComboBoxFilters(columnNames, columnClasses, comboBoxFilters, comboBoxColumns);

		// Create the text field for enterinng the value for the filter
		textField = new JTextField();
		DocumentListener documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				printIt(documentEvent);
			}

			public void insertUpdate(DocumentEvent documentEvent) {
				printIt(documentEvent);
			}

			public void removeUpdate(DocumentEvent documentEvent) {
				printIt(documentEvent);
			}

			private void printIt(DocumentEvent documentEvent) {
				// When the user type in the jtext field, if the text is not empty
				// we will show the button "add filter" as enabled.
				if (textField.getText().isEmpty() == false) {
					btnAddFilter.setEnabled(true);
				} else {
					// otherwise, the button is disabled
					btnAddFilter.setEnabled(false);
				}
			}
		};
		textField.getDocument().addDocumentListener(documentListener);

		textField.setBounds(401, 100, 200, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		textField.setEnabled(true);

		// set the size of the filter selection window
		setSize(632, 228);
	}
	
	/**
	 * This method is called whe the user click on the button "Add filter...".
	 * It adds the filter to the list of filter, and then close the window.	 */
	private void addFilter() {
		// Get the class corresponding to the chosen filter
		Class selectedFilterClass = listComboBoxFilters.get(comboBoxFilters.getSelectedIndex());
		// Get the value to be used for the filter
		String valueString = textField.getText();
		// Get the  index of the column to be used by the filter
		int columnID = comboBoxColumns.getSelectedIndex();
		// Get the column  to be used by the filter
		String columnName = (String)comboBoxColumns.getSelectedItem();
		
		// Create the filter
		try{
			AbstractFilter filter = null;
			if(selectedFilterClass.equals(FilterEqualBoolean.class)){
				boolean value = Boolean.parseBoolean(valueString);
				filter = new FilterEqualBoolean(value, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterEqualDouble.class)){
				double value = Double.parseDouble(valueString);
				filter = new FilterEqualDouble(value, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterEqualInteger.class)){
				int value = Integer.parseInt(valueString);
				filter = new FilterEqualInteger(value, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterGreaterThanDouble.class)){
				double value = Double.parseDouble(valueString);
				filter = new FilterGreaterThanDouble(value, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterGreaterThanInteger.class)){
				int value = Integer.parseInt(valueString);
				filter = new FilterGreaterThanInteger(value, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterLessThanDouble.class)){
				double value = Double.parseDouble(valueString);
				filter = new FilterLessThanDouble(value, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterLessThanInteger.class)){
				int value = Integer.parseInt(valueString);
				filter = new FilterLessThanInteger(value, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterStringContains.class)){
				filter = new FilterStringContains(valueString, columnName, columnID);
			}else if (selectedFilterClass.equals(FilterStringNotContains.class)){
				filter = new FilterStringNotContains(valueString, columnName, columnID);
			}
			// Add the filter to the list of current filters
			rowfilters.filters.add(filter);
		}catch(Exception e){
			// If there is some error because the user has entered an invalid value for the given
			// filter, then a dialog is shown to the user.
			JOptionPane.showMessageDialog(null,
		            "Invalid value in text field. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
			return;
		}
		// hide this window
		setVisible(false);
		
		// notify the pattern vizualizer window that the filters have been updated
		// so that the table can be refreshed using the new filters
		patternVisualizer.filtersHaveBeenUpdated();
	}

	/**
	 * This method fill the list of available filters for the chosen column in the combo
	 * box for filters
	 * @param columnNames the list of column names
	 * @param columnClasses the list of classes corresponding to columns
	 * @param comboBoxFilters the combo box for filters
	 * @param comboBoxColumns the combo box for colums
	 */
	private void refreshComboBoxFilters(Vector<String> columnNames,
			Vector<Class> columnClasses, JComboBox comboBoxFilters,
			JComboBox comboBoxColumns) {
		
		// get the selected column
		String selectedColumnName = (String) comboBoxColumns.getSelectedItem();

		// clear combobox filters
		comboBoxFilters.removeAllItems();
		listComboBoxFilters.clear();

		// find the class of the chosen column
		Class selectedColumnClass = null;
		for (int i = 0; i < columnNames.size(); i++) {
			if (columnNames.get(i).equals(selectedColumnName)) {
				selectedColumnClass = columnClasses.get(i);
				break;
			}
		}

		// If the selected column is of type integer,
		// then we make available the filters for integer values in the combo box.
		if (selectedColumnClass.equals(Integer.class)) {
			listComboBoxFilters.add(FilterEqualInteger.class);
			comboBoxFilters.addItem(FilterEqualInteger.getFilterGenericName());
			listComboBoxFilters.add(FilterGreaterThanInteger.class);
			comboBoxFilters.addItem(FilterGreaterThanInteger.getFilterGenericName());
			listComboBoxFilters.add(FilterLessThanInteger.class);
			comboBoxFilters.addItem(FilterLessThanInteger.getFilterGenericName());
		} else if (selectedColumnClass.equals(Double.class)) {
			// If the selected column is of type double,
			// then we make available the filters for doule values in the combo box.
			listComboBoxFilters.add(FilterEqualDouble.class);
			comboBoxFilters.addItem(FilterEqualDouble.getFilterGenericName());
			listComboBoxFilters.add(FilterGreaterThanDouble.class);
			comboBoxFilters.addItem(FilterGreaterThanDouble
					.getFilterGenericName());
			listComboBoxFilters.add(FilterLessThanDouble.class);
			comboBoxFilters
					.addItem(FilterLessThanDouble.getFilterGenericName());
		} else if (selectedColumnClass.equals(String.class)) {
			// If the selected column is of type string,
			// then we make available the filters for string values in the combo box.
			listComboBoxFilters.add(FilterStringContains.class);
			comboBoxFilters
					.addItem(FilterStringContains.getFilterGenericName());
			listComboBoxFilters.add(FilterStringNotContains.class);
			comboBoxFilters.addItem(FilterStringNotContains
					.getFilterGenericName());
		} else if (selectedColumnClass.equals(Boolean.class)) {
			// If the selected column is of type boolean,
			// then we make available the filters for boolean values in the combo box.
			listComboBoxFilters.add(FilterEqualBoolean.class);
			comboBoxFilters.addItem(FilterEqualBoolean.getFilterGenericName());
		}
	}
}
