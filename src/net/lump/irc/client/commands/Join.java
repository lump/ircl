package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

/**
 * Join a channel or channels.
 *
 * @author troy
 * @version $Id: Join.java,v 1.2 2010/04/30 22:27:04 troy Exp $
 */
public class Join extends Command {

   Channel[] channels;

   public Join(Channel... channels) {
      super();
      this.channels = channels;
   }

   public Channel[] getChannels() {
      return channels;
   }

   @Override
   public String[] getArgs() {
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
