package fr.launchmycraft.library.util.network;

import java.util.ArrayList;

import fr.launchmycraft.library.util.listeners.FileEndedListener;

public class DownloadJob {
    
    private ArrayList<Downloadable> files;
    private boolean jobFinished = false;
    
    private int threadCount;
    private int threadTerminatedCount;
    
    int downloadedcount;
    int totalcount;
    
    boolean failed;   
    
    boolean library;
    
    public void setLibraryMode(boolean m)
    {
        this.library = m;
    }
    
    FileEndedListener listener;
    
    public DownloadJob(ArrayList<Downloadable> f)
    {
        this(f, 30);
    }
     
    public DownloadJob(ArrayList<Downloadable> f, int threads)
    {
        this.files = f;
        this.threadCount = threads;
        
        totalcount = f.size();
    }
    
    public void setNewFileListener(FileEndedListener l)
    {
        listener = l;
    }
    
    public boolean downloadAll() throws InterruptedException
    {
        //On lance les t�l�chargements
        startThreads();
        //On attend
        while (jobFinished == false)
        {
            Thread.sleep(1000);
        }
        
        return !failed;
    }
    
    private synchronized void threadTerminated()
    {
        threadTerminatedCount++;
        if (threadTerminatedCount == threadCount)
        {
            jobFinished = true;
        }
    }
    
    private synchronized void fileEnded()
    {
        downloadedcount++;
        if (listener != null)
        {
            listener.onFileEnded(downloadedcount, totalcount);
        }
    }
    
    private synchronized Downloadable getNextFile()
    {
        if (files.isEmpty() || failed == true)
        {
            return null;
        }
        else
        {
            Downloadable next = files.get(0);
            files.remove(0);           
            return next;
        }
    }
    
    public int getDownloadedFilesCount()
    {
        return downloadedcount;
    }
    
    public int getTotalFilesCount()
    {
        return totalcount;
    }
    
    private void startThreads()
    {
        for (int i = 0; i < threadCount; i++)
        {
            new DownloadThread().start();
        }
    }
    
    private class DownloadThread extends Thread
    {
        @Override
        public void run()
        {
            while (true)
            {
                Downloadable down = getNextFile();
                if (down == null)
                {
                    threadTerminated();
                    break;
                }
                else
                {
                    try
                    {
                    	if (!down.file.exists())
                    	{
                    		down.file.getParentFile().mkdirs();
                    	}
                        new HTTPDownloader(down.url, down.file).downloadFile();
                        
                        fileEnded();
                    }
                    catch (Exception ex)
                    {                    
                        if (!library)
                        {
                            ex.printStackTrace();
                            failed = true; 
                        }                       
                    }
                }
            }
        }
    }
    
}