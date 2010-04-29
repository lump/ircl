package net.lump.irc.client.listeners;

import net.lump.irc.client.Prefix;
import net.lump.irc.client.Response;
import net.lump.irc.client.commands.CommandName;

/**
 * Allows you to handle events from the server.
 *
 * @author troy
 * @version $Id: IrcEventListener.java,v 1.2 2010/04/29 03:47:26 troy Exp $
 */
public interface IrcEventListener {
   public void handleResponse(Prefix prefix, Response r, String[] args, String message);
   public void handleCommand(Prefix prefix, CommandName c, String[] args, String message);
   /** This is separate from handleResponse because it has to be handled */
   public void handleNickNameInUse(String[] args, String message);
   public void handleDisconnected(String[] args, String message);
}
