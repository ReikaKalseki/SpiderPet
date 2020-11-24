/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.CritterPet.Biome;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import Reika.CritterPet.CritterPet;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Instantiable.Worldgen.StackableBiomeDecorator;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;

public class DecoratorPinkForest extends StackableBiomeDecorator {

	private final WorldGenRedBamboo redBambooGenerator = new WorldGenRedBamboo();

	private int riverHeight;

	public DecoratorPinkForest() {
		super();
	}

	@Override
	protected void genDecorations(BiomeGenBase biome) {
		for (int i = 0; i < 16; i++) {
			for (int k = 0; k < 16; k++) {
				int dx = chunk_X+i;
				int dz = chunk_Z+k;
				int top = this.getTrueTopAt(currentWorld, dx, dz);
				if (currentWorld.getBiomeGenForCoords(dx, dz) == biome) {
					this.cleanColumn(currentWorld, dx, top, dz);
				}
			}
		}

		super.genDecorations(biome);

		int x = chunk_X + randomGenerator.nextInt(16) + 8;
		int z = chunk_Z + randomGenerator.nextInt(16) + 8;

		int top = currentWorld.getTopSolidOrLiquidBlock(x, z);

		BiomePinkForest forest = (BiomePinkForest)biome;
		redBambooGenerator.setFrequency(forest.getSubBiome(currentWorld, x, z));
		redBambooGenerator.generate(currentWorld, randomGenerator, x, top, z);
	}

	private int getTrueTopAt(World currentWorld, int dx, int dz) {
		int top = currentWorld.getTopSolidOrLiquidBlock(dx, dz);
		Block at = currentWorld.getBlock(dx, top, dz);
		while (at == Blocks.air || at == CritterPet.log || at == CritterPet.leaves || at == Blocks.glass || at.isLeaves(currentWorld, dx, top, dz) || ReikaWorldHelper.softBlocks(currentWorld, dx, top, dz)) {
			top--;
			at = currentWorld.getBlock(dx, top, dz);
		}
		return top;
	}

	private void cleanColumn(World world, int x, int top, int z) {
		boolean river = world.getBlock(x, top, z) == Blocks.clay;
		for (int i = top; i >= top-6; i--) {
			Block b = i == 0 ? Blocks.grass : Blocks.stone;
			Block at = world.getBlock(x, top-i, z);
			if (i > 0 && (at == Blocks.dirt || at == Blocks.grass))
				b = Blocks.dirt;
			if (at != b)
				world.setBlock(x, top-i, z, b);
		}
		if (river) {
			riverHeight = -1;
			double avg = this.getAverageHeight(world, x, z, 15); //was 6 then 9
			int watermax = (int)(Math.min(avg-1.5, riverHeight));
			if (watermax > top) {
				for (int i = top+1; i <= watermax; i++) {
					world.setBlock(x, i, z, Blocks.water);
				}
			}
			else {
				world.setBlock(x, top, z, Blocks.sand);
			}
		}

		for (int h = 0; h < 10; h++) {
			if (world.getBlock(x, top+h, z) == Blocks.glass)
				world.setBlock(x, top+h, z, Blocks.air);
		}
	}

	private double getAverageHeight(World world, int x, int z, int r) {
		double avg = 0;
		int n = 0;
		for (int i = -r; i <= r; i++) {
			for (int k = -r; k <= r; k++) {
				int dx = x+i;
				int dz = z+k;
				int top = this.getTrueTopAt(world, dx, dz);
				if (world.getBlock(dx, top, dz) == Blocks.clay) {
					riverHeight = Math.max(riverHeight, top+1);

					int glassHeight = -1;
					for (int h = top+1; h < 12; h++) {
						if (world.getBlock(dx, h, dz) == Blocks.glass) {
							glassHeight = h;
							break;
						}
					}
					if (glassHeight >= 0) {
						avg += glassHeight;
						n++;
					}
				}
				else {
					avg += top;
					n++;
				}
			}
		}
		if (n > 0)
			avg /= n;
		return n == 0 ? riverHeight : avg;
	}

	@Override
	protected ModLogger getLogger() {
		return CritterPet.logger;
	}


}
