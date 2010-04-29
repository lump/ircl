package net.lump.irc.client.exception;

public class IllegalChannelException extends Exception {
   public IllegalChannelException() {
      super();
   }

   public IllegalChannelException(String message) {
      super(message);
   }

   public IllegalChannelException(String message, Throwable cause) {
      super(message, cause);
   }

   public IllegalChannelException(Throwable cause) {
      super(cause);
   }
}
