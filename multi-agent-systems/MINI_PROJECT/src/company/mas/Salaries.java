package company.mas;

import java.util.HashMap;
import java.util.Map;

public class Salaries {
    public static final Map<String, Integer> salaries = new HashMap<String, Integer>() {{
        put("Hr", 1500);
        put("Manager", 3000);
        // Note: The below integers represents the amount of money they receive per project (multiplied with the workload of the project), not their monthly salary
        put("UiUx", 60);
        put("Developer", 80);
        put("Qa", 70);
    }};
}
