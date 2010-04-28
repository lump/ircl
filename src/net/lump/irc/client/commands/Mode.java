package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

import java.util.ArrayList;

/**
 * .
 *
 * @author troy
 * @version $Id: Mode.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public class Mode extends Command {
   Nick nick;
   User.Mode[] userModes;

   Boolean add;

   Channel channel;
   Channel.Mode[] channelModes;

   public Mode(Nick nick, Boolean add, User.Mode... modes) {
      super();
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
   public String[] getArgs() {
      if (nick != null)
         if (userModes.length > 0) return
             new String[]{nick.name(), (add ? User.Mode.addString(userModes) : User.Mode.removeString(userModes))};
         else return new String[]{nick.name()};
      else if (channel != null)
         return new String[]{}; // todo: fill in
      else return new String[]{};
   }
}
