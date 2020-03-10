/*
 *   
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.Size;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.world.heightmap.Levels;
import me.dags.noise.util.NoiseUtil;

import java.util.Random;

/*
 * This class in an adaption of the work by Sebastian Lague which is also licensed under MIT.
 * Reference:
 * https://github.com/SebLague/Hydraulic-Erosion/blob/Coding-Adventure-E01/Assets/Scripts/Erosion.cs
 * https://github.com/SebLague/Hydraulic-Erosion/blob/Coding-Adventure-E01/LICENSE
 *
 * License In Full:
 * MIT License
 *
 * Copyright (c) 2019 Sebastian Lague
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Erosion implements Filter {

    private int erosionRadius = 3;
    private float inertia = 0.05f;
    private float sedimentCapacityFactor = 4;
    private float minSedimentCapacity = 0.01f;
    private float erodeSpeed = 0.3f;
    private float depositSpeed = 0.3f;
    private float evaporateSpeed = 0.01f;
    private float gravity = 8;
    private int maxDropletLifetime = 30;
    private float initialWaterVolume = 1;
    private float initialSpeed = 1;
    private final TerrainPos gradient = new TerrainPos();
    private int[][] erosionBrushIndices = new int[0][];
    private float[][] erosionBrushWeights = new float[0][];

    private final Modifier modifier;
    private final Random random = new Random();

    public Erosion(Settings settings, Levels levels) {
        erodeSpeed = settings.filters.erosion.erosionRate;
        depositSpeed = settings.filters.erosion.depositeRate;
        modifier = Modifier.range(levels.ground, levels.ground(15));
    }

    @Override
    public void apply(Filterable<?> map, int seedX, int seedZ, int iterations) {
        if (erosionBrushIndices.length != map.getSize().total) {
            init(map.getSize().total, erosionRadius);
        }

        applyMain(map, seedX, seedZ, iterations, random);

//        applyNeighbours(map, seedX, seedZ, iterations, random);
    }

    private int nextCoord(Size size, Random random) {
        return random.nextInt(size.total - 1);
    }

    private void applyMain(Filterable<?> map, int seedX, int seedZ, int iterations, Random random) {
        random.setSeed(NoiseUtil.seed(seedX, seedZ));
        while (iterations-- > 0) {
            int posX = nextCoord(map.getSize(), random);
            int posZ = nextCoord(map.getSize(), random);
            apply(map.getBacking(), posX, posZ, map.getSize().total);
        }
    }

    private void apply(Cell<?>[] cells, float posX, float posY, int size) {
        float dirX = 0;
        float dirY = 0;
        float speed = initialSpeed;
        float water = initialWaterVolume;
        float sediment = 0;

        for (int lifetime = 0; lifetime < maxDropletLifetime; lifetime++) {
            int nodeX = (int) posX;
            int nodeY = (int) posY;
            int dropletIndex = nodeY * size + nodeX;
            // Calculate droplet's offset inside the cell (0,0) = at NW node, (1,1) = at SE node
            float cellOffsetX = posX - nodeX;
            float cellOffsetY = posY - nodeY;

            // Calculate droplet's height and direction of flow with bilinear interpolation of surrounding heights
            gradient.update(cells, size, posX, posY);

            // Update the droplet's direction and position (move position 1 unit regardless of speed)
            dirX = (dirX * inertia - gradient.gradientX * (1 - inertia));
            dirY = (dirY * inertia - gradient.gradientY * (1 - inertia));

            // Normalize direction
            float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (Float.isNaN(len)) {
                len = 0;
            }

            if (len != 0) {
                dirX /= len;
                dirY /= len;
            }

            posX += dirX;
            posY += dirY;

            // Stop simulating droplet if it's not moving or has flowed over edge of map
            if ((dirX == 0 && dirY == 0) || posX < 0 || posX >= size - 1 || posY < 0 || posY >= size - 1) {
                break;
            }

            // Find the droplet's new height and calculate the deltaHeight
            float oldHeight = gradient.height;
            float newHeight = gradient.update(cells, size, posX, posY).height;
            float deltaHeight = newHeight - oldHeight;

            // Calculate the droplet's sediment capacity (higher when moving fast down a slope and contains lots of water)
            float sedimentCapacity = Math.max(-deltaHeight * speed * water * sedimentCapacityFactor, minSedimentCapacity);

            // If carrying more sediment than capacity, or if flowing uphill:
            if (sediment > sedimentCapacity || deltaHeight > 0) {
                // If moving uphill (deltaHeight > 0) try fill up to the current height, otherwise deposit a fraction of the excess sediment
                float amountToDeposit = (deltaHeight > 0) ? Math.min(deltaHeight, sediment) : (sediment - sedimentCapacity) * depositSpeed;
                sediment -= amountToDeposit;

                // Add the sediment to the four nodes of the current cell using bilinear interpolation
                // Deposition is not distributed over a radius (like erosion) so that it can fill small pits
                deposit(cells[dropletIndex], amountToDeposit * (1 - cellOffsetX) * (1 - cellOffsetY));
                deposit(cells[dropletIndex + 1], amountToDeposit * cellOffsetX * (1 - cellOffsetY));
                deposit(cells[dropletIndex + size], amountToDeposit * (1 - cellOffsetX) * cellOffsetY);
                deposit(cells[dropletIndex + size + 1], amountToDeposit * cellOffsetX * cellOffsetY);
            } else {
                // Erode a fraction of the droplet's current carry capacity.
                // Clamp the erosion to the change in height so that it doesn't dig a hole in the terrain behind the droplet
                float amountToErode = Math.min((sedimentCapacity - sediment) * erodeSpeed, -deltaHeight);

                // Use erosion brush to erode from all nodes inside the droplet's erosion radius
                for (int brushPointIndex = 0; brushPointIndex < erosionBrushIndices[dropletIndex].length; brushPointIndex++) {
                    int nodeIndex = erosionBrushIndices[dropletIndex][brushPointIndex];
                    Cell<?> cell = cells[nodeIndex];
                    float brushWeight = erosionBrushWeights[dropletIndex][brushPointIndex];
                    float weighedErodeAmount = amountToErode * brushWeight;
                    float deltaSediment = Math.min(cell.value, weighedErodeAmount);//cell.value < weighedErodeAmount) ? cell.value : weighedErodeAmount;
                    erode(cell, deltaSediment);
                    sediment += deltaSediment;
                }
            }

            // Update droplet's speed and water content
            speed = (float) Math.sqrt(speed * speed + deltaHeight * gravity);
            water *= (1 - evaporateSpeed);

            if (Float.isNaN(speed)) {
                speed = 0;
            }
        }
    }

    private void init(int size, int radius) {
        erosionBrushIndices = new int[size * size][];
        erosionBrushWeights = new float[size * size][];

        int[] xOffsets = new int[radius * radius * 4];
        int[] yOffsets = new int[radius * radius * 4];
        float[] weights = new float[radius * radius * 4];
        float weightSum = 0;
        int addIndex = 0;

        for (int i = 0; i < erosionBrushIndices.length; i++) {
            int centreX = i % size;
            int centreY = i / size;

            if (centreY <= radius || centreY >= size - radius || centreX <= radius + 1 || centreX >= size - radius) {
                weightSum = 0;
                addIndex = 0;
                for (int y = -radius; y <= radius; y++) {
                    for (int x = -radius; x <= radius; x++) {
                        float sqrDst = x * x + y * y;
                        if (sqrDst < radius * radius) {
                            int coordX = centreX + x;
                            int coordY = centreY + y;

                            if (coordX >= 0 && coordX < size && coordY >= 0 && coordY < size) {
                                float weight = 1 - (float) Math.sqrt(sqrDst) / radius;
                                weightSum += weight;
                                weights[addIndex] = weight;
                                xOffsets[addIndex] = x;
                                yOffsets[addIndex] = y;
                                addIndex++;
                            }
                        }
                    }
                }
            }

            int numEntries = addIndex;
            erosionBrushIndices[i] = new int[numEntries];
            erosionBrushWeights[i] = new float[numEntries];

            for (int j = 0; j < numEntries; j++) {
                erosionBrushIndices[i][j] = (yOffsets[j] + centreY) * size + xOffsets[j] + centreX;
                erosionBrushWeights[i][j] = weights[j] / weightSum;
            }
        }
    }

    private void deposit(Cell<?> cell, float amount) {
        float change = modifier.modify(cell, amount);
        cell.value += change;
        cell.sediment += change;
    }

    private void erode(Cell<?> cell, float amount) {
        float change = modifier.modify(cell, amount);
        cell.value -= change;
        cell.erosion -= change;
    }

    private static class TerrainPos {
        private float height;
        private float gradientX;
        private float gradientY;

        private TerrainPos update(Cell<?>[] nodes, int mapSize, float posX, float posY) {
            int coordX = (int) posX;
            int coordY = (int) posY;

            // Calculate droplet's offset inside the cell (0,0) = at NW node, (1,1) = at SE node
            float x = posX - coordX;
            float y = posY - coordY;

            // Calculate heights of the four nodes of the droplet's cell
            int nodeIndexNW = coordY * mapSize + coordX;
            float heightNW = nodes[nodeIndexNW].value;
            float heightNE = nodes[nodeIndexNW + 1].value;
            float heightSW = nodes[nodeIndexNW + mapSize].value;
            float heightSE = nodes[nodeIndexNW + mapSize + 1].value;

            // Calculate droplet's direction of flow with bilinear interpolation of height difference along the edges
            this.gradientX = (heightNE - heightNW) * (1 - y) + (heightSE - heightSW) * y;
            this.gradientY = (heightSW - heightNW) * (1 - x) + (heightSE - heightNE) * x;
            // Calculate height with bilinear interpolation of the heights of the nodes of the cell
            this.height = heightNW * (1 - x) * (1 - y) + heightNE * x * (1 - y) + heightSW * (1 - x) * y + heightSE * x * y;
            return this;
        }
    }
}
