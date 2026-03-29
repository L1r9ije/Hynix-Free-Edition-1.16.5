package su.hynix.ui.gui.autobuy;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import su.hynix.modules.api.constructors.impl.ItemSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ItemList {
    private static final List<ItemSetting> ITEMS = new ArrayList<>();

    static {
        //сферы
        add(new ItemSetting(Items.PLAYER_HEAD.getDefaultInstance(), "Сфера цербера", false).addNBTparametr("Урон V", "Спешка I"));
        add(new ItemSetting(Items.PLAYER_HEAD.getDefaultInstance(), "Легендарная сфера", false).addNBTparametr("Скорость II", "Урон II"));
        add(new ItemSetting(Items.PLAYER_HEAD.getDefaultInstance(), "Легендарная сфера", false).addNBTparametr("Урон III"));
        add(new ItemSetting(Items.PLAYER_HEAD.getDefaultInstance(), "Сфера stinger", false).addNBTparametr("Скорость I", "Броня II", "Урон II"));
        add(new ItemSetting(Items.PLAYER_HEAD.getDefaultInstance(), "Сфера флеша", false).addNBTparametr("Броня I", "Скорость III"));
        add(new ItemSetting(Items.PLAYER_HEAD.getDefaultInstance(), "Сфера eternity", false).addNBTparametr("Скорость II", "Броня II", "Урон II"));
        //талики
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Талисман infinity", false).addNBTparametr("Макс. здоровье II", "Урон II", "Скорость II", "Броня II"));
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Легендарный талисман", false).addNBTparametr("Броня II", "Урон II"));
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Эпический талисман", false).addNBTparametr("Урон II"));
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Легендарный талисман", false).addNBTparametr("Урон III"));
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Талисман stinger", false).addNBTparametr("Скорость I", "Броня II", "Урон II"));
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Талисман eternity", false).addNBTparametr("Скорость II", "Броня II", "Урон II"));
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Талисман Сатиры", false).addNBTparametr("Спешка II", "Урон III"));
        //прочая хуета
        add(new ItemSetting(Items.COMPASS.getDefaultInstance(), "Особый компас", false).addNBTparametr("ведёт к ближайшему или рандомному сокровищу,", "после нажатия ПКМ, держа предмет в руках;", "можно использовать раз в 8 часов."));
        add(new ItemSetting(Items.TNT.getDefaultInstance(), "Динамит Б2", false).addNBTparametr("взрывает практически все блоки", "в радиусе 12 блоков;", "не работает на заприваченных", "территориях."));
        add(new ItemSetting(Items.TNT.getDefaultInstance(), "Динамит В", false).searchQuery("Динамит").addNBTparametr("имеет в 10 раз больший радиус взрыва."));
        add(new ItemSetting(Items.TNT.getDefaultInstance(), "с4 взрывчатка", false).addNBTparametr("разрушает блок незеритового привата;", "взрывает блоки обсидиана."));
        add(new ItemSetting(Items.TNT.getDefaultInstance(), "Разрывная волна", false).addNBTparametr("разрушает взрывом обсидиан и", "любой блок привата", "работает в воде"));
        add(new ItemSetting(Items.TNT.getDefaultInstance(), "Надёжный стиллер", false).addNBTparametr("после взрыва выпадает", "спавнер с мобом шансом в 75%"));
        add(new ItemSetting(Items.DISPENSER.getDefaultInstance(), "ТНТ-пушка", false).addNBTparametr("запускает летящий динамит", "со скоростью до 5 блоков за секунду;", "при запуске, сохраняет свойства", "особых динамитов и пиротехники."));


        //уникальные предметы
        add(new ItemSetting(Items.NETHERITE_PICKAXE.getDefaultInstance(), "Мечта шахтера", false).searchQuery("незеритовая кирка")
                .addNBTparametr("Прочность X"));
        add(new ItemSetting(Items.NETHERITE_SWORD.getDefaultInstance(), "Меч выгодный фарм", false)
                .addNBTparametr("при убийстве мобов выпадает", "в 2 раза больше опыта"));
        add(new ItemSetting(Items.TNT.getDefaultInstance(), "Разрывная волна", false)
                .addNBTparametr("разрушает взрывом обсидиан и", "любой блок привата", "работает в воде"));
        add(new ItemSetting(Items.GOLDEN_PICKAXE.getDefaultInstance(), "Золотая кирка Джейка", false)
                .addNBTparametr("сломав спавнер этой киркой,", "он выпадет, сохранив моба внутри,", "после чего кирка сломается."));
        add(new ItemSetting(Items.CREEPER_SPAWN_EGG.getDefaultInstance(), "Загадочное яйцо призыва", false)
                .addNBTparametr("Брутальный пиглин — 33.0%",
                        "Крипер — 2.0%",
                        "Блейз — 17.5%",
                        "Зомби — 17.5%",
                        "Скелет — 30.0%"));
        add(new ItemSetting(Items.field_242399_ol.getDefaultInstance(), "Загадочное яйцо призыва", false)
                .addNBTparametr("Брутальный пиглин — 50.0%",
                        "Мини-зомби — 20.0%",
                        "Блейз — 25.0%",
                        "Крипер — 1.0%",
                        "Ведьма — 4.0%"));
        add(new ItemSetting(Items.WITCH_SPAWN_EGG.getDefaultInstance(), "Загадочное яйцо призыва", false)
                .addNBTparametr("Брутальный пиглин — 25.0%",
                        "Ведьма — 7.0%",
                        "Скелет — 30.0%",
                        "Блейз — 20.0%",
                        "Зомби — 18.0%"));
        add(new ItemSetting(Items.SPAWNER.getDefaultInstance(), "Загадочный спавнер", false)
                .addNBTparametr("Брутальный пиглин — 33.0%",
                        "Крипер — 2.0%",
                        "Блейз — 17.5%",
                        "Зомби — 17.5%",
                        "Скелет — 30.0%"));
        add(new ItemSetting(Items.SPAWNER.getDefaultInstance(), "Загадочный спавнер", false)
                .addNBTparametr("Брутальный пиглин — 50.0%",
                        "Ведьма — 4.0%",
                        "Мини-зомби — 20.0%",
                        "Крипер — 1.0%",
                        "Блейз — 25.0%"));
        add(new ItemSetting(Items.SPAWNER.getDefaultInstance(), "Загадочный спавнер", false)
                .addNBTparametr("Брутальный пиглин — 25.0%",
                        "Ведьма — 7.0%",
                        "Блейз — 20.0%",
                        "Зомби — 18.0%",
                        "Скелет — 30.0%"));
        add(new ItemSetting(Items.GOLDEN_HELMET.getDefaultInstance(), "Шлем солнца", false)
                .addNBTparametr("Непробиваемый I")
                .requireUnbreakable()
                .addEnchantment(Enchantments.AQUA_AFFINITY, 1)
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.RESPIRATION, 3)
        );
        add(new ItemSetting(Items.ELYTRA.getDefaultInstance(), "нерушимые элитры", false)
                .requireUnbreakable()
        );
        add(new ItemSetting(Items.ANCIENT_DEBRIS.getDefaultInstance(), "Уникальный приват", false)
                .addNBTparametr("имеет свойства незеритового региона;", "невозможно найти через особый компас;", "имеет максимальный 4 уровень прочности.")
        );
        add(new ItemSetting(Items.NETHERITE_PICKAXE.getDefaultInstance(), "ДыРяВая кИркА", false)
                .addNBTparametr("разрушает массив блоков", "размерами 5х5 и 3 блока глубиной;", "не работает на всех автошахтах.")
        );
        add(new ItemSetting(Items.NETHERITE_SWORD.getDefaultInstance(), "Мечта вупсеня", false)
                .requireUnbreakable()
        );
        add(new ItemSetting(Items.TRIDENT.getDefaultInstance(), "Громовержец", false)
                .addEnchantment(Enchantments.IMPALING, 5)
                .addEnchantment(Enchantments.LOOTING, 5)
                .addEnchantment(Enchantments.LOYALTY, 3)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.ELYTRA.getDefaultInstance(), "Броневая элитра", false)
                .addNBTparametr("имеет свойства алмаз. нагрудника", "возможно зачаровывать на", "починку и прочность")
        );


        //eternity
        add(new ItemSetting(Items.NETHERITE_AXE.getDefaultInstance(), "Топор eternity", false)
                .addNBTparametr("Неразрушимость I")
                .addEnchantment(Enchantments.EFFICIENCY, 7)
                .addEnchantment(Enchantments.FORTUNE, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.BOW.getDefaultInstance(), "Лук eternity", false)
                .addNBTparametr("Оглушение II")
                .addEnchantment(Enchantments.FLAME, 1)
                .addEnchantment(Enchantments.INFINITY, 1)
                .addEnchantment(Enchantments.POWER, 5)
                .addEnchantment(Enchantments.PUNCH, 2)
                .addEnchantment(Enchantments.UNBREAKING, 3)
        );
        add(new ItemSetting(Items.CROSSBOW.getDefaultInstance(), "Арбалет eternity", false)
                .addNBTparametr("Оглушение II")
                .addEnchantment(Enchantments.MULTISHOT, 1)
                .addEnchantment(Enchantments.PIERCING, 5)
                .addEnchantment(Enchantments.QUICK_CHARGE, 3)
                .addEnchantment(Enchantments.UNBREAKING, 3)
        );
        add(new ItemSetting(Items.NETHERITE_PICKAXE.getDefaultInstance(), "Кирка eternity", false)
                .addNBTparametr("Неразрушимость I", "Опытный III", "Бур II", "Автоплавка I", "Магнетизм I")
                .addEnchantment(Enchantments.EFFICIENCY, 10)
                .addEnchantment(Enchantments.FORTUNE, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_SWORD.getDefaultInstance(), "Меч eternity", false)
                .addNBTparametr("Разрушитель II", "Богач I", "Критический II")
                .addEnchantment(Enchantments.BANE_OF_ARTHROPODS, 7)
                .addEnchantment(Enchantments.FIRE_ASPECT, 2)
                .addEnchantment(Enchantments.LOOTING, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.SHARPNESS, 7)
                .addEnchantment(Enchantments.SMITE, 7)
                .addEnchantment(Enchantments.SWEEPING, 3)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_SHOVEL.getDefaultInstance(), "Лопата eternity", false)
                .addNBTparametr("Бур II")
                .addEnchantment(Enchantments.EFFICIENCY, 5)
                .addEnchantment(Enchantments.FORTUNE, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_HELMET.getDefaultInstance(), "Шлем eternity", false)
                .addNBTparametr("Непробиваемый I")
                .addEnchantment(Enchantments.AQUA_AFFINITY, 1)
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.RESPIRATION, 3)
                .addEnchantment(Enchantments.THORNS, 3)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_CHESTPLATE.getDefaultInstance(), "Нагрудник eternity", false)
                .addNBTparametr("Непробиваемый I")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_LEGGINGS.getDefaultInstance(), "Штаны eternity", false)
                .addNBTparametr("Непробиваемый I")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.THORNS, 3)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_BOOTS.getDefaultInstance(), "Ботинки eternity", false)
                .addNBTparametr("Непробиваемый I")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.DEPTH_STRIDER, 3)
                .addEnchantment(Enchantments.FEATHER_FALLING, 4)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.SOUL_SPEED, 3)
                .addEnchantment(Enchantments.THORNS, 3)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        //


        //infinity
        add(new ItemSetting(Items.NETHERITE_HELMET.getDefaultInstance(), "Шлем infinity", false)
                .addNBTparametr("Непробиваемый II")
                .addEnchantment(Enchantments.AQUA_AFFINITY, 1)
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 2)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.RESPIRATION, 3)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_CHESTPLATE.getDefaultInstance(), "Нагрудник infinity", false)
                .addNBTparametr("Непробиваемый II")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 2)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_LEGGINGS.getDefaultInstance(), "Штаны infinity", false)
                .addNBTparametr("Непробиваемый II")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 2)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        add(new ItemSetting(Items.NETHERITE_BOOTS.getDefaultInstance(), "Ботинки infinity", false)
                .addNBTparametr("Непробиваемый II")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 5)
                .addEnchantment(Enchantments.DEPTH_STRIDER, 3)
                .addEnchantment(Enchantments.FEATHER_FALLING, 4)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 5)
                .addEnchantment(Enchantments.MENDING, 2)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 5)
                .addEnchantment(Enchantments.PROTECTION, 5)
                .addEnchantment(Enchantments.SOUL_SPEED, 3)
                .addEnchantment(Enchantments.UNBREAKING, 5)
        );
        //


        //stinger
        add(new ItemSetting(Items.BOW.getDefaultInstance(), "Лук stinger", false)
                .addNBTparametr("Оглушение II")
                .addEnchantment(Enchantments.FLAME, 1)
                .addEnchantment(Enchantments.INFINITY, 1)
                .addEnchantment(Enchantments.POWER, 4)
                .addEnchantment(Enchantments.PUNCH, 2)
                .addEnchantment(Enchantments.UNBREAKING, 3)
        );
        add(new ItemSetting(Items.NETHERITE_SWORD.getDefaultInstance(), "Меч stinger", false)
                .addNBTparametr("Критический II", "Богач I")
                .addEnchantment(Enchantments.BANE_OF_ARTHROPODS, 7)
                .addEnchantment(Enchantments.FIRE_ASPECT, 2)
                .addEnchantment(Enchantments.LOOTING, 5)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.SHARPNESS, 7)
                .addEnchantment(Enchantments.SMITE, 7)
                .addEnchantment(Enchantments.SWEEPING, 3)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        add(new ItemSetting(Items.NETHERITE_PICKAXE.getDefaultInstance(), "Кирка stinger", false)
                .addNBTparametr("Неразрушимость I", "Автоплавка I", "Опытный III", "Бур I")
                .addEnchantment(Enchantments.EFFICIENCY, 8)
                .addEnchantment(Enchantments.FORTUNE, 4)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        add(new ItemSetting(Items.NETHERITE_AXE.getDefaultInstance(), "Топор stinger", false)
                .addNBTparametr("Неразрушимость I")
                .addEnchantment(Enchantments.EFFICIENCY, 6)
                .addEnchantment(Enchantments.FORTUNE, 4)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        add(new ItemSetting(Items.NETHERITE_SHOVEL.getDefaultInstance(), "Лопата stinger", false)
                .addNBTparametr("Бур I")
                .addEnchantment(Enchantments.EFFICIENCY, 5)
                .addEnchantment(Enchantments.FORTUNE, 4)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        add(new ItemSetting(Items.CROSSBOW.getDefaultInstance(), "Арбалет stinger", false)
                .addNBTparametr("Оглушение II")
                .addEnchantment(Enchantments.MULTISHOT, 1)
                .addEnchantment(Enchantments.PIERCING, 4)
                .addEnchantment(Enchantments.QUICK_CHARGE, 3)
                .addEnchantment(Enchantments.UNBREAKING, 3)
        );
        add(new ItemSetting(Items.NETHERITE_HELMET.getDefaultInstance(), "Шлем stinger", false)
                .addEnchantment(Enchantments.AQUA_AFFINITY, 1)
                .addEnchantment(Enchantments.BLAST_PROTECTION, 4)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 4)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 4)
                .addEnchantment(Enchantments.PROTECTION, 4)
                .addEnchantment(Enchantments.RESPIRATION, 3)
                .addEnchantment(Enchantments.THORNS, 3)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        add(new ItemSetting(Items.NETHERITE_CHESTPLATE.getDefaultInstance(), "Нагрудник stinger", false)
                .addNBTparametr("Непробиваемый I")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 4)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 4)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 4)
                .addEnchantment(Enchantments.PROTECTION, 4)
                .addEnchantment(Enchantments.THORNS, 3)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        add(new ItemSetting(Items.NETHERITE_LEGGINGS.getDefaultInstance(), "Штаны stinger", false)
                .addNBTparametr("Непробиваемый I")
                .addEnchantment(Enchantments.BLAST_PROTECTION, 4)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 4)
                .addEnchantment(Enchantments.MENDING, 1)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 4)
                .addEnchantment(Enchantments.PROTECTION, 4)
                .addEnchantment(Enchantments.THORNS, 3)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        add(new ItemSetting(Items.NETHERITE_BOOTS.getDefaultInstance(), "Ботинки stinger", false)
                .addEnchantment(Enchantments.BLAST_PROTECTION, 4)
                .addEnchantment(Enchantments.DEPTH_STRIDER, 3)
                .addEnchantment(Enchantments.FEATHER_FALLING, 4)
                .addEnchantment(Enchantments.FIRE_PROTECTION, 4)
                .addEnchantment(Enchantments.MENDING, 2)
                .addEnchantment(Enchantments.PROJECTILE_PROTECTION, 4)
                .addEnchantment(Enchantments.PROTECTION, 4)
                .addEnchantment(Enchantments.SOUL_SPEED, 3)
                .addEnchantment(Enchantments.THORNS, 3)
                .addEnchantment(Enchantments.UNBREAKING, 4)
        );
        //

        //Helper
        add(new ItemSetting(Items.EXPERIENCE_BOTTLE.getDefaultInstance(), "Пузырёк опыта", false));
        add(new ItemSetting(Items.POPPED_CHORUS_FRUIT.getDefaultInstance(), "Трапка", false).addNBTparametr("при взрыве наносит урон", "в радиусе 3 блоков и создаёт", "коробку в месте активации."));
        add(new ItemSetting(Items.PRISMARINE_SHARD.getDefaultInstance(), "Взрывная трапка", false).addNBTparametr("при взрыве наносит урон в радиусе 3 блоков", "и создаёт воронку в земле."));
        add(new ItemSetting(Items.FIREWORK_STAR.getDefaultInstance(), "Прощальный гул", false).addNBTparametr("Отталкивает всех игроков", "в радиусе 5 блоков."));
        add(new ItemSetting(Items.FIRE_CHARGE.getDefaultInstance(), "Взрывная штучка", false).addNBTparametr("при нажатии ПКМ вызывает взрыв;", "может убить игрока без брони на", "расстоянии 3 блоков от активации;", "урон получает и сам активатор."));
        add(new ItemSetting(Items.NETHER_STAR.getDefaultInstance(), "Стан", false).addNBTparametr("создает куб (30x30x30) на 15 секунд", "игроки в нем не могут использовать", "эндер-жемчуги и хорусы и накладывает", "эффект Замедление 1 на всех, кроме", "активатора."));
        add(new ItemSetting(Items.SNOWBALL.getDefaultInstance(), "Ком снега", false).addNBTparametr("при попадании накладывает слепоту и", "медлительность VI на 10 секунд;", "можно использовать раз в 30 секунд."));
        //

        //default items
        add(new ItemSetting(Items.ELYTRA.getDefaultInstance(), "Элитры", false));
        add(new ItemSetting(Items.TOTEM_OF_UNDYING.getDefaultInstance(), "Тотем бессмертия", false));
        add(new ItemSetting(Items.GOLDEN_APPLE.getDefaultInstance(), "Золотое яблоко", false));
        add(new ItemSetting(Items.ENCHANTED_GOLDEN_APPLE.getDefaultInstance(), "Зачарованное золотое яблоко", false));
        add(new ItemSetting(Items.GOLDEN_CARROT.getDefaultInstance(), "Золотая морковь", false));
        add(new ItemSetting(Items.DIAMOND.getDefaultInstance(), "Алмаз", false));
        add(new ItemSetting(Items.IRON_INGOT.getDefaultInstance(), "Железный слиток", false));
        add(new ItemSetting(Items.GOLD_INGOT.getDefaultInstance(), "Золотой слиток", false));
        add(new ItemSetting(Items.EMERALD.getDefaultInstance(), "Изумруд", false));
        add(new ItemSetting(Items.NETHERITE_INGOT.getDefaultInstance(), "Незеритовый слиток", false));
        add(new ItemSetting(Items.NETHERITE_SCRAP.getDefaultInstance(), "Незеритовый лом", false));
        add(new ItemSetting(Items.IRON_BLOCK.getDefaultInstance(), "Железный блок", false));
        add(new ItemSetting(Items.GOLD_BLOCK.getDefaultInstance(), "Золотой блок", false));
        add(new ItemSetting(Items.DIAMOND_BLOCK.getDefaultInstance(), "Алмазный блок", false));
        add(new ItemSetting(Items.EMERALD_BLOCK.getDefaultInstance(), "Изумрудный блок", false));
        add(new ItemSetting(Items.ANCIENT_DEBRIS.getDefaultInstance(), "Древние обломки", false));
        add(new ItemSetting(Items.BEACON.getDefaultInstance(), "Маяк", false));
        add(new ItemSetting(Items.ENDER_PEARL.getDefaultInstance(), "Эндер-жемчуг", false));
        add(new ItemSetting(Items.FIREWORK_ROCKET.getDefaultInstance(), "Фейерверк", false));
        add(new ItemSetting(Items.CHORUS_FRUIT.getDefaultInstance(), "Хорус", false));
        add(new ItemSetting(Items.SHULKER_BOX.getDefaultInstance(), "Шалкеровый ящик", false));
        add(new ItemSetting(Items.DRAGON_HEAD.getDefaultInstance(), "Голова дракона", false));
        add(new ItemSetting(Items.DRAGON_EGG.getDefaultInstance(), "Яйцо дракона", false));
        //
    }

    private static void add(ItemSetting setting) {
        ITEMS.add(setting);
    }

    public static List<ItemSetting> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static ItemSetting getByName(String name) {
        if (name == null) return null;
        for (ItemSetting item : ITEMS) {
            if (item.getName().equalsIgnoreCase(name)) return item;
        }
        return null;
    }
}
