package bramar.othergg;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TestCommand extends CommandBase {
	boolean print = false;
	TestCommand() { MinecraftForge.EVENT_BUS.register(this); }
	@SubscribeEvent
	public void onChatReceived(ClientChatReceivedEvent e) {
		if(print) System.out.println("\"" + e.message.getUnformattedText() + "\"");
	}
	@Override
	public String getCommandName() {
		return "tog";
	}
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Usage: /tog: OtherGG's test command";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		// Test
		print = !print;
		sender.addChatMessage(new ChatComponentText("It now " + (print ? "prints chat" : "doesn't print chat") + "!"));
	}
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
}
