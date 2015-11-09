package fr.launchmycraft.bootstrapupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Natinusala
 */
public class HTTPDownloader {

	String from;
	File originalFile;

	int downloaded;

	public HTTPDownloader(String from, File f)
	{
		this.originalFile = f;
		this.from = from;
	} 

	public boolean downloadFile() throws Exception
	{
		File tempFile = File.createTempFile("LMC" + originalFile.getName() + Math.random(), ".tmp");

		if (originalFile.exists())
		{
			originalFile.delete();
		}

		URLConnection con;
		FileOutputStream fos;

		URL url = new URL(from);
		con = url.openConnection();                  

		con.setUseCaches(false);
		con.setDefaultUseCaches(false);
		con.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		con.setRequestProperty("Expires", "0");
		con.setRequestProperty("Pragma", "no-cache");
		int timeout = 60000;
		con.setConnectTimeout(timeout);
		con.setReadTimeout(timeout);

		InputStream input = con.getInputStream();
		int bufferSize = 512;
		byte[] buffer = new byte[bufferSize];
		int read;
		fos = new FileOutputStream(tempFile);

		while ((read = input.read(buffer)) > 0)
		{
			fos.write(buffer, 0, read);
			downloaded += read;
		}

		fos.flush();        
		fos.close();
		input.close();


		//Copie du fichier
		return tempFile.renameTo(originalFile);
	}    
}