package org.example;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkInterfaces {
    // Método para desplegar la información de una interfaz de red
    static void despliegaInfoNIC(NetworkInterface netint) throws SocketException {
        System.out.printf("Nombre de despliegue: %s\n", netint.getDisplayName());
        System.out.printf("Nombre: %s\n", netint.getName());
        String multicast = netint.supportsMulticast() ? "Soporta multicast" : "No soporta multicast";
        System.out.printf("Multicast: %s\n", multicast);

        byte[] hardwareAddress = netint.getHardwareAddress();
        if (hardwareAddress != null) {
            StringBuilder mac = new StringBuilder();
            for (byte b : hardwareAddress) {
                mac.append(String.format("%02X:", b));
            }
            // Elimina el último ":"
            if (mac.length() > 0) {
                mac.deleteCharAt(mac.length() - 1);
            }
            System.out.printf("Hardware address: %s\n", mac);
        } else {
            System.out.println("Hardware address: No disponible");
        }

        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("Dirección: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }

    public static void main(String[] args) {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            int z = 0;
            for (NetworkInterface netint : Collections.list(nets)) {
                System.out.printf("[Interfaz %d]:\n", ++z);
                despliegaInfoNIC(netint);
            }
        } catch (SocketException e) {
            System.err.println("Error al obtener las interfaces de red: " + e.getMessage());
        }
    }
}
