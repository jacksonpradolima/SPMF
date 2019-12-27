package ca.pfv.spmf.welwindow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;

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
public class Plugin implements Serializable {

	/** Serial ID */
	private static final long serialVersionUID = 8961825827831257902L;

	/** the plugin name */
	private String name;

	/** the plugin description */
	private String description;

	/** the author of the plugin */
	private String author;

	/** the category of plugin */
	private String category;

	/** the plugin version */
	private String version;

	/** the url of the documentation **/
	private String urlOfDocumentation;

	/** the repository from where the plugin was downloaded from **/
	private String repositoryURL;

	/** the url of the plugin in the repository */
	private String repositoryPluginFolder;

	// ----------------------------------------------

	/** Input file types */
	private List<String> inputFileTypes = new ArrayList<String>();

	/** Output file types */
	private List<String> outputFileTypes = new ArrayList<String>();

	/** number of parameters */
	int parameterCount = 0;

	/** parameters */
	private List<DescriptionOfParameter> parameters = new ArrayList<DescriptionOfParameter>();

	// ===============================================================

	public String getRepositoryURL() {
		return repositoryURL;
	}

	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}

	public void setUrlOfDocumentation(String urlOfDocumentation) {
		this.urlOfDocumentation = urlOfDocumentation;
	}

	public String getUrlOfDocumentation() {
		return urlOfDocumentation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setRepositoryPluginFolder(String folderName) {
		repositoryPluginFolder = folderName;
	}

	public String getRepositoryPluginFolder() {
		return repositoryPluginFolder;
	}

	public List<String> getInputFileTypes() {
		return inputFileTypes;
	}

	public void setInputFileTypes(List<String> inputFileTypes) {
		this.inputFileTypes = inputFileTypes;
	}

	public int getParameterCount() {
		return parameterCount;
	}

	public void setParameterCount(int parameterCount) {
		this.parameterCount = parameterCount;
	}

	public List<DescriptionOfParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<DescriptionOfParameter> parameters) {
		this.parameters = parameters;
	}

	public List<String> getOutputFileTypes() {
		return outputFileTypes;
	}

	public void setOutputFileTypes(List<String> outputFileTypes) {
		this.outputFileTypes = outputFileTypes;
	}

}
