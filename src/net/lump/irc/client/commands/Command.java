package net.lump.irc.client.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Command skeleton.
 *
 * @author troy
 * @version $Id: Command.java,v 1.2 2010/04/29 03:06:09 troy Exp $
 */
public abstract class Command {
   CommandName commandName;

   Command() {
      try {
         commandName =  CommandName.valueOf(this.getClass().getSimpleName().toUpperCase());
      } catch (IllegalArgumentException e) {
         commandName = CommandName.UNKNOWN;
      }
   }

   public CommandName getCommandName() {
      return commandName;
   }

   public abstract String[] getArgs();

   public String toString() {
      if (commandName != CommandName.UNKNOWN)
         return commandName.name() + " " + join(Arrays.asList(getArgs()), " ");
      else return "";
   }

  /**
    * A utility to join a collection into a string with a specified delimiter between each entry.
    *
    * @param entities  each entity to be joined, .toString() will be called on them
    * @param delimiter the string to be placed between each entity
    * @return String
    */
   public static String join(Collection entities, String delimiter)
   {
      // if it's zero length, return an empty string.
      if (entities.size() == 0)
      {
         return "";
      }

      // start an iterator and step through it.
      final Iterator it = entities.iterator();
      final StringBuilder sb = new StringBuilder(it.next().toString());
      while (it.hasNext())
      {
         final Object nextVal = it.next();
         sb.append(delimiter);
         if (nextVal != null)
         {
            sb.append(nextVal.toString());
         }
      }
      return sb.toString();
   }
}
