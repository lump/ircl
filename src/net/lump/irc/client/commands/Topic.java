package net.lump.irc.client.commands;

/**
 * Topic.
 *
 * @author M. Troy Bowman
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
