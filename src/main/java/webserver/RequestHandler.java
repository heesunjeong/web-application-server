package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            byte[] body = "Hello World".getBytes();

            while (true) {
                String line = reader.readLine();
                if (line == null || "".equals(line)) {
                    break;
                }

                String[] tokens = line.split(" ");
                String path = HttpRequestUtils.getPath(tokens);

                if (path != null) {
                    if (path.startsWith("/user/create")) {
                        int index = path.indexOf("?");
                        String requestPath = path.substring(0, index);
                        String queryParams = path.substring(index + 1);

                        Map<String, String> params = HttpRequestUtils.parseQueryString(queryParams);

                        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                    }

                    body = Files.readAllBytes(new File("./webapp" + path).toPath());
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
