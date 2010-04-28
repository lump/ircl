package net.lump.irc.client;

import net.lump.irc.client.commands.Command;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The socket that is used to talk to the server.
 *
 * @author troy
 * @version $Id: Connection.java,v 1.1 2010/04/28 03:12:47 troy Exp $
 */
public class Connection {

   private Socket socket;
   private BufferedReader reader;
   private PrintWriter writer;
   private State state;
   Thread readerThread;


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
          state.getServer().getCanonicalHostName(), state.getPort(), state.nick);

      synchronized (connections) {
         if (connections.containsKey(signature))
            connection = connections.get(signature);
         else {
            connection = new Connection(state);
         }

         if (connection.socket == null || connection.socket.isClosed() || !connection.socket.isConnected())
            connection.connect();
      }
      return connection;
   }




   private void connect() throws IOException
   {
      socket = new Socket(state.getServer(), state.getPort());
      socket.setKeepAlive(true);
      socket.setSoLinger(false, 0);
      socket.setSoTimeout(15 * MINUTE);
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writer = new PrintWriter(socket.getOutputStream());

      readerThread = new Thread(new Runnable() {
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
               socket = null;
               reader = null;
            }
         }
      }, "IRC Reader");

      readerThread.setDaemon(true);
      readerThread.start();

      if (state.getPass() != null) send(state.getPass());
      send(state.getUser());
      send(state.getNick());
   }

   private static final Pattern numericReply = Pattern.compile("^:(\\S+)+\\s(\\d{3})\\s+(.*)$");


   public void send(Command command) {
      if (command != null) writer.print(command.toString() + "\r\n");
      writer.flush();
   }

   private void processLine(String line) {
      Matcher m = numericReply.matcher(line);
      if (m.matches()) {
         String server = m.group(1);
         Response response = Response.fromCode(m.group(2));
         ArrayList<String> arguments = new ArrayList<String>();
         String message = "";
         boolean hitColon = false;
         if (m.group(3) != null) {
            for (String s : m.group(3).split("\\s")) {
               if (s.startsWith(":")) { hitColon = true; s = s.substring(1); }
               if (hitColon) message += (message.length() > 0 ? " " : "") + s;
               else arguments.add(s);
            }
         }

         for (IrcEventListener l : state.getListeners())
            l.handleResponse(server, response, arguments.toArray(new String[arguments.size()]), message);
      }
   }
}
