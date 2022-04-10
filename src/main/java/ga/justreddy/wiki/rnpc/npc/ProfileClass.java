package ga.justreddy.wiki.rnpc.npc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class ProfileClass {

    private static int lastProfileId = 0;
    private final UUID npcUuid;
    private String uuidAsString, profileOwner, profileId;
    private final GameProfile gameProfile;

    public ProfileClass() {
        this.npcUuid = UUID.randomUUID();
        this.profileId = getNewProfileId();
        this.gameProfile = new GameProfile(npcUuid, profileId);
    }

    private String getNewProfileId(){
        if((lastProfileId + "").length() > 31) return "";
        return  lastProfileId++ + "";
    }

    private void updateProfileTextures() {
        try{
            final URL profileUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidAsString + "?unsigned=false");
            InputStreamReader reader = getNewInputStreamReader(profileUrl);
            JsonObject textureProperty = getProperty(getNewJsonElement(reader));
            this.gameProfile.getProperties().put("textures", getNewProperties("textures", getPropertyAsString(textureProperty, "value"),
                    getPropertyAsString(textureProperty, "signature")));

        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private InputStreamReader getNewInputStreamReader(URL url) throws IOException {
        return new InputStreamReader(url.openStream());
    }

    private JsonObject getProperty(JsonElement element) {
        return element.getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
    }

    private JsonElement getNewJsonElement(InputStreamReader reader){
        return new JsonParser().parse(reader);
    }

    private boolean isElementJson(JsonElement element) {
        return element instanceof JsonObject;
    }

    private Property getNewProperties(String name, String propertyAsString, String signature) {
        return new Property(name, propertyAsString, signature);
    }

    private String getPropertyAsString(JsonObject container, String property) {
        return container.get(property).getAsString();
    }

    public boolean updateSkinOwner(String skinOwnerName) {
        this.profileOwner = skinOwnerName;
        this.uuidAsString = getUuidFromProfileOwner();
        if(uuidAsString == null) return false;
        updateProfileTextures();
        return false;
    }

    private String getUuidFromProfileOwner(){
        try{
            URL profile = getProfileUrl();
            InputStreamReader read = getNewInputStreamReader(profile);
            JsonElement element = getNewJsonElement(read);
            if(!isElementJson(element)) return null;
            return element.getAsJsonObject().get("id").getAsString();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private URL getProfileUrl() throws IOException {
        return new URL("https://api.mojang.com/users/profiles/minecaft/" + profileOwner);
    }

    public GameProfile getProfile() {
        return gameProfile;
    }

}
