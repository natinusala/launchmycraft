package fr.launchmycraft.library.authentication;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import fr.launchmycraft.library.util.Util;

public class LoginRequest
    {
		@SerializedName("username")
        public String username = "";
		
		@SerializedName("password")
        public String password = "";    
        
		@SerializedName("agent")
        public Map<String, Object> agent = new HashMap<>();
        
        
        public LoginRequest(String u, String p)
        {
            username = u;
            password = p;
            
            agent.put("name", "Minecraft");
            agent.put("version", Util.getAuthVersion());
        }
    }