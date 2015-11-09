package fr.launchmycraft.library.util.listeners;

public interface StateListener 
{
	public void onStateChanged(boolean failed, String text, boolean onLogin);
	
	public void onStateChanged(boolean failed, String text);
}
