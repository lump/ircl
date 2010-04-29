import net.lump.irc.client.*;
import net.lump.irc.client.commands.*;
import net.lump.irc.client.listeners.AbstractIrcEventListener;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * .
 *
 * @author troy
 * @version $Id: TestConnect.java,v 1.3 2010/04/29 03:47:26 troy Exp $
 */
public class TestConnect {

   private static final Logger logger = Logger.getLogger(TestConnect.class);

   static {
      BasicConfigurator.configure();
   }

   @Test
   public void testConnect() throws Exception {

      final HashMap<Object, Boolean> checkFlags = new HashMap<Object, Boolean>();
      checkFlags.put(Response.RPL_WELCOME, false);
      checkFlags.put(Response.RPL_YOURHOST, false);
      checkFlags.put(Response.RPL_ENDOFMOTD, false);
      checkFlags.put(CommandName.MODE, false);
      checkFlags.put(CommandName.JOIN, false);
      checkFlags.put(CommandName.PART, false);
      checkFlags.put("Disconnected", false);
      final TestConnect thisObject = this;

      User user = new User("Testing 1 2 3", User.Mode.invisible, User.Mode.wallops);
      Nick nick = new Nick("testing");
      Oper oper = new Oper("soar","billy.go");
      final State state = new State("gargamel.sosstaffing.com", user, nick, oper);
      final Connection c = Connection.getConnection(state);

      state.addListener(new AbstractIrcEventListener() {
         @Override
         public void handleResponse(Prefix prefix, Response r, String[] args, String message) {
            if (checkFlags.containsKey(r)) {
               logger.info("got " + r);
               checkFlags.put(r, true);
            }
            if (done(checkFlags)) synchronized(thisObject) { thisObject.notify(); }
         }

         @Override
         public void handleCommand(Prefix prefix, CommandName c, String[] args, String message) {
            if (prefix.getNick() != null && prefix.getNick().equals(state.getNick().name())
                || prefix.getHost() != null && prefix.getHost().equals(state.getNick().name()))
               switch (c) {
                  case JOIN:
                     logger.info("got " + c);
                     checkFlags.put(c, true); break;
                  case MODE:
                     logger.info("got " + c);
                     checkFlags.put(c, true); break;
                  case PART:
                     logger.info("got " + c);
                     checkFlags.put(c, true); break;
               }
            if (done(checkFlags)) synchronized(thisObject) { thisObject.notify(); }
         }

         public void handleNickNameInUse(String[] args, String message) {
            if (args[1].equals(state.getNick().name())) {
               state.setNick(state.getNick().increment());
            }
            if (c != null) c.send(state.getNick());
         }

         public void handleDisconnected(String[] args, String message) {
            logger.info(message);
            checkFlags.put("Disconnected", true);
         }
      });

      try {
         c.connect();

         Channel channel = new Channel("#soar");

         c.send(new Join(channel));
         c.send(new Privmsg(channel.getName(), "Hello World"));
         c.send(new Part(channel));
         c.send(new Quit("I'm outta here"));
      }
      catch (IOException ioe)
      {
         assert false : "couldn't connect";
      }


      long time = System.currentTimeMillis();
      while (!done(checkFlags) && (time > (System.currentTimeMillis() - 100000)))
         synchronized (this) { wait(1000); }

      assert done(checkFlags) : "didn't get all expected messages";
   }

   private boolean done(HashMap<Object, Boolean> checkFlags) {
      boolean done = true;
      for (Object i : checkFlags.keySet()) {
         if (checkFlags.get(i).equals(Boolean.FALSE)) {
            done = false;
            break;
         }
      }
      return done;
   }
}
