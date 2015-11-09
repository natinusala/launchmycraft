package fr.launchmycraft.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.google.gson.JsonSyntaxException;

import fr.launchmycraft.library.util.Util;

public class LoggerUtils 
{
	public static void init(long launcherId, boolean hasPaid, String identifier) throws JsonSyntaxException, IOException
	{		
		//On set le PrintStream
		File logFile = Util.getLogFile();
		
		if (logFile.exists())
		{
			logFile.delete();
		}
		
		logFile.getParentFile().mkdirs();
		logFile.createNewFile();
		
		FileOutputStream logFileStream = new FileOutputStream(logFile);
		LauncherPrintStream out = new LauncherPrintStream(logFileStream, System.out);
		System.setOut(out);
		System.setErr(out);	
			
		//On set la consoleframe
		ExecutableMain.consoleFrame = new ConsoleFrame();
	}
	
	public static void println(String line)
	{
	    System.out.println(line);
		ConsoleFrame.writeLine(line);
	}
	
	public static void print(String text)
	{
		System.out.print(text);
		ConsoleFrame.write(text);
	}
	
	public static void openSavePrompt() throws JsonSyntaxException, IOException
	{
		final JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Enregistrer les logs");
		chooser.setSelectedFile(new File("launcher" + ExecutableMain.launcherId + ".log"));
		
		FileFilter filter = new FileFilter(){
			@Override
			public boolean accept(File f) 
			{
				return f.getName().endsWith(".log");
			}

			@Override
			public String getDescription() 
			{
				return "Fichier LOG";
			}
		};
		
		chooser.setFileFilter(filter);
		
		int chooserResult = chooser.showSaveDialog(null);
		if (chooserResult == JFileChooser.APPROVE_OPTION)
		{
			File logFile = Util.getLogFile();
			if (chooser.getSelectedFile().exists())
			{
				chooser.getSelectedFile().delete();
			}
			Files.copy(logFile.toPath(), chooser.getSelectedFile().toPath());
			LoggerUtils.println("### Enregistrement des logs dans " + chooser.getSelectedFile().getAbsolutePath());
			JOptionPane.showMessageDialog(null, "Logs enregistrés ! Ouvrez le fichier créé pour les lire.", "Logs enregistrés", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
