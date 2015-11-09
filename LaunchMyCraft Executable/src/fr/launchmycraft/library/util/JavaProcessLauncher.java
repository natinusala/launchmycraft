package fr.launchmycraft.library.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Natinusala
 */

public class JavaProcessLauncher {
	
	String executable;
	File workingDir;
	
	ProcessBuilder launcher;
	
	public String getCommandLine()
	{
		String c  = "";
		for (String s : launcher.command())
		{
			c += " ";
			c += s;
		}
		
		return c;
	}
	
	public Map<String, String> getEnvironment()
	{
		return launcher.environment();
	}
	
    public JavaProcessLauncher(String exec, File workingDirectory)
    {
    	executable = exec;
    	workingDir = workingDirectory;
    	
    	launcher = new ProcessBuilder(executable);
    	
    	launcher.directory(workingDirectory);
    	
    	launcher.redirectErrorStream(true);
    }
    
    public void addCommand(String[] command)
    {
    	launcher.command().addAll(Arrays.asList(command));
    }
    
    public void addSplitCommand(String command)
    {
    	addCommand(command.split(" "));
    }
    
    public Process start() throws IOException
    {
    	return launcher.start();
    }
}
