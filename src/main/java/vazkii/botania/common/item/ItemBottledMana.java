/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jun 27, 2014, 2:41:19 AM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.entity.EntityPixie;
import vazkii.botania.common.entity.EntitySignalFlare;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class ItemBottledMana extends ItemMod {
	private static final int SWIGS = 6;
	private static final String TAG_SWIGS_LEFT = "swigsLeft";
	private static final String TAG_SEED = "randomSeed";

	public ItemBottledMana(Properties props) {
		super(props);
		addPropertyOverride(new ResourceLocation(LibMisc.MOD_ID, "swigs_taken"), (stack, world, entity) -> SWIGS - getSwigsLeft(stack));
	}

	public void effect(ItemStack stack, LivingEntity living, int id) {
		switch(id) {
		case 0 : { // Random motion
			living.setMotion((Math.random() - 0.5) * 3, living.getMotion().getY(),
					(Math.random() - 0.5) * 3);
			break;
		}
		case 1 : { // Water
			if(!living.world.isRemote && !living.world.getDimension().doesWaterVaporize())
				living.world.setBlockState(new BlockPos(living), Blocks.WATER.getDefaultState());
			break;
		}
		case 2 : { // Set on Fire
			if(!living.world.isRemote)
				living.setFire(4);
			break;
		}
		case 3 : { // Mini Explosion
			if(!living.world.isRemote)
				living.world.createExplosion(null, living.posX, living.posY,
						living.posZ, 0.25F, Explosion.Mode.NONE);
			break;
		}
		case 4 : { // Mega Jump
			if(!living.world.getDimension().isNether()) {
				if(!living.world.isRemote)
					living.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 300, 5));
				living.setMotion(living.getMotion().getX(), 6, living.getMotion().getZ());
			}

			break;
		}
		case 5 : { // Randomly set HP
			if(!living.world.isRemote)
				living.setHealth(living.world.rand.nextInt(19) + 1);
			break;
		}
		case 6 : { // Lots O' Hearts
			if(!living.world.isRemote)
				living.addPotionEffect(new EffectInstance(Effects.ABSORPTION, 20 * 60 * 2, 9));
			break;
		}
		case 7 : { // All your inventory is belong to us
			if(!living.world.isRemote && living instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) living;
				for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
					ItemStack stackAt = player.inventory.getStackInSlot(i);
					if(stackAt != stack) {
						if(!stackAt.isEmpty())
							player.entityDropItem(stackAt, 0);
						player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
					}
				}
			}

			break;
		}
		case 8 : { // Break your neck
			living.rotationPitch = (float) Math.random() * 360F;
			living.rotationYaw = (float) Math.random() * 180F;

			break;
		}
		case 9 : { // Highest Possible
			int x = MathHelper.floor(living.posX);
			int z = MathHelper.floor(living.posZ);
			for(int i = 256; i > 0; i--) {
				Block block = living.world.getBlockState(new BlockPos(x, i, z)).getBlock();
				if(!block.isAir(living.world.getBlockState(new BlockPos(x, i, z)), living.world, new BlockPos(x, i, z))) {
					if(living instanceof ServerPlayerEntity) {
						ServerPlayerEntity mp = (ServerPlayerEntity) living;
						mp.connection.setPlayerLocation(living.posX, i, living.posZ, living.rotationYaw, living.rotationPitch);
					}
					break;
				}
			}

			break;
		}
		case 10 : { // HYPERSPEEEEEED
			if(!living.world.isRemote)
				living.addPotionEffect(new EffectInstance(Effects.SPEED, 60, 200));
			break;
		}
		case 11 : { // Night Vision
			if(!living.world.isRemote)
				living.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 6000, 0));
			break;
		}
		case 12 : { // Flare
			if(!living.world.isRemote) {
				EntitySignalFlare flare = new EntitySignalFlare(living.world);
				flare.setPosition(living.posX, living.posY, living.posZ);
				flare.setColor(living.world.rand.nextInt(16));
				flare.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 40F, (1.0F + (living.world.rand.nextFloat() - living.world.rand.nextFloat()) * 0.2F) * 0.7F);

				living.world.addEntity(flare);

				int range = 5;
				List<LivingEntity> entities = living.world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(living.posX - range, living.posY - range, living.posZ - range, living.posX + range, living.posY + range, living.posZ + range));
				for(LivingEntity entity : entities)
					if(entity != living && (!(entity instanceof PlayerEntity) || ServerLifecycleHooks.getCurrentServer() == null || ServerLifecycleHooks.getCurrentServer().isPVPEnabled()))
						entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 50, 5));
			}

			break;
		}
		case 13 : { // Pixie Friend
			if(!living.world.isRemote) {
				EntityPixie pixie = new EntityPixie(living.world);
				pixie.setPosition(living.posX, living.posY + 1.5, living.posZ);
				living.world.addEntity(pixie);
			}
			break;
		}
		case 14 : { // Nausea + Blindness :3
			if(!living.world.isRemote) {
				living.addPotionEffect(new EffectInstance(Effects.NAUSEA, 160, 3));
				living.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 160, 0));
			}

			break;
		}
		case 15 : { // Drop own Head
			if(!living.world.isRemote && living instanceof PlayerEntity) {
				living.attackEntityFrom(DamageSource.MAGIC, living.getHealth() - 1);
				ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
				ItemNBTHelper.setString(skull, "SkullOwner", ((PlayerEntity) living).getGameProfile().getName());
				living.entityDropItem(skull, 0);
			}
			break;
		}
		}
	}

	private void randomEffect(LivingEntity player, ItemStack stack) {
		effect(stack, player, new Random(getSeed(stack)).nextInt(16));
	}

	private long getSeed(ItemStack stack) {
		long seed = ItemNBTHelper.getLong(stack, TAG_SEED, -1);
		if(seed == -1)
			return randomSeed(stack);
		return seed;
	}

	private long randomSeed(ItemStack stack) {
		long seed = Math.abs(random.nextLong());
		ItemNBTHelper.setLong(stack, TAG_SEED, seed);
		return seed;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack par1ItemStack, World world, List<ITextComponent> stacks, ITooltipFlag flags) {
		stacks.add(new TranslationTextComponent("botaniamisc.bottleTooltip"));
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
		player.setActiveHand(hand);
		return ActionResult.newResult(ActionResultType.SUCCESS, player.getHeldItem(hand));
	}

	@Nonnull
	@Override
	public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World world, LivingEntity living) {
		randomEffect(living, stack);
		int left = getSwigsLeft(stack);
		if(left <= 1) {
			return new ItemStack(Items.GLASS_BOTTLE);
		} else {
			setSwigsLeft(stack, left - 1);
			randomSeed(stack);
			return stack;
		}
	}

	@Override
	public int getUseDuration(ItemStack par1ItemStack) {
		return 20;
	}

	@Nonnull
	@Override
	public UseAction getUseAction(ItemStack par1ItemStack) {
		return UseAction.DRINK;
	}

	private int getSwigsLeft(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_SWIGS_LEFT, SWIGS);
	}

	private void setSwigsLeft(ItemStack stack, int swigs) {
		ItemNBTHelper.setInt(stack, TAG_SWIGS_LEFT, swigs);
	}

}
