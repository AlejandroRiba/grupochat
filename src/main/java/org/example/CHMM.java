package org.example;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;
import org.json.JSONObject;

/**
 *
 * @author axel
 */
public class CHMM {

    private static final int PORT = 9331;
    
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        // Solicitar nombre de usuario
        System.out.print("Ingrese su nombre de usuario: ");
        String username = scanner.nextLine();
      try{
          /* BLOQUE PARA ELEGIR LA INTERAZ DE CONEXIÓN A LA RED */
          NetworkInterface ni = NetworkInterface.getByName("wlan3");
          MulticastSocket s = new MulticastSocket(PORT);
          s.setReuseAddress(true);
          s.setTimeToLive(255);
          InetAddress gpo = InetAddress.getByName("230.1.1.1"); //IPV4
          //InetAddress gpo = InetAddress.getByName("ff3e:40:2001::1"); //IPV6
          SocketAddress dir;
            try{
                 dir = new InetSocketAddress(gpo,PORT);
            }catch(Exception e){
              System.err.println("Sintaxis: java UDPEchoClient host [port]");
               return;
            }//catch

          s.joinGroup(dir, ni);
          // Crear y arrancar el hilo de recepción de mensajes
          Thread receiveThread = new Thread(() -> receiveMessages(s));
          receiveThread.start();

          // Iniciar el hilo para manejar la entrada del usuario (el menú)
          Thread inputThread = new Thread(() -> {
              while (true) {
                  showMenu();
                  String option = scanner.nextLine();
                  switch (option) {
                      case "1":
                          sendMessage(s, gpo, scanner, username);
                          break;
                      case "2":
                          sendFile(s, gpo, scanner, username);
                          break;
                      case "3":
                          sendEmoji(s, gpo, scanner, username);
                          break;
                      case "4":
                          sendPrivateMessage(s, gpo, scanner, username);
                          break;
                      case "5":
                          System.out.println("Saliendo del chat...");
                          s.close();
                          return;
                      default:
                          System.out.println("Opción no válida.");
                          break;
                  }
              }
          });

          // Iniciar el hilo de entrada
          inputThread.start();
          /*System.out.println("Servicio iniciado y unido al grupo.. comienza escucha de mensajes");
          for(;;){
              DatagramPacket p = new DatagramPacket(new byte[65535],65535);
              s.receive(p);
              System.out.println("Datagrama multicast recibido desde "+p.getAddress()+":"+p.getPort()+"Con el mensaje:"+new String(p.getData(),0,p.getLength()));
          }//for*/
      }catch(Exception e){
          e.printStackTrace();
      }
    }

    // Método para recibir mensajes y mostrarlos en el chat
    private static void receiveMessages(MulticastSocket socket) {
        byte[] buffer = new byte[1024];
        try {
            while (true) {
                System.out.println("Escuchando mensajes");
                DatagramPacket p = new DatagramPacket(new byte[65535],65535);
                socket.receive(p);
                String receivedMessage = new String(p.getData(), 0, p.getLength());
                System.out.println("Mensaje recibido: " + receivedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error");
        }
    }

    // Método para mostrar el menú de opciones y gestionar las acciones del usuario
    // Mostrar el menú de opciones
    private static void showMenu() {
        System.out.println("\n--- Menú ---");
        System.out.println("1. Enviar mensaje");
        System.out.println("2. Enviar archivo");
        System.out.println("3. Enviar emoji");
        System.out.println("4. Enviar mensaje privado");
        System.out.println("5. Salir");
        System.out.print("Elige una opción: ");
    }

    // Método para enviar un mensaje de texto
    private static void sendMessage(MulticastSocket socket, InetAddress group, Scanner scanner, String username) {
        System.out.print("Escribe el mensaje: ");
        String message = scanner.nextLine();

        JSONObject json = new JSONObject();
        json.put("type", "message");
        json.put("usr", username);
        json.put("content", message);

        sendData(socket, group, json.toString());
        System.out.println("Mensaje enviado: " + message);  // Depuración
    }

    // Método para enviar un archivo
    private static void sendFile(MulticastSocket socket, InetAddress group, Scanner scanner, String username) {
        System.out.print("Ruta del archivo: ");
        String filePath = scanner.nextLine();
        // Aquí puedes implementar la lógica de envío de archivos en paquetes (ver tu implementación anterior)
        System.out.println("Función de envío de archivos aún no implementada.");
    }

    // Método para enviar un emoji
    private static void sendEmoji(MulticastSocket socket, InetAddress group, Scanner scanner, String username) {
        System.out.print("Escribe el emoji: ");
        String emoji = scanner.nextLine();

        JSONObject json = new JSONObject();
        json.put("type", "emoji");
        json.put("content", emoji);

        sendData(socket, group, json.toString());
    }

    // Método para enviar un mensaje privado
    private static void sendPrivateMessage(MulticastSocket socket, InetAddress group, Scanner scanner, String username) {
        System.out.print("Escribe el nombre de usuario destino: ");
        String recipient = scanner.nextLine();
        System.out.print("Escribe el mensaje privado: ");
        String privateMessage = scanner.nextLine();

        JSONObject json = new JSONObject();
        json.put("type", "private_message");
        json.put("recipient", recipient);
        json.put("content", privateMessage);

        sendData(socket, group, json.toString());
    }

    // Método auxiliar para enviar datos al grupo multicast
    private static void sendData(MulticastSocket socket, InetAddress group, String data) {
        try {
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
