package net.lump.irc.client;

import net.lump.irc.client.commands.Command;
import net.lump.irc.client.commands.CommandName;
import net.lump.irc.client.commands.Join;
import net.lump.irc.client.commands.Pong;
import net.lump.irc.client.listeners.IrcEventListener;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The socket that is used to talk to the server.
 *
 * @author troy
 * @version $Id: Connection.java,v 1.3 2010/04/29 03:47:26 troy Exp $
 */
public class Connection {

   private Socket socket;
   private State state;

   private Thread readerThread;
   private Thread writerThread;

   private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();

   private static final Logger logger = Logger.getLogger(Connection.class);
   private static final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
   private static final int MINUTE = 60000;

   private Connection(State state)
   {
      this.state = state;
      socket = null;
   }

   public static Connection getConnection(State state) throws IOException {
      Connection connection;
      String signature = String.format("%s %d %s",
          state.getServer().getCanonicalHostName(), state.getPort(), state.getNick());

      synchronized (connections) {
         if (connections.containsKey(signature))
            connection = connections.get(signature);
         else {
            connection = new Connection(state);
         }
      }
      return connection;
   }


   public void connect() throws IOException
   {
      if (socket != null && !socket.isClosed() && socket.isConnected()
          && readerThread != null && readerThread.isAlive()
          && writerThread != null && writerThread.isAlive()
          ) return;

      socket = new Socket(state.getServer(), state.getPort());
      socket.setKeepAlive(true);
      socket.setSoLinger(false, 0);
      socket.setSoTimeout(15 * MINUTE);

      if (readerThread != null && readerThread.isAlive()) { readerThread.interrupt(); }
      if (writerThread != null && writerThread.isAlive()) { writerThread.interrupt(); }

      readerThread = new Thread(new Runnable() {
         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         public void run() {
            try {
               while (!Thread.currentThread().isInterrupted()) {
                  if (reader != null) {
                     String line = reader.readLine();
                     if (line == null) throw new EOFException("End of file reached");
                     else processLine(line);
                  }
               }
            } catch (IOException exc) {
               if (socket != null) try { socket.close(); } catch (IOException ignore) { }
               reader = null;
               InetAddress socketInetAddress = socket.getInetAddress();
               socket = null;
               if (writerThread != null && (!writerThread.isInterrupted() || writerThread.isAlive()))
                  writerThread.interrupt();
               for (IrcEventListener l : state.getListeners())
                  l.handleDisconnected(new String[]{""},"Reader Thread Closed");
            }
         }
      }, "IRC Reader");

      readerThread.setDaemon(true);
      readerThread.start();

      writerThread = new Thread(new Runnable() {
         PrintWriter writer = new PrintWriter(socket.getOutputStream());
         public void run() {
            while(!Thread.currentThread().isInterrupted()) {
               try {
                  Command command = commandQueue.take();
                  // if we're not registered, wait until we are
                  while (!state.isRegistered() && command.getCommandName().requiresRegistration()) {
                     for (Command c : commandQueue) {
                        // flush all non-registration-dependent commands through
                        if (!c.getCommandName().requiresRegistration()) {
                           commandQueue.remove(command);
                           send(command);
                        }
                     }
                     synchronized (commandQueue) { commandQueue.wait(1000); }
                  }
                  send(command);
               } catch (InterruptedException e) {
                  writer.close();
                  writer = null;
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

      if (state.getPass() != null) send(state.getPass());
      send(state.getUser());
      send(state.getNick());
      if (state.getOper() != null) send(state.getOper());
   }


   public void send(Command command) {
      if (command instanceof Join) {
         for (Channel c : ((Join)command).getChannels())
            state.putChannel(c);
      }
      commandQueue.add(command);
   }

   private static final Pattern messagePattern = Pattern.compile("^(?::(\\S+)\\s)?(\\d{3}|[A-Za-z]+)\\s+(.*)$");
   private static final Pattern numericCommandPattern = Pattern.compile("^\\d{3}$");

   private void processLine(String line) {
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
            if (!state.isRegistered()
                && response.getType() == Response.Type.Reply
                && arguments.size() > 0
                && arguments.get(0).equals(state.getNick().name())) {
               state.setRegistered(true);
               synchronized(commandQueue) { commandQueue.notify(); }
            }

            for (IrcEventListener l : state.getListeners())
               l.handleResponse(prefix, response, arguments.toArray(new String[arguments.size()]), message);
         }
      }
      else {
         try {
            CommandName commandName = CommandName.valueOf(messageMatcher.group(2));

            switch(commandName) {
               // handle PING here automatically
               case PING:
                  Pong pong = new Pong(state.getIrcHost(), message);
                  send(pong);
                  logger.debug("Sent pong: " + pong);
                  break;
            }

            for (IrcEventListener l : state.getListeners())
               l.handleCommand(prefix, commandName, arguments.toArray(new String[arguments.size()]), message);

         } catch (IllegalArgumentException iae) {
            logger.warn("Unknown Command: (" + messageMatcher.group(2) + ") for message: " + line);
         }
      }
   }
}
