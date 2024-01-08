/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package taroko.com;

import java.util.HashMap;
import java.util.Map;

public class ManualBean {

  private static ManualBean instance = new ManualBean();

  static boolean reloadMode = true;
  static String compDate = "";
  static Map<String, String> manualHash = new HashMap<String, String>(); // 儲存 MANUAL MAPPING 資料

  private ManualBean() {}

  public static ManualBean getInstance() {
    return instance;
  }

  public void setManualData(Map<String, String> parmManual) {

    setReloadMode(false);
    manualHash.clear();
    for (Map.Entry manu : parmManual.entrySet()) {
      manualHash.put((String) manu.getKey(), (String) manu.getValue());
    }

    return;
  }

  public String[] getManualData(String menuSeq) {
    String[] nullString = {""};
    String checkData = (String) manualHash.get(menuSeq);
    if (checkData == null) {
      return nullString;
    }
    String[] manualData = checkData.split("#");
    return manualData;
  }

  public void setReloadMode(boolean parmMode) {
    reloadMode = parmMode;
    return;
  }

  public boolean checkLoadManual(String currDate) {
    if (!currDate.equals(compDate)) { // 每天最少 RELOAD 一次
      reloadMode = true;
      compDate = currDate;
    }
    return reloadMode;
  }

}
