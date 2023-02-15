package com.calvinmt.powerstones.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class PowerstoneWireBlock extends RedstoneWireBlock {

    protected final Vec3d[] COLORS;

    public PowerstoneWireBlock(Settings settings, Vec3d[] colors) {
        super(settings);
        COLORS = colors;
    }

    public int getPowerStoneColor(int i) {
        Vec3d vec3d = COLORS[i];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    private void addPoweredParticles(World world, Random random, BlockPos pos, Vec3d color, Direction direction, Direction direction2, float f, float g) {
        float h = g - f;
        if (random.nextFloat() >= 0.2f * h) {
            return;
        }
        float i = 0.4375f;
        float j = f + h * random.nextFloat();
        double d = 0.5 + (double)(i * (float)direction.getOffsetX()) + (double)(j * (float)direction2.getOffsetX());
        double e = 0.5 + (double)(i * (float)direction.getOffsetY()) + (double)(j * (float)direction2.getOffsetY());
        double k = 0.5 + (double)(i * (float)direction.getOffsetZ()) + (double)(j * (float)direction2.getOffsetZ());
        world.addParticle(new DustParticleEffect(color.toVector3f(), 1.0f), (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + k, 0.0, 0.0, 0.0);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int i = state.get(POWER);
        if (i == 0) {
            return;
        }
        block4: for (Direction direction : Direction.Type.HORIZONTAL) {
            WireConnection wireConnection = (WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
            switch (wireConnection) {
                case UP: {
                    this.addPoweredParticles(world, random, pos, COLORS[i], direction, Direction.UP, -0.5f, 0.5f);
                }
                case SIDE: {
                    this.addPoweredParticles(world, random, pos, COLORS[i], Direction.DOWN, direction, 0.0f, 0.5f);
                    continue block4;
                }
                case NONE:
                    break;
                default:
                    break;
            }
            this.addPoweredParticles(world, random, pos, COLORS[i], Direction.DOWN, direction, 0.0f, 0.3f);
        }
    }

}
