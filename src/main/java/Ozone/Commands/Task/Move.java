/*
 * Copyright 2021 Itzbenz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Ozone.Commands.Task;

import Ozone.Commands.Pathfinding;
import Ozone.Commands.TaskInterface;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Tile;

import static Ozone.Commands.Pathfinding.distanceTo;
import static Ozone.Commands.TaskInterface.setMov;

public class Move extends Task {
	public static final float airTolerance = 12f, landTolerance = 1f;
	private Position destTilePos;
	private Tile destTile;
	private Seq<Tile> pathfindingCache = new Seq<>();
	
	//tile coordinate
	public Move(float x, float y) {
		this(new Vec2(x, y));
	}
	
	public Move(Position dest) {
		if (dest == null) dest = Vars.player.tileOn();
		
		destTile = Vars.world.tileWorld(dest.getX(), dest.getY());
		destTilePos = dest;
		setTick(10);
		
		if (!Vars.player.unit().isFlying()) {
			if (Pathfinding.passable(destTile)) {
				pathfindingCache = Pathfinding.pathfind(destTile);
				destTile = null;//to use the next tile
			}else {
				destTile = Vars.player.tileOn();
				destTilePos = Vars.player.tileOn();
			}
			
		}
		name = "Goto: " + dest.toString();
	}
	
	
	@Override
	public boolean isCompleted() {
		return Pathfinding.withinPlayerTolerance(destTilePos) || (!Vars.player.unit().isFlying() && pathfindingCache.isEmpty());
	}

	@Override
	public void update() {
		if (!tick()) if (Vars.player.dead()) return;
		if (!Vars.player.unit().isFlying()) {
			if (pathfindingCache.isEmpty()) return;
			if (destTile != null) {
				if (distanceTo(TaskInterface.getCurrentTilePos(), destTile) <= landTolerance) {
					pathfindingCache.remove(0).clearOverlay();
				}
			}
			if (pathfindingCache.isEmpty()) return;
			destTile = pathfindingCache.get(0);
			destTile.setOverlay(Blocks.dirt);
		}else {
			setMov(destTilePos);
			return;
		}
		setMov(destTile);
	}

	public float getCurrentDistance() {
		return (float) distanceTo(TaskInterface.getCurrentTilePos(), destTilePos);
	}


}
