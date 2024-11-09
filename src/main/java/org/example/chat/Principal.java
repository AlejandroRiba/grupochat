package org.example.chat;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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

    String mensaje_inicio = "", mensaje_medio = "", mensaje_final = "";
    String mensaje_inicio1 = "";

    String path,tmp_u="",tmp_m="";
    public Principal(){
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
                "font-family: Arial, Helvetica, sans-serif;"+
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
                "font-family: Arial, Helvetica, sans-serif;"+
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
        //Metodo para enviar un mensaje de texto
        sendButton.addActionListener(e -> enviarMensaje());
    }

    public void enviarMensaje(){
        String mensaje = fieldMessage.getText();
        if(!mensaje.isEmpty()){
            System.out.println("Mensaje enviado : " + mensaje);
            fieldMessage.setText("");
            mensaje_medio= mensaje_medio+"  <tr>\n" +
                    "    <td>"+"Usuario"+" dice: </td>\n" +
                    "    <td>"+mensaje+"</td>\n" +
                    "  </tr>";
            editorChat.setText(mensaje_inicio+mensaje_medio+mensaje_final);

            this.repaint();
        }else{
            System.out.println("Campo del mensaje vacío, no debe hacer nada.");
        }
    }

    public void actualizarUsuarios(String usuario){
        System.out.println("La lista de usuarios se actualiza");
        mensaje_medio= "  <tr>\n" +
                "    <td>"+usuario+"</td>\n" +
                "  </tr>";
        editorUsers.setText(mensaje_inicio+mensaje_medio+mensaje_final);

        this.repaint();
    }

}
