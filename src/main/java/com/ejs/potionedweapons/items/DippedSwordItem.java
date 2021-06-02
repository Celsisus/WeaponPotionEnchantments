package com.ejs.potionedweapons.items;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Vanishable;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DippedSwordItem extends ToolItem implements Vanishable {
   private final float attackDamage;
   private final StatusEffect effect;
   private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

   public DippedSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, StatusEffect effect, Item.Settings settings) {
      super(toolMaterial, settings);
      this.attackDamage = (float)attackDamage + toolMaterial.getAttackDamage();
      this.effect = effect;
      Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", (double)this.attackDamage, EntityAttributeModifier.Operation.ADDITION));
      builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", (double)attackSpeed, EntityAttributeModifier.Operation.ADDITION));
      this.attributeModifiers = builder.build();
   }

   public float getAttackDamage() {
      return this.attackDamage;
   }

   public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
      return !miner.isCreative();
   }

   public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
      if (state.isOf(Blocks.COBWEB)) {
         return 15.0F;
      } else {
         Material material = state.getMaterial();
         return material != Material.PLANT && material != Material.REPLACEABLE_PLANT && material != Material.UNUSED_PLANT && !state.isIn(BlockTags.LEAVES) && material != Material.GOURD ? 1.0F : 1.5F;
      }
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damage(1, (LivingEntity)attacker, (Consumer<LivingEntity>)((e) -> {
         ((LivingEntity) e).sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
      }));
      target.addStatusEffect(new StatusEffectInstance(effect, 400));
      return true;
   }
   
   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if (state.getHardness(world, pos) != 0.0F) {
         stack.damage(2, (LivingEntity)miner, (Consumer<LivingEntity>)((e) -> {
            ((LivingEntity) e).sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }
      return true;
   }

   public boolean isEffectiveOn(BlockState state) {
      return state.isOf(Blocks.COBWEB);
   }

   public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
      return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
   }
}
