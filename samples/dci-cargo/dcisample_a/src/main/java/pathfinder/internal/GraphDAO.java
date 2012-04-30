package pathfinder.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GraphDAO {

  private static final Random random = new Random();

  public List<String> listLocations() {
    return new ArrayList<String>(Arrays.asList(
      "CNHKG", "AUMEL", "SESTO", "FIHEL", "USCHI", "JNTKO", "DEHAM", "CNSHA", "NLRTM", "SEGOT", "CNHGH", "USNYC", "USDAL"
    ));
  }

  public String getVoyageNumber(String from, String to) {
    final int i = random.nextInt(5);
    if (i == 0) return "V100S";
    if (i == 1) return "V200T";
    if (i == 2) return "V300A";
    if (i == 3) return "V400S";
    return "V500S";
  }
  
}
