/*--------------------------------------------------------
1. Name / Date:  
   Ralph / 2018-01-20


2. Java version used, if not the official version for the class:
   build 1.8.0_111-b14


3. Command-line compilation instructions:
   > javac JokeServer.java 


4. Instructions to run this program:
   In two different shell windows, type in:
   window 1: > java JokeServer
   window 2: > java JokeServer secondary
   There can be at most two servers running.

   The primary server runs by default.
   If the argument "secondary" is indicated, the secondary server starts.


5. List of files needed for running the program.
   a. JokeServer.java
   b. JokeClient.java
   c. JokeClientAdmin.java

----------------------------------------------------------*/

import java.io.*;	//Import I/O libraries
import java.net.*;	//Import networking libraries

/**
 * This is the Server class.
 */
public class JokeServer {

	//Define booleans containing server Mode and On/Off information
	//Default mode: Joke
	public static boolean jokeMode = true;	
	//Default on/off: on
	public static boolean turnOn = true;
	
	/**
	 * Main driver to run a server.
	 * @param args
	 * @throws IOException
	 */
	public static void main (String args[]) throws IOException {
				
		//Default port, portc - client port, porta - admin port
		int portc1 = 4545;
		int portc2 = 4546;
		int porta1 = 5050;
		int porta2 = 5051;
		//Signal indicating if current server is the secondary server
		boolean second = false;

		//Default server: server1 
		int portc = portc1; 
		int porta = porta1; 
		
		//Start server2
		if (args.length > 0 && args[0].equals("secondary")) {
			portc = portc2;
			porta = porta2;
			second = true;
		}

		//Prompt msg on server console once starting server
		System.out.println("Ralph Zhang's Joke Server beta starting up.");
		System.out.println("Starting " + (second ? "second" : "first" ) + " server.");
		System.out.println("Listening to Client at port " + portc + ".");
		System.out.println("Listening to ClientAdmin at port " + porta + ".");
		
		//Maximum requests number in queue from Client to Server
		int queueLen = 6;
		//Socket for connection with Client
		Socket sock;
		//Listen to portc for client's connection
		ServerSocket servSock = new ServerSocket(portc, queueLen);
		
		//Create a thread waiting for asynchronous call from ClientAdmin
		//Pass in a reference of Client-Server socket for 'shutdown' manipulation
	    ModeServer admin = new ModeServer(porta, servSock);
	    Thread adm = new Thread(admin);		//Create a thread for connection with ClientAdmin
	    adm.start();  						//Run the thread
		
		//Create new workers to work on the connection with Client
		while (turnOn) {
			try {
				sock = servSock.accept();
				//Pass in socket, mode, server state to Worker, see Worker class for details
				new Worker(sock, jokeMode, second).start();	
			}  catch (SocketException e) {
				System.out.println("Server stops listening to Clients.");
			}
		}
	}
}


/**
 * This class is defined as a thread to build connection with ClientAdmin.
 *
 */
class ModeServer implements Runnable {
	
	int port = 5050;	//Class member, indicate port for primary/secondary server
	ServerSocket sockCS;		//A reference of Client-Server socket 
		
	//Parameterized c'tor, 
	ModeServer(int port, ServerSocket sock) {
		this.port = port;  	//Pass in the listening port number of current server
		this.sockCS = sock;	//Pass in a reference of socket
	}
  
	/*
	 * Method of Runnable interface,
	 * handle I/O streams via socket.
	 * Basic routine:
	 * Listen at server's port.
	 * Create ModeWorkers to work on the connection with ClientAdmin
	 */
	public void run(){
		//Prompt msg showing
		System.out.println("Getting into the ModeServer thread.");

		//Maximum requests number in queue from ClientAdmin to Server
		int queueLen = 6;
		//Socket for connection with ClientAdmin
		Socket sock;
		
		//Try/catch block to handle I/O exceptions in socket I/O manipulation 
		try{
			ServerSocket servsock = new ServerSocket(port, queueLen);
			
			//Create new ModeWorkers to work on the connections with ClientAdmin
			while (JokeServer.turnOn) {
				sock = servsock.accept();
				new ModeWorker(sock, sockCS).start(); 
			}
		}catch (IOException ioe) {System.out.println(ioe);}
  }
}
	  

/**
 *  This class is defined to handle socket with ClientAdmin.
 */
class ModeWorker extends Thread {
	
	Socket sock;			//ClientAdmin-ModeWorker(Server) socket
	ServerSocket sockCS;	//A reference of Client-Server socket
	
	//Parameterized c'tor, store sockets information to instances
	ModeWorker (Socket sock, ServerSocket sockCS) {
		this.sock = sock;
		this.sockCS = sockCS;
	}
	
	/**
	 * Method of Runnable interface,
	 * handle I/O streams via socket.
	 * Basic routine:
	 * Get information (input stream) from ClientAdmin.
	 * Switch mode or shut down Server.
	 * Send information (output stream) back to ClientAdmin.
	 */
	public void run(){
		
		BufferedReader fromClientAdmin = null;	//Declare an input stream
		PrintStream toClientAdmin = null;		//Declare an output stream
		
		//Try/catch block to handle I/O exceptions in socket I/O manipulation  
		try {
			
			//Get I/O stream from socket
			fromClientAdmin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toClientAdmin = new PrintStream(sock.getOutputStream());
			

			String input = "";			//String to get input information
			
			boolean turnOff = false;	//Signal indicating if getting 'shutdown' command	
			
			//Try/catch block to handle I/O exceptions during reading ClientAdmin's input
			try {
				input = fromClientAdmin.readLine();	//Get ClientAdmin's input
				
				String msg = "";					//String for prompting msg

				if (input.equals("")) {
					if (JokeServer.jokeMode) {
						msg = "Server has been switched to Proverb Mode.";
					} else {
						msg = "Server has been switched to Joke Mode.";
					}
					//Switch Mode on Server
					JokeServer.jokeMode = !JokeServer.jokeMode;
				}
				
				//Shut down Server when inputting shut down
				else if (input.equals("shutdown")) {
					JokeServer.turnOn = false;	//Change signal on Server
					turnOff = true;				//Change local signal
					msg = "Server stops listening to Clients.";
				}
				//Prompt msg on Server console 
				System.out.println(msg);
				//Prompt msg on Client Admin console 
				toClientAdmin.println(msg);

			}	catch (IOException e) {	
				System.out.println("Read from Client Admin error.");
				e.printStackTrace();
			}

			//Conversation ends, close ClientAdmin-ModeWorker(Server) socket on server side
			sock.close();
			
			//Reach back and kill off parent(Client-Server) socket
			if (turnOff && (sockCS != null)) sockCS.close();
			

		}	catch (IOException e) {	
			System.out.println("Socket error.");
			e.printStackTrace();
		}
	}
}




/**
 *  This class is defined to handle socket with client on server side.
 */
class Worker extends Thread {
	
	Socket sock;				//Client-Worker(Server) socket
	boolean jokeMode = true;	//Signal indicating if in Joke mode
	boolean secondServ = false;	//Signal indicating if on secondary server
	
	//Parameterized c'tor, store socket information to instances
	Worker (Socket s, boolean mode, boolean second) {
		sock = s;
		this.jokeMode = mode;
		this.secondServ = second;
	}
	
	/**
	 * Method of Runnable interface,
	 * handle I/O streams via socket.
	 * Basic routine:
	 * get request (input stream) from client,
	 * look up info (host IP or name) through InetAddress,
	 * send result (output stream) back to client.
	 */
	public void run(){
		
		BufferedReader fromClient = null;	//Declare an input stream
		PrintStream toClient = null;		//Declare an output stream
		
		//Try/catch block to handle I/O exceptions in socket I/O manipulation  
		try {
			
			//Get I/O stream from socket
			fromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toClient = new PrintStream(sock.getOutputStream());
			
			//Declare string to read in input information
			String userName = "";
			String info = "";
			
			//Try/catch block to handle I/O exceptions during reading client's input
			try {
				userName = fromClient.readLine();	//Get client's user name
				info = fromClient.readLine();		//Get client's state record
				
				//Prompt msg on server console 
				if (JokeServer.turnOn) {
					System.out.println("Receiving request from client " + userName + "." );
				}
				
				//Get response according to input information, write into output stream 
				printResp(userName, info, jokeMode, secondServ, toClient);
			}	catch (IOException e) {	
				System.out.println("Read from server error.");
				e.printStackTrace();
			}
			//Conversation ends, close Client-Worker socket on server side
			sock.close();
		}	catch (IOException e) {	
			System.out.println("Socket error.");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Static method to get response and send to client,
	 * @param userName: input, username string sent from client
	 * @param info: input, state record sent from client
	 * @param jokeMode: input, mode of current server
	 * @param secondServ: input, server state (primary or secondary) of current server
	 * @param out: output, stream sent to client
	 */
	static void printResp (String userName, String info, boolean jokeMode, 
			boolean secondServ, PrintStream out) {
		
		//Jokes and Proverbs
		String[] jokes = {
				"A bank is a place that will lend you money, if you can prove that you don't need it.",
				"Evening news is where they begin with 'Good evening', and then proceed to tell you why it isn't.",
				"Hospitality: making your guests feel like they're at home, even if you wish they were.",
				"A bus station is where a bus stops. A train station is where a train stops. On my desk, I have a work station.."};
		String[] proverbs = {"Absence makes the heart grow fonder.",
				"Before criticizing a man, walk a mile in his shoes.",
				"Common sense is not so common.", 
				"Don't fall before you're pushed."};
		
		//Signal indicating if end of a Joke/Proverb cycle
		int cycleSig = 0;

		//Read a byte from info string
		byte b = (byte) Integer.parseInt(info);
		int idx;							//Declare a index number
		char state;
		while (true) {
			idx = (int) (Math.random()*4);	//Randomize a number from 0~3 as index
			int bitIdx = idx;				//Assign value of index as the bit index
			if (!jokeMode) bitIdx += 4;		//Increase bitIdx to 4~7 if in Proverb mode
			
			int bit = (b >> bitIdx) & 0x1;	//Get that bit from state info
			if (bit == 1) continue;			//If the bit is 1, generate another index
			
			//If the bit is 0£¬set it to 1
			b = (byte) (b | (1 << bitIdx));
			
			//Inspect if a cycle completes after bit set
			if (jokeMode) {	
				char cJ = (char) (b & 0x0f);	//Read last 4 bits in Joke mode
				if (cJ == 0x0f) {				//If Joke cycle completes
					cycleSig = 1;				//Set cycle signal to 1
					b = (byte) (b & 0xf0);		//Initialize last 4 bits to 0
				}
			} else {
				char cP = (char) (b & 0xf0);	//Read first 4 bits in Proverb mode
				if (cP == 0xf0) {				//If Proverb cycle completes
					cycleSig = 2;				//Set cycle signal to 2
					b = (byte) (b & 0x0f);		//Initialize first 4 bits to 0
				}
			}
			
			//Convert byte to char and get a new state record
			state = (char) (b & 0xff);
			break;
		}
		
		//Build output message, and write in output stream as the first line
		StringBuffer msg = new StringBuffer();
		msg.append(secondServ? "<S2> " : ""); 	//Primary or secondary server?
		msg.append(jokeMode? "J" : "P");		//Joke or proverb?
		msg.append((char) (idx + 65));			//A, B, C, D
		msg.append(" " + userName + ": ");		//User name
		msg.append(jokeMode? jokes[idx] : proverbs[idx]);	//Joke/Proverb
		out.println(msg.toString());
		
		//Prompt in Server console
		System.out.println("Sending " + (jokeMode? "J" : "P") + 
				((char) (idx + 65)) + " to Client " + userName + ".");
		
		//Write new state record as the second line
		int infoNew = (int) state;
		out.println(infoNew);
		
		//Write announcement for end of cycle as the third line
		if (cycleSig == 1) {
			out.println((secondServ? "<S2> " : "") + "JOKE CYCLE COMPLETED");
		} else if (cycleSig == 2) {
			out.println((secondServ? "<S2> " : "") + "PROVERB CYCLE COMPLETED");
		}

		out.flush();
	}
	
}

