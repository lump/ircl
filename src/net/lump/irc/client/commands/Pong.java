package net.lump.irc.client.commands;


/**
 * Pong.
 *
 * @author troy
 * @version $Id: Pong.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public class Pong extends Command {
   String server;
   String message;

   public Pong(String server, String message) {
      super();
      this.server = server;
   }

   @Override
   public String[] getArgs() {
      if (message == null) return new String[]{server};
      else return new String[]{server, ":"+message};
   }
}
