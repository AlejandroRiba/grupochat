package org.example.chat;

import org.example.ChatClient;
import org.example.clases.Usuario;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
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
    private JLabel labelFiles;

    private JComboBox<String> archivos;
    private ChatClient cliente;

    String mensaje_inicio, mensaje_medio, mensaje_final;
    String mensaje_inicio1;
    String path;


    public Principal(ChatClient cliente){
        setContentPane(mainPanel);  // Usa el mainPanel creado en el .form
        setTitle("Chat");
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
        mensaje_medio = "";
        this.repaint();
        this.cliente = cliente;
        // Agregar una opción predeterminada que no sea seleccionable
        archivos.addItem("Select a file...");
        // Hacer que la primera opción sea solo un "placeholder"
        archivos.setSelectedIndex(0); // Seleccionar por defecto el primer ítem (placeholder)
        //Metodo para enviar un mensaje de texto
        sendButton.addActionListener(e -> enviarMensaje());
        emojisButton.addActionListener(e -> listarEmojis());
        fileButton.addActionListener(e -> selectAndSendFile());
        // Crear un MouseListener para detectar clic derecho
        archivos.addActionListener(e -> mostrarMenuEmergente());
    }

    // Método para mostrar el menú emergente con la opción de descargar
    private void mostrarMenuEmergente() {
        if(archivos.getSelectedIndex() != 0){
            // Mostrar un JOptionPane de confirmación
            int respuesta = JOptionPane.showConfirmDialog(null,
                    "¿Deseas descargar el archivo " + archivos.getSelectedItem() + "?",
                    "Confirmación de descarga", JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_OPTION) {
                // Aquí puedes agregar el código para manejar la descarga
                System.out.println("Descargando " + archivos.getSelectedItem());
            }
        }
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
        cliente.sendMessage(source, "emoji");
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

    public void actualizarChat(String usuario, String mensaje, String publico, String tipo){
        String style;
        String fila;
        if("all".equals(publico)){
            style = "#12067c";
        }else{
            style = "red";
        }
        if("emoji".equals(tipo)){
            // Ruta base de los emojis
            URL emojiURL = getClass().getResource("/emojis/" + mensaje);

            // Construir el mensaje HTML para el emoji
            assert emojiURL != null;
            fila = "    <td>" + "<img src=\"" + emojiURL + "\" width=\"62\" height=\"62\"></img>" + "</td>\n";
        }else{
            fila ="    <td>"+mensaje+"</td>\n";
        }
        if(!mensaje.isEmpty() && !usuario.isEmpty()){
            mensaje_medio=  mensaje_medio + "  <tr>\n" +
                    "    <td  style='color: "+style+";'><b>"+usuario+"</b></td>\n" +
                    fila +
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

        // Ajuste de alineación
        usuariosPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Establecer un tamaño mínimo al panel de usuarios
        usuariosPanel.add(Box.createVerticalStrut(6)); // Espacio vertical entre los paneles

        for (Usuario usuario : usuarios) {
            JPanel usuarioPanel = new JPanel();
            usuarioPanel.setLayout(new BoxLayout(usuarioPanel, BoxLayout.X_AXIS));  // Apilar elementos en fila dentro de cada panel de usuario
            usuarioPanel.setOpaque(false); // Fondo transparente para el panel

            // Crear un JLabel con un punto verde
            JLabel puntoVerde = new JLabel();
            puntoVerde.setOpaque(true);
            puntoVerde.setBackground(Color.GREEN);
            puntoVerde.setPreferredSize(new Dimension(10, 10)); // Tamaño del punto
            puntoVerde.setMaximumSize(new Dimension(10, 10)); // Fijar el tamaño

            JLabel usuarioLabel = new JLabel(usuario.getNombre());
            usuarioLabel.setPreferredSize(new Dimension(50, 20)); // Fijar el tamaño para el nombre
            usuarioLabel.setMaximumSize(new Dimension(50, 20)); // Fijar el tamaño máximo
            usuarioLabel.setBackground(new Color(246, 241, 215));
            JButton mensajePrivadoBtn = getMensajePrivadoBtn(usuario);

            // Agregar los componentes al panel del usuario
            usuarioPanel.add(puntoVerde); // Agrega el punto verde primero
            usuarioPanel.add(Box.createHorizontalStrut(5)); // Espacio entre el punto y el nombre
            usuarioPanel.add(usuarioLabel);
            usuarioPanel.add(mensajePrivadoBtn);
            usuariosPanel.add(usuarioPanel);

            // Agregar un espacio entre los paneles
            usuariosPanel.add(Box.createVerticalStrut(6)); // Espacio vertical entre los paneles
        }

        usuariosPanel.revalidate();
        usuariosPanel.repaint();
    }

    private JButton getMensajePrivadoBtn(Usuario usuario) {
        // Cargar el ícono desde la carpeta resources/img
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/emojis/privado.png"))); // Ruta correcta según la estructura del proyecto
        // Redimensionar el icono
        icon = new ImageIcon(icon.getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
        // Crear el JButton con el ícono (sin texto)
        JButton mensajePrivadoBtn = new JButton(icon);
        mensajePrivadoBtn.setPreferredSize(new Dimension(70, 32)); // Ajusta el tamaño para que se vea bien
        mensajePrivadoBtn.setMaximumSize(new Dimension(70, 32)); // Fijar el tamaño máximo
        // Agregar acción al botón de mensaje privado
        mensajePrivadoBtn.addActionListener(e -> enviarMensajePrivado(usuario));
        return mensajePrivadoBtn;
    }

    public void nombreUsuario(String nombre){
        usuario.setText("Session: " + nombre);
    }
    public void setCliente(ChatClient cliente){
        this.cliente = cliente;
    }

    private void enviarMensajePrivado(Usuario destinatario){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        PrivadoDialog dialog = new PrivadoDialog(frame);
        dialog.setVisible(true);

        String mensaje = dialog.getMessage();
        if (mensaje != null && !mensaje.isEmpty()) {
            String tipo = dialog.getTipo();
            System.out.println(tipo);
            if("message".equals(tipo)){
                cliente.sendPrivateMessage(destinatario.getNombre(),mensaje,"message");
            } else if ("emoji".equals(tipo)) { //Tipo emoji
                cliente.sendPrivateMessage(destinatario.getNombre(),mensaje,"emoji");
            }else{ //Tipo file
                System.out.println(mensaje);
            }

        }
    }

    private void selectAndSendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                cliente.sendFile(selectedFile);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al enviar el archivo");
            }
        }
    }

    // Método para agregar un item a la lista
    public void addItemToList(String item) {
        archivos.addItem(item);
    }

}
