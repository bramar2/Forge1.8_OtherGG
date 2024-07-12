package bramar.othergg;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

/**
 * For config button in mod
 * @author bramar
 * @since 1.0
 * @see ConfigGUI
 */
public class Factory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft mc) {
		
	}
	
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ConfigGUI.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

}
