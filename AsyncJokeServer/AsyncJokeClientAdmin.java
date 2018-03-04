/*--------------------------------------------------------
1. Name / Date:  
   Ralph / 2018-02-26


2. Java version used, if not the official version for the class:
   build 1.8.0_111-b14


3. Command-line compilation instructions:
   > javac *.java  


4. Instructions to run this program:
   In shell window, type in:
   > java AsyncJokeClientAdmin <serverA port> <serverB port>

   When starting the administration client, you can pass in port number of 
   servers to client admin.
   The default server IP address is "localhost".
   Server A is the default server with a port number of 5245.

   You can pass in two server port numbers. If there are two
   arguments passed in, the client admin can switch between two 
   servers A and B.

   For example, if starting only server A at port 5245, 
   you should type:
   > java JokeClient 5245
   ("5245" can be omitted as default)

   If you allow the client to switch between servers A and
   B, you can type in a second port number as second argument:
   > java JokeClient 5245 5246
   

5. List of files needed for running the program.
   a. AsyncJokeServer.java
   b. AsyncJokeClient.java
   c. AsyncJokeClientAdmin.java

----------------------------------------------------------*/

import java.io.*;	//Import I/O libraries
import java.net.*;	//Import networking libraries

/**
 *	This is the Client Admin class.
 */
public class AsyncJokeClientAdmin {
	
	/**
	 * Main driver to run a Client Administrator.
	 * @param args
	 */
	public static void main (String args[]) {
		
		int port1 = 5245;
		int port2 = 5246;
		
		//Server addresses and ports
		String server = "localhost";
		int port = port1;
		//Signal indicating if there is a secondary server
		boolean second = false;		
		
		if (args.length > 0) {
			port1 = Integer.parseInt(args[0]);		//Read first server IP address
			if (args.length > 1) {
				second = true;		                //Set signal to be true
				port2 = Integer.parseInt(args[1]);	//Read second server IP address
			}
		}
		
		//Prompt msg on ClientAdmin console while starting
		System.out.println("Ralph Zhang's Asynchronous Joke ClientAdmin started up.");
		System.out.println("Server A at port " + port1);
		if (second) {
			System.out.println("Server B at port " + port2);
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
						+ " ¡°quit¡± to end: ");     //Prompt for user input
				System.out.flush();				     //Clear output stream
				
				input = in.readLine();			     //Read user's input
				
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
						port = (isFirst ? port2 : port1);
						isFirst = !isFirst;		//Switch signal
						System.out.println("Now communicating with Server " + (isFirst? "A":"B"));
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
				String sv = (port == 5245)? "Server A" : "Server B";
				System.out.println(sv + readInLine.substring(6)); 	//Print message on client console
			}
			sock.close();	//Conversation ends, close the socket on ClientAdmin side
		}	catch (IOException e) {
			System.out.println("Socket error.");
			e.printStackTrace();
		}
	}
}
