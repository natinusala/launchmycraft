package fr.launchmycraft.library.resourcing;

import java.util.LinkedHashMap;

import com.google.gson.annotations.SerializedName;

public class AssetIndex {
	
	@SerializedName("objects")
	public LinkedHashMap<String, AssetObject> objects = new LinkedHashMap<String, AssetObject>();

}
