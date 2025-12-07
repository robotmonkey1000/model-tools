package studio.robotmonkey.model_tools.mixin.client;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("age")
    void setAge(int age);

    @Accessor("age")
    int getAge();

}
