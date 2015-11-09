package fr.launchmycraft.library;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

import fr.launchmycraft.library.authentication.Credentials;

public class Configuration {
	
	@SerializedName("storedCredentials")
	public HashMap<Long, Credentials> storedCredentials = new HashMap<Long, Credentials>();
	
	@SerializedName("javaPath")
	public HashMap<Long, String> javaPath = new HashMap<Long, String>();

	@SerializedName("cguRevision")
	public int cguRevision = 0;
	
	@SerializedName("javaMaxRam")
	public HashMap<Long, Integer> javaMaxRam = new HashMap<Long, Integer>();
	
	@SerializedName("javaMinRam")
	public HashMap<Long, Integer> javaMinRam = new HashMap<Long, Integer>();
}
