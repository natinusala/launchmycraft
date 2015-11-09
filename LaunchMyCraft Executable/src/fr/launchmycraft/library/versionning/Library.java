package fr.launchmycraft.library.versionning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.gson.annotations.SerializedName;

import fr.launchmycraft.library.util.Util;

public class Library {
    @SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	private static final StrSubstitutor SUBSTITUTOR = new StrSubstitutor(new HashMap() {
    });
    
    @SerializedName("name")
    private String name;
    
    private String nameOnly;
    private String version;
    
    @SerializedName("rules")
    private List<VersionRule> rules;
    
    @SerializedName("natives")
    private Map<String, String> natives;
    
    @SerializedName("url")
    private String url;

    public Library() {}

    public Library(final String name) 
    {
        if(name == null || name.isEmpty())
        {
        	throw new IllegalArgumentException("Le nom de la librairie ne peut être vide");
        }
        
        this.name = name;
        computeArch();
    }
    
    public void splitNameAndVersion()
    {
        String[] versionSplit = name.split(":");
        
        this.nameOnly = versionSplit[1];
        this.version = versionSplit[2];
    }
    
    public String getNameOnly()
    {
    	return nameOnly;
    }
    
    public String getVersion()
    {
    	return version;
    }
    
    public void computeArch()
    {    
	    this.name = this.name.replaceAll(Pattern.quote("${arch}"), Util.getArchIdentifier());
    }

    public boolean appliesToCurrentEnvironment() {
        if(rules == null)
        {
           return true; 
        }
        
        for(VersionRule rule : rules) {
            
            if (rule.getAppliedAction() == false)
            {
                return false;
            }
        }  
        
        return true;       
    }

    public String getArtifactBaseDir() {
        if(name == null)
            throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
        final String[] parts = name.split(":", 3);
        return String.format("%s/%s/%s", new Object[] { parts[0].replaceAll("\\.", "/"), parts[1], parts[2] });
    }

    public String getArtifactFilename(final String classifier) {
        if(name == null)
            throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");

        final String[] parts = name.split(":", 3);
        final String result = String.format("%s-%s%s.jar", new Object[] { parts[1], parts[2], classifier != null ? "-" + classifier : "" });

        return SUBSTITUTOR.replace(result);
    }

    public String getArtifactPath() {
        return getArtifactPath(null);
    }

    public String getArtifactPath(final String classifier) {
        if(name == null)
            throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
        return String.format("%s/%s", new Object[] { getArtifactBaseDir(), getArtifactFilename(classifier) });
    }

    public String getDownloadUrl() {
        if(url != null)
            return url;
        return Util.getLibrariesUrl();
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getNatives() {
        return natives;
    }

    public List<VersionRule> getRules() {
        return rules;
    }

    public boolean hasCustomUrl() {
        return url != null;
    }

	@Override
	public String toString() 
	{
		return "Library [name=" + name + ", rules=" + rules + ", natives="
				+ natives + ", url=" + url + "]";
	}

    
}
