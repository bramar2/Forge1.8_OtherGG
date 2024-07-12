package bramar.othergg;

import java.util.Locale;
import java.util.regex.Pattern;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
/**
 * Display and functionality of the Config GUI from the config button on mod list
 * @author bramar
 * @since 1.0
 */
public class ConfigGUI extends GuiConfig {
	private OtherGG o;
	public ConfigGUI(GuiScreen parent) {
		super(parent,
				OtherGG.instance.getConfigElements(),
				"othergg",
				false,
				false,
				"OtherGG Ingame Config");
		o = OtherGG.instance;
		titleLine2 = "Config Path: " + o.config.getConfigFile().getPath();
	}
	private Pattern CFG1 = Pattern.compile(
			"(ModEnabled|GoodGameMessage|Interval|Enabled|NewPartySystem|ChangeChat)"
			, Pattern.CASE_INSENSITIVE);
	public boolean getDefaultBoolean(String key) {
		if(key.contains("_")) key = key.split("\\_")[1];
		Boolean output = OtherGG.instance.defaultBools.get(key);
		if(output != null) return output;
		if(CFG1.matcher(key).matches()) return true;
		return false;
	}
	public int getDefaultInt(String key) {
		if(key.contains("_")) key = key.split("\\_")[1];
		if(key.equalsIgnoreCase("WebTimeout")) return 60000;
		return 0;
	}
	public String getDefaultString(String key) {
		if(key.contains("_")) key = key.split("\\_")[1];
		if(key.equalsIgnoreCase("GoodGameMessage")) return "gg";
		return "";
	}
	public String getCategory(String key) {
		if(key.toLowerCase().startsWith("j_")) return "jartexnetwork";
		if(key.toLowerCase().startsWith("h_")) return "hypixel";
		if(key.toLowerCase().startsWith("mp_")) return "mineplex";
		String s = OtherGG.instance.scToCategory.get(key);
		if(s != null) return s;
		for(String category : o.config.getCategoryNames()) {
			for(IConfigElement element : new ConfigElement(o.config.getCategory(category)).getChildElements()) {
				if(element.getName().equalsIgnoreCase(key)) return category.toLowerCase(Locale.ENGLISH);
			}
		}
		return "general";
	}
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		// Save
		for(IConfigElement element : configElements) {
			Object val = element.get();
			if(val instanceof Double) o.config.get(getCategory(element.getName()), element.getName(), 0.0D).set((Double) val);
			else if(val instanceof double[]) o.config.get(getCategory(element.getName()), element.getName(), new double[] {}).set((double[]) val);
			else if(val instanceof Boolean) o.config.get(getCategory(element.getName()), element.getName(), getDefaultBoolean(element.getName())).set((Boolean) val);
			else if(val instanceof boolean[]) o.config.get(getCategory(element.getName()), element.getName(), new boolean[] {}).set((boolean[]) val);
			else if(val instanceof Integer) o.config.get(getCategory(element.getName()), element.getName(), getDefaultInt(element.getName())).set((Integer) val);
			else if(val instanceof int[]) o.config.get(getCategory(element.getName()), element.getName(), new int[] {}).set((int[]) val);
			else if(val instanceof String[]) o.config.get(getCategory(element.getName()), element.getName(), new String[] {}).set((String[]) val);
			else o.config.get(getCategory(element.getName()), element.getName(), getDefaultString(element.getName())).set(val + "");
			
			o.config.save();
			o.loadConfig();
		}
	}
}
