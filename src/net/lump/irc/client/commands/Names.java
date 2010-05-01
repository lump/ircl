package net.lump.irc.client.commands;

/**
 * Names.
 *
 * @author troy
 * @version $Id: Names.java,v 1.2 2010/05/01 20:22:04 troy Exp $
 */
public class Names extends Command {
   String target = null;

   public Names() {
   }

   public Names(String target) {
      this.target = target;
   }

   @Override
   protected String[] getArgs() {
      if (target == null) return new String[0];
      else return new String[]{target};
   }
}
