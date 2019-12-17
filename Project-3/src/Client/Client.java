/* Name: Harshali Mahesh Mugutrao
   UTA ID: 1001747263
 */

package Client; //package that contains all files related to Client side processing java.awt.EventQueue;

/* packages required for Directory Watching and Input Output*/
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;


public class Client {

	public static void Conn(String address, int port,String input) throws Exception {
		   
		   	String user=input;      //String variable to store user name
		   	Socket soc= new Socket(address, port);   //Connection to be established on address of local server and port 5000
           	//System.out.println("Connected to server");
		     DataOutputStream oos = new DataOutputStream(soc.getOutputStream());  //DataOutputStream to receive msg/data from server
		     oos.flush();     //empty OutputStream
		     oos.writeUTF(user);  //send user name to server for validation
		     DataInputStream is=new DataInputStream(soc.getInputStream());//DataInputstream to send msg/data to server
		    
		     System.out.println(is.readUTF());  //read data from input stream
		     
		     String connection=is.readUTF(); //store value of connection state
		     
		     //check if connected to server or not
             if("open"==connection.intern()) //https://stackoverflow.com/questions/8484668/java-does-not-equal-not-working
		     CreateClientDirectory(soc,user);  //function to create directory for client
	}

	/*
	 * Function to Create Directory for client so as to store new files and upload it to server
	 * https://alvinalexander.com/java/java-create-directory-directories-file-mkdirs
	 * https://stackoverflow.com/questions/18628023/to-create-a-new-directory-and-a-file-within-it-using-java
	 */
	public static void CreateClientDirectory (Socket soc,String user){
        
		String path = "C:/Users/harsh/Desktop/"+user+"/SharedFile.txt"; //Static path for Server directory
		File file = new File(path);  // Sets File instance of directory
			
		/*create a new dynamic directory at specified location with directory name same as user name */
		boolean success = file.getParentFile().mkdirs(); 
		
		if (!success)     //if directory exists, delete and make again. This is when we run program again with same usernames
		{  
			try {
			FileUtils.deleteDirectory(file.getParentFile()); //delete existing directory
			file.getParentFile().mkdir();  //create new directory
		    file.createNewFile();  //create a text file in the directory
		    
		    //inform user of successful connection and directory creation
		    System.out.println("A directory with your username ["+user+"] has been created for you."
					+ "\nTo start sharing, place a file in this directory.");	
			System.out.println("-------------------------------------------");
			} 
		
			catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}  
		    
	    }
			
		else              //If directory does not exist, create new and let user know 
		{	
			try {
			file.createNewFile();   //reference link in description of function
			System.out.println("A directory with your username ["+user+"] has been created for you."
					+ "\nTo start sharing, place a file in this directory.");	
			System.out.println("-------------------------------------------");
			} 
		
			catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		    
		}
		
		while(true) {
			WatchDirectory(user, soc);   //Function call to watch directory for any new file      	
		    }
	}
	
	/* 
	 Function to watch directory of user for new file/deletion
	 If new file added, let user know and push file to server directory.
	 If file deleted, initiate 2PC
	 https://www.baeldung.com/java-nio2-watchservice
	 */
	public static void WatchDirectory(String user,Socket soc){
		Path path = Paths.get("C:/Users/harsh/Desktop/"+user); //path for user directory to be monitored
		WatchService watchService;   //variable of type watchservice
		
		try {       		        
			        watchService = FileSystems.getDefault().newWatchService();  //initiate watch service

					/* Register type of events to be monitored*/
					path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_MODIFY,
							StandardWatchEventKinds.ENTRY_DELETE);
					
					//http://www.java2s.com/Tutorials/Java/java.nio.file/WatchService/Java_WatchService_poll_long_timeout_TimeUnit_unit_.htm
					WatchKey key=watchService.poll(5, TimeUnit.SECONDS);  //pick a watchkey from list
					
					if(key==null)
					{    
						DataInputStream dis=new DataInputStream(soc.getInputStream());
						if(dis.available()!=0)
						{ System.out.println(dis.readUTF());
						}
					}
					
					else {
			        
			        String filename=null;   //variable to store filename

		            for (WatchEvent<?> event : key.pollEvents()) //For all events in list

		            { 
		            	WatchEvent.Kind<?> kind = event.kind();   //match current event kind with key in registry
                      
		                if ((kind == StandardWatchEventKinds.ENTRY_CREATE) || (kind== StandardWatchEventKinds.ENTRY_MODIFY)) //when adding file in client directory
		                {
		                    filename=event.context().toString();   //store file name in variable filename
		                    SendFileToServer(user,filename, soc);  //Function call to put file in Server Directory
		                }
		                
		                else if (kind == StandardWatchEventKinds.ENTRY_DELETE) //if file is deleted
		                {
		                    filename=event.context().toString();   //store file name in variable filename
		                    InitiateVoting(user,filename, soc);  //Function call to initiate voting for 2PC
		                }
		            } 
		            
		            
			  }
		}
		catch (Exception e) {
			// print error (if any)
			e.printStackTrace();
		}
}    

	/*
	  Function to send file to server directory.
	  https://www.java67.com/2016/09/how-to-copy-file-from-one-location-to-another-in-java.html
	 */
	private static void SendFileToServer(String user, String filename,Socket soc) {
       
		File From = new File("C:/Users/harsh/Desktop/"+user+"/"+filename); //path of client directory
		File To = new File("C:/Users/harsh/Desktop/Server/"+filename);  //path of server directory
		
		try {
            if(To.exists()==false)
			
			{ 	
            	FileUtils.copyFile(From, To); //Apache File Utils library function to copy file from one location to another location
				System.out.println("File:" +filename+ " uploaded on server sucessfully !!"); //let user know file has been uploaded to server
				System.out.println("------------------------------------------------------------------");
			
			DataOutputStream dos=new DataOutputStream(soc.getOutputStream());
			dos.writeUTF("File: "+filename+" uploaded by user: "+user);
			}
			
		} 
		catch (IOException e) {
			//print error (if any)
			e.printStackTrace();
		}
		
	}
	
	/*
	 Function for requesting votes
	*/
	private static void InitiateVoting(String user, String filename, Socket soc) {
		DataOutputStream iv;
		
		try {
			System.out.println("File: "+filename+ " deleted !!!"); // prints that file is deleted on client deleting the particular file
			iv = new DataOutputStream(soc.getOutputStream());      // OutputStream for communicating to server
			iv.writeUTF("File: "+filename+ ",user: "+user);   //send instruction containing filename and user name (who deleted file) to server
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/*
	 Function from org.apache.commons.io.FileUtils to copy file from one location to another
	 https://www.java67.com/2016/09/how-to-copy-file-from-one-location-to-another-in-java.html
	 */
	public static void copyFile(String from, String to) throws IOException{
        Path src = Paths.get(from); // set path of source directory from parent function
        Path dest = Paths.get(to);  // set path of destination directory from parent function
        Files.copy(src, dest);      //inbuilt function to copy files
        
    }
	
	/* 
	 Main function calling function to connect Server
	 */
	public static void main(String uname) throws Exception {
		String ipusername= uname;  //variable to store user name
	    Conn("127.0.0.1",5000,ipusername);  //Function call to connect to server via socket
	       
		}
}
