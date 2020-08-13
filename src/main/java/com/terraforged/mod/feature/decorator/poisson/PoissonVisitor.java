package com.terraforged.mod.feature.decorator.poisson;

import com.terraforged.core.util.poisson.Poisson;
import com.terraforged.core.util.poisson.PoissonContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.Random;
import java.util.function.Consumer;

public class PoissonVisitor extends PoissonContext implements Poisson.Visitor, Consumer<Consumer<? super BlockPos>> {

    private final BlockPos pos;
    private final ISeedReader world;
    private final Poisson poisson;
    private final PoissonDecorator decorator;
    private final BlockPos.Mutable mutable = new BlockPos.Mutable();

    private final int chunkX;
    private final int chunkZ;

    private Consumer<? super BlockPos> consumer;

    public PoissonVisitor(ISeedReader world, PoissonDecorator decorator, Poisson poisson, Random random, BlockPos pos) {
        super(world.getSeed(), random);
        this.pos = pos;
        this.world = world;
        this.decorator = decorator;
        this.chunkX = pos.getX() >> 4;
        this.chunkZ = pos.getZ() >> 4;
        this.poisson = poisson;
    }

    @Override
    public void visit(int x, int z) {
        mutable.setPos(x, pos.getY(), z);
        mutable.setY(decorator.getYAt(world, mutable, random));
        consumer.accept(mutable);
    }

    @Override
    public void accept(Consumer<? super BlockPos> consumer) {
        this.consumer = consumer;
        this.poisson.visit(chunkX, chunkZ, this, this);
        this.consumer = null;
    }
}
