package ca.pfv.spmf.welwindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import ca.pfv.spmf.gui.Main;
import ca.pfv.spmf.gui.PreferencesManager;
/*
 * Copyright (c) 2008-2019 Philippe Fournier-Viger
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
public class PluginWindow extends JDialog {
	private static final long serialVersionUID = 1L;

	JButton jButton2Install;
	private JButton jButton2Update;
	private JButton jButton3Remove;
	private JButton jButton5ConnectDefault;
	private JButton jButton5Connect;
	private JLabel jLabelRemoteRepository;
	private JLabel jLabelDescription;
	private JLabel jLabelDescriptionInstalled;
	private JLabel jLabelPlugins;
	private JLabel jLabelInstalledPlugins;
	private JLabel jLabelInstalledPluginsLocalComputer;
	private JPanel jPanel1;
	
	JTextArea jTextAreaDescription;
	JTextArea jTextAreaDescriptionInstalled;
	
	// Plugins from repository
	DefaultTableModel tableModelPlugins;
	JTable jTablePlugins;
	private JScrollPane jScrollPane1;
	
	// Installed plugins 
	DefaultTableModel tableModelInstalledPlugins;
	JTable jTableInstalledPlugins;
	private JScrollPane jScrollPaneInstalled;
	

	public PluginWindow(Welcome welcome) {
		this.setAlwaysOnTop(true);
		this.setModal(true);

		initComponents();
	}

	private void initComponents() {
		setTitle("SPMF-V." + Main.SPMF_VERSION + "-Plugin Manager");
		this.setLocation(400, 100);
		this.setSize(975, 619);
		this.setResizable(false);

		jLabelRemoteRepository = new JLabel();
		jLabelDescription = new JLabel();
		jLabelDescriptionInstalled = new JLabel();
		jLabelPlugins = new JLabel();
		jLabelInstalledPlugins = new JLabel();
		jLabelInstalledPluginsLocalComputer = new JLabel();
		
		jButton2Install = new JButton();
		jButton2Install.setIcon(new ImageIcon(PluginWindow.class.getResource("ico_down.gif")));
		
		jButton2Update = new JButton();
		jButton2Update.setIcon(new ImageIcon(PluginWindow.class.getResource("ico_update.gif")));
		
		jButton3Remove = new JButton();
		jButton3Remove.setIcon(new ImageIcon(PluginWindow.class.getResource("ico_remove.gif")));

		jButton5Connect = new JButton();
		jButton5ConnectDefault = new JButton();
		
		jTextAreaDescription = new JTextArea();
		jTextAreaDescription.setEnabled(true);
		jTextAreaDescription.setEditable(false);
		
		jTextAreaDescriptionInstalled = new JTextArea();
		jTextAreaDescriptionInstalled.setEnabled(true);
		jTextAreaDescriptionInstalled.setEditable(false);
		
		jPanel1 = new JPanel();
		
		//================   Table plugins
		{
			
			tableModelPlugins = new DefaultTableModel();
			tableModelPlugins.addColumn("Name");
			tableModelPlugins.addColumn("Author");
			tableModelPlugins.addColumn("Category");
			tableModelPlugins.addColumn("Version");
			tableModelPlugins.addColumn("Documentation");
	
			jTablePlugins = new JTable(tableModelPlugins) {
				private static final long serialVersionUID = 3834308709152773954L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return column == 4;
				}
			};

			jTablePlugins.setAutoCreateRowSorter(true);

			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(jTablePlugins);
	
			jTablePlugins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTablePlugins.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
		        public void valueChanged(ListSelectionEvent event) {
		            // do some actions here, for example
		            // print first column value from selected row
					// if the user has selected something
					if (jTablePlugins.getSelectedRow() != -1) {
						String pluginName = (String) jTablePlugins.getModel().getValueAt(jTablePlugins.getSelectedRow(), 0);
						Plugin plugin = PluginManager.getPluginByNameFromList(pluginName);
						
						jTextAreaDescription.setText(plugin.getDescription());

						
						if (PluginManager.isPluginInstalled(plugin.getName())) {
							jButton2Install.setEnabled(false);
						}else{
							jButton2Install.setEnabled(true);
						}
						
						jTableInstalledPlugins.clearSelection();
					}else{
						jButton2Install.setEnabled(false);
						jTextAreaDescription.setText("");
					}
		        }
		    });
			
			jTablePlugins.getColumnModel().getColumn(4).setCellRenderer(new TableButtonRenderer());
			jTablePlugins.getColumnModel().getColumn(4).setCellEditor(new TableButtonEditor(new JCheckBox()));
		}
		//===================================================
		
		//================   Table INSTALLED plugins
		{
			tableModelInstalledPlugins = new DefaultTableModel();
			tableModelInstalledPlugins.addColumn("Name");
			tableModelInstalledPlugins.addColumn("Author");
			tableModelInstalledPlugins.addColumn("Category");
			tableModelInstalledPlugins.addColumn("Version");
			tableModelInstalledPlugins.addColumn("Documentation");

	
			jTableInstalledPlugins = new JTable(tableModelInstalledPlugins) {
				@Override
				public boolean isCellEditable(int row, int column) {
					return column == 4;
				}
			};
	
			jTableInstalledPlugins.setAutoCreateRowSorter(true);

			fillInstalledPluginTable();
			
			jTableInstalledPlugins.setShowGrid(false);
	
			jScrollPaneInstalled = new JScrollPane();
			jScrollPaneInstalled.setViewportView(jTableInstalledPlugins);
	
			jTableInstalledPlugins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableInstalledPlugins.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					// TODO Auto-generated method stub
					// if the user has selected something
					if (jTableInstalledPlugins.getSelectedRow() != -1) {
						
						String pluginName = (String) jTableInstalledPlugins.getModel().getValueAt(jTableInstalledPlugins.getSelectedRow(), 0);
						Plugin plugin = PluginManager.getInstalledPluginByNameFromList(pluginName);
						jTextAreaDescriptionInstalled.setText(plugin.getDescription());
						
						jButton2Update.setEnabled(true);
						jButton3Remove.setEnabled(true);
						jTablePlugins.clearSelection();
					}else{
						jButton3Remove.setEnabled(false);
						jButton2Update.setEnabled(false);
						jTextAreaDescriptionInstalled.setText("");
						
					}
				}
			});
			
			jTableInstalledPlugins.getColumnModel().getColumn(4).setCellRenderer(new TableButtonRenderer());
			jTableInstalledPlugins.getColumnModel().getColumn(4).setCellEditor(new TableButtonEditor(new JCheckBox()));

		}
		//===================================================

		jLabelRemoteRepository.setText("Plugin repository: ");
		jLabelDescription.setText("Selected plugin description:");
		jLabelDescriptionInstalled.setText("Selected plugin description:");
		jLabelPlugins.setText("Available plugins (that have not been installed):");
		jLabelInstalledPlugins.setText("Installed Plugins:");
		jLabelInstalledPluginsLocalComputer.setText("Local computer:");
		jTextAreaDescription.setLineWrap(true);
		jTextAreaDescriptionInstalled.setLineWrap(true);

		jButton2Install.setText("Install");
		jButton2Install.setEnabled(false);
		jButton2Install.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButtonInstallActionPerformed(evt);
			}
		});

		jButton2Update.setText("Update");
		jButton2Update.setEnabled(false);
		jButton2Update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButtonUpdateActionPerformed(evt);
			}
		});

		jButton3Remove.setText("Remove");
		jButton3Remove.setEnabled(false);
		jButton3Remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButton3RemoveActionPerformed(evt);
			}
		});

		jButton5Connect.setText("other repository");
		jButton5Connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				boolean succeed = fillPluginTable(evt, false);
				if(succeed){
					jButton5Connect.setVisible(false);
					jButton5ConnectDefault.setVisible(false);
				}
			}
		});
		
		jButton5ConnectDefault.setText("default repository");
		jButton5ConnectDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				boolean succeed = fillPluginTable(evt, true);
				if(succeed){
					jButton5Connect.setVisible(false);
					jButton5ConnectDefault.setVisible(false);
				}
			}
		});


		jButton5Connect.setSize(200, 20);
		jButton5Connect.setLocation(360, 20);
		getContentPane().add(jButton5Connect);
		

		jButton5ConnectDefault.setSize(200, 20);
		jButton5ConnectDefault.setLocation(145, 20);
		getContentPane().add(jButton5ConnectDefault);

		jLabelRemoteRepository.setBounds(25, 20, 180, 20);
		getContentPane().add(jLabelRemoteRepository);

		
		// -------- Description - available plugins
		jLabelDescription.setBounds(760, 70, 300, 20);
		getContentPane().add(jLabelDescription);

		jTextAreaDescription.setSize(200, 120);
		jTextAreaDescription.setLocation(755, 90);
		getContentPane().add(jTextAreaDescription);
		
		// -------- Description  - installed plugins
		jLabelDescriptionInstalled.setBounds(760, 360, 300, 20);
		getContentPane().add(jLabelDescriptionInstalled);

		jTextAreaDescriptionInstalled.setSize(200, 120);
		jTextAreaDescriptionInstalled.setLocation(755, 380);
		getContentPane().add(jTextAreaDescriptionInstalled);

		// ===== Table plugins ======
		
		jLabelPlugins.setBounds(40, 50, 300, 20);
		getContentPane().add(jLabelPlugins);
		
		jScrollPane1.setSize(700, 220);
		jScrollPane1.setLocation(40, 75);
		getContentPane().add(jScrollPane1);
		//=================================
		

		jButton2Install.setSize(140, 30);
		jButton2Install.setLocation(315, 300);
		getContentPane().add(jButton2Install);

		// ===== Table installed plugins ======
		jLabelInstalledPluginsLocalComputer.setBounds(25, 320, 300, 20);
		getContentPane().add(jLabelInstalledPluginsLocalComputer);
		
		jLabelInstalledPlugins.setBounds(40, 340, 300, 20);
		getContentPane().add(jLabelInstalledPlugins);
		
		jScrollPaneInstalled.setSize(700, 180);
		jScrollPaneInstalled.setLocation(40, 360);
		getContentPane().add(jScrollPaneInstalled);
		//=================================

		jButton2Update.setSize(140, 30);
		jButton2Update.setLocation(410, 540);
		getContentPane().add(jButton2Update);

		jButton3Remove.setSize(140, 30);
		jButton3Remove.setLocation(260, 540);
		getContentPane().add(jButton3Remove);
		
		// HIDE REPOSITORY OBJECTS
		jLabelPlugins.setVisible(false);
		jScrollPane1.setVisible(false);
		jButton2Install.setVisible(false);
		jLabelDescription.setVisible(false);
//		jButton4Documentation.setVisible(false);
		jTextAreaDescription.setVisible(false);

		getContentPane().add(jPanel1);
		this.setVisible(true);
	}

	 void fillPluginTable() {
		 try {
				//  Load the plugin names from repository
				PluginManager.pluginInit();
				
				if (tableModelPlugins.getRowCount() >0) {
					// Delete all rows
					int rowCount = tableModelPlugins.getRowCount();
					for (int i = rowCount - 1; i >= 0; i--) {
						tableModelPlugins.removeRow(i);
					}
					
				}
				
				// count number of installed plugins in this list
				List<Plugin> notInstalledYet = new ArrayList<Plugin>();
				for (Plugin plugin : PluginManager.listPlugin) {
					if(!PluginManager.isPluginInstalled(plugin.getName())){
						notInstalledYet.add(plugin);
					}
				}

				for (int i = 0; i < notInstalledYet.size(); i++) {
					Object[] object = new Object[] {
							notInstalledYet.get(i).getName(),
							notInstalledYet.get(i).getAuthor(),
							notInstalledYet.get(i).getCategory(),
							notInstalledYet.get(i).getVersion(),
							"Webpage"};
					
					tableModelPlugins.addRow(object);
				}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Network error : " + e.getMessage());
		}
		
	}
	
	void fillInstalledPluginTable() {
		for (int i = 0; i < PluginManager.listInstalledPlugins.size(); i++) {
			Object[] object = new Object[] {
					PluginManager.listInstalledPlugins.get(i).getName(),
					PluginManager.listInstalledPlugins.get(i).getAuthor(),
					PluginManager.listInstalledPlugins.get(i).getCategory(),
					PluginManager.listInstalledPlugins.get(i).getVersion(),
					"Webpage"};
			tableModelInstalledPlugins.addRow(object);
			
			
		}
	}

	private void jButtonInstallActionPerformed(ActionEvent evt) {

		new DownloadWindow(PluginManager.getPluginFolderPath()
				.getAbsolutePath(), false, this).setVisible(true);
		
	}

	private void jButtonUpdateActionPerformed(ActionEvent evt) {

		new DownloadWindow(PluginManager.getPluginFolderPath()
				.getAbsolutePath(), true, this).setVisible(true);
		
		jButton3Remove.setEnabled(false);
		jButton2Update.setEnabled(false);
		
	}

	private void jButton3RemoveActionPerformed(ActionEvent evt) {

		try {
			String pluginName = (String) jTableInstalledPlugins.getModel().getValueAt(jTableInstalledPlugins.getSelectedRow(), 0);
			
			// Remove the plugin
			PluginManager.removePlugin(pluginName);

			jButton3Remove.setEnabled(false);
			jButton2Update.setEnabled(false);
			
			
			// REMOVE FROM TABLE OF INSTALLED PLUGIN
			tableModelInstalledPlugins.removeRow(jTableInstalledPlugins.getSelectedRow());
			
			// ADD TO TABLE OF AVAILABLE PLUGINS
			// IF NECESSARY 
			Plugin pluginFromRepository = PluginManager.getPluginByNameFromList(pluginName);
			if(pluginFromRepository != null){
				Object[] object = new Object[] {
						pluginFromRepository.getName(),
						pluginFromRepository.getAuthor(),
						pluginFromRepository.getCategory(),
						pluginFromRepository.getVersion(), "Webpage"};
				tableModelPlugins.addRow(object);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	private void jButton4WebpageActionPerformed(ActionEvent evt) {
		String pluginName = (String) jTablePlugins.getModel().getValueAt(jTablePlugins.getSelectedRow(), 0);
		Plugin plugin = PluginManager.getPluginByNameFromList(pluginName);
		
		// =================================
		// Create the URL:
		// http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/documentation.php
		String url2 = plugin.getUrlOfDocumentation();
		// =================================
		// String url2 = Plugins.url2.replace("{pluginname}",
		if (Desktop.isDesktopSupported()) {
			try {
				URI uri = URI.create(url2);
				Desktop dp = Desktop.getDesktop();
				if (dp.isSupported(Desktop.Action.BROWSE)) {
					dp.browse(uri);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Null!");
		}
	}

	/**
	 * 
	 * @param evt
	 * @param connectToDefault
	 * @return false if cannot establish connection
	 */
	private boolean fillPluginTable(ActionEvent evt, boolean connectToDefault) {

		String choice; 
		if(!connectToDefault){
			 choice = JOptionPane.showInputDialog(this,
					"Enter the URL of a plugin repository.",
					PluginManager.DEFAULT_PLUGIN_REPOSITORY);
			
		}else{
			choice = PluginManager.DEFAULT_PLUGIN_REPOSITORY; 
		}
		if (choice != null) {
			// check if the URL is really a repository
			boolean isARepository = PluginManager.checkIfURLisAPluginRepository(choice);
			if (isARepository) {
				// Remember the URL so that if we close the software we still
				// remember it.
				PreferencesManager.getInstance().setRepositoryURL(choice);

				// Refresh the list of plugins in the JTABLE...
				fillPluginTable();
				
				jLabelPlugins.setVisible(true);
				jScrollPane1.setVisible(true);
				jButton2Install.setVisible(true);
				jLabelDescription.setVisible(true);
				jTextAreaDescription.setVisible(true);
				
				jButton5Connect.setEnabled(false);
				jButton5ConnectDefault.setEnabled(false);
				
				return true;
			} else {
				JOptionPane.showMessageDialog(this,
						"Cannot establish connection!");
				return false;
			}
		}
		return false;
		
	}
	
	//------------------------------- Code for buttons in tables  (cell editor)
	
	public class TableButtonEditor extends DefaultCellEditor {
		private JButton button;
		private String label;
		private boolean clicked;
		private int row;
		private int col;
		private JTable table;

		public TableButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.table = table;
			this.row = row;
			this.col = column;

			button.setForeground(Color.black);
			button.setBackground(UIManager.getColor("Button.background"));
			label = (value == null) ? "" : value.toString();
			button.setText(label);
			clicked = true;
			return button;
		}

		public Object getCellEditorValue() {
			if (clicked) {
				// ============= OPEN THE DOCUMENTATION ==============
				String pluginName = (String) table.getModel().getValueAt(table.getSelectedRow(), 0);
				Plugin plugin = null;
				
				if(table == jTablePlugins){
					plugin = PluginManager.getPluginByNameFromList(pluginName);
				}else if (table == jTableInstalledPlugins){
					plugin = PluginManager.getInstalledPluginByNameFromList(pluginName);
				}
				
				// =================================
				// Create the URL:
				// http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/documentation.php
				String url2 = plugin.getUrlOfDocumentation();
				// =================================
				// String url2 = Plugins.url2.replace("{pluginname}",
				if (Desktop.isDesktopSupported()) {
					try {
						URI uri = URI.create(url2);
						Desktop dp = Desktop.getDesktop();
						if (dp.isSupported(Desktop.Action.BROWSE)) {
							dp.browse(uri);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Null!");
				}
			}
			clicked = false;
			return new String(label);
		}

		public boolean stopCellEditing() {
			clicked = false;
			return super.stopCellEditing();
		}
	}
	
	//------------------------------- Code for buttons in tables  (cell renderer)
	
	class TableButtonRenderer extends JButton implements TableCellRenderer {
		/**
		 * serial iD
		 */
		private static final long serialVersionUID = 2276033826743007852L;

		public TableButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			setForeground(Color.black);
			setBackground(UIManager.getColor("Button.background"));
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}


}
