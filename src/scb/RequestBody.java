package scb;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that connects to the Statistics Sweden Agency (SCB) and
 * which holds the response body by a list of column names, column types,
 * and the associated values.
 */
class RequestBody {

    private String datasetName;
    private String title;
    private String latestTimeObservation;
    private ArrayList<String> columnNames;
    private ArrayList<String> columnTypes;
    private HashMap<String, String> columnNamesToTypes;
    private HashMap<String, Integer> columnNamesToIndex;
    private JsonArray scbResponseJArray;

    /**
     * Class holding the JsonObject with the response body data and
     * a string describing the data category/type.
     * @param scbQueryObject Object holding the table url and JsonObject query specifying
     *                       what data to query SCB by a POST request.
     */
    public <T extends ScbQueryObjects> RequestBody(T scbQueryObject) {
        JsonObject jsonResponse = this.responseBody(scbQueryObject);
        JsonArray jsonColumns = jsonResponse.getJsonArray("columns");

        this.datasetName = scbQueryObject.getDatasetName();
        this.title = getDatasetTitle(scbQueryObject);

        this.latestTimeObservation = getLatestTimeObservation(scbQueryObject);
        this.scbResponseJArray = jsonResponse.getJsonArray("data");

        this.columnNamesToTypes = columnNamesToTypes(jsonColumns);
        this.columnNamesToIndex = columnNamesToIndex(jsonColumns);
        this.columnNames = columnNames(jsonColumns);
        this.columnTypes = columnTypes(jsonColumns);
    }

    /**
     * @return The data set name for this request.
     */
    public String getDatasetName() { return this.datasetName; }

    /**
     * @return A title describing what type of data set that is queried.
     */
    public String getTitle() { return this.title; }

    /**
     * Returns the e:th (row) value for the column columnName in the SCB data returned by the request
     * body.
     * @param row The n:th value.
     * @param columnName The value for columnName.
     * @return A JsonValue for the columnName, the type for the returned value is specified by
     * columnNamesToTypes HashMap.
     */
    public JsonValue getValue(int row, String columnName) {
        JsonObject jsonData = this.scbResponseJArray.getJsonObject(row);
        String type = this.columnNamesToTypes.get(columnName);
        int indexToColumnValue = this.columnNamesToIndex.get(columnName);

        JsonValue returnValue;
        if (type.equals("String")) {
            returnValue = jsonData.getJsonArray("key").getJsonString(indexToColumnValue);
        } else {
            returnValue = jsonData.getJsonArray("values").getJsonString(indexToColumnValue);
        }
        return returnValue;
    }

    /**
     * Get the number of data values for the SCB request.
     * @return Integer specifying the number of values for the SCB request.
     */
    public int size() { return this.scbResponseJArray.size(); }

    /**
     * Get the number of columns.
     * @return The number columns.
     */
    public int nCols() { return this.columnNames.size(); }

    /**
     * Returns the column names obtained from the SCB request body data.
     * @return An ArrayList with the column names for the SCB request data.
     */
    public ArrayList<String> getColumnNames() { return this.columnNames; }

    /**
     * Returns the column types obtained from the SCB request body data.
     * @return An ArrayList with the associated column types for each column name obtained from the
     * SCB request data.
     */
    public ArrayList<String> getColumnTypes() { return this.columnTypes; }

    /**
     * @return The latest time observation for the queried data.
     */
    public <T extends ScbQueryObjects> String getLatestTimeObservation() { return this.latestTimeObservation; }

    /**
     * Returns the last observation time at SCB for the given table specified by the scbQuery.
     * @param scbQuery Holds the URL to the table and a query which specifies what data to request.
     * @return A string denoting the last time observation.
     */
    private <T extends ScbQueryObjects> String getLatestTimeObservation(T scbQuery) {
        JsonObject metadata = getMetadata(scbQuery);
        JsonArray variables = metadata.getJsonArray("variables");

        // Search for the time column and return the last value
        Boolean defaultValue = false;
        for (JsonObject j : variables.getValuesAs(JsonObject.class)) {
            if (j.getBoolean("time", defaultValue)) {
                JsonArray values = j.getJsonArray("values");
                return values.getString(values.size() - 1);
            }
        }
        return null;
    }

    private <T extends ScbQueryObjects> JsonObject getMetadata(T scbQuery) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            // Connect to SCB and send a GET request
            HttpGet request = new HttpGet(scbQuery.getUrl());
            request.setHeader("content-type", "application/json; charset=ISO-8859-1");
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String metaData = cleanBody(EntityUtils.toString(entity));
                JsonReader jsonReader = Json.createReader(new StringReader(metaData));
                return jsonReader.readObject();
            }
            httpClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private <T extends ScbQueryObjects> String getDatasetTitle(T scbQueryObject) {
        JsonObject metadata = getMetadata(scbQueryObject);
        // If metadata is null the program should crash
        return metadata.getString("title");
    }

    /**
     * Sends a POST request and returns a JsonObject holding the response body.
     * @param scbQuery Holds the URL to the specific query together with the specific
     *                 json query specifying what data to get.
     * @return Returns a JsonObject holding the response body. The the relevant structure for the
     * JsonObject is:
     * columns: [{code: .., text:.., type: ..},.., {code:.., text..:, type..:}], and with
     * data: [{key: [k1, k2,..,kn], values: [val]}]. Where each key ki`s dimension is specified by
     * the text field with type not equal to 'c' and the dimension for the values are specified by
     * the text field with type = 'c'
     */
    private <T extends ScbQueryObjects> JsonObject responseBody(T scbQuery) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(scbQuery.getUrl());
            StringEntity params = new StringEntity(scbQuery.getJsonQuery().toString());
            request.setHeader("content-type", "application/json; charset=ISO-8859-1");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String body = cleanBody(EntityUtils.toString(entity));
                JsonReader jsonReader = Json.createReader(new StringReader(body));

                JsonObject responseBody = jsonReader.readObject();
                return responseBody;
            }
            httpClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /* Remove Windows newline character and remove BOM character. */
    private String cleanBody(String body) {
        body = body.replaceAll("\\\\r\\\\n|\\\\n", "");
        body = body.replace("\uFEFF", "");

        return body;
    }

    /* Returns a mapping over columns to column data type. */
    private HashMap<String, String> columnNamesToTypes(JsonArray columns) {
        HashMap<String, String> columnNamesToTypes = new HashMap<>();

        for (JsonObject jObject: columns.getValuesAs(JsonObject.class)) {
            columnNamesToTypes.put(jObject.getString("code"), getType(jObject));
        }
        return columnNamesToTypes;
    }

    /* Returns a mapping over columns to indices for keys and values.  */
    private HashMap<String, Integer> columnNamesToIndex(JsonArray columns) {
        HashMap<String, Integer> columnNamesToIndex = new HashMap<>();
        int keysIndex = 0;
        int valuesIndex = 0;

        for (JsonObject jObject: columns.getValuesAs(JsonObject.class)) {
            String type = getType(jObject);
            String columnName = jObject.getString("code");
            if (type.equals("String")) {
                columnNamesToIndex.put(columnName, keysIndex);
                keysIndex++;
            } else {
                columnNamesToIndex.put(columnName, valuesIndex);
                valuesIndex++;
            }
        }

        return columnNamesToIndex;
    }

    /* Returns the data type for the column. */
    private String getType(JsonObject jObject) {
        String defaultValue = null;
        String type = jObject.getString("type", defaultValue);

        return type.equals("c") ? "Number" : "String";
    }

    private ArrayList<String> columnNames(JsonArray columns) {
        ArrayList<String> columnNames = new ArrayList<>();
        for (JsonObject jObject: columns.getValuesAs(JsonObject.class)) {
            columnNames.add(jObject.getString("code")) ;
        }
        return columnNames;
    }

    private ArrayList<String> columnTypes(JsonArray columns) {
        ArrayList<String> columnTypes = new ArrayList<>();
        for (JsonObject jObject: columns.getValuesAs(JsonObject.class)) {
            columnTypes.add(getType(jObject));
        }
        return columnTypes;
    }
}
