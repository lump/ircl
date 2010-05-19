package net.lump.irc.client.commands;


/**
 * Terminate the connection.
 *
 * @author M. Troy Bowman
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
