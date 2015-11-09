package fr.launchmycraft.library.resourcing;

import java.util.ArrayList;

import fr.launchmycraft.library.util.SyncArrayList;
import fr.launchmycraft.library.util.Util;

public class ResourceJob {
    
    ArrayList<Resoursable> fileList;
    
    int totalCount;
    int checkedCount;
    
    int threadsCount = 6;
    int threadsTerminated;
    
    boolean forceUpdate;
    
    SyncArrayList<Resoursable> filesToDownload = new SyncArrayList<>();
    
    boolean ended;
    
    boolean failed;
    
    public ResourceJob(ArrayList<Resoursable> list, boolean update)
    {
        this.fileList = list;
        this.forceUpdate = update;
        
        totalCount = list.size();
    }
    
    public synchronized void threadTerminated()
    {
        threadsTerminated++;
        if (threadsCount == threadsTerminated)
        {
            ended = true;
        }
    }
    
    public boolean checkAll() throws InterruptedException
    {
        //On d�marre le truc
        startThreads();
        //On attend
        while (ended == false)
        {
            Thread.sleep(1000);
        }
        
        return !failed;
    }
    
    public void startThreads()
    {
        for (int i = 0; i < threadsCount; i++)
        {
            new SHA1Thread().start();
        }
    }
    
    public void fileEnded()
    {
        checkedCount++;
        /*if (listener != null)
        {
            listener.onFileChecked();
        }*/
    }
    
    public int getTotalFilesCount()
    {
        return totalCount;
    }
    
    public int getCheckedFiles()
    {
        return checkedCount;
    }
    
    public synchronized Resoursable getNextFile()
    {
        if (failed == true || fileList.isEmpty())
        {
            return null;
        }
        else
        {
            Resoursable m = fileList.get(0);
            fileList.remove(0);
            return m;
        }    
    }
    
    public ArrayList<Resoursable> getFilesToDownload() throws Exception
    {
        if (ended == false)
        {
            throw new Exception();
        }
        else
        {
            return filesToDownload.getArrayList();
        }
    }
    
    public class SHA1Thread extends Thread
    {
        @Override
        public void run()
        {
            while (true)
            {
                Resoursable next = getNextFile();
                if (next == null)
                {
                    threadTerminated();
                    break;
                }
                
                try
                {
                    if 
                    (!next.fileToCheck.exists() || ((!next.expected_sha1.contains(Util.getHash(next.fileToCheck, "SHA-1")) || forceUpdate)
                    		&& 
                    		(!next.fileToCheck.getName().contains(".txt") && !next.fileToCheck.getName().contains(".lang")&& !next.fileToCheck.getName().contains(".mcmeta") && !next.fileToCheck.getName().contains(".json"))))          
                    {
                        //On l'ajoute
                        filesToDownload.add(next);
                    }
                    fileEnded();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    failed = true;
                }
            }
        }
    }
}
