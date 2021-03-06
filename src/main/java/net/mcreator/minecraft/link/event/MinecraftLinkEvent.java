/*
 * Copyright 2018 Pylo
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

package net.mcreator.minecraft.link.event;

import net.mcreator.minecraft.link.devices.AbstractDevice;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Abstract class that defines the base of the Minecraft Link events
 */
public abstract class MinecraftLinkEvent extends Event {

	private AbstractDevice device;

	/**
	 * Basic constructor
	 *
	 * @param device The device to which the event is associated to
	 */
	public MinecraftLinkEvent(AbstractDevice device) {
		this.device = device;
	}

	/**
	 * Returns the device event is associated to
	 *
	 * @return AbstractDevice object that triggered this event
	 */
	public AbstractDevice getDevice() {
		return device;
	}

}
