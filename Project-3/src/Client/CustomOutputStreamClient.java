/* Name: Harshali Mahesh Mugutrao
 * UTA ID: 1001747263
 */

package Client;  //package that contains all files related to Client side processing

import java.io.*;
import javax.swing.JTextArea;


/*Code to override Standard output stream:
 https://stackoverflow.com/questions/5107629/how-to-redirect-console-content-to-a-textarea-in-java
 */

public class CustomOutputStreamClient extends OutputStream  {

	    private JTextArea textArea1; //Text area on Client Window

	    public CustomOutputStreamClient(JTextArea textArea) {
	        this.textArea1 = textArea;
	    }
	    
	    @Override
	    public void write(int b) throws IOException {
	        // redirects data to the text area
	        textArea1.append(String.valueOf((char)b));
	        // scrolls the text area to the end of data
	        textArea1.setCaretPosition(textArea1.getDocument().getLength());
	        // keeps the textArea up to date
	        textArea1.update(textArea1.getGraphics());
	    }
	}


