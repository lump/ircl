package net.lump.irc.client.commands;

/**
 * A Nick.
 *
 * @author troy
 * @version $Id: Nick.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public class Nick extends Command {
   private String nick;

   public Nick(String nick) {
      super();
      this.nick = nick;
   }

   public String name() {
      return nick;
   }

   public String[] getArgs() {
      return new String[]{":" + nick};
   }
}
