package bramar.othergg;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * When the player ALREADY says GG<br>
 * Not cancellable
 * 
 * @author bramar
 * @since 1.0
 * @see PlayerSayGGEvent
 */
public class PlayerPostSayGGEvent extends Event {
	private String ggMessage;
	private String serverIpOrRegex;
	private String winMessageFull;
	private String serverIP;
	public PlayerPostSayGGEvent(String serverIpOrRegex, String serverIP, String ggMessage, String winMessageFull) {
		this.serverIpOrRegex = serverIpOrRegex;
		this.ggMessage = ggMessage;
		this.winMessageFull = winMessageFull;
		this.serverIP = serverIP;
	}
	public String getMCServerIP() {
		return serverIP;
	}
	public String getServerIpOrRegex() {
		return serverIpOrRegex;
	}
	public String getGGMessage() {
		return ggMessage;
	}
	public String getWinMessage() {
		return winMessageFull;
	}
	@Override
	public boolean isCancelable() {
		return false;
	}
}
