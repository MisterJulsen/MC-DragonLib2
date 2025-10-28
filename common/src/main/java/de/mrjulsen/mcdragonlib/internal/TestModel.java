package de.mrjulsen.mcdragonlib.internal;

import org.joml.Vector3f;

import com.mojang.math.Axis;

import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.BasicMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TestModel extends DLModel {

    @Override
    protected Mesh getMesh(ModelType type, BakedModel originalModel, BlockState state, RandomSource random, ModelContext context) {
        Mesh mesh = BasicMesh.fromBlock(Blocks.GOLD_BLOCK.defaultBlockState(), random);
        mesh.rotate(Axis.YP.rotationDegrees(45), new Vector3f(0.5f));
        return mesh;
    }
    
}
