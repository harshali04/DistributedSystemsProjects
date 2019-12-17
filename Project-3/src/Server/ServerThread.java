/* Name: Harshali Mahesh Mugutrao
 * UTA ID: 1001747263
 */

package Server;   //package that contains all files related to Server side processing

import java.io.*;
import java.net.Socket;
import java.nio.file.*;  //package required for directory monitoring
import java.util.*;
import java.util.concurrent.TimeUnit; //package required for wait/timeout

import org.apache.commons.io.FileUtils;  //package for copying files from one folder to another


	
	class ServerThread  extends Thread {	
		Socket soc;                      //socket for communication between clients and server
		ArrayList<String> userList;      //arraylist with list of connected users
		Map<String, Socket> socketList;  //Hash map with user name and corresponding sockets. Used to send msg to particular user's socket.
	
		//starting a new thread for every client
		public ServerThread(Socket soc, ArrayList<String> users, Map<String, Socket> sockets) throws IOException {

		this.soc=soc;
		this.userList=users;
		this.socketList=sockets;
	}
		
	@Override
		  public void run(){                     //run method of thread class is overridden to implement our custom code
      
		//continuously monitor server directory and take necessary action when event occurs
		while(true)
		{
			Download(soc,userList,socketList); 
		}
		
		    
	}
	
	
	public static void Download(Socket soc,ArrayList<String> userList,Map<String, Socket> socketList ) {
		 
		 //global variables used to send info to particular clients
		 
		String currentUser=null;      // stores name of client who modified the file
		 String filename=null;        // stores filename which is modified
		 String msg=null;            //stores any incoming msg
		 
   try {
			 DataInputStream dis=new DataInputStream(soc.getInputStream()); //get InputSream to read from client
			
			 if(dis.available()!=0)            //if Inputstream not empty, it means we have some msg. Read it.
			{ msg=dis.readUTF();        
			  dis=null;                        //empty inputstream
			 
			  currentUser=msg.substring(msg.indexOf("user:") + 6, msg.length());             //store username from msg in variable to identify current user
			   		
			  //depending on msg, we extract filename
				 if(msg.contains("uploaded"))
					    filename=msg.substring(msg.indexOf("File:") + 6, msg.indexOf("uploaded"));     //store filename in variable to identify file for download)
					
				else if(msg.contains(","))
					 {
						 filename=msg.substring(msg.indexOf("File:") + 6, msg.indexOf(",user: "));    //store filename in variable to identify file (for delete-2PC)
					     SendVotingReq(soc, userList, socketList,currentUser,filename);
					}
				else
					System.out.println(msg);     //print msg
				}
			  
		    } 
		
		 catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	     
	    Path path = Paths.get("C:/Users/harsh/Desktop/Server"); //path for server directory to be monitored
		WatchService watchService;   //variable of type watchservice
		
		//https://www.geeksforgeeks.org/iterate-map-java/
        //Hashmap to store username and sockets, for message passing purpose
		 Iterator<Map.Entry<String,Socket> > iterator = socketList.entrySet().iterator();  
		
		 String keyToBeChecked = currentUser;    //stores name of user who modified file
		
		try {
			DataInputStream dis=new DataInputStream(soc.getInputStream()); //get InputSream to read from client
			watchService = FileSystems.getDefault().newWatchService();     //initiate watchservice
			
			/* Register type of events to be monitored (Used in downloading)*/
			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_MODIFY);
			
			//http://www.java2s.com/Tutorials/Java/java.nio.file/WatchService/Java_WatchService_poll_long_timeout_TimeUnit_unit_.htm
			WatchKey key=watchService.poll(2, TimeUnit.SECONDS);  //Pick a watchkey from list within 2 seconds.
			
			if(key==null)            
			{    
				Download(soc,userList,socketList);     //If no event occurs, try again
			}
			
			else                                      //If new file is added by a client to server, send it to other clients
		  {     
				if(keyToBeChecked== null || filename==null)
			  {  msg=dis.readUTF();                                                             //store msg in variable to identify file and user
				 dis =null;
				 System.out.println(msg);                                                       //print received msg
				 
				 currentUser=msg.substring(msg.indexOf("user:") + 6, msg.length());             //store username in variable to identify current user
				 keyToBeChecked=currentUser;                                                    //stores name of user who modified file
				 
				 //Depending on msg, we choose filename
				 if(msg.contains("uploaded"))
					    filename=msg.substring(msg.indexOf("File:") + 6, msg.indexOf("uploaded"));  //store filename in variable to identify file (for download)
					
				else if(msg.contains(","))
					 {
						 filename=msg.substring(msg.indexOf("File:") + 6, msg.indexOf(",user: "));  //store filename in variable to identify file (for delete-2PC)
					     SendVotingReq(soc, userList, socketList,currentUser,filename);             //If deletion instruction, proceed to Election
					}
				else
					System.out.println(msg);                                                       //print msg
				}
		  
			for (WatchEvent<?> event : key.pollEvents())                                            //For all events in list

            { 
            	WatchEvent.Kind<?> kind = event.kind();                                             //match current event kind with key in registry
              
         if ((kind == StandardWatchEventKinds.ENTRY_CREATE) || (kind== StandardWatchEventKinds.ENTRY_MODIFY))   //events that indicate new file upload
               {
                	
                 while (iterator.hasNext()) {                                                       // Get the entry at this iteration 
                 
   			     Map.Entry<String,Socket> entry = iterator.next(); 
   	  
   	            // If user in list is sender, skip 
   	            if (keyToBeChecked.equals(entry.getKey())) { 
   	                continue; 
   	            } 
   	            
   	       else {                                                                              //if user is not a sender, we need to send files (i.e. DOWNLOAD)
                      
   	            	File From = new File("C:/Users/harsh/Desktop/Server/"+filename);             //path of server directory
   	        		File To = new File("C:/Users/harsh/Desktop/"+entry.getKey()+"/"+filename);  //path of client directory
   	        		
   	        		try {

   	        			FileUtils.copyFile(From, To); //Apache File Utils library function to copy file from one location to another location
   	        			Socket sendToPort=entry.getValue(); //get port of particular client where we need to download file
   	        			DataOutputStream os = new DataOutputStream(sendToPort.getOutputStream());
   	        			os.writeUTF("File: "+ filename+" downloaded from server sucessfully..!!"); //print on client
   	        			os.writeUTF("------------------------------------------------------------------");
   	        			System.out.println("File:" +filename+ " sent to user "+entry.getKey()+" sucessfully !!"); //let user know file has been uploaded to server
   	        			System.out.println("------------------------------------------------------------------");
   	        		 }
   	        		
   	        		catch (IOException e) {
   	        			//print error (if any)
   	        			e.printStackTrace();
   	        		}
   	           }
   	          }
             }
            }
		  }			
		} 
		catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  //initiate watch service
	        
      } 
	
	/*Function to request remaining clients to vote when a client deletes a file in its directory*/
	public static void SendVotingReq(Socket soc,ArrayList<String> userList,Map<String, Socket> socketList, String currentUser, String filename) {
	  
	   //Iterator for Hashmap-SocketList which maintains list of ALL clients and their sockets
	    Iterator<Map.Entry<String,Socket> > iterator = socketList.entrySet().iterator();
	    ArrayList <String> ParticipantVotes=new ArrayList<String>(); //ArrayList to store all votes

	    /******GLOBAL VARIABLES FOR THIS FUNCTION*******/
	    //https://www.geeksforgeeks.org/iterate-map-java/
	    //Hashmap to store username and sockets of REMAINING clients, for message passing purpose
	    Map<String, Socket> DelFilesForUsers = new HashMap<String,Socket>();;
		
		DataOutputStream toCoordinator = null;       // OutputStream for coordinator 
		String coordinator=null;                     //String to store name of coordinator
		
		while (iterator.hasNext())                   //iterator for List of ALL Clients to distinguish coordinator and participants
        {
       	 // Get the entry at this iteration 
		     Map.Entry<String,Socket> entry = iterator.next(); 

          // If user in list initiated delete operation, it means it is coordinator
          if (currentUser.equals(entry.getKey())) { 
        	  
        	  Socket sendToPort=entry.getValue(); //get port of coordinator for communication
        	  try {
        		  DataOutputStream os = new DataOutputStream(sendToPort.getOutputStream());
            	  os.writeUTF("Voting initiated. Status changed to CO-ORDINATOR"); //print on client initiating deletion. Notify that it is coordinator
            	  toCoordinator=new DataOutputStream(os);
            	  coordinator=entry.getKey();                                      //store username of client who is coordinator
				} 
        	 
        	  catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
          } 
          
          /*if client is not coordinator, it means that it is participant*/
          else {
        	    Socket sendToPort=entry.getValue();                           //get port of particular client which is participant
     			
     		try {  DelFilesForUsers.put(entry.getKey(), sendToPort);
     				DataOutputStream os = new DataOutputStream(sendToPort.getOutputStream());  
					os.writeUTF("File: "+filename+" deleted by user: "+currentUser+". You are now a PARTICIPANT...Please cast your vote!");  //print on participant
					String vote=GenerateRandomVote(os,filename,currentUser);        //Request participant to vote
					
					
					ParticipantVotes.add(vote);  //add votes in arraylist
					
					
				} 
     		catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
              
             }
	
         }
		
		/*After all votes are checked, we take necessary action depending on count*/
		if(ParticipantVotes.contains("abort"))
		{                            //If list contains abort, it means GLOBAL_ABORT and we need to restore deleted file
		  try {
			  	TimeUnit.SECONDS.sleep(20);   //wait so that we get votes from all participants
			  	System.out.println("------------------------------------------------------------------");
			  	toCoordinator.writeUTF("Final decision: GLOBAL ABORT");  //coordinator informs the final decision to user
			  	File From = new File("C:/Users/harsh/Desktop/Server/"+filename);          //path of server directory
				File To = new File("C:/Users/harsh/Desktop/"+coordinator+"/"+filename);  //path of coordinator directory
				FileUtils.copyFile(From, To);                                            //restore deleted file from server
				toCoordinator.writeUTF("----------(File "+" restored sucessfully!!)--------------");               //Notify user after file is restored at coordinator
		  	} 
		  catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  	}
		 
		}
		else           //If list contains no abort, it means GLOBAL_COMMIT and we need to delete files on participants
		{ try {
				TimeUnit.SECONDS.sleep(30);      //wait so that we get votes from all participants
				System.out.println("------------------------------------------------------------------");
				toCoordinator.writeUTF("Final decision: GLOBAL COMMIT. Deleting files in other clients"); //coordinator informs the final decision to user
				coordinator=null;                                             
				DeleteFiles(filename,DelFilesForUsers);                                       //Delete files on participants
				toCoordinator.writeUTF("----------(Files deleted on all clients successfully!!)----------");      
				//Notify user when all files deleted successfully on participants
			 } 
		  catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  
		}
     }

	/*Function to Delete Files on participants in case of GLOBAL_COMMIT*/
	private static void DeleteFiles(String filename, Map<String, Socket> delFilesForUsers) {
		
		    //https://www.geeksforgeeks.org/iterate-map-java/
		   //Iterator for hashmap which contains list of participants
		    
		   Iterator<Map.Entry<String,Socket> > loop = delFilesForUsers.entrySet().iterator(); 
		   
		   while (loop.hasNext()) 
	        {  
			     // Get the entry at this iteration 
			     Map.Entry<String,Socket> entry = loop.next(); 
				
				try {
					File f = new File("C:/Users/harsh/Desktop/"+entry.getKey()+"/"+filename);             //path of file on participants which is to be deleted
					DataOutputStream onclient=new DataOutputStream(entry.getValue().getOutputStream());   //OutputStream for participant
					onclient.writeUTF("File "+filename+" deleted successfully");                          //Notify user when file deleted on participant
					f.delete();                                                                           //delete file on participant
					
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("--------------------------------------------------");
				}
				
			}
		  
		   File fserver = new File("C:/Users/harsh/Desktop/Server/"+filename);    //Path of server file
		   fserver.delete();                                                      //Delete file on server as it should not download files again due to watcher
		   
	}

	/*Function to create vote randomly for participants*/
	//https://www.geeksforgeeks.org/generating-random-numbers-in-java/
	private static String GenerateRandomVote(DataOutputStream os, String filename, String currentUser) {
		
	      try {
	    	os.writeUTF("Casting vote...Please wait");
	    	TimeUnit.SECONDS.sleep(3);                   //wait 3 sec before casting vote
	    	
		      Random randomno = new Random();            // create random boolean value

		      boolean value = true;   // get next boolean value 
              
		      if (value==true)                          //If value=true, it means commit. Notify on participant screen and return 
			  {os.writeUTF("I vote: Commit");
		       return "commit";
			  }
		      else                                      //If value=false, it means abort. Notify on participant screen and return
			  {os.writeUTF("I vote: Abort");
			  return "abort";
			  }
		}
	      catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "abort";
		
	}
	
}		
 
