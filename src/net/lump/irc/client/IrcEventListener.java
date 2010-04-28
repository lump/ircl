package net.lump.irc.client;

/**
 * Allows you to handle events from the server.
 *
 * @author troy
 * @version $Id: IrcEventListener.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public interface IrcEventListener {
   public void handleResponse(String server, Response r, String[] args, String message);
   public void handleServerMessage(String rawMessage);
   public void handlePing(String server);
   public void handleNick(String oldnick, String user, String newnick);
}
