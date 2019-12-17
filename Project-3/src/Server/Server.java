/* Name: Harshali Mahesh Mugutrao
 * UTA ID: 1001747263
 */

package Server;   //package that contains all files related to Server side processing

/* packages Input,Output,Utilities,Directory Handling*/
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.*;
import org.apache.commons.io.FileUtils;


public class Server {

	private ServerSocket serverSocket = null; //declared variable of type ServerSocket
    
	/* Establish connection with server
	 https://www.tutorialspoint.com/java/java_networking.htm#
	 */
	public void startServer() {
	     
	        
	    	
	    		 try {
	 	                serverSocket = new ServerSocket(5000); //Connection to be established on port 5000
					    ArrayList <String> users=new ArrayList<String>(); //ArrayList to store all user names of users connected to client
					    
					    //https://stackoverflow.com/questions/36849197/send-message-to-specific-clients-using-java
		 				Map<String, Socket> sockets = new HashMap<String,Socket>();
		 				
					    String tempusername=null; // Temporary variable to store user name
					    int count=1; //initialized  counter for tracking user number
					    String usr=null;
					    
	 					System.out.println("Server Started ...."); //Indicates that server is ON
	 					
	 					String path = "C:/Users/harsh/Desktop/Server"; //Static path for Server directory
	 					File file = new File(path);  // Sets File instance of directory
	 					
	 					if (!file.exists()) {                        //If Directory not present, make one 
	 			            file.mkdir();                            //Utils library function to create new folder
	 			            System.out.println("Server Directory Created");
	 					}
	 					
	 					else
	 					{   /*
	 					      If Directory exists, delete and make new, empty directory
	 					      https://www.programcreek.com/java-api-examples/?class=org.apache.commons.io.FileUtils&method=deleteDirectory
	 					    */
	 						FileUtils.deleteDirectory(file);         
	 						file.mkdir();                     
	 						System.out.println("Server Directory Created");
	 			        }
	 					
                        
	 					while(count>0)    
	 						
	 					{  
	 					Socket s2=serverSocket.accept(); //client connection accepted
	 								
	 					DataInputStream sis = new DataInputStream(s2.getInputStream()); //DataInputstream to send msg/data to client
	 					DataOutputStream sos = new DataOutputStream(s2.getOutputStream()); //DataOutputStream to receive msg/data from client
	 					usr=sis.readUTF(); //read data from input stream
	 					
	 					/* Count indicates user number.
	 					   If it is first user, we just add user name to ArrayList as no validation required.
	 					 */
	 					if(count==1)
				           {
	 							users.add(usr); //add user to ArrayList
	 							sockets.put(usr,s2); //https://stackoverflow.com/questions/36849197/send-message-to-specific-clients-using-java
	 							System.out.println("-----------------------------------------------------");
	 							System.out.println("New user- "+usr+" connected successfully"); //print message on client side
	 							
	 							/*Pass message to client using OutputStream
	 							 * https://gist.github.com/chatton/14110d2550126b12c0254501dde73616
	 							 */
	 							sos.writeUTF("Connected to server successfully"); //Inform user that connection established successfully
	 							sos.writeUTF("open");//indicate open connection
	 							sos.flush(); //empty OutputStream
	 							
	 							//Get list of connected users
					            System.out.println("\nUsers Connected:");
					            	
					            //https://www.geeksforgeeks.org/iterating-arraylists-java/
	 							for (int i = 0; i < users.size(); i++)  
	 								System.out.println(users.get(i));    
	 							 
	 							count++;  //Increment count for next user
	 							
	 							/*As we make user name null for loop to work,store user name in temp variable for further processing*/
	 							tempusername=usr; 
	 							
	 							usr=null;   //Set user name to null for loop condition to be satisfied when validating for other users
	 			              
	 							/*After successful validation, start new thread for the particular client*/
	 							ServerThread sct1 = new ServerThread(s2,users,sockets); 
	 							sct1.start();
	 							
				              }
	 					  
	 					/* Count indicates user number.
	 					   If it is not first user, we check if user name exists in list.
	 					   If user name exists in list, we inform client and ask to input different user name.
	 					   If user name does not exist in list, we add it to the list and start new thread for the client
	 					 */
	 					
	 					
	 					/*count >1 for 2nd user onwards, 
	 					 * usr !=null because in first condition counter is incremented, and to avoid the control from
	 					 * going inside loop we needed another condition
	 					 */
						while (count>1 && usr!=null)
						{           
	 					            	        if (users.contains(usr)) 
	 					            	       /*https://stackoverflow.com/questions/8936141/java-how-to-compare-strings-with-string-arrays*/
	 					            	        { 
	 					            	        	 sos.writeUTF("Username already in use. Please try again.\n");//print message on client side
	 					            	        	 sos.flush(); //empty OutputStream buffer
	 					 								
		 					                    	 usr=null; //set user name to null so that loop is not called again for same user	 
		 					                    	 sos.writeUTF("closed");
		 					                    	 s2.close(); // close socket as invalid user
	 					            	        }
	 					            	        
		 					                    else
		 					                   {   
			 					            		users.add(usr); //add valid, unique user name to ArrayList
			 					            		sockets.put(usr, s2); //https://stackoverflow.com/questions/36849197/send-message-to-specific-clients-using-java
			 					            		System.out.println("-----------------------------------------------------");
				 					            	System.out.println("New user "+usr+" connected successfully"); //print message on server side
				 					            	sos.writeUTF("Connected to server successfully"); //print message on client side
				 					            	sos.writeUTF("open"); //indicated open connection
				 					            	sos.flush(); //empty OutputStream buffer
				 					            	
				 					            	//Get list of connected users
				 					            	System.out.println("Users Connected:");
				 					            	
				 					            	//https://www.geeksforgeeks.org/iterating-arraylists-java/
				 		 							for (int i = 0; i < users.size(); i++) 
				 		 								System.out.println(users.get(i)); 				 		 		 				  
				 		 					     
				 				            	    count++; //increment counter for new user
				 				            	    tempusername=usr; //store username in temp variable for further processing
				 				            		usr=null; //set user name to null so that loop is not called again for same user
				 				            		
				 				            		/*start new thread for the particular client*/
				 				            		ServerThread sct1 = new ServerThread(s2,users,sockets);
				 			 					    sct1.start();
				 			 					    break;
			 					            	}
	 					            	        	 					           
	 					            	    }              
	 			                     }
	 					

	                      }			
	 	        
	    		 catch (Exception e) {
	 	        }
	        
	       }
	    
	
	    public void windowClosing(WindowEvent e) { 
	        
	        try {
				serverSocket.close(); //Close window 
			} 
	        catch (IOException e1) {
				// TODO Auto-generated catch block
			} 
	    } 
}
