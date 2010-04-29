package net.lump.irc.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This models an IRC message prefix.
 *
 * @author troy
 * @version $Id: Prefix.java,v 1.1 2010/04/29 03:06:09 troy Exp $
 */
public class Prefix {
   String host;
   String nick;
   String user;

   private static final Pattern prefixPattern = Pattern.compile("^(\\S+?)(?:(?:!(\\S+))?@(\\S+))?$");

   public String getHost() {
      return host;
   }

   public Prefix setHost(String host) {
      this.host = host;
      return this;
   }

   public String getNick() {
      return nick;
   }

   public Prefix setNick(String nick) {
      this.nick = nick;
      return this;
   }

   public String getUser() {
      return user;
   }

   public Prefix setUser(String user) {
      this.user = user;
      return this;
   }

   public Prefix parseString(String prefix) {
      if (prefix != null) {
         Matcher prefixMatcher = prefixPattern.matcher(prefix);
         if (prefixMatcher.matches()) {
            if (prefixMatcher.group(3) != null) {
               setHost(prefixMatcher.group(3));
               setNick(prefixMatcher.group(1));
               if (prefixMatcher.group(2) != null)
                  setUser(prefixMatcher.group(2));
            }
            else  setHost(prefixMatcher.group(1));
         }
      }

      return this;
   }

   @Override
   public String toString() {
      String out = host;
      if (nick != null && nick.length() != 0) {
         String prefix = nick;
         if (user != null && user.length() > 0) prefix += '!' + user;
         prefix += '@';
      }
      return out;
   }
}
