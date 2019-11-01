import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HelloWorld extends NanoHTTPD {
    private HelloWorld() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning server on http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        try {
            new HelloWorld();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, List<String>> params = session.getParameters();
        if (params.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + params.get("username") + "!</p>";
        }
        return newFixedLengthResponse(msg + "</body></html>\n");
    }
}
