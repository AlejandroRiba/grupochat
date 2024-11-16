package org.example.chat;

import javax.swing.*;

public class NombreUsuarioDialog extends JDialog {
    private JTextField nombreField;
    private JButton continuarButton;
    private String nombreUsuario;

    public NombreUsuarioDialog(JFrame parent) {
        super(parent, "Enter your name", true);
        nombreField = new JTextField(20);
        continuarButton = new JButton("Next");

        continuarButton.addActionListener(e -> {
            nombreUsuario = nombreField.getText().trim();
            if (!nombreUsuario.isEmpty()) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a name.");
            }
        });


        JPanel panel = new JPanel();
        panel.add(new JLabel("Username:"));
        panel.add(nombreField);
        panel.add(continuarButton);
        add(panel);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}

