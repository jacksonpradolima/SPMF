package ca.pfv.spmf.welwindow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
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
public class PluginManager {
	
	public static final String DEFAULT_PLUGIN_REPOSITORY = "http://www.philippe-fournier-viger.com/spmf/plugins/";
	
	/** The list of all available plugins in the current repository **/
	protected static List<Plugin> listPlugin = new ArrayList<Plugin>();
	
	/** The list of plugin names */
	protected static List<String> pluginNames;

	/** The list of installed plugins */
	protected static List<Plugin> listInstalledPlugins = null;

	static {
		// Load the list of installed plugin when the software is started
		loadListOfInstalledPluginsFromFile();
	}
	
	private PluginManager(){
		
	}

	public static Plugin getPluginInfoFromRepository(String pluginName) {

		// =================================
		// Create the URL:
		// "http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/info.txt"
		String url = PreferencesManager.getInstance().getRepositoryURL() + pluginName + "/info.txt";

		String param = null;

		String encodingFormat = "UTF-8";

		BufferedReader read = null;
		Plugin p = new Plugin();

		try {
			URL realurl = new URL(url + "?" + param);
			URLConnection connection = realurl.openConnection();
			connection.connect();

			read = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), encodingFormat));
			String line;
			while ((line = read.readLine()) != null) {
				if (line.indexOf("#NAME") == 0) {
					p.setName(line.substring(7));

				}
				if (line.indexOf("#IMPLEMENTATION_AUTHOR") == 0) {
					p.setAuthor(line.substring(23));

				}

				if (line.indexOf("#CATEGORY") == 0) {
					p.setCategory(line.substring(10));

				}
				if (line.indexOf("#PLUGIN_VERSION") == 0) {
					p.setVersion(line.substring(17));

				}
				if (line.indexOf("#DESCRIPTION") == 0) {
					p.setDescription(line.substring(14));

				}

				if (line.indexOf("#URL_OF_DOCUMENTATION") == 0) {
					p.setUrlOfDocumentation(line.substring(23));

				}
				
				if (line.indexOf("#INPUT_FILE_TYPES") == 0) {
					String lineInput = line.substring(18);
					String[] splitByComma = lineInput.split(",");

					for(String type : splitByComma){
						 type = type.substring(1, type.length()-1);
						 p.getInputFileTypes().add(type);
					}
				}
				
				if (line.indexOf("#OUTPUT_FILE_TYPES") == 0) {
					String lineOutput = line.substring(18);
					String[] splitByComma = lineOutput.split(",");

					for(String type : splitByComma){
						 type = type.substring(1, type.length()-1);
						 p.getOutputFileTypes().add(type);
					}
				}
				
				if (line.indexOf("#NUMBER_OF_PARAMETERS") == 0) {
					int count = Integer.parseInt(line.substring(23));
					p.setParameterCount(count);
				}
				
				if (line.indexOf("#PARAMETER") == 0) {
					int position = line.indexOf(": ");
					String substring = line.substring(position+2);
					//System.err.println(substring);
					// "minimum transaction length", "(e.g. 3)", INTEGER, false
					String[] split = substring.split("\"");
					String name = split[1];
					String example = split[3];
					
					String rest = split[4]; 
					//, INTEGER, false
					
					String[] restSplit = split[4].split(", "); 
					
					String className = restSplit[1];
					String isOptionalSring = restSplit[2];
					
					Class parameterType = null;
					switch(className){
						case "INTEGER": 
							parameterType = Integer.class;
							break;
						case "BOOLEAN": 
							parameterType = Boolean.class;
							break;
						case "STRING": 
							parameterType = String.class;
							break;
						case "FLOAT": 
							parameterType = String.class;
							break;
						case "DOUBLE": 
							parameterType = Double.class;
							break;
						case "LONG": 
							parameterType = Long.class;
							break;
					}
					boolean isOptional = Boolean.parseBoolean(isOptionalSring);
					DescriptionOfParameter parameter = new DescriptionOfParameter(name, example, parameterType, isOptional);
					p.getParameters().add(parameter);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (read != null) {
				try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Save information about the respository where this plugin was read
		// from.
		p.setRepositoryURL(PreferencesManager.getInstance().getRepositoryURL());
		p.setRepositoryPluginFolder(pluginName);

		return p;
	}

	public static void pluginInit() throws UnsupportedEncodingException, IOException {
		// FIRST, get the list of plugins
		// =================================
		// Create the URL:
		// "http://www.philippe-fournier-viger.com/spmf/plugins/list_of_plugins.txt"
		String urlPluginNames = PreferencesManager.getInstance()
				.getRepositoryURL() + "/list_of_plugins.txt";
		pluginNames = getPluginNamesFromRepository(urlPluginNames, "UTF8", null);

		// EMPTY THE LIST OF PLUGIN
		listPlugin.clear();

		// Load the plugins
		for (String pluginName : pluginNames) {
			Plugin plugin = getPluginInfoFromRepository(pluginName);
			listPlugin.add(plugin);
		}
	}

	// SengGetNames
	private static List<String> getPluginNamesFromRepository(String url,
			String encodingFormat, String param) throws UnsupportedEncodingException, IOException {
		List<String> pluginNames = new ArrayList<String>();
		BufferedReader read = null;
		URL realurl = new URL(url + "?" + param);
		URLConnection connection = realurl.openConnection();
		connection.connect();
//			Map<String, List<String>> map = connection.getHeaderFields();
		// for (String key : map.keySet()) {
		// }
		read = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), encodingFormat));
		String line;
		while ((line = read.readLine()) != null) {
			if (line.length() >= 1) {
				pluginNames.add(line);
			}
		}

		return pluginNames;
	}

	/**
	 * Check if a URL is a plugin repository
	 * 
	 * @param urlName
	 *            a URL
	 * @return true if it is a plugin repository
	 */
	public static boolean checkIfURLisAPluginRepository(String urlName) {
		// FIRST, get the list of plugins
		// =================================
		// Create the URL:
		// "http://www.philippe-fournier-viger.com/spmf/plugins/list_of_plugins.txt"
		String urlPluginNames = urlName + "/list_of_plugins.txt";
		// =================================

		try {

			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			// HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con = (HttpURLConnection) new URL(urlPluginNames)
					.openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Install a plugin
	 * 
	 * @param plugin
	 *            the plugin
	 */
	public static void installPlugin(Plugin plugin) {
		// Add the plugin to the list of installed plugins.
		listInstalledPlugins.add(plugin);

		// Save the list of installed plugins to file
		saveListOfInstalledPluginsToFile();
	}

	/**
	 * Update a plugin
	 * 
	 * @param newPlugin
	 *            the plugin
	 */
	public static void updatePlugin(Plugin newPlugin) {
		// Replace the old plugin by the updated version in the list of
		// installed plugins
		for (int i = 0; i < listInstalledPlugins.size(); i++) {
			Plugin oldPlugin = listInstalledPlugins.get(i);
			if (oldPlugin.getName().equals(newPlugin.getName())) {
				listInstalledPlugins.set(i, newPlugin);
			}
		}

		// Save the list of installed plugins to file
		saveListOfInstalledPluginsToFile();
	}

	/**
	 * Uninstall a plugin
	 * 
	 * @param pluginName
	 */
	public static void removePlugin(String pluginName) {
		// Delete the plugin (the file)
		String filename = getPluginFolderPath() + File.separator + pluginName + ".jar";
		File file = new File(filename);
		file.delete();

		// Remove the plugin from the list of installed plugins
		Iterator<Plugin> iter = listInstalledPlugins.iterator();
		while (iter.hasNext()) {
			Plugin plugin = iter.next();
			if (plugin.getName().equals(pluginName)) {
				iter.remove();
			}
		}

		// Save the list of installed plugins to file
		saveListOfInstalledPluginsToFile();
	}

	/**
	 * Check if a plugin has been installed
	 * 
	 * @param pluginName
	 * @return true if the plugin has been installed. Otherwise false.
	 */
	public static boolean isPluginInstalled(String pluginName) {

		// Check if the plugin is in the list of installed plugin
		for (Plugin plugin : listInstalledPlugins) {
			if (plugin.getName().equals(pluginName)) {
				return true;
			}
		}

		// If we did not find it, return false.
		return false;
	}

	

	/**
	 * Get the path of the folder where plugins are stored
	 * 
	 * @return the path
	 */
	public static File getPluginFolderPath() {
//		File path;
//		// Get the last path used by the user, if there is one
//		String previousPath = PreferencesManager.getInstance()
//				.getPluginFolderFilePath();

		// If there is no previous path (first time user), we use the user
		// directory
//		if (previousPath == null) {
//			path = new File(System.getProperty("user.dir"));
//		} else {
//			// Otherwise, use the last path used by the user.
//			path = new File(previousPath);
//		}
		File path = new File(System.getProperty("user.dir"));
		return path;
	}

	/**
	 * Save the list of installed plugins to a file, so that we can remember the
	 * plugins that have been installed.
	 */
	public static void loadListOfInstalledPluginsFromFile() {
		String path = getPluginFolderPath() + File.separator
				+ "SPMFInstalledPluginsInfo.ser";
//		System.err.println(path);
		if (new File(path).exists()) {
			try {
				FileInputStream fileInStr = new FileInputStream(path);
				ObjectInputStream objInStr = new ObjectInputStream(fileInStr);
				listInstalledPlugins = (ArrayList<Plugin>) objInStr.readObject();
				objInStr.close();
				fileInStr.close();
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		} else {
			listInstalledPlugins = new ArrayList<Plugin>();
		}
	}

	/**
	 * Read the list of installed plugins from the hard disk, so that we can
	 * remember the plugins that have been installed.
	 */
	public static void saveListOfInstalledPluginsToFile() {
		String path = getPluginFolderPath() + File.separator
				+ "SPMFInstalledPluginsInfo.ser";
		try {
			// Create file output stream
			FileOutputStream fileOutStr = new FileOutputStream(path);
			// Create object output stream and write object
			ObjectOutputStream objOutStr = new ObjectOutputStream(fileOutStr);
			objOutStr.writeObject(listInstalledPlugins);
			// Close all streams
			objOutStr.close();
			fileOutStr.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}
	
	static Plugin getPluginByNameFromList(String name){
		for(Plugin plugin : listPlugin){
			if(plugin.getName().equals(name)){
				return plugin;
			}
		}
		// if not found
		return null;
	}
	
	static Plugin getInstalledPluginByNameFromList(String name){
		for(Plugin plugin : listInstalledPlugins){
			if(plugin.getName().equals(name)){
				return plugin;
			}
		}
		// if not found
		return null;
	}

}
