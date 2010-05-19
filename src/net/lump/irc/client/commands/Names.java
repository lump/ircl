package net.lump.irc.client.commands;

/**
 * Names.
 *
 * @author M. Troy Bowman
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
