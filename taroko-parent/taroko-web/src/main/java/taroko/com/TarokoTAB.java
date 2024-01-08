/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 110-01-08  V1.00.02   shiyuqi       修改无意义命名                            
* 111-01-19  V1.00.03   Justin       fix Missing Check against Null          * 
******************************************************************************/
package taroko.com;

import java.io.*;
import java.util.*;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoTAB {
  public String tabTemplate = "", inputStr = "", tabStart = "", tabEnd = "", cvtString = "";
  // public String tabString="";
  public String[] tabContent = {"", "", "", "", "", "", "", "", "", ""};
  public int tabWidth = 200, tabHeight = 30, tabCount = 0;
  public String tabType = "K";

  StringBuffer tabString = new StringBuffer();

  public void generateTab(TarokoCommon wp) {
    FileReader fr = null;
    BufferedReader br = null;
    try {
      if (tabType.equals("S")) {
        tabTemplate = TarokoParm.getInstance().getHtmlDir() + "taroko/tabs/tabs-style-4.js";
      } else if (tabType.equals("S5")) {
        tabTemplate = TarokoParm.getInstance().getHtmlDir() + "taroko/tabs/tabs-style-5.js";
      } else if (tabType.equals("G")) {
        tabTemplate = TarokoParm.getInstance().getHtmlDir() + "taroko/tabs/tabs-glass.js";
      } else if (tabType.equals("I")) {
        tabTemplate = TarokoParm.getInstance().getHtmlDir() + "taroko/tabs/tabs-IE7.js";
      } else {
        tabTemplate = TarokoParm.getInstance().getHtmlDir() + "taroko/tabs/tabs-kasper.js";
      }

      fr = new FileReader(tabTemplate);
      br = new BufferedReader(fr);
      // tabString="";

      while (br.ready()) {
        inputStr = br.readLine();
        if (inputStr != null) {
        	if (inputStr.length() > 4) {
                if (inputStr.substring(0, 4).equals("W@@W")) {
                  cvtString = inputStr.replaceAll("W@@W", "    ");
                  cvtString = cvtString.replaceAll("TAB_WIDTH", tabWidth + "px");
                  tabString.append((cvtString + wp.newLine));
                  continue;
                } else if (inputStr.substring(0, 4).equals("H@@H")) {
                  cvtString = inputStr.replaceAll("H@@H", "    ");
                  cvtString = cvtString.replaceAll("TAB_HEIGHT", tabHeight + "px");
                  tabString.append((cvtString + wp.newLine));
                  continue;
                } else if (inputStr.substring(0, 4).equals("S@@S")) {
                  tabStart = inputStr.replaceAll("S@@S", "    ");
                  if (tabType.equals("S") || tabType.equals("I")) {
                    processContent(wp);
                  }
                  continue;
                } else if (inputStr.substring(0, 4).equals("E@@E")) {
                  tabEnd = inputStr.replaceAll("E@@E", "    ");
                  processContent(wp);
                  continue;
                } else {
                  tabString.append((inputStr + wp.newLine));
                }
              } else {
                tabString.append((inputStr + wp.newLine));
              }
		}
      } // end of while

      wp.setValue("TAB_SCRIPT", tabString.toString(), 0);
    }

    catch (Exception ex) {
      wp.expMethod = "generateTab";
      wp.expHandle(ex);
    }

    finally {
      try {
        if (fr != null) {
          fr.close();
        }
        fr = null;
        if (br != null) {
          br.close();
        }
        br = null;
      } catch (Exception ex2) {
      }
    }
    return;
  }

  public void processContent(TarokoCommon wp) throws Exception {
    for (int i = 0; i < tabCount; i++) {
      cvtString = tabStart.replaceAll("TAB_DESC", tabContent[i]);
      cvtString = cvtString.replaceAll("TAB_CONTENT", ("content" + (i + 1)));
      if (i == 0) {
        cvtString = cvtString.replaceAll("TAB_INDEX", "1");
      } else if (i == (tabCount - 1)) {
        cvtString = cvtString.replaceAll("TAB_INDEX", "2");
      } else {
        cvtString = cvtString.replaceAll("TAB_INDEX", "");
      }

      tabString.append((cvtString + wp.newLine));
      if (i != (tabCount - 1)) {
        tabString.append((tabEnd + wp.newLine));
      }
    }
  }

  public void setTabConent(int cnt, String tabDesc) throws Exception {
    tabCount = cnt;
    tabContent[cnt - 1] = tabDesc;
  }

} // end of class
