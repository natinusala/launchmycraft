package fr.launchmycraft.launcher.bootstrap;

/**
 *
 * @author Natinusala
 */
public class Constants {
	
	private static final boolean DEBUG = false;
    
    public static String LAUNCHER_MD5_URL()
    {
    	if (DEBUG)
    	{
    		return "http://127.0.0.1:8082/getlauncher/launchermd5";
    	}
    	
    	return "https://launchmycraft.fr/getlauncher/launchermd5";
    }
    
    public static String LAUNCHER_FILE_URL()
    {
    	if (DEBUG)
    	{
    		return "http://127.0.0.1:8082/getlauncher/launcherfile";
    	}
    	
    	return "https://launchmycraft.fr/getlauncher/launcherfile";
    }
    
    public static String LAUNCHER_MAIN_CLASS = "fr.launchmycraft.launcher.ExecutableMain";
    
    public static long VERSION = 9; 
    
}
