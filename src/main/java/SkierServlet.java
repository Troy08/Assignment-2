import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "SkierServlet", urlPatterns = "/skiers/*")
public class SkierServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final static String QUEUE_NAME = "hello";

    @Override
    public void init() throws ServletException {
        RabbitMQUtil.initRMQ();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set the MIME type for the response
        response.setContentType("text/html");

        // Introduce a delay of 1000 milliseconds for testing purposes
        try {
            Thread.sleep(1000); // 1000ms pause
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Write the response content
        PrintWriter out = response.getWriter();
        out.println("<h1>hello</h1>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        // Extracting path parameters
        // /{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo.split("/");
            // Basic validation of path parameters
            if (pathParts.length != 8 || !pathParts[2].equals("seasons") ||
                    !pathParts[4].equals("days") || !pathParts[6].equals("skiers")) {
                throw new RequestException();
            }
            // check path
            checkInt(pathParts[1]);
            checkInt(pathParts[5]);
            checkInt(pathParts[7]);
            checkRange(pathParts[5], 1, 366);

            // check request body
            StringBuilder requestBody = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            LiftRide liftRide = gson.fromJson(requestBody.toString(), LiftRide.class);
            if (liftRide.getTime() == null || liftRide.getLiftID() == null) {
                throw new RequestException();
            }
            // send to queue
            MessageData messageData = new MessageData(
                    pathParts[1], pathParts[2], pathParts[5],
                    pathParts[7], liftRide.getTime().toString(), liftRide.getLiftID().toString()
            );
            RabbitMQUtil.sendMessage(gson.toJson(messageData));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"message\":\"Invalid request format.\"}");
            return;
        }

        // If everything is valid, send a dummy response
        response.setStatus(HttpServletResponse.SC_CREATED);
        out.println("{\"message\":\"data create successfully.\"}");
    }

    private void checkInt(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new RequestException();
        }
    }

    private void checkRange(String input, int min, int max) {
        int dayID = Integer.parseInt(input);
        if (dayID < min || dayID > max) {
            throw new RequestException();
        }
    }

    @Override
    public void destroy() {
        // Clean-up code (if needed)
        RabbitMQUtil.destroyRMQ();
    }
}
