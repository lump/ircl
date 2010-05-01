package net.lump.irc.client.commands;


/**
 * Terminate the connection.
 *
 * @author troy
 * @version $Id: Quit.java,v 1.2 2010/05/01 20:22:04 troy Exp $
 */
public class Quit extends Command {

   String message;

   public Quit(String message) {
      this.message = message;
   }

   @Override
   protected String[] getArgs() {
      return new String[]{":"+message};
   }
}
