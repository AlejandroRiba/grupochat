package org.example.chat;

import org.example.ChatClient;
import org.example.clases.Usuario;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
        String ruta = f.getAbsolutePath();
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
        // Crear el JPopupMenu para los emojis
        JPopupMenu emojiMenu = new JPopupMenu();

        // Ruta de los recursos de los emojis
        String rutaEmojis = "/emojis/";

        // Crear los iconos de los emojis desde los recursos
        ImageIcon emoji1 = new ImageIcon(Objects.requireNonNull(getClass().getResource(rutaEmojis + "emoji1.gif")));
        ImageIcon emoji2 = new ImageIcon(Objects.requireNonNull(getClass().getResource(rutaEmojis + "emoji2.gif")));
        ImageIcon emoji3 = new ImageIcon(Objects.requireNonNull(getClass().getResource(rutaEmojis + "emoji3.gif")));
        ImageIcon emoji4 = new ImageIcon(Objects.requireNonNull(getClass().getResource(rutaEmojis + "emoji4.gif")));
        ImageIcon emoji5 = new ImageIcon(Objects.requireNonNull(getClass().getResource(rutaEmojis + "emoji5.gif")));
        ImageIcon emoji6 = new ImageIcon(Objects.requireNonNull(getClass().getResource(rutaEmojis + "emoji6.gif")));

        // Limitar el tamaño de los emojis
        int ancho = 50; // Ancho deseado
        int alto = 53;  // Alto deseado

        // Redimensionar los emojis para que no sean gigantes en el menú
        emoji1 = new ImageIcon(emoji1.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));
        emoji2 = new ImageIcon(emoji2.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));
        emoji3 = new ImageIcon(emoji3.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));
        emoji4 = new ImageIcon(emoji4.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));
        emoji5 = new ImageIcon(emoji5.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));
        emoji6 = new ImageIcon(emoji6.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT));

        // Crear un JPanel con GridLayout para organizar los emojis
        JPanel emojiPanel = new JPanel(new GridLayout(0, 3, 2, 2)); // 0 filas, 3 columnas, 5px de espacio entre cada elemento

        // Crear los JMenuItems sin texto, solo con el icono
        JMenuItem emojiItem1 = new JMenuItem("", emoji1);
        JMenuItem emojiItem2 = new JMenuItem("", emoji2);
        JMenuItem emojiItem3 = new JMenuItem("", emoji3);
        JMenuItem emojiItem4 = new JMenuItem("", emoji4);
        JMenuItem emojiItem5 = new JMenuItem("", emoji5);
        JMenuItem emojiItem6 = new JMenuItem("", emoji6);

        // Acción cuando se selecciona un emoji
        emojiItem1.addActionListener(e -> insertarEmoji("<img src=\"emoji1.gif\" width=\"30\" height=\"30\">"));
        emojiItem2.addActionListener(e -> insertarEmoji("<img src=\"emoji2.gif\" width=\"30\" height=\"30\">"));
        emojiItem3.addActionListener(e -> insertarEmoji("<img src=\"emoji3.gif\" width=\"30\" height=\"30\">"));
        emojiItem4.addActionListener(e -> insertarEmoji("<img src=\"emoji4.gif\" width=\"30\" height=\"30\">"));
        emojiItem5.addActionListener(e -> insertarEmoji("<img src=\"emoji5.gif\" width=\"30\" height=\"30\">"));
        emojiItem6.addActionListener(e -> insertarEmoji("<img src=\"emoji6.gif\" width=\"30\" height=\"30\">"));

        // Añadir los JMenuItems al JPanel
        emojiPanel.add(emojiItem1);
        emojiPanel.add(emojiItem2);
        emojiPanel.add(emojiItem3);
        emojiPanel.add(emojiItem4);
        emojiPanel.add(emojiItem5);
        emojiPanel.add(emojiItem6);

        // Envolver el JPanel en un JScrollPane para hacerlo desplazable
        JScrollPane scrollPane = new JScrollPane(emojiPanel);
        scrollPane.setPreferredSize(new Dimension(200, 200)); // Ajustar el tamaño del JScrollPane
        // Agregar el JScrollPane al JPopupMenu
        emojiMenu.add(scrollPane);

        // Mostrar el menú de emojis cuando se haga clic en el botón
        emojiMenu.show(emojisButton, 0, emojisButton.getHeight());
    }

    private void insertarEmoji(String source) {
        System.out.println("Source: " + source);
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
