package net.lump.irc.client.commands;

import net.lump.irc.client.exception.IllegalNickException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Nick.
 *
 * @author troy
 * @version $Id: Nick.java,v 1.2 2010/04/29 03:06:09 troy Exp $
 */
public class Nick extends Command {
   private String nick;
   private static final int maxLength = 15;

   private Nick(){}
   public Nick(String nick) throws IllegalNickException {
      super();
      if (!nick.matches("^[A-Za-z].*")) throw new IllegalNickException("nicknames must start with a letter");
      if (nick.length() > maxLength) nick = nick.substring(0, 15);
      this.nick = nick;
   }

   public static Nick newNickDisregardingException(String nick) {
      Nick t = new Nick();
      t.nick = nick;
      return t;
   }

   public String name() {
      return nick;
   }

   /**
    * Creates a new Nick with a number on the end, incremented up from this nick.  Safely deals with size limits.
    * @return a new Nick
    */
   public Nick increment() {
      String nextVal = "1";
      String tempNick = nick;
      Matcher m = Pattern.compile("^(.*\\D)(\\d+)+$").matcher(nick);
      if (m.matches()) {
         tempNick = m.group(1);
         nextVal = String.valueOf(Long.parseLong(m.group(2)) + 1);
      }

      if ((tempNick + '-' + nextVal).length() <= maxLength) tempNick += '-' + nextVal;
      else if ((tempNick + nextVal).length() <= maxLength) tempNick += nextVal;
      else tempNick = tempNick.substring(0, maxLength - nextVal.length()) + nextVal;

      return newNickDisregardingException(tempNick);
   }

   public String[] getArgs() {
      return new String[]{":" + nick};
   }
}
