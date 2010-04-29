package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

import java.util.Arrays;

/**
 * Part from a channel or channels.
 *
 * @author troy
 * @version $Id: Part.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public class Part extends Command{
   private Channel[] channels;

   public Part(Channel... c) {
      super();
      channels = c;
   }

   @Override
   public String[] getArgs() {
      return new String[]{join(Arrays.asList(channels), ",")};
   }

}
