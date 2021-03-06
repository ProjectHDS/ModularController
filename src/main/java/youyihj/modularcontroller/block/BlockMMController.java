package youyihj.modularcontroller.block;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import youyihj.modularcontroller.util.ControllerInformation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class BlockMMController extends BlockController implements ItemDynamicColor {

    private final ControllerInformation information;
    private final List<String> machineIDs;

    public static final Map<String, BlockMMController> CONTROLLERS = new HashMap<>();
    public static final List<Item> CONTROLLER_ITEMS = new ArrayList<>();

    private BlockMMController(ControllerInformation information) {
        this.information = information;
        this.setRegistryName(information.getName());
        this.fullBlock = information.isFullBlock();
        this.lightOpacity = this.fullBlock ? 255 : 0;
        this.machineIDs = Lists.newArrayList(information.getMachineName());
    }

    public static BlockMMController getOrCreate(ControllerInformation information) {
        if (CONTROLLERS.containsKey(information.getName())) {
            BlockMMController existingController = CONTROLLERS.get(information.getName());
            existingController.machineIDs.add(information.getMachineName());
            return existingController;
        }
        BlockMMController controller = new BlockMMController(information);

        CONTROLLERS.put(information.getName(), controller);
        CONTROLLER_ITEMS.add(new ItemBlock(controller) {
            @Override
            public String getItemStackDisplayName(ItemStack stack) {
                return controller.getLocalizedName();
            }
        }.setRegistryName(information.getName()));

        return controller;
    }

    @Override
    public int getColorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        return information.getColor();
    }

    @Override
    public String getLocalizedName() {
        return information.getLocalizedName();
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        return information.getColor();
    }

    public List<DynamicMachine> getAssociatedMachines() {
        return machineIDs.stream()
                .map(id -> new ResourceLocation(ModularMachinery.MODID, id))
                .map(MachineRegistry.getRegistry()::getMachine)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return information.isFullBlock();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return information == null || information.isFullBlock();
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return information.getLightValue();
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return information.isEnableAlpha() ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.CUTOUT;
    }
}
