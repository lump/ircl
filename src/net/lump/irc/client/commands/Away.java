package net.lump.irc.client.commands;

/**
 * Away.
 *
 * @author troy
 * @version $Id: Away.java,v 1.3 2010/05/01 20:22:04 troy Exp $
 */
public class Away extends Command {

   String message;

   /**
    * @param message the away message, null means not away.
    */
   public Away(String message) {
      this.message = message;
   }

   @Override
   protected String[] getArgs() {
      return message == null ? new String[0] : new String[]{":" + message};
   }
}
