package fr.launchmycraft.library.util.network;

import java.io.File;

public class Downloadable
{
    String url = "";
    File file;
    
    public Downloadable(String u, File f)
    {
        this.url = u;
        this.file = f;
    }
}
