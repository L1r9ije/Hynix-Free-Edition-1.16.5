package su.hynix.modules;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import su.hynix.events.EventKey;
import su.hynix.hynix;
import su.hynix.managers.impl.notificationmanager.NotificationManager;
import su.hynix.modules.api.constructors.Setting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.impl.combat.*;
import su.hynix.modules.impl.miscellaneous.*;
import su.hynix.modules.impl.movement.*;
import su.hynix.modules.impl.player.*;
import su.hynix.modules.impl.visuals.*;
import su.hynix.ui.Interface.elements.impl.NotificationRender;
import su.hynix.utils.Wrapper;
import su.hynix.utils.misc.SoundUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ModuleManager implements Wrapper {
    private final List<Module> modules = new CopyOnWriteArrayList<>();
    public AttackAura attackAura;
    public SwordAnimations swordAnimations;
    public ViewModel viewModel;
    public Removals removals;
    public AspectRatio aspectRatio;
    public Ambience ambience;
    public ElytraTarget elytraTarget;
    public OpenWalls openWalls;
    public SantaHat santaHat;
    public ChatHelper chatHelper;

    public void init() {


        // Combat
        this.attackAura = new AttackAura();
        add(attackAura);
        add(new AutoTotem());
        add(new AntiBot());
        add(new AutoExplosion());
        add(new NoFriendDamage());
        add(new AutoSwap());
        add(new NoEntityTrace());
        add(new HitBox());
        add(new CrystalOptimizer());
        //  add(new AutoTrap());
        add(new NoSlotChange());
        add(new NoVelocity());
        add(new PacketCriticals());
        add(new FakeLag());


        // Movement
        add(new Sprint());
        add(new NoSlow());
        add(new SuperFirework());
        add(new AutoJump());
        add(new FreeCamera());
        add(new AirStuck());
        add(new NoJumpDelay());
        add(new NoPush());
        add(new GuiMove());
        add(new Speed());
        add(new NoWeb());
        add(new Flight());
        add(new ElytraMotion());


        // Player
        add(new ClickPearl());
//        add(new AutoEat());
//        add(new AutoFish());
        add(new AutoRespawn());
        //  add(new AntiAFK());
        add(new FunTimeHelper());
        //   add(new TestFunction());
        add(new ItemsCooldown());
        add(new FastExp());
        add(new ItemScroller());
        add(new AutoPotion());
        add(new AutoTool());
        add(new LockSlot());
        add(new FastBreak());
        add(new AutoArmor());
        add(new AutoLeave());
        //  add(new Nuker());
        add(new AutoJoiner());
        add(new ChestStealer());
        add(new NoInteract());
        //    add(new ItemRelease());


        // Visuals
        this.ambience = new Ambience();
        add(ambience);
        this.viewModel = new ViewModel();
        add(viewModel);
        this.swordAnimations = new SwordAnimations();
        add(swordAnimations);
        this.removals = new Removals();
        add(removals);
        this.aspectRatio = new AspectRatio();
        add(aspectRatio);
        this.santaHat = new SantaHat();
        add(santaHat);
        add(new ArmorDurabilityView());
        add(new ExtendedTab());
        add(new ItemPhysics());
        add(new ChinaHat());
        add(new ThirdPerson());
        add(new Prediction());
        add(new Tags());
        add(new Arrows());
        add(new SRPSpoofer());
        add(new FullBright());
        add(new Interface());
        add(new Tracers());
        add(new Trails());
        add(new SeeInvisibles());
        add(new ShulkerPreview());
        add(new Hands());
        add(new Animation());
        add(new FireworkESP());
        add(new Crosshair());
        //add(new Particles());


        // Miscellaneous
        add(new ElytraHelper());
        add(new AutoAccept());
        add(new ItemHelper());
        add(new HolyWorldHelper());
        add(new ClickFriend());
        add(new NameProtect());
        add(new VoiceChat());
        add(new PotionTracker());
        this.openWalls = new OpenWalls();
        add(this.openWalls);
        add(new UseTracker());
        add(new ToggleSounds());
        add(new ScoreboardHealth());
        add(new InventoryPlus());
        add(new ReallyWorldHelper());
        add(new TapeMouse());
        add(new AutoContract());
        add(new AutoDuel());
        this.chatHelper = new ChatHelper();
        add(this.chatHelper);


// TODO
//        add(new HolyWorldHelper());
//        add(new AuctionHelper());
        add(new BlockESP());
//        this.elytraTarget = new ElytraTarget();
//        add(elytraTarget);
        add(new Particles());
        add(new JumpCircle());

        modules.sort(Comparator.comparing(Module::getName));
        EventManager.register(this);
    }

    private void add(Module module) {
        modules.add(module);
    }

    public Module getModule(Class<? extends Module> classModule) {
        for (Module module : modules) {
            if (module != null && module.getClass() == classModule) {
                return module;
            }
        }
        return null;
    }

    public Optional<Module> findModuleByName(String name) {
        return modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst();
    }

    @EventTarget
    private void onKey(EventKey e) {
        if (e.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            mc.displayGuiScreen(hynix.getInstance().getDropDown());
        }
        for (Module module : modules) {
            if (module.getBind() == e.getKey()) {
                if ((module.getToggleMode() == Module.ToggleMode.TOGGLE && !e.isHold()) || module.getToggleMode() == Module.ToggleMode.HOLD) {
                    module.toggle();
                }
            }
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof BooleanSetting booleanSetting && booleanSetting.getBind() == e.getKey()) {
                    boolean isToggleMode = booleanSetting.getToggleMode() == Module.ToggleMode.TOGGLE;
                    if (!isToggleMode || !e.isHold()) {
                        boolean newValue = !booleanSetting.get();
                        booleanSetting.set(newValue);
                        if (!Module.isSuppressToggleEffects() && booleanSetting.isKeybindvisible()) {
                            SoundUtil.playSound(ToggleSounds.getSoundFile(newValue));
                            if (NotificationRender.module.get()) {
                                NotificationManager.addNotification(newValue ? "J" : "K", "«" + booleanSetting.getName() + "»" + (newValue ? " включен!" : " выключен!"), -1);
                            }
                        }
                    }
                }
            }
        }
    }
}