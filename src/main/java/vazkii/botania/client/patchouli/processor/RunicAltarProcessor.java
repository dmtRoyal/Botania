/**
 * This class was created by <Hubry>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Sep 10 2019, 9:52 AM (GMT)]
 */
package vazkii.botania.client.patchouli.processor;

import net.minecraft.util.ResourceLocation;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeRuneAltar;
import vazkii.patchouli.api.IVariableProvider;

public class RunicAltarProcessor extends PetalApothecaryProcessor {
	@Override
	public void setup(IVariableProvider<String> variables) {
		this.recipe = BotaniaAPI.runeAltarRecipes.get(new ResourceLocation(variables.get("recipe")));
	}

	@Override
	public String process(String key) {
		if(key.equals("mana"))
			return String.valueOf(((RecipeRuneAltar) recipe).getManaUsage());
		return super.process(key);
	}
}
