/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.launchmycraft.launcher.bootstrap;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Natinusala
 */
public class Config {
    
	@SerializedName("launcherid")
    public long launcherid;
	
	@SerializedName("newsurl")
    public String newsurl;   
	
	@SerializedName("identifier")
    public String identifier;
    
}
