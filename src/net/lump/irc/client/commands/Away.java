package net.lump.irc.client.commands;

/**
 * Away.
 *
 * @author troy
 * @version $Id: Away.java,v 1.2 2010/04/30 22:28:58 troy Exp $
 */
public class Away extends Command {

   String message;

   /**
    * @param message the away message, null means not away.
    */
   public Away(String message) {
      super();
      this.message = message;
   }

   @Override
   public String[] getArgs() {
      return message == null ? new String[0] : new String[]{":" + message};
   }
}
