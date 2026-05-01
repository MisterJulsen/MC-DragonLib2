package de.mrjulsen.mcdragonlib.client.model;

import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import net.minecraft.client.resources.model.BakedModel;

public interface IDynamicBakedModel {
    BakedModel getOriginalModel();
    DLModel getModel();
}
