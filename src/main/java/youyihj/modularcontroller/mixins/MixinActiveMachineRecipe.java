package youyihj.modularcontroller.mixins;

import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import youyihj.modularcontroller.event.MachineRecipeEventFactory;
import youyihj.modularcontroller.util.IRecipeCraftingContextPatch;

import java.util.List;
import java.util.Map;

/**
 * @author youyihj
 */
@Mixin(value = ActiveMachineRecipe.class, remap = false)
public abstract class MixinActiveMachineRecipe {
    @Shadow
    @Final
    private MachineRecipe recipe;

    @Shadow
    private int tick;

    private Map<RequirementType<?, ?>, List<RecipeModifier>> currentModifiers;

    @Inject(method = "complete", at = @At("RETURN"))
    private void postCompleteEvent(RecipeCraftingContext completionContext, CallbackInfo ci) {
        TileMachineController controller = completionContext.getMachineController();
        MachineRecipeEventFactory.onCompleted(recipe, recipe.getOwningMachine(), controller.getPos(), controller.getWorld());
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lhellfirepvp/modularmachinery/common/crafting/ActiveMachineRecipe;tick:I",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void postTickEvent(TileMachineController ctrl, RecipeCraftingContext context, CallbackInfoReturnable<TileMachineController.CraftingStatus> cir) {
        String s = MachineRecipeEventFactory.onTick(ctrl, recipe, tick);
        if (s != null) {
            tick = 0;
            cir.setReturnValue(TileMachineController.CraftingStatus.failure(s));
        }
    }

    @Inject(method = "start", at = @At("RETURN"))
    private void setCurrentModifiersOnStarting(RecipeCraftingContext context, CallbackInfo ci) {
        currentModifiers = ((IRecipeCraftingContextPatch) context).getAllModifiers();
    }

    @Inject(method = "complete", at = @At("HEAD"))
    private void putCurrentModifiersOnCompleted(RecipeCraftingContext completionContext, CallbackInfo ci) {
        ((IRecipeCraftingContextPatch) completionContext).setModifiers(currentModifiers);
    }
}
