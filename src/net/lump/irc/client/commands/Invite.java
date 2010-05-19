package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

/**
 * Invite.
 *
 * This invites a nick to a channel.
 *
 * @author M. Troy Bowman
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
