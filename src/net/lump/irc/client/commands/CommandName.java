package net.lump.irc.client.commands;

/**
 * Valid IRC Commands.
 *
 * @author troy
 * @version $Id: CommandName.java,v 1.2 2010/04/29 03:06:09 troy Exp $
 */
@SuppressWarnings({"UnusedDeclaration"})
public enum CommandName {
   UNKNOWN,
   PASS(false),
   NICK(false),
   USER(false),
   OPER,
   SERVICE,
   QUIT(false),
   SQUIT,
   JOIN,
   PART,
   MODE,
   TOPIC,
   NAMES,
   LIST,
   INVITE,
   KICK,
   PRIVMSG,
   NOTICE,
   MOTD,
   LUSERS,
   VERSION,
   STATS,
   LINKS,
   TIME,
   CONNECT,
   TRACE,
   ADMIN,
   INFO,
   SERVLIST,
   SQUERY,
   WHO,
   WHOIS,
   WHOWAS,
   KILL,
   PING,
   PONG,
   ERROR,
   AWAY,
   REHASH,
   DIE,
   RESTART,
   SUMMON,
   USERS,
   WALLOPS,
   USERHOST,
   ISON;

   private boolean requiresRegistration = true;

   private CommandName(){}
   private CommandName(boolean registrationRequired){
     requiresRegistration = registrationRequired;
   }

   public boolean requiresRegistration() {
      return requiresRegistration;
   }
}
