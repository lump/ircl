import net.lump.log4j.IrcAppender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.testng.annotations.Test;

import java.util.ArrayList;

/**
 * .
 *
 * @author troy
 * @version $Id: TestIrcAppender.java,v 1.1 2010/04/30 22:27:04 troy Exp $
 */
public class TestIrcAppender {
   private static volatile int blah = 0;

   @Test
   public void TestIRCLogger() {
      BasicConfigurator.configure();
      blort(
          getIRCLogger("#soar"),
          getIRCLogger("#soar-servlets"),
          getIRCLogger("#soar-one"),
          getIRCLogger("#soar-two"),
          getIRCLogger("#soar-three"));

   }

   class R implements Runnable {
      Logger logger;
      public R(Logger logger) {
         this.logger= logger;
      }

      public void run() {
         for (Level l : new Level[]{Level.ALL, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL})
            for (String s : new String[]{"Zero","One","Two","Three","Four","Five","Six","Seven","Eight","Nine"}) {
               logger.log(l, String.format("%s logging at level %s %s", Thread.currentThread().getName(), l.toString(), s));
               try {Thread.sleep((int)(Math.random()*200));} catch (InterruptedException ignore){}
            }
      }
   }

   private void blort(Logger... loggers) {
      ArrayList<Thread> threads = new ArrayList<Thread>();

      for (Logger l : loggers) {
         Thread t = new Thread(new R(l));
         threads.add(t);
      }
      for (Thread t : threads) {
         try {Thread.sleep(1000);} catch (InterruptedException ignore){}
         t.start();
      }

      for (Thread t : threads)
         try {
            t.join();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
   }

   private Logger getIRCLogger(String channel) {
      IrcAppender a = new IrcAppender();
      a.setPort(6667);
      a.setHost("gargamel.sosstaffing.com");
      //a.setNick("test" + (++blah));
      a.setNick("test");
      a.setOperUser("soar");
      a.setOperPassword("billy.go");
      a.setChannel(channel);
      a.setRealName("Troy Bowman");
      a.setThreshold(Level.ALL);
      a.setLayout(new PatternLayout("%p [%c{1}] %m%n"));
      a.setName("IRC-" + channel);
      Logger l = Logger.getLogger("logger"+channel);
      l.addAppender(a);
      return l;
   }
}
