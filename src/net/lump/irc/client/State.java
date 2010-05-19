package net.lump.irc.client;

/**
 * An IRC State.
 *
 * @author M. Troy Bowman
 */
public class State extends BitwiseEnum {
   public enum States {
      NONE,
      CONNECTED,
      REGISTERED,
      IRC_OPERATOR,
      JOINED,
      AWAY,
   }

   @Override
   protected Class getEnum() {
      return States.class;
   }

   public State() {
      super();
   }

   public State(States... states) {
      super(states);
   }

   public State(int flags) {
      super(flags);
   }

   public State add(States... states) {
      return (State)super.add(states);
   }

   public State remove(States... states) {
      return (State)super.remove(states);
   }

   public State set(States flags) {
      return (State)super.set(flags);
   }

   public State set(int flags) {
      return (State)super.set(flags);
   }

   public State toggle(States... states) {
      return (State)super.toggle(states);
   }

   public State clear() {
      return (State)super.clear();
   }
}

