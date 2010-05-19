package net.lump.irc.client.listeners;

import net.lump.irc.client.Prefix;
import net.lump.irc.client.Response;
import net.lump.irc.client.commands.CommandName;

/**
 * Allows you to handle events from the server.
 *
 * @author M. Troy Bowman
 */
public interface IrcEventListener {

   /**
    * Every server {@link Response} will go through this method.
    *
    * @param prefix The prefix consists of Host, Nick, and User, each given only where applicable.
    * @param r The {@link Response} code.
    * @param args The arguments for the response code.  Refer to the RFCs for what should be returned.
    * @param message If the code has a message, it will be placed here.
    */
   public void handleResponse(Prefix prefix, Response r, String[] args, String message);

   /**
    * The server will send IRC commands to the client.
    *
    * @param prefix The prefix consists of Host, Nick and User, each given only if applicable.
    * @param c The {@link CommandName}
    * @param args The arguments for the command.  Refer to the RFCs for what should be returned.
    * @param message If the command contains a message, it will be placed here.
    */
   public void handleCommand(Prefix prefix, CommandName c, String[] args, String message);

   /**
    * This is separate from handleResponse because it has to be handled.
    * This method will be called on any nick problem.
    *
    * @param prefix The prefix consists of Host, Nick, and User, each given only where applicable.
    * @param r The {@link Response} code.
    * @param args Response Arguments
    * @param message Message
    */
   public void handleNickProblem(Prefix prefix, Response r, String[] args, String message);

   /**
    * A method to perform upon connection (pre registration).
    */
   public void onConnect();

   /**
    * A method to perform upon registration.
    */
   public void onRegistration();

   /**
    * This will be called on disconnection.
    *
    * @param args Arguments
    * @param message Message
    */
   public void onDisconnect(String[] args, String message);
}
