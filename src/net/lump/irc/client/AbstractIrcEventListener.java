package net.lump.irc.client;

/**
 * This is a skeleton class which has empty handlers, to allow you to implement what you please.
 *
 * @author troy
 * @version $Id: AbstractIrcEventListener.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public abstract class AbstractIrcEventListener implements IrcEventListener {
   public void handleResponse(String server, Response r, String[] args, String message) { }
   public void handleServerMessage(String rawMessage) { }
   public void handlePing(String server) { }
   public void handleNick(String oldnick, String user, String newnick) { }
}
