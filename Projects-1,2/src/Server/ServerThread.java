/* Name: Harshali Mahesh Mugutrao
 * UTA ID: 1001747263
 */

package Server;   //package that contains all files related to Server side processing

import java.io.*;
import java.net.Socket;
import java.nio.file.*;  //package required for directory monitoring
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


	
	class ServerThread  extends Thread {

		DataInputStream dis;	
		Socket soc;
		ArrayList<String> userList;
		Map<String, Socket> socketList;
	
		public ServerThread(Socket soc, ArrayList<String> users, Map<String, Socket> sockets) throws IOException {

		this.soc=soc;
		this.userList=users;
		this.socketList=sockets;
	}
		
	@Override
		  public void run(){                     //run method of thread class is overridden to implement our custom code

		try {
		     dis=new DataInputStream(soc.getInputStream()); //get InputSream to read from client
		     System.out.println("\n"+dis.readUTF());        //print message which contains username from client
		     String currentUser=dis.readUTF();              //store username in variable to identify current user
		     
		     InvalidationNotice(currentUser,userList,socketList);  //call to display invalidation notices
		}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		    
	}
	
	//InvalidationNotice call prints a message on the clients that do not have updated file
	
	public static void InvalidationNotice(String currentUser,ArrayList<String> userList,Map<String, Socket> socketList ) {
		         //https://www.geeksforgeeks.org/iterate-map-java/
		        //Hashmap to store username and sockets, for message passing purpose
				 Iterator<Map.Entry<String,Socket> > iterator = socketList.entrySet().iterator();  
				 String keyToBeChecked = currentUser; 
	  
				 // Iterate over the HashMap 
				 while (iterator.hasNext()) { 
	  
	             // Get the entry at this iteration 
			     Map.Entry<String,Socket> entry = iterator.next(); 
	  
	            // If user in list is sender, skip 
	            if (keyToBeChecked.equals(entry.getKey())) { 
	                continue; 
	            } 
	            
	            else {
	            	System.out.println("Sending Message to: "+entry.getKey());  //Print clients who do not have updated files
	            	
	            	Socket sendToPort=entry.getValue(); //get port of particular user
	            	
	            	try {
						DataOutputStream os = new DataOutputStream(sendToPort.getOutputStream());
						os.flush();
						//send notice to port of client 
						os.writeUTF("Invalidation Notice: File SharedFile.txt has been modified. Retrieving updated version...");

					} 
	            	catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

	            }
	        
			   } 
			}
		 }
		

