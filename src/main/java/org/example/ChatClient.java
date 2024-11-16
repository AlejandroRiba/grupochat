package org.example;

import org.example.chat.NombreUsuarioDialog;
import org.example.chat.Principal;
import org.example.clases.Usuario;
import org.json.JSONException;
import org.json.JSONObject;
import  org.json.JSONArray;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClient extends Thread{

    private final Principal formulario; // Referencia a la interfaz gráfica
    private static final int PORT = 9331;
    private static final String ADDR = "230.1.1.1"; //IPV4
    //"ff3e:40:2001::1" //IPV6
    public static final int DGRAM_BUF_LEN = 4096;

    private static final int TAMANO_VENTANA = 3;

    private static final int TIMEOUT = 3000;


    // Lista de usuarios conectados en este cliente
    private ArrayList<Usuario> usuarios = new ArrayList<>();
    private String username;

    private Usuario usuario_actual;
    private MulticastSocket socket;
    private InetAddress group;

    // Mapa para almacenar fragmentos de archivos en proceso de recepción
    private final Map<String, Map<Integer, Boolean>> userAcks = new ConcurrentHashMap<>();
    private final Map<String, Integer> paquetesTotalesPorArchivo = new ConcurrentHashMap<>();

    private final Map<String, String> archivosUsuario = new ConcurrentHashMap<>(); //Archivos y quien lo envia

    private FragmentHandler handler;

    public ChatClient(String username, Principal formulario) throws IOException {
        this.username = username;
        this.formulario = formulario; // Asigna el formulario al campo
        this.socket = new MulticastSocket(PORT);
        this.group = InetAddress.getByName(ADDR);
        SocketAddress mcastAddr = new InetSocketAddress(group, PORT);
        // Especifica la interfaz
        NetworkInterface netIf = NetworkInterface.getByName("wlan3"); //eth1wlan3"
        socket.joinGroup(mcastAddr, netIf);
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
                break;
            }

            // Esperar la respuesta
            try {
                socket.receive(respuesta);  // Esto bloquea el hilo hasta que se recibe el paquete
            } catch (SocketTimeoutException e) {
                // Se produce si el socket no recibe respuesta en el tiempo especificado
                System.out.println("Timeout: No se recibió respuesta en el tiempo especificado.");
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
                        cliente.handler = new FragmentHandler(nombreUsuario);
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
                            // Cerrar la ventana de inmediato
                            formulario.dispose(); // Esto cierra la ventana
                            // Luego de la pausa, termina la aplicación
                            System.exit(0);
                        }
                        // Si el usuario elige "No", la ventana no se cierra automáticamente
                    }
                });
                //Formulario visible
                formulario.setVisible(true);


                // Agregar un shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("La aplicación se está cerrando. Realizando limpieza...");
                    //Elimina carpeta temporal
                    try {
                        cliente.handler.eliminarCarpetaTemporal();
                    } catch (IOException ex) {
                        System.out.println("Error al eliminar la carpeta temp");
                    }
                }));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }//main

    private void enviarUsuario(String nombre, String direccion, String tipo) {
        JSONObject mensaje = new JSONObject();
        String contenido;
        mensaje.put("public", "all");
        mensaje.put("tipo", tipo); //Declaramos la variable tipo para indicar al recibidor si lo va a quitar o lo va a agregar
        mensaje.put("usr", username);
        if("inicio".equals(tipo)){
            contenido = "Has joined the chat.";
        }else{
            contenido = "Has left the room.";
        }
        mensaje.put("content", contenido);

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
    private boolean existeUsuario(String nombre) {
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
        byte[] buffer = new byte[65507];  // Máximo tamaño de paquete UDP
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mensajeRecibido = new String(packet.getData(), 0, packet.getLength()).trim();
                try{
                    JSONObject jsonMensaje = new JSONObject(mensajeRecibido);

                    String tipo = jsonMensaje.optString("tipo", ""); // tipo de mensaje
                    String publico = jsonMensaje.optString("public", ""); // para quien va dirigido
                    String solicitante = jsonMensaje.optString("usr", ""); // quien lo manda
                    String contenido = jsonMensaje.optString("content", ""); // Extraer el contenido
                    String remitente = jsonMensaje.optString("recipient", ""); // el remitente si existe
                    String filename = jsonMensaje.optString("filename", ""); // nombre del archivo
                    int packetNumber = jsonMensaje.optInt("packetNumber", 0);
                    int totalPackets = jsonMensaje.optInt("totalPackets", 1);
                    boolean isLastPacket = jsonMensaje.optBoolean("end", false);
                    String sender; //Para la interfaz gráfica
                    if("all".equals(publico)){
                        if(solicitante.equals(username)){
                            sender = "You say: ";
                        } else {
                            sender = solicitante + " says: ";
                        }
                        System.out.println("Mensaje recibido: " + jsonMensaje);
                        if ("activos".equals(tipo) && username != null) { //es un solicitante diferente
                            this.enviarListaUsuarios();
                        } else if("inicio".equals(tipo)){
                            // Extraer el usuario del mensaje para agregar a la lista
                            JSONObject usuarioJson = jsonMensaje.getJSONObject("usuario_obj");
                            agregarUsuario(usuarioJson);
                            SwingUtilities.invokeLater(() -> {
                                // Actualiza el área de chat en el formulario
                                formulario.actualizarUsuarios(usuarios); // Llama al método para agregar el mensaje en el área de chat
                                formulario.actualizarChat(solicitante, contenido, publico, "inicio");
                            });
                        } else if ("salida".equals(tipo) && !username.equals(solicitante)) { //es un solicitante diferente
                            JSONObject usuarioJson = jsonMensaje.getJSONObject("usuario_obj");
                            eliminarUsuario(usuarioJson);
                            SwingUtilities.invokeLater(() -> {
                                // Actualiza el área de chat en el formulario
                                formulario.actualizarUsuarios(usuarios); // Llama al método para agregar el mensaje en el área de chat
                                formulario.actualizarChat(solicitante, contenido, publico, "salida");
                            });
                        } else if ("message".equals(tipo) || "emoji".equals(tipo)){
                            // Actualiza la interfaz gráfica con el mensaje recibido
                            SwingUtilities.invokeLater(() -> {
                                // Actualiza el área de chat en el formulario
                                formulario.actualizarChat(sender, contenido, publico, tipo); // Llama al método para agregar el mensaje en el área de chat
                            });
                        } else if("file".equals(tipo) && username != null){
                            // Verificar si el archivo ya está en proceso
                            if (!archivosUsuario.containsKey(filename)) {
                                // Si no existe, inicializa el archivo y los fragmentos
                                paquetesTotalesPorArchivo.put(filename, totalPackets);
                            }

                            if(isLastPacket && !archivosUsuario.containsKey(filename)){ //hasta que recibe el último paqute de ese archivo manda a actualizar la lista
                                archivosUsuario.put(filename, solicitante); //lo agregamos al arreglo que contiene el nombre del archivo y quien lo manda
                                SwingUtilities.invokeLater(() -> {
                                    formulario.addItemToList(filename);
                                });
                            }

                            //decodificamos el contenido de base64 para byte
                            byte[] data = Base64.getDecoder().decode(contenido);
                            handler.guardarFragmento(filename, packetNumber, data);
                            //Responde con un acuse a quien nos envia el archivo junyo con el numero de paquete
                            sendAck(solicitante, packetNumber);
                        }
                    } else if ("private".equals(publico) && (remitente.equals(username) || solicitante.equals(username))) { //si el mensaje es para el user actual o para el que lo mando
                        if("ack".equals(tipo) && remitente.equals(username)){ //Es un acuse el recibido y va dirigido al usuario actual
                            handleAck(solicitante, packetNumber);
                        }else{
                            if(solicitante.equals(username)) { //el que envia el mensaje es el usuario mismo
                                if (remitente.equals(username)) {  //el que recibe
                                    sender = "You say privately\n to you: ";
                                } else {
                                    sender = "You say privately\n to " + remitente + " : "; //el que recibe es alguien más
                                }
                            }else { //el que envia el mensaje es alquien más
                                if (remitente.equals(username)) {
                                    sender = solicitante + " says privately\n to you : ";
                                } else{
                                    sender = solicitante + " says privately\n to " + remitente + " : ";
                                }
                            }
                            if("message".equals(tipo) || "emoji".equals(tipo)){
                                // Actualiza la interfaz gráfica con el mensaje recibido
                                SwingUtilities.invokeLater(() -> {
                                    // Actualiza el área de chat en el formulario
                                    formulario.actualizarChat(sender, contenido, publico, tipo); // Llama al método para agregar el mensaje en el área de chat
                                });
                            }
                        }
                    }
                }catch (JSONException e) {
                    // Si el mensaje no es un JSON válido, lo ignoramos y continuamos
                    System.out.println("Error en el JSON recibido: " + mensajeRecibido);
                }
            } catch (IOException ignored) { }
        }
    }

    public void downloadFile(String filename){
        //Extraemos el total de paquetes
        Integer totalPackets = paquetesTotalesPorArchivo.get(filename);
        try {
            handler.reconstruirArchivo(filename,totalPackets);
            SwingUtilities.invokeLater(() -> {
                // Muestra una ventana emergente confirmando la descarga
                formulario.mensajeConfirmacion(filename + "File downloaded successfully.");
            });
        } catch (IOException e){
            System.out.println("Error con la descarga del archivo");
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
        mensaje.put("public", "all");
        mensaje.put("tipo", "activos");
        mensaje.put("usr", Objects.requireNonNullElse(nombre, "null"));
        sendData(this.socket, group, mensaje.toString());
    }

    // Método para enviar un mensaje GRUPAL
    public void sendMessage(String message, String type) { //TYPOE = message, emoji, file, activos
        JSONObject json = new JSONObject();
        json.put("public", "all");
        json.put("tipo", type);
        json.put("usr", username);
        json.put("content", message);
        sendData(socket, group, json.toString());
    }

    // Método para enviar un archivo
    public void sendFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[DGRAM_BUF_LEN];
            int totalPackets = (int) Math.ceil(file.length() / (double) DGRAM_BUF_LEN);
            int packetNumber = 1;

            // Leer el archivo y dividirlo en paquetes
            List<byte[]> packets = new ArrayList<>();
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                packets.add(Arrays.copyOf(buffer, bytesRead));
            }

            // Inicializar estructura de ACKs
            for (Usuario user : usuarios) { // Lista de usuarios activos
                userAcks.put(user.getNombre(), new ConcurrentHashMap<>());
                for (int i = 1; i <= totalPackets; i++) {
                    userAcks.get(user.getNombre()).put(i, false); // Ningún paquete ha sido confirmado aún
                }
            }

            // Control de la ventana deslizante
            int windowStart = 0;
            while (windowStart < totalPackets) {
                // Enviar paquetes dentro de la ventana
                for (int i = windowStart; i < Math.min(windowStart + TAMANO_VENTANA, totalPackets); i++) {
                    for (Usuario user : usuarios) {
                        if (!userAcks.get(user.getNombre()).getOrDefault(i + 1, false)) {
                            sendPacket(packets.get(i), i + 1, totalPackets, file.getName(), user.getNombre());
                        }
                    }
                }

                // Esperar ACKs o retransmitir
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < TIMEOUT) {
                    if (allAcksReceived(windowStart + 1, windowStart + TAMANO_VENTANA)) {
                        break;
                    }
                }

                // Mover la ventana si se recibieron los ACKs esperados
                while (allUsersAckedPacket(windowStart + 1)) {
                    windowStart++;
                }
            }

            sendMessage("New file! - " + file.getName(), "message"); //Hasta que se envia correctamente notifica del archivo nuevo
            System.out.println("Archivo enviado correctamente a todos los usuarios.");
        }
    }


    //Método que comprueba si todos los acuses fueron recibidos
    private boolean allAcksReceived(int start, int end) {
        for (Usuario user : usuarios) {
            for (int i = start; i <= end; i++) {
                if (!userAcks.get(user.getNombre()).getOrDefault(i, false)) {
                    return false;
                }
            }
        }
        return true;
    }

    //Método que comprueba que todos los usuarios marcaron como recibido el paquete en especifico
    private boolean allUsersAckedPacket(int packetNumber) {
        for (Usuario user : usuarios) {
            if (!userAcks.get(user.getNombre()).getOrDefault(packetNumber, false)) {
                return false;
            }
        }
        return true;
    }

    //Método para enviar cada paquete de archivo
    private void sendPacket(byte[] data, int packetNumber, int totalPackets, String filename, String recipient) throws IOException {
        JSONObject json = new JSONObject();
        // Agregar información sobre el destinatario
        json.put("public", "all");
        json.put("tipo", "file");
        json.put("usr", username); // Remitente

        // Datos del paquete
        String encodedContent = Base64.getEncoder().encodeToString(data); // Codificar contenido
        json.put("filename", filename);
        json.put("packetNumber", packetNumber);
        json.put("totalPackets", totalPackets);
        json.put("content", encodedContent);

        // Indicador de fin
        json.put("end", packetNumber == totalPackets);

        // Enviar el JSON como string
        sendData(socket, group, json.toString());
    }

    //Método para enviar el acuse
    private void sendAck(String destino, Integer packetNumber){
        // Enviar ACK
        JSONObject ack = new JSONObject();
        ack.put("public", "private");
        ack.put("tipo", "ack");
        ack.put("usr", username);
        ack.put("recipient", destino);
        ack.put("packetNumber", packetNumber);

        sendData(socket, group, ack.toString());
    }

    //Método para actualizar el acuse recibido
    public void handleAck(String user, Integer packetNumber) {
        // Marcar el paquete como recibido por este usuario
        if (userAcks.containsKey(user)) {
            userAcks.get(user).put(packetNumber, true);
        }
    }

    // Método para enviar un mensaje privado
    public void sendPrivateMessage(String receptor, String privateMessage, String type) {
        JSONObject json = new JSONObject();
        json.put("public", "private");
        json.put("tipo", type);
        json.put("usr", username);
        json.put("recipient", receptor);
        json.put("content", privateMessage);

        sendData(socket, group, json.toString());
    }

    // Método auxiliar para enviar datos al grupo multicast
    private static void sendData(MulticastSocket socket, InetAddress group, String data) {
        try {
            byte[] buffer = data.getBytes(StandardCharsets.UTF_8);  // Especifica UTF-8 para la conversión a bytes
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);

            // Verificar que el tamaño del paquete no exceda el máximo permitido (65507 bytes)
            if (buffer.length > 65507) {
                System.err.println("El mensaje es demasiado grande para enviarse en un solo paquete.");
                return; // O podrías implementar lógica de fragmentación adicional
            }
            System.out.println("Mensaje enviado: " + data); // Imprime el JSON enviado.
            socket.send(packet); //Envia el paquete

        } catch (IOException e) {
            System.err.println("Error al enviar el paquete: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
