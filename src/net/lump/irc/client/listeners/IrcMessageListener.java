package net.lump.irc.client.listeners;

// Copyright SOS Staffing 2010

/**
 * .
 *
 * @author troy
 * @version $Id: IrcMessageListener.java,v 1.1 2010/04/30 01:48:03 troy Exp $
 */
public interface IrcMessageListener {
   public void handleMessage(String line);
   public void handleDisconnected(String line);
}
