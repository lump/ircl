package net.lump.irc.client.commands;

import static net.lump.irc.client.State.States;
import static net.lump.irc.client.State.States.*;

/**
 * Valid IRC Command Names.  These are used in both {@link Command} construction as well as server command parsing.
 *
 * @author M. Troy Bowman
 */
@SuppressWarnings({"UnusedDeclaration"})
public enum CommandName {
   UNKNOWN(NONE),
   PASS(CONNECTED),
   NICK(CONNECTED),
   USER(CONNECTED),
   OPER(REGISTERED),
   SERVICE(REGISTERED),
   QUIT(CONNECTED),
   SQUIT(REGISTERED),
   JOIN(REGISTERED),
   PART(JOINED),
   MODE(REGISTERED),
   TOPIC(REGISTERED),
   NAMES(REGISTERED),
   LIST(REGISTERED),
   INVITE(JOINED),
   KICK(JOINED),
   PRIVMSG(JOINED),
   NOTICE(REGISTERED),
   MOTD(REGISTERED),
   LUSERS(REGISTERED),
   VERSION(REGISTERED),
   STATS(REGISTERED),
   LINKS(REGISTERED),
   TIME(REGISTERED),
   CONNECT(IRC_OPERATOR),
   TRACE(REGISTERED),
   ADMIN(CONNECTED),
   INFO(REGISTERED),
   SERVLIST(REGISTERED),
   SQUERY(REGISTERED),
   WHO(REGISTERED),
   WHOIS(REGISTERED),
   WHOWAS(REGISTERED),
   KILL(IRC_OPERATOR),
   PING(REGISTERED),
   PONG(REGISTERED),
   ERROR(REGISTERED),
   AWAY(REGISTERED),
   REHASH(IRC_OPERATOR),
   DIE(IRC_OPERATOR),
   RESTART(IRC_OPERATOR),
   SUMMON(REGISTERED),
   USERS(REGISTERED),
   WALLOPS(REGISTERED),
   USERHOST(REGISTERED),
   ISON(REGISTERED);

   private States requiredState;

   private CommandName(States state) {
      this.requiredState = state;
   }

   public States getRequiredState() {
      return this.requiredState;
   }
}
