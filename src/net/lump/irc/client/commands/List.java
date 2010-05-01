package net.lump.irc.client.commands;

import java.util.Arrays;

/**
 * List.
 *
 * @author troy
 * @version $Id: List.java,v 1.1 2010/05/01 20:22:04 troy Exp $
 */
public class List extends Command {

   String[] channels;
   public List() {}


   public List(String... channels) {
      this.channels = channels;
   }

   @Override
   protected String[] getArgs() {
      if (channels == null || channels.length == 0) return new String[0];
      else return new String[]{join(Arrays.asList(channels), ",")};
   }
}
