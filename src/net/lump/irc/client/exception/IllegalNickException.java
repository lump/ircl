package net.lump.irc.client.exception;

public class IllegalNickException extends Exception {
   public IllegalNickException() {
      super();
   }

   public IllegalNickException(String message) {
      super(message);
   }

   public IllegalNickException(String message, Throwable cause) {
      super(message, cause);
   }

   public IllegalNickException(Throwable cause) {
      super(cause);
   }
}
