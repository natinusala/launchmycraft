package fr.launchmycraft.launcher.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class Util 
{
    
    @SuppressWarnings({ "resource" })
	public static String doGET(String u, String type) throws IOException
    {
        HttpClient httpClient = new DefaultHttpClient();          
        HttpGet request = new HttpGet(u);
      
        request.addHeader("content-type", type);
        
        HttpResponse response = httpClient.execute(request);
        
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8"); 
    }
    
    @SuppressWarnings("resource")
	public static String getHash(final File file, String hash) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        DigestInputStream stream = null;
        
            stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(hash));
            final byte[] buffer = new byte[65536];

            int read = stream.read(buffer);
            while(read >= 1)
                read = stream.read(buffer);
        return String.format("%1$032x", new Object[] { new BigInteger(1, stream.getMessageDigest().digest()) });
    }
}
