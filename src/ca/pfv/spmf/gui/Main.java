package ca.pfv.spmf.gui;
/*
 * Copyright (c) 2008-2018 Philippe Fournier-Viger
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
import java.lang.reflect.Method;

import ca.pfv.spmf.algorithmmanager.AlgorithmManager;
import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;


/**
 * This is a simple user interface to run the main algorithms in SPMF.
 * 
 * @author Philippe Fournier-Viger 2018
 */
public class Main {

    // variable for the current version of SPMF
    public final static String SPMF_VERSION = "2.40";
    
    /**
     * Method to launch the software. If there are command line arguments, it
     * means that the software is launched from the command line. Otherwise,
     * this method launches the graphical user interface.
     *
     * @param args command line arguments.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	// The following commented lines of code are to be used for debugging purposes:
		//args = new String[]{"run", "SPADE", "C:\\Users\\ph\\Desktop\\SPMF\\test_files\\contextPrefixSpan.txt", "output.txt",  "50%", "100"};
//C:\Users\ph\Desktop\SPMF\test_files
//		System.out.println("Command " + Arrays.toString(args));
        
    	// If there are command line arguments, we don't launch
        // the user interface. It means that the user is using
        // the command line.

        if (args.length != 0) {
        	// process command line arguments.
        	processCommandLineArguments(args); 
        } else {
            // Else, we launch the graphical user interface.
        	MainWindow mainWindow = new MainWindow(true, true);
        	mainWindow.setVisible(true);
        }
    }
    
    /**
     * This method process the command line arguments when the spmf.jar file is
     * called from the command line.
     *
     * @param args command line arguments.
     */
    public static void processCommandLineArguments(String[] args) {
        //java -Xmx1024m -jar spmfGUIv090b.jar run PrefixSpan /home/ph/Bureau/contextPrefixSpan.txt /home/ph/Bureau/test3.txt 60%
        //java -Xmx1024m -jar spmfGUIv090b.jar run PrefixSpan contextPrefixSpan.txt test3.txt 60%
//		System.out.println(" \n\n-- SPMF version " + SPMF_VERSION + " --\n\n");

        // "version" --> show the current version
        if ("version".equals(args[0])) {
            System.out.println(" \n-- SPMF version " + SPMF_VERSION + " --\n");
        } // "help" --> show the link to read the documentation
        else if ("help".equals(args[0])) {
            System.out.println("\n\nFor help, please check the documentation section of the SPMF website: http://philippe-fournier-viger.com/spmf/ \n\n");
        } //"run" -->  the user wants to run an algorithm
        else if ("run".equals(args[0])) {
        	
        	
            try {
            	
	            // We get the parameters :
	            String algoName = null;
	            
	            if(args.length > 1){
	            	algoName= args[1]; // algorithm name
	            }
	            
	            // Get the description of the algorithm
	            DescriptionOfAlgorithm description = AlgorithmManager.getInstance().getDescriptionOfAlgorithm(algoName);
	
	            // the next argument is 2
	            int i = 2;
	            
	            String input = null;
	            if(description.getInputFileTypes() != null){
	            	if(args.length > i){
	                	input = args[i];  // input file
	                }
	            	i++;
	            }
	            
	            
	            String output = null;
	
	            if(description.getOutputFileTypes() != null){
		            if(args.length > i){
		            	output = args[i]; // output file
		            }
		            i++;
	            }
	            
	            // create an array to store the parameters of the algorithm
				String[] parameters;
				// copy the arguments in the array of parameters:
	            if (args.length > i) {
	            	parameters = new String[args.length - i];
	                System.arraycopy(args, i, parameters, 0, args.length - i);
	            }else{
	            	// This happens  because the authors has provided no parameter in the command line interface
	            	parameters = new String[0];
	            }
	            
	            // run the algorithm:
            	CommandProcessor.runAlgorithm(algoName, input, output, parameters);
            }catch (NumberFormatException e) {
                System.out.println("Error. Please check the parameters of the algorithm.  The format for numbers is incorrect. \n"
                        + "\n ERROR MESSAGE = " + e.toString());
            } catch (Exception e) {
            	System.out.println("An error while trying to run the algorithm. \n ERROR MESSAGE = " + e.toString());
                e.printStackTrace();
            }
        } // "test" --> this is to run a test file (for developers only).
        else if ("test".equals(args[0])) {
            String testName = args[1];
            try {
                @SuppressWarnings("rawtypes")
				Class testClass = Class.forName("ca.pfv.spmf.tests." + testName);
                @SuppressWarnings("unchecked")
				Method mainMethod = testClass.getMethod("main", String[].class);
                String[] params = null;
                mainMethod.invoke(null, (Object) params);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // if any other commands that we don't recognize, we show this:
            System.out.println("\n\n Command not recognized.\n For help, please check the documentation section of the SPMF website: http://philippe-fournier-viger.com/spmf/ \n \n");
        }
    }
} // S P M F
