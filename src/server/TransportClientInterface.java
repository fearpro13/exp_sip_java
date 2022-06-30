package server;

/**
 * Интерфейс объекта, который позволяет отправлять сообщения по сети
 */
public interface TransportClientInterface extends NetworkMessageReceiver {
    /**
     * Отправка сообщения получателю
     *
     * @param ip IP адрес получателя
     * @param port Порт получателя
     * @param message Сообщение для получателя
     */
    void send(String ip, int port, String message);

    /**
     * В случае если для работы клиента требуется его запуск или инициализация
     */
    void start();
}
