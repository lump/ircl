package net.lump.irc.client.commands;

/**
 * Oper.
 *
 * @author troy
 * @version $Id: Oper.java,v 1.2 2010/05/01 20:22:04 troy Exp $
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
