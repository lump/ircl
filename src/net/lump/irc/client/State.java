package net.lump.irc.client;

import net.lump.irc.client.commands.*;
import net.lump.irc.client.listeners.IrcEventListener;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
 * @version $Id: State.java,v 1.4 2010/04/29 03:47:26 troy Exp $
 */
public class State {
   private InetAddress server;
   private int port;
   private User user;
   private Pass pass;
   private Nick nick;
   private Oper oper;

   private String ircHost;
   private String ircId;

   private volatile boolean registered = false;

   private User.Mode[] validUserModes;
   private Channel.Mode[] validChannelModes;

   private ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
   private ConcurrentHashMap<IrcEventListener, Object> listeners =  new ConcurrentHashMap<IrcEventListener, Object>();
   private static final Object placeholder = new Object();
   private static final Logger logger = Logger.getLogger(State.class);

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
               if (prefix.getNick().equals(nick) && args[0] != null) removeChannel(args[0]);
               break;
            case ERROR:
               if (message != null && message.matches("^[Cc]losing [Ll]ink.*$")) {
                  logger.warn(message);
                  for (IrcEventListener l : getListeners()) l.handleDisconnected(args, message);
               }
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
         // we don't handle this here
      }
   };

   static final int defaultPort = 6667;

   {
      addListener(changeListener) ;
   }

   public State(String hostname, User user, Nick nick) throws UnknownHostException {
      this(InetAddress.getByName(hostname), defaultPort, null, user, nick, null);
   }
   public State(String hostname, Pass pass, User user, Nick nick) throws UnknownHostException {
      this(InetAddress.getByName(hostname), defaultPort, pass, user, nick, null);
   }
   public State(String hostname, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(InetAddress.getByName(hostname), defaultPort, null, user, nick, oper);
   }
   public State(String hostname, Pass pass, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(InetAddress.getByName(hostname), defaultPort, pass, user, nick, oper);
   }
   public State(String hostname, int port, Pass pass, User user, Nick nick) throws UnknownHostException {
      this(InetAddress.getByName(hostname), port, pass, user, nick, null);
   }
   public State(String hostname, int port, Pass pass, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(InetAddress.getByName(hostname), port, pass, user, nick, oper);
   }
   public State(InetAddress server, int port, Pass pass, User user, Nick nick, Oper oper) {
      this.server = server;
      this.port = port;
      this.user = user;
      this.pass = pass;
      this.nick = nick;
      this.oper = oper;
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

   public InetAddress getServer() {
      return server;
   }

   public State setServer(InetAddress server) {
      this.server = server;
      return this;
   }

   public int getPort() {
      return port;
   }

   public State setPort(int port) {
      this.port =port;
      return this;
   }

   public User getUser() {
      return user;
   }

   public State setUser(User user) {
      this.user = user;
      return this;
   }

   public Pass getPass() {
      return pass;
   }

   public State setPass(Pass pass) {
      this.pass = pass;
      return this;
   }

   public Nick getNick() {
      return nick;
   }

   public State setNick(Nick nick) {
      this.nick = nick;
      return this;
   }

   public Oper getOper() {
      return oper;
   }

   public State setOper(Oper oper) {
      this.oper = oper;
      return this;
   }

   public String getIrcHost() {
      return ircHost;
   }

   public State setIrcHost(String ircHost) {
      this.ircHost = ircHost;
      return this;
   }

   public String getIrcId() {
      return ircId;
   }

   public State setIrcId(String ircId) {
      this.ircId = ircId;
      return this;
   }

   public User.Mode[] getValidUserModes() {
      return validUserModes;
   }

   public State setValidUserModes(User.Mode[] validUserModes) {
      this.validUserModes = validUserModes;
      return this;
   }

   public Channel.Mode[] getValidChannelModes() {
      return validChannelModes;
   }

   public State setValidChannelModes(Channel.Mode[] validChannelModes) {
      this.validChannelModes = validChannelModes;
      return this;
   }

   public Channel getChannel(String channel) {
      return channels.get(channel);
   }

   public boolean isRegistered() {
      return registered;
   }

   public State removeChannel(String name) {
      channels.remove(name);
      return this;
   }

   public State putChannel(Channel channel) {
      channels.put(channel.getName(), channel);
      return this;
   }

   public State setRegistered(boolean b) {
      registered = b;
      return this;
   }

}
