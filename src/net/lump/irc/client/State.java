package net.lump.irc.client;

import com.sun.xml.internal.bind.v2.util.QNameMap;
import net.lump.irc.client.commands.Nick;
import net.lump.irc.client.commands.Pass;
import net.lump.irc.client.commands.User;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This keeps track of Connection State.
 *
 * @author troy
 * @version $Id: State.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public class State {
   InetAddress server;
   int port;
   User user;
   Pass pass;
   Nick nick;

   String ircHost;
   String ircId;

   private ConcurrentHashMap<Channel,Object> channels = new ConcurrentHashMap<Channel,Object>();
   private ConcurrentHashMap<IrcEventListener, Object> listeners =  new ConcurrentHashMap<IrcEventListener, Object>();
   private static final Object placeholder = new Object();

   AbstractIrcEventListener changeListener = new AbstractIrcEventListener(){
      public void handleResponse(String server, Response r, String[] args, String message) {
         switch(r) {
            case RPL_WELCOME:
               if (!nick.name().equals(args[0])) nick = new Nick(args[0]);
               break;
            case RPL_YOURHOST:
               Matcher m = Pattern.compile("^Your\\s+host\\s+is\\s+(\\S+).*$").matcher(message);
               if (m.matches() && m.group(1) != null) ircHost = m.group(1);
               break;
            case RPL_MYINFO:
               ircHost = args[1];
               break;
            case RPL_YOURID:
               ircId = args[1];
         }
         System.err.printf("State got event: %s %s %s %s%n", server, r.name(), (Arrays.asList(args)).toString(), message);
      }
   };

   static final int defaultPort = 6667;

   {
      addListener(changeListener) ;
   }

   public State(String hostname, User user, Nick nick) throws UnknownHostException {
      this(InetAddress.getByName(hostname), defaultPort, user, null, nick);
   }

   public State(String hostname, User user, Pass pass, Nick nick) throws UnknownHostException {
      this(InetAddress.getByName(hostname), defaultPort, user, pass, nick);
   }

   public State(String hostname, int port, User user, Pass pass, Nick nick) throws UnknownHostException {
      this(InetAddress.getByName(hostname), port, user, pass, nick);
   }

   public State(InetAddress server, int port, User user, Pass pass, Nick nick) {
      this.server = server;
      this.port = port;
      this.user = user;
      this.pass = pass;
      this.nick = nick;
   }


   public InetAddress getServer() {
      return server;
   }

   public int getPort() {
      return port;
   }

   public User getUser() {
      return user;
   }

   public Pass getPass() {
      return pass;
   }

   public Nick getNick() {
      return nick;
   }

   public String getIrcHost() {
      return ircHost;
   }

   public void addListener(IrcEventListener i) {
      listeners.put(i, placeholder);
   }

   public Set<IrcEventListener> getListeners() {
      return listeners.keySet();
   }
}
