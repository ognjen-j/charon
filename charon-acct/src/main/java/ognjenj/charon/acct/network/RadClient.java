package ognjenj.charon.acct.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import ognjenj.charon.acct.exceptions.MalformedPacketException;
import ognjenj.charon.acct.radius.RadPacket;
import ognjenj.charon.acct.runner.PluginConfiguration;

public class RadClient {
	public static RadPacket sendReceiveRadiusPacket(RadPacket request, PluginConfiguration config) throws IOException {
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(config.getNetworkTimeoutLength() * 1000);
			byte[] transformedRequest = request.convertToNetworkReadyFormat();
			DatagramPacket packet = new DatagramPacket(transformedRequest, 0, transformedRequest.length,
					config.getRadiusIpAddress().toInetAddress(),
					request.getPacketType() == RadPacket.RadPacketType.ACCESS_REQUEST
							? config.getRadiusAuthPort()
							: config.getRadiusAcctPort());
			socket.send(packet);

			byte[] responseBuffer = new byte[4096];
			DatagramPacket responsePacket = new DatagramPacket(responseBuffer, 4096);
			socket.receive(responsePacket);

			RadPacket response = RadPacket.constructFromBinaryFormat(responsePacket.getData());
			socket.close();
			return response;
		} catch (MalformedPacketException ex) {
			config.getLogger().error(ex.getMessage(), ex);
			return null;
		}
	}
}
