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

package net.mcreator.minecraft.link.devices.raspberrypi;

import net.mcreator.minecraft.link.LinkProtocol;
import net.mcreator.minecraft.link.devices.AbstractDevice;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RaspberryPi extends AbstractDevice {

	private InetAddress remote_address;
	private InetAddress local_address;
	private boolean connected;

	private DatagramSocket socket;

	private ExecutorService deviceCommunicationThread = Executors.newSingleThreadExecutor();

	private static final int LOCAL_PORT = 25564;
	static final int REMOTE_PORT = 25563;

	/**
	 * Raspberry Pi device constructor
	 *
	 * @param name             Device name as sent via Link
	 * @param description      Device description as set by device detector
	 * @param digitalInputsNum Number of digital inputs the device has
	 * @param analogInputsNum  Numver of digital outputs the device has
	 * @param remote_address   Address of the device that was detected
	 * @param local_address    Address of the local interface on the computer running Minecraft Link mod in Minecraft on which the communication with the device is happening
	 */
	RaspberryPi(String name, String description, int digitalInputsNum, int analogInputsNum, InetAddress remote_address,
			InetSocketAddress local_address) {
		super(name, description, digitalInputsNum, analogInputsNum);
		this.remote_address = remote_address;
		this.local_address = local_address.getAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void connect() {
		if (!connected) {
			deviceCommunicationThread.submit(() -> {
				byte[] connectPacket = ("connect?" + local_address.getHostAddress()).getBytes();
				try {
					DatagramSocket datagramSocket = new DatagramSocket();
					datagramSocket
							.send(new DatagramPacket(connectPacket, connectPacket.length, remote_address, REMOTE_PORT));
					datagramSocket.close();

					new Thread(() -> {
						try {
							socket = new DatagramSocket(LOCAL_PORT);
							this.connected = true; // at this point, we can say connection is successful
							while (connected) {
								try {
									byte[] buf = new byte[1024];
									DatagramPacket packet = new DatagramPacket(buf, buf.length);
									socket.receive(packet);
									String msg = new String(buf).split("\n")[0].trim();
									processInboundMessage(msg.getBytes());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} catch (SocketException e) {
							e.printStackTrace();
						}
					}).start();

					Thread.sleep(250);

					sendData(LinkProtocol.START_POLLING_INPUTS);

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void disconnect() {
		if (connected) {
			deviceCommunicationThread.submit(() -> {
				if (socket != null)
					socket.close(); // close inbound socket
				sendData(LinkProtocol.STOP_POLLING_INPUTS);
				this.connected = false;
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void sendData(byte[] dataPacket) {
		if (connected) {
			deviceCommunicationThread.submit(() -> {
				try (DatagramSocket datagramSocket = new DatagramSocket()) {
					datagramSocket.send(new DatagramPacket(dataPacket, dataPacket.length, remote_address, REMOTE_PORT));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean isConnected() {
		return connected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean validateConnection() {
		try {
			return remote_address.isReachable(500);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}