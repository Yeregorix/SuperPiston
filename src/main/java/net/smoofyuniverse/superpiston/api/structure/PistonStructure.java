/*
 * Copyright (c) 2018-2019 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.superpiston.api.structure;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

/**
 * This immutable object represents blocks that will be moved or destroyed by a piston movement.
 * If the structure is not moveable then the piston can't move and no blocks will be affected.
 */
public final class PistonStructure {
	private final List<Vector3i> blocksToMove, blocksToDestroy;
	private final boolean moveable;

	/**
	 * Constructs a moveable structure.
	 *
	 * @param blocksToMove    The block that will be moved.
	 * @param blocksToDestroy The blocks that will be destroyed.
	 */
	public PistonStructure(Collection<Vector3i> blocksToMove, Collection<Vector3i> blocksToDestroy) {
		this.blocksToMove = ImmutableList.copyOf(blocksToMove);
		this.blocksToDestroy = ImmutableList.copyOf(blocksToDestroy);
		this.moveable = true;
	}

	/**
	 * Constructs a structure with no blocks.
	 *
	 * @param moveable Whether this structure is moveable or not.
	 */
	public PistonStructure(boolean moveable) {
		this.blocksToMove = ImmutableList.of();
		this.blocksToDestroy = ImmutableList.of();
		this.moveable = moveable;
	}

	/**
	 * @return The list of blocks that will be moved.
	 */
	public List<Vector3i> getBlocksToMove() {
		return this.blocksToMove;
	}

	/**
	 * @return The list of blocks that will be destroyed.
	 */
	public List<Vector3i> getBlocksToDestroy() {
		return this.blocksToDestroy;
	}

	/**
	 * @return Whether this structure is moveable or not.
	 */
	public boolean isMoveable() {
		return this.moveable;
	}
}
