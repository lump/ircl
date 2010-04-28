package net.lump.irc.client.commands;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User.
 *
 * @author troy
 * @version $Id: User.java,v 1.1 2010/04/28 03:12:47 troy Exp $
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
      private static HashMap<Character, Mode> revMap = new HashMap<Character, Mode>();

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
         return addString();
      }

      public static Mode modeOf(char c) {
         // bootstrap revmap because enums we can't refer to static hashmaps in their constructors
         if (!revMap.containsKey(c))
            for (Mode m : Mode.values())
               if (!revMap.containsKey(m.modeChar)) revMap.put(m.modeChar, m);
         return revMap.get(c);
      }

      public Mode[] parseString(String modes) {
         ArrayList<Mode> out = new ArrayList<Mode>();
         if (modes.startsWith("+"))  {
            for (int x=1; x<modes.length(); x++) {
               Mode m = modeOf(modes.charAt(x));
               if (m != null) out.add(m);
            }
         }
         return out.toArray(new Mode[out.size()]);
      }
   }

   public User(String user, String realName, Mode... modes) {
      super();
      this.user = user;
      this.realName = realName;
      this.modes = modes;
   }

   @Override
   public String[] getArgs() {
      return new String[]{user, Integer.toString(Mode.userBits(modes)), "*", ":"+realName };
   }
}
