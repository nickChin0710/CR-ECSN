/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 107-12-07  V1.00.00  Jack,Liao  關帳統計表                                 *
 * 111/10/28  V1.00.01  jiangyigndong  updated for project coding standard    *
 *                                                                            *
 ******************************************************************************/

package cycr01;

import ofcapp.BaseAction;
import taroko.com.TarokoExcel;
import java.util.*;

public class Cycr0050 extends BaseAction {

  String xlsFunction = "";
  String mProgName = "cycr0050";

  @Override
  public void userAction() throws Exception {
    rc = 1;
    strAction = wp.buttonCode;
    switch (wp.buttonCode)
    {
      case "Q"    : queryFunc();    /* 查詢功能 */
        break;
      case "L"    : strAction = ""; /* 清畫面   */
        clearFunc();
        break;
      case "XLS"  : xlsPrint();     /* 產生 Excel */
        break;
      default     : break;
    }
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.setQueryMode();
    queryRead();
    dddwSelect();
  }

  @Override
  public void queryRead() throws Exception {

    int saveCount=0,sv=0;
    String[] monNum  = {"01","02","03","04","05","06","07","08","09","10","11","12",""};
    String[] monName = {"jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};
    Object   param[] = {"","",""};

    HashMap<String,String>  totalTxDesc  = new  HashMap<String,String>();
    HashMap<String,String>  totalTxData  = new  HashMap<String,String>();

    String curr_code = wp.colStr("ex_curr_code");
    String acct_type = wp.colStr("ex_acct_type");

    if ( curr_code.length() == 0 )
    { curr_code = "77"; }
    if ( acct_type.length() == 0 )
    { acct_type = "77"; }

    wp.colSet(0,"YYY",wp.colStr("stat_year"));
    wp.colSet(0,"curr_code",curr_code);
    wp.colSet(0,"acct_type",acct_type);

    String alias="";
    for ( int i=0; i<12; i++ ) {

      daoTid = monName[i]+"_";
      alias = daoTid;

      wp.pageControl();
      wp.sqlCmd = "select "
              + "acct_month ,"
              + "curr_code ,"
              + "acct_type ,"
              + "stat_type ,"
              + "sort_code ,"
              + "bill_type ,"
              + "bill_desc ,"
              + "stat_data_03 ,"
              + "stat_data_06 ,"
              + "stat_data_09 ,"
              + "stat_data_12 ,"
              + "stat_data_15 ,"
              + "stat_data_18 ,"
              + "stat_data_21 ,"
              + "stat_data_24 ,"
              + "stat_data_27 ,"
              + "(stat_data_03+stat_data_06+stat_data_09+stat_data_12+stat_data_15+stat_data_18+stat_data_21+stat_data_24+stat_data_27) as stat_data_tt ,"
              + "dummy_code "
              + "from cyc_statistic "
              + "where acct_month like ? and curr_code = ? and acct_type = ? order by stat_type,sort_code,card_type,acct_code,bill_type ";
      param[0] = wp.colStr("stat_year")+monNum[i]+"%";
      param[1] = curr_code;
      param[2] = acct_type;
      pageQuery(param);
      wp.setListCount(i+1);
      //if ( sql_notFind() )
      //   { continue; }
      for ( int k = 0; k < wp.selectCnt; k++)
      { listWkdata(i,k,alias);  }
      procBillType(alias,totalTxDesc,totalTxData);
      if ( saveCount == 0 && wp.selectCnt > 5 )
      { saveCount = wp.selectCnt; sv = i; }
    }

    // 年度 TAG 合計

    procYearTotal(saveCount,monName[sv]+"_",monName,totalTxDesc,totalTxData);
    wp.notFound ="";
    wp.notFoundMesg = false;
  }

  // 說明欄 顏色控制
  void listWkdata(int i, int k, String alias) throws Exception {

    if ( wp.colStr(k,alias+"sort_code").equals("00") ) {
      if ( xlsFunction.equals("Y") )
      {
        String statType = wp.colStr(k,alias+"stat_type");
        if ( statType.equals("00") )
        { wp.colSet(k,"XLS_COLOR_"+i,"153, 204, 255"); }
        else
        if ( statType.equals("01") )
        { wp.colSet(k,"XLS_COLOR_"+i,"173, 235, 173"); }
        else
        if ( Arrays.asList("2A","2B","2C").contains(statType) )
        { wp.colSet(k,"XLS_COLOR_"+i,"204, 204, 179"); }
        else
        { wp.colSet(k,"XLS_COLOR_"+i,"255, 204, 204"); }
      }
      else  {
        String statType = wp.colStr(k,alias+"stat_type");
        if ( statType.equals("00") )
        { wp.colSet(k,alias+"BG_COLOR","style='background:#b3daff'"); }
        else
        if ( statType.equals("01") )
        { wp.colSet(k,alias+"BG_COLOR","style='background:#c2f0c2'"); }
        else
        if ( Arrays.asList("2A","2B","2C").contains(statType) )
        { wp.colSet(k,alias+"BG_COLOR","style='background:#dcdcbc'"); }
        else
        { wp.colSet(k,alias+"BG_COLOR","style='background:#f2d9e6'"); }
      }
    }
    return;
  }

  // 每月 BILL_TYPE 統計不同
  void procBillType(String alias, HashMap<String,String> totalTxDesc, HashMap<String,String> totalTxData) throws Exception {

    for (int k = 0; k < wp.selectCnt; k++) {
      String statType = wp.colStr(k,alias+"stat_type");
      if ( Arrays.asList("3A","3B").contains(statType) ) {
        totalTxDesc.put(statType+"-"+wp.colStr(k,alias+"bill_type"),wp.colStr(k,alias+"bill_desc"));
        totalTxData.put(alias+statType+"-"+wp.colStr(k,alias+"bill_type"),wp.colStr(k,alias+"stat_data_tt"));
      } else {
        wp.colSet(k,alias+"stat_data_yy",wp.colStr(k,alias+"stat_data_tt"));
      }
    }
    return;
  }

  // 年度 TAG 合計
  void procYearTotal(int saveCount, String alias, String[] monName, HashMap<String,String> totalTxDesc, HashMap<String,String> totalTxData) throws Exception {

    int cnt=0;
    for ( int k = 0; k < saveCount; k++ ) {
      String statType = wp.colStr(k,alias+"stat_type");
      if ( Arrays.asList("3A","3B").contains(statType))
      { continue; }
      if ( cnt < 9 )
      { wp.colSet(cnt,"SER_NUM_TOT","0"+(cnt+1)); }
      else
      { wp.colSet(cnt,"SER_NUM_TOT",""+(cnt+1));  }
      wp.colSet(cnt,"tot_bill_desc",wp.colStr(cnt,alias+"bill_desc"));

      wp.colSet(k,"yyy_stat_type",wp.colStr(k,alias+"stat_type"));
      wp.colSet(k,"yyy_sort_code",wp.colStr(k,alias+"sort_code"));
      listWkdata(12,k,"yyy_");
      cnt++;
    }

    String[] keyArray = (String[])(totalTxDesc.keySet().toArray(new String[0]));
    Arrays.sort(keyArray);
    for ( String srtKey : keyArray ) {
      wp.colSet(cnt,"SER_NUM_TOT",""+(cnt+1));
      wp.colSet(cnt,"tot_bill_desc",(String)totalTxDesc.get(srtKey));
      for ( int i=0; i<12; i++ ) {
        String checkData = (String)totalTxData.get(monName[i]+"_"+srtKey);
        if ( checkData == null )
        { checkData = "0"; }
        wp.colSet(cnt,monName[i]+"_"+"stat_data_yy",checkData);
      }
      if ( srtKey.length() == 3 ) // desc record
      {
        if ( xlsFunction.equals("Y") )
        { wp.colSet(cnt,"XLS_COLOR_12","255, 204, 204"); }
        else
        { wp.colSet(cnt,"yyy_BG_COLOR","style='background:#f2d9e6'"); }
      }
      cnt++;
    }

    for ( int i=0; i<cnt; i++ ) {
      double totalData=0;
      for ( int k=0; k<12; k++ )
      { totalData += wp.colNum(i,monName[k]+"_"+"stat_data_yy"); }
      wp.colSet(i,"tot_stat_data_yy",""+totalData);
    }

    wp.selectCnt = cnt;
    wp.setListCount(13);

    return;
  }

  void xlsPrint() throws Exception {

    xlsFunction = "Y";
    TarokoExcel xlsx   = new TarokoExcel();
    xlsx.excelTemplate = mProgName + ".xlsx";
    //xlsx.pageBreak="Y";  // 是否分頁
    //xlsx.pageCount=13;   // 每頁幾筆
    wp.pageRows = 99999;
    queryFunc();
    xlsx.processExcelSheet(wp);
    xlsx.outputExcel();
    xlsx = null;

    return;
  }

  @Override
  public void dddwSelect() {
    try {
      wp.initOption = "全部";
      wp.optionKey  = wp.itemStr("ex_curr_code");
      dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc", "where 1=1 and wf_type = 'DC_CURRENCY' order by wf_id desc");

      wp.initOption = "全部";
      wp.optionKey  = wp.itemStr("ex_acct_type");
      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 and acct_type != '07' order by acct_type");

    }
    catch (Exception ex) { }
  }

  @Override
  public void dataRead() throws Exception {
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void initPage() {
  }

  @Override
  public void procFunc() {
  }

  @Override
  public void saveFunc() throws Exception {
  }

  @Override
  public void initButton() {
  }

} // end of class
