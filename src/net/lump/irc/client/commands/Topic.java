package net.lump.irc.client.commands;

/**
 * Topic.
 *
 * @author troy
 * @version $Id: Topic.java,v 1.1 2010/05/01 20:22:04 troy Exp $
 */
public class Topic extends Command {
   String topic;

   public Topic() {
   }

   public Topic(String topic) {
      this.topic = topic;
   }

   @Override
   protected String[] getArgs() {
      return topic == null ? new String[0] : new String[]{":"+topic};
   }
}
