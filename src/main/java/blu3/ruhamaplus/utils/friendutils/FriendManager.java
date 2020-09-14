package blu3.ruhamaplus.utils.friendutils;

import blu3.ruhamaplus.RuhamaPlus;
import blu3.ruhamaplus.module.ModuleManager;
import blu3.ruhamaplus.utils.ClientChat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class FriendManager {

    public static FriendManager Get()
    {
        return RuhamaPlus.GetFriendManager();
    }



    public FriendManager()
    {
    }
    public void LoadFriends()
    {
        File l_Exists = new File("blu3/ruhama+/Friendlist.json");
        if (!l_Exists.exists())
            return;

        try
        {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("blu3/" + "ruhama+/" + "FriendList" + ".json"));

            // convert JSON file to map
            FriendList = gson.fromJson(reader, new TypeToken<LinkedTreeMap<String, Friend>>(){}.getType());

            // close reader
            reader.close();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void SaveFriends()
    {
        GsonBuilder builder = new GsonBuilder();

        Gson gson = builder.setPrettyPrinting().create();

        Writer writer;
        try
        {
            writer = Files.newBufferedWriter(Paths.get("blu3/" + "ruhama+/" + "FriendList" + ".json"));

            gson.toJson(FriendList, new TypeToken<LinkedTreeMap<String, Friend>>(){}.getType(), writer);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private LinkedTreeMap<String, Friend> FriendList = new LinkedTreeMap<>();

    public boolean IsFriend(Entity p_Entity)
    {
        return p_Entity instanceof EntityPlayer && FriendList.containsKey(p_Entity.getName().toLowerCase());
    }

    public boolean addFriend(String p_Name)
    {
        if (FriendList.containsKey(p_Name.toLowerCase())) {
            ClientChat.log(p_Name.toLowerCase() + " is already a friend");
            return false;
        }
        Friend l_Friend = new Friend(p_Name.toLowerCase(), p_Name.toLowerCase());

        FriendList.put(p_Name.toLowerCase(), l_Friend);
        ClientChat.log(p_Name.toLowerCase() + " has been added as a friend");
        SaveFriends();
        return true;
    }

    public boolean removeFriend(String p_Name)
    {
        if (!FriendList.containsKey(p_Name.toLowerCase())) {
            ClientChat.log(p_Name.toLowerCase() + " is not a friend");
            return false;
        }
        FriendList.remove(p_Name.toLowerCase());
        SaveFriends();
        ClientChat.log(p_Name.toLowerCase() + " removed from friends");
        return true;
    }

    public final LinkedTreeMap<String, Friend> GetFriends()
    {
        return FriendList;
    }
    public boolean isFriend(String p_Name)
    {
        if ((!Objects.requireNonNull(ModuleManager.getModuleByName("Friends")).isToggled())) return false;

        return FriendList.containsKey(p_Name.toLowerCase());


        //FriendManager.Get().isFriend(e.toLowerCase())
    }

    public Friend GetFriend(Entity e)
    {
        if ((!Objects.requireNonNull(ModuleManager.getModuleByName("Friends")).isToggled()))
            return null;

        if (!FriendList.containsKey(e.getName().toLowerCase()))
            return null;

        return FriendList.get(e.getName().toLowerCase());
    }

    public void Load()
    {
        LoadFriends();
    }
}
