package net.lump.irc.client.commands;

/**
 * Oper.
 *
 * @author troy
 * @version $Id: Oper.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public class Oper extends Command {
   String name;
   String password;

   public Oper(String name, String password) {
      super();
      this.name = name;
      this.password = password;
   }

   @Override
   public String[] getArgs() {
      return new String[]{name, password};
   }
}
