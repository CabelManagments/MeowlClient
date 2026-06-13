package ru.meowl.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SoundManager {
    // Регистрируем твои звуки
    public static final SoundEvent HIT_SOUND = register("hit_sound"); // 5 (soft).ogg
    public static final SoundEvent KILL_SOUND = register("kill_sound"); // 5 (танк уничтожен).ogg
    public static final SoundEvent ENABLE_SOUND = register("enable_sound"); // 5 (bell).ogg
    public static final SoundEvent DISABLE_SOUND = register("disable_sound"); // 3 (bell).ogg

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of("meowldlc", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void playSound(SoundEvent sound) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.getSoundManager().play(PositionedSoundInstance.master(sound, 1.0F, 1.0F));
        }
    }
}

