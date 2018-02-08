/*--------------------------------------------------------
1. Name / Date:  
   Ralph / 2018-01-20


2. Java version used, if not the official version for the class:
   build 1.8.0_111-b14


3. Command-line compilation instructions:
   > javac JokeClientAdmin.java 


4. Instructions to run this program:
   In shell window, type in:
   > java JokeClientAdmin <IPaddr> <IPaddr>

   When starting the administration client, you can pass the IP addresses
   of servers to client (assuming program running across machines).
   The default server IP address is "localhost".
   You can pass in at most two server IP. If a second IP address is 
   passed in as the second argument, the client allows the user to 
   switch between primary and secondary servers.

   For example, if the first server is running at 192.168.1.8,
   you should type:
   > java JokeClientAdmin 192.168.1.8

   If you allow the client to switch between two servers, 
   you can type in another IP as a second argument, e.g.:
   > java JokeClientAdmin 192.168.1.8 192.168.1.10


5. List of files needed for running the program.
   a. JokeServer.java
   b. JokeClient.java
   c. JokeClientAdmin.java


----------------------------------------------------------*/

import java.io.*;	//Import I/O libraries
import java.net.*;	//Import networking libraries

/**
 *	This is the Client Admin class.
 */
public class JokeClientAdmin {
	
	/**
	 * Main driver to run a Client Administrator.
	 * @param args
	 */
	public static void main (String args[]) {
		
		//Server addresses and ports
		String server1 = "localhost";
		String server2 = "localhost";
		int port1 = 5050;
		int port2 = 5051;
		//Signal indicating if there is a secondary server
		boolean second = false;		
		
		if (args.length > 0) {
			server1 = args[0];		//Read first server IP address
			if (args.length > 1) {
				second = true;		//Set signal to be true
				server2 = args[1];	//Read second server IP address
			}
		}
		
		//Default server: server1 
		String server = server1;
		int port = port1;
		
		//Prompt msg on ClientAdmin console while starting
		System.out.println("Ralph Zhang's Joke ClientAdmin, beta.");
		System.out.println("Server one: " + server1 + ", port " + port1);
		if (second) {
			System.out.println("Server two: " + server1 + ", port " + port2);
		}
		
		//Buffer for user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		//Try/catch block to handle I/O exceptions during reading client's input
		try {
			String input = "";			//User input string
			boolean isFirst = true; 	//Signal indicating if on server1
			
			while (true) {
				System.out.print("Press <Enter> to toggle server mode. "
						+ "\nInput ¡°s¡± to change server, ¡°shutdown¡± to shut down server,"
						+ " ¡°quit¡± to end: ");	//Prompt for user input
				System.out.flush();				//Clear output stream
				
				input = in.readLine();			//Read user's input
				
				if (input == null) continue;
				
				//Quit if user inputs 'quit'
				if (input.indexOf("quit") >= 0) {
					System.out.println("Cancelled by user request.");
					break;
				}
				
				//Switch server if 's'
				if (input.equals("s")) {
					//If no secondary server, prompt msg on console
					if (!second) System.out.println("No secondary server being used.");
					//Otherwise, switch server address and port
					else {
						server = (isFirst ? server2 : server1);
						port = (isFirst ? port2 : port1);
						isFirst = !isFirst;		//Switch signal
						System.out.println("Now communicating with: " + server 
								+ " port " + port);
					}
					//Move to next iteration
					continue;
				}
				
				//Request a response if 'shutdown' or <Enter>
				if (input.equals("") || input.equals("shutdown")) {
					//Connect to server, switch the mode or turn off server
					getResp(server, port, input);
				}
			}
		}	catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to connect with server to do some operations.
	 * Basic routine:
	 * send request (output stream) to server.
	 * read responses request (input stream) from server.
	 * @param server: server address
	 * @param port: port for connection
	 * @param input: input from ClientAdmin
	 * @return an updated state record
	 */
	static void getResp (String server, int port, String input) {

		PrintStream toServer = null;		//Declare an output stream
		BufferedReader fromServer = null;	//Declare an input stream
		
		Socket sock;		//Declare a socket
		String readInLine;	//Declare a string for input text
		
		try {
			sock = new Socket(server, port);	//Build connection to server port
			
			//Get I/O stream from socket
			toServer = new PrintStream(sock.getOutputStream());
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			toServer.println(input);//Send ClientAdmin's input to Server
			toServer.flush();		//Clear output stream
			
			//Read responses from server, there should be 1 line
			readInLine = fromServer.readLine();
			if (readInLine != null) {
				String sv = (port == 5050)? "Server 1" : "Server 2";
				System.out.println(sv + readInLine.substring(6)); 	//Print message on client console
			}
			sock.close();	//Conversation ends, close the socket on ClientAdmin side
		}	catch (IOException e) {
			System.out.println("Socket error.");
			e.printStackTrace();
		}
	}
}
