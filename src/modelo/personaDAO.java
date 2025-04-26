package modelo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class personaDAO {

    private static final String DIRECTORIO = "c:/gestionContactos";
    private static final String ARCHIVO_NOMBRE = "datosContactos.csv";
    private static final String ENCABEZADO = "NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO";

    private File archivo;
    private persona persona;

    public personaDAO() {
        prepararArchivo();
    }

    public personaDAO(persona persona) {
        this.persona = persona;
        prepararArchivo();
    }

    // Crear directorio y archivo si no existen
    private void prepararArchivo() {
        File directorio = new File(DIRECTORIO);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        archivo = new File(directorio, ARCHIVO_NOMBRE);

        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
                escribirLinea(ENCABEZADO, false);
            } catch (IOException e) {
                System.err.println("Error al crear archivo: " + e.getMessage());
            }
        }
    }

    // Escribir línea al archivo
    private void escribirLinea(String texto, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, append))) {
            bw.write(texto);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al escribir línea: " + e.getMessage());
        }
    }

    // Escribe la persona actual en el archivo
    public boolean escribirArchivo() {
        if (persona != null) {
            escribirLinea(persona.datosContacto(), true);
            return true;
        }
        return false;
    }

    // Leer el archivo y obtener la lista de personas
    public List<persona> leerArchivo() {
        List<persona> personas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith(ENCABEZADO)) continue;
                String[] datos = linea.split(";");
                if (datos.length == 5) {
                    persona p = new persona(
                        datos[0],
                        datos[1],
                        datos[2],
                        datos[3],
                        Boolean.parseBoolean(datos[4])
                    );
                    personas.add(p);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }

        return personas;
    }

    // Actualiza todo el archivo con una nueva lista de personas
    public void actualizarContactos(List<persona> personas) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write(ENCABEZADO);
            bw.newLine();
            for (persona p : personas) {
                bw.write(p.datosContacto());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al actualizar contactos: " + e.getMessage());
        }
    }

    // Eliminar un contacto específico por nombre
    public boolean eliminarContacto(String nombre) {
        List<persona> personas = leerArchivo();
        boolean eliminado = false;
        for (int i = 0; i < personas.size(); i++) {
            if (personas.get(i).getNombre().equals(nombre)) {
                personas.remove(i);
                eliminado = true;
                break;
            }
        }
        if (eliminado) {
            actualizarContactos(personas);
        }
        return eliminado;
    }

    // Modificar un contacto específico
    public boolean modificarContacto(persona pModificada) {
        List<persona> personas = leerArchivo();
        boolean modificado = false;
        for (int i = 0; i < personas.size(); i++) {
            if (personas.get(i).getNombre().equals(pModificada.getNombre())) {
                personas.set(i, pModificada);
                modificado = true;
                break;
            }
        }
        if (modificado) {
            actualizarContactos(personas);
        }
        return modificado;
    }

    // Validación de los datos antes de agregar o modificar
    public boolean validarDatos(persona p) {
        // Validar nombre
        if (p.getNombre().isEmpty()) {
            return false;
        }
        // Validar teléfono (solo números y longitud de 10)
        if (!p.getTelefono().matches("\\d{10}")) {
            return false;
        }
        // Validar email
        if (!p.getEmail().contains("@")) {
            return false;
        }
        return true;
    }

    // Excepción personalizada para el manejo de errores en archivos
    public class ArchivoException extends Exception {
        public ArchivoException(String mensaje) {
            super(mensaje);
        }
    }
}


