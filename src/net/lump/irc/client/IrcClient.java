package net.lump.irc.client;

import net.lump.irc.client.commands.*;
import net.lump.irc.client.listeners.IrcEventListener;
import net.lump.irc.client.listeners.IrcMessageListener;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This keeps track of Connection State.
 *
 * @author troy
 * @version $Id: IrcClient.java,v 1.1 2010/04/30 01:48:03 troy Exp $
 */
public class IrcClient {


   private IrcSocket ircSocket;

   private InetSocketAddress server;
   private User user;
   private Pass pass;
   private Nick nick;
   private Oper oper;

   private String ircHost;
   private String ircId;

   private boolean away = false;

   private User.Mode[] validUserModes;
   private Channel.Mode[] validChannelModes;
   private ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
   private ConcurrentHashMap<IrcEventListener, Object> listeners = new ConcurrentHashMap<IrcEventListener, Object>();

   private static final ConcurrentHashMap<InetSocketAddress, IrcSocket> connections =
       new ConcurrentHashMap<InetSocketAddress, IrcSocket>();
   private static final Object placeholder = new Object();
   private static final Logger logger = Logger.getLogger(IrcClient.class);

   IrcMessageListener messageListener = new IrcMessageListener() {

      private final Pattern messagePattern = Pattern.compile("^(?::(\\S+)\\s)?(\\d{3}|[A-Za-z]+)\\s+(.*)$");
      private final Pattern numericCommandPattern = Pattern.compile("^\\d{3}$");

      public void handleMessage(String line) {
         Matcher messageMatcher = messagePattern.matcher(line);
         if (!messageMatcher.matches()) {
            logger.warn("Could not parse message: " + line);
            return;
         }

         Prefix prefix = new Prefix().parseString(messageMatcher.group(1));

         // parse arguments
         String message = "";
         ArrayList<String> arguments = new ArrayList<String>();
         boolean hitColon = false;
         if (messageMatcher.group(3) != null) {
            for (String s : messageMatcher.group(3).split("\\s")) {
               if (s.startsWith(":")) { hitColon = true; s = s.substring(1); }
               if (hitColon) message += (message.length() > 0 ? " " : "") + s;
               else arguments.add(s);
            }
         }

         if (numericCommandPattern.matcher(messageMatcher.group(2)).matches()) {
            Response response = Response.fromCode(messageMatcher.group(2));
            if (response == Response.Unknown_Response)
               logger.warn(String.format("Unknown response code: (%s) for message: %s", messageMatcher.group(2), line));
            else {
               if (!ircSocket.isRegistered()
                   && response.getType() == Response.Type.Reply
                   && arguments.size() > 0
                   && arguments.get(0).equals(IrcClient.this.getNick().name())) {
                  ircSocket.setRegistered(true);
                  synchronized(ircSocket.getCommandQueueMutex()) { ircSocket.getCommandQueueMutex().notify(); }
               }

               for (IrcEventListener l : IrcClient.this.getListeners())
                  l.handleResponse(prefix, response, arguments.toArray(new String[arguments.size()]), message);
            }
         }
         else {
            try {
               CommandName commandName = CommandName.valueOf(messageMatcher.group(2));

               switch(commandName) {
                  // handle PING here automatically
                  case PING:
                     Pong pong = new Pong(IrcClient.this.getIrcHost(), message);
                     ircSocket.queueCommand(pong);
                     logger.debug("Sent pong: " + pong);
                     break;
               }

               for (IrcEventListener l : getListeners())
                  l.handleCommand(prefix, commandName, arguments.toArray(new String[arguments.size()]), message);

            } catch (IllegalArgumentException iae) {
               logger.warn("Unknown Command: (" + messageMatcher.group(2) + ") for message: " + line);
            }
         }
      }

      public void handleDisconnected(String line) {
         for (IrcEventListener l : IrcClient.this.getListeners())
            l.handleDisconnected(new String[0], line);
      }
   };

   IrcEventListener changeListener = new IrcEventListener(){
      public void handleResponse(Prefix prefix, Response r, String[] args, String message) {

         switch(r) {
            case RPL_WELCOME:
               if (!nick.name().equals(args[0])) nick = Nick.newNickDisregardingException(args[0]);
               break;
            case RPL_YOURHOST:
               Matcher m = Pattern.compile("^Your\\s+host\\s+is\\s+(\\S+).*$").matcher(message);
               if (m.matches() && m.group(1) != null) ircHost = m.group(1);
               break;
            case RPL_MYINFO:
               ircHost = args[1];
               validUserModes = User.Mode.parseString(args[3]);
               validChannelModes = Channel.Mode.parseString(args[4]);
               break;
            case RPL_YOURID:
               ircId = args[1];
               break;
            case RPL_NOWAWAY:
               if (args[0] != null && args[0].equals(nick.name())) away = true;
               break;
            case RPL_UNAWAY:
               if (args[0] != null && args[0].equals(nick.name())) away = false;
               break;
            case ERR_NICKNAMEINUSE:
               for (IrcEventListener l : getListeners())
                  l.handleNickNameInUse(args, message);
               break;
            default:
               logger.debug(String.format(
                   "%s %s %s %s %s", server, prefix, r.name(), (Arrays.asList(args)).toString(), message));
         }
      }

      public void handleCommand(Prefix prefix, CommandName c, String[] args, String message) {
         switch(c) {
            case MODE:
               if (args[0].equals(getNick().name())) user.setModes(User.Mode.parseString(message));
               break;
            case PART:
               if (prefix.getNick().equals(nick.name()) && args[0] != null) removeChannel(args[0]);
               break;
            case ERROR:
               if (message != null && message.matches("^[Cc][Ll][Oo][Ss][Ii][Nn][Gg].*$"))
                  for (IrcEventListener l : getListeners()) l.handleDisconnected(args, message);
               break;
            default:
               logger.debug(String.format(
                   "%s %s %s %s %s", server, prefix, c.name(), (Arrays.asList(args)).toString(), message));
         }
      }

      public void handleNickNameInUse(String[] args, String message) {
         // we don't handle this here
      }

      public void handleDisconnected(String[] args, String message) {
         logger.warn(message);
         ircSocket.setRegistered(false);
      }
   };

   static final int defaultPort = 6667;

   {
      addListener(changeListener) ;
   }


   public IrcClient() { }

   public IrcClient(String hostname, User user, Nick nick) throws UnknownHostException {
      this(new InetSocketAddress(hostname, defaultPort), null, user, nick, null);
   }
   public IrcClient(String hostname, Pass pass, User user, Nick nick) throws UnknownHostException {
      this(new InetSocketAddress(hostname, defaultPort), pass, user, nick, null);
   }
   public IrcClient(String hostname, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(new InetSocketAddress(hostname, defaultPort), null, user, nick, oper);
   }
   public IrcClient(String hostname, Pass pass, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(new InetSocketAddress(hostname, defaultPort), pass, user, nick, oper);
   }
   public IrcClient(String hostname, int port, Pass pass, User user, Nick nick) throws UnknownHostException {
      this(new InetSocketAddress(hostname, port), pass, user, nick, null);
   }
   public IrcClient(String hostname, int port, Pass pass, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(new InetSocketAddress(hostname, port), pass, user, nick, oper);
   }
   public IrcClient(InetSocketAddress server, Pass pass, User user, Nick nick, Oper oper) {
      this.server = server;
      this.user = user;
      this.pass = pass;
      this.nick = nick;
      this.oper = oper;

      synchronized(connections) {

         if (connections.containsKey(server)) {
            this.ircSocket = connections.get(server);
            this.ircSocket.addIrcMessageListener(this.messageListener);
         }
         else {
            ircSocket = new IrcSocket(server, this.messageListener);
            connections.put(server, ircSocket);
         }
      }
   }


   public void send(Command command) {
      if (command instanceof Join) for (Channel c : ((Join)command).getChannels()) putChannel(c);
      ircSocket.queueCommand(command);
   }

   public void connect() {

      if (!ircSocket.isConnected()) {
         ircSocket.connect();
      }

      if (!ircSocket.isRegistered()) {
         if (getPass() != null) ircSocket.queueCommand(getPass());
         ircSocket.queueCommand(getUser());
         ircSocket.queueCommand(getNick());
         if (getOper() != null) ircSocket.queueCommand(getOper());
      }
   }

   public void addListener(IrcEventListener i) {
      listeners.put(i, placeholder);
   }

   public Set<IrcEventListener> getListeners() {
      Set<IrcEventListener> r = new HashSet<IrcEventListener>();
      r.addAll(listeners.keySet());
      for (Channel c : channels.values()) r.add(c.getIrcEventListener());
      return r;
   }

   public InetSocketAddress getServer() {
      return server;
   }

   public IrcClient setServer(InetSocketAddress server) {
      this.server = server;
      return this;
   }

   public User getUser() {
      return user;
   }

   public IrcClient setUser(User user) {
      this.user = user;
      return this;
   }

   public Pass getPass() {
      return pass;
   }

   public IrcClient setPass(Pass pass) {
      this.pass = pass;
      return this;
   }

   public Nick getNick() {
      return nick;
   }

   public IrcClient setNick(Nick nick) {
      this.nick = nick;
      return this;
   }

   public Oper getOper() {
      return oper;
   }

   public IrcClient setOper(Oper oper) {
      this.oper = oper;
      return this;
   }

   public String getIrcHost() {
      return ircHost;
   }

   public IrcClient setIrcHost(String ircHost) {
      this.ircHost = ircHost;
      return this;
   }

   public String getIrcId() {
      return ircId;
   }

   public IrcClient setIrcId(String ircId) {
      this.ircId = ircId;
      return this;
   }

   public User.Mode[] getValidUserModes() {
      return validUserModes;
   }

   public IrcClient setValidUserModes(User.Mode[] validUserModes) {
      this.validUserModes = validUserModes;
      return this;
   }

   public Channel.Mode[] getValidChannelModes() {
      return validChannelModes;
   }

   public IrcClient setValidChannelModes(Channel.Mode[] validChannelModes) {
      this.validChannelModes = validChannelModes;
      return this;
   }

   public Channel getChannel(String channel) {
      return channels.get(channel);
   }

   public boolean isAway() {
      return away;
   }

   public IrcClient removeChannel(String name) {
      channels.remove(name);
      return this;
   }

   public IrcClient putChannel(Channel channel) {
      channels.put(channel.getName(), channel);
      return this;
   }

   public boolean isRegistered() {
      return ircSocket.isRegistered();
   }

   public String toString() {
      return toString(nick, server);
   }

   public String toString(Nick nick, InetSocketAddress server) {
      return String.format("%s@%s", nick.name(), server.toString());
   }

   @Override
   public boolean equals(Object o) {
      return this == o || o instanceof IrcClient && toString().equals(o.toString());
   }

   @Override
   public int hashCode() {
      return toString().hashCode();
   }
}
