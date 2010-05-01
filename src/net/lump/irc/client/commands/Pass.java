package net.lump.irc.client.commands;

/**
 * Password.
 *
 * @author troy
 * @version $Id: Pass.java,v 1.2 2010/05/01 20:22:04 troy Exp $
 */
public class Pass extends Command {
   String password;

   public Pass(String password) {
      this.password = password;
   }

   @Override
   protected String[] getArgs() {
      return new String[]{password};
   }
}
