package net.lump.irc.client.commands;

// Copyright SOS Staffing 2010

/**
 * Away.
 *
 * @author troy
 * @version $Id: Away.java,v 1.1 2010/04/30 01:48:03 troy Exp $
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
