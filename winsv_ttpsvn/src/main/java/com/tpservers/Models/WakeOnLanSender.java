package com.tpservers.Models;

import java.net.*;
import tungtt.Console.Console;

public final class WakeOnLanSender {

    public static void send(String mac, String broadcastIp) throws Exception {
        Console.info(mac);
        byte[] macBytes = parseMac(mac);
        byte[] packet = new byte[6 + 16 * macBytes.length];

        for (int i = 0; i < 6; i++)
            packet[i] = (byte) 0xFF;

        for (int i = 6; i < packet.length; i += macBytes.length)
            System.arraycopy(macBytes, 0, packet, i, macBytes.length);

        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.send(new DatagramPacket(
            packet,
            packet.length,
            InetAddress.getByName(broadcastIp),
            9
        ));
        socket.close();
    }

    private static byte[] parseMac(String mac) {
        String[] s = mac.split(":");
        if (s.length != 6) {
            Console.error("Invalid MAC");
            throw new IllegalArgumentException("Invalid MAC");
        }
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++)
            b[i] = (byte) Integer.parseInt(s[i], 16);
        return b;
    }
}
