package fr.launchmycraft.library.versionning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import fr.launchmycraft.library.util.Util;

public class VersionDetails {
    
	@SerializedName("id")
    public String id = "";
	
	@SerializedName("minecraftArguments")
    public String minecraftArguments = "";
	
	@SerializedName("libraries")
    public ArrayList<Library> libraries = new ArrayList<Library>();
    
    @SerializedName("mainClass")
    public String mainClass = "";
    
    @SerializedName("rules")
    public ArrayList<VersionRule> rules = new ArrayList<VersionRule>();
    
    @SerializedName("assets")
    public String assets;
    
    @SerializedName("inheritsFrom")
    public String inheritsFrom;
    
    public VersionDetails resolveDependencies() throws Exception
    {
    	if (inheritsFrom == null || inheritsFrom.isEmpty())
    	{
    		return this;
    	}
    	
    	//Le nouvel objet
    	VersionDetails newData = null;
    	
    	//Le fichier du parent
    	File parentFile = Util.getJsonFile(inheritsFrom);
    	
    	//Le fichier existe, on le récupère
    	if (parentFile.exists())
    	{
    		newData = new Gson().fromJson(Util.getFileContent(new FileInputStream(parentFile), true, true), VersionDetails.class);
    	}
    	//Le fichier n'existe pas
    	else
    	{
    		throw new Exception("Impossible de résoudre la dépendance " + inheritsFrom + " - fichier inexistant");
    	}
    	
    	//On résout les dépendances du parent
    	newData = newData.resolveDependencies();
    	
    	//On effectue l'héritage
    	if (this.id != null) {newData.id = this.id;}
    	if (minecraftArguments != null) {newData.minecraftArguments = minecraftArguments;}
    	if (mainClass != null) {newData.mainClass = mainClass;}
    	if (rules != null) {newData.rules.addAll(rules);}
    	if (assets != null) {newData.assets = assets;}
    	newData.inheritsFrom = null;
    	
    	//Les librairies
    	for (Library lib : libraries)
    	{
    		lib.splitNameAndVersion();
    		
    		//On regarde si le newData le contient
    		Library libNew = null;
    		
    		for (Library libNewData : newData.libraries)
    		{
    			libNewData.splitNameAndVersion();
    			    			
    			if (lib.getNameOnly().equals(libNewData.getNameOnly()))
    			{
    				libNew = libNewData;
    				break;
    			}
    		}
    		
    		if (libNew == null)
    		{
    			//On ne l'a pas trouvée, on la rajoute
    			newData.libraries.add(lib);
    		}
    		else
    		{
    			//On l'a trouvée, on regarde si la version est supérieure		
    			if (isVersionGreater(lib.getVersion(), libNew.getVersion()))
    			{
    				//On vire l'ancienne et on met la nouvelle à la place
    				newData.libraries.remove(libNew);
    				newData.libraries.add(lib);
    			}
    		}
    	}
    	
    	return newData;
    }
    
    boolean isVersionGreater(String v1, String v2)
    {
    	String[] v1Split = v1.split("\\.");
    	String[] v2Split = v2.split("\\.");
    	
    	ArrayList<Integer> v1Numbers = new ArrayList<Integer>();
    	ArrayList<Integer> v2Numbers = new ArrayList<Integer>();
    	
    	for (String s : v1Split)
    	{
    		try
    		{
    			v1Numbers.add(Integer.parseInt(s));
    		}
    		catch (NumberFormatException ex)
    		{
    			//Is that Netty ?
    		}
    	}
    	
    	for (String s : v2Split)
    	{
    		try
    		{
    			v2Numbers.add(Integer.parseInt(s));
    		}
    		catch (NumberFormatException ex)
    		{
    			//Is that Netty ?
    		}
    	}
    	
    	int count = (v1Numbers.size() > v2Numbers.size()) ? v1Numbers.size() : v2Numbers.size();
    	
    	for (int i = 0; i < count; i++)
    	{
			if (v1Numbers.get(i) > v2Numbers.get(i))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
}

