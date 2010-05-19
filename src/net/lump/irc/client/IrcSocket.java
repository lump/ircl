package net.lump.irc.client;

import net.lump.irc.client.commands.Command;
import net.lump.irc.client.listeners.IrcMessageListener;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The socket that is used to talk to the server.
 *
 * @author M. Troy Bowman
 */
public class IrcSocket {

   private Socket socket;
   private InetSocketAddress address;
   private boolean registered = false;
   private Thread readerThread;
   private Thread writerThread;


   private static final Object placeholder = new Object();
   private static final Object commandQueueMutex = new Object();
   private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();

   private final ConcurrentHashMap<IrcMessageListener, Object> messageListeners
       = new ConcurrentHashMap<IrcMessageListener, Object>();
   private static final Logger logger = Logger.getLogger(IrcSocket.class);
   private static final int MINUTE = 60000;

   protected IrcSocket(InetSocketAddress address, IrcMessageListener listener) {
      this.address = address;
      addIrcMessageListener(listener);
      socket = null;
      readerThread = null;
      writerThread = null;
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
            socket.connect(address, 15000);

            if (readerThread != null && readerThread.isAlive()) { readerThread.interrupt(); readerThread = null; }
            if (writerThread != null && writerThread.isAlive()) { writerThread.interrupt(); writerThread = null; }

            final PrintWriter writer = new PrintWriter(socket.getOutputStream());
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            readerThread = new Thread(new Runnable() {
               public void run() {
                  try {
                     while (!Thread.currentThread().isInterrupted()) {
                        String line = reader.readLine();
                        if (line == null) throw new EOFException("End of file reached");
                        else for (IrcMessageListener l : IrcSocket.this.getMessageListeners())
                           l.handleMessage(line);
                     }
                  } catch (IOException exc) {
                     if (socket != null) try { socket.close(); } catch (IOException ignore) { }
                     socket = null;
                     if (writerThread != null && (!writerThread.isInterrupted() || writerThread.isAlive()))
                        writerThread.interrupt();
                     for (IrcMessageListener l : IrcSocket.this.getMessageListeners())
                        l.handleDisconnected("Reader Thread Closed");
                  }
               }
            }, "IRC Reader");

            readerThread.setDaemon(true);
            readerThread.start();

            writerThread = new Thread(new Runnable() {
               public void run() {
                  while(!Thread.currentThread().isInterrupted()) {
                     try {
                        Command command = commandQueue.take();
                        // if we're not registered, wait until we are
                        while (!isRegistered() && command.getCommandName().requiresRegistration()) {
                           for (Command c : commandQueue) {
                              // flush all non-registration-dependent commands through
                              if (!c.getCommandName().requiresRegistration()) {
                                 commandQueue.remove(command);
                                 send(command);
                              }
                           }
                           synchronized (commandQueueMutex) { commandQueueMutex.wait(1000); }
                        }
                        send(command);
                     } catch (InterruptedException e) {
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
            logger.error("Connect timed out to " + address);
            return;
         } catch (Exception e) {
            logger.error("Socket connection to " + address + " failed: " + e.getMessage());
            return;
         }
      }

   }

   public void addIrcMessageListener(IrcMessageListener m) {
      messageListeners.put(m, placeholder);
   }

   public void removeIrcMessageListener(IrcMessageListener m) {
      messageListeners.remove(m);
   }

   public Set<IrcMessageListener> getMessageListeners() {
      return messageListeners.keySet();
   }

   public boolean isRegistered() {
      return registered;
   }

   public void setRegistered(boolean registered) {
      this.registered = registered;
   }

   public void queueCommand(Command command) {
      commandQueue.add(command);
   }

   Object getCommandQueueMutex() {
      return commandQueueMutex;
   }

}
