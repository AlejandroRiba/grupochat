package org.example.chat;

import org.example.ChatClient;
import org.example.clases.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class Principal extends JFrame {
    private JPanel mainPanel;
    private JLabel txtmensaje;
    private JTextField fieldMessage;
    private JEditorPane editorUsers;
    private JEditorPane editorChat;
    private JButton sendButton;
    private JButton fileButton;
    private JButton emojisButton;
    private JLabel txtchat;
    private JLabel txtusers;
    private JLabel usuario;
    private JPanel usuariosPanel;

    private ChatClient cliente;

    String mensaje_inicio = "", mensaje_medio = "", mensaje_final = "";
    String mensaje_inicio1 = "";

    private ArrayList<Usuario> usuarios = new ArrayList<>(); // Lista de objetos Usuario
    String path,tmp_u="",tmp_m="";

    public Principal(ChatClient cliente){
        setContentPane(mainPanel);  // Usa el mainPanel creado en el .form
        setTitle("Interfaz de Chat");
        pack();       // Tamaño de la ventana
        setResizable(false);
        setLocationRelativeTo(null); // Centrar en la pantalla
        File f = new File("");
        String ruta = f.getAbsolutePath().replace("\\", "/");
        path=ruta;
        mensaje_inicio= "<head><base href=\"file:"+ruta+"\\\">"+
                "<style>#usuarios {"+
                "font-family: sans-serif;"+
                "border-collapse: collapse;"+
                "width: 100%;"+
                "} #usuarios td, #usuarios th {"+
                "border: 0px solid #ddd;"+
                " padding: 8px;"+
                "}#usuarios tr:nth-child(even){background-color: #f2f2f2;}"+
                "#usuarios tr:hover {background-color: #ddd;}"+
                "#usuarios th {"+
                " padding-top: 12px;"+
                "padding-bottom: 12px;"+
                "text-align: left;"+
                "background-color: #04AA6D;"+
                "color: black;}"+
                "</style>"+
                "</head>"+
                "<table id=\"mensajes\">\n";

        mensaje_inicio1= "<head><base href=\"file:"+ruta+"\\\">"+
                "<style>#usuarios {"+
                "font-family: sans-serif;"+
                "border-collapse: collapse;"+
                "width: 100%;"+
                "} #usuarios td, #usuarios th {"+
                "border: 0px solid #ddd;"+
                " padding: 8px;"+
                "}#usuarios tr:nth-child(even){background-color: #f2f2f2;}"+
                "#usuarios tr:hover {background-color: #ddd;}"+
                "#usuarios th {"+
                " padding-top: 12px;"+
                "padding-bottom: 12px;"+
                "text-align: left;"+
                "background-color: #04AA6D;"+
                "color: black;}"+
                "</style>"+
                "</head>"+
                "<table id=\"usuarios\">\n";

        mensaje_final="</table>";
        this.repaint();
        this.cliente = cliente;
        //Metodo para enviar un mensaje de texto
        sendButton.addActionListener(e -> enviarMensaje());
        emojisButton.addActionListener(e -> listarEmojis());
    }

    private void listarEmojis() {
        // Crear el JPanel que contendrá los emojis
        JPanel emojiPanel = new JPanel();
        emojiPanel.setLayout(new GridLayout(0, 3, 5, 5)); // Para organizar en filas y columnas

        // Ruta de los recursos de los emojis
        String rutaEmojis = "/emojis/";

        // Crear un array de nombres de archivos de emojis (esto permite añadir más fácilmente)
        String[] emojis = {"emoji1.gif", "emoji2.gif", "emoji3.gif", "emoji4.gif", "emoji5.gif", "emoji6.gif", "emoji7.gif", "emoji8.gif", "emoji9.gif", "emoji10.gif", "emoji11.gif", "emoji12.gif"};

        int ancho = 50; // Ancho deseado
        int alto = 53;  // Alto deseado

        // Crear el JDialog para mostrar el panel de emojis
        final JDialog emojiDialog = new JDialog();
        emojiDialog.setTitle("Emojis");

        // Agregar los emojis al panel
        for (String emoji : emojis) {
            try {
                ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(rutaEmojis + emoji)));
                // Redimensionar el icono
                icon = new ImageIcon(icon.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));
                JLabel emojiLabel = new JLabel(icon);
                emojiLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                // Cambiar el borde y el fondo al pasar el mouse sobre el JLabel
                emojiLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        emojiLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1)); // Cambia el borde
                        emojiLabel.setOpaque(true);
                        emojiLabel.setBackground(new Color(220, 220, 220)); // Cambia el fondo
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        emojiLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Restaura el borde
                        emojiLabel.setBackground(null); // Quita el fondo
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        insertarEmoji(emoji);
                        emojiDialog.dispose(); // Cierra la ventana de emojis
                    }
                });

                emojiPanel.add(emojiLabel); // Añadir cada emoji al panel
            } catch (NullPointerException ex) {
                System.err.println("Emoji no encontrado: " + emoji);
            }
        }

        // Crear el JScrollPane y establecer dimensiones
        JScrollPane emojiScrollPane = new JScrollPane(emojiPanel);
        emojiScrollPane.setPreferredSize(new Dimension(200, 200)); // Tamaño fijo para permitir scroll
        emojiScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        emojiScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Visbile el dialog
        emojiDialog.getContentPane().add(emojiScrollPane);
        emojiDialog.pack();
        emojiDialog.setLocationRelativeTo(emojisButton); // Posición relativa al botón
        emojiDialog.setVisible(true);

        // Agregar un MouseListener al JFrame o contenedor principal para cerrar el JDialog si se hace clic fuera de él
        emojiDialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                emojiDialog.dispose(); // Cierra el diálogo si pierde el foco
            }
        });

        // Detectar clic fuera del JDialog
        emojiDialog.getRootPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!emojiDialog.isAncestorOf(e.getComponent())) {
                    emojiDialog.dispose(); // Cerrar el JDialog si el clic es fuera de él
                }
            }
        });

    }

    private void insertarEmoji(String source) {
        System.out.println("Source: " + source);
        cliente.sendEmoji(source);
    }

    public void enviarMensaje(){
        String mensaje = fieldMessage.getText();
        if(!mensaje.isEmpty()){
            fieldMessage.setText("");
            cliente.sendMessage(mensaje, "message");
            this.repaint();
        }else{
            System.out.println("Campo del mensaje vacío, no debe hacer nada.");
        }
    }

    public void actualizarChat(String usuario, String mensaje){
        if(!mensaje.isEmpty() && !usuario.isEmpty()){
            mensaje_medio=  mensaje_medio + "  <tr>\n" +
                    "    <td>"+usuario+"</td>\n" +
                    "    <td>"+mensaje+"</td>\n" +
                    "  </tr>";
            editorChat.setText(mensaje_inicio+mensaje_medio+mensaje_final);
            this.repaint();
        }
    }

    public void actualizarChatEmoji(String usuario, String contenido){
        if(!contenido.isEmpty() && !usuario.isEmpty()){
            // Ruta base de los emojis
            URL emojiURL = getClass().getResource("/emojis/" + contenido);

            // Construir el mensaje HTML para el emoji
            assert emojiURL != null;
            mensaje_medio = mensaje_medio + "  <tr>\n" +
                    "    <td>" + usuario + "</td>\n" +
                    "    <td>" + "<img src=\"" + emojiURL + "\" width=\"50\" height=\"50\"></img>" + "</td>\n" +
                    "  </tr>";
            editorChat.setText(mensaje_inicio+mensaje_medio+mensaje_final);
            this.repaint();
        }
    }

    public void actualizarPrivado(String usuario, String mensaje){
        if(!mensaje.isEmpty() && !usuario.isEmpty()){
            mensaje_medio=  mensaje_medio + "  <tr>\n" +
                    "    <td style='color: red;'>"+usuario+"</td>\n" +
                    "    <td>"+mensaje+"</td>\n" +
                    "  </tr>";
            editorChat.setText(mensaje_inicio+mensaje_medio+mensaje_final);
            this.repaint();
        }
    }

    public void actualizarUsuarios(ArrayList<Usuario> usuarios){
        // Limpiar el panel de usuarios existente
        usuariosPanel.removeAll();

        // Establecer un BoxLayout para que los usuarios se apilen verticalmente
        usuariosPanel.setLayout(new BoxLayout(usuariosPanel, BoxLayout.Y_AXIS));

        for (Usuario usuario : usuarios) {
            JPanel usuarioPanel = new JPanel();
            usuarioPanel.setLayout(new BoxLayout(usuarioPanel, BoxLayout.X_AXIS));  // Apilar elementos en fila dentro de cada panel de usuario

            JLabel usuarioLabel = new JLabel(usuario.getNombre());
            JButton mensajePrivadoBtn = new JButton("Private Message");

            // Agregar acción al botón de mensaje privado
            mensajePrivadoBtn.addActionListener(e -> enviarMensajePrivado(usuario));

            usuarioPanel.add(usuarioLabel);
            usuarioPanel.add(mensajePrivadoBtn);
            usuariosPanel.add(usuarioPanel);
        }

        usuariosPanel.revalidate();
        usuariosPanel.repaint();
    }

    public void nombreUsuario(String nombre){
        usuario.setText(nombre);
    }
    public void setCliente(ChatClient cliente){
        this.cliente = cliente;
    }

    private void enviarMensajePrivado(Usuario destinatario){
        String mensaje = JOptionPane.showInputDialog(null,
                "Escribe tu mensaje privado para " + destinatario.getNombre() + ":");
        if (mensaje != null && !mensaje.isEmpty()) {
            cliente.sendPrivateMessage(destinatario.getNombre(),mensaje);
        }
    }

}
