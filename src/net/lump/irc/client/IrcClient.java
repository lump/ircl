package net.lump.irc.client;

import net.lump.irc.client.commands.*;
import net.lump.irc.client.listeners.IrcEventListener;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This keeps track of Connection State.
 *
 * @author troy
 * @version $Id: IrcClient.java,v 1.3 2010/05/01 01:08:55 troy Exp $
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

   private volatile boolean registered = false;
   private boolean operator = false;
   private boolean away = false;

   private User.Mode[] validUserModes;
   private Channel.Mode[] validChannelModes;
   private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
   private final ConcurrentHashMap<IrcEventListener, Object> listeners = new ConcurrentHashMap<IrcEventListener, Object>();

   private static final Object commandQueueMutex = new Object();
   private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();

   private static final Object placeholder = new Object();
   private static final Logger logger = Logger.getLogger(IrcClient.class);

   private final Pattern messagePattern = Pattern.compile("^(?::(\\S+)\\s)?(\\d{3}|[A-Za-z]+)\\s+(.*)$");
   private final Pattern numericCommandPattern = Pattern.compile("^\\d{3}$");


   public static final int MAX_COMMAND_QUEUE_SIZE = 50;
   public static final int DEFAULT_PORT = 6667;

   public IrcClient() {
      addListener(changeListener) ;
   }

   public IrcClient(String hostname, User user, Nick nick) throws UnknownHostException {
      this(new InetSocketAddress(hostname, DEFAULT_PORT), null, user, nick, null);
   }
   public IrcClient(String hostname, Pass pass, User user, Nick nick) throws UnknownHostException {
      this(new InetSocketAddress(hostname, DEFAULT_PORT), pass, user, nick, null);
   }
   public IrcClient(String hostname, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(new InetSocketAddress(hostname, DEFAULT_PORT), null, user, nick, oper);
   }
   public IrcClient(String hostname, Pass pass, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(new InetSocketAddress(hostname, DEFAULT_PORT), pass, user, nick, oper);
   }
   public IrcClient(String hostname, int port, Pass pass, User user, Nick nick) throws UnknownHostException {
      this(new InetSocketAddress(hostname, port), pass, user, nick, null);
   }
   public IrcClient(String hostname, int port, Pass pass, User user, Nick nick, Oper oper) throws UnknownHostException {
      this(new InetSocketAddress(hostname, port), pass, user, nick, oper);
   }
   public IrcClient(InetSocketAddress server, Pass pass, User user, Nick nick, Oper oper) {
      this();
      this.server = server;
      this.user = user;
      this.pass = pass;
      this.nick = nick;
      this.oper = oper;
   }


   public void send(Command command) {
      if (command instanceof Join) for (Channel c : ((Join)command).getChannels()) putChannel(c);

      // only add privmsgs if our queue isn't big
      if (commandQueue.size() > MAX_COMMAND_QUEUE_SIZE && command instanceof Privmsg) {
         logger.warn(String.format("Skipping message because commandQueue size is > %d: %s",
             MAX_COMMAND_QUEUE_SIZE, ((Privmsg)command).getMessage()));
         return;
      }

      commandQueue.add(command);

      // notify anything waiting that we got a new command
      synchronized(commandQueueMutex) { commandQueueMutex.notify(); }
   }

   public void connect() {

      if (ircSocket == null) ircSocket = new IrcSocket();

      if (!ircSocket.isConnected()) ircSocket.connect();

      if (!registered) {
         if (getPass() != null) send(getPass());
         send(getUser());
         send(getNick());

         // this isn't a registration command, but it will be run after registration
         if (getOper() != null) send(getOper());
      }
   }

   public void disconnect(String reason) {
      if (registered) { send(new Quit(reason)); }
      ircSocket.abortConnection();
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
      if (registered && nick != null && !nick.equals(this.nick)) send(nick);
      else this.nick = nick;
      return this;
   }

   public Oper getOper() {
      return oper;
   }

   public IrcClient setOper(Oper oper) {
      if (registered && oper != null && !oper.equals(this.oper)) send(oper);
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
      return registered;
   }

   public boolean isOperator() {
      return operator;
   }

   public String toString() {
      return toString(nick, server);
   }

   public static String toString(Nick nick, InetSocketAddress server) {
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

   private void handleMessage(String line) {
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
            if (!registered
                && response.getType() == Response.Type.Reply
                && arguments.size() > 0
                && arguments.get(0).equals(IrcClient.this.getNick().name())) {
               registered = true;
               // notify anything waiting that we are registered now
               synchronized(commandQueueMutex) { commandQueueMutex.notify(); }
            }

            for (IrcEventListener l : IrcClient.this.getListeners())
               l.handleResponse(prefix, response, arguments.toArray(new String[arguments.size()]), message);
         }
      }
      else {
         try {
            CommandName commandName = CommandName.valueOf(messageMatcher.group(2));

            for (IrcEventListener l : getListeners())
               l.handleCommand(prefix, commandName, arguments.toArray(new String[arguments.size()]), message);

         } catch (IllegalArgumentException iae) {
            logger.warn("Unknown Command: (" + messageMatcher.group(2) + ") for message: " + line);
         }
      }
   }

   private void handleDisconnected(String line) {
      for (IrcEventListener l : IrcClient.this.getListeners())
         l.handleDisconnected(new String[0], line);
   }


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
            case RPL_YOUREOPER:
               operator = true;
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
            case JOIN:
               if (channels.containsKey(message) && prefix.getNick().equals(nick.name())) {
                  channels.get(message).setJoined(true);
                  // notify anything waiting that we got a join
                  synchronized(commandQueueMutex) { commandQueueMutex.notify(); }
               }
               break;
            case PART:
               if (prefix.getNick().equals(nick.name()) && args[0] != null && channels.containsKey(args[0])) {
                  channels.get(args[0]).setJoined(false);
                  removeChannel(args[0]);
               }
               break;
            case NICK:
               if (prefix.getNick().equals(nick)) nick = Nick.newNickDisregardingException(message);
               else for (Channel channel : channels.values()) {
                  for (String nick : channel.getNicks())
                     if (nick.replaceAll("^[^A-Za-z]","").equals(prefix.getNick())) {
                        send(new Names(channel.getName()));
                        break;
                     }
               }
               break;
            case ERROR:
               if (message != null && message.matches("^[Cc][Ll][Oo][Ss][Ii][Nn][Gg].*$"))
                  for (IrcEventListener l : getListeners()) l.handleDisconnected(args, message);
               break;
            case PING:
               Pong pong = new Pong(IrcClient.this.getIrcHost(), message);
               send(pong);
               logger.debug("Sent pong: " + pong);
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
         channels.clear();
         registered = false;
         operator = false;
      }
   };


   private class IrcSocket {

      private Socket socket;
      private Thread readerThread;
      private Thread writerThread;

      private static final int MINUTE = 60000;

      protected IrcSocket() {
         socket = null;
         readerThread = null;
         writerThread = null;
      }

      public void abortConnection() {
         readerThread.interrupt();
      }

      public boolean isConnected() {
         return socket != null
             && socket.isConnected()
             && !socket.isClosed()
             && !socket.isOutputShutdown()
             && !socket.isInputShutdown()
             && readerThread != null && readerThread.isAlive()
             && writerThread != null && writerThread.isAlive();
      }

      protected void connect()
      {
         synchronized(commandQueueMutex) {

            if (isConnected()) return;

            try {
               socket = new Socket();
               socket.setKeepAlive(true);
               socket.setSoLinger(false, 0);
               socket.setSoTimeout(15 * MINUTE);
               socket.connect(server, 15000);

               if (readerThread != null && readerThread.isAlive()) { readerThread.interrupt(); readerThread = null; }
               if (writerThread != null && writerThread.isAlive()) { writerThread.interrupt(); writerThread = null; }

               final PrintWriter writer = new PrintWriter(new PrintWriter(socket.getOutputStream()));
               final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

               readerThread = new Thread(new Runnable() {
                  public void run() {
                     try {
                        while (!Thread.currentThread().isInterrupted()) {
                           String line = reader.readLine();
                           if (line == null) throw new EOFException("End of file reached");
                           handleMessage(line);
                        }
                     } catch (Exception e) {
                        logger.error(e);
                     } finally {
                        if (socket != null) try { socket.close(); } catch (IOException ignore) { }
                        socket = null;

                        if (writerThread != null && (!writerThread.isInterrupted() || writerThread.isAlive()))
                           writerThread.interrupt();

                        handleDisconnected("Reader Thread Closed");
                     }
                  }
               }, "IRC Reader");

               readerThread.setDaemon(true);
               readerThread.start();

               writerThread = new Thread(new Runnable() {

                  private boolean shouldWait(Command command) {

                     if (!registered && command.getCommandName().requiresRegistration())
                        return true;

                     if (command instanceof Privmsg) {
                        Privmsg msg = (Privmsg)command;
                        if (msg.targetIsChannel()
                            && channels.contains(msg.getTarget())
                            && !channels.get(msg.getTarget()).isJoined())
                        return true;
                     }

                     return false;
                  }

                  private void takeAndSend() throws InterruptedException {
                     Command command = commandQueue.take();

                     while (shouldWait(command)) {
                        if (!commandQueue.isEmpty())
                           for (Command c : commandQueue)
                              if (!shouldWait(c))
                                 if (commandQueue.remove(c))
                                    send(c);
                        while (commandQueue.peek() != null && !shouldWait(commandQueue.peek())) takeAndSend();
                        synchronized (commandQueueMutex) { commandQueueMutex.wait(1000); }
                     }
                     send(command);
                  }

                  public void run() {
                     while(!Thread.currentThread().isInterrupted()) {
                        try {
                           takeAndSend();
                        } catch (Exception e) {
                           writer.close();
                           break;
                        }
                     }
                  }

                  private void send(Command command)  {
                     writer.print(command + "\r\n");
                     writer.flush();
                  }
               }, "IRC Writer");

               writerThread.setDaemon(true);
               writerThread.start();

            } catch (SocketTimeoutException e) {
               handleDisconnected("Connect timed out to " + server);
            } catch (Exception e) {
               handleDisconnected("Socket connection to " + server + " failed: " + e.getMessage());
            }
         }
      }
   }
}
