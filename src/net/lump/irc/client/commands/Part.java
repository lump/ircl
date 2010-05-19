package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

import java.util.Arrays;

/**
 * Part from a channel or channels.
 *
 * @author M. Troy Bowman
 */
public class Part extends Command {
   private Channel[] channels;

   public Part(Channel... c) {
      channels = c;
   }

   @Override
   protected String[] getArgs() {
      return new String[]{join(Arrays.asList(channels), ",")};
   }
}
