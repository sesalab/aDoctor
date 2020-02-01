package adoctor.application.analytics;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeasurementManager {
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";
    public static final String DEFAULT_V = "1";
    public static final String DEFAULT_T = "event";
    public static final String DEFAULT_TID = "UA-157298764-1";

    private static final String COLLECT_URL = "https://www.google-analytics.com/collect";
    private static final String BATCH_URL = "https://www.google-analytics.com/batch";
    private static final String BASE_PAYLOAD = "v=%s&t=%s&tid=%s&cid=%s";
    private static final String ANALYSIS_PAYLOAD = "&ec=p_analysis&ea=select&el=%s&ev=%s\n";
    private static final String REFACTORING_PAYLOAD = "&ec=p_refactoring&ea=apply&el=%s&ev=%s\n";
    private static final String TRUE = "1";
    private static final String FALSE = "0";

    private String initialPayload;
    private ResponseHandler<Integer> responseHandler;

    public MeasurementManager(String v, String t, String tid, String cid) {
        this.responseHandler = response -> response.getStatusLine().getStatusCode();
        this.initialPayload = String.format(BASE_PAYLOAD, v, t, tid, cid);
    }

    public MeasurementManager(String cid) {
        this(DEFAULT_V, DEFAULT_T, DEFAULT_TID, cid);
    }

    public String getInitialPayload() {
        return this.initialPayload;
    }

    public boolean sendAnalysisData(List<Boolean> selectedSmells) throws IOException {
        String payload = processAnalysisPayload(selectedSmells);
        if (payload.equals("")) {
            throw new IllegalArgumentException("No selected smells");
        }
        return sendData(BATCH_URL, payload);
    }

    public boolean sendRefactoringData(String selectedSmell) throws IOException {
        String payload = processRefactoringPayload(selectedSmell);
        if (payload.equals("")) {
            throw new IllegalArgumentException("No selected smell");
        }
        return sendData(COLLECT_URL, payload);
    }

    private boolean sendData(String url, String payload) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPostRequest = new HttpPost(url);
            httpPostRequest.setHeader(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT);
            httpPostRequest.setEntity(new StringEntity(payload));
            int status = httpClient.execute(httpPostRequest, responseHandler);
            return status >= 200 && status < 300;
        }
    }

    /**
     * Prepare the payload to collect data about the selected smells
     *
     * @param selectedSmells Max 20 elements
     * @return Payload for the request (max 16kB)
     */
    private String processAnalysisPayload(List<Boolean> selectedSmells) {
        if (selectedSmells.size() > 20) {
            return "";
        }
        ArrayList<String> rows = new ArrayList<>();
        // TODO: Find a more scalable solution
        rows.add(String.format(ANALYSIS_PAYLOAD, "dw", selectedSmells.get(0) ? TRUE : FALSE));
        rows.add(String.format(ANALYSIS_PAYLOAD, "erb", selectedSmells.get(1) ? TRUE : FALSE));
        rows.add(String.format(ANALYSIS_PAYLOAD, "ids", selectedSmells.get(2) ? TRUE : FALSE));
        rows.add(String.format(ANALYSIS_PAYLOAD, "is", selectedSmells.get(3) ? TRUE : FALSE));
        rows.add(String.format(ANALYSIS_PAYLOAD, "lt", selectedSmells.get(4) ? TRUE : FALSE));
        rows.add(String.format(ANALYSIS_PAYLOAD, "mim", selectedSmells.get(5) ? TRUE : FALSE));
        StringBuilder builder = new StringBuilder();
        rows.stream().map(row -> this.initialPayload + row).forEach(builder::append);
        return builder.toString();
    }

    /**
     * Prepare the payload to collect data about the refactored smell
     *
     * @param selectedSmell Non empty short name of the smell
     * @return Payload for the request (max 16kB)
     */
    private String processRefactoringPayload(String selectedSmell) {
        if (selectedSmell.equals("")) {
            return "";
        }
        return this.initialPayload + String.format(REFACTORING_PAYLOAD, selectedSmell, TRUE);
    }
}
