import java.io.InputStream;
import java.util.Properties;

/**
 * @Date 2024/2/22 18:39
 * @Created by weixiao
 */
public class RabbitMqConfig {
    private String rmqHostAddress;
    private String rmqUsername;
    private String rmqPassword;
    private String rmqMainQueueName;

    public RabbitMqConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                return;
            }
            Properties prop = new Properties();
            prop.load(input);
            parseProperties(prop);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseProperties(Properties prop) {
        rmqHostAddress = prop.getProperty("rmq.host.address");
        rmqUsername = prop.getProperty("rmq.username");
        rmqPassword = prop.getProperty("rmq.password");
        rmqMainQueueName = prop.getProperty("rmq.main.queue.name");
    }


    public String getRmqHostAddress() {
        return rmqHostAddress;
    }

    public String getRmqUsername() {
        return rmqUsername;
    }

    public String getRmqPassword() {
        return rmqPassword;
    }

    public String getRmqMainQueueName() {
        return rmqMainQueueName;
    }
}
