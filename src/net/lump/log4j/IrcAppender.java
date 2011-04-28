package net.lump.log4j;

import net.lump.irc.client.Channel;
import net.lump.irc.client.IrcClient;
import net.lump.irc.client.Prefix;
import net.lump.irc.client.Response;
import net.lump.irc.client.commands.*;
import net.lump.irc.client.exception.IllegalChannelException;
import net.lump.irc.client.exception.IllegalNickException;
import net.lump.irc.client.listeners.AbstractIrcEventListener;
import net.lump.irc.client.listeners.ChannelListener;
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
 * <p>This provides a log4j IRC appender.</p>
 * <p><b>Please realize that IRC usually has mechanisms that prevent flooding.  If you are logging to an IRC server on
 * your LAN, and its clients are also on your LAN, then flooding is not an issue and can be safely turned off.  Some
 * IRC servers have oper flags that allow an IRC OPER to flood without penalty.  If you have a high-volume logging,
 * you will need to coordinate with your IRC server's administrator to somehow allow flooding without penalty.</b></p>
 *
 * <p>Configure log4j an appender like this (host, port, and nick are required):
 * <pre>

   &lt;appender name="IRC_LOG" class="net.lump.log4j.IrcAppender"&gt;
      &lt;errorHandler class="org.jboss.logging.util.OnlyOnceErrorHandler"/&gt;
      &lt;param name="port" value="6667"/&gt;
      &lt;param name="host" value="irc.domain.com"/&gt;
      &lt;param name="nick" value="log"/&gt;
      &lt;param name="operUser" value="oper"/&gt;
      &lt;param name="operPass" value="secret"/&gt;
      &lt;param name="channel" value="#applog"/&gt;
      &lt;param name="Threshold" value="INFO"/&gt;

      &lt;layout class="org.apache.log4j.PatternLayout"&gt;
         &lt;param name="ConversionPattern" value="%m%n"/&gt;
      &lt;/layout&gt;
   &lt;/appender&gt;

   </pre>
 * You can provide multiple appender configurations that have identical host, port, and nick but have a different
 * channel, and the appender will share the same connection for those log messages.</p>
 *
 * @author M. Troy Bowman
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
         this.channel = new Channel(channel).addListener(channelListener);
      } catch (IllegalChannelException e) {
         LogLog.error("Nick \"" + nick + "\" does not follow IRC channel format");
      }
   }

   ChannelListener channelListener = new ChannelListener() {

      public void onNames(String[] names) {
      }

      public void onJoin(Prefix prefix, String message) {
         if (prefix.getNick().equals(client.getNick().name()) && message.equals(channel.getName()))
            client.send(new Privmsg(channel.getName(), String.format(
                "Hello, I'm Log4j appender \"%s\" and my threshold is %s", name, getThreshold())));
         else if (message.equals(channel.getName())) {
            client.send(new Privmsg(channel.getName(), String.format(
                "Hello, " + prefix.getNick() + ".  Welcome to " + channel.getName())));
         }
      }

      public void onTopic(String topic) {
      }

      public void onMode(Channel.Mode[] modes, String[] args) {
      }

      public void onPart(Prefix prefix, String message) {
      }

      public void onQuit(Prefix prefix, String message) {
      }

      public void onPrivmsg(Prefix prefix, String message) {
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

      public void onNotice(Prefix prefix, String message) {
      }
   };

   IrcEventListener listener = new AbstractIrcEventListener() {
      public void handleNickProblem(Prefix prefix, Response r, String[] args, String message) {
         if (args[1].equals(client.getNick().name())) {
            client.setNick(client.getNick().increment());
         }
         client.send(client.getNick());
      }

      public void onDisconnect(String[] args, String message) {
         LogLog.error("Disconnected: " + message);
         IrcAppender.this.close();
//         if (message.contains("Connection refused")) {
//            IrcAppender.this.close();
//         }
//         else {
//            try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
//            activateOptions();
//         }
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

      if (client == null || !client.isConnected() || !client.isRegistered()) activateOptions();
      if (client == null || !client.isConnected()) return;
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
      if (client.isConnected() && !client.isAway()) {
         client.send(new Away("I'm not listening to you right now."));
      }
   }
}
