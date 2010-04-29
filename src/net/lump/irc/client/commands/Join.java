package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;
import net.lump.irc.client.exception.IllegalChannelException;

/**
 * Join a channel or channels.
 *
 * @author troy
 * @version $Id: Join.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public class Join extends Command {

   Channel[] channels;

   public Join(Channel... channels) throws IllegalChannelException {
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
