package server;

/**
 * Интерфейс объекта, способный принимать сообщения из сети
 */
public interface NetworkMessageReceiver {
    /**
     * Вызывается каждый раз, когда приходит сообщение
     *
     * @param ip IP адрес отправителя
     * @param port Порт отправителя
     * @param message Сообщение от отправителя
     */
    void onReceive(String ip, int port, String message);
}
