package fr.launchmycraft.library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

import fr.launchmycraft.launcher.ExecutableMain;
import fr.launchmycraft.launcher.LoggerUtils;
import fr.launchmycraft.launcher.OperatingSystem;
import fr.launchmycraft.library.authentication.Account;
import fr.launchmycraft.library.authentication.Credentials;
import fr.launchmycraft.library.authentication.LoginException;
import fr.launchmycraft.library.authentication.LoginResult;
import fr.launchmycraft.library.resourcing.ResourcesUpdater;
import fr.launchmycraft.library.util.JavaProcessLauncher;
import fr.launchmycraft.library.util.Util;
import fr.launchmycraft.library.util.listeners.FileEndedListener;
import fr.launchmycraft.library.util.listeners.FileProgressionListener;
import fr.launchmycraft.library.util.listeners.GameLaunchedListener;
import fr.launchmycraft.library.util.listeners.StateListener;
import fr.launchmycraft.library.util.network.DownloadJob;
import fr.launchmycraft.library.util.network.Downloadable;
import fr.launchmycraft.library.util.network.HTTPDownloader;
import fr.launchmycraft.library.versionning.Library;
import fr.launchmycraft.library.versionning.VersionDetails;
import fr.launchmycraft.library.versionning.VersionsList;

public class GameLauncher 
{
	FileProgressionListener progressListener;
	StateListener stateListener;
	GameLaunchedListener gameLaunchedListener;
	
	public static boolean forceUpdate;
	
	Account account;
	
	class SeparatorStringBuilder
    { 
        StringBuilder builder = new StringBuilder();
        String separator = System.getProperty("path.separator");
        
        public void append(String str)
        {
            if (builder.length() > 0)
            {
                builder.append(separator);
            }
            
            builder.append(str);
        }
        
        @Override
        public String toString()
        {
            return builder.toString();
        }
    }
	
	Configuration config;
		
	public GameLauncher(boolean forceUpdate, Account account, FileProgressionListener progressListener, StateListener stateListener, GameLaunchedListener gameLaunchedListener)
	{
		this.progressListener = progressListener;
		this.stateListener = stateListener;
		this.gameLaunchedListener = gameLaunchedListener;
		GameLauncher.forceUpdate = forceUpdate;
		this.account = account;
	}
	
	public GameLauncher launchGame()
	{
		new Worker().start();	
		return this;
	}
	
	class Worker extends Thread
	{
		boolean isLogin = false;;
		
		@Override
		public void run()
		{
			try
			{		
				//Variables
				OperatingSystem currentPlatform = OperatingSystem.getCurrentPlatform();
				File jsonFile = Util.getJsonFile();
				boolean isCrackedAllowed = Util.isCrackedAllowed();
				File jarFile = Util.getJarFile();
				boolean isCustomJarEnabled = Util.isCustomJarEnabled();
				File revisionFile = Util.getJarRevisionFile();
				File versionFolder = Util.getVersionFolder();
				File librariesFolder = Util.getLibrariesFolder();
				String librariesUrl = Util.getLibrariesUrl();             
                File gameDirectory = Util.getGameDirectory();
                File resourcesFolder = Util.getResourcesFolder();		
                File nativeDir = Util.getNativesDir();
                
				//Config
				config = Util.getConfiguration();
				
				//Connexion au compte
				isLogin = true;
				stateListener.onStateChanged(false, "Connexion au compte...");
				
				LoginResult result = new LoginResult();
				
				try
				{
					result = account.doLogin();
				}
				catch (LoginException ex)
				{
					//On regarde si ï¿½a accepte les versions crackï¿½es ET SI y'a pas de mot de passe
					if (!isCrackedAllowed || (isCrackedAllowed && !account.password.equals("")))
					{
						throw new Exception(ex.getLocalizedMessage());
					}
				}
				
				//SetStoredCredentials
				Credentials credentials = new Credentials();
				credentials.username = account.username;
				if (result.selectedProfile != null && result.accessToken != null && result.clientToken != null)
				{
					credentials.accesstoken = result.accessToken;
					credentials.clienttoken = result.clientToken;
					credentials.profilename = result.selectedProfile.name;
					credentials.profileid = result.selectedProfile.id;
				}
				Util.setStoredCredentials(credentials);
				
				Gson gson = new Gson();
				
				isLogin = false;
				//Tï¿½lï¿½chargement de la liste des versions
				stateListener.onStateChanged(false, "Téléchargement de la liste des versions...");
				
				VersionsList versionsList = gson.fromJson(Util.doGET(Util.getOnlineVersionsListUrl(), null), VersionsList.class);
				
				//Vï¿½rification de la version
				stateListener.onStateChanged(false, "Détermination de la version à télécharger...");
				
				//On regarde si y'en a pas et auquel cas on met la derniï¿½re version release
				if (!ExecutableMain.launcherDetails.containsKey("gameversion"))
				{
					ExecutableMain.launcherDetails.put("gameversion", versionsList.latest.get("release"));
				}
				
				LoggerUtils.println("Version " + ExecutableMain.launcherDetails.get("gameversion"));
				
				//Mise ï¿½ jour du JAR
				stateListener.onStateChanged(false, "Calcul des différences...");
				
				if (!jarFile.exists() || !jsonFile.exists() || forceUpdate || !Util.isRevisionUpToDate(isCustomJarEnabled, revisionFile, Long.parseLong(ExecutableMain.launcherDetails.get("customjarrevision")), false))
				{
					stateListener.onStateChanged(false, "Récupération des informations de la version...");
					
					VersionDetails details = gson.fromJson(Util.doGET(Util.getOnlineJsonUrl(ExecutableMain.launcherDetails.get("gameversion")), null), VersionDetails.class);
					
					//Mise ï¿½ jour du jeu
					stateListener.onStateChanged(false, "Mise à  jour du jeu...");
					
					versionFolder.mkdirs();
					
					HTTPDownloader downloader = new HTTPDownloader(Util.getOnlineJarFile(), jarFile);
					downloader.setDownloadProgressionListener(progressListener);
					downloader.downloadFile();
					
					//Sauvegarde du JSON
					stateListener.onStateChanged(false, "Enregistrement des paramètres de la version...");
					if (!jsonFile.exists())
					{
						jsonFile.getParentFile().mkdirs();
						jsonFile.createNewFile();
					}
					Util.writeToFile(jsonFile, gson.toJson(details, VersionDetails.class));
					
					//On met ï¿½ jour la rï¿½vision
					if (isCustomJarEnabled)
					{
						Util.writeToFile(revisionFile, ExecutableMain.launcherDetails.get("customjarrevision"));
					}
				}	
				
				FileEndedListener endedListener = new FileEndedListener(){
					@Override
					public void onFileEnded(int fileIndex, int fileCount) {
						progressListener.onFileProgressChanged(fileIndex, fileCount);			
					}
                	
                };
                
                VersionDetails versionDetails = gson.fromJson(Util.getFileContent(new FileInputStream(jsonFile), true, true), VersionDetails.class);
                
                //Ressouces
                ResourcesUpdater.updateResources(versionDetails, progressListener, endedListener, stateListener);            
                
                //Refresh des dÃ©tails aprÃ¨s ressources perso
                versionDetails = gson.fromJson(Util.getFileContent(new FileInputStream(jsonFile), true, true), VersionDetails.class);
                
                stateListener.onStateChanged(false, "Vérification des bibliothèques...");
                
                //Bibliothï¿½ques
                
                //On résoud les dépendances
                versionDetails = versionDetails.resolveDependencies();
                
                ArrayList<Downloadable> libList = new ArrayList<>();
                LoggerUtils.println(versionDetails.libraries.size() + " bibliothèques à  vérifier...");
                for (int i = 0; i < versionDetails.libraries.size(); i++)
                {
                    Library lib = versionDetails.libraries.get(i);
                    
                    String classifier = null;
                    
                    if (lib.getNatives() != null && lib.getNatives().get(currentPlatform.getName()) != null)
                    {
                        classifier = lib.getNatives().get(currentPlatform.getName());
                    }    
                    
                    File libFile = new File(librariesFolder + "/" + lib.getArtifactPath(classifier));
                    
                    if ((!libFile.exists() || forceUpdate))
                    {
                    	stateListener.onStateChanged(false, "Mise à jour des bibliothèques...");
                        libFile.getParentFile().mkdirs();
                        libList.add(new Downloadable(librariesUrl +  lib.getArtifactPath(classifier), libFile));
                    }
                }
                
                LoggerUtils.println(libList.size() + " bibliothèques à mettre à jour...");
                
                DownloadJob libJob = new DownloadJob(libList);
                libJob.setNewFileListener(endedListener);
                libJob.setLibraryMode(true);
                if (!libJob.downloadAll())
                {
                    throw new Exception("impossible de vérifier les bibliothèques");                   
                    
                }
                
                //Lancement du jeu
                stateListener.onStateChanged(false, "Exécution du jeu...");
                
                final OperatingSystem os = currentPlatform;
                
                String javaDir = (config.javaPath.containsKey(ExecutableMain.launcherId)) ? config.javaPath.get(ExecutableMain.launcherId) : currentPlatform.getJavaDir();
                JavaProcessLauncher processLauncher = new JavaProcessLauncher(javaDir, gameDirectory);                                  
                
                LoggerUtils.println("Chemin vers Java : " + javaDir);
                
                //Arguments                      
                //Truc zarb de l'OS
                
                //Verbose
                //processLauncher.addCommand(new String[] {"-verbose:class", "-verbose:jni"});
                
                if(os.equals(OperatingSystem.OSX))
                {
                    processLauncher.addCommand(new String[] {"-Xdock:icon=" + new File(resourcesFolder, "icons/minecraft.icns").getAbsolutePath() + " -Xdock:name=" + "Minecraft" });
                }
                else if(os.equals(OperatingSystem.WINDOWS))
                {
                    processLauncher.addCommand(new String[] {"-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump" });

                }
                
                //Options recommandées du launcher de base
                processLauncher.addCommand(new String[] {"-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode", "-XX:-UseAdaptiveSizePolicy"});                     

                //Options de RAM Java
                if (config.javaMinRam.containsKey(ExecutableMain.launcherId))
                {
                	processLauncher.addCommand(new String[]{"-Xmn" + config.javaMinRam.get(ExecutableMain.launcherId) + "m"});
                }
                else
                {
                	processLauncher.addCommand(new String[]{"-Xmn" + Util.DEFAULT_MINIMUM_RAM + "m"});
                }
                
                if (config.javaMaxRam.containsKey(ExecutableMain.launcherId))
                {
                	processLauncher.addCommand(new String[]{"-Xmx" + config.javaMaxRam.get(ExecutableMain.launcherId) + "m"});
                }
                else
                {
                	processLauncher.addCommand(new String[]{"-Xmx" + Util.DEFAULT_MAXIMUM_RAM + "m"});
                }                
                
                //Arguments personnalisï¿½s
                if (ExecutableMain.launcherDetails.containsKey("arguments"))          
                {
                	LoggerUtils.println("Ajout des arguments personnalisés : " + ExecutableMain.launcherDetails.get("arguments"));
                	processLauncher.addSplitCommand(ExecutableMain.launcherDetails.get("arguments"));
                } 
           
                //ClassPath
                SeparatorStringBuilder classPath = new SeparatorStringBuilder();
                
                classPath.append(jarFile.getAbsolutePath());
                
                for (Library lib : versionDetails.libraries)
                {               
                    String classifier = null;

                    if (lib.getNatives() != null && lib.getNatives().get(currentPlatform.getName()) != null) {
                        classifier = lib.getNatives().get(currentPlatform.getName());
                    }

                    classPath.append(new File(librariesFolder.getAbsolutePath() + "/" + lib.getArtifactPath(classifier)).getAbsolutePath());
                    
                }
                processLauncher.addCommand(new String[] {"-cp", "\"" + classPath.toString() + "\""});
                
                //Library Path
                //On supprime le dossier des natives
                nativeDir.delete();
                //On le refait
                nativeDir.mkdirs();
                
                //Et on les unpack
                for (Library lib : versionDetails.libraries)
                {                  
                    if (lib.getNatives() != null && lib.getNatives().get(currentPlatform.getName()) != null)
                    {
                        try
                        {
                            String classifier = lib.getNatives().get(currentPlatform.getName());
                            File file = new File(librariesFolder + "/" + lib.getArtifactPath(classifier));                     
                       
                            LoggerUtils.println("Extraction de la librairie native : " + file.getAbsolutePath() + " dans " + nativeDir.getAbsolutePath());
                            Util.unpackArchive(file, nativeDir);
                        }
                        catch (Exception ex)
                        {}                      
                    }                      
                }
                processLauncher.addCommand(new String[] {"-Djava.library.path=" + nativeDir.getAbsolutePath()});
                
                //Main Class
                processLauncher.addCommand(new String[] {versionDetails.mainClass});
                
                //Arguments pour le jeu
                String minecraftArguments = versionDetails.minecraftArguments;
                
                //Auth
                if (result.loggedIn)
                {
                	LoggerUtils.println("Authentifé ; ajout des informations...");
                    //Username
                    minecraftArguments = minecraftArguments.replace("${auth_player_name}", result.selectedProfile.name);
                    
                    //AccessToken
                    minecraftArguments = minecraftArguments.replace("${auth_access_token}", result.accessToken);
                    
                    //Session
                    minecraftArguments = minecraftArguments.replace("${auth_session}", "token:" + result.accessToken + ":" + result.selectedProfile.id);
                    
                    //UUID
                    minecraftArguments = minecraftArguments.replace("${auth_uuid}", result.selectedProfile.id);
                }
                else
                {          
                	LoggerUtils.println("Non authentifié ; ajout des informations par défaut...");
                    minecraftArguments = minecraftArguments.replace("${auth_player_name}", account.username);
                    minecraftArguments = minecraftArguments.replace("${auth_uuid}", new UUID(0L, 0L).toString());
                }
                
                //Version
                minecraftArguments = minecraftArguments.replace("${version_name}", ExecutableMain.launcherDetails.get("gameversion"));
                
                //GameDir
                minecraftArguments = minecraftArguments.replace("${game_directory}", "\"" + gameDirectory.getAbsolutePath() + "\"");
                
                //AssetsDir
                minecraftArguments = minecraftArguments.replace("${assets_root}", "\"" + Util.getAssetsRoot(Util.getAssetsIndex(versionDetails)) + "\"");
                minecraftArguments = minecraftArguments.replace("${game_assets}", "\"" + new File(resourcesFolder, "/" + "virtual" + "/" + "legacy").getAbsolutePath() + "\"");
                
                //AssetsIndex
                minecraftArguments = minecraftArguments.replace("${assets_index_name}", Util.getAssetsIndex(versionDetails));
                
                //UserProperties
                minecraftArguments = minecraftArguments.replace("${user_properties}", new Gson().toJson(new HashMap<String, Collection<String>>()));
                
                //UserType
                minecraftArguments = minecraftArguments.replace("${user_type}", "legacy");
                              
                //Le serveur
                if (ExecutableMain.launcherDetails.containsKey("enableautoconnection") && ExecutableMain.launcherDetails.get("enableautoconnection").equals("1"))
                {
                	LoggerUtils.println("Connexion automatique activée");
                	String port = "25565";
                    if (ExecutableMain.launcherDetails.get("serveraddress").contains(":"))
                    {
                        port = ExecutableMain.launcherDetails.get("serveraddress").substring(ExecutableMain.launcherDetails.get("serveraddress").lastIndexOf(":"));  
                        ExecutableMain.launcherDetails.put("serveraddress", ExecutableMain.launcherDetails.get("serveraddress").replace(port, ""));
                        port = port.replace(":", "");
                    }
                    
                    minecraftArguments += " --port " + port + " --server " + ExecutableMain.launcherDetails.get("serveraddress");
                }     
                
                //La taille de la fenï¿½tre
                if (ExecutableMain.launcherDetails.containsKey("width"))
                {
                	LoggerUtils.println("Ajout de la largeur personnalisée : " + ExecutableMain.launcherDetails.get("width"));
                	minecraftArguments += " --width " + ExecutableMain.launcherDetails.get("width");
                }
                else
                {
                	LoggerUtils.println("Largeur par défaut : " + 854);
                	minecraftArguments += " --width " + 854;
                }
                
                if (ExecutableMain.launcherDetails.containsKey("height"))
                {
                	LoggerUtils.println("Ajout de la hauteur personnalisée : " + ExecutableMain.launcherDetails.get("height"));
                	minecraftArguments += " --height " + ExecutableMain.launcherDetails.get("height");
                }
                else
                {
                	LoggerUtils.println("Hauteur par défaut : " + 480);
                	minecraftArguments += " --height " + 480;
                }
                
                //Plein ï¿½cran
                if (ExecutableMain.launcherDetails.containsKey("fullscreen") && ExecutableMain.launcherDetails.get("fullscreen").equals("1"))
                {
                	LoggerUtils.println("Plein écran activé");
                	minecraftArguments += " --fullscreen";
                }
                       
                //Ajout de tout ï¿½a
                processLauncher.addSplitCommand(minecraftArguments);
                
                //Variables d'environnement
                if (ExecutableMain.launcherDetails.containsKey("debug") && ExecutableMain.launcherDetails.get("debug").equals("1"))
                {
                	LoggerUtils.println("Variables d'environnement :");
                	LoggerUtils.println(processLauncher.getEnvironment().toString());
                }
                
                //Exï¿½cution
                if (ExecutableMain.launcherDetails.containsKey("debug") && ExecutableMain.launcherDetails.get("debug").equals("1"))
                {       
                    LoggerUtils.println("Exécution de la ligne de commande :");
                    LoggerUtils.println(processLauncher.getCommandLine());
                }                          
                
                //DÃ©tecteur de crash
                boolean hasStopped = false;
                boolean crashDetectorEnabled = ExecutableMain.launcherDetails.containsKey("crashdetectorenabled");
                
                LoggerUtils.println("\n--- LOGS DU JEU  ---\n");
                Process p = processLauncher.start();                           
                
                //On cache la fenï¿½tre
                gameLaunchedListener.onGameLaunched();
                
                //Le jeu
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                
                String resultLine = in.readLine();  
                
                while (resultLine != null) 
                {             	
                	LoggerUtils.println(resultLine);
                	//DÃ©tecteur de crash
                	if (crashDetectorEnabled && resultLine.contains("Stopping!"))
                	{
                		hasStopped = true;
                	}
                    resultLine = in.readLine();
                }
                
                //DÃ©tecteur de crash               
                if (crashDetectorEnabled && !hasStopped)
                {
                	LoggerUtils.println("### Le jeu a crashé ! ###");
                	int dialogResult = JOptionPane.showOptionDialog(null, "On dirait que le jeu a crashé.\nSi vous voulez de l'aide pour résoudre ce souci, allez faire un tour sur le forum.\nOn vous y demandera les logs du launcher ; voulez-vous les enregistrer ?", "Oups !", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[] {"Oui", "Non"}, "Oui");
                	if (dialogResult == 0)
                	{
                		LoggerUtils.openSavePrompt();
                	}
                }
                         
				//Fermeture
                gameLaunchedListener.onGameClosed();
                
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				stateListener.onStateChanged(true, ex.getLocalizedMessage(), isLogin);
			}
		}
	}
}
