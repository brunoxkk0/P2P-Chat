package br.com.brunoxkk0.common;

/**
 * Define os estados para realizar a troca das chaves.
 */
public enum KeyShareStatus {

    SEND_SERVER_AES     (0),
    RECEIVE_SERVER_AES  (1),
    FINISH              (2);

    int code;

    KeyShareStatus(int code){
        this.code = code;
    }

}
