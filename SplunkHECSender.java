import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplunkHECSender {

    private static final String HEC_URL = "http://<host>:8088/services/collector";
    private static final String HEC_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final int NUM_EVENTS = 1000; // Define the number of events you want to send
    private static final int NUM_THREADS = 5; // Define the number of threads to send events in parallel

    public static void main(String[] args) {
        try {
            sendSampleEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendSampleEvents() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_EVENTS; i++) {
            int eventId = i + 1;
            String event = "{\"event\": \"Sample event " + eventId + "\", \"source\": \"java\",\"sourcetype\": \"custom\", \"host\": \"localhost\"}";
            executorService.submit(() -> {
                try {
                    sendEventToHEC(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
    }

    private static void sendEventToHEC(String event) throws Exception {
        URL url = new URL(HEC_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Splunk " + HEC_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(event.getBytes());
            outputStream.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Sending event: " + event);
        System.out.println("Response code: " + responseCode);
        connection.disconnect();
    }
}
