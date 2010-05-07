package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

import java.util.Arrays;

/**
 * Part from a channel or channels.
 *
 * @author troy
 * @version $Id: Part.java,v 1.3 2010/05/07 18:42:22 troy Exp $
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
