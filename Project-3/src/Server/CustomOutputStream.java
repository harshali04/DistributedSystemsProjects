/* Name: Harshali Mahesh Mugutrao
 * UTA ID: 1001747263
 */

package Server;  //package that contains all files related to Server side processing

import java.io.*;
import javax.swing.JTextArea;

/*Code to override Standard output stream:
 https://stackoverflow.com/questions/5107629/how-to-redirect-console-content-to-a-textarea-in-java
 */

public class CustomOutputStream extends OutputStream {
    private JTextArea textArea;    //Text area on Server Window

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
    
    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
        // keeps the textArea up to date
        textArea.update(textArea.getGraphics());
    }
}