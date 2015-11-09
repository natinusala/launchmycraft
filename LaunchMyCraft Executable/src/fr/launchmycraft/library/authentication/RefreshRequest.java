package fr.launchmycraft.library.authentication;

import com.google.gson.annotations.SerializedName;

public class RefreshRequest {
	
	@SerializedName("accessToken")
	public String accessToken;
	
	@SerializedName("clientToken")
	public String clientToken;

}
