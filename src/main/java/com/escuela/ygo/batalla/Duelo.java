package com.escuela.ygo.batalla;

import com.escuela.ygo.modelo.Carta;
import java.util.List;
import java.util.Random;

public class Duelo {
    private final List<Carta> cartasJugador;
    private final List<Carta> cartasIA;
    private final Batalla oyente;
    private int puntajeJugador = 0;
    private int puntajeIA = 0;
    private final Random random = new Random();

    public Duelo(List<Carta> cartasJugador, List<Carta> cartasIA, Batalla oyente) {
        this.cartasJugador = cartasJugador;
        this.cartasIA = cartasIA;
        this.oyente = oyente;
    }

    public void jugarRonda(int indiceJugador, boolean jugadorAtaca, boolean iaAtaca) {
        Carta cartaJugador = cartasJugador.get(indiceJugador);
        Carta cartaIA = cartasIA.get(random.nextInt(cartasIA.size()));

        String ganadorTurno;
        int valorJugador = jugadorAtaca ? cartaJugador.getAtaque() : cartaJugador.getDefensa();
        int valorIA = iaAtaca ? cartaIA.getAtaque() : cartaIA.getDefensa();

        if (valorJugador > valorIA) {
            puntajeJugador++;
            ganadorTurno = "Jugador";
        } else if (valorJugador < valorIA) {
            puntajeIA++;
            ganadorTurno = "IA";
        } else {
            ganadorTurno = "Empate";
        }

        oyente.alIniciarTurno(cartaJugador.getNombre(), cartaIA.getNombre(), ganadorTurno);
        oyente.alCambiarPuntaje(puntajeJugador, puntajeIA);

        if (puntajeJugador >= 2 || puntajeIA >= 2) {
            String ganadorFinal = puntajeJugador > puntajeIA ? "Jugador" : "IA";
            oyente.alFinalizarDuelo(ganadorFinal);
        }
    }
}
