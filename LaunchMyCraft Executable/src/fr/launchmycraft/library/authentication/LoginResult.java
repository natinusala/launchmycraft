package fr.launchmycraft.library.authentication;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
    
    public class LoginResult
    {
    	@SerializedName("loggedIn")
        public boolean loggedIn = false;
        
    	@SerializedName("error")
        public String error;
    	
    	@SerializedName("errorMessage")
        public String errorMessage = "";
    	
    	@SerializedName("cause")
        public String cause = "";
        
    	@SerializedName("accessToken")
        public String accessToken = "";
    	
    	@SerializedName("clientToken")
        public String clientToken = "";
    	
    	@SerializedName("availableProfiles")
        public ArrayList<LoginProfile> availableProfiles = new ArrayList<>();
        
        @SerializedName("selectedProfile")
        public LoginProfile selectedProfile;
        
    }