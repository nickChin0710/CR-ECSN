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

import java.io.*;
import java.util.*;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoHelp extends TarokoDAO {
  /* 顯示啟始畫面 */
  public void showHelp(TarokoCommon wr) throws Exception {
    super.wp = wr;

    wp.showLogMessage("D", "showHelp", "started11");

    wp.setValue("OPERATION_DESC", "", 0);
    wp.setValue("BUSINESS_DESC", "", 0);
    wp.setValue("PROGRAM_SPEC", "", 0);

    wp.daoTable = "COMM_HELP_DATA";
    wp.whereStr = "WHERE HTML_NAME = ? AND ITEM_TYPE = 'OP' ";
    // setString(1,wp.helpHtml);
    selectTable();

    wp.setValue("OPERATION_DESC", wp.getValue("DESC_DATA", 0), 0);

    wp.daoTable = "COMM_HELP_DATA";
    wp.whereStr = "WHERE HTML_NAME = ? AND ITEM_TYPE = 'FU' ";
    // setString(1,wp.helpHtml);
    selectTable();

    // wp.setValue("BUSINESS_DESC",wp.getValue("DESC_DATA",0),0);

    // wp.setValue("OPERATION_DESC",wp.helpHtml +" 操作說明",0);
    // wp.setValue("BUSINESS_DESC",wp.helpHtml +" 作業功能說明",0);
    // wp.setValue("PROGRAM_SPEC",wp.helpJava +" 程式規範",0);

    processTab();

    wp.respHtml = "TarokoHelp";
    wp.notFound = "";
    wp.showLogMessage("D", "showHelp", "ended");

    return;
  }

  /* TAB 控制處理 */
  public void processTab() throws Exception {
    TarokoTAB tab = new TarokoTAB();
    tab.tabType = "S";
    tab.tabWidth = 300;
    tab.tabHeight = 30;
    tab.setTabConent(1, "操作說明");
    tab.setTabConent(2, "作業功能");
    if (wp.loginUser.equals("0900")) {
      tab.setTabConent(3, "程式規範");
    }
    tab.generateTab(wp);
    tab = null;
    return;
  }

} // End of class TarokoHelp
