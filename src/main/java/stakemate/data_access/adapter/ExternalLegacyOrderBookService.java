package stakemate.data_access.adapter;

import java.util.HashMap;
import java.util.Map;

public class ExternalLegacyOrderBookService {
    public Map<String, Double> fetchLegacyData(String id) {
        // Returns data in weird format: Key="BID_0.50", Value=100.0
        Map<String, Double> data = new HashMap<>();
        data.put("BID_0.50", 100.0);
        data.put("ASK_0.60", 200.0);
        return data;
    }
}
