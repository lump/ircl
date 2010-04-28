package net.lump.irc.client.commands;

/**
 * Password.
 *
 * @author troy
 * @version $Id: Pass.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public class Pass extends Command {
   String password;

   public Pass(String password) {
      super();
      this.password = password;
   }

   @Override
   public String[] getArgs() {
      return new String[]{password};
   }
}
