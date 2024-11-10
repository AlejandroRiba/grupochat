package org.example;

import org.example.chat.NombreUsuarioDialog;
import org.example.chat.Principal;
import org.example.clases.Usuario;
import org.json.JSONObject;
import  org.json.JSONArray;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class ChatClient extends Thread{

    private final Principal formulario; // Referencia a la interfaz gráfica
    private static final int PORT = 9331;
    private static final String ADDR = "230.1.1.1"; //IPV4
    //"ff3e:40:2001::1" //IPV6
    public static final int DGRAM_BUF_LEN = 2048;

    // Lista de usuarios conectados en este cliente
    private ArrayList<Usuario> usuarios = new ArrayList<>();
    private String username;

    private Usuario usuario_actual;
    private MulticastSocket socket;
    private InetAddress group;

    public ChatClient(String username, Principal formulario) throws IOException {
        this.username = username;
        this.formulario = formulario; // Asigna el formulario al campo
        this.socket = new MulticastSocket(PORT);
        this.group = InetAddress.getByName(ADDR);
        socket.joinGroup(group);
    }

    //Método para recibir la lista de usuarios activos
    private void recibirActivos() throws IOException {
        // Establecer un tiempo de espera máximo
        long tiempoInicio = System.currentTimeMillis();
        long tiempoEsperaMaximo = 2000; // 2 segundos en milisegundos

        // Espera una respuesta durante un tiempo específico
        socket.setSoTimeout(2000);
        byte[] buffer = new byte[DGRAM_BUF_LEN];
        DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);
        boolean status = true; //variable para que se ejecute el bucle while
        while (status) { //bucle para esperar la respuesta correcta
            // Verificar si el tiempo de espera máximo se ha superado
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            if (tiempoTranscurrido > tiempoEsperaMaximo) {
                System.out.println("Tiempo de espera excedido. No se recibió respuesta.");
                status = false;
                break;
            }

            // Esperar la respuesta
            try {
                socket.receive(respuesta);  // Esto bloquea el hilo hasta que se recibe el paquete
            } catch (SocketTimeoutException e) {
                // Se produce si el socket no recibe respuesta en el tiempo especificado
                System.out.println("Timeout: No se recibió respuesta en el tiempo especificado.");
                status = false;
                break;
            }

            // Convertir los datos del DatagramPacket en un String
            String mensajeRecibido = new String(respuesta.getData(), 0, respuesta.getLength()).trim();

            // Parsear el mensaje a un objeto JSON
            JSONObject jsonMensaje = new JSONObject(mensajeRecibido);
            // Verificar que el tipo de mensaje es 'listaUsuarios'
            String tipo = jsonMensaje.getString("tipo");
            if("listaUsuarios".equals(tipo)){
                System.out.println("Usuarios recibidos: " + jsonMensaje);
                // Extraer la lista de usuarios del mensaje
                JSONArray usuariosRecibidos = jsonMensaje.getJSONArray("usuarios");
                actualizarUsuarios(usuariosRecibidos);
                status =  false; //para que salga del bucle
            }
        }
    }

    @Override
    public void run() {
        // Crear y arrancar el hilo de recepción de mensajes
        Thread receiveThread = new Thread(() -> {
            try {
                receiveMessages(this.socket);
            } catch (IOException e) {
                System.out.println("No se recibe nada aún.");
            }
        });
        receiveThread.start();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            String nombreUsuario = null;
            try {
                Principal formulario = new Principal(null);
                ChatClient cliente = new ChatClient(nombreUsuario, formulario);
                formulario.setCliente(cliente);
                System.out.println("Comienza verificación de usuario existente.");
                // Obtener la dirección IP local del usuario
                InetAddress localAddress = InetAddress.getLocalHost();
                String usr_direccionUnicast = localAddress.getHostAddress();
                // Solicitamos la lista de usuarios conectados
                cliente.solicitarUsuarios(nombreUsuario);
                // Esperamos la respuesta de esa petición
                cliente.recibirActivos();
                //Variable auxiliar para la validación del usuario
                boolean verificacion = true;

                //Iniciamos la escucha de los mensajes y declaramos el formulario
                cliente.start();

                while (nombreUsuario == null || verificacion) {
                    NombreUsuarioDialog nombreDialog = new NombreUsuarioDialog(frame);
                    nombreDialog.setVisible(true);
                    nombreUsuario = nombreDialog.getNombreUsuario();
                    if (nombreUsuario != null && cliente.existeUsuario(nombreUsuario)) {
                        JOptionPane.showMessageDialog(frame, "User already exists. Try again.");
                    } else {
                        verificacion = false; //El usuario es valido, entonces entrará a la sesión
                        cliente.setUsername(nombreUsuario);
                        cliente.setUsuario(nombreUsuario, usr_direccionUnicast); //guardas en la variable Usuario
                    }
                }

                cliente.enviarUsuario(nombreUsuario, usr_direccionUnicast, "inicio");
                SwingUtilities.invokeLater(() -> {
                    // Actualiza el área de chat en el formulario
                    formulario.nombreUsuario(cliente.username); // Llama al método para agregar el mensaje en el área de chat
                });
                // Manejo del cierre de ventana
                formulario.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // No hacer nada al cerrar la ventana

                // Agregar un listener para manejar el cierre de la ventana
                formulario.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        // Aquí puedes realizar las acciones antes de cerrar la ventana
                        // Por ejemplo, puedes preguntar si el usuario realmente quiere salir
                        int opcion = JOptionPane.showConfirmDialog(formulario, "¿Estás seguro de que quieres salir?",
                                "Confirmar salida", JOptionPane.YES_NO_OPTION);
                        if (opcion == JOptionPane.YES_OPTION) {
                            // Si el usuario elige "Sí", cerrar la aplicación
                            cliente.eliminarUsuarioDeSesion(cliente.usuario_actual);

                            // Pausa de 2 segundos antes de cerrar la aplicación
                            try {
                                Thread.sleep(1000);  // 2 segundos de espera
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }

                            // Luego de la pausa, termina la aplicación
                            System.exit(0);
                        }
                        // Si el usuario elige "No", la ventana no se cierra automáticamente
                    }
                });
                //Formulario visible
                formulario.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }//main

    private void enviarUsuario(String nombre, String direccion, String tipo) {
        JSONObject mensaje = new JSONObject();
        mensaje.put("tipo", tipo); //Declaramos la variable tipo para indicar al recibidor si lo va a quitar o lo va a agregar
        mensaje.put("usr", username);

        // Crear el objeto JSON para el usuario
        JSONObject usuarioJson = new JSONObject();
        usuarioJson.put("nombre", nombre);
        usuarioJson.put("direccionUnicast", direccion);

        // Agregar el objeto del usuario directamente al mensaje
        mensaje.put("usuario_obj", usuarioJson);

        sendData(this.socket, group, mensaje.toString());
    }


    //Método para agregar un usuario a la lista
    private void setUsuario(String nombreUsuario, String direccionUnicast){
        this.usuario_actual = new Usuario(nombreUsuario, direccionUnicast);
    }

    //Método para cambiar el username
    private void setUsername(String username){
        this.username = username;
    }

    // Método para eliminar el usuario de la sesión
    private void eliminarUsuarioDeSesion(Usuario usuario) {
        // Suponiendo que 'usuarios' es el ArrayList que contiene los usuarios activos
        if (usuario != null) {
            usuarios.remove(usuario);  // Eliminar el usuario actual de la lista
            System.out.println("Usuario eliminado." + usuario.getNombre());
            this.enviarUsuario(usuario.getNombre(), usuario.getDireccionUnicast(), "salida");
        }
    }

    //Método para actualizar la lista de usuarios activos
    private void actualizarUsuarios(JSONArray usuariosRecibidos){
        //Ya que esta función solo se llama cuando se solicita una lista entera
        usuarios.clear(); //Quitamos los usuarios para evitar duplicados
        // Iterar sobre los usuarios recibidos
        for (int i = 0; i < usuariosRecibidos.length(); i++) {
            JSONObject usuarioJson = usuariosRecibidos.getJSONObject(i);
            String nombre = usuarioJson.getString("nombre");
            String direccionUnicast = usuarioJson.getString("direccionUnicast");

            // Crear el objeto Usuario y agregarlo a la lista
            usuarios.add(new Usuario(nombre, direccionUnicast));
        }
    }

    //responder con la lista de usuarios
    private void enviarListaUsuarios() {
        JSONObject mensaje = new JSONObject();
        mensaje.put("tipo", "listaUsuarios");
        mensaje.put("usr", username);

        JSONArray listaUsuarios = new JSONArray();
        for (Usuario usuario : usuarios) {
            JSONObject usuarioJson = new JSONObject();
            usuarioJson.put("nombre", usuario.getNombre());
            usuarioJson.put("direccionUnicast", usuario.getDireccionUnicast());
            listaUsuarios.put(usuarioJson);
        }

        mensaje.put("usuarios", listaUsuarios);
        sendData(this.socket, group, mensaje.toString());
    }

    // Método para comprobar si existe el usuario o no
    private boolean existeUsuario(String nombre) throws IOException {
        for (Usuario usuario : usuarios) {
            System.out.println("Probando si ya existe - Usuario: " + usuario.getNombre());
            if (usuario.getNombre().equals(nombre)) {
                return true;
            }
        }
        return false;
    }


    // Método para recibir mensajes y mostrarlos en el chat
    private void receiveMessages(MulticastSocket socket) throws IOException {
        byte[] buffer = new byte[DGRAM_BUF_LEN];
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mensajeRecibido = new String(packet.getData(), 0, packet.getLength()).trim();
                JSONObject jsonMensaje = new JSONObject(mensajeRecibido);

                String tipo = jsonMensaje.getString("tipo");
                String solicitante = jsonMensaje.getString("usr");
                if ("activos".equals(tipo) && username != null) { //es un solicitante diferente
                    System.out.println("Mensaje recibido: " + jsonMensaje);
                    this.enviarListaUsuarios();
                }else if("inicio".equals(tipo)){
                    // Extraer el usuario del mensaje para eliminarlo
                    System.out.println("Mensaje recibido: " + jsonMensaje);
                    JSONObject usuarioJson = jsonMensaje.getJSONObject("usuario_obj");
                    agregarUsuario(usuarioJson);
                    SwingUtilities.invokeLater(() -> {
                        // Actualiza el área de chat en el formulario
                        formulario.actualizarUsuarios(usuarios); // Llama al método para agregar el mensaje en el área de chat
                    });
                } else if ("salida".equals(tipo) && !username.equals(solicitante)) { //es un solicitante diferente
                    // Extraer el usuario del mensaje para eliminarlo
                    System.out.println("Mensaje recibido: " + jsonMensaje);
                    JSONObject usuarioJson = jsonMensaje.getJSONObject("usuario_obj");
                    eliminarUsuario(usuarioJson);
                    SwingUtilities.invokeLater(() -> {
                        // Actualiza el área de chat en el formulario
                        formulario.actualizarUsuarios(usuarios); // Llama al método para agregar el mensaje en el área de chat
                    });
                } else if ("message".equals(tipo)){
                    // Extraer el contenido
                    String contenido = jsonMensaje.getString("content");
                    System.out.println("Mensaje recibido: " + contenido);
                    // Actualiza la interfaz gráfica con el mensaje recibido
                    SwingUtilities.invokeLater(() -> {
                        // Actualiza el área de chat en el formulario
                        String sender = solicitante + " says: ";
                        if(solicitante.equals(username)){
                            sender = "You say:";
                        }
                        formulario.actualizarChat(sender, contenido); // Llama al método para agregar el mensaje en el área de chat
                    });
                } else if ("private_message".equals(tipo)) {
                    // Extraer el contenido
                    String remitente = jsonMensaje.getString("recipient");
                    if(remitente.equals(username) || solicitante.equals(username)){//si el mensaje es para el user actual o para el que lo mando
                        String contenido = jsonMensaje.getString("content");
                        System.out.println("Mensaje recibido: " + contenido);
                        // Actualiza la interfaz gráfica con el mensaje recibido
                        SwingUtilities.invokeLater(() -> {
                            // Actualiza el área de chat en el formulario
                            String sender = solicitante + " says to "+ remitente +":";
                            if(solicitante.equals(username)){
                                sender = "You say to "+ remitente +":";
                            }
                            formulario.actualizarPrivado(sender, contenido); // Llama al método para agregar el mensaje en el área de chat
                        });
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void eliminarUsuario(JSONObject usuarioJson) {
        String nombre = usuarioJson.getString("nombre");
        String direccionUnicast = usuarioJson.getString("direccionUnicast");
        // Buscar y eliminar el usuario en la lista 'usuarios' basado en nombre y dirección
        usuarios.removeIf(usuario -> usuario.getNombre().equals(nombre) && usuario.getDireccionUnicast().equals(direccionUnicast));
    }

    private void agregarUsuario(JSONObject usuarioJson) {
        String nombre = usuarioJson.getString("nombre");
        String direccionUnicast = usuarioJson.getString("direccionUnicast");
        // Buscar y eliminar el usuario en la lista 'usuarios' basado en nombre y dirección
        usuarios.add(new Usuario(nombre, direccionUnicast));
        System.out.println("Usuario agregado: " +  nombre);
    }


    //Método para solicitar la lista de usuarios activos
    private void solicitarUsuarios(String nombre){
        JSONObject mensaje = new JSONObject();
        mensaje.put("tipo", "activos");
        mensaje.put("usr", Objects.requireNonNullElse(nombre, "null"));
        sendData(this.socket, group, mensaje.toString());
    }

    // Método para enviar un mensaje de texto
    public void sendMessage(String message, String type) {
        JSONObject json = new JSONObject();
        json.put("tipo", type);
        json.put("usr", username);
        json.put("content", message);
        sendData(socket, group, json.toString());
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
        json.put("tipo", "emoji");
        json.put("content", emoji);

        sendData(socket, group, json.toString());
    }

    // Método para enviar un mensaje privado
    public void sendPrivateMessage(String receptor, String privateMessage) {
        JSONObject json = new JSONObject();
        json.put("tipo", "private_message");
        json.put("usr", this.username);
        json.put("recipient", receptor);
        json.put("content", privateMessage);

        sendData(socket, group, json.toString());
    }

    // Método auxiliar para enviar datos al grupo multicast
    private static void sendData(MulticastSocket socket, InetAddress group, String data) {
        try {
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            System.out.println("Mensaje enviado: " + data); //Imprime el json enviado.
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
