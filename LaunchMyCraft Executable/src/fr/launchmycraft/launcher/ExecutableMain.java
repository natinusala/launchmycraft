package fr.launchmycraft.launcher;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.launchmycraft.launcher.DialogFactory.DialogType;
import fr.launchmycraft.library.GetResult;
import fr.launchmycraft.library.util.Util;
import fr.launchmycraft.library.util.network.HTTPDownloader;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;

public class ExecutableMain 
{
	public static boolean hasPaid;
	public static String identifier;

	private static final long DEFAULT_ID = 1;

	static boolean noBootstrap = false;
	public static JFrame frame;
	public static long launcherId;

	public static HashMap<String, String> launcherDetails; 

	public boolean forceUpdate;

	public static ConsoleFrame consoleFrame; //TODO A changer par du JavaFX propre

	String newsUrl = "http://mcupdate.tumblr.com/";
	
	private ExecutableMain executableMain = this;

	//TODO Entrée pour valider le formulaire

	public static void main(String[] args) throws IOException, JsonSyntaxException, URISyntaxException
	{		
		Platform.setImplicitExit(false);
		
		DialogFactory.createDialog(null, DialogType.INFO, "Aucun bootstrap détecté, lancement du launcher #" + DEFAULT_ID + " dans une nouvelle fenêtre.").setVisible(true);
		
		JFrame frame = new JFrame("Minecraft");       
		frame.setBackground(Color.DARK_GRAY);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(850, 550);
		frame.setResizable(false);

		frame.setVisible(true);

		noBootstrap = true;

		new ExecutableMain(Integer.MAX_VALUE, frame, DEFAULT_ID, false, "");
	}

	//Constructeur normal
	public ExecutableMain(long bootstrapVersion, JFrame frame, long id,  boolean hasPaid, String identifier)
	{
		Platform.setImplicitExit(false);
		
		ExecutableMain.frame = frame;
		ExecutableMain.launcherId = id;
		ExecutableMain.hasPaid = hasPaid;
		ExecutableMain.identifier = identifier;

		//Exécution du worker
		new Worker(bootstrapVersion).start();
	}

	//Constructeur legacy
	public ExecutableMain(JFrame frame, long id, String newsUrl, long version, boolean hasPaid, String identifier)
	{
		Platform.setImplicitExit(false);
		
		ExecutableMain.launcherId = id;

		//On met du bidon parce que c'est final
		ExecutableMain.hasPaid = hasPaid;
		ExecutableMain.identifier = identifier;

		//Exécution du worker sans version pour qu'il le mette à jour direct
		new Worker().start();
	}

	public class Worker extends Thread
	{
		long bootstrapVersion = 0;

		public Worker()
		{}

		public Worker(long bversion)
		{
			bootstrapVersion = bversion;
		}

		@Override
		public void run()
		{
			try
			{
				//Récupération de la dernière version
				long lastBootstrapVersion = Long.parseLong(Util.doGET(Util.getLastBootstrapVersionUrl(), null));

				if (!noBootstrap && lastBootstrapVersion != bootstrapVersion)
				{
					//Mise à jour du bootstrap		
					File bootstrapFile = Util.getBootstrapFile();

					if (bootstrapFile.isFile())
					{
						//On télécharge l'updater
						File updaterFile = File.createTempFile("LMCupdater" + Math.random(), ".tmp");
						new HTTPDownloader("https://launchmycraft.fr/getlauncher/bootstrapupdater", updaterFile).downloadFile();

						//On le lance
						new ProcessBuilder(OperatingSystem.getCurrentPlatform().getJavaDir(), "-jar", updaterFile.getAbsolutePath(), "--launcherid", Long.toString(launcherId), "--bootstrapfile", bootstrapFile.getAbsolutePath()).start();

						//On se kille
						System.exit(0);
					}
				}

				//Chargement des données du launcher
				GetResult result = new Gson().fromJson(Util.doGET(Util.getLauncherGetUrl(launcherId), null), GetResult.class);
				launcherDetails = result.data;

				if (result.error == true)
				{
					die(result.message);
					return;
				}	

				//On init le LoggerUtils
				LoggerUtils.init(launcherId, hasPaid, identifier);

				//Les logs !
				String debug = launcherDetails.containsKey("debug") ? " (debug)" : "";
				LoggerUtils.println("--- LOGS DU LAUNCHER #" + launcherId + " - " + new SimpleDateFormat().format(new Date()) + debug + " ---\n");

				//Frame title
				if (launcherDetails.containsKey("servername"))
				{
					frame.setTitle("Minecraft - " + launcherDetails.get("servername"));
				}

				//La newsframe			
				//On met le remote en priorité
				if (launcherDetails.containsKey("websiteurl"))
				{
					newsUrl = launcherDetails.get("websiteurl");				

					//On rajoute http si y'a pas
					if (!newsUrl.startsWith("http://") && !newsUrl.startsWith("https://"))
					{
						newsUrl = "http://" + newsUrl;
					}
				}

				LoggerUtils.println("Page des news : " + newsUrl);

				//Interface
				JFXPanel jfxPanel = new JFXPanel();	
				Platform.runLater(new Runnable()
				{
					@Override
					public void run() 
					{
						try
						{
							frame.setContentPane(jfxPanel);

							Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("theme/launcher.fxml"));	
							Scene scene = new Scene(root);
							jfxPanel.setScene(scene);

							//Webnews
							setupNews(launcherDetails.containsKey("openbrowser"), (WebView) scene.lookup("#newsWebView"), newsUrl);

							//Logo
							setupLogo((ImageView) scene.lookup("#serverLogoImageView"));
							
							//Creditshyperlink
							Hyperlink creditsLink = (Hyperlink)scene.lookup("#creditsHyperlink");
							creditsLink.setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent arg0) 
								{
									try
									{
										OperatingSystem.openLink(new URI("https://www.launchmycraft.fr/"));							
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								}							
							});
							
							if (hasPaid)
							{
								creditsLink.setVisible(false);
							}
							
							//Bouton des options
							((Button) scene.lookup("#optionsButton")).setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent event) 
								{
									showOptions();		
								}							
							});
							
							//Formulaire de connexion
							if (Util.isCrackedAllowed())
			                {
			                	((PasswordField) scene.lookup("#passwordField")).setPromptText("Mot de passe (facultatif)");
			                }

							//Pack
							frame.pack();
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
							die(ex.getLocalizedMessage());
						}

					}
				});
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				die(ex.getLocalizedMessage());
			}	
		}
	}

	void die(String message)
	{
		DialogFactory.createDialog(ExecutableMain.frame, DialogType.ERROR, message).setVisible(true);;
		System.exit(0);
	}
	
	void showOptions() //TODO Changer ça pour les optiosn en JavaFX
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
            public void run() 
            {       		
        		try 
        		{
        			JDialog optionsDialog = new JDialog(frame, "Options", true);
					optionsDialog.add(new OptionsPanel(executableMain));
	        		optionsDialog.setResizable(false);
	        		optionsDialog.pack();
	        		optionsDialog.setLocationRelativeTo(frame);
	        		optionsDialog.setVisible(true);
				} 
        		catch (Exception ex) 
				{
					ex.printStackTrace();
					DialogFactory.createDialog(ExecutableMain.frame, DialogType.ERROR, "Impossible de charger les options (" + ex.getLocalizedMessage() + ").");
				}
            }
        });
	}

	void setupLogo(ImageView view)
	{
		if (ExecutableMain.launcherDetails.containsKey("serverlogo"))
		{
			view.setImage(new Image(ExecutableMain.launcherDetails.get("serverlogo")));
		}
		else
		{
			if (ExecutableMain.launcherDetails.containsKey("servername"))
			{
				view.setImage(new Image(Util.getLogoTextUrl(ExecutableMain.launcherDetails.get("servername"))));
			}
			else
			{
				view.setImage(new Image(Util.getDefaultLogoUrl()));
			}
		}			
	}

	void setupNews(boolean externalBrowser, WebView view, String url)
	{
		view.getEngine().load(url);

		if (externalBrowser)
		{
			view.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() 
			{
				@Override
				public void changed(ObservableValue<? extends State> observable, final State oldValue, State newValue)
				{
					if (newValue.equals(State.SUCCEEDED))
					{
						NodeList nodeList = view.getEngine().getDocument().getElementsByTagName("a");
						for (int i = 0; i < nodeList.getLength(); i++)
						{
							Node node = nodeList.item(i);
							EventTarget eventTarget = (EventTarget) node;
							eventTarget.addEventListener("click", new EventListener()
							{
								@Override
								public void handleEvent(Event evt)
								{
									try
									{
										EventTarget target = evt.getCurrentTarget();
										HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
										String href = anchorElement.getHref();		                   
										OperatingSystem.openLink(new URI(href));
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}

									evt.preventDefault();
								}
							}, false);
						}
					}
				}
			});
		}

		view.getStylesheets().add(getClass().getClassLoader().getResource("theme/jmetrolight.css").toExternalForm());
	}
}
