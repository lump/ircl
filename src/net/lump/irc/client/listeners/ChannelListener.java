package net.lump.irc.client.listeners;

import net.lump.irc.client.Channel;
import net.lump.irc.client.Prefix;

/**
 * Pre-filtered channel specific events.
 *
 * @author troy
 * @version $Id: ChannelListener.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public interface ChannelListener {
   public void onNames(String[] names);
   public void onJoin(Prefix prefix, String message);
   public void onTopic(String topic);
   public void onMode(Channel.Mode[] modes, String[] args);
   public void onPart(Prefix prefix, String message);
   public void onQuit(Prefix prefix, String message);
   public void onPrivmsg(Prefix prefix, String message);
   public void onNotice(Prefix prefix, String message);
}
