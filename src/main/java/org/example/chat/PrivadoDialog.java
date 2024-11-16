package org.example.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class PrivadoDialog extends JDialog {
    private JTextField mensajeField;
    private JButton continuarButton;
    private JButton emojisButton;
    private String mensajePrivado;
    private String tipo;

    public PrivadoDialog(JFrame parent) {
        super(parent, "Private Message", true);
        mensajeField = new JTextField(20);
        continuarButton = new JButton("Send");
        emojisButton = new JButton("Emojis");

        continuarButton.addActionListener(e -> {
            mensajePrivado = mensajeField.getText().trim();
            if (!mensajePrivado.isEmpty()) {
                tipo = "message";
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a message.");
            }
        });

        emojisButton.addActionListener(e -> {
            tipo = "emoji";
            listarEmojis();
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Message:"));
        panel.add(mensajeField);
        panel.add(continuarButton);
        panel.add(emojisButton);
        add(panel);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public String getMessage() {
        return mensajePrivado;
    }

    public void setMessage(String mensajePrivado){ this.mensajePrivado = mensajePrivado; }
    public String getTipo(){ return  tipo;}

    private void listarEmojis() {
        // Crear el JDialog para mostrar el panel de emojis
        final JDialog emojiDialog = new JDialog((Frame) null, "Emojis", true);

        JPanel emojiPanel = new JPanel();
        emojiPanel.setLayout(new GridLayout(0, 3, 5, 5)); // Para organizar en filas y columnas

        // Ruta de los recursos de los emojis
        String rutaEmojis = "/emojis/";

        // Crear un array de nombres de archivos de emojis (esto permite añadir más fácilmente)
        String[] emojis = {"emoji1.gif", "emoji2.gif", "emoji3.gif", "emoji4.gif", "emoji5.gif", "emoji6.gif", "emoji7.gif", "emoji8.gif", "emoji9.gif", "emoji10.gif", "emoji11.gif", "emoji12.gif"};

        int ancho = 50; // Ancho deseado
        int alto = 53;  // Alto deseado

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
                        setMessage(emoji);
                        emojiDialog.dispose(); // Cierra la ventana de emojis
                        dispose(); //Se supone que cierre la ventana completa
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

        // Agregar un WindowFocusListener para cerrar el diálogo cuando pierda el foco
        emojiDialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                emojiDialog.dispose();
            }
        });

        emojiDialog.setVisible(true);
    }

}
