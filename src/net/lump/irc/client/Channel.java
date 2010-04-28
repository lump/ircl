package net.lump.irc.client;

/**
 * .
 *
 * @author troy
 * @version $Id: Channel.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public class Channel {
   public enum Mode {
      creator('O'),
      operator('o'),
      voice('v'),
      anonymous('a'),
      inviteOnly('i'),
      moderated('m'),
      noOutsidePrivmsg('n'),
      quiet('q'),
      secret('s'),
      reOp('r'),
      topicByOperOnly('t'),
      password('k'),
      userLimit('l'),
      banMask('b'),
      exceptionMask('e'),
      invitationMask('i');

      char modeChar;
      private Mode(char m) {
         modeChar = m;
      }
   }

   Mode mode;
   String[] nicks = new String[0];
}
