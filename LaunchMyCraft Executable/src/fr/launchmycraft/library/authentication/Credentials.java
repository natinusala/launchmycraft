package fr.launchmycraft.library.authentication;

import com.google.gson.annotations.SerializedName;

public class Credentials {
	
	@SerializedName("username")
	public String username = "";
	
	@SerializedName("accesstoken")
	public String accesstoken = "";
	
	@SerializedName("clienttoken")
	public String clienttoken = "";
	
	@SerializedName("profilename")
	public String profilename = "";
	
	@SerializedName("profileid")
	public String profileid = "";

}
