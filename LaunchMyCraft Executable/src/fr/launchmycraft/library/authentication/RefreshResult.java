package fr.launchmycraft.library.authentication;

import com.google.gson.annotations.SerializedName;

public class RefreshResult {
	
	@SerializedName("accessToken")
	public String accessToken;	
	
	@SerializedName("error")
	public String error;

}
