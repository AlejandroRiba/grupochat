package org.example.clases;

public class Usuario {
    private String nombre;
    private String direccionUnicast;

    public Usuario(String nombre, String direccionUnicast) {
        this.nombre = nombre;
        this.direccionUnicast = direccionUnicast;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public String getDireccionUnicast() { return direccionUnicast; }

    // Sobreescribir equals para comparar usuarios por nombre
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return nombre.equals(usuario.nombre); // Comparación por nombre
    }

    // Sobreescribir hashCode para que coincida con equals
    @Override
    public int hashCode() {
        return nombre.hashCode();
    }
}

