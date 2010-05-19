package net.lump.irc.client.commands;

/**
 * Password.
 *
 * @author M. Troy Bowman
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
