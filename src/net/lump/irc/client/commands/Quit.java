package net.lump.irc.client.commands;


/**
 * Terminate the connection.
 *
 * @author troy
 * @version $Id: Quit.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public class Quit extends Command {

   String message;

   public Quit(String message) {
      this.message = message;
   }

   @Override
   public String[] getArgs() {
      return new String[]{":"+message};
   }
}
