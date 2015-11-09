package fr.launchmycraft.launcher.bootstrap;

import static fr.launchmycraft.launcher.bootstrap.Constants.LAUNCHER_FILE_URL;
import static fr.launchmycraft.launcher.bootstrap.Constants.LAUNCHER_MAIN_CLASS;
import static fr.launchmycraft.launcher.bootstrap.Constants.LAUNCHER_MD5_URL;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Natinusala
 */
public class BootstrapCore {

    static TexturedPanel mainPanel;
    static JLabel logo;
    static JTextArea text;
    static JProgressBar loading;
    static JFrame frame;
    
    static Config config;
    
    static boolean hasPaid;
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        //ContentPane
        mainPanel = new TexturedPanel(ImageIO.read(BootstrapCore.class.getResourceAsStream("/dirt.png")));
        mainPanel.setOpaque(true);
        
        //JFrame
        frame = new JFrame("Minecraft");       
        frame.setContentPane(mainPanel);
        frame.setBackground(Color.DARK_GRAY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 550);
        frame.setResizable(false);
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        
        //CrÃ©ation de l'interface
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(true);
        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 25;
        c.ipady = 25;
        mainPanel.add(panel, c);
        
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        c.ipady = 5;
        
        c.gridx = 0;
        c.gridy = 0;
        
        //Logo
        logo = new JLabel();
        logo.setIcon(new ImageIcon(ImageIO.read(BootstrapCore.class.getResourceAsStream("/logo.png"))));
        c.gridy++;
        panel.add(logo, c);
        
        //Texte
        text = new JTextArea();
        text.setText("Chargement du launcher en cours...");
        text.setLineWrap(true);
        text.setEditable(false);
        text.setFocusable(false);
        text.setBackground(panel.getBackground());
        text.setWrapStyleWord(true);
        c.gridy++;
        panel.add(text, c);
        
        //Loading
        loading = new JProgressBar();
        loading.setIndeterminate(true);
        c.gridy++;
        panel.add(loading, c);
        
        //Crï¿½dits
        JCredits credits = new JCredits("Launcher proposé par LaunchMyCraft.fr", "https://www.launchmycraft.fr/");  
        credits.setHorizontalAlignment(JTextField.RIGHT);
        c.gridy++;       
        //panel.add(credits, c);
        
        //Icï¿½ne
        ImageIcon img = new ImageIcon(ImageIO.read(BootstrapCore.class.getResourceAsStream("/icon.png")));
        frame.setIconImage(img.getImage());
        
        //Affichage
        frame.setVisible(true);     
        
        //Exï¿½cution de la tï¿½che
        new Worker().start();
    }
    
    
    
    public static void die(String message)
    {
        loading.setVisible(false);
        text.setText("Une erreur est survenue pendant l'exécution du launcher (" + message + ").");
    }
    
    static File getLauncherFile() throws JsonSyntaxException, IOException
    {
        File f = new File(OperatingSystem.getBaseWorkingDirectory(hasPaid, config.identifier), "launcher.jar");
        f.getParentFile().mkdirs();
        return f;
    }
    
    static class Worker extends Thread
    {
        @SuppressWarnings({ "unchecked", "rawtypes", "resource" })
		@Override
        public void run()
        {
            try
            {                  	    
        		//Lecture
                text.setText("Lecture des informations du launcher...");
                Gson gson = new Gson();
                Reader reader = new InputStreamReader(BootstrapCore.class.getResourceAsStream("/config.json"));
                config = gson.fromJson(reader, Config.class);
                
                try
                {
                	//HasPaid
            		hasPaid = (new Gson().fromJson(Util.doGET(getHasPaidUrl(config.launcherid), null), HasPaid.class)).data;		         	
                }
                catch (Exception ex)
                {
                	ex.printStackTrace();
                	throw new Exception("impossible de se connecter au serveur");
                }
                              
                //Vï¿½rification du JAR du launcher
                text.setText("Vérification du launcher...");
                File launcherFile = getLauncherFile();
                String lastLauncherMd5 = Util.doGET(LAUNCHER_MD5_URL(), null);
                if (!launcherFile.exists() || !Util.getHash(launcherFile, "MD5").equals(lastLauncherMd5))
                {
                    //Tï¿½lï¿½chargement du launcher
                    text.setText("Mise à jour du launcher...");
                    loading.setIndeterminate(false);
                    downloadJar();
                    loading.setIndeterminate(true);
                }
                
                //Exï¿½cution
                text.setText("Chargement du launcher...");
                Class aClass = new URLClassLoader(new URL[] { launcherFile.toURI().toURL() }).loadClass(LAUNCHER_MAIN_CLASS);
                Constructor constructor = aClass.getConstructor(new Class[] { long.class, JFrame.class, long.class,  boolean.class, String.class });
                constructor.newInstance(new Object[] { Constants.VERSION, frame, config.launcherid, hasPaid, config.identifier });
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                die(ex.getLocalizedMessage());
            }
        }
    }
    
    static void downloadJar() throws MalformedURLException, IOException
    {
        URLConnection con;
        DataInputStream dis;  
        FileOutputStream fos;
        byte[] fileData;
        
        URL url = new URL(LAUNCHER_FILE_URL());
        con = url.openConnection();
        
        dis = new DataInputStream(con.getInputStream());
        
        fileData = new byte[con.getContentLength()];
        loading.setMaximum(fileData.length);
        for (int x = 0; x < fileData.length; x++) 
        {             
            fileData[x] = dis.readByte();
            loading.setValue(x);
        }
        
        dis.close(); 
        fos = new FileOutputStream(getLauncherFile());
        fos.write(fileData);  
        fos.close();
    }
    
	public static String getHasPaidUrl(long id)
	{
		return "https://launchmycraft.fr/api/haspaid/" + id;
	}
	

	public class HasPaid
	{
		@SerializedName("data")
		public boolean data;
	}
	

	public static boolean hasPaid() throws JsonSyntaxException, IOException
	{
		return hasPaid;
	}  
}
