package fr.launchmycraft.library.versionning;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.launchmycraft.launcher.OperatingSystem;

import com.google.gson.annotations.SerializedName;

public class VersionRule {
    public static enum Action {
        ALLOW, DISALLOW;
    }

    public class OSRestriction {
    	@SerializedName("name")
        private String name;
    	
    	@SerializedName("version")
        private String version;

        public OSRestriction() {
        }

        public boolean isCurrentOperatingSystem() {
            if(name != null && !name.equals(OperatingSystem.getCurrentPlatform().getName()))
            {
                return false;   
            }

            if(version != null)
                try {
                    final Pattern pattern = Pattern.compile(version);
                    final Matcher matcher = pattern.matcher(System.getProperty("os.version"));
                    if(!matcher.matches())
                        return false;
                }
                catch(final Throwable localThrowable) {
                }
            return true;
        }

        @Override
        public String toString() {
            return "OSRestriction{name=" + name + ", version='" + version + '\'' + '}';
        }
    }

    @SerializedName("action")
    private String action;

    @SerializedName("os")
    private OSRestriction os;

    public boolean getAppliedAction() {
        if (os == null)
        {
            return action.equals("allow");
        }
        else
        {
            if (os.isCurrentOperatingSystem())
            {
                return action.equals("allow");
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        return "Rule{action=" + action + ", os=" + os + '}';
    }
}
