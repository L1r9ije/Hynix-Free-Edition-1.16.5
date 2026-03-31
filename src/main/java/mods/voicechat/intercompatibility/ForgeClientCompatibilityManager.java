package mods.voicechat.intercompatibility;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import mods.voicechat.eventforge.ClientPlayerNetworkEvent;
import mods.voicechat.eventforge.InputEvent;
import mods.voicechat.eventforge.TickEvent;
import mods.voicechat.eventforge.WorldEvent;
import mods.voicechat.events.ClientVoiceChatConnectedEvent;
import mods.voicechat.events.ClientVoiceChatDisconnectedEvent;
import mods.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.integrated.IntegratedServer;
import su.hynix.events.EventRender2D;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ForgeClientCompatibilityManager extends ClientCompatibilityManager {

    private final Minecraft minecraft;

    private final List<RenderNameplateEvent> renderNameplateEvents;
    private final List<RenderHUDEvent> renderHUDEvents;
    private final List<KeyboardEvent> keyboardEvents;
    private final List<MouseEvent> mouseEvents;
    private final List<Runnable> clientTickEvents;
    private final List<Runnable> inputEvents;
    private final List<Runnable> disconnectEvents;
    private final List<Runnable> joinWorldEvents;
    private final List<Consumer<ClientVoicechatConnection>> voicechatConnectEvents;
    private final List<Runnable> voicechatDisconnectEvents;
    private final List<Consumer<Integer>> publishServerEvents;
    private boolean wasPublished;

    public ForgeClientCompatibilityManager() {
        minecraft = Minecraft.getInstance();
        renderNameplateEvents = new CopyOnWriteArrayList<>();
        renderHUDEvents = new CopyOnWriteArrayList<>();
        keyboardEvents = new CopyOnWriteArrayList<>();
        mouseEvents = new CopyOnWriteArrayList<>();
        clientTickEvents = new CopyOnWriteArrayList<>();
        inputEvents = new CopyOnWriteArrayList<>();
        disconnectEvents = new CopyOnWriteArrayList<>();
        joinWorldEvents = new CopyOnWriteArrayList<>();
        voicechatConnectEvents = new CopyOnWriteArrayList<>();
        voicechatDisconnectEvents = new CopyOnWriteArrayList<>();
        publishServerEvents = new CopyOnWriteArrayList<>();
    }

    @EventTarget
    public void onRenderName(mods.voicechat.eventforge.RenderNameplateEvent event) {
        renderNameplateEvents.forEach(renderNameplateEvent -> renderNameplateEvent.render(event.getEntity(), event.getContent(), event.getStack(), event.getRenderTypeBuffer(), event.getPackedLight()));
        if (minecraft.player == null || event.getEntity().isInvisibleToPlayer(minecraft.player)) {
            return;
        }
        renderNameplateEvents.forEach(renderNameplateEvent -> renderNameplateEvent.render(event.getEntity(), event.getContent(), event.getStack(), event.getRenderTypeBuffer(), event.getPackedLight()));
    }

    @EventTarget
    public void onRenderOverlay(EventRender2D event) {
//        if (!RenderGameOverlayEvent.ElementType.HOTBAR.equals(event.getType())) {
//            return;
//        }
        renderHUDEvents.forEach(renderHUDEvent -> renderHUDEvent.render(event.getStack(), event.getPartialTicks()));
    }

    @EventTarget
    public void onKey(InputEvent.KeyInputEvent event) {
        keyboardEvents.forEach(keyboardEvent -> keyboardEvent.onKeyboardEvent(minecraft.getMainWindow().getHandle(), event.getKey(), event.getScanCode()));
    }

    @EventTarget
    public void onMouse(InputEvent.RawMouseEvent event) {
        mouseEvents.forEach(mouseEvent -> mouseEvent.onMouseEvent(minecraft.getMainWindow().getHandle(), event.getButton(), event.getAction(), event.getMods()));
    }

    @EventTarget
    public void onKeyInput(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        clientTickEvents.forEach(Runnable::run);
    }

    @EventTarget
    public void onInput(TickEvent.ClientTickEvent event) {
        inputEvents.forEach(Runnable::run);
    }

    @EventTarget
    public void onDisconnect(WorldEvent.Unload event) {
        if (minecraft.playerController == null) {
            disconnectEvents.forEach(Runnable::run);
        }
    }

    @EventTarget
    public void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
        if (event.getPlayer() != minecraft.player) {
            return;
        }
        joinWorldEvents.forEach(Runnable::run);
    }

    @EventTarget
    public void onServer(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }
        IntegratedServer server = Minecraft.getInstance().getIntegratedServer();
        if (server == null) {
            return;
        }

        boolean published = server.getPublic();

        if (published && !wasPublished) {
            publishServerEvents.forEach(portConsumer -> portConsumer.accept(server.getServerPort()));
        }

        wasPublished = published;
    }

    @Override
    public void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate) {
        renderNameplateEvents.add(onRenderNamePlate);
    }

    @Override
    public void onRenderHUD(RenderHUDEvent onRenderHUD) {
        renderHUDEvents.add(onRenderHUD);
    }

    @Override
    public void onKeyboardEvent(KeyboardEvent onKeyboardEvent) {
        keyboardEvents.add(onKeyboardEvent);
    }

    @Override
    public void onMouseEvent(MouseEvent onMouseEvent) {
        mouseEvents.add(onMouseEvent);
    }

    @Override
    public void onClientTick(Runnable onClientTick) {
        clientTickEvents.add(onClientTick);
    }

    @Override
    public InputMappings.Input getBoundKeyOf(KeyBinding keyBinding) {
        return keyBinding.getKey();
    }

    @Override
    public void onHandleKeyBinds(Runnable onHandleKeyBinds) {
        inputEvents.add(onHandleKeyBinds);
    }

    @Override
    public KeyBinding registerKeyBinding(KeyBinding keyBinding) {
        return keyBinding;
    }

    @Override
    public void emitVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(client));
        EventManager.call(new ClientVoiceChatConnectedEvent(client));
    }

    @Override
    public void emitVoiceChatDisconnectedEvent() {
        voicechatDisconnectEvents.forEach(Runnable::run);
        EventManager.call(new ClientVoiceChatDisconnectedEvent());
    }

    @Override
    public void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected) {
        voicechatConnectEvents.add(onVoiceChatConnected);
    }

    @Override
    public void onVoiceChatDisconnected(Runnable onVoiceChatDisconnected) {
        voicechatDisconnectEvents.add(onVoiceChatDisconnected);
    }

    @Override
    public void onDisconnect(Runnable onDisconnect) {
        disconnectEvents.add(onDisconnect);
    }

    @Override
    public void onJoinWorld(Runnable onJoinWorld) {
        joinWorldEvents.add(onJoinWorld);
    }

    @Override
    public void onPublishServer(Consumer<Integer> onPublishServer) {
        publishServerEvents.add(onPublishServer);
    }

    @Override
    public SocketAddress getSocketAddress(NetworkManager connection) {
        return connection.channel().remoteAddress();
    }

    @Override
    public void addResourcePackSource(ResourcePackList packRepository, IPackFinder repositorySource) {
        //packRepository.addPackFinder(repositorySource);
    }
}
