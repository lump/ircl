package net.lump.irc.client.listeners;

import net.lump.irc.client.Prefix;
import net.lump.irc.client.Response;
import net.lump.irc.client.commands.CommandName;

/**
 * This is a skeleton class which has empty handlers, to allow you to implement what you please.
 *
 * @author M. Troy Bowman
 */
public abstract class AbstractIrcEventListener implements IrcEventListener {
   public void handleResponse(Prefix prefix, Response r, String[] args, String message) { }
   public void handleCommand(Prefix prefix, CommandName c, String[] args, String message) { }
   public void onConnect() { }
   public void onRegistration() { }
}
