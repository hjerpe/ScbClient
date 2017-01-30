package scb;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public enum QueryObjectsPriceIndices implements ScbQueryObjects {
    ByProductImportPriceIndex(
            "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/IMPIM07",
            jsonTemplate(arrayBuilderWithDefaultValues())
    ),
    ByProductExportPriceIndex(
            "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/EXPIM07",
            jsonTemplate(arrayBuilderWithDefaultValues())
    ),

    ByProductHomeSalesProducerPriceIndex(
            "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/HMPIM07",
            jsonTemplate(arrayBuilderWithDefaultValues())
    ),
    ByProductProducerPriceIndex(
            "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/PPIM07",
            jsonTemplate(arrayBuilderWithDefaultValues())
    ),

    ByProductDomesticSupplyPriceIndex(
            "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/ITPIM07",
            jsonTemplate(arrayBuilderWithDefaultValues())
    ),
    ByProductServicesProducerPriceIndex(
            "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/TPI2005Kv07",
            jsonTemplate(
                    Json.createArrayBuilder().
                            add("49-53").
                            add("55-56").
                            add("61").
                            add("62").
                            add("64+69-71+73 ").
                            add("68").
                            add("69").
                            add("77+78+80+81 ").
                            add("80").
                            add("93+95-96").
                            add("TPI")
            )
    );

    private String url;
    private JsonObject jsonQuery;

    QueryObjectsPriceIndices(String url, JsonObject jsonQuery) {
        this.url = url;
        this.jsonQuery = jsonQuery;
    }

    public String getUrl() {
        return url;
    }

    public JsonObject getJsonQuery() {
        return jsonQuery;
    }

    public String getDatasetName() {
        return this.name();
    }

    private static JsonArrayBuilder arrayBuilderWithDefaultValues() {
        return Json.createArrayBuilder().
                add("B-E").
                add("B").
                add("C").
                add("D").
                add("E");
    }

    private static JsonObject jsonTemplate(JsonArrayBuilder arrayWithValues) {
        return Json.createObjectBuilder().
                add("query", Json.createArrayBuilder().
                        add(Json.createObjectBuilder().
                                add("code", "SPIN2007").
                                add("selection", Json.createObjectBuilder().
                                        add("filter", "item").
                                        add("values", arrayWithValues
                                        )
                                )
                        )
                ).
                add("response", Json.createObjectBuilder().
                        add("format", "json")
                ).
                build();
    }
}

