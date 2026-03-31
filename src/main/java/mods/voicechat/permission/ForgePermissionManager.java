package mods.voicechat.permission;


public class ForgePermissionManager extends PermissionManager {

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new ForgePermission(modId + "." + node, type);
    }

}
