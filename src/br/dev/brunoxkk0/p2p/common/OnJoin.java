package br.dev.brunoxkk0.p2p.common;

import br.dev.brunoxkk0.p2p.server.Connection;

@FunctionalInterface
public interface OnJoin {

    /**
     * Função a ser executada quando um novo cliente se conecta.
     * @param connection cliente conectado.
     */
    void onJoin(Connection connection);

}
