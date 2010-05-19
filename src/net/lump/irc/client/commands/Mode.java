package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

/**
 * Mode.
 *
 * todo: Mode needs some ironing out
 *
 * @author M. Troy Bowman
 */
public class Mode extends Command {
   Nick nick;
   User.Mode[] userModes;

   Boolean add;

   Channel channel;
   Channel.Mode[] channelModes;

   public Mode(Nick nick, Boolean add, User.Mode... modes) {
      this.nick = nick;
      this.add = add;
      this.userModes = modes;
   }

   public Mode(Nick nick) {
      this(nick, null);
   }

   public Mode(Channel channel, Channel.Mode... modes) {
      super();
      this.channel = channel;
      this.channelModes = modes;
   }


   @Override
   protected String[] getArgs() {
      if (nick != null)
         if (userModes.length > 0) return
             new String[]{nick.name(), (add ? User.Mode.addString(userModes) : User.Mode.removeString(userModes))};
         else return new String[]{nick.name()};
      else if (channel != null)
         return new String[]{}; // todo: fill in
      else return new String[]{};
   }
}
