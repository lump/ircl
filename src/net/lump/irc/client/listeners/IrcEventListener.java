package net.lump.irc.client.listeners;

import net.lump.irc.client.Prefix;
import net.lump.irc.client.Response;
import net.lump.irc.client.commands.CommandName;

import java.net.InetAddress;

/**
 * Allows you to handle events from the server.
 *
 * @author troy
 * @version $Id: IrcEventListener.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public interface IrcEventListener {
   public void handleResponse(Prefix prefix, Response r, String[] args, String message);
   public void handleCommand(Prefix prefix, CommandName c, String[] args, String message);
   /** This is separate from handleResponse because it has to be handled */
   public void handleNickNameInUse(String[] args, String message);
   public void handleDisconnected(InetAddress address);
}
