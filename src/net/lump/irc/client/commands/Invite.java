package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

/**
 * Invite.
 *
 * This invites a nick to a channel.
 *
 * @author troy
 * @version $Id: Invite.java,v 1.1 2010/05/01 20:22:04 troy Exp $
 */
public class Invite extends Command {
   Nick nick;
   Channel channel;

   public Invite(Nick nick, Channel channel) {
      this.nick = nick;
      this.channel = channel;
   }

   @Override
   protected String[] getArgs() {
      return new String[]{nick.name(), channel.getName()};
   }
}
