package com.complexindustries.mekanism.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class UltravioletLaserParticleType extends ParticleType<UltravioletLaserParticleData> {

    public UltravioletLaserParticleType() {
        super(false);
    }

    @Override
    public MapCodec<UltravioletLaserParticleData> codec() {
        return UltravioletLaserParticleData.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, UltravioletLaserParticleData> streamCodec() {
        return UltravioletLaserParticleData.STREAM_CODEC;
    }
}
