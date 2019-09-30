package buffer.screen;

import java.util.Arrays;
import java.util.List;

import buffer.inventory.BufferInventory;
import buffer.inventory.BufferInventory.BufferStack;
import buffer.registry.ItemRegistry;
import io.github.cottonmc.cotton.gui.CottonScreenController;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.LiteralText;

public class BufferBaseController extends CottonScreenController {
	public BufferInventory bufferInventory = new BufferInventory(1);

	protected WPlainPanel rootPanel = new WPlainPanel();

	protected WLabel labelOne = new WLabel("");
	protected WLabel labelTwo = new WLabel("");
	protected WLabel labelThree = new WLabel("");
	protected WLabel labelFour = new WLabel("");
	protected WLabel labelFive = new WLabel("");
	protected WLabel labelSix = new WLabel("");

	protected List<WItemSlot> controllerSlots = Arrays.asList(null, null, null, null, null, null);
	protected List<WLabel> controllerLabels = Arrays.asList(labelOne, labelTwo, labelThree, labelFour, labelFive, labelSix);

	protected static final int sectionX = 48;
	protected static final int sectionY = 20;

	public BufferBaseController(int syncId, PlayerInventory playerInventory, BlockContext context) {
		super(RecipeType.CRAFTING, syncId, playerInventory, getBlockInventory(context), getBlockPropertyDelegate(context));

		setRootPanel(rootPanel);
	}

	public static String withSuffix(long count) {
		if (count < 1000) return "" + count;
		int exp = (int) (Math.log(count) / Math.log(1000));
		return String.format("%.1f %c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp-1));
	}

	@Override
	public ItemStack onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity playerEntity) {
		Slot slot;
		if (slotNumber < 0 || slotNumber >= super.slotList.size()) {
			return ItemStack.EMPTY;
		} else {
			slot = super.slotList.get(slotNumber);
		}
		if (slot == null || !slot.canTakeItems(playerEntity)) {
			return ItemStack.EMPTY;
		} else {
			if (slot.getStack().getItem() == ItemRegistry.BUFFER_ITEM) {
				return ItemStack.EMPTY;
			}
			if (action == SlotActionType.QUICK_MOVE) {
				ItemStack quickStack;
				if (slot.inventory instanceof BufferInventory) {
					BufferStack bufferStack = bufferInventory.getSlot(slotNumber);
					bufferStack.restockStack(false);
					ItemStack wrappedStack = bufferStack.getStack();
					int amountToRemove = wrappedStack.getMaxCount();
					if (amountToRemove > wrappedStack.getCount()) {
						amountToRemove = wrappedStack.getCount();
					}
					ItemStack insertStack = wrappedStack.copy();
					wrappedStack.decrement(amountToRemove);
					insertStack.setCount(amountToRemove);
					if (playerEntity.inventory.insertStack(insertStack)) {
						return ItemStack.EMPTY;
					} else {
						return insertStack.copy();
					}
				} else {
					quickStack = bufferInventory.insertStack(slot.getStack().copy());
					this.setStackInSlot(slotNumber, quickStack.copy());
				}
				return quickStack;
			} else if (action == SlotActionType.PICKUP) {
				if (slot.inventory instanceof BufferInventory) {
					BufferStack bufferStack = bufferInventory.getSlot(slotNumber);
					if (playerEntity.inventory.getCursorStack().isEmpty() && !bufferStack.getStack().isEmpty()) {
							bufferStack.restockStack(false);
							final ItemStack wrappedStack = bufferStack.getStack().copy();
							playerEntity.inventory.setCursorStack(wrappedStack.copy());
							bufferStack.setStack(ItemStack.EMPTY);
					} else if (!playerEntity.inventory.getCursorStack().isEmpty() && !slot.hasStack()) {
						bufferInventory.getSlot(slotNumber).setStack(playerEntity.inventory.getCursorStack().copy());
						playerEntity.inventory.setCursorStack(ItemStack.EMPTY);
					} else if (!playerEntity.inventory.getCursorStack().isEmpty() && slot.hasStack()) {
						final ItemStack cursorStack = playerEntity.inventory.getCursorStack();
						ItemStack cursedStack = bufferInventory.insertStack(cursorStack.copy());
						playerEntity.inventory.setCursorStack(cursedStack);
					}
					return ItemStack.EMPTY;
				} else {
					return super.onSlotClick(slotNumber, button, action, playerEntity);
				}
			} else {
				return super.onSlotClick(slotNumber, button, action, playerEntity);
			}
		}
	}

	public void tickLabels() {
		for (int bufferSlot : this.bufferInventory.getInvAvailableSlots(null)) {
			controllerLabels.get(bufferSlot).setText(new LiteralText(withSuffix(this.bufferInventory.getStored(bufferSlot))));
		}
	}

	public void tick() {
		bufferInventory.restockAll();
		tickLabels();
	}

	public void setBaseWidgets() {
		controllerSlots.set(0, new BufferInventory.WBufferSlot(this.bufferInventory, 0, 1, 1, playerInventory));
		controllerSlots.set(1, new BufferInventory.WBufferSlot(this.bufferInventory, 1, 1, 1, playerInventory));
		controllerSlots.set(2, new BufferInventory.WBufferSlot(this.bufferInventory, 2, 1, 1, playerInventory));
		controllerSlots.set(3, new BufferInventory.WBufferSlot(this.bufferInventory, 3, 1, 1, playerInventory));
		controllerSlots.set(4, new BufferInventory.WBufferSlot(this.bufferInventory, 4, 1, 1, playerInventory));
		controllerSlots.set(5, new BufferInventory.WBufferSlot(this.bufferInventory, 5, 1, 1, playerInventory));

		tickLabels();

		switch (bufferInventory.getTier()) {
			case 1:
				rootPanel.add(controllerSlots.get(0), sectionX * 2 - 27, sectionY - 12);
				rootPanel.add(controllerLabels.get(0), sectionX * 2 - 27, sectionY + 10);
				break;
			case 2:
				rootPanel.add(controllerSlots.get(0), sectionX * 2 + 1, sectionY - 12);
				rootPanel.add(controllerSlots.get(1), sectionX * 1 - 7, sectionY - 12);
				rootPanel.add(controllerLabels.get(0), sectionX * 2 + 1, sectionY + 10);
				rootPanel.add(controllerLabels.get(1), sectionX * 1 - 7, sectionY + 10);
				break;
			case 3:
				rootPanel.add(controllerSlots.get(0), sectionX * 1 - 36, sectionY - 12);
				rootPanel.add(controllerSlots.get(1), sectionX * 2 - 27, sectionY - 12);
				rootPanel.add(controllerSlots.get(2), sectionX * 3 - 18, sectionY - 12);
				rootPanel.add(controllerLabels.get(0), sectionX * 1 - 36, sectionY + 10);
				rootPanel.add(controllerLabels.get(1), sectionX * 2 - 27, sectionY + 10);
				rootPanel.add(controllerLabels.get(2), sectionX * 3 - 18, sectionY + 10);
				break;
			case 4:
				rootPanel.add(controllerSlots.get(0), sectionX * 1 - 36, sectionY - 12);
				rootPanel.add(controllerSlots.get(1), sectionX * 2 - 27, sectionY - 12);
				rootPanel.add(controllerSlots.get(2), sectionX * 3 - 18, sectionY - 12);
				rootPanel.add(controllerSlots.get(3), sectionX * 2 - 27, sectionY * 2 + 4);
				rootPanel.add(controllerLabels.get(0), sectionX * 1 - 36, sectionY + 10);
				rootPanel.add(controllerLabels.get(1), sectionX * 2 - 27, sectionY + 10);
				rootPanel.add(controllerLabels.get(2), sectionX * 3 - 18, sectionY + 10);
				rootPanel.add(controllerLabels.get(3), sectionX * 2 - 27, sectionY * 2 + 26);
				break;
			case 5:
				rootPanel.add(controllerSlots.get(0), sectionX * 1 - 36, sectionY - 12);
				rootPanel.add(controllerSlots.get(1), sectionX * 2 - 27, sectionY - 12);
				rootPanel.add(controllerSlots.get(2), sectionX * 3 - 18, sectionY - 12);
				rootPanel.add(controllerSlots.get(3), sectionX * 1 - 7, sectionY * 2 + 4);
				rootPanel.add(controllerSlots.get(4), sectionX * 2 + 1, sectionY * 2 + 4);
				rootPanel.add(controllerLabels.get(0), sectionX * 1 - 36, sectionY + 10);
				rootPanel.add(controllerLabels.get(1), sectionX * 2 - 27, sectionY  + 10);
				rootPanel.add(controllerLabels.get(2), sectionX * 3 - 18, sectionY + 10);
				rootPanel.add(controllerLabels.get(3), sectionX * 1 - 7, sectionY * 2 + 26);
				rootPanel.add(controllerLabels.get(4), sectionX * 2 + 1, sectionY * 2 + 26);
				break;
			case 6:
				rootPanel.add(controllerSlots.get(0), sectionX * 1 - 36, sectionY - 12);
				rootPanel.add(controllerSlots.get(1), sectionX * 2 - 27, sectionY - 12);
				rootPanel.add(controllerSlots.get(2), sectionX * 3 - 18, sectionY - 12);
				rootPanel.add(controllerSlots.get(3), sectionX * 1 - 36, sectionY * 2 + 4);
				rootPanel.add(controllerSlots.get(4), sectionX * 2 - 27, sectionY * 2 + 4);        
				rootPanel.add(controllerSlots.get(5), sectionX * 3 - 18, sectionY * 2 + 4);  
				rootPanel.add(controllerLabels.get(0), sectionX * 1 - 36, sectionY + 10);
				rootPanel.add(controllerLabels.get(1), sectionX * 2 - 27, sectionY  + 10);
				rootPanel.add(controllerLabels.get(2), sectionX * 3 - 18, sectionY + 10);
				rootPanel.add(controllerLabels.get(3), sectionX * 1 - 36, sectionY * 2 + 26);
				rootPanel.add(controllerLabels.get(4), sectionX * 2 - 27, sectionY * 2 + 26);        
				rootPanel.add(controllerLabels.get(5), sectionX * 3 - 18, sectionY * 2 + 26);  
				break;  
		}
	}
	
	@Override   
	public int getCraftingResultSlotIndex() {
		return -1;
	}
	
	@Override
	public boolean canUse(PlayerEntity entity) {
		return true;
	}
}