package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

/**
 * Join a channel or channels.
 *
 * @author M. Troy Bowman
 */
public class Join extends Command {

   Channel[] channels;

   public Join(Channel... channels) {
      this.channels = channels;
   }

   public Channel[] getChannels() {
      return channels;
   }

   @Override
   protected String[] getArgs() {
      String chs = "";
      String pws = "";

      for (Channel c : channels) {
         if (chs.length() > 0) { chs += ','; pws += ','; }
         chs += c.getName();
         pws += c.getPassword() == null ? "" : c.getPassword();
      }

      return new String[]{chs, pws};
   }
}
