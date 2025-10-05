package com.escuela.ygo.batalla;

public interface Batalla {
    void alIniciarTurno(String cartaJugador, String cartaIA, String ganador);
    void alCambiarPuntaje(int puntajeJugador, int puntajeIA);
    void alFinalizarDuelo(String ganador);
}
