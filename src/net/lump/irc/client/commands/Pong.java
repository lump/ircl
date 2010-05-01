package net.lump.irc.client.commands;


/**
 * Pong.
 *
 * @author troy
 * @version $Id: Pong.java,v 1.2 2010/05/01 20:22:04 troy Exp $
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
