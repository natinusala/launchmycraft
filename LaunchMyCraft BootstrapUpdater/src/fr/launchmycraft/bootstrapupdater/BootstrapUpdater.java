package fr.launchmycraft.bootstrapupdater;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class BootstrapUpdater 
{
	static OptionSet options;
	static String[] args;
	public static void main(String[] args) throws Exception
	{
		BootstrapUpdater.args = args;
		
		JFrame frame = new JFrame();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(450, 75);
		frame.setTitle("Mise à jour du launcher");
		frame.setContentPane(new JLabel("   Patientez durant la mise à jour de votre launcher..."));
		frame.setVisible(true);
				
		new Worker().run();
	}
	
	static class Worker extends Thread
	{				
		@Override
		public void run()
		{			
			try
			{
				OptionParser optionsParser = new OptionParser();
				optionsParser.accepts("bootstrapfile").withRequiredArg().ofType(String.class);
				optionsParser.accepts("launcherid").withRequiredArg().ofType(String.class);
				
				options = optionsParser.parse(args);
				if (!options.has("bootstrapfile") || !options.has("launcherid"))
				{
					throw new Exception("Missing arguments");
				}

				File bootstrapFile = new File((String) options.valueOf("bootstrapfile"));
				long launcherId = Long.parseLong((String) options.valueOf("launcherid"));
				boolean exe = bootstrapFile.getName().endsWith(".exe");
								
				new HTTPDownloader("https://launchmycraft.fr/getlauncher/" + launcherId + "/executable/" + ((exe == true) ? "exe" : "jar"), bootstrapFile).downloadFile();
				
				//Lancement du nouveau bootstrap
				ProcessBuilder process;
				
				if (exe)
				{
					process = new ProcessBuilder(bootstrapFile.getAbsolutePath());
				}
				else
				{
					process = new ProcessBuilder(OperatingSystem.getCurrentPlatform().getJavaDir(), "-jar", bootstrapFile.getAbsolutePath());
				}
				
				process.start();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Impossible de mettre à jour le launcher (" + ex.getLocalizedMessage() + ") ; réessayez de le lancer, sinon supprimez-le et retéléchargez-le.", "Erreur", JOptionPane.ERROR_MESSAGE);
			}
					
			//On se ferme
			System.exit(0);
		}
	}
}
