package fr.launchmycraft.library;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

public class GetResult 
{
	
	@SerializedName("error")
	public boolean error;
	
	@SerializedName("message")
	public String message;
	
	@SerializedName("data")
	public HashMap<String, String> data;

}
