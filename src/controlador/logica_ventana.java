package controlador;

import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import vista.ventana;
import modelo.*;

public class logica_ventana implements ActionListener, ListSelectionListener, ItemListener {
	private ventana vista;
	private String nombres, email, telefono, categoria = "";
	private List<persona> contactos;
	private boolean favorito = false;

	public logica_ventana(ventana vista) {
		this.vista = vista;
		cargarContactos();

		// Asignar listeners
		vista.btn_add.addActionListener(this);
		vista.btn_eliminar.addActionListener(this);
		vista.btn_modificar.addActionListener(this);
		vista.cmb_categoria.addItemListener(this);
		vista.chb_favorito.addItemListener(this);
		vista.btn_exportar.addActionListener(e -> exportarCSV());

		// Filtro en tiempo real
		vista.txt_buscar.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { filtrarContactos(vista.txt_buscar.getText()); }
			public void removeUpdate(DocumentEvent e) { filtrarContactos(vista.txt_buscar.getText()); }
			public void changedUpdate(DocumentEvent e) { filtrarContactos(vista.txt_buscar.getText()); }
		});

		// Ctrl + S para agregar contacto
		vista.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke("control S"), "agregar");
		vista.contentPane.getActionMap().put("agregar", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				vista.btn_add.doClick();
			}
		});

		// Menú contextual para eliminar
		agregarMenuContextual();
	}

	private void cargarContactos() {
		contactos = new personaDAO(new persona()).leerArchivo();
		actualizarTabla(contactos);
		actualizarEstadisticas();
	}

	private void actualizarTabla(List<persona> lista) {
		DefaultTableModel modelo = vista.modeloTabla;
		modelo.setRowCount(0);
		for (persona p : lista) {
			modelo.addRow(new Object[]{
					p.getNombre(), p.getTelefono(), p.getEmail(), p.getCategoria(),
					p.isFavorito() ? "Sí" : "No"
			});
		}
	}

	private void actualizarEstadisticas() {
		vista.lbl_totalContactos.setText("Total de contactos: " + contactos.size());
		vista.lbl_favoritos.setText("Favoritos: " + contactos.stream().filter(persona::isFavorito).count());
		vista.lbl_familia.setText("Familia: " + contactos.stream().filter(p -> p.getCategoria().equals("Familia")).count());
		vista.lbl_amigos.setText("Amigos: " + contactos.stream().filter(p -> p.getCategoria().equals("Amigos")).count());
		vista.lbl_trabajo.setText("Trabajo: " + contactos.stream().filter(p -> p.getCategoria().equals("Trabajo")).count());
	}

	private void limpiarCampos() {
		vista.txt_nombres.setText("");
		vista.txt_telefono.setText("");
		vista.txt_email.setText("");
		vista.cmb_categoria.setSelectedIndex(0);
		vista.chb_favorito.setSelected(false);
		categoria = "";
		favorito = false;
		cargarContactos();
	}

	private void inicializarCampos() {
		nombres = vista.txt_nombres.getText();
		telefono = vista.txt_telefono.getText();
		email = vista.txt_email.getText();
	}

	private void agregarMenuContextual() {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem itemEliminar = new JMenuItem("Eliminar");
		menu.add(itemEliminar);
		itemEliminar.addActionListener(e -> eliminarSeleccionado());

		vista.tbl_contactos.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) { mostrarMenuSiNecesario(e); }
			@Override
			public void mouseReleased(MouseEvent e) { mostrarMenuSiNecesario(e); }

			private void mostrarMenuSiNecesario(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int fila = vista.tbl_contactos.rowAtPoint(e.getPoint());
					if (fila != -1) {
						vista.tbl_contactos.setRowSelectionInterval(fila, fila);
						menu.show(vista.tbl_contactos, e.getX(), e.getY());
					}
				}
			}
		});
	}

	private void filtrarContactos(String texto) {
		List<persona> filtrados = contactos.stream()
			.filter(p -> p.getNombre().toLowerCase().contains(texto.toLowerCase()))
			.collect(Collectors.toList());
		actualizarTabla(filtrados);
	}

	private void exportarCSV() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Guardar contactos como CSV");
		chooser.setSelectedFile(new java.io.File(System.getProperty("user.home") + "/Downloads/contactos.csv"));

		if (chooser.showSaveDialog(vista) == JFileChooser.APPROVE_OPTION) {
			try (java.io.FileWriter writer = new java.io.FileWriter(chooser.getSelectedFile())) {
				writer.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");
				for (persona p : contactos) {
					writer.write(String.join(";", p.getNombre(), p.getTelefono(), p.getEmail(), p.getCategoria(), String.valueOf(p.isFavorito())) + "\n");
				}
				JOptionPane.showMessageDialog(vista, "Contactos exportados correctamente.");
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(vista, "Error al exportar el archivo.");
			}
		}
	}

	private void eliminarSeleccionado() {
		int fila = vista.tbl_contactos.getSelectedRow();
		if (fila != -1) {
			contactos.remove(fila);
			new personaDAO().actualizarContactos(contactos);
			limpiarCampos();
			JOptionPane.showMessageDialog(vista, "Contacto eliminado correctamente.");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		inicializarCampos();

		if (e.getSource() == vista.btn_add) {
			if (validarCampos()) {
				if (validarCategoria()) {
					registrarNuevoContacto();
				} else {
					JOptionPane.showMessageDialog(vista, "Elija una categoría válida.");
				}
			} else {
				JOptionPane.showMessageDialog(vista, "Todos los campos deben estar llenos.");
			}
		}

		if (e.getSource() == vista.btn_modificar) {
			modificarContacto();
		}

		if (e.getSource() == vista.btn_eliminar) {
			eliminarSeleccionado();
		}
	}

	private boolean validarCampos() {
		return !nombres.isEmpty() && !telefono.isEmpty() && !email.isEmpty();
	}

	private boolean validarCategoria() {
		return !categoria.equals("Elija una Categoria") && !categoria.isEmpty();
	}

	private void registrarNuevoContacto() {
		vista.barraProgreso.setValue(0);
		Timer timer = new Timer(10, null);
		timer.addActionListener(new ActionListener() {
			int progreso = 0;
			public void actionPerformed(ActionEvent e) {
				progreso++;
				vista.barraProgreso.setValue(progreso);
				if (progreso >= 100) {
					timer.stop();
					persona nuevo = new persona(nombres, telefono, email, categoria, favorito);
					new personaDAO(nuevo).escribirArchivo();
					limpiarCampos();
					JOptionPane.showMessageDialog(vista, "Contacto registrado.");
					vista.barraProgreso.setValue(0);
				}
			}
		});
		timer.start();
	}

	private void modificarContacto() {
		int fila = vista.tbl_contactos.getSelectedRow();
		if (fila != -1 && validarCampos() && validarCategoria()) {
			persona actual = contactos.get(fila);
			boolean hayCambios = !actual.getNombre().equals(nombres) ||
								 !actual.getTelefono().equals(telefono) ||
								 !actual.getEmail().equals(email) ||
								 !actual.getCategoria().equals(categoria) ||
								 actual.isFavorito() != favorito;

			if (!hayCambios) {
				JOptionPane.showMessageDialog(vista, "Debes hacer algún cambio.");
				return;
			}

			persona actualizado = new persona(nombres, telefono, email, categoria, favorito);
			contactos.set(fila, actualizado);
			new personaDAO().actualizarContactos(contactos);
			limpiarCampos();
			JOptionPane.showMessageDialog(vista, "Contacto modificado.");
		} else {
			JOptionPane.showMessageDialog(vista, "Selecciona un contacto y completa todos los campos.");
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == vista.cmb_categoria) {
			categoria = vista.cmb_categoria.getSelectedItem().toString();
		} else if (e.getSource() == vista.chb_favorito) {
			favorito = vista.chb_favorito.isSelected();
		}
	}

	@Override public void valueChanged(ListSelectionEvent e) { /* No usado */ }
}


