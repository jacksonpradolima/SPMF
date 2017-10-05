package ca.pfv.spmf.gui.patternvizualizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

public class JTableSearchAndHighlight extends JFrame {

	   private JTextField searchField;
	   private JTable table;
	   private JPanel panel;
	   private JScrollPane scroll;

	   public JTableSearchAndHighlight() {

	     initializeInventory();
	   }

	private void initializeInventory() {

	    panel = new JPanel();

	    searchField = new JTextField();

	    panel.setLayout(null);

	    final String[] columnNames = {"Name", "Surname", "Age"};

	    final Object[][] data = {{"Jhon", "Java", "23"}, {"Stupid", "Stupido", "500"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Michael", "Winnie", "20"}, {"Winnie", "Thepoor", "23"},
	                            {"Max", "Dumbass", "10"}, {"Melanie", "Martin", "500"},
	                            {"Jollibe", "Mcdonalds", "15"}};

	    table = new JTable(data, columnNames);
	    table.setColumnSelectionAllowed(true);
	    table.setRowSelectionAllowed(true);

	    scroll = new JScrollPane(table);
	    scroll.setBounds(0, 200, 900, 150);

	    searchField.setBounds(10, 100, 150, 20);
	    searchField.addActionListener(new ActionListener() {

	        public void actionPerformed(ActionEvent e) {

	            String value = searchField.getText();

	            for (int row = 0; row <= table.getRowCount() - 1; row++) {

	                for (int col = 0; col <= table.getColumnCount() - 1; col++) {

	                    if (value.equals(table.getValueAt(row, col))) {

	                        // this will automatically set the view of the scroll in the location of the value
	                        table.scrollRectToVisible(table.getCellRect(row, 0, true));

	                        // this will automatically set the focus of the searched/selected row/value
	                        table.setRowSelectionInterval(row, row);

	                        for (int i = 0; i <= table.getColumnCount() - 1; i++) {

	                            table.getColumnModel().getColumn(i).setCellRenderer(new HighlightRenderer());
	                        }
	                    }
	                }
	            }
	        }
	    });

	    panel.add(searchField);
	    panel.add(scroll);

	    getContentPane().add(panel);

	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setTitle("Inventory Window");
	    setSize(900, 400);
	    setLocationRelativeTo(null);
	    setVisible(true);
	}

	private class HighlightRenderer extends DefaultTableCellRenderer {

	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

	        // everything as usual
	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	        // added behavior
	        if(row == table.getSelectedRow()) {

	            // this will customize that kind of border that will be use to highlight a row
	            setBorder(BorderFactory.createMatteBorder(2, 1, 2, 1, Color.BLACK));
	        }

	        return this;
	    }
	}

	public static void main(String[] args) {

	    SwingUtilities.invokeLater(new Runnable() {

	        public void run() {

	            new JTableSearchAndHighlight();
	        }
	    });
	  }
	}