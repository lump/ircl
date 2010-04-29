package net.lump.irc.client.commands;

/**
 * Privmsg.
 *
 * @author troy
 * @version $Id: Privmsg.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public class Privmsg extends Command {
   String target;
   String message;

   public Privmsg(String target, String message) {
      this.target = target;
      this.message = message;
   }

   @Override
   public String[] getArgs() {
      return new String[]{target, ":"+(message == null ? "" : message)};
   }
}
