package main;

import javax.swing.SwingUtilities;
import vista.ventana;

public class Main {
    public static void main(String[] args) {
        // Utilizamos SwingUtilities.invokeLater para asegurarnos que la interfaz gráfica se ejecute en el hilo adecuado.
        SwingUtilities.invokeLater(() -> {
            try {
                // Crear una instancia de la clase ventana desde el paquete 'vista'
                ventana v = new ventana();
                
                // Hacer visible la ventana
                v.setVisible(true);  
                
                // Centrar la ventana en la pantalla
                v.setLocationRelativeTo(null);  
                
                // Ajuste adicional (si es necesario): establecer el tamaño de la ventana si no está configurado en la clase ventana
                // v.setSize(800, 600);  // o el tamaño que desees
            } catch (Exception e) {
                // Manejo de errores si la ventana no se puede inicializar
                e.printStackTrace();  
            }
        });
    }
}
