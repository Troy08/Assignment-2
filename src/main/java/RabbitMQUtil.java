import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;


import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * @Date 2024/2/22 18:38
 * @Created by weixiao
 */
public class RabbitMQUtil {
    private static final RabbitMqConfig CONFIG = new RabbitMqConfig();
    private static Connection connection;
    private static ChannelPool channelPool;
    private static final String QUEUE_NAME = CONFIG.getRmqMainQueueName();

    public static void initRMQ() throws ServletException {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(CONFIG.getRmqHostAddress());
            factory.setUsername(CONFIG.getRmqUsername());
            factory.setPassword(CONFIG.getRmqPassword());
            connection = factory.newConnection();
            channelPool = new ChannelPool(connection); // Initialize the channel pool
        } catch (IOException | TimeoutException e) {
            throw new ServletException("Failed to establish RabbitMQ connection", e);
        }
    }

    public static void destroyRMQ() {
        try {
            channelPool.close(); // Close all channels in the pool

            if (connection != null) {
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            System.out.println("Failed to close RabbitMQ connection");
        }
    }

    public static void sendMessage(String message) throws Exception {
        Channel channel = null;
        try {
            channel = channelPool.getChannel(); // Borrow a channel from the pool

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServletException("Failed to borrow a channel from the pool", e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel); // Return the channel to the pool
            }
        }
    }
}
