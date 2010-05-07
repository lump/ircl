import net.lump.log4j.IrcAppender;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.annotations.Test;

import java.util.ArrayList;

/**
 * Tests the IRC Appender
 *
 * @author troy
 * @version $Id: TestIrcAppender.java,v 1.3 2010/05/07 18:42:22 troy Exp $
 */
public class TestIrcAppender {
   private static volatile int blah = 0;

   /**
    * This tests the IRC Logger by starthing five threads and logs to all five at the same time.
    */
   @Test
   public void TestIRCLogger() {

      // empty configured log
      BasicConfigurator.configure(new AppenderSkeleton(){
         @Override
         protected void append(LoggingEvent event) {
            System.err.println(event.getRenderedMessage());
         }
         @Override
         public boolean requiresLayout() { return false; }
         @Override
         public void close() {}
      });

      blort(
          getIRCLogger("#test"),
          getIRCLogger("#test-one"),
          getIRCLogger("#test-two"),
          getIRCLogger("#test-three"),
          getIRCLogger("#test-four"));
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
      a.setHost("localhost");  // you will need to be running an IRC server that listens to localhost
      //a.setNick("test" + (++blah));
      a.setNick("TestIRCLogger");
      a.setOperUser("soar");
      a.setOperPass("billy.go");
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
