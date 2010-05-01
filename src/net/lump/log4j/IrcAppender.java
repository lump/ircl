package net.lump.log4j;

import net.lump.irc.client.Channel;
import net.lump.irc.client.IrcClient;
import net.lump.irc.client.Prefix;
import net.lump.irc.client.Response;
import net.lump.irc.client.commands.*;
import net.lump.irc.client.exception.IllegalChannelException;
import net.lump.irc.client.exception.IllegalNickException;
import net.lump.irc.client.listeners.AbstractIrcEventListener;
import net.lump.irc.client.listeners.IrcEventListener;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This provides a log4j IRC appender.
 *
 * @author troy
 * @version $Id: IrcAppender.java,v 1.4 2010/05/01 01:08:55 troy Exp $
 */
public class IrcAppender extends AppenderSkeleton {

   private static final ConcurrentHashMap<String, IrcClient> clients = new ConcurrentHashMap<String, IrcClient>();

   private IrcClient client;

   private Integer port;
   private String host;
   private Pass pass;
   private Nick nick;

   private String userName;
   private String realName;
   private String operUser;
   private String operPass;
   private Channel channel;

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
         LogLog.error("Nick \"" + nick + "\" does not follow IRC nick format");
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

   public void setOperPass(String operPass) {
      this.operPass = operPass;
   }

   public void setChannel(String channel) {
      try {
         this.channel = new Channel(channel);
      } catch (IllegalChannelException e) {
         LogLog.error("Nick \"" + nick + "\" does not follow IRC channel format");
      }
   }

   IrcEventListener listener = new AbstractIrcEventListener() {
         @Override
         public void handleResponse(Prefix prefix, Response r, String[] args, String message) {
         }

         @Override
         public void handleCommand(Prefix prefix, CommandName c, String[] args, String message) {
            switch (c) {
               case PRIVMSG:
                  if (args[0] != null && args[0].equals(channel.getName())) {
                     Matcher m = Pattern.compile("^\\s*"+ client.getNick().name()+",\\s*(.+)$",Pattern.CASE_INSENSITIVE).matcher(message);
                     if (m.matches() && m.group(1) != null) {
                        Matcher ct = Pattern.compile("^(greetings|hi|hello|welcome).*$",Pattern.CASE_INSENSITIVE).matcher(m.group(1));
                        if (ct.matches() && ct.group(1) != null) {
                           client.send(new Privmsg(channel.getName(),
                               String.format("And %s to you, too, %s.", ct.group(1).toLowerCase(), prefix.getNick())));
                        }
                        Matcher mt = Pattern.compile("^threshold\\s+(.+?)$",Pattern.CASE_INSENSITIVE).matcher(m.group(1));
                        if (mt.matches() && mt.group(1) != null) {
                           if (mt.group(1).equals("?")) {
                              client.send(new Privmsg(channel.getName(), String.format(
                                  "My log4j appender threshold is %s, %s.", getThreshold().toString(), prefix.getNick())));
                           }
                           else {
                              Level level = Level.toLevel(mt.group(1));
                              setThreshold(level);
                              client.send(new Privmsg(channel.getName(), String.format(
                                  "Ok, I set my log4j appender threshold to %s, %s.", level.toString(), prefix.getNick())));
                           }
                        }
                     }
                  }

                  break;
               case JOIN:
                  if (prefix.getNick().equals(client.getNick().name()) && message.equals(channel.getName()))
                     client.send(new Privmsg(channel.getName(), String.format(
                         "Hello, I'm Log4j appender \"%s\" and my threshold is %s", name, getThreshold())));
                  else if (prefix.getNick().matches("^[Tt]roy|[Ff]roy|[Ll]ump|[Bb]owmant") && message.equals(channel.getName())) {
                     client.send(new Privmsg(channel.getName(), String.format(
                         "Hello, Troy.  Welcome to " + channel.getName())));
                  }
                  break;
            }
         }

         public void handleNickNameInUse(String[] args, String message) {
            if (args[1].equals(client.getNick().name())) {
               client.setNick(client.getNick().increment());
            }
            client.send(client.getNick());
         }

         public void handleDisconnected(String[] args, String message) {
            LogLog.error("Disconnected");
            try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
            activateOptions();
         }
      };

   @Override
   public void activateOptions() {

      if (nick == null) {
         LogLog.error(IrcAppender.class.getSimpleName() + " requires a nick parameter.");
         return;
      }

      if (host == null) {
         LogLog.error(IrcAppender.class.getSimpleName() + " requires a host parameter.");
         return;
      }

      User user = new User(userName, realName);
      Oper oper = (operUser != null && operPass != null) ? new Oper(operUser, operPass) : null;

      if (client == null) {
         InetSocketAddress address = new InetSocketAddress(host, port == null ? IrcClient.DEFAULT_PORT : port);

         String key = IrcClient.toString(nick, address);
         if (clients.containsKey(key))
            client = clients.get(key);
         else {
            client = new IrcClient(address, pass, user, nick, oper);
            clients.put(client.toString(), client);
         }
         client.addListener(listener);
      }
      else {
         client.setNick(nick);
         client.setUser(user);
         client.setOper((operUser != null && operPass != null) ? new Oper(operUser, operPass) : null);
      }

      if (!client.isRegistered()) client.connect();

   }

   Pattern emptyWhiteSpace = Pattern.compile("^\\s*$");

   @Override
   public void append(LoggingEvent event) {

      if (client == null || !client.isRegistered()) activateOptions();
      if (client == null) return;
      if (client.getChannel(channel.getName()) == null) client.send(new Join(channel));
      if (client.isAway()) client.send(new Away(null));

      // only log if there are others in there listening
      // if the channel hasn't been joined, we don't know how many are in there yet, so queue it anyway.
      if (channel.getNicks().size() > 1 || !channel.isJoined()) {

         //each line in an event needs to be sent as a separate msg
         //delimit on newlines, carriage returns and line feeds
         for (String line : layout.format(event).split("\\r\\n|\\r|\\n|\\f"))
            if (!emptyWhiteSpace.matcher(line).matches())
               client.send(new Privmsg(channel.getName(), line.replaceAll("\\s*$", "")));

         if (event.getThrowableStrRep() != null && layout.ignoresThrowable())
            for (String t : event.getThrowableStrRep())
               client.send(new Privmsg(channel.getName(), t.replaceAll("\\s*$", "")));
      }
   }

   @Override
   public boolean requiresLayout() {
      return true;
   }

   @Override
   public void close() {
      client.send(new Privmsg(channel.getName(), "I'm going away for a bit."));
      client.send(new Away("I'm not listening to you right now."));
   }
}
