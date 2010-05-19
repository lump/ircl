package net.lump.irc.client.commands;

import java.util.Arrays;

/**
 * List.
 *
 * @author M. Troy Bowman
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
