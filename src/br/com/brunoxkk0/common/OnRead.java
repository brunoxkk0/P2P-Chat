package br.com.brunoxkk0.common;

import br.com.brunoxkk0.server.Connection;

@FunctionalInterface
public interface OnRead {
    void onRead(Connection connection, String message);
}
