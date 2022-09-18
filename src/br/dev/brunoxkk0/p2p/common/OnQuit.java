package br.dev.brunoxkk0.p2p.common;

import br.dev.brunoxkk0.p2p.server.Connection;

@FunctionalInterface
public interface OnQuit {

    /**
     * Função a ser executada quando um novo cliente se desconecta.
     * @param connection cliente desconectado.
     */
    void onQuit(Connection connection);

}
