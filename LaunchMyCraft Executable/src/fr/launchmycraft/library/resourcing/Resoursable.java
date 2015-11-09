package fr.launchmycraft.library.resourcing;

import java.io.File;

public class Resoursable {
    
    public String expected_sha1 = "";
    public String url = "";
    public File fileToCheck;
    
    public Resoursable(String e, String u, File f)
    {
        this.expected_sha1 = e;
        this.url = u;
        this.fileToCheck = f;
    }
}