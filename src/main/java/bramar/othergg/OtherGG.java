package bramar.othergg;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

/**
 * OtherGG for other servers. Needs at least Java 8
 * <br><strong>Changelog:</strong><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Update 1.5:
 * <ul>
 * <li>Added configuration to change chat to ALL chat in jartex when saying GG</li>
 * <li>Added regex for Jartex Practice</li>
 * <li>Added seperate config for Jartex Practice</li>
 * <li>Added online compatibility. Can easily add servers without updating the mod! [Online using pastebin]</li>
 * <li>Repudiate.java has been replaced with java's built-in Supplier interface</li>
 * </ul>
 * &nbsp;&nbsp;&nbsp;&nbsp;Update 1.7:
 * <ul>
 * <li>Added configuration for Web Timeout</li>
 * <li>Hypixel (including casual) and Mineplex regex's are now online (Pastebin)</li>
 * </ul>
 * <br><strong>License:</strong><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MIT License
 * @since 1.0
 * @author bramar
 */
@Mod(guiFactory = "bramar.othergg.Factory", clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]", name = "OtherGG", modid = "othergg", version = OtherGG.VERSION)
public class OtherGG {
	
	private static long CONNECT_WEB_TIMEOUT = 60000; // ms
	private static final URL PASTEBIN;
	static {
		URL u;
		try {
			u = new URL("https://pastebin.com/raw/CamhjJpp");
		}catch(Exception e1) {
			u = null;
		}
		PASTEBIN = u;
	}
	// Testing
	
	public static void main(String[] args) {
		
	}
	
	//
	static final String VERSION = "1.7";
	
	// Configuration
	protected static final List<String> validConfigKeys = new ArrayList<String>();
	
	/* General */
	public static boolean modEnabled = true;
	public static String ggMessage = "gg";
	public static int interval = 0;
	/* Jartexnetwork */
	public static boolean j_enabled = true;
	public static boolean j_newPartySystem = true;
	public static boolean j_changeChat = true;
	public static boolean j_practice = false;
	/* Not config */
	public static boolean j_inParty = false;
	public static boolean j_partyChat = false;
	private static boolean j_pcu = false;
	/*            */
	private static final Pattern JARTEX_URL = Pattern.compile(
			"(mp|top|mc|play){0,1}(.)?(jartexnetwork|jartex).(com|fun)([\\.]+)?",
			Pattern.CASE_INSENSITIVE);
	
	// Automatically /party chat to Global if its currently in party
	// OLD PARTY SYSTEM 
	public static final String JCHAT_TO_GLOBAL = "PARTY | Your chat channel has been set to Global"; // Chat to Global which means final result is Global Chat
	public static final String JCHAT_TO_PARTY = "PARTY | Your chat channel has been set to Party Only"; // Chat to Party which means final result is Party Chat
	public static final String JAUTO_PARTY_DISBAND = "{player} disbanded the party! If you wish to play with your friends again try /party!"; // Chat when party is disbanded.
	public static final String JPLAYER_PARTY_DISBAND = "The party has been disbanded! If you wish to play with your friends again try /party!"; // Chat when party is disbanded BY a player.
	public static final String JPARTY_JOIN = "| {player} joined the party!"; // Chat when someone joined the party
	public static final String JPARTY_LEAVE = "| {player} has left the party."; // Chat when someone leaves the party
	
	// NEW PARTY SYSTEM
	public static final String JCHAT_TO_GLOBAL2 = "Party | Your chat channel has been set to Global";
	public static final String JCHAT_TO_PARTY2 = "Party | Your chat channel has been set to Party Only";
	public static final String JAUTO_PARTY_DISBAND2 = "Party | The party has been disbanded! If you wish to play with your friends again try /party!";
	public static final String JPLAYER_PARTY_DISBAND2 = "Party | {player} disbanded the party! If you wish to play with your friends again try /party!";
	public static final String JPARTY_JOIN2 = "Party | | {player} joined the party!";
	public static final String JPARTY_LEAVE2 = "Party | | {player} has left the party.";
	
	// Regex's
	public static final Pattern JARTEX_REGEX = Pattern.compile(
			" Play Again\\? Click here!| Top (Final Kills|Kills)\\:| [a-zA-Z_0-9-]{1,16} won the game!| Team (Red|Blue|Green|Yellow|Aqua|White|Pink|Gray|Grey) won the game!| #[1-5]{1,2} (Final Killer|Killer) \\w{1,16} \\| [0-9]{1,5}",
			Pattern.CASE_INSENSITIVE
			);
	public static final Pattern JARTEX_PRAC = Pattern.compile(
			"|Practice \\| You (won|lost) the fight! (:\\(|:\\))(.)?| (WINNER|LOSER) (\\w{1,16} \\w{1,16}|\\w{1,16}) \\[\\d{1,4}(\\|)?\\]",
			Pattern.CASE_INSENSITIVE);
	
	public void sendChat(String str) {
		mc.thePlayer.sendChatMessage(str);
	}
	public void schedule(Runnable run, long delay) {
		if(delay == 0) {
			run.run();
			return;
		}else if(delay <= 0) {
			new IllegalArgumentException("Delay must be a positive number!").printStackTrace();
			System.out.println("Failed scheduling a task: Delay must be a positive number");
			return;
		}
		final Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				run.run();
				timer.cancel();
			}
		}, delay, delay);
	}
	public Configuration config;
	public void loadConfig() {
		modEnabled = (Boolean) loadConfigKey("general", "ModEnabled", true, "Whether or not the mod is enabled");
		ggMessage = (String) loadConfigKey("general", "GoodGameMessage", "gg", "The text that is sent on servers if not detected");
		int interval = (Integer) loadConfigKey("general", "Interval", 0, "The delay between the win message and the GG text being sent");
		int webTimeout = (Integer) loadConfigKey("general", "WebTimeout", 60000, "The delay in milliseconds that the mod tries to connect to Pastebin for regex's (if it fails, it will wait X milliseconds until it retries)");
		if(interval >= 0) this.interval = interval;
		if(webTimeout >= 0) CONNECT_WEB_TIMEOUT = webTimeout;
		
		j_enabled = (Boolean) loadConfigKey("jartexnetwork", "J_Enabled", true, "Whether or not the mod is enabled on Jartexnetwork");
		j_newPartySystem = (Boolean) loadConfigKey("jartexnetwork", "J_NewPartySystem", true, "Whether or not the party system is new");
		j_changeChat = (Boolean) loadConfigKey("jartexnetwork", "J_ChangeChat", true, "Whether or not the mod changes party chat on Jartexnetwork");
		j_practice = (Boolean) loadConfigKey("jartexnetwork", "J_Practice", false, "Whether or not the mod says GG on a finished practice match");
		
		disabledON.clear();
		for(String configName : scToCategory.keySet()) {
			try {
				String category = scToCategory.get(configName);
				Boolean bool = (Boolean) loadConfigKey(category, configName, defaultBools.get(configName), "Whether or not this mod works on " + category);
				if(!bool)
					disabledON.add(category);	
			}catch(Exception e1) { e1.printStackTrace(); }
		}
		
		config.save();
	}
	public String getGG() {
		if(ggMessage.equalsIgnoreCase("")) return "gg";
		int count = 0;
		for(char c : ggMessage.toCharArray()) {
			if(c != ' ') count++;
		}
		return count <= 1 ? "gg" : ggMessage;
	}
	boolean testCommand = false;
	File configFile;
	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		configFile = e.getSuggestedConfigurationFile();
	}
	@EventHandler
	public void init(FMLInitializationEvent e) {
		try {
			config = new Configuration(configFile != null ? configFile : (configFile = new File("config" + File.separator + "othergg.cfg")));
			config.load();
			config.save();
			loadConfig();
			if(testCommand) ClientCommandHandler.instance.registerCommand(new TestCommand());
			
			init();
			
			registerServers(); // Offline
			schedule(this::registerServerON, 1); // Online with Pastebin
			
			 // Clone			
			System.out.println("OtherGG successfully initialized! Loading regex's from the web (async)...");
		}catch(Exception e1) {
			e1.printStackTrace();
			System.out.println("Failed to initialize OtherGG!");
		}
	}
	public void registerServerON() {
		HttpURLConnection con = null;
		Exception e;
		StringBuilder str = new StringBuilder();
		try {
			con = (HttpURLConnection) PASTEBIN.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Java " + System.getProperty("java.version") + " \"Minecraft Forge Mods\" \"OtherGG v" + VERSION + "\"");
			con.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while((line = reader.readLine()) != null) str.append(line);
			reader.close();
			registerServerON(str.toString());
			return;
		}catch(Exception e1) { e = e1; }
		finally {
			if(con != null) con.disconnect();
		}
		if(e != null) e.printStackTrace();
		int column = 1577;
		int far = 50;
		StringBuilder builder = new StringBuilder();
		for(int i = -50; i <= Math.min(str.length(), 50); i++) {
			try {
				builder.append(str.charAt(column + i));
			}catch(Exception ignored) {}
		}
		schedule(this::registerServerON, CONNECT_WEB_TIMEOUT);
	}
	final ArrayList<String> disabledON = new ArrayList<>();
	final HashMap<String, String> scToCategory = new HashMap<>();
	final HashMap<String, String> chatPrefix = new HashMap<>();
	final HashMap<String, Boolean> defaultBools = new HashMap<>();
	final long JSON_VERSION = 1;
	@SubscribeEvent
	public void chatPrefix(PlayerSayGGEvent e) {
		String str = chatPrefix.getOrDefault(e.getServerIpOrRegex(), null);
		if(str != null) {
			String newMsg = str + e.getGGMessage();
			e.setGGMessage(newMsg);
		}
	}
	public void registerServerON(String jsonString) {
		JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
		List<String> loaded = new ArrayList<>();
		long wv = json.get("version").getAsLong();
		if(wv != JSON_VERSION) {
			new JsonSyntaxException("This mod is outdated! Update it. Current JSON version: " + JSON_VERSION + ", New JSON version: " + wv).printStackTrace(); // Don't crash the client
			return;
		}
		forEach(json, (str, element) -> {
			String nameSave = null;
			if(element != null && element.isJsonObject()) try {
				JsonObject info = element.getAsJsonObject();
				JsonElement ipNullable = info.get("ip");
				String serverIP = ipNullable != null ? ipNullable.getAsString() : str;
				nameSave = title(info.get("name").getAsString());
				String name = nameSave;
//				System.out.println("Loading " + name);
				String nameSC = info.get("config_name").getAsString();
				JsonArray regex = info.get("regex").getAsJsonArray();
				List<Pattern> patterns = new ArrayList<>();
				for(JsonElement e : regex) patterns.add(Pattern.compile(e.getAsString(), Pattern.CASE_INSENSITIVE));
//				System.out.println(regex + " | " + regex.getAsJsonArray().size());
				JsonElement cpNullable = info.get("chat_prefix");
				String chatPrefix = cpNullable != null ? cpNullable.getAsString() : null;
				JsonElement defaultNullable = info.get("default");
				boolean defaultEnabled = defaultNullable == null ? true : defaultNullable.getAsBoolean();
				JsonElement flagsNullable = info.get("flags");
				int flags = flagsNullable != null ? flagsNullable.getAsInt() : Pattern.CASE_INSENSITIVE;
				// Register server
				registerServer(serverIP, (s) -> {
					if(disabledON.contains(name)) return false;
					for(Pattern pattern : patterns) {
						if(pattern.matcher(s).matches()) return true;
					}
					return false;
				}, () -> ggMessage, () -> interval);
				// Register config
				String key = nameSC + "_Enabled";
				loadConfigKey(name, key, defaultEnabled, "Whether or not this mod works on " + name);
				scToCategory.put(key, name);
				defaultBools.put(key, defaultEnabled);
				if(!defaultEnabled) disabledON.add(name);
				if(chatPrefix != null) this.chatPrefix.put(serverIP, chatPrefix);
				loaded.add(name);
			}catch(Exception e1) {
				e1.printStackTrace();
				System.out.println("[OtherGG] Failed to load regex of '"+str+"'" + (nameSave != null? " with name " + nameSave : ""));
			}
		});
		System.out.println("[OtherGG] Loaded " + loaded.size() + " regex's from pastebin!");
		System.out.println("[OtherGG] Loaded regex's: " + loaded);
	}
	private String title(String str) {
		return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
	}
	private void forEach(JsonObject json, BiConsumer<String, JsonElement> consumer) {
		for(Map.Entry<String, JsonElement> entry : json.entrySet()) consumer.accept(entry.getKey(), entry.getValue());
	}
	/*
	JartexNetwork
	Events. Party Change Chat
	*/
	@SubscribeEvent
	public void resetPartyField(ClientDisconnectionFromServerEvent e) {
		j_partyChat = j_inParty = false;
	}
	
	@SubscribeEvent
	public void jartexPartyChat(ClientChatReceivedEvent e) {
		try {
			if(modEnabled && j_changeChat && JARTEX_URL.matcher(mc.getCurrentServerData().serverIP).matches()) {
				String str = e.message.getUnformattedText();
				if(chatGlobal(str)) {
					// Set to global
					j_partyChat = false;
					j_inParty = true;
				}else if(chatParty(str)) {
					// Set to party
					j_partyChat = true;
					j_inParty = true;
				}else if(chatAPD(str) || checkIfPPD(str)) {
					j_inParty = false;
					if(j_newPartySystem) j_partyChat = false;
				}else if(selfJoin(str)) {
					j_inParty = true;
				}else if(selfLeave(str)) {
					j_inParty = false;
					if(j_newPartySystem) j_partyChat = false;
				}
			}
		}catch(Exception ignored) {}
	}
	@SubscribeEvent
	public void jartexChangeChatToGlobal(PlayerSayGGEvent e) {
		try {
			if(modEnabled && j_changeChat && j_inParty && j_partyChat && JARTEX_URL.matcher(mc.getCurrentServerData().serverIP).matches()) {
				j_pcu = true;
				sendChat("/p chat");
			}
		}catch(Exception ignored) {}
	}
	@SubscribeEvent
	public void jartexChangeChatBack(PlayerPostSayGGEvent e) {
		try {
			if(j_pcu && JARTEX_URL.matcher(mc.getCurrentServerData().serverIP).matches()) {
				j_pcu = false;
				sendChat("/p chat");
			}else if(j_pcu) j_pcu = false;
		}catch(Exception ignored) {}
	}
	
	public String getPartyJoin(String playerName, int n) {
		return n == 0 ? JPARTY_JOIN.replace("{player}", playerName) : JPARTY_JOIN2.replace("{player}", playerName);
	}
	public String getPartyLeave(String playerName, int n) {
		return n == 0 ? JPARTY_LEAVE.replace("{player}", playerName) : JPARTY_LEAVE2.replace("{player}", playerName);
	}
	public boolean chatGlobal(String u) {
		u = removeUnicode(u);
		return u.equalsIgnoreCase(JCHAT_TO_GLOBAL) || u.equalsIgnoreCase(JCHAT_TO_GLOBAL2);
		
	}
	public boolean chatParty(String u) {
		u = removeUnicode(u);
		return u.equalsIgnoreCase(JCHAT_TO_PARTY) || u.equalsIgnoreCase(JCHAT_TO_PARTY2);
	}
	public boolean chatAPD(String u) {
		u = removeUnicode(u);
		return u.equalsIgnoreCase(JAUTO_PARTY_DISBAND) || u.equalsIgnoreCase(JAUTO_PARTY_DISBAND2);
	}
	public boolean selfJoin(String u) {
		u = removeUnicode(u);
		return u.equalsIgnoreCase(getPartyJoin(name(), 0)) || u.equalsIgnoreCase(getPartyJoin(name(), 1));
	}
	public boolean selfLeave(String u) {
		u = removeUnicode(u);
		return u.equalsIgnoreCase(getPartyLeave(name(), 0)) || u.equalsIgnoreCase(getPartyLeave(name(), 1));
	}
	private String name() {
		return mc.thePlayer.getName();
	}
	public boolean checkIfPPD(String unformattedText) {
		try {
			unformattedText = removeUnicode(unformattedText);
			String splitted = "";
			String[] splitArray = unformattedText.split(" ");
			for(int i = 1; i < splitArray.length; i++) {
				String s = splitArray[i];
				splitted += s + " ";
			}
			splitted = splitted.substring(0, splitted.length() - 1);
			String splitted2 = "";
			String[] splitArray2 = JPLAYER_PARTY_DISBAND.split(" ");
			for(int i = 1; i < splitArray2.length; i++) {
				String s = splitArray2[i];
				splitted2 += s + " ";
			}
			splitted2 = splitted2.substring(0, splitted2.length() - 1);
			boolean sb = splitted.equalsIgnoreCase(splitted2);
			splitted = "";
			splitArray = unformattedText.split(" ");
			for(int i = 3; i < splitArray.length; i++) {
				String s = splitArray[i];
				splitted += s + " ";
			}
			splitted = splitted.substring(0, splitted.length() - 1);
			splitted2 = "";
			splitArray2 = JPLAYER_PARTY_DISBAND2.split(" ");
			for(int i = 3; i < splitArray2.length; i++) {
				String s = splitArray2[i];
				splitted2 += s + " ";
			}
			splitted2 = splitted2.substring(0, splitted2.length() - 1);
			
			/////////////////////////
			boolean o1 = sb;
			boolean o2 = splitted.equalsIgnoreCase(splitted2);
			return o1 || o2;
		}catch(Exception e1) {
			return false;
		}
	}
	
	/* END */
	public String removeUnicode(String str) {
		return str.replaceAll("[^\\x00-\\x7F]", "|");
	}
	public String replaceSpace(String str) {
		while(str.contains("  ")) str = str.replace("  ", " ");
		return str;
	}
	public List<IConfigElement> getConfigElements() {
		final List<IConfigElement> list = new ArrayList<>();
		config.getCategoryNames().forEach((s) ->
			config.getCategory(s).forEach((a, b) -> list.add(new ConfigElement(b))));
		new ArrayList<>(list).forEach((el) -> {
			if(!validConfigKeys.contains(el.getName().toLowerCase())) list.remove(el);
		});
		return list;
	}
	public void registerServers() {
		// Register servers here
		registerServer(JARTEX_URL.toString(), new Predicate<String>() {
			@Override
			public boolean test(String t) {
				t = replaceSpace(removeUnicode(t));
				return j_enabled && (JARTEX_REGEX.matcher(t).matches()) || ((j_practice) && JARTEX_PRAC.matcher(t).matches());
			}
		}, () -> ggMessage, () -> interval);
	}
	
	public <T> Object loadConfigKey(String category, String key, T defaultValue, String comment) {
		Object output = loadConfigKey(category, key, defaultValue, comment, "Unused");
		config.save();
		validConfigKeys.add(key.toLowerCase());
		return output;
	}
	private <A, T> Object loadConfigKey(String category, String key, T defaultValue, String comment, A... a) {
		if(defaultValue instanceof Boolean) return config.get(category, key, (Boolean)
				defaultValue, comment).getBoolean();
		else if(defaultValue instanceof boolean[]) return config.get(category, key, (boolean[])
				defaultValue, comment).getBooleanList();
		else if(defaultValue instanceof Double) return config.get(category, key, (Double)
				defaultValue, comment).getDouble(); 
		else if(defaultValue instanceof double[]) return config.get(category, key, (double[])
				defaultValue, comment).getDoubleList(); 
		else if(defaultValue instanceof Integer) return config.get(category, key, (Integer)
				defaultValue, comment).getInt(); 
		else if(defaultValue instanceof int[]) return config.get(category, key, (int[])
				defaultValue, comment).getIntList(); 
		else if(defaultValue instanceof String) return config.get(category, key, (String)
				defaultValue, comment).getString();
		else if(defaultValue instanceof String[]) return config.get(category, key, (String[])
				defaultValue, comment).getStringList();
		else throw new IllegalArgumentException("Default value can only be these types: boolean, double, int, and string. The only types allowed are arrays of them.");
	}
	/* The mod functions, above
	
	
	
	   How the mod works is down below
	*/
	// Final modifier cuz it cant be accessed and if it is it can just be easily modified through reflection
	private HashMap<String, Predicate<String>> predicates = new HashMap<>();
	private HashMap<String, Supplier<String>> ggMessages = new HashMap<>();
	private HashMap<String, Supplier<Integer>> intervals = new HashMap<>();
	private final Minecraft mc;
	public static OtherGG instance;
	{
		mc = Minecraft.getMinecraft();
		instance = this;
	}
	/**
	 * Store object using this class so 'final' modifier is accessible.
	 * @param <T> For compatibility with objects. So, it sets ONLY for that type and return THAT type.
	 */
	private class ObjectStore<T> {
		private T obj;
		private ObjectStore() { this(null); }
		private ObjectStore(T obj) {
			this.obj = obj;
		}
		public T get() {
			return obj;
		}
		public void set(T obj) {
			this.obj = obj;
		}
	}	
	
	void init() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	public String getGGMessage(String serverIpOrRegex) {
		if(!contains(ggMessages, serverIpOrRegex)) return getGG();
		else return get(ggMessages, serverIpOrRegex).get();
	}
	public void registerServer(String serverIpOrRegex, Predicate<String> winMessage) {
		registerServer(serverIpOrRegex, winMessage, null, () -> 0);
	}
	public void registerServer(String serverIpOrRegex, Predicate<String> winMessage,
			Supplier<String> ggMessage) {
		registerServer(serverIpOrRegex, winMessage, ggMessage, () -> 0);
	}
	
	// Modifies it if it exists
	// Server IP or regex is the server that will say GG automatically in. Could be regex or just text
	// Predicate winMessage returns boolean to check if its the correct win message
	// StringRepudiate ggMessage returns String text that will be said, null = default / "gg"
	// Interval is the time between the win message is sent and the message is sent in milliseconds
	public void registerServer(String serverIpOrRegex, Predicate<String> winMessage, 
			Supplier<String> ggMessage, Supplier<Integer> interval) {
		addServer(serverIpOrRegex, winMessage);
		if(ggMessage != null) setGGMessage(serverIpOrRegex, ggMessage);
		setIntervals(serverIpOrRegex, interval);
	}
	
	public void setIntervals(String serverIpOrRegex, Supplier<Integer> interval) {
		intervals = remove(intervals, serverIpOrRegex);
		intervals.put(serverIpOrRegex, interval);
	}
	
	public HashMap<String, Supplier<String>> getGGMessages() {
		return new HashMap<>(ggMessages);
	}
	public void setGGMessage(String serverIpOrRegex, Supplier<String> repudiate) {
		ggMessages = remove(ggMessages, serverIpOrRegex);
		ggMessages.put(serverIpOrRegex, repudiate);
	}
	public HashMap<String, Supplier<Integer>> getIntervalMap() {
		return new HashMap<>(intervals);
	}
	public int getIntervals(String serverIpOrRegex) {
		return intervals.getOrDefault(serverIpOrRegex, () -> 0).get().intValue();
	}
	public boolean isServer(String serverName) {
		return !mc.isSingleplayer() ? mc.getCurrentServerData().serverIP.equalsIgnoreCase(serverName) : false;
	}
	public HashMap<String, Predicate<String>> getPredicates() {
		return new HashMap<>(predicates);
	}
	public void addServer(String serverIpOrRegex, Predicate<String> predicate) {
		removeServer(serverIpOrRegex);
		predicates.put(serverIpOrRegex, predicate);
	}
	private <K> boolean contains(HashMap<String, K> h, String str) {
		final ObjectStore<Boolean> o = new ObjectStore<Boolean>();
		h.forEach((s, p) -> {
			if(str.equalsIgnoreCase(s)) {
				o.set(true);
				return;
			}
		});
		return o.get() == null ? false : o.get();
	}
	private <K> K get(HashMap<String, K> h, String str) {
		final ObjectStore<K> o = new ObjectStore<K>();
		h.forEach((s, p) -> {
			if(str.equalsIgnoreCase(s)) {
				o.set(p);
				return;
			}
		});
		return o.get();
	}
	private <K> HashMap<String, K> remove(HashMap<String, K> h, String str) {
		final List<String> o = new ArrayList<>();
		h.forEach((s, p) -> {
			if(str.equalsIgnoreCase(s)) {
				o.add(s);
			}
		});
		for(String s : o) {
			h.remove(str);
		}
		return h;
	}
	public void removeServer(String serverIpOrRegex) {
		predicates = remove(predicates, serverIpOrRegex);
	}
	
	private static boolean sayingGG = false;
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void otherGG(ClientChatReceivedEvent e) {
		try {
			String serverIP = mc.getCurrentServerData().serverIP;
			if(!OtherGG.sayingGG && OtherGG.modEnabled) for(Map.Entry<String, Predicate<String>> entry : predicates.entrySet()) {
				boolean equals = entry.getKey().equalsIgnoreCase(serverIP);
				boolean regex = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE).matcher(serverIP).matches();
				if(equals || regex) if(entry.getValue().test(e.message.getUnformattedText())) {
					int interval = getIntervals(entry.getKey());
					if(interval == 0) interval = this.interval;
					String ggMsg = getGGMessage(entry.getKey());
					System.out.println("Saying GG in " + interval + "ms");
					schedule(() -> {
						PlayerSayGGEvent event = new PlayerSayGGEvent(entry.getKey(), serverIP, ggMsg, e.message.getUnformattedText());
						if(!MinecraftForge.EVENT_BUS.post(event)) {
							System.out.println("Saying '" + event.getGGMessage() + "'");
							schedule(() -> {
								sendChat(event.getGGMessage());
							}, 250L);
							schedule(() -> {
								PlayerPostSayGGEvent post = new PlayerPostSayGGEvent(entry.getKey(), serverIP, ggMsg, e.message.getUnformattedText());
								MinecraftForge.EVENT_BUS.post(post);
							}, 500L);
						}else System.out.println("GG has been cancelled (by a third-party mod)!");
					}, interval);
					sayingGG = true;
					schedule(() -> {
						sayingGG = false;
						System.out.println("Cooldown of GG has expired!");
					}, 5000L + interval);
				}
			}
		}catch(Exception ignored) {}
	}
}
