package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

/**
 * Privmsg.
 *
 * @author M. Troy Bowman
 */
public class Privmsg extends Command {
   String target;
   String message;

   public Privmsg(String target, String message) {
      this.target = target;
      this.message = message;
   }

   public String getTarget() {
      return target;
   }

   public String getMessage() {
      return message;
   }

   public boolean targetIsChannel() {
     return Channel.isValidChannelName(target);
   }

   @Override
   protected String[] getArgs() {
      return new String[]{target, ":"+(message == null ? "" : message)};
   }
}
