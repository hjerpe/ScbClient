package scb;

import javax.json.*;

/**
 * Class holding an URL and a query used to request data from the API provided
 * by the Statistics Sweden agency (SCB).
 * @author Adam Hjerpe
 * @version 1.0
 * @since 2016-12-12
 */
public class ScbQueryObject {

    /**
     * Table to query data from SCB.
     */
    private String url;

    /**
     *  What category/type of data to query, e.g. Consumer Price Index.
     */
    private String datasetName;

    /**
     * Holds the query for requesting data from SCB.
     */
    private JsonObject jsonQuery;


    public enum DataSets {
        /**
         * Enumeration of available DataSets with available ScbQueryObject(s).
         */
        ByProductImportPriceIndex,
        ByProductExportPriceIndex,
        ByProductHomeSalesProducerPriceIndex,
        ByProductProducerPriceIndex,
        ByProductDomesticSupplyPriceIndex,
        ByProductServicesProducerPriceIndex,
        ByProductConsumerPriceIndex
    }

    /**
     * Returns a query object holding the url, query, and the name of the data set.
     * This data is used to (POST) request SCB for data.
     * @param dataSet This query objects data category, eg
     *                Consumer Price Index etc.
     */
    public ScbQueryObject(DataSets dataSet) {
        this.url = queryUrl(dataSet);
        this.datasetName = dataSet.name();
        this.jsonQuery = queryObject(dataSet);
    }

    public String getUrl() {
        return this.url;
    }

    public String getDatasetName() {
        return this.datasetName;
    }

    public JsonObject getJsonQuery() {
        return this.jsonQuery;
    }

    private String queryUrl(DataSets dataSet) {
        switch (dataSet) {
            case ByProductConsumerPriceIndex:
                return "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0101/PR0101A/KPICOI80M";

            case ByProductImportPriceIndex:
                return "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/IMPIM07";

            case ByProductExportPriceIndex:
                return "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/EXPIM07";

            case ByProductHomeSalesProducerPriceIndex:
                return "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/HMPIM07";

            case ByProductProducerPriceIndex:
                return "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/PPIM07";

            case ByProductDomesticSupplyPriceIndex:
                return "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/ITPIM07";

            case ByProductServicesProducerPriceIndex:
                return "http://api.scb.se/OV0104/v1/doris/en/ssd/START/PR/PR0301/PR0301B/TPI2005Kv07";
            default:
                return null;
        }
    }

    private JsonObject queryObject(DataSets dataSet) {

        JsonArrayBuilder values;
        switch (dataSet) {
            case ByProductConsumerPriceIndex:
                return Json.createObjectBuilder().
                        add("query", Json.createArrayBuilder().
                                        add(Json.createObjectBuilder().
                                                add("code", "VaruTjanstegrupp").
                                                add("selection", Json.createObjectBuilder().
                                                        add("filter", "vs:VaruTjänstegrCoicopA").
                                                        add("values", Json.createArrayBuilder().
                                                                add("01").
                                                                add("02").
                                                                add("03").
                                                                add("04").
                                                                add("05").
                                                                add("06").
                                                                add("07").
                                                                add("08").
                                                                add("09").
                                                                add("10").
                                                                add("11").
                                                                add("12")
                                                        )
                                                )
                                        ).add(Json.createObjectBuilder().
                                        add("code", "ContentsCode").
                                        add("selection", Json.createObjectBuilder().
                                                add("filter", "item").
                                                add("values", Json.createArrayBuilder().
                                                        add("PR0101B3")
                                                )
                                        )
                                )
                        ).
                        add("response", Json.createObjectBuilder().
                                add("format", "json")
                        ).build();

            case ByProductImportPriceIndex:
                values = arrayBuilderWithDefaultValues();
                return jsonTemplate(values);

            case ByProductExportPriceIndex:
                values = arrayBuilderWithDefaultValues();
                return jsonTemplate(values);

            case ByProductHomeSalesProducerPriceIndex:
                values = arrayBuilderWithDefaultValues();
                return jsonTemplate(values);

            case ByProductProducerPriceIndex:
                values = arrayBuilderWithDefaultValues();
                return jsonTemplate(values);

            case ByProductDomesticSupplyPriceIndex:
                values = arrayBuilderWithDefaultValues();
                return jsonTemplate(values);

            case ByProductServicesProducerPriceIndex:
                values = Json.createArrayBuilder().
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
                        add("TPI");
                return jsonTemplate(values);

            default:
                return null;
        }
    }

    private JsonArrayBuilder arrayBuilderWithDefaultValues() {
        return Json.createArrayBuilder().
                add("B-E").
                add("B").
                add("C").
                add("D").
                add("E");
    }

    private JsonObject jsonTemplate(JsonArrayBuilder arrayWithValues) {
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
