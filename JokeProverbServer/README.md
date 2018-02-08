# Joke/Proverb Server

## JokeServer, JokeClient:

JokeClient connects to JokeServer with a request to the server, and get respond with a joke or a proverb, depending on the server's current mode.

For each conversation with a client, complete sets of four jokes [JA, JB, JC, JD] and, independently, four proverbs [PA, PB, PC, PD] are returned in random order to the client one at a time. 

When all of the jokes or proverbs in each set have been returned, then start over at the beginning of the set. 

Multiple clients [theoretcially, many thousands of clients] run simultaneously, and each client conversation is completely independent of all other client conversations.

## JokeClientAdmin:

JokeClientAdmin connects to the server and sets it in Joke Mode [the default], or Proverb Mode for all subsequent client connections within the respective conversations.