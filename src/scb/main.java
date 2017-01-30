package scb;

public class main {

    public static void main(String[] args) {
        for (QueryObjectsPriceIndices qo : QueryObjectsPriceIndices.values()) {
            System.out.println(qo.getUrl());
            System.out.println(qo.getJsonQuery().toString());
        }

        RequestBody req = new RequestBody(
                QueryObjectsPriceIndices.ByProductProducerPriceIndex);
    }
}
