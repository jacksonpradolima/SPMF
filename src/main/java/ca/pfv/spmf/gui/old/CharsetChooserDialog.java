package ca.pfv.spmf.gui.old;

import java.nio.charset.Charset;
import java.util.Set;

import javax.swing.JOptionPane;

public class CharsetChooserDialog {

	  public static void main(String[] a) {
		  
		// Fill the list of charsets available
		Set<String> setCharsets =  Charset.availableCharsets().keySet();
	    String[] choices = new String[setCharsets.size()];
	    int defaultcharsetPosition = -1;
	    int position = 0;
	    for(String charsetName : setCharsets){
	    	if(charsetName.equals(Charset.defaultCharset().name())){
	    		defaultcharsetPosition = position;
	    	}
	    	choices[position++] = charsetName;
	    }
	    
	    
	    String input = (String) JOptionPane.showInputDialog(null, "Default = " +Charset.defaultCharset().name() ,
	        "Choose TEXT file encoding:", JOptionPane.QUESTION_MESSAGE, null, 
	        choices, // This is the list of choices
	        choices[defaultcharsetPosition]); // This is the initial choice
	    
	    System.out.println(input);
	  }
}
