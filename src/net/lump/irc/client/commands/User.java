package net.lump.irc.client.commands;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User.
 *
 * @author troy
 * @version $Id: User.java,v 1.5 2010/05/07 18:42:22 troy Exp $
 */
public class User extends Command {
   String user;
   Mode[] modes;
   String realName;

   public enum Mode {
      away('a', false, false), // use Away to add/remove
      invisible('i', true, true, 8),
      wallops('w', true, true, 4),
      restricted('r', true, false), // can restrict self, can't unrestrict
      operator('o', false, true), // can deop, use Oper to set
      localOperator('O', false, true), // can deop, use Oper to set.
      serverNotices('s', false, false); // obsolete

      private char modeChar;
      private int userBit;
      private boolean addable = false;
      private boolean removable = false;
      private static final HashMap<Character, Mode> reverseMap = new HashMap<Character, Mode>();

      private Mode(char m, boolean addable, boolean removable) {
         this(m, addable, removable, 0);
      }
      private Mode(char m, boolean addable, boolean removable, int bit) {
         modeChar = m;
         this.addable = addable;
         this.removable = removable;
         userBit = bit;
      }

      public static int userBits(Mode... modes) {
         int mode = 0;
         for (Mode m : modes) mode |= m.userBit;
         return mode;
      }

      public static String addString(Mode... modes) {
         String out = "+";
         for (Mode m : modes) if (m.addable) out += m.modeChar;
         return out;
      }

      public static String removeString(Mode... modes) {
         String out = "-";
         for (Mode m : modes) if (m.removable) out += m.modeChar;
         return out;
      }

      public static String toString(Mode... modes) {
         String out = "";
         for (Mode m : modes) out += m.modeChar;
         return out;
      }

      public static Mode modeOf(char c) {
         // bootstrap revmap because enums we can't refer to static hashmaps in their constructors
         if (reverseMap.isEmpty())
            synchronized (reverseMap) { // only generate this once, and make other threads wait until it's generated.
               if (reverseMap.isEmpty()) for (Mode m : Mode.values()) reverseMap.put(m.modeChar, m);
            }
         return reverseMap.get(c);
      }

      public static Mode[] parseString(String modes) {
         ArrayList<Mode> out = new ArrayList<Mode>();
         if (modes.startsWith("+") || modes.startsWith("-")) modes = modes.substring(1);
         for (int x=0; x<modes.length(); x++) {
            Mode m = modeOf(modes.charAt(x));
            if (m != null) out.add(m);
         }
         return out.toArray(new Mode[out.size()]);
      }
   }

   public User(String user, String realName, Mode... modes) {
      this.user = (user == null ? System.getProperty("user.name") : user);
      this.realName = realName == null ? this.user : realName;
      this.modes = modes;
   }

   public User(String realName, Mode... modes) {
      this(null, realName, modes);
   }

   public User setModes(Mode... modes) {
      this.modes = modes;
      return this;
   }

   public String getUser() {
      return user;
   }

   public Mode[] getModes() {
      return modes;
   }

   public String getRealName() {
      return realName;
   }

   @Override
   protected String[] getArgs() {
      return new String[]{user, Integer.toString(Mode.userBits(modes)), "*", ":"+realName };
   }
}
