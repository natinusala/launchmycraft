/*
package fr.launchmycraft.launcher;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.launchmycraft.library.GameLauncher;
import fr.launchmycraft.library.GetResult;
import fr.launchmycraft.library.authentication.Account;
import fr.launchmycraft.library.authentication.Credentials;
import fr.launchmycraft.library.authentication.LoginProfile;
import fr.launchmycraft.library.util.CustomFocusTraversalPolicy;
import fr.launchmycraft.library.util.Util;
import fr.launchmycraft.library.util.listeners.FileProgressionListener;
import fr.launchmycraft.library.util.listeners.GameLaunchedListener;
import fr.launchmycraft.library.util.listeners.StateListener;
import fr.launchmycraft.library.util.network.HTTPDownloader;
import fr.launchmycraft.library.Configuration;

public class ExecutableMain {
	
	public static boolean hasPaid;
	public static String identifier;
	
	private static final long DEFAULT_ID = 1;
	
	public static void main(String[] args) throws IOException, JsonSyntaxException, URISyntaxException
	{		
		JOptionPane.showMessageDialog(null, "Aucun bootstrap détecté, lancement du launcher #" + DEFAULT_ID + " dans une nouvelle fenêtre");
		
		JFrame frame = new JFrame("Minecraft");       
        frame.setBackground(Color.DARK_GRAY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 550);
        frame.setResizable(false);
        
        frame.setVisible(true);
        
        noBootstrap = true;
		
		new ExecutableMain(Integer.MAX_VALUE, frame, DEFAULT_ID, false, "");
	}
	
	//Instance de la consoleframe
	public static ConsoleFrame consoleFrame;
	
	ExecutableMain executableMain = this;
	
	public static JFrame frame;
	public static long launcherId;
	String newsUrl = "http://mcupdate.tumblr.com/";
	
	Account account;
	
	public static HashMap<String, String> launcherDetails;
	
	static boolean noBootstrap = false;
	
	//Constructeur normal
	public ExecutableMain(long bootstrapVersion, JFrame frame, long id,  boolean hasPaid, String identifier) throws JsonSyntaxException, FileNotFoundException, IOException, URISyntaxException
	{
		ExecutableMain.frame = frame;
		ExecutableMain.launcherId = id;
		ExecutableMain.hasPaid = hasPaid;
		ExecutableMain.identifier = identifier;
				
		//Exécution du worker
		new Worker(bootstrapVersion).start();
	}
	
	//Constructeur legacy
	public ExecutableMain(JFrame frame, long id, String newsUrl, long version, boolean hasPaid, String identifier) throws URISyntaxException
	{
		ExecutableMain.launcherId = id;
		
		//On met du bidon parce que c'est final
		ExecutableMain.hasPaid = hasPaid;
		ExecutableMain.identifier = identifier;
		
		//ExÃ©cution du worker sans version pour qu'il le mette à jour direct
	    new Worker().start();
	}
	
	JPanel loginForm;
	JPanel loadingForm;
	
	JPanel loginPanel;
	CardLayout loginCardLayout;
	
	WhiteJLabel loadingText;
	JProgressBar loadingBar;
	
	JPanel bottomPanel;
	
	JLabel errorLabel;
	
	JPasswordField passwordField;
	JTextField usernameField;
	
	JButton optionsButton;
	JPanel optionsPanel;
	
	JButton loginButton;
	
	public boolean forceUpdate;
	
	ActionListener loginListener = new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (loginButton.isEnabled())
			{
				doLogin();
			}
		}
    	
    };
    
    DocumentListener textListener = new DocumentListener(){
		@Override
		public void changedUpdate(DocumentEvent arg0) {}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
            Util.computeLoginButtonAvaibility(loginButton, usernameField, passwordField);
			
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
            Util.computeLoginButtonAvaibility(loginButton, usernameField, passwordField);
		}
    	
    };
    
    void die(String message)
    {
    	JOptionPane.showMessageDialog(null, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    	System.exit(0);
    }
	
	class Worker extends Thread
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
				
				//Chargement des donnÃ©es du launcher
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
							
				//Interface
				final JPanel contentPane = new JPanel(new BorderLayout());
				contentPane.setOpaque(true);		
				
				if (launcherDetails.containsKey("servername"))
				{
					frame.setTitle("Minecraft - " + launcherDetails.get("servername"));
				}
													
				//La newsframe			
				//On met le remote en prioritÃ©
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
				
				WebsiteTab scrollPane = new WebsiteTab(newsUrl, launcherDetails.containsKey("openbrowser"));
				contentPane.add(scrollPane, BorderLayout.CENTER);
								
				//Le cadre en bas
				bottomPanel = new TexturedPanel(Util.getThemeImage(launcherDetails.get("theme")));
				bottomPanel.setOpaque(true);
				bottomPanel.setLayout(new BorderLayout());
				bottomPanel.setBorder(new EmptyBorder( 12, 40, 12, 20 ) );
				contentPane.add(bottomPanel, BorderLayout.SOUTH);		
				
				//ErrorLabel
				errorLabel = new JLabel("", JLabel.RIGHT);
				errorLabel.setForeground(Color.RED);
				errorLabel.setVisible(false);
				bottomPanel.add(errorLabel, BorderLayout.NORTH);
				
				//Le logo du serveur		
                bottomPanel.add(Util.getLogoJLabel(true), BorderLayout.WEST); 
                
                loginCardLayout = new CardLayout();
                loginPanel = new JPanel(loginCardLayout);
                loginPanel.setOpaque(false);
                bottomPanel.add(loginPanel, BorderLayout.EAST);
                
                //Le formulaire
                loginForm = new JPanel();
                loginForm.setLayout(new GridBagLayout());
                loginForm.setOpaque(false);
                loginPanel.add(loginForm, "login");
                
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(2, 4, 2, 4);
               
                //c.ipadx = 125;
                
                c.gridx = 0;
                c.gridy = 0;
                WhiteJLabel usernameLabel = new WhiteJLabel("Nom d'utilisateur : ", JLabel.RIGHT);
                loginForm.add(usernameLabel, c);
                
                c.gridx = 1;
                c.gridy = 0;
                usernameField = new JTextField();               
                usernameField.getDocument().addDocumentListener(textListener);
                usernameField.addActionListener(loginListener);
                usernameField.setPreferredSize(new Dimension(150, 20));
                JPanel usernamePanel = new JPanel();
                usernamePanel.setOpaque(false);
                usernamePanel.add(usernameField);
                loginForm.add(usernamePanel, c);
                
                c.gridx = 0;
                c.gridy = 1;
                WhiteJLabel passwordLabel = new WhiteJLabel("Mot de passe : ", JLabel.RIGHT);
                
                if (Util.isCrackedAllowed())
                {
                	passwordLabel.setText("Mot de passe (facultatif) : ");
                }
                
                loginForm.add(passwordLabel, c);
                
                //c.ipadx = 0;
                
                c.gridx = 1;
                c.gridy = 1;
                passwordField = new JPasswordField();
                passwordField.getDocument().addDocumentListener(textListener);
                passwordField.addActionListener(loginListener);
                passwordField.setPreferredSize(new Dimension(150, 20));
                JPanel passwordPanel = new JPanel();            
                passwordPanel.setOpaque(false);
                passwordPanel.add(passwordField);
                loginForm.add(passwordPanel, c);
                
                
                c.gridy = 0;
                c.gridx = 2;
                optionsButton = new JButton("Options");
                optionsButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						showOptions();			
					}               	
                });
                loginForm.add(optionsButton, c);
                
                c.gridy = 1;
                c.gridx = 2;
                loginButton = new JButton("Se connecter");
                if (launcherDetails.containsKey("hidepassword"))
                {
                	loginButton.setText("Jouer");
                }
                loginButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						doLogin();					
					}              	
                });
                loginForm.add(loginButton, c);
                
                c.gridx = 1;
                c.gridy = 2;
                                
                JCredits credits = new JCredits("Proposé par LaunchMyCraft.fr", "https://www.launchmycraft.fr/");
                credits.setHorizontalAlignment(JTextField.RIGHT);
                if (!hasPaid)
                {
                    bottomPanel.add(credits, BorderLayout.SOUTH);
                }
                
                //Le loadingForm
                loadingText = new WhiteJLabel("Chargement en cours...", JLabel.RIGHT);            
                loadingBar = new JProgressBar();
                
                loadingForm = new JPanel();
                loadingForm.setLayout(new BoxLayout(loadingForm, BoxLayout.Y_AXIS));
                loadingForm.setOpaque(false);      
                
                loadingText.setAlignmentX(Component.RIGHT_ALIGNMENT);
                
                loadingForm.add(Box.createVerticalGlue());
                loadingForm.add(loadingText);
                loadingForm.add(loadingBar);
                loadingForm.add(Box.createVerticalGlue());
                
                loginPanel.add(loadingForm, "loading");   
                
                //Option pour cacher le mot de passe
                if (launcherDetails.containsKey("hidepassword"))
                {
                	passwordField.setVisible(false);
                	passwordLabel.setVisible(false);
                }
                
                //StoredCredentials
                Credentials storedCredentials = Util.getStoredCredentials();
                
                //On regarde si c'est rien
                if (storedCredentials == null)
                {
                	//On affiche le formulaire
                	loginCardLayout.show(loginPanel, "login");
                }
                //On regarde si c'est crackÃ©
                else if (storedCredentials.accesstoken == null || storedCredentials.profilename == null || storedCredentials.accesstoken.equals("") || storedCredentials.clienttoken.equals(""))
                {
                	//On met l'username
                	usernameField.setText(storedCredentials.username);
                	
                	//On affiche le formulaire
                	loginCardLayout.show(loginPanel, "login");
                }
                else
                {
                	//On vÃ©rifie l'accesstoken
                	String newToken = Util.refreshCredentials(storedCredentials);
                	if (newToken != null)
                	{              	
                    	//Si c'est bon
                		//On sauvegarde le nouveau token
                		storedCredentials.accesstoken = newToken;
                		Util.setStoredCredentials(storedCredentials);
                		
                    	//On construit l'account
                		LoginProfile profile = new LoginProfile();
                		profile.name = storedCredentials.profilename;
                		profile.id = storedCredentials.profileid;
                		account = new Account(storedCredentials.username, storedCredentials.accesstoken, storedCredentials.clienttoken, profile, launcherDetails);
                    	
                		//On fait et on affiche le menu
                		final JPanel playPanel = new JPanel(new GridBagLayout());
                		GridBagConstraints co = new GridBagConstraints();
                		co.insets = new Insets(5, 5, 5, 25);
                		co.fill = GridBagConstraints.NONE;
                		co.anchor = GridBagConstraints.EAST;
                		co.weightx = 1.0f;
                		playPanel.setOpaque(false);
                		//On prend l'avatar
                		JLabel avatar = new JLabel();
                		Util.setJLabelLogo(avatar, Util.getAvatarUrl(storedCredentials.profilename, 72), false);
                		co.gridx = 0;
                		co.gridy = 0;
                		playPanel.add(avatar, co);
                		
                		//Le texte
                		JLabel playername = new JLabel(storedCredentials.profilename);
                		playername.setForeground(Color.WHITE);
                		playername.setFont(playername.getFont().deriveFont(14.0f));
                		co.gridx = 0; 
                		co.gridy = 1;
                		playPanel.add(playername, co);
                		
                		//Seconde colonne
                		co.weightx = 0f;
                		co.anchor = GridBagConstraints.CENTER;
                		
                		//Le bouton pour se connecter
                		JButton play = new JButton("Jouer maintenant");
                		play.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent arg0) {
								doLogin();								
							}});
                		co.ipady = 25;
                		co.gridx = 1;
                		co.gridy = 0;               		
                		playPanel.add(play, co);
                		
                		//Le bouton pour changer de compte
                		JButton logout = new JButton("Se déconnecter");
                		logout.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent e) {
								try
								{
									//On supprime le compte
									Util.setStoredCredentials(new Credentials());
									account = null;
									
									//On affiche le truc
									loginCardLayout.show(loginPanel, "login");
								}
								catch(Exception ex)
								{ex.printStackTrace();}								
							}
                			
                		});
                		co.gridx = 1;
                		co.gridy = 1;
                		co.ipady = 0;
                		playPanel.add(logout, co);
                		            		
                		loginPanel.add(playPanel, "play");
                		loginCardLayout.show(loginPanel, "play");
                	}
                	else
                	{                    	
                    	//Sinon on affiche le formulaire avec un username
                    	usernameField.setText(storedCredentials.username);
                    	loginCardLayout.show(loginPanel, "login");
                	}
                }
                
                frame.setContentPane(contentPane);
                frame.pack();
                frame.setSize(850, 550);
                
                //Focus order
                Vector<Component> order = new Vector<Component>(2);
                order.add(usernameField);
                order.add(passwordField);
                CustomFocusTraversalPolicy newPolicy = new CustomFocusTraversalPolicy(order);
                frame.setFocusTraversalPolicy(newPolicy);

                //On compute le bouton
                Util.computeLoginButtonAvaibility(loginButton, usernameField, passwordField);
			
                //News
				//scrollPane.setPage(newsUrl);
				
				LoggerUtils.println("### Chargement terminé ; affichage de l'interface");		
				
				//Vérification des CGU
				Configuration config = Util.getConfiguration();
				int latestCgu = Util.getLatestCGURevision();
				if (config.cguRevision < latestCgu)
				{
					//Affichage du message
					JPanel panel = new JPanel(new BorderLayout());
					JTextPane pane = new JTextPane();
					pane.setOpaque(false);
					pane.setText("Depuis votre dernière visite, les Conditions générales d'utilisation du service ont changées ; vous devez donc lire et accepter la dernière version, accessible depuis le bouton ci-dessous.\nEn cliquant sur le bouton d'acceptation, vous reconnaissez avoir lu et accepté les Conditions générales d'utilisation du service.");
					pane.setPreferredSize(new Dimension(600, 75));
					panel.add(pane, BorderLayout.NORTH);
					JButton cguButton = new JButton("Lire les CGU");					
					cguButton.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent arg0) 
						{
							try 
							{
								OperatingSystem.openLink(new URL("https://www.launchmycraft.fr/cgu").toURI());
							} 
							catch (Exception e) 
							{
								e.printStackTrace();
							}							
						}					
					});
					panel.add(cguButton, BorderLayout.LINE_START);					
					int choice = JOptionPane.showOptionDialog(null, panel, "Conditions générales d'utilisation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Accepter et continuer", "Refuser et fermer le launcher"}, "Accepter et continuer");
					if (choice == 1 || choice == -1)
					{
						System.exit(0);
					}
					else
					{
						config.cguRevision = latestCgu;
						Util.saveConfiguration(config);
					}
				}
				
				//Affichage des infos debug
				if (launcherDetails.containsKey("debug"))
				{
					LoggerUtils.println("[DEBUG] OperatingSystem.getCurrentPlatform().getName() : " + OperatingSystem.getCurrentPlatform().getName());
					LoggerUtils.println("[DEBUG] System.getProperty(\"user.home\", \".\") : " + System.getProperty("user.home", "."));
					LoggerUtils.println("[DEBUG] OperatingSystem.getBaseWorkingDirectory(); : " + OperatingSystem.getBaseWorkingDirectory(hasPaid, identifier).getAbsolutePath());
				}
				
			}
			catch (Exception ex)
			{
				//On fail
				ex.printStackTrace();
				die(ex.getLocalizedMessage());
			}
		}
	}
	
	void showOptions()
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
					JOptionPane.showMessageDialog(null, "Impossible de charger les options (" + ex.getLocalizedMessage() + ").", "Erreur", JOptionPane.ERROR_MESSAGE);
				}
            }
        });
	}
	
	int errorsCount = 0; //Le compteur de fails
	static private final int MAX_ERRORS = 4;
	
	void doLogin()
	{
		//On cache le loginForm
		loginCardLayout.show(loginPanel, "loading");
		errorLabel.setVisible(false);
		
		//On fait tout notre bordel
		//progressListener
		FileProgressionListener progressListener = new FileProgressionListener()
		{
			@Override
			public void onFileProgressChanged(int value, int max) {
						loadingBar.setIndeterminate(false);
						loadingBar.setMaximum(max);
						loadingBar.setValue(value);
			}
			
		};
		
		//stateListener
		StateListener stateListener = new StateListener()
		{
			@Override
			public void onStateChanged(boolean failed, String text, boolean onLogin) 
			{
				if (failed == false)
				{
					//Pas d'erreur
					loadingBar.setIndeterminate(true);
					loadingText.setText(text);
					if (errorsCount != 0)
					{
						loadingText.setText(loadingText.getText() + " (essai n°" + (errorsCount + 1) + ")");
					}
					LoggerUtils.println("### " + text);
				}
				else
				{
					LoggerUtils.println("### ERREUR : \"" + text + "\" A L'ESSAI " + (errorsCount + 1) + " ! ###");
					if (errorsCount == MAX_ERRORS || onLogin)
					{
						//Une erreur
						errorLabel.setVisible(true);
						errorLabel.setText("Une erreur est survenue pendant le chargement" + (onLogin ? "" : "au bout de " + (MAX_ERRORS + 1) + " essais ") + "(" + text +").");
						//On réaffiche tout					
						loginCardLayout.show(loginPanel, "login");
						account = null;
						
						LoggerUtils.println("### ABANDON ! ###");
					}
					else
					{
						errorsCount++;
						doLogin();
					}

				}
			}

			@Override
			public void onStateChanged(boolean failed, String text) 
			{
				onStateChanged(failed, text, false);
			}			
		};		
		GameLaunchedListener launchedListener = new GameLaunchedListener(){
			@Override
			public void onGameClosed() {
				System.exit(0);	
			}

			@Override
			public void onGameLaunched() {
				frame.setVisible(false);		
			}
			
		};
		
		//Le account
		if (account == null)
		{
			account = new Account(usernameField.getText(), new String(passwordField.getPassword()), launcherDetails);
		}
		
		new GameLauncher(
				forceUpdate, 
				account,
				progressListener, 
				stateListener,
				launchedListener
		).launchGame();
	}
	
	@SuppressWarnings("serial")
	class WhiteJLabel extends JLabel
	{
		public WhiteJLabel(String text, int align)
		{
			super(text, align);
			this.setForeground(Color.WHITE);
		}
	}

}
*/