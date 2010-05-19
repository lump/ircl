package net.lump.irc.client.commands;

/**
 * Oper.
 *
 * @author M. Troy Bowman
 */
public class Oper extends Command {
   String name;
   String password;

   public Oper(String name, String password) {
      this.name = name;
      this.password = password;
   }

   @Override
   protected String[] getArgs() {
      return new String[]{name, password};
   }
}
