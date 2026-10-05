package com.complexindustries.mekanism.client.particle;

import com.complexindustries.mekanism.registration.MCIParticleTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import mekanism.api.SerializationConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record UltravioletLaserParticleData(Direction direction, double distance, float energyScale) implements ParticleOptions {

    public static final MapCodec<UltravioletLaserParticleData> CODEC = RecordCodecBuilder.mapCodec(val -> val.group(
            Direction.CODEC.fieldOf(SerializationConstants.DIRECTION).forGetter(data -> data.direction),
            Codec.DOUBLE.fieldOf(SerializationConstants.DISTANCE).forGetter(data -> data.distance),
            Codec.FLOAT.fieldOf(SerializationConstants.ENERGY).forGetter(data -> data.energyScale)
    ).apply(val, UltravioletLaserParticleData::new));

    public static final StreamCodec<ByteBuf, UltravioletLaserParticleData> STREAM_CODEC = StreamCodec.composite(
            Direction.STREAM_CODEC, UltravioletLaserParticleData::direction,
            ByteBufCodecs.DOUBLE, UltravioletLaserParticleData::distance,
            ByteBufCodecs.FLOAT, UltravioletLaserParticleData::energyScale,
            UltravioletLaserParticleData::new
    );

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return MCIParticleTypes.ULTRAVIOLET_LASER.get();
    }
}
