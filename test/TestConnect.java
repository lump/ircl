import net.lump.irc.client.*;
import net.lump.irc.client.commands.*;
import net.lump.irc.client.listeners.AbstractIrcEventListener;
import org.apache.log4j.BasicConfigurator;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * .
 *
 * @author troy
 * @version $Id: TestConnect.java,v 1.2 2010/04/29 03:06:09 troy Exp $
 */
public class TestConnect {

   static {
      BasicConfigurator.configure();
   }

   @Test
   public void testConnect() throws Exception {

      final HashMap<Response, Boolean> checkFlags = new HashMap<Response, Boolean>();
      checkFlags.put(Response.RPL_WELCOME, false);
      checkFlags.put(Response.RPL_YOURHOST, false);
      checkFlags.put(Response.RPL_MOTD, false);
      checkFlags.put(Response.RPL_ENDOFMOTD, false);
      final TestConnect thisObject = this;

      User user = new User("Testing 1 2 3", User.Mode.invisible, User.Mode.wallops);
      Nick nick = new Nick("abcdefghijklm");
      Oper oper = new Oper("soar","billy.go");
      final State state = new State("gargamel.sosstaffing.com", user, nick, oper);
      final Connection c = Connection.getConnection(state);

      state.addListener(new AbstractIrcEventListener() {
         @Override
         public void handleResponse(Prefix prefix, Response r, String[] args, String message) {
            checkFlags.put(r, true);
            if (done(checkFlags)) synchronized(thisObject) { thisObject.notify(); }
         }

         public void handleNickNameInUse(String[] args, String message) {
            if (args[1].equals(state.getNick().name())) {
               state.setNick(state.getNick().increment());
            }
            if (c != null) c.send(state.getNick());
         }

         public void handleDisconnected(InetAddress address) {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            try {
               c.connect();
            } catch (IOException e) {
               System.err.println("Can't connect to " + address);
               System.exit(1);
            }
         }

      });

      try {
         c.connect();

         Channel channel = new Channel("#soar");

         c.send(new Join(channel));
         c.send(new Privmsg(channel.getName(), "Hello World"));
      }
      catch (IOException ioe)
      {
         assert false : "couldn't connect";
      }


      long time = System.currentTimeMillis();
      while (!done(checkFlags) && (time > (System.currentTimeMillis() - 100000)))
         synchronized (this) { wait(1000); }

      assert done(checkFlags) : "didn't get all expected messages";
      c.send(new Quit("I'm outta here"));
   }

   private boolean done(HashMap<Response, Boolean> checkFlags) {
      boolean done = true;
      for (Response i : checkFlags.keySet()) {
         if (checkFlags.get(i).equals(Boolean.FALSE)) {
            done = false;
            break;
         }
      }
      return done;
   }
}
