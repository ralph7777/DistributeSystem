# Asynchronous Joke/Proverb Server

AsyncJokeClient connects to AsyncJokeServer with a request to the server, and get respond with a joke or a proverb, depending on the server's current mode.

## AsyncJokeServer
After receiveing a request through TCP/IP connection, break the connection.
Sleep for 40 seconds.
Using UDP, connect to back to (call back to) client to send back the results.

Multiple clients [theoretcially, many thousands of clients] run simultaneously, and each client conversation is completely independent of all other client conversations.

## AsyncJokeClient:
Client sends requests to Server through TCP/IP connection.
This is an ASYNCHRONOUS design: the client maintains a UDP connection, listening for a callback from the server at an available UDP port, which must be sent to any server communicating with this client. The asynchronous design allows client to get other work done while waiting for a result from the server. 
In this program, client displays a prompt for adding two numbers together and displaying the results on the console. Client keeps displaying the prompt to get more numbers, calculate and display the result, until the joke/proverb is ready to display. 
Once receiving a response from server, the client finishes the latest cycle of getting numbers and displaying the results, then it displays a joke/proverb which has been asynchronously returned.

## AsyncJokeClientAdmin:
Client Administrator connects to the server and sets it in Joke Mode [the default], or Proverb Mode for all subsequent client connections within the respective conversations.