package net.lump.irc.client;

import net.lump.irc.client.commands.CommandName;
import net.lump.irc.client.exception.IllegalChannelException;
import net.lump.irc.client.listeners.AbstractIrcEventListener;
import net.lump.irc.client.listeners.ChannelListener;
import net.lump.irc.client.listeners.IrcEventListener;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * .
 *
 * @author M. Troy Bowman
 */
public class Channel {

   @SuppressWarnings({"UnusedDeclaration"})
   public enum Mode {
      creator('O'),
      operator('o'),
      voice('v'),
      anonymous('a'),
      inviteOnly('i'),
      moderated('m'),
      noOutsidePrivmsg('n'),
      quiet('q'),
      secret('s'),
      reOp('r'),
      topicByOperOnly('t'),
      password('k'),
      userLimit('l'),
      banMask('b'),
      exceptionMask('e'),
      invitationMask('i');

      char modeChar;
      private static final HashMap<Character, Mode> reverseMap = new HashMap<Character, Mode>();

      private Mode(char m) {
         modeChar = m;
      }

      public static String toString(Mode... modes) {
         String out = "";
         for (Mode m : modes) out += m.modeChar;
         return out;
      }

      public static Mode modeOf(char c) {
         // bootstrap revmap because we can't refer to static hashmaps in enum constructors
         if (reverseMap.isEmpty())
            synchronized (reverseMap) { // only generate this once, and make other threads wait until it's generated.
               if (reverseMap.isEmpty()) for (Mode m : Mode.values()) reverseMap.put(m.modeChar, m);
            }
         return reverseMap.get(c);
      }

      public static Mode[] parseString(String modes) {
         ArrayList<Mode> out = new ArrayList<Mode>();
         if (modes.startsWith("+") || modes.startsWith("-")) modes = modes.substring(1);
         for (int x=0; x<modes.length(); x++) {
            Mode m = modeOf(modes.charAt(x));
            if (m != null) out.add(m);
         }
         return out.toArray(new Mode[out.size()]);
      }
   }

   private Mode[] modes;
   private String password;
   private String name;
   private boolean joined = false;
   final private HashSet<String> nicks = new HashSet<String>();

   private String topic;
   private static final Object placeholder  = new Object();
   private static final Logger logger = Logger.getLogger(Channel.class);

   private IrcEventListener listener = new AbstractIrcEventListener() {

      public void handleResponse(Prefix prefix, Response r, String[] args, String message) {
         switch(r) {
            case RPL_NAMREPLY:
               if (args[2] != null && args[2].equals(getName())) {
                  String[] nicks = message.split("\\s+");
                  getNicks().clear();
                  getNicks().addAll(Arrays.asList(nicks));
                  for (ChannelListener l : getListeners()) l.onNames(nicks);
               }
               break;
            case RPL_TOPIC:
               if (args[1] != null && args[1].equals(getName())) {
                  topic = message;
                  for (ChannelListener l : getListeners()) l.onTopic(message);
               }
               break;
            case RPL_CHANNELMODEIS:
               if (args[1] != null && args[1].equals(getName()) && args[2] != null) {
                  modes = Mode.parseString(args[2]);
                  for (ChannelListener l : getListeners()) l.onMode(modes, new String[]{});
               }
               break;
         }
      }

      public void handleCommand(Prefix prefix, CommandName c, String[] args, String message) {
         switch(c) {
            case JOIN:
               if (message.equals(getName())) {
                  getNicks().add(prefix.getNick());
                  for (ChannelListener l : getListeners()) l.onJoin(prefix, message);
               }
               break;
            case MODE:
               if (args[0] != null && args[0].equals(getName())){
                  String mode = args[1] == null ? "" : args[1];
                  for (ChannelListener l : getListeners())
                     l.onMode(Channel.Mode.parseString(args[1] != null ? args[1] : ""), args);
               }
               break;
            case PART:
               if (args[0] != null && args[0].equals(getName())) {
                  getNicks().remove(prefix.getNick());
                  for (ChannelListener l : getListeners()) l.onPart(prefix, message);
               }
               break;
            case TOPIC:
               if (args[0] != null && args[0].equals(getName())) {
                  for (ChannelListener l : getListeners()) l.onTopic(message);
                  topic = message;
               }
               break;
            case QUIT:
               getNicks().remove(prefix.getNick());
               for (ChannelListener l : getListeners()) l.onQuit(prefix, message);
               break;
            case PRIVMSG:
               if (args[0] != null && args[0].equals(getName()))
                  for (ChannelListener l : getListeners()) l.onPrivmsg(prefix, message);
               break;
            case NOTICE:
               if (args[0] != null && args[0].equals(getName()))
                  for (ChannelListener l : getListeners()) l.onNotice(prefix, message);
               break;
         }
      }

      public void handleNickProblem(Prefix prefix, Response r, String[] args, String message) {
         // not to be used here
      }

      public void onDisconnect(String[] args, String message) {
         // not to be used here
      }
   };

   private final ConcurrentHashMap<ChannelListener, Object> listeners = new ConcurrentHashMap<ChannelListener, Object>();

   {
      addListener(new ChannelListener(){

         public void onNames(String[] names) {
            logger.debug(String.format("%s names: %s", name, Arrays.asList(names).toString()));
         }

         public void onJoin(Prefix prefix, String message) {
            logger.debug(String.format("%s join: %s", name, message));
         }

         public void onTopic(String topic) {
            logger.debug(String.format("%s topic: %s", name, topic));
         }

         public void onMode(Mode[] modes, String[] args) {
            logger.debug(String.format("%s modes: %s args: %s", name, Mode.toString(modes), Arrays.asList(args).toString()));
         }

         public void onPart(Prefix prefix, String message) {
            logger.debug(String.format("%s part: %s message: %s", name, prefix, message));
         }

         public void onQuit(Prefix prefix, String message) {
            logger.debug(String.format("%s quit: %s message: %s", name, prefix, message));
         }

         public void onPrivmsg(Prefix prefix, String message) {
            logger.debug(String.format("%s privmsg: %s message: %s", name, prefix, message));
         }

         public void onNotice(Prefix prefix, String message) {
            logger.debug(String.format("%s notice: %s message: %s", name, prefix, message));
         }
      });
   }

   public Channel(String name) throws IllegalChannelException {
      this(name, null);
   }

   public static boolean isValidChannelName(String channel) {
      return channel.matches("^[\\&\\#\\+\\!].*");
   }

   public Channel(String name, String password) throws IllegalChannelException {
      if (!isValidChannelName(name)) throw new IllegalChannelException("Channels must start with any of &, #, +, !");
      this.name = name;
      this.password = password;

   }

   private Set<ChannelListener> getListeners() {
      return listeners.keySet();
   }

   public Channel removeListener(ChannelListener c) {
      listeners.remove(c, placeholder);
      return this;
   }

   public Channel addListener(ChannelListener c) {
      listeners.put(c, placeholder);
      return this;
   }

   public IrcEventListener getIrcEventListener() {
      return listener;
   }

   public Mode[] getModes() {
      return modes;
   }

   public String getPassword() {
      return password;
   }

   public String getName() {
      return name;
   }

   public HashSet<String> getNicks() {
      return nicks;
   }

   public String getTopic() {
      return topic;
   }

   public String toString() {
      return name;
   }

   public boolean isJoined() {
      return joined;
   }

   public void setJoined(boolean joined) {
      this.joined = joined;
   }
}
