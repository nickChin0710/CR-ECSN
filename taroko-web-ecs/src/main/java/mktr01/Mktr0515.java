/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110-12-31  V1.00.01     馬超         新增功能: mktr0515員工推卡達成率報表                           *
 ***************************************************************************/
package mktr01;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;
import taroko.com.TarokoJasperUtils;

// ************************************************************************
public class Mktr0515 extends BaseAction implements InfaceExcel {
  private String PROGNAME = "員工推卡達成率報表 110/12/29 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  String dataKK1;
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];

  // ************************************************************************
  @Override
  public void userAction() throws Exception {
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "XLS")) {/* Excek- */
      strAction = "XLS";
      xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // 導出PDF報表
      strAction = "PDF";
      pdfPrint();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
	  if(wp.itemEmpty("ex_introduce_emp_no")) {
		  wp.whereStr = "WHERE 1=1 "
		            + sqlCol(wp.itemStr("ex_record_date_s"), "a.ACCT_MONTH", "like%")
		            + sqlCol(wp.itemStr("ex_BRANCH"), "a.BRANCH", "like%")
		    ; 
	  }else {
		  wp.whereStr = "WHERE 1=1 "
		            + sqlCol(wp.itemStr("ex_record_date_s"), "a.ACCT_MONTH", "like%")
		            + sqlCol(wp.itemStr("ex_BRANCH"), "a.BRANCH", "like%")
		            + sqlCol(wp.itemStr("ex_introduce_emp_no"), "a.INTRODUCE_EMP_NO", "like%")
		    ;  
	  }
	  
    
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " "
            + "a.BRANCH, "
            + "a.EMPLOYEE_CNT, "
            + "a.INTRODUCE_EMP_NO, "
            + "a.M_ACT_CARD_CNT + a.M_NOACT_CARD_CNT as ACT_CARD_CNT, "
            + "a.CONSUME_CARD_CNT, "
            + "a.COMMON_FEES + a.CA_FEES as FEES, "
            + "dec((a.M_ACT_CARD_CNT + a.M_NOACT_CARD_CNT) / a.EMPLOYEE_CNT * 100,10,1) as M_RATE, "
            + "a.Y_ACT_CARD_CNT + a.Y_NOACT_CARD_CNT as Y_ACT_CARD_CNT, "
            + "a.Y_CONSUME_CARD_CNT, "
            + "a.YEAR_FEES, "
            + "dec((a.Y_ACT_CARD_CNT + a.Y_NOACT_CARD_CNT) / a.EMPLOYEE_CNT * 100,10,1) as Y_RATE, "
            + "a.H_ACT_CARD_CNT + a.H_NOACT_CARD_CNT as H_ACT_CARD_CNT, "
            + "a.H_CONSUME_CARD_CNT, "
            + "a.H_YEAR_FEES "
    ;

    wp.daoTable = " MKT_MCARD_STATIC_REACH a ";
    wp.whereOrder = " "
            + " order by a.BRANCH";

    wp.pageCountSql = " select count(*) "
            + " from " + wp.daoTable + " " + wp.queryWhere
    ;

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
//    wp.colSet("BRANCH", wp.itemStr("ex_BRANCH"));
//    // 獲取年度目標書
//    wp.colSet("TARGET_CARD_CNT", get_TARGET_CARD_CNT());
//    // 獲取年度合計
//    wp.colSet("T_Y_ACT_CARD_CNT", get_T_Y_ACT_CARD_CNT());
//    wp.colSet("Y_RATE", String.format("%.3f", Double.parseDouble(wp.colStr("T_Y_ACT_CARD_CNT"))/Double.parseDouble(wp.colStr("TARGET_CARD_CNT"))*100));

  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    dataKK1 = itemkk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    try {
      if ((wp.respHtml.equals("mktr0515"))) {
        wp.initOption = "--";
        wp.optionKey = itemkk("ex_BRANCH");
        if (wp.colStr("ex_BRANCH").length() > 0) {
          wp.optionKey = wp.colStr("ex_BRANCH");
        }
        this.dddwList("dddw_BRANCH_b", "GEN_BRN", "trim(BRANCH)", "trim(BRIEF_CHI_NAME)",
                " where 1 = 1 order by BRANCH");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "mktr0515";
      String allStr = "";
      if (wp.itemStr("ex_BRANCH").length() > 0)
        allStr = allStr + "專案代號：" + wp.itemStr("ex_BRANCH");
      if (wp.itemStr("ex_apply_type").length() > 0)
        allStr = allStr + "  申請方式：" + wp.itemStr("ex_apply_type");
      if (wp.itemStr("ex_record_date").length() > 0)
        allStr = allStr + "  登錄日期：" + wp.itemStr("ex_record_date");
      wp.colSet("cond1", allStr);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "mktr0515.xlsx";
      wp.pageRows = 9999;
      queryFunc();
      wp.setListCount(1);
      queryFunc();
      wp.listCount[1] = sqlRowNum;
      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }
  }
  // 打印PDF
  public void pdfPrint() throws Exception {
    try {
      String exDate1 = wp.itemStr("ex_record_date_s"); // 查詢年月
      String branch = wp.itemStr("ex_BRANCH"); // 受理行

      String cond1 = "" +
              "查詢年月: " + dateFormatTo(exDate1, "yyyyMM", "yyyy/MM");

      // 設定報表頭信息
      HashMap<String, Object> params = new HashMap<String, Object>();
      params.put("title", "員工推卡達成率報表");
      params.put("report", "Report: mktr0515");
      params.put("user", "User: " + wp.loginUser);
      params.put("date", "Date: " + new SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date()));
      params.put("time", "Time: " + new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
      params.put("cond1", cond1);
//      params.put("TARGET_CARD_CNT", get_TARGET_CARD_CNT());
//      params.put("T_Y_ACT_CARD_CNT", get_T_Y_ACT_CARD_CNT());
//      params.put("Y_RATE", String.format("%.3f", Double.parseDouble((String) params.get("T_Y_ACT_CARD_CNT"))/Double.parseDouble((String) params.get("TARGET_CARD_CNT"))*100));

      // sql 執行
      ArrayList<Object> pa = new ArrayList<Object>();
      String sqlStr = "SELECT " +
              "a.BRANCH, " +	  
              "a.EMPLOYEE_CNT, " +
              "a.INTRODUCE_EMP_NO, "+
              "a.M_ACT_CARD_CNT + a.M_NOACT_CARD_CNT as ACT_CARD_CNT, " +
              "a.CONSUME_CARD_CNT, " +
              "a.COMMON_FEES + a.CA_FEES as FEES, " +
              "dec((a.M_ACT_CARD_CNT + a.M_NOACT_CARD_CNT) / a.EMPLOYEE_CNT * 100,10,1) as M_RATE, " +
              "a.Y_ACT_CARD_CNT + a.Y_NOACT_CARD_CNT as Y_ACT_CARD_CNT, " +
              "a.Y_CONSUME_CARD_CNT, " +
              "a.YEAR_FEES, " +
              "dec((a.Y_ACT_CARD_CNT + a.Y_NOACT_CARD_CNT) / a.EMPLOYEE_CNT * 100,10,1) as Y_RATE, " +
              "a.H_ACT_CARD_CNT + a.H_NOACT_CARD_CNT as H_ACT_CARD_CNT, " +
              "a.H_CONSUME_CARD_CNT, " +
              "a.H_YEAR_FEES " +
              "FROM MKT_MCARD_STATIC_REACH a " +
              "WHERE 1=1 and a.ACCT_MONTH like ? ";
      pa.add(exDate1);
      if (branch.length() > 0) {
        sqlStr += "and a.BRANCH = ? ";
        pa.add(branch);
      }
      sqlStr += "ORDER BY a.BRANCH";

      sqlSelect(sqlStr, pa.toArray(new Object[pa.size()]));

      ArrayList<HashMap> list = new ArrayList<HashMap>();
      // sql查詢結果獲取
      for (int i = 0; i < sqlRowNum; i++) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("BRANCH",  sqlStr(i, "BRANCH"));  
        item.put("EMPLOYEE_CNT",  sqlStr(i, "EMPLOYEE_CNT"));
        item.put("INTRODUCE_EMP_NO",  sqlStr(i, "INTRODUCE_EMP_NO"));
        item.put("ACT_CARD_CNT",  sqlStr(i, "ACT_CARD_CNT"));
        item.put("CONSUME_CARD_CNT",  sqlStr(i, "CONSUME_CARD_CNT"));
        item.put("FEES",  sqlStr(i, "FEES"));
        item.put("M_RATE",  sqlStr(i, "M_RATE"));
        item.put("Y_ACT_CARD_CNT",  sqlStr(i, "Y_ACT_CARD_CNT"));
        item.put("Y_CONSUME_CARD_CNT",  sqlStr(i, "Y_CONSUME_CARD_CNT"));
        item.put("YEAR_FEES",  sqlStr(i, "YEAR_FEES"));
        item.put("Y_RATE",  sqlStr(i, "Y_RATE"));
        item.put("H_ACT_CARD_CNT",  sqlStr(i, "H_ACT_CARD_CNT"));
        item.put("H_CONSUME_CARD_CNT",  sqlStr(i, "H_CONSUME_CARD_CNT"));
        item.put("H_YEAR_FEES",  sqlStr(i, "H_YEAR_FEES"));
        list.add(item);
      }

      TarokoJasperUtils.exportPdf(wp, "mktr0515", "mktr0515", params, list);

    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /***
   * 日期月份减指定月份
   *
   * @param datetime
   *            日期(2014-11)
   * @param num
   *            要减去的月份数
   * @return 2014-10
   */
  public String subMonth(String datetime, int num) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    Date date = null;
    try {
      date = sdf.parse(datetime);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Calendar cl = Calendar.getInstance();
    cl.setTime(date);
    cl.add(Calendar.MONTH, -num);
    date = cl.getTime();
    return sdf.format(date);
  }

  /**
   * 轉換日期格式
   * @param datetime 日期字符串
   * @param format1 原有格式
   * @param format2 目標格式
   * @return 字符串
   * @throws Exception
   */
  public String dateFormatTo(String datetime, String format1, String format2) throws Exception  {
    SimpleDateFormat sdf1 = new SimpleDateFormat(format1);
    SimpleDateFormat sdf2 = new SimpleDateFormat(format2);
    Date date = sdf1.parse(datetime);
    return sdf2.format(date);
  }


  /**
   * 检查文件的存在性,不存在则创建相应的路由，文件
   * @param filepath
   * @return 创建失败则返回null
   */
  public File checkFileExistence(String filepath){
    File file = new File(filepath);
    try {
      if (!file.exists()){
        if (filepath.charAt(filepath.length()-1) == '/' || filepath.charAt(filepath.length()-1) == '\\') {
          file.mkdirs();
        } else {
          String[] split = filepath.split("[^/\\\\]+$");
          checkFileExistence(split[0]);
          file.createNewFile();
        }
      }
    }catch (IOException e) {
      e.printStackTrace();
      file = null;
    }
    return file;
  }

  // ************************************************************************
  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }


} // End of class
