package scb;

import javax.json.JsonObject;

public interface ScbQueryObjects {
    String getUrl();
    JsonObject getJsonQuery();
    String getDatasetName();
}
