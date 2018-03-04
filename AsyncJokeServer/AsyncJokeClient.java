/*--------------------------------------------------------
1. Name / Date:  
   Ralph / 2018-02-26


2. Java version used, if not the official version for the class:
   build 1.8.0_111-b14


3. Command-line compilation instructions:
   > javac *.java 


4. Instructions to run this program:
   In different shell windows, type in:
   window 1: > java AsyncJokeClient <serverA port> <serverB port>
   ....
   (We can start as many clients as we want in different window.)

   When starting a new client, you can pass in port number of 
   servers to client.
   The default server IP address is "localhost".
   Server A is the default server with a port number of 3245.

   You can pass in two server port numbers. If there are two
   arguments passed in, the client can switch between two 
   servers A and B.

   For example, if starting only server A at port 3245, 
   you should type:
   > java JokeClient 3245
   ("3245" can be omitted as default)

   If you allow the client to switch between servers A and
   B, you can type in a second port number as second argument:
   > java JokeClient 3245 3246


5. List of files needed for running the program.
   a. AsyncJokeServer.java
   b. AsyncJokeClient.java
   c. AsyncJokeClientAdmin.java

----------------------------------------------------------*/

import java.io.*;	//Import I/O libraries
import java.net.*;	//Import networking libraries
import java.util.LinkedList;
import java.util.Queue;

/**
 *	This is the Client class.
 */
public class AsyncJokeClient {
	
	//Create a record to store the state (Joke/Proverb) of each client
	//Contains two values for communication to possible two independent servers
	//Each value is an integer in range of 0(0x00) to 255(0xff), initial value 0
	public static int[] infoArray = {0,0};
	
	//Create a queue to store returning jokes
	public static Queue<String> jokes = new LinkedList<String>();
	
	/**
	 * Main driver to run a client.
	 * @param args
	 */
	public static void main (String args[]) {
		
		//Server addresses and ports
		String server = "localhost";
		int port1 = 3245;
		int port2 = 3246;
		//Signal indicating if there is a secondary server
		boolean second = false;		
		
        //Read in args as port numbers
		if (args.length > 0) {
			port1 = Integer.parseInt(args[0]);
			if (args.length > 1) {
				port2 = Integer.parseInt(args[1]);
				second = true;		//Set signal to be true
			}
			
		}
		
		//Create socket for UDP connection to receive jokes
		DatagramSocket sockU1 = null;
		DatagramSocket sockU2 = null;
		try {
			//+1000, to avoid conflict of port number
			sockU1 = new DatagramSocket(port1 + 1000);
			//Prompt msg on client console while starting
			System.out.println("Ralph Zhang's Asynchronous Joke Client started with bindings:");
			System.out.println("Server A at "+ port1);
			if (second) {
				sockU2 = new DatagramSocket(port2 + 1000);
				System.out.println("Server B at "+ port2);
			}
		} catch (Exception e) {
			System.out.println("UDP socket error. Connection refused.");
		}

		//Buffer for user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		//Try/catch block to handle I/O exceptions during reading client's input
		try {
			String input = "";			//User input string
			boolean isFirst = true; 	//Signal indicating if on server1
			
			int info;			
			while (true) {
				if (!jokes.isEmpty()) {
					System.out.println("\n" + jokes.poll());
				}
				//Prompt for user input
				System.out.print("\nEnter A or B to get a joke or proverb, "
						+ "or numbers for sum, quit to end: ");	
				System.out.flush();		//Clear output stream
				input = in.readLine();	//Read user input

				//Quit if user inputs 'quit'
				if (input.indexOf("quit") >= 0) {
					System.out.println("Cancelled by user request.");
					break;
				}
				
				//Request a response from server B
				else if (input.equals("B")) {
					//If no secondary server, prompt msg on console
					if (!second) System.out.println("No secondary server being used.");
					//Otherwise, switch server address and port
					else {
						isFirst = false;	//Switch to server B
						info = infoArray[1];
						//Connect to server, get response and update state information
						getResp(server, port2, sockU2, info, isFirst);
					}
				}
				
				//Request a response from server A
				else if (input.equals("A")) {
					isFirst = true;		//Switch to server A
					info = infoArray[0];
					//Connect to server, get response and update state information
					getResp(server, port1, sockU1, info, isFirst);
				}
				
				//Add two numbers
				else {
					String[] nums = input.split("\\s+");
					if (nums.length == 2) {
						int n1 = Integer.parseInt(nums[0]);
						int n2 = Integer.parseInt(nums[1]);
						System.out.println("Your sum is: " + (n1+n2));
					}
				}
				
			}
		}	catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Method to build connection with server to get response.
	 * Basic routine:
	 * send request (output stream) to server using TCP/IP connection.
     * start a new thread waiting for responses from server using UDP connection.
	 * @param server: input, server address
	 * @param port: input, port for connection
	 * @param sockU: UDP socket to get resp from server
	 * @param info: input, state record
	 * @param isFirst: input, state record
	 */
	static void getResp (String server, int port, DatagramSocket sockU, int info, boolean isFirst) {
		
		PrintStream toServer = null;	//Declare an output stream
		Socket sockS;					//TCP/IP connection to Server
		
		try {
			//Send request to Server through
			sockS = new Socket(server, port);	//Build TCP/IP connection to server
			
			toServer = new PrintStream(sockS.getOutputStream());
			toServer.println(info);	//Send state of record to server
			toServer.flush();		//Clear output stream
			sockS.close();
			
			new Thread(new UDPServer(sockU, info, isFirst)).start();
			
		}	catch (IOException e) {
			System.out.println("TCP/IP socket error. Connection refused.");
		}
	}
}

class UDPServer extends Thread {
	DatagramSocket sock;
	int info;
	boolean isFirst;
	
	UDPServer (DatagramSocket sock, int info, boolean isFirst) {
		this.sock = sock;
		this.info = info;
		this.isFirst = isFirst;
	}
	
	public void run(){
		//Connect to Server back for response
		DatagramPacket fromServer; 	//Declare an input datagram
		
		try {
            //Connect back to server to get joke/proverb message

			byte[] readIn = new byte[1024];		//Bytes array to store data
			fromServer = new DatagramPacket(readIn, readIn.length);
			sock.receive(fromServer);	

			//Read responses
			String resp = new String(readIn, 0, fromServer.getLength());
			//Parse string array, format: resp = joke + "xxxx" + info
			String[] resps = resp.split("xxxx");
            
			//Store response Joke
			AsyncJokeClient.jokes.add(resps[0]);
            
            //Get updated client's record state
			info = Integer.parseInt(resps[1]);	
			
			//sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Update records of one client
		AsyncJokeClient.infoArray[isFirst? 0:1] = info;
	}
	
	
}
