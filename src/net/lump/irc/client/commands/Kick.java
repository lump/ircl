package net.lump.irc.client.commands;

import net.lump.irc.client.Channel;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Kick.
 *
 * @author troy
 * @version $Id: Kick.java,v 1.1 2010/05/01 20:22:04 troy Exp $
 */
public class Kick extends Command {
   private final ArrayList<Channel> channels = new ArrayList<Channel>();
   private final ArrayList<Nick> users = new ArrayList<Nick>();
   private String reason;

   public Kick(Channel channel, Nick user) {
      this(channel, user, null);
   }

   public Kick(Channel channel, Nick user, String reason) {
      addChannelNickPair(channel, user);
      this.reason = reason;
   }

   public Kick(Channel channel, Nick... users) {
      this(channel, null, users);
   }

   public Kick(Channel channel, String reason, Nick... users) {
      channels.add(channel);
      this.users.addAll(Arrays.asList(users));
      this.reason = reason;
   }


   public Kick addChannelNickPair(Channel channel, Nick user) {
      channels.add(channel);
      users.add(user);
      return this;
   }

   @Override
   protected String[] getArgs() {
      ArrayList<String> args = new ArrayList<String>();

      args.add(join(channels, ","));
      args.add(join(users, ","));
      if (reason != null) args.add(":"+reason);

      return args.toArray(new String[args.size()]);
   }
}
