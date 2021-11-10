package br.com.brunoxkk0.common;

import br.com.brunoxkk0.server.Connection;

@FunctionalInterface
public interface OnJoin {

    /**
     * Função a ser executada quando um novo cliente se conecta.
     * @param connection cliente conectado.
     */
    void onJoin(Connection connection);

}
