package net.lump.log4j;

import net.lump.irc.client.IrcClient;
import net.lump.irc.client.IrcSocket;
import net.lump.irc.client.commands.Away;
import net.lump.irc.client.commands.Nick;
import net.lump.irc.client.commands.Pass;
import net.lump.irc.client.commands.Privmsg;
import net.lump.irc.client.exception.IllegalNickException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.util.regex.Pattern;

// Copyright SOS Staffing 2010

/**
 * .
 *
 * @author troy
 * @version $Id: IrcAppender.java,v 1.1 2010/04/30 01:48:03 troy Exp $
 */
public class IrcAppender extends AppenderSkeleton {

   private final IrcClient state = new IrcClient();
   private IrcSocket ircSocket;

   private int port;
   private String host;
   private Pass pass;
   private Nick nick;

   private String userName;
   private String realName;
   private String operUser;
   private String operPass;
   private String channel;

   public void setHost(String host) {
      this.host = host;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public void setPass(String pass) {
      this.pass = new Pass(pass);
   }

   public void setNick(String nick) {
      try {
         this.nick = new Nick(nick);
      } catch (IllegalNickException e) {
         LogLog.error("Nick \"" + nick + "\" does not follow IRC nick rules");
      }
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public void setRealName(String realName) {
      this.realName = realName;
   }

   public void setOperUser(String operUser) {
      this.operUser = operUser;
   }

   public void setOperPassword(String operPassword) {
      this.operPass = operPassword;
   }

   public void setChannel(String channel) {
      this.channel = channel;
   }

   /** Derived appenders should override this method if option structure requires it.  */
   @Override
   public void activateOptions() {
/*
      if (state.isAway())

      boolean connectionChanged = false;


      InetSocketAddress newServer = new InetSocketAddress(host, port);

      Oper newOper = null;
      if (operUser != null && operPass != null) newOper = new Oper(operUser, operPass);

      if (!state.toString().equals(state.toString(nick, newServer))) connectionChanged = true;



      if (!connectionChanged && connection.isConnected() && state.isRegistered()) {
         if (state.getOper())
      }


      if (host.equals(state.getServer().getHostName()))

         state.setServer(new InetSocketAddress(host, port));

      try {
         state.setNick(new Nick(nick));
      } catch (IllegalNickException e) {
         return;
         LogLog.error("Nick \"" + nick + "\" does not follow IRC nick rules");
      }

      if (connection == null || !connection.isConnected()) {
         if (connection == null) try {
            connection = Connection.getConnection(state);
         } catch (IOException e) {
            LogLog.error("Couldn't connect to " + state.getServer() + ":" + state.getPort());
         }
      }

      if (state.isRegistered()) {
      }


      state.setUser(new User(userName, realName));
      if (operUser != null && operPass != null) state.setOper(new Oper(operUser, operPass));
*/

   }

   Pattern emptyWhiteSpace = Pattern.compile("^\\s*$");

   @Override
   public void append(LoggingEvent event) {
      if (state.isRegistered()
          && state.getChannel(channel) != null
          && state.getChannel(channel).getNicks().size() > 1
          && ircSocket != null
          && ircSocket.isConnected()) {

         if (state.isAway()) ircSocket.queueCommand(new Away(null));

         //each line in an event needs to be sent as a separate msg
         //delimit on newlines, carriage returns and line feeds
         for (String line : layout.format(event).split("\\r\\n|\\r|\\n|\\f"))
            if (!emptyWhiteSpace.matcher(line).matches())
               ircSocket.queueCommand(new Privmsg(channel, line.replaceAll("\\s*$", "")));

         if (event.getThrowableStrRep() != null && layout.ignoresThrowable())
            for (String t : event.getThrowableStrRep())
               ircSocket.queueCommand(new Privmsg(channel, t.replaceAll("\\s*$", "")));
      }
   }

   @Override
   public boolean requiresLayout() {
      return true;
   }

   @Override
   public void close() {
      ircSocket.queueCommand(new Privmsg(channel, "I'm going away for a bit."));
      ircSocket.queueCommand(new Away("I'm not listening to you right now."));
   }
}
