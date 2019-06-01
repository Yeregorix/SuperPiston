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

public final class PistonStructure {
	private final List<Vector3i> blocksToMove, blocksToDestroy;
	private final boolean moveable;

	public PistonStructure(Collection<Vector3i> blocksToMove, Collection<Vector3i> blocksToDestroy) {
		this.blocksToMove = ImmutableList.copyOf(blocksToMove);
		this.blocksToDestroy = ImmutableList.copyOf(blocksToDestroy);
		this.moveable = true;
	}

	public PistonStructure(boolean moveable) {
		this.blocksToMove = ImmutableList.of();
		this.blocksToDestroy = ImmutableList.of();
		this.moveable = moveable;
	}

	public List<Vector3i> getBlocksToMove() {
		return this.blocksToMove;
	}

	public List<Vector3i> getBlocksToDestroy() {
		return this.blocksToDestroy;
	}

	public boolean isMoveable() {
		return this.moveable;
	}
}
