/*--------------------------------------------------------
1. Name / Date:  
   Ralph / 2018-01-20


2. Java version used, if not the official version for the class:
   build 1.8.0_111-b14


3. Command-line compilation instructions:
   > javac JokeClient.java 


4. Instructions to run this program:
   In different shell windows, type in:
   window 1: > java JokeClient <IPaddr> <IPaddr>
   window 2: > java JokeClient <IPaddr> <IPaddr>
   ....
   (We can start as many clients as we want in different window.)

   When starting a new client, you can pass the IP addresses of servers
   to client (assuming programs running across machines).
   The default server IP address is "localhost".
   You can pass in at most two server IP. If a second IP address is 
   passed in as the second argument, the client allows the user to 
   switch between primary and secondary servers.

   For example, if the first server is running at 192.168.1.8,
   you should type:
   > java JokeClient 192.168.1.8

   If you allow the client to switch between two servers, 
   you can type in another IP as a second argument, e.g.:
   > java JokeClient 192.168.1.8 192.168.1.10


5. List of files needed for running the program.
   a. JokeServer.java
   b. JokeClient.java
   c. JokeClientAdmin.java

----------------------------------------------------------*/

import java.io.*;	//Import I/O libraries
import java.net.*;	//Import networking libraries

/**
 *	This is the Client class.
 */
public class JokeClient {
	
	/**
	 * Main driver to run a client.
	 * @param args
	 */
	public static void main (String args[]) {
		
		//Server addresses and ports
		String server1 = "localhost";
		String server2 = "localhost";
		int port1 = 4545;
		int port2 = 4546;
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
		
		//Prompt msg on client console while starting
		System.out.println("Ralph Zhang's Joke Client, beta.");
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
			
			//Get user's name
			System.out.print("Please enter your username: ");
			String name = in.readLine();
			
			//Create a record to store the state (Joke/Proverb) of each client
			//Two values for possible two independent servers
			//Each value, one integer in range of 0(0x00) to 255(0xff), initial value 0
			int[] infoArray = {0,0};
			int info = (isFirst ? infoArray[0] : infoArray[1]);
			
			while (true) {
				//Prompt for user input
				System.out.print("\nPress <Enter> to request a response. "
						+ "Input ¡°s¡± to change server, ¡°quit¡± to end: ");	
				System.out.flush();		//Clear output stream
				input = in.readLine();	//Read user input
				
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
						isFirst = !isFirst;								//Switch signal
						server = (isFirst ? server1 : server2);			//Switch server address
						port = (isFirst ? port1 : port2);				//Switch port
						info = (isFirst ? infoArray[0] : infoArray[1]);	//Switch record
						System.out.println("Now communicating with: " + server 
								+ " port " + port);
					}
					//Move to next iteration
					continue;
				}
				
				//Request a response if <Enter>
				if (input.equals("")) {
					//Connect to server, get response and update state information
					info = getResp(server, port, name, info);
					//Store the state
					infoArray[isFirst ? 0 : 1] = info;
				}
			}
		}	catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Method to build connection with server to get response.
	 * Basic routine:
	 * send request (output stream) to server.
	 * read responses request (input stream) from server.
	 * @param server: input, server address
	 * @param port: input, port for connection
	 * @param name: input, client's username
	 * @param info: input, state record
	 * @return an updated state record
	 */
	static int getResp (String server, int port, String name, int info) {
	
		PrintStream toServer = null;		//Declare an output stream
		BufferedReader fromServer = null;	//Declare an input stream
		
		Socket sock;		//Declare a socket
		String readInLine;	//Declare a string for input text
		
		try {
			sock = new Socket(server, port);	//Build connection to server port
			
			//Get I/O stream from socket
			toServer = new PrintStream(sock.getOutputStream());
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			toServer.println(name);	//Send client user name to server as first line
			toServer.println(info);	//Send state of record to server as second line
			toServer.flush();		//Clear output stream
			
			//Read responses from server, there should be 2 or 3 lines
			readInLine = fromServer.readLine();		//Read first line
			if (readInLine != null) {
				System.out.println(readInLine); 	//Print message on client console
				
				readInLine = fromServer.readLine();	//Read second line, update client's record state
				info = Integer.parseInt(readInLine);
				
				
				//If there is a third line, a cycle has completed
				readInLine = fromServer.readLine();	//Read third line
				if (readInLine != null) {
					System.out.println(readInLine); //Announce client on console
				}
			}
			sock.close();	//Conversation ends, close the socket on client side
		}	catch (IOException e) {
			System.out.println("Socket error. Connection refused.");
		}
		//Return new state record
		return info;
	}
}
