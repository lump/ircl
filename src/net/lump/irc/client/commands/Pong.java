package net.lump.irc.client.commands;


/**
 * Pong.
 *
 * @author M. Troy Bowman
 */
public class Pong extends Command {
   String server;
   String message;

   public Pong(String server, String message) {
      this.server = server;
   }

   @Override
   protected String[] getArgs() {
      if (message == null) return new String[]{server};
      else return new String[]{server, ":"+message};
   }
}
