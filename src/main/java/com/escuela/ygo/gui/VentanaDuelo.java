package com.escuela.ygo.gui;

import com.escuela.ygo.api.ClienteApiYgo;
import com.escuela.ygo.batalla.Batalla;
import com.escuela.ygo.batalla.Duelo;
import com.escuela.ygo.modelo.Carta;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VentanaDuelo implements Batalla {

    private final JFrame ventana = new JFrame("Duelo YGO - Laboratorio");
    private final JPanel panelCartasJugador = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 30));
    private final JTextArea areaLog = new JTextArea(12, 50);
    private final JButton botonCargar = new JButton("Cargar cartas");
    private final JButton botonIniciar = new JButton("Iniciar duelo");
    private final ClienteApiYgo api = new ClienteApiYgo();

    private final List<Carta> manoJugador = new ArrayList<>();
    private final List<Carta> manoIA = new ArrayList<>();

    private Duelo duelo;

    public VentanaDuelo() {
        configurarInterfaz();
    }

    private void configurarInterfaz() {
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(1050, 900);
        ventana.setLayout(new BorderLayout());
        ventana.setLocationRelativeTo(null);
        ventana.getContentPane().setBackground(new Color(30, 30, 30));

        // Panel superior
        JPanel superior = new JPanel(new BorderLayout());
        superior.setBackground(new Color(40, 40, 40));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botones.setBackground(new Color(40, 40, 40));
        estilizarBoton(botonCargar, new Color(75, 110, 175));
        estilizarBoton(botonIniciar, new Color(220, 100, 60));
        botonIniciar.setEnabled(false);

        botones.add(botonCargar);
        botones.add(botonIniciar);
        superior.add(botones, BorderLayout.NORTH);

        panelCartasJugador.setBackground(new Color(30, 30, 30));
        superior.add(panelCartasJugador, BorderLayout.CENTER);

        ventana.add(superior, BorderLayout.CENTER);

        // Área de registro (log)
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaLog.setBackground(new Color(15, 15, 15));
        areaLog.setForeground(new Color(0, 255, 120));
        areaLog.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setPreferredSize(new Dimension(1050, 220));
        ventana.add(scroll, BorderLayout.SOUTH);

        // Eventos
        botonCargar.addActionListener(e -> cargarCartasAsincronas());
        botonIniciar.addActionListener(e -> iniciarDuelo());

        ventana.setVisible(true);
    }

    // Diseño de botones personalizados
    private void estilizarBoton(JButton boton, Color colorFondo) {
        boton.setFocusPainted(false);
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        boton.setPreferredSize(new Dimension(180, 40));
        boton.setBorder(new RoundBorder(colorFondo.darker()));
    }

    // Cargar cartas (asincrónico)
    private void cargarCartasAsincronas() {
        botonCargar.setEnabled(false);
        registrar("Cargando cartas...");

        CompletableFuture.runAsync(() -> {
            try {
                cargarMano(manoJugador, "Jugador");
                cargarMano(manoIA, "IA");
                SwingUtilities.invokeLater(() -> {
                    mostrarManoJugador();
                    botonIniciar.setEnabled(true);
                    registrar("Cartas cargadas correctamente.\n");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    registrar("Error al cargar cartas: " + e.getMessage());
                    botonCargar.setEnabled(true);
                });
            }
        });
    }

    private void cargarMano(List<Carta> mano, String quien) throws InterruptedException {
        mano.clear();
        int intentos = 0;
        while (mano.size() < 3 && intentos < 10) {
            intentos++;
            try {
                Carta carta = api.obtenerCartaAleatoria();
                mano.add(carta);
                registrar(quien + " obtuvo: " + carta);
            } catch (IOException | InterruptedException ex) {
                registrar("Reintentando para " + quien + " (" + ex.getMessage() + ")");
            }
        }
        if (mano.size() < 3) {
            throw new RuntimeException("No se pudieron obtener 3 cartas para " + quien);
        }
    }

    // Mostrar cartas del jugador
    private void mostrarManoJugador() {
        panelCartasJugador.removeAll();
        for (int i = 0; i < manoJugador.size(); i++) {
            panelCartasJugador.add(crearPanelCarta(manoJugador.get(i), i));
        }
        panelCartasJugador.revalidate();
        panelCartasJugador.repaint();
    }

    // Crear panel visual de cada carta
    private JPanel crearPanelCarta(Carta carta, int indice) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(45, 45, 45));
        panel.setPreferredSize(new Dimension(230, 360));
        panel.setMaximumSize(new Dimension(230, 360));
        panel.setBorder(new CompoundBorder(
                new MatteBorder(3, 3, 3, 3, new Color(100, 100, 100)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Imagen más grande
        JLabel lblImagen = new JLabel();
        lblImagen.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImagen.setPreferredSize(new Dimension(180, 250));

        CompletableFuture.runAsync(() -> {
            try {
                if (!carta.getUrlImagen().isEmpty()) {
                    Image imagen = ImageIO.read(new URL(carta.getUrlImagen()));
                    Image esc = imagen.getScaledInstance(180, 250, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> lblImagen.setIcon(new ImageIcon(esc)));
                }
            } catch (IOException ignored) {}
        });

        // Nombre centrado
        JLabel lblNombre = new JLabel("<html><div style='text-align: center; width: 180px;'>"
                + carta.getNombre() + "</div></html>");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stats centrados
        JLabel lblStats = new JLabel("<html><div style='text-align: center; width: 180px;'>"
                + "ATK: " + carta.getAtaque() + " / DEF: " + carta.getDefensa()
                + "</div></html>");
        lblStats.setForeground(new Color(200, 200, 200));
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStats.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón de acción amplio
        JButton botonUsar = new JButton("Usar carta " + (indice + 1));
        botonUsar.setAlignmentX(Component.CENTER_ALIGNMENT);
        estilizarBoton(botonUsar, new Color(70, 160, 90));
        botonUsar.setPreferredSize(new Dimension(180, 40));
        botonUsar.setMaximumSize(new Dimension(180, 40));

        botonUsar.addActionListener(e -> {
            boolean jugadorAtaca = preguntarModo();
            boolean iaAtaca = new Random().nextBoolean();
            if (duelo != null) {
                duelo.jugarRonda(indice, jugadorAtaca, iaAtaca);
            }
        });

        panel.add(lblImagen);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblNombre);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblStats);
        panel.add(Box.createVerticalStrut(10));
        panel.add(botonUsar);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private boolean preguntarModo() {
        int res = JOptionPane.showOptionDialog(ventana,
                "¿Deseas poner la carta en modo Ataque o Defensa?",
                "Seleccionar modo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Ataque", "Defensa"},
                "Ataque");
        return res == 0;
    }

    private void iniciarDuelo() {
        if (manoJugador.size() < 3 || manoIA.size() < 3) {
            registrar("No se puede iniciar: faltan cartas.");
            return;
        }
        duelo = new Duelo(manoJugador, manoIA, this);
        registrar("Duelo iniciado. Elige una carta para jugar.");
        botonIniciar.setEnabled(false);
    }

    private void registrar(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    // Implementación de la interfaz Batalla
    @Override
    public void alIniciarTurno(String cartaJugador, String cartaIA, String ganador) {
        registrar("Turno: " + cartaJugador + " vs " + cartaIA + " → Ganador: " + ganador);
    }

    @Override
    public void alCambiarPuntaje(int puntajeJugador, int puntajeIA) {
        registrar("Marcador → Jugador: " + puntajeJugador + " | IA: " + puntajeIA);
    }

    @Override
    public void alFinalizarDuelo(String ganador) {
        registrar("Duelo finalizado. Ganador: " + ganador + "\n");
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(ventana, "Ganador: " + ganador);
            botonCargar.setEnabled(true);
        });
    }

    public static void lanzar() {
        SwingUtilities.invokeLater(VentanaDuelo::new);
    }

    // Clase interna para bordes redondeados
    static class RoundBorder extends LineBorder {
        public RoundBorder(Color color) {
            super(color, 2, true);
        }
    }
}
