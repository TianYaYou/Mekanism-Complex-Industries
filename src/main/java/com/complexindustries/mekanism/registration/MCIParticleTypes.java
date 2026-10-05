package com.complexindustries.mekanism.registration;

import com.complexindustries.mekanism.MCIConstants;
import com.complexindustries.mekanism.client.particle.UltravioletLaserParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MCIParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MCIConstants.MODID);

    public static final DeferredHolder<ParticleType<?>, UltravioletLaserParticleType> ULTRAVIOLET_LASER =
            PARTICLE_TYPES.register("ultraviolet_laser", UltravioletLaserParticleType::new);

    private MCIParticleTypes() {}
}
