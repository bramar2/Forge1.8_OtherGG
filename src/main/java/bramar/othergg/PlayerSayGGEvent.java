package bramar.othergg;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * When the player says GG (after interval, if exists)<br>
 * Cancellable. If cancelled, it wouldn't say the message
 * 
 * @author bramar
 * @since 1.0
 * @see PlayerPostSayGGEvent
 */
@Cancelable
public class PlayerSayGGEvent extends Event {
	private String ggMessage;
	private String serverIpOrRegex;
	private String winMessageFull;
	private String serverIP;
	public PlayerSayGGEvent(String serverIpOrRegex, String serverIP, String ggMessage, String winMessageFull) {
		this.serverIpOrRegex = serverIpOrRegex;
		this.ggMessage = ggMessage;
		this.winMessageFull = winMessageFull;
		this.serverIP = serverIP;
	}
	/**
	 * Whether or not this is cancelled.
	 * @return Whether or not the event was cancelled
	 * @see #setCanceled(boolean)
	 */
	@Override
	public boolean isCanceled() { return super.isCanceled(); }
	/**
	 * The event is cancellable, no point checking
	 * @return True
	 */
	@Override
	public boolean isCancelable() { return true; }
	/**
	 * Whether or not the mod continue sending the GG message
	 * @param cancel The new cancelled boolean (true if cancelled, false if not)
	 */
	@Override
	public void setCanceled(boolean cancel) { super.setCanceled(cancel); }
	/**
	 * Gets the server IP in minecraft (not regex)
	 * @return The server ip
	 */
	public String getMCServerIP() {
		return serverIP;
	}
	/**
	 * Gets the server IP or regex INPUTTED internally
	 * @return The server ip/regex
	 */
	public String getServerIpOrRegex() {
		return serverIpOrRegex;
	}
	/**
	 * Gets the current GG message
	 * @return GG message
	 */
	public String getGGMessage() {
		return ggMessage;
	}
	/**
	 * Sets the GG message
	 * @param newGGMessage The new GG message sent
	 */
	public void setGGMessage(String newGGMessage) {
		this.ggMessage = newGGMessage;
	}
	/**
	 * Gets the win message that was detected
	 * @return The win message
	 */
	public String getWinMessage() {
		return winMessageFull;
	}
}
