import net.lump.irc.client.*;
import net.lump.irc.client.commands.Nick;
import net.lump.irc.client.commands.User;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * .
 *
 * @author troy
 * @version $Id: TestConnect.java,v 1.1 2010/04/28 03:16:14 troy Exp $
 */
public class TestConnect {

   @Test
   public void testConnect() throws Exception {

      final HashMap<Response, Boolean> checkFlags = new HashMap<Response, Boolean>();
      checkFlags.put(Response.RPL_WELCOME, false);
      checkFlags.put(Response.RPL_YOURHOST, false);
      checkFlags.put(Response.RPL_MOTD, false);
      checkFlags.put(Response.RPL_ENDOFMOTD, false);
      final TestConnect thisObject = this;

      User user = new User("test", "Testing 1 2 3", User.Mode.invisible, User.Mode.wallops);
      Nick nick = new Nick("testing");
      State state = new State("gargamel.sosstaffing.com", user, nick);
      state.addListener(new AbstractIrcEventListener(){
         @Override
         public void handleResponse(String server, Response r, String[] args, String message) {
            checkFlags.put(r, true);
            if (done(checkFlags)) synchronized(thisObject) { thisObject.notify(); }
         }
      });
      Connection c = Connection.getConnection(state);

      long time = System.currentTimeMillis();
      while (!done(checkFlags) && (time > (System.currentTimeMillis() - 10000)))
         synchronized (this) { wait(1000); }

      assert done(checkFlags) : "didn't get all expected messages";
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
