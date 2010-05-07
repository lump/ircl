package net.lump.irc.client;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * An abstract class that provides bitwise operations for an enum.  The class which extends this one should define
 * its own enum inner class for use with the superclass methods.  The subclass may also create aliases of the methods
 * which return {@link BitwiseEnum} for ease of casting.
 *
 * @author troy
 * @version $Id: BitwiseEnum.java,v 1.1 2010/05/07 18:42:22 troy Exp $
 */
public abstract class BitwiseEnum implements Serializable {

   protected volatile int flags = 0;

   /**
    * Provides the enum class to the superclass.
    * The implementing class must provide an enum class definition for this method.
    *
    * @return the class that contains the enum elements
    */
   protected abstract Class getEnum();

   /**
    * Constrcu a new object with no flags turned on.
    */
   public BitwiseEnum()
   {
   }

   /**
    * Construct a new object with the provided flags.  If no flags are provided, then no bits will be turned on.
    *
    * @param flags the flags to turn on
    */
   public BitwiseEnum(Enum... flags) {
      if (flags.length > 0)
         add(flags);
   }

   /**
    * This utility derives the bit of the enum provided.
    *
    * @param flag the flag to derive from
    * @return the corresponding bit
    */
   public static int bitOfFlag(Enum flag) {
      int bit = 0;
      if (flag.ordinal() > 0)
         bit = (1 << (flag.ordinal() - 1));
      return bit;
   }

   /**
    * Sets the internal integer which represents the flags of this object.
    *
    * @param flags the flags
    */
   public BitwiseEnum(int flags) {
      this.flags = flags;
   }

   /**
    * Adds the provided flags to this object.  This means, the bits representing the flags provided will be turned on.
    *
    * @param flags the flags to add
    * @return this object
    */
   public BitwiseEnum add(Enum... flags) {
      if (flags.length == 0) return this;
      this.flags |= addFlagsTogether(flags);
      return this;
   }

   /**
    * Adds all the provided enums' bits together, returns an integer that represents all of the flags.
    *
    * @param flags the flags to merge together.
    * @return the integer representing all of the flags.
    */
   protected static int addFlagsTogether(Enum... flags) {
      int ored = 0;
      for (Enum flag : flags)
         ored |= bitOfFlag(flag);
      return ored;
   }

   /**
    * The integer representation of this object.
    *
    * @return the integer
    */
   public int getInt() {
      return flags;
   }

   /**
    * Whether this object has any of the flags provided.
    *
    * @param flags the flags to test
    * @return whether any of the flags were found.
    */
   public boolean hasAny(Enum... flags)
   {
      if (flags.length == 0)
         return this.flags == 0;
      else if ((this.flags & addFlagsTogether(flags)) > 0)
         return true;
      return false;
   }

   /**
    * Whether this object has all of the flags provided.
    *
    * @param flags the flags to test
    * @return boolean, whether they all exist.
    */
   public boolean has(Enum... flags) {
      if (flags.length == 0)
         return this.flags == 0;
      else {
         int that = addFlagsTogether(flags);
         return ((this.flags & that) == that);
      }
   }

   /**
    * Remove the provided flags from this object.
    *
    * @param flags the flags to remove
    * @return the current instance
    */
   public BitwiseEnum remove(Enum... flags) {
      this.flags &= this.flags ^ addFlagsTogether(flags);
      return this;
   }

   /**
    * Sets all of the flags in this object to exactly what is contained in the
    * int that is provided.
    *
    * @param flags to set
    * @return BitwiseEnum
    */
   public BitwiseEnum set(Enum flags) {
      this.flags = bitOfFlag(flags);
      return this;
   }

   /**
    * Sets internal integer representing the flags in this object.
    *
    * @param flags the integer
    * @return this object
    */
   public BitwiseEnum set(int flags) {
      this.flags = flags;
      return this;
   }

   /**
    * Toggles the provided flags.  If they are off, they'll be turned on, if they're on, they'll be turned off.
    *
    * @param flags the flags to toggle
    * @return this object
    */
   public BitwiseEnum toggle(Enum... flags) {
      this.flags ^= addFlagsTogether(flags);
      return this;
   }

   /**
    * Clears all of the flags.
    * @return this object
    */
   public BitwiseEnum clear() {
      this.flags = 0;
      return this;
   }

   /**
    * A list of Enums that are set in this object.
    *
    * @return String
    */
   public Enum[] setValues() {
      Enum[] enums = (Enum[])getEnum().getEnumConstants();

      if (enums == null || enums.length == 0) return new Enum[0];

      ArrayList<Enum> out = new ArrayList<Enum>();
      for (Enum f : enums)
         if (this.has(f))
            out.add(f);

      return out.toArray(new Enum[out.size()]);
   }

   /**
    * A nice human-readable representation of this flag object.
    *
    * @return String
    */
   public String toString() {
      Enum[] enums = setValues();
      String out = "";
      for (Enum f : enums) {
         if (out.length() > 0) out += ",";
         out += f.name();
      }
      return out;
   }
}
