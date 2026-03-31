package mods.voicechat.permission;

import net.minecraft.entity.player.ServerPlayerEntity;

public class ForgePermission implements Permission {

    private final String node;
    private final PermissionType type;

    public ForgePermission(String node, PermissionType type) {
        this.node = node;
        this.type = type;
    }

    @Override
    public boolean hasPermission(ServerPlayerEntity player) {
        return true;
    }

    @Override
    public PermissionType getPermissionType() {
        return type;
    }

    public String getNode() {
        return node;
    }
}
