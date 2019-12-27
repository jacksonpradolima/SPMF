package ca.pfv.spmf.welwindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

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
public class DownloadWindow extends JDialog {
	private static final long serialVersionUID = 1L;

	private JButton jButton1;
	private JProgressBar jProgressBar1;
	private boolean stateWindow = false;
	private int count = 0;

	static long byteCountRead = 0;

	private Thread workThead = null;

	boolean downloadFailed = false;

	public DownloadWindow(final String path, boolean isUpdate,
			final PluginWindow mainPlugin) {
		byteCountRead = 0;
		this.setAlwaysOnTop(true);
		this.setModal(true);
		initComponents();
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {

					if (isUpdate) {
						// *********** UPDATE A PLUGIN ***********

						String pluginName = (String) mainPlugin.jTableInstalledPlugins
								.getModel()
								.getValueAt(
										mainPlugin.jTableInstalledPlugins
												.getSelectedRow(),
										0);

						Plugin plugin = PluginManager
								.getInstalledPluginByNameFromList(pluginName);

						plugin.getRepositoryURL();
						// =================================
						// Create the URL:
						// http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/{pluginname}.jar
						String url1 = plugin.getRepositoryURL() + pluginName
								+ "/" + pluginName + ".jar";
						// =================================
						downLoadFromUrl(url1, pluginName + ".jar", path);
						
						workThead.join();
						
						if(downloadFailed){
							stateWindow = false;
							jButton1.setText("Cancel");
							jProgressBar1.setIndeterminate(false);
							jProgressBar1.setMinimum(jProgressBar1.getMaximum());
							jProgressBar1.setString(byteCountRead
									+ " bytes - download failed");
							setTitle("Download failed");
						}else{
							jProgressBar1.setIndeterminate(false);
							jProgressBar1.setMinimum(jProgressBar1.getMaximum());
							jProgressBar1.setString(byteCountRead + " bytes - completed");
							jButton1.setText("Done");
							setTitle("Download completed");
							
							// If we update
							PluginManager.updatePlugin(plugin);
						}


						// *****SFDLKSJDF:LSKJDF:LSKJDF:LSKDFJ:SLDFJKS:DLFKJSJKDLF:JKSDFL
						// *****SFDLKSJDF:LSKJDF:LSKJDF:LSKDFJ:SLDFJKS:DLFKJSJKDLF:JKSDFL
						// *****SFDLKSJDF:LSKJDF:LSKJDF:LSKDFJ:SLDFJKS:DLFKJSJKDLF:JKSDFL

						// TODO : WE DO NOT CHECK THE VERSION AND UPDATE IT IN
						// THE JTABLE

					} else {
						// *********** INSTALL A PLUGIN ***********

						String pluginName = (String) mainPlugin.jTablePlugins
								.getModel()
								.getValueAt(
										mainPlugin.jTablePlugins
												.getSelectedRow(),
										0);

						// =================================
						// Create the URL:
						// http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/{pluginname}.jar
						String url1 = PreferencesManager.getInstance()
								.getRepositoryURL()
								+ pluginName
								+ "/"
								+ pluginName + ".jar";
						// =================================

						downLoadFromUrl(url1, pluginName + ".jar", path);

						workThead.join();
						
						if(downloadFailed){
							stateWindow = false;
							jButton1.setText("Cancel");
							jProgressBar1.setIndeterminate(false);
							jProgressBar1.setMinimum(jProgressBar1.getMaximum());
							jProgressBar1.setString(byteCountRead
									+ " bytes - download failed");
							setTitle("Download failed");
						}else{
							Plugin plugin = PluginManager
									.getPluginByNameFromList(pluginName);
							// If we install
							PluginManager.installPlugin(plugin);

							// UPDATE THE JTABLE OF INSTALLED PLUGINS
							Object[] objectNew = new Object[] { plugin.getName(),
									plugin.getAuthor(), plugin.getCategory(),
									plugin.getVersion(), "Webpage" };
							mainPlugin.tableModelInstalledPlugins.addRow(objectNew);

							// UPDATE THE JTABLE OF PLUGINS FROM REPOSITORY
							for (int i = 0; i < mainPlugin.tableModelPlugins
									.getRowCount(); i++) {
								String name = (String) mainPlugin.tableModelPlugins
										.getValueAt(i, 0);
								if (name.equals(plugin.getName())) {
									mainPlugin.tableModelPlugins.removeRow(i);
									break;
								}
							}
							
							jProgressBar1.setIndeterminate(false);
							jProgressBar1.setMinimum(jProgressBar1.getMaximum());
							jProgressBar1.setString(byteCountRead + " bytes - completed");
							jButton1.setText("Done");
							setTitle("Download completed");

							mainPlugin.jButton2Install.setEnabled(false);
							mainPlugin.jTextAreaDescription.setText("");
						}

						
					}
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	private void initComponents() {

		jProgressBar1 = new JProgressBar();
		jProgressBar1.setStringPainted(true);
		jProgressBar1.setIndeterminate(true);
		jButton1 = new JButton();
		setTitle("Downloading plugin...");
		jButton1.setText("Cancel");

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(70, 70, 70)
								.addComponent(jProgressBar1,
										GroupLayout.PREFERRED_SIZE, 288,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap(71, Short.MAX_VALUE))
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addComponent(jButton1)
								.addGap(28, 28, 28)));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addGap(48, 48, 48)
						.addComponent(jProgressBar1,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED, 50,
								Short.MAX_VALUE).addComponent(jButton1)
						.addContainerGap()));

		jButton1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				stateWindow = false;
				setVisible(false);
			}
		});
		pack();
		setLocationRelativeTo(null);
	}

	public void downLoadFromUrl(String urlStr, String fileName, String savePath) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setConnectTimeout(2000);

			conn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			stateWindow = true;
			if (workThead == null) {
				workThead = new WorkThead();
				workThead.start();
			}

			InputStream inputStream = conn.getInputStream();

			byte[] getData = readInputStream(inputStream);

			File saveDir = new File(savePath);
			if (!saveDir.exists()) {
				saveDir.mkdir();
			}
			File file = new File(saveDir + File.separator + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(getData);
			fos.close();
			inputStream.close();
		} catch (IOException e) {
			downloadFailed = true;
		}
	}

	public static byte[] readInputStream(InputStream inputStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
			byteCountRead += len;
		}
		bos.close();
		return bos.toByteArray();
	}

	class WorkThead extends Thread {
		@Override
		public void run() {
			while (count < 100) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();// Restore interrupted state...
					Thread.currentThread().interrupt();
				}
				if (stateWindow) {
					count++;
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							jProgressBar1.setValue(count);
							jProgressBar1.setString(byteCountRead + " bytes read");
						}
					});
				}
			}


		}
	}

}
