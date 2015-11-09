package fr.launchmycraft.library.versionning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;


public class VersionsList {
    
    public class Version
    {
    	@SerializedName("id")
        public String id = "";
    	
    	@SerializedName("time")
        public String time = "";
    	
    	@SerializedName("releaseTime")
        public String releaseTime = "";
    	
    	@SerializedName("type")
        public String type = "";
    }
           
    
    @SerializedName("latest")
    public Map<String, String> latest = new HashMap<>();
    
    @SerializedName("versions")
    public ArrayList<Version> versions = new ArrayList<Version>();
    
    public boolean hasVersionById(String id)
    {
        for (Version v : versions)
        {
            if (v.id.equals(id))
            {
                return true;
            }
        }
        return false;
    }
    
}