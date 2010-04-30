package net.lump.irc.client.commands;

/**
 * Names.
 *
 * @author troy
 * @version $Id: Names.java,v 1.1 2010/04/30 22:27:04 troy Exp $
 */
public class Names extends Command {
   String target = null;

   public Names() {
     super();
   }

   public Names(String target) {
      this();
      this.target = target;
   }

   @Override
   public String[] getArgs() {
      if (target == null) return new String[0];
      else return new String[]{target};
   }
}
