/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-12-07  V1.00.00              DM 參數維護                                                                                            *
* 2020-03-09 V1.00.01 Yuqi Shi    修改參數條件及文字                                                                                 *
* 109-04-23  V1.00.01  YangFang   updated for project coding standard        *
* 109-05-28  V1.00.02  JustinWu    wp.rootDir + "/WebData/work -> wp.workDir
* 111/08/22  V1.00.03   Machao      Mktm0110 DM參數資料檔維護
* 111/09/08  V1.00.04   Machao      調整畫面:選項,更名,格式設定微調
* 111/09/16  V1.00.04   Machao      調整畫面選項, 修訂[匯入修件]menu 的寫檔處理
* 111/09/21  V1.00.04   Machao      頁面bug調整
* 111/09/28  V1.00.04   Machao      頁面刪除匯入button調整
* 111/12/27  V1.00.05  Zuwei Su   sync from mega                                          *
* 111/12/29  V1.00.06  Zuwei Su   匯入資料查詢總數判斷錯誤, 產出格式設定查询过滤掉空的optionText, naming rule update  *
* 112/08/28  V1.00.07  Grace Huang  MCHT_COUNTRY 如為空白者, 視同'TW'              *
******************************************************************************/

package mktm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;
import it.sauronsoftware.ftp4j.*;
import java.io.*;
import java.util.*;

@SuppressWarnings({"unchecked","deprecation"})
public class Mktm0110 extends BaseAction {

  String  groupCheck="",dummyCode="",detailCode="";
  int     seqNo=0,parmSeq=0;
  boolean visaDebit=false;

  @Override
  public void userAction() throws Exception {
    rc = 1;
    strAction = wp.buttonCode;
    switch (wp.buttonCode)
     {
       case "Q"    : queryFunc();      break;  /* 查詢功能  */
       case "M"    : queryRead();      break;  /* 瀏覽功能  */
       case "L"    : clearFunc();
                     showNewAdd();     break;  /* 清畫面    */
       case "X"    : showNewAdd();     break;  /* 顯示新增畫面 */
       case "A"    : insertFunc();     break;  /* 新增功能  */
       case "S"    : querySelect();    break;  /* 動態查詢  */
       case "R"    : dataRead();       break;  /* 資料讀取  */
       case "U"    : saveFunc();       break;  /* 更新資料  */
       case "D"    : deleteFunc();     break;  /* 刪除資料  */
       case "TC"   : typeConfirm();    break;  /* 篩選類別 確定 */
       case "DL"   : downLoad();       break;  /* 下載篩選名單 */
       case "AJAX" : ajaxFunction();   break;  /* AJAX 功能 */
       default     : break;
     }
  }

  /* AJAX 功能 */
  public void  ajaxFunction() throws Exception {

    String jdCode = wp.getValue("JD_CODE",0);
    switch (jdCode)
    {
       case "1"  : typeChange();     break;  /* 篩選類別 optionChange */
       case "3"  : formatChange();   break;  /* 格式類別 optionChange */
       case "4"  : formatConfirm();  break;  /* 格式類別 確定 */
       case "S"  : callBatch("S");   break;  /* 執行篩選   */
       case "G"  : callBatch("G");   break;  /* 產生篩選檔 */
       case "SD" : selTypeQuery();   break;  /* 顯示 已選篩選項目 */
       case "IM" : importData();     break;  /* 資料匯入 */
       case "ID" : deleteImport();   break;  /* 刪除匯入 */
       case "IS" : importDisplay();  break;  /* 顯示 已匯入項目 */
       case "Q2" : importQuery();    break;  /* 匯入資料查詢 */
       case "Q3" : statsQuery();     break;  /* 篩選分析查詢 */
       default   : break;
     }

    return;
  }

  /* 查詢功能  */
  @Override
  public void queryFunc() throws Exception {

    String lsWhere = "";
    lsWhere = " where 1=1 "
             + sqlCol(wp.getValue("ex_batch_no"),"batch_no")
             + sqlStrend(wp.getValue("ex_crt_date_s"), wp.getValue("ex_crt_date_e"), "crt_date");
    wp.whereStr   = lsWhere + " and template != 'Y' ";
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();

  }

  /* 顯示新增畫面 */
  public void showNewAdd() throws Exception {

    wp.initFlag="Y";
    wp.setValue("DIS_SEL", "disabled='disabled'", 0);
    wp.setValue("DIS_GEN", "disabled='disabled'", 0);
    wp.setValue("DIS_DW", "disabled='disabled'", 0);
    return;
  }


  @Override
  public void queryRead() throws Exception {

     wp.pageControl();
     wp.selectSQL  = "batch_no,"
                   + "file_mode,"
                   + "batch_desc,"
                   + "active_date_s,"
                   + "active_date_e,"
                   + "stat_count,"
                   + "total_count,"
                   + "proc_date,"
                   + "crt_date,"
                   + "crt_user,"
                   + "apr_user,"
                   + "apr_date ";
     wp.daoTable   = "mkt_dm_format";
     wp.whereOrder = "order by batch_no";
     pageQuery();
     wp.setListCount(0);
     if( sqlNotFind() ) {
         alertErr("此條件查無資料");
         return ;
      }
     wp.setPageValue();
     return;
  }

  @Override
  public void initPage() {
  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");
  }

  @Override
  public void procFunc() {
  }

  @Override
  public void dddwSelect() {
    try {
          wp.initOption = "--";
          wp.optionKey  = wp.itemStr("import_field_code");
          dddwList("dddw_imp_field", "select imp_field as db_code,imp_desc as db_desc from mkt_dm_imp_field order by imp_field");
        }
    catch (Exception ex) { }
  }

  /* 動態查詢  */
  @Override
  public void querySelect() throws Exception {

     String batchNo    = wp.itemStr("data_k1");
     String batchGroup = wp.itemStr("data_k2");
     clearFunc();
     wp.initFlag="Y";
     int n = selectMktDmParm(batchNo,batchGroup);
     if ( n == 0 ) {
          String batchDesc = wp.itemStr("batch_desc");
          selectMktDmFormat1(batchNo,batchGroup);
          wp.setValue("ex_batch_no",batchNo);
          wp.setValue("ex_batch_group",batchGroup);
          wp.setValue("ex_exclude_type", "A", 0);
          return;
       }

     selectMktDmFormat1(batchNo,batchGroup);
     wp.setValue("ex_batch_group",batchGroup);
     wp.setValue("DIS_CLEAR", "disabled='disabled'", 0);
     wp.setValue("ex_exclude_type", "A", 0);

     if ( Arrays.asList("1","2").contains(wp.getValue("proc_status")) ) {
          wp.setValue("DIS_SEL", "disabled='disabled'", 0);
          wp.setValue("DIS_GEN", "disabled='disabled'", 0);
          wp.setValue("DIS_DW", "disabled='disabled'", 0);
        }

     if ( !Arrays.asList("SE").contains(wp.getValue("proc_status")) ) {
          wp.setValue("DIS_GEN", "disabled='disabled'", 0);
        }

     if ( !Arrays.asList("GE").contains(wp.getValue("proc_status")) ) {
          wp.setValue("DIS_DW", "disabled='disabled'", 0);
        }

     return;
  }

  /* 資料讀取  */
  @Override
  public void dataRead() throws Exception {

    String batchNo    = wp.itemStr("ex_batch_no");
    String batchGroup = wp.itemStr("ex_batch_group");
    clearFunc();
    wp.initFlag="Y";
    selectMktDmParm(batchNo,batchGroup);
    selectMktDmFormat1(batchNo,batchGroup);
    wp.setValue("ex_batch_group",batchGroup);
    return;
  }

  public void selectMktDmFormat1(String batchNo,String batchGroup) throws Exception {

    wp.selectSQL  = "batch_no as ex_batch_no,"
                  + "batch_desc,"
                  + "active_date_s,"
                  + "active_date_e,"
                  + "file_mode,"
                  + "proc_status,"
                  + "merg_code_a,merg_code_b,merg_code_c,merg_code_d,merg_code_e,format_type ";
    wp.daoTable   = "mkt_dm_format";
    wp.whereStr   = "where batch_no = :batch_no ";
    setString("batch_no",batchNo);
    pageQuery();
    wp.setValue("formatListName",wp.getValue("format_type"));
    wp.setValue("format_flag","Y");

    switch (batchGroup)
     {
       case "A"  : wp.setValue("merg_code",wp.getValue("merg_code_a")); break;
       case "B"  : wp.setValue("merg_code",wp.getValue("merg_code_b")); break;
       case "C"  : wp.setValue("merg_code",wp.getValue("merg_code_c")); break;
       case "D"  : wp.setValue("merg_code",wp.getValue("merg_code_d")); break;
       case "E"  : wp.setValue("merg_code",wp.getValue("merg_code_e")); break;
       default   : break;
     }
    return;
  }

  public int selectMktDmParm(String batchNo,String batchGroup) throws Exception {

    wp.varRows   = 1000;
    wp.pageRows  = 1000;
    wp.selectSQL  = "exclude_code,"
                  + "field_code,"
                  + "field_name, "
                  + "operator, "
                  + "obj_type ";
    wp.daoTable   = "mkt_dm_parm";
    wp.whereStr   = "where batch_no    = :batch_no and batch_group = :batch_group "
                  + "and   field_code  < '600' ";
    wp.whereOrder = "order by sort_code,table_name,field_code,seq_no";

    setString("batch_no",batchNo);
    setString("batch_group",batchGroup);
    pageQuery();
    int parmCnt = wp.selectCnt;

    for (  int i=0; i < parmCnt; i++ )  {

           String exclude    = wp.getValue("exclude_code",i);
           String fieldCode  = wp.getValue("field_code",i);
           String fieldName  = wp.getValue("field_name",i);
           String objType    = wp.getValue("obj_type",i);
           String opCode     = wp.getValue("operator",i);
           String[] cvtField = fieldName.split(" AS ");
           String f_Name     = cvtField[0].trim();
           if ( cvtField.length > 1 )
              { f_Name = cvtField[1].trim(); }

           if ( fieldCode.equals("310") && f_Name.equals("PAY_RATE") )
              { wp.setValue("rate_months_op",opCode); }

           if ( fieldCode.equals("350") )
              { wp.setValue("purc_check-Y","checked",0); }
           else
              { wp.setValue("purc_check-N","checked",0); }

           if ( fieldCode.equals("385") )
              { wp.setValue("contract_amt-TOT","checked",0); }

           if ( fieldCode.equals("090") )
              { wp.setValue("tpan_dw_code-0","checked",0); }
           else
           if ( fieldCode.equals("091") )
              { wp.setValue("tpan_dw_code-1","checked",0); }

           selectMktDmCheckData(batchNo,batchGroup,exclude,fieldCode);

           if  ( f_Name.equals("NO_BILL") || f_Name.equals("EASY_TX") )
               { ; }
           else
           if  ( opCode.equals("*") )
               { wp.setValue(f_Name,wp.getValue("field_value",1));  continue; }
           else
           if  ( !objType.equals("C") )
               { wp.setValue(f_Name,wp.getValue("field_value",0));  continue; }

           for ( int k=0; k < wp.selectCnt; k++ )  {
                 String val = wp.getValue("field_value",k);
                 if ( objType.equals("C") )
                    { wp.setValue(f_Name+"-"+val,"checked",0); }
               }
         }

    return parmCnt;
  }

  public void selectMktDmCheckData(String batchNo,String batchGroup,String exclude,String fieldCode) throws Exception {

    wp.varRows    = 1000;
    wp.pageRows   = 1000;
    wp.selectSQL  = "field_value ";
    wp.daoTable   = "mkt_dm_check_data";
    wp.whereStr   = "where batch_no   = :batch_no and batch_group    = :batch_group "
                  + "and exclude_code = :exclude_code and field_code = :field_code";
    wp.whereOrder = "order by seq_no";
    setString("batch_no",batchNo);
    setString("batch_group",batchGroup);
    setString("exclude_code",exclude);
    setString("field_code",fieldCode);
    pageQuery();
    return;

  }

  /* 新增功能  */
  public void insertFunc() throws Exception {

    deleteBatchParm();

    processCheckField("A","010","CRD_CARD","BIN_TYPE","C","=");
    processCheckField("A","020","CRD_CARD","BIN_TYPE AS VD_CARD","C","=");

    processCheckField("A","030","CRD_CARD","SUP_FLAG","C","=");
    if ( !wp.getValue("VD_CARD").equals("V") ) {
         processCheckField("A","040","CRD_CARD","CURR_CODE","C","=");
         processCheckField("A","050","CRD_CARD","CARD_NOTE","C","=");
         processCheckField("A","060","CRD_CARD","decode(ELECTRONIC_CODE,'','00',ELECTRONIC_CODE","C","=");
       }
    processCheckField("A","070","CRD_CARD","decode(CURRENT_CODE,'0','0','1') AS CURR_STATUS","C","=");
    processCheckField("A","075","CRD_CARD","ACTIVATE_FLAG","C","=");

    processCheckField("A","080","CRD_CARD","ISSUE_DATE AS ISSUE_DATE_S","","<>");
    processCheckField("A","080","CRD_CARD","ISSUE_DATE AS ISSUE_DATE_E","","*");

    processCheckField("A","085","CRD_CARD","APPLY_NO AS APPLY_NO_S","","<>");
    processCheckField("A","085","CRD_CARD","APPLY_NO AS APPLY_NO_E","","*");

    if ( wp.getValue("TPAN_DW_CODE").equals("0") ) {
         processCheckField("A","090","HCE_CARD_LOG","MIN(CRT_DATE) AS TPAN_DATE_S","","<>");
         processCheckField("A","090","HCE_CARD_LOG","MIN(CRT_DATE) AS TPAN_DATE_E","","*");
       } else {
         processCheckField("A","091","HCE_CARD_LOG","MAX(CRT_DATE) AS TPAN_DATE_S","","<>");
         processCheckField("A","091","HCE_CARD_LOG","MAX(CRT_DATE) AS TPAN_DATE_E","","*");
    }

    if ( !wp.getValue("VD_CARD").equals("V") ) {
          processCheckField("A","120","CRD_CARD","JCIC_SCORE AS JCIC_SCORE_S","N","<>");
          processCheckField("A","120","CRD_CARD","JCIC_SCORE AS JCIC_SCORE_E","N","*");
       }

    processCheckField("A","125","CRD_CARD","LENGTH(OLD_CARD_NO) AS FIRST_APPLY","C","=");

    processCheckField("A","130","CRD_IDNO","SEX","C","=");
    processCheckField("A","140","CRD_IDNO","EDUCATION","C","=");
    processCheckField("A","150","CRD_IDNO","SUBSTR(BIRTHDAY,5,2) AS BIRTH_MONTH","C","=");
    processCheckField("A","160","CRD_IDNO","NATION","C","=");
    processCheckField("A","170","CRD_IDNO","MARRIAGE","C","=");

    groupCheck = "Y";
    processCheckField("A","190","CRD_IDNO","decode(ACCEPT_DM,'Y','DM',ACCEPT_DM) AS ACCEPT_DM","C","=");
    processCheckField("A","190","CRD_IDNO","decode(ACCEPT_MBULLET,'Y','MB',ACCEPT_MBULLET) AS ACCEPT_MB","C","=");
    processCheckField("A","190","CRD_IDNO","decode(ACCEPT_SMS,'Y','SMS',ACCEPT_SMS) AS ACCEPT_SMS","C","=");
    processCheckField("A","190","CRD_IDNO","decode(ACCEPT_CALL_SELL,'Y','TEL',ACCEPT_CALL_SELL) AS ACCEPT_TEL","C","=");
    processCheckField("A","190","CRD_IDNO","decode(E_NEWS,'Y','EN',E_NEWS) AS ACCEPT_EN ","C","=");
    groupCheck = "";

    processCheckField("A","210","CRD_IDNO","BIRTHDAY AS AGE_S","N","<>");
    processCheckField("A","210","CRD_IDNO","BIRTHDAY AS AGE_E","N","*");

    processCheckField("A","211","CRD_IDNO","1 AS NEW_CARD","C","=");

    if ( wp.getValue("iss_chk_year").length() > 0 && wp.getValue("iss_chk_cnt").length() > 0 ) {
         processCheckField("A","216","CRD_IDNO","2 AS ISS_CHK_YEAR","N","=");
         processCheckField("A","217","CRD_IDNO","3 AS ISS_CHK_CNT","N",">=");
       }

    processCheckField("A","220","ACT_ACNO","ACCT_TYPE","C","=");
    processCheckField("A","230","ACT_ACNO","STMT_CYCLE","C","=");
    processCheckField("A","240","ACT_ACNO","decode(ACNO_FLAG,'2','Y','1','3',ACNO_FLAG) AS ACNO_FLAG","C","=");
    processCheckField("A","250","ACT_ACNO","decode(AUTOPAY_ACCT_NO,'','N','Y') AS AUTO_PAY","C","=");
    processCheckField("A","260","ACT_ACNO","decode(RC_USE_INDICATOR,'1','Y','N') AS RC_USE","C","=");
    if ( !wp.getValue("VD_CARD").equals("V") ) {
         processCheckField("A","270","ACT_ACNO","decode(COMBO_INDICATOR,'','N','N','N','Y') AS COMBO","C","=");
         processCheckField("A","280","ACT_ACNO","decode(DEPOSIT_FLAG,'','N','N','N','Y') AS DEPOSIT_FLAG","C","=");
         processCheckField("A","290","ACT_ACNO","decode(LOAN_FLAG,'B','N','N','N','Y') AS LOAN_FLAG","C","=");
       }

    processCheckField("A","300","ACT_ACNO","LINE_OF_CREDIT_AMT AS CREDIT_AMT_S","N","<>");
    processCheckField("A","300","ACT_ACNO","LINE_OF_CREDIT_AMT AS CREDIT_AMT_E","N","*");

    processPaymentRate();

    // 帳務條件
    if ( !wp.getValue("VD_CARD").equals("V") ) {
         processCheckField("A","320","ACT_ACCT","TTL_AMT_BAL AS TTL_AMT_BAL_S","N","<>");
         processCheckField("A","320","ACT_ACCT","TTL_AMT_BAL AS TTL_AMT_BAL_E","N","*");
       }

    processCheckField("A","330","MKT_BONUS_HST","NET_BONUS AS NET_BONUS_S","N","<>");
    processCheckField("A","330","MKT_BONUS_HST","NET_BONUS AS NET_BONUS_E","N","*");

    processCheckField("A","340","MKT_FUND_HST","NET_FUND AS NET_FUND_S","N","<>");
    processCheckField("A","340","MKT_FUND_HST","NET_FUND AS NET_FUND_E","N","*");

    // 消費條件
    if ( wp.getValue("PURC_CHECK").equals("Y") ) {
         processCheckField("A","350","BIL_BILL","PURCHASE_DATE AS PURC_DATE_S","N","<>");
         processCheckField("A","350","BIL_BILL","PURCHASE_DATE AS PURC_DATE_E","N","*");
         processCheckField("A","351","BIL_BILL","PURCHASE_DATE AS PURC_DATE_I","","<>");
         processCheckField("A","360","BIL_BILL","ACCT_CODE","C","=");
         processCheckField("A","361","BIL_BILL","MCHT_NO AS EASY_TX","C","*");
         //processCheckField("A","370","BIL_BILL","decode(MCHT_COUNTRY,'TWN','TW','TW','TW','OV') AS MCHT_COUNTRY","C","=");
         processCheckField("A","370","BIL_BILL","decode(MCHT_COUNTRY,'TWN','TW','TW','TW','','TW','OV') AS MCHT_COUNTRY","C","=");	//grace, 20230828
         processCheckField("A","380","BIL_BILL","DECODE(TXN_CODE,'06',DEST_AMT * -1,DEST_AMT) AS SINGLE_AMT_S","N","<>");
         processCheckField("A","380","BIL_BILL","DECODE(TXN_CODE,'06',DEST_AMT * -1,DEST_AMT) AS SINGLE_AMT_E","N","*");
         if ( wp.getValue("CONTRACT_AMT").equals("TOT") )
            { processCheckField("A","385","BIL_BILL","CONTRACT_AMT","C","S"); }
         processCheckField("A","390","DUAL","ACCU_AMT_S","N","<>");
         processCheckField("A","390","DUAL","ACCU_AMT_E","N","*");
         processCheckField("A","390","BIL_BILL","DECODE(TXN_CODE,'06',DEST_AMT * -1,DEST_AMT) AS ACCU_AMT_S","N","S");
         processCheckField("A","400","BIL_BILL","1 AS ACCU_CNT_S","N","S");
         processCheckField("A","400","DUAL","ACCU_CNT_S","N","<>");
         processCheckField("A","400","DUAL","ACCU_CNT_E","N","*");
         processCheckField("A","410","BIL_BILL","2 AS NO_BILL","C","*");
       }

    update_mkt_dm_format();
    clearFunc();
    wp.initFlag="Y";

    Object param1[] = {""};
    wp.selectSQL = "proc_status ";
    wp.daoTable  = "mkt_dm_format";
    wp.whereStr  = "where batch_no = ? and proc_status = 'GE' ";
    param1[0]    = wp.getValue("ex_batch_no");
    pageSelect(param1);
    if ( wp.selectCnt == 0 ) {
         wp.setValue("DIS_DW", "disabled='disabled'", 0);
       }

    wp.notFound = "";

    wp.respCode = "00";
    wp.respMesg = "";
    alertMsg("存檔完成");

    return;
  }

  public void processCheckField (String exclude,String fieldCode,String tableName,String parmField,String objType,String opCode)
              throws Exception {

    String[] cvtField = parmField.split(" AS ");
    String fieldName  = cvtField[0].trim();
    if ( cvtField.length > 1 )
       { fieldName = cvtField[1].trim(); }

    String[] fieldValue = wp.getInBuffer(fieldName);
    String   skip = "Y";
    for( int i=0; i<fieldValue.length; i++) {
         if ( fieldValue[i].length() > 0 && !fieldValue[i].equals("ALL") )
            { skip = "N"; }
       }

    if ( skip.equals("Y") ) // 未設定
       { return; }

    insertMktDmParm(exclude,fieldCode,tableName,parmField,objType,opCode);
    if ( Arrays.asList("S").contains(opCode) )
       { return; }

    for( int i=0; i<fieldValue.length; i++)
       { insertMktDmCheckData(exclude,fieldCode,fieldValue[i]); }
    return;
  }

  public void processPaymentRate() throws Exception {

     if ( wp.getValue("rate_months").length() == 0 || wp.getValue("rate_months_op").equals("X") || wp.getValue("pay_rate").length() == 0 )
        { return; }

     String opCode   = wp.getValue("rate_months_op");
     String fieldName = "PAYMENT_RATE1 AS PAY_RATE";
     processCheckField("A","310","ACT_ACNO",fieldName,"",opCode);
     fieldName = "1 AS RATE_MONTHS";
     processCheckField("A","310","ACT_ACNO",fieldName,"","*");
     return;
  }

  @Override
  public void saveFunc() throws Exception {

    return;
  }

  /* 刪除資料  */
  public void deleteFunc() throws Exception {

    String isSql="";
    String[] delBatch = wp.getInBuffer("del_sel");
    for(int i=0; i<delBatch.length; i++ ) {
        if ( delBatch[i].length() == 0 )
           { break;}
        isSql = "delete mkt_dm_parm where batch_no = :batch_no ";
        setString("batch_no",delBatch[i]);
        sqlExec(isSql);
        isSql = "delete mkt_dm_check_data where batch_no = :batch_no ";
        setString("batch_no",delBatch[i]);
        sqlExec(isSql);
        isSql = "delete mkt_dm_format where batch_no = :batch_no ";
        setString("batch_no",delBatch[i]);
        sqlExec(isSql);
        isSql = "delete mkt_dm_stat where batch_no = :batch_no ";
        setString("batch_no",delBatch[i]);
        sqlExec(isSql);
     }
    queryFunc();
    wp.showLogMessage("D","","deleteFunc ended ");
    return;
  }

  public void callBatch(String parmCode) throws Exception {

     Object param1[] = {""};
     wp.selectSQL = "proc_status ";
     wp.daoTable  = "mkt_dm_format";
     wp.whereStr  = "where batch_no = ? and proc_status in ('1','2') ";
     param1[0]    = wp.getValue("ex_batch_no");
     pageSelect(param1);
     if ( wp.selectCnt > 0 ) {
          if ( wp.getValue("proc_status").equals("1")  )
             { wp.addJSON("RESP_MESG","執行篩選 處理中,勿重覆執行");   }
          else
          if ( wp.getValue("proc_status").equals("2")  )
             { wp.addJSON("RESP_MESG","產生篩選檔 處理中,勿重覆執行"); }
          wp.showLogMessage("I","","callBatch duplicate "+parmCode);
          return;
        }

    wp.showLogMessage("I","","callBatch started "+parmCode);
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    int  rc = batch.callBatch("MktA910 " + wp.getValue("ex_batch_no") + " " + parmCode);
    if ( rc != 1 ) {
         if ( parmCode.equals("S") )
            { wp.addJSON("RESP_MESG","執行篩選 處理 : callbatch 失敗");   }
         else
            { wp.addJSON("RESP_MESG","產生篩選檔 處理 : callbatch 失敗"); }
         return;
      }

    wp.addJSON("RESP_MESG","執行完成");
    wp.showLogMessage("I","","callBatch ended "+parmCode);
    return;
  }


  /* 篩選類別 optionChange */
  public void  typeChange() throws Exception {

     wp.showLogMessage("D","","typeChange started");

     wp.pageRows  = 1000;
     wp.selectSQL = "exclude_code,field_code,field_value";
     wp.daoTable  = "mkt_dm_check_data";
     wp.whereStr  = " where 1=1 "
                  + sqlCol(wp.getValue("ex_batch_no"),"batch_no")
                  + sqlCol(wp.getValue("ex_batch_group"),"batch_group")
                  + sqlCol(wp.getValue("ex_exclude_code"),"exclude_code")
                  + sqlCol(wp.getValue("ex_field_code"),"field_code");
     pageQuery();

     HashMap<String, String> valHash = new HashMap<String, String>();
     for ( int i=0; i < wp.selectCnt; i++ ) {
           String hashKey = wp.getValue("exclude_code")+"#"+wp.getValue("field_code")+"#"+wp.getValue("field_value",i);
           valHash.put(hashKey,"Y");
         }

     String  fieldCode = wp.getValue("ex_field_code",0);
     wp.whereStr = "";
     switch (fieldCode)
       {
         case "610"  : wp.selectSQL  = "card_type as option_value,'['||card_type||']- '||name as option_text";
                       wp.daoTable   = "PTR_CARD_TYPE";
                       wp.whereOrder = "order by card_type";
                       break;
         case "620"  : wp.selectSQL  = "group_code as option_value,'['||group_code||']- '||group_name as option_text";
                       wp.daoTable   = "PTR_GROUP_CODE";
                       wp.whereOrder = "order by group_code";
                       break;
         case "630"  : wp.selectSQL  = "source_code as option_value,source_name as option_text";
                       wp.daoTable   = "ptr_src_code";
                       wp.whereOrder = "order by source_code";
                       break;
         case "640"  : wp.selectSQL  = "bin_no as option_value,card_desc as option_text";
                       wp.daoTable   = "PTR_BINTABLE";
                       wp.whereOrder = "order by bin_no";
                       break;
         case "650"  : wp.selectSQL  = "zip_code as option_value,zip_city||zip_town as option_text";
                       wp.daoTable   = "ptr_zipcode";
                       wp.whereOrder = "order by zip_code";
                       break;
         case "660"  : wp.selectSQL  = "class_code as option_value,'帳戶卡人等級 - '||class_code as option_text";
                       wp.daoTable   = "ptr_class_code2";
                       wp.whereOrder = "group by class_code order by class_code";
                       break;
         case "670"  : wp.selectSQL  = "vip_code as option_value,'VIP 等級 - '||vip_code as option_text";
                       wp.daoTable   = "ptr_vip_code";
                       wp.whereOrder = "order by vip_code";
                       break;
         case "680"  : wp.selectSQL  = "curr_pd_rating as option_value,'違約預測評等 - '||curr_pd_rating as option_text";
                       wp.daoTable   = "act_acno";
                       wp.whereStr   = "where curr_pd_rating != '' group by curr_pd_rating";
                       wp.whereOrder = "order by curr_pd_rating";
                       break;
         case "690"  : wp.selectSQL  = "zip_code as option_value,zip_city||zip_town as option_text";
                       wp.daoTable   = "ptr_zipcode";
                       wp.whereOrder = "order by zip_code";
                       break;
         case "710"  : wp.selectSQL  = "mcc_code as option_value,mcc_code||'-'||mcc_remark as option_text";
                       wp.daoTable   = "cca_mcc_risk";
                       wp.whereOrder = "order by mcc_code";
                       break;
         case "720"  : wp.selectSQL  = "unit_no as option_value,unit_no||'-'||unit_name as option_text";
                       wp.daoTable   = "crd_employee";
                       wp.whereOrder = "group by unit_no,unit_name order by unit_no";
                       break;
         case "730"  : wp.selectSQL  = "unit_no as option_value,unit_no||'-'||unit_name as option_text";
                       wp.daoTable   = "crd_employee_a";
                       wp.whereOrder = "group by unit_no,unit_name order by unit_no";
                       break;
         case "740"  : wp.selectSQL  = "acct_code as option_value,acct_code||'-'||chi_short_name as option_text";
                       wp.daoTable   = "ptr_actcode";
                       wp.whereOrder = "order by acct_code";
                       break;
         case "750"  : wp.selectSQL  = "SPEC_CODE as option_value,SPEC_CODE||'-'||SPEC_DESC as option_text";
                       wp.daoTable   = "CCA_SPEC_CODE";
                       wp.whereOrder = "order by SPEC_CODE";
                       break;
         case "760"  : wp.selectSQL  = "MCHT_GROUP_ID as option_value,MCHT_GROUP_ID||'-'||MCHT_GROUP_DESC as option_text";
                       wp.daoTable   = "MKT_MCHT_GP";
                       wp.whereOrder = "order by MCHT_GROUP_ID";
                       break;
         case "770"  : wp.setValue("option_value","00",0);  wp.setValue("option_text","00-未定義,人工授權",0);
                       wp.setValue("option_value","01",1);  wp.setValue("option_text","01-人工輸入",1);
                       wp.setValue("option_value","02",2);  wp.setValue("option_text","02-讀磁條 (不保證磁條資料完整傳輸)",2);
                       wp.setValue("option_value","05",3);  wp.setValue("option_text","05-讀晶片(ICC read)",3);
                       wp.setValue("option_value","07",4);  wp.setValue("option_text","07-感應交易(Contactless)",4);
                       wp.setValue("option_value","09",5);  wp.setValue("option_text","09-PAN entry EC 限 MasterCard",5);
                       wp.setValue("option_value","10",6);  wp.setValue("option_text","10-未定義",6);
                       wp.setValue("option_value","79",7);  wp.setValue("option_text","79-未定義",7);
                       wp.setValue("option_value","80",8);  wp.setValue("option_text","80-未定義",8);
                       wp.setValue("option_value","81",9);  wp.setValue("option_text","81-Master EC或JCB EC交易",9);
                       wp.setValue("option_value","90",10); wp.setValue("option_text","90-讀磁條 (磁條資料完整傳輸",10);
                       wp.setValue("option_value","91",11); wp.setValue("option_text","91-感應交易(Contactless)",11);
                       wp.setValue("option_value","92",12); wp.setValue("option_text","92-感應交易(Contactless)",12);
                       wp.setValue("option_value","95",13); wp.setValue("option_text","95-感應交易(Contactless)",13);
                       wp.setValue("option_value","97",14); wp.setValue("option_text","97-感應交易(Contactless)",14);
                       wp.setValue("option_value","XX",15); wp.setValue("option_text","無",15);
                       wp.selectCnt = 16;
                       break;
         default    :  break;
       }

     if ( !fieldCode.equals("770") )
        { pageQuery(); }

     for ( int i=0; i < wp.selectCnt; i++ )
         {
           String optionValue =  wp.getValue("option_value",i);
           String optionText  =  wp.getValue("option_text",i);
           String hashKey     =  wp.getValue("exclude_code")+"#"+wp.getValue("ex_field_code")+"#"+optionValue;
           String checkCode   =  (String)valHash.get(hashKey);
           if ( checkCode == null ) {
                wp.addJSON("OPTION_TEXT_L",optionText);
                wp.addJSON("OPTION_VALUE_L",optionValue);
              }
           else {
                wp.addJSON("OPTION_TEXT_R",optionText);
                wp.addJSON("OPTION_VALUE_R",optionValue);
              }
         }
     return;
  }

  /* 篩選類別 確定 */
  public void typeConfirm() throws Exception {

    wp.showLogMessage("D","","typeConfirm started");

    String   exclude   = wp.getValue("ex_exclude_type",0);
    String   fieldCode = wp.getValue("typeListName",0);
    String[] selRight  = wp.getInBuffer("rightListName");

    deleteMktDmParm(exclude,fieldCode);
    deleteMktDmCheckData(exclude,fieldCode);

    if ( selRight.length == 1 && selRight[0].length() == 0 )
       { return; }

    switch (fieldCode)
     {
        case "610" : insertMktDmParm(exclude,fieldCode,"CRD_CARD","CARD_TYPE","","=");          break;
        case "620" : insertMktDmParm(exclude,fieldCode,"CRD_CARD","GROUP_CODE","","=");         break;
        case "630" : insertMktDmParm(exclude,fieldCode,"CRD_CARD","SOURCE_CODE","","=");        break;
        case "640" : insertMktDmParm(exclude,fieldCode,"CRD_CARD","SUBSTR(CARD_NO,1,6) AS BIN_NO","","="); break;
        case "650" : insertMktDmParm(exclude,fieldCode,"CRD_IDNO","RESIDENT_ZIP","","=");       break;
        case "660" : insertMktDmParm(exclude,fieldCode,"ACT_ACNO","CLASS_CODE","","=");         break;
        case "670" : insertMktDmParm(exclude,fieldCode,"ACT_ACNO","VIP_CODE","","=");           break;
        case "680" : insertMktDmParm(exclude,fieldCode,"ACT_ACNO","CURR_PD_RATING","","=");     break;
        case "690" : insertMktDmParm(exclude,fieldCode,"BIL_BILL","MCHT_ZIP","","=");           break;
        case "710" : insertMktDmParm(exclude,fieldCode,"BIL_BILL","MCHT_CATEGORY","","=");      break;
        case "720" : if ( wp.getValue("formatListName").equals("F"))
                        { insertMktDmParm(exclude,fieldCode,"CRD_CARD","INTRODUCE_ID AS BANK_ID","","=");  }
                     else
                        { insertMktDmParm(exclude,fieldCode,"CRD_IDNO","ID_NO AS BANK_ID","","="); }
                                                                                                   break;
        case "730" : if ( wp.getValue("formatListName").equals("F"))
                        { insertMktDmParm(exclude,fieldCode,"CRD_CARD","INTRODUCE_ID AS MEGA_ID","","=");  }
                     else
                        { insertMktDmParm(exclude,fieldCode,"CRD_IDNO","ID_NO AS MEGA_ID","","="); }
                                                                                                   break;
        case "740" : insertMktDmParm(exclude,fieldCode,"BIL_BILL","ACCT_CODE","","=");          break;
        case "750" : insertMktDmParm(exclude,fieldCode,"CCA_CARD_ACCT","BLOCK_REASON1","","="); break;
        case "760" : insertMktDmParm(exclude,fieldCode,"BIL_BILL","MCHT_NO AS MCHT_GP","","="); break;
        case "770" : insertMktDmParm(exclude,fieldCode,"BIL_BILL","POS_ENTRY_MODE","","=");     break;
        default    : break;
     }

    for( int i=0; i<selRight.length; i++ )
       { insertMktDmCheckData(exclude,fieldCode,selRight[i]); }

    return;
  }

  /* 格式類別 formatChange */
  public void  formatChange() throws Exception {

     wp.showLogMessage("D","","formatChange started");

     selectMktDmFormat2(wp.getValue("format_type"));
     if ( sqlNotFind() ) {
          selectMktDmFormat2("X");
       }

     String[] svName = new String[300];
     int colCount = columnCnt;
     for( int i=1; i < colCount; i++ ) {
         svName[i] = colName[i];
      }

     selectSysDb2Columns();

     String[] saveText  = new String[colCount];
     String[] saveValue = new String[colCount];
     int rightCnt=0;
     for( int i=1; i < colCount; i++ )
        {
          String formatValue = wp.getValue("dm."+svName[i],0);
          String optionText  = columnDescription(svName[i]);
          String optionValue = "";
          if ( i <= 9 )
             { optionValue="0"+i+","+svName[i]; }
          else
             { optionValue=""+i+","+svName[i];  }
          if ( formatValue.length() == 0 ) {
              if(!optionText.isEmpty()) {
                   wp.addJSON("OPTION_TEXT_L",optionText);
                   wp.addJSON("OPTION_VALUE_L",optionValue);
              }
             }
          else {
               int k = Integer.parseInt(formatValue);
               saveText[k]  = optionText;
               saveValue[k] = optionValue;
               rightCnt++;
             }
        }

     for( int i=0; i < rightCnt; i++ )  {
          wp.addJSON("OPTION_TEXT_R",saveText[i]);
          wp.addJSON("OPTION_VALUE_R",saveValue[i]);
        }

    return;
  }

  public void  selectMktDmFormat2(String fmtCode) throws Exception {

     wp.pageRows = 1000;
     daoTid = "dm.";

     wp.selectSQL = "chi_name,id_no,ages,birthday,sex,marriage,branch,brief_chi_name,company_name,reg_zip,reg_addr1,"
                  + "job_position,business_code,cellar_phone,office_tel_no1,home_tel_no1,resident_addr1,bill_addr1,e_mail_addr,"
                  + "card_no,issue_date,group_code,bin_type,card_type,current_code,oppost_date,reg_bank_no,"
                  + "activate_date,new_card,promote_new_card,sup_flag,autopay_date,stat_send_internet,apply_line_date,"
                  + "first_purc_date,apply_no,introduce_emp_no,bank_employee,employ_no,unit_no,unit_name,status_id,"
                  + "purchase_date,mcht_country,mcht_category,mcht_chi_name,dest_amt,source_amt,total_amt,year_apply_count";
     wp.daoTable  = "mkt_dm_format ";
     wp.whereStr  = "where 1=1 "
                  + sqlCol("Y","template")
                  + sqlCol(fmtCode,"format_type");
     pageSelect();
     return;
  }

  public void  selectSysDb2Columns() throws Exception {

     HashMap col  = new HashMap();
     wp.selectSQL = "db2_column_name,db2_data_desc";
     wp.daoTable  = "sys_db2_columns";
     wp.whereStr  = "WHERE db2_table_name in ('MKT_DM_FORMAT') ";
     pageSelect();
     if ( sqlNotFind() )
        { return; }
     for( int i=0; i<wp.selectCnt; i++) {
          String colName = "desc."+wp.getValue("db2_column_name",i);
          wp.setValue(colName,wp.getValue("db2_data_desc",i));
        }
     return;
  }

  public String  columnDescription(String parmName) throws Exception {

     String cvtDesc = "";
     switch (parmName)
         {
             default                 : break;
       }

     if ( cvtDesc.length() > 0 )
        { return cvtDesc; }

     return wp.getValue("desc."+parmName);
  }

  /* 格式類別 確定 */
  public void  formatConfirm() throws Exception {

    wp.showLogMessage("D","","formatConfirm started");

    String insertField="",insertValue="",detailCodeB="",detailCodeC="";

    deleteMktDmformat();

    String[] selRight = wp.getInBuffer("OPTION_VALUE_R");

    for( int i=0; i<selRight.length; i++ ) {

         if ( selRight[i].length() == 0 )
            { continue; }
         String fieldName = selRight[i].split(",")[1];
         insertField  = insertField + fieldName+",";
         insertValue  = insertValue + ":"+fieldName +",";
         setString(fieldName,"Y");
         if ( Arrays.asList("PURCHASE_DATE","MCHT_COUNTRY","MCHT_CATEGORY","MCHT_CHI_NAME","DEST_AMT","SOURCE_AMT").contains(fieldName) )
            { detailCodeB="B"; }
         else
         if ( Arrays.asList("CARD_NO","CARD_TYPE","GROUP_CODE","REG_BANK_NO","FIRST_PURC_DATE","ACCT_TYPE").contains(fieldName) )
            { detailCodeC="C"; }
         else
         if ( Arrays.asList("INTRODUCE_EMP_NO","CURRENT_CODE","OPPOST_DATE","ACTIVATE_DATE","TOTAL_AMT","NEW_CARD","PROMOTE_NEW_CARD","SUP_FLAG","APPLY_NO").contains(fieldName) )
            { detailCodeC="C"; }
          else
         if ( Arrays.asList("ISSUE_DATE","BIN_TYPE").contains(fieldName) )
            { detailCodeC="C"; }
       }

    if ( detailCodeB.equals("B") )
       { detailCode = "B"; }
    else
    if ( detailCodeC.equals("C") )
       { detailCode = "C"; }
    else
       { detailCode = "I"; }

    setString("template","");
    insertMktDmFormat(insertField,insertValue);


    int k=0;
    for( int i=0; i<selRight.length; i++ ) {
         if ( selRight[i].length() == 0 )
            { continue; }
         String fieldName = selRight[i].split(",")[1];
         setString(fieldName,""+k);
         k++;
       }

    wp.setValue("template","Y");
    setString("template","Y");
    insertMktDmFormat(insertField,insertValue);

    wp.addJSON("RESP_MESG","格式設定完成");
    return;
  }

  /* 資料匯入 */
  public void importData() throws Exception {

    wp.showLogMessage("D","","importData started");

    String   exclude   = wp.getValue("ex_exclude_import",0);
    String   impField  = wp.getValue("ex_field_code",0);
    String[] cvtField  = impField.split(",");
    String[] fieldCode = {"","","","","","",""};

    for( int k=0; k<cvtField.length; k++ ) {
         switch (cvtField[k])
           {
             case "ID_NO"       : fieldCode[k] = convertFieldName(cvtField[k]);
                                  deleteMktDmParm(exclude,fieldCode[k]);
                                  deleteMktDmCheckData(exclude,fieldCode[k]);
                                  insertMktDmParm(exclude,fieldCode[k],"CRD_IDNO",cvtField[k],"","="); break;
             case "BUSINESS_ID" : fieldCode[k] = convertFieldName(cvtField[k]);
                                  deleteMktDmParm(exclude,fieldCode[k]);
                                  deleteMktDmCheckData(exclude,fieldCode[k]);
                                  insertMktDmParm(exclude,fieldCode[k],"CRD_IDNO",cvtField[k],"","="); break;
             case "P_SEQNO"     : fieldCode[k] = convertFieldName(cvtField[k]);
                                  deleteMktDmParm(exclude,fieldCode[k]);
                                  deleteMktDmCheckData(exclude,fieldCode[k]);
                                  insertMktDmParm(exclude,fieldCode[k],"CRD_CARD",cvtField[k],"","="); break;
             case "MCHT_NO"     : fieldCode[k] = convertFieldName(cvtField[k]);
                                  deleteMktDmParm(exclude,fieldCode[k]);
                                  deleteMktDmCheckData(exclude,fieldCode[k]);
                                  insertMktDmParm(exclude,fieldCode[k],"BIL_BILL",cvtField[k],"","="); break;
             default            : break;
         }
     }

    String   fileName = TarokoParm.getInstance().getDataRoot() + "/upload/" + wp.getValue("zz_file_name",0);
    int cnt=0;
    try(BufferedReader dr = new BufferedReader( new FileReader(fileName) );) {
        String fileData="";
        while ( dr.ready() )  {
             fileData = dr.readLine().trim();
             String[] impData  = fileData.split(",");
             for( int k=0;k<impData.length; k++ ) {
                  insertMktDmCheckData(exclude,fieldCode[k],impData[k]);
                }
             cnt++;
           }
//    dr.close();
    }

    wp.addJSON("RESP_MESG","匯入筆數 : "+cnt);
    return;

  }

  public void deleteImport() throws Exception {

    wp.showLogMessage("D","","deleteImport started");

    String   exclude   = wp.getValue("ex_exclude_import");
    String   impField  = wp.getValue("ex_field_code");
    String[] cvtField  = impField.split(",");
    for( int k=0; k<cvtField.length; k++ ) {
         String   fieldCode = convertFieldName(cvtField[k]);
         deleteMktDmParm(exclude,fieldCode);
         deleteMktDmCheckData(exclude,fieldCode);
      }
    return;
  }

  public String convertFieldName(String parmField) throws Exception {

      String fieldCode="";
      switch (parmField)
           {
             case "ID_NO"       : fieldCode = "810"; break;
             case "BUSINESS_ID" : fieldCode = "820"; break;
             case "P_SEQNO"     : fieldCode = "830"; break;
             case "MCHT_NO"     : fieldCode = "840"; break;
             default            : break;
         }
      return fieldCode;
   }

  public void update_mkt_dm_format() throws Exception {

      String batchGroup = wp.getValue("ex_batch_group");
      String mergSql = "";
      switch (batchGroup)
         {
            case "A"  : mergSql = "merg_code_a = 'U',";        break;
            case "B"  : mergSql = "merg_code_b = :merg_code,"; break;
            case "C"  : mergSql = "merg_code_c = :merg_code,"; break;
            case "D"  : mergSql = "merg_code_d = :merg_code,"; break;
            case "E"  : mergSql = "merg_code_e = :merg_code,"; break;
            default   : break;
         }

      String is_sql="";
      is_sql = "update mkt_dm_format set "
             + mergSql
             + "file_mode   = :file_mode,"
             + "batch_desc  = :batch_desc,"
             + "active_date_s  = :active_date_s,"
             + "active_date_e  = :active_date_e "
             + "where batch_no = :ex_batch_no";

      if ( !batchGroup.equals("A") )
         { item2ParmStr("merg_code"); }

      item2ParmStr("file_mode");
      item2ParmStr("batch_desc");
      item2ParmStr("active_date_s");
      item2ParmStr("active_date_e");
      item2ParmStr("ex_batch_no");
      sqlExec(is_sql);
      wp.showLogMessage("D","","update_mkt_dm_parm "+sqlRowNum);
      if ( sqlRowNum == 0 )
         { insertMktDmFormat("",""); }
      return;
  }

  /* 匯入資料查詢 */
  public void importQuery() throws Exception {

     wp.pageRows= 1000;

     String   impField  = wp.getValue("ex_field_code");
     String[] cvtField  = impField.split(",");
     String   impDesc   = wp.getValue("import_field_code");
     String[] cvtDesc   = impDesc.split(",");
     int selCount = 0;
     for( int k=0; k<cvtField.length; k++ ) {

          String   fieldCode = convertFieldName(cvtField[k]);
          wp.selectSQL = "field_code,field_value";
          wp.daoTable  = "mkt_dm_check_data";
          wp.whereStr  = " where 1=1 "
                       + sqlCol(wp.getValue("ex_batch_no"),"batch_no")
                       + sqlCol(wp.getValue("ex_batch_group"),"batch_group")
                       + sqlCol(wp.getValue("ex_exclude_import"),"exclude_code")
                       + sqlCol(fieldCode,"field_code")
                       + sqlCol(wp.getValue("ex_query_data"),"field_value","like%");
          pageSelect();
          selCount += wp.selectCnt;
          for( int i=0; i < wp.selectCnt; i++ ) {
               wp.addJSON("IMPORT_DESC",cvtDesc[k]);
               wp.addJSON("IMPORT_DATA",wp.getValue("field_value",i));
             }

       }

     if ( selCount == 0 )
        { wp.addJSON("PROC_CODE","01"); }
     else
        { wp.addJSON("PROC_CODE","00"); }
     return;
  }


  /* 顯示 已選篩選項目 */
  public void selTypeQuery() throws Exception {

     wp.selectSQL = "exclude_code,field_code";
     wp.daoTable  = "mkt_dm_check_data";
     wp.whereStr  = " where 1=1 "
                  + sqlCol(wp.getValue("ex_batch_no"),"batch_no")
                  + sqlCol(wp.getValue("ex_batch_group"),"batch_group")
                  + " and field_code >= '610' and field_code <= '770' "
                  + " group by exclude_code,field_code order by exclude_code,field_code";
     pageSelect();
     for( int i=0; i < wp.selectCnt; i++ ) {
          if ( wp.getValue("exclude_code",i).equals("A") )
             { wp.addJSON("DISP_TYPE_1","指定"); }
          else
             { wp.addJSON("DISP_TYPE_1","排除"); }
          String type_desc =  convertFieldCode(wp.getValue("field_code",i));
          wp.addJSON("DISP_TYPE_2",type_desc);
        }

     return;

   }

  /* 顯示 已匯入項目 */
  public void importDisplay() throws Exception {

     wp.selectSQL = "exclude_code,field_code";
     wp.daoTable  = "mkt_dm_check_data";
     wp.whereStr  = " where 1=1 "
                  + sqlCol(wp.getValue("ex_batch_no"),"batch_no")
                  + sqlCol(wp.getValue("ex_batch_group"),"batch_group")
                  + " and field_code >= '810' and field_code <= '840' "
                  + " group by exclude_code,field_code order by exclude_code,field_code";
     pageSelect();
     for( int i=0; i < wp.selectCnt; i++ ) {
          if ( wp.getValue("exclude_code",i).equals("A") )
             { wp.addJSON("DISP_IMP_1","指定"); }
          else
             { wp.addJSON("DISP_IMP_1","排除"); }
          String type_desc =  convertFieldCode(wp.getValue("field_code",i));
          wp.addJSON("DISP_IMP_2",type_desc);
        }
     return;
   }

  /* 篩選分析查詢 */
  public void statsQuery() throws Exception {

     wp.selectSQL = "proc_status";
     wp.daoTable  = "mkt_dm_format";
     wp.whereStr  = " where 1=1 "
                  + sqlCol(wp.getValue("ex_batch_no"),"batch_no");
     pageQuery();
     String proc_status = wp.getValue("proc_status");

     wp.pageRows = 1000;

     wp.selectSQL = "batch_group as batch_group2,field_code,check_count,pass_count";
     wp.daoTable  = "mkt_dm_stat";
     wp.whereStr  = " where 1=1 "
                  + sqlCol(wp.getValue("ex_batch_no"),"batch_no");
     wp.whereOrder = " order by batch_group,sort_code,field_code";
     pageSelect();
     for( int i=0; i < wp.selectCnt; i++ )
        {
          String fieldCode  =  wp.getValue("field_code",i);
          String statsItem =  convertFieldCode(fieldCode);

          statsItem = "( "+wp.getValue("batch_group2",i)+" ) "+statsItem;
          wp.addJSON("BATCH_GROUP",wp.getValue("batch_group2",i));
          wp.addJSON("STATS_ITEM",statsItem);
          wp.addJSON("CHECK_COUNT",wp.getValue("check_count",i));
          wp.addJSON("PASS_COUNT",wp.getValue("pass_count",i));
        }

     if ( Arrays.asList("1","2","X","SE","GE").contains(proc_status) )
        { wp.addJSON("PROC_CODE",proc_status); }
     else
     if ( wp.selectCnt == 0 )
        { wp.addJSON("PROC_CODE","9"); }
     else
        { wp.addJSON("PROC_CODE","00"); }
     return;
  }

  public String convertFieldCode(String fieldCode) throws Exception {

      String statsItem="";
      switch (fieldCode)
           {
              case "000"  : statsItem="關帳週期";       break;
              case "010"  : statsItem="卡別";           break;
              case "020"  : statsItem="DEBIT卡";        break;
              case "030"  : statsItem="正附卡";         break;
              case "040"  : statsItem="卡片幣別";       break;
              case "050"  : statsItem="卡片等級";       break;
              case "060"  : statsItem="電子票證";       break;
              case "070"  : statsItem="卡友選項";       break;
              case "075"  : statsItem="開卡選項";       break;
              case "080"  : statsItem="發卡期間";       break;
              case "085"  : statsItem="進件期間";       break;
              case "090"  : statsItem="TPAN 下載日";    break;
              case "091"  : statsItem="TPAN 下載日";    break;
              case "120"  : statsItem="JCIC 徵審評分";  break;
              case "130"  : statsItem="性別";           break;
              case "140"  : statsItem="學歷";           break;
              case "150"  : statsItem="生日月分限制";   break;
              case "160"  : statsItem="國籍";           break;
              case "170"  : statsItem="婚姻狀態";       break;
              case "180"  : statsItem="身份別";         break;
              case "190"  : statsItem="行銷選項";       break;
              case "210"  : statsItem="年齡區間";       break;
              case "211"  : statsItem="新舊卡友";       break;
              case "220"  : statsItem="帳戶類別";       break;
              case "240"  : statsItem="繳款類別";       break;
              case "250"  : statsItem="自動扣繳戶";     break;
              case "260"  : statsItem="允用循環息";     break;
              case "270"  : statsItem="COMBO卡";        break;
              case "280"  : statsItem="存款戶";         break;
              case "290"  : statsItem="授信戶";         break;
              case "300"  : statsItem="循環信用額度";   break;
              case "310"  : statsItem="信用狀況";       break;
              case "320"  : statsItem="總欠款餘額";     break;
              case "330"  : statsItem="紅積點數結餘";   break;
              case "340"  : statsItem="基金結餘";       break;
              case "380"  : statsItem="單筆消費金額";   break;
              case "390"  : statsItem="累積消費金額";   break;
              case "400"  : statsItem="累積消費筆數";   break;
              case "610"  : statsItem="卡片種類";       break;
              case "620"  : statsItem="團體代號";       break;
              case "630"  : statsItem="來源代號";       break;
              case "640"  : statsItem="CARD BIN";       break;
              case "650"  : statsItem="郵遞區號";       break;
              case "660"  : statsItem="帳戶卡人等級";   break;
              case "670"  : statsItem="VIP等級";        break;
              case "680"  : statsItem="違約預測評等";   break;
              case "690"  : statsItem="消費地區";       break;
              case "710"  : statsItem="特店類別(MCC)";  break;
              case "720"  : statsItem="兆豐銀行-(分行)";   break;
              case "730"  : statsItem="兆豐金控-(子公司)"; break;
              case "740"  : statsItem="帳單科目";       break;
              case "750"  : statsItem="凍結原因";       break;
              case "760"  : statsItem="特店群組";       break;
              case "770"  : statsItem="刷卡模式(POS_ENTRY)"; break;
              case "810"  : statsItem="身分證字號";     break;
              case "820"  : statsItem="公司統一編號";   break;
              case "830"  : statsItem="P_SEQNO";        break;
              case "840"  : statsItem="特店代號";       break;
              case "991"  : statsItem="總共篩選帳單";   break;
              case "992"  : statsItem="總共篩選卡片";   break;
              case "993"  : statsItem="總共篩選 ID";    break;
              default     : statsItem=fieldCode;        break;
           }

     return statsItem;
  }

  public void insertMktDmParm(String exclude,String fieldCode,String tableName,String fieldName,String objType,String opCode)
              throws Exception {

    String sortCode="";
    switch (tableName) // TABLE 查核順序
     {
       case "ACT_ACNO"      : sortCode="010";  break;
       case "ACT_ACCT"      : sortCode="020";  break;
       case "MKT_BONUS_HST" : sortCode="030";  break;
       case "MKT_FUND_HST"  : sortCode="040";  break;
       case "CRD_IDNO"      : sortCode="810";  break;
       case "HCE_CARD_LOG"  : sortCode="820";  break;
       case "CRD_CARD"      : sortCode="830";  break;
       case "BIL_BILL"      : sortCode="840";  break;
       case "DUAL"          : sortCode="990";  break;
       default              : sortCode="000";  break;
     }

    if ( wp.getValue("VD_CARD").equals("V") )
       { visaDebit = true; }

     if ( visaDebit ) {  // visa debit
          switch ( tableName ) {
                 case "ACT_ACNO" : tableName="DBA_ACNO";  break;
                 case "CRD_IDNO" : tableName="DBC_IDNO";  break;
                 case "CRD_CARD" : tableName="DBC_CARD";  break;
                 case "BIL_BILL" : tableName="DBB_BILL";  break;
                 default         : break;
          }
     }

     String isSql="";
     isSql = "insert into mkt_dm_parm ( "
            + "batch_no,"
            + "batch_group,"
            + "sort_code,"
            + "table_name,"
            + "exclude_code,"
            + "seq_no,"
            + "field_code,"
            + "field_name,"
            + "operator,"
            + "group_check,"
            + "obj_type"
            + " ) values ( "
            + ":batch_no,"
            + ":batch_group,"
            + ":sort_code,"
            + ":table_name,"
            + ":exclude_code,"
            + ":seq_no,"
            + ":field_code,"
            + ":field_name,"
            + ":operator,"
            + ":group_check,"
            + ":obj_type"
            + " ) ";

      parmSeq++;
      setString("batch_no",wp.getValue("ex_batch_no"));
      setString("batch_group",wp.getValue("ex_batch_group"));
      setString("sort_code",sortCode);
      setString("table_name",tableName);
      setString("field_code",fieldCode);
      setString("field_name",fieldName);
      setString("operator",opCode);
      setString("exclude_code",exclude);
      setString("seq_no",""+parmSeq);
      setString("group_check",groupCheck);
      setString("obj_type",objType);
      sqlExec(isSql);
      //wp.showLogMessage("D","","insert_mkt_dm_parm "+sql_nrow);
      return;
  }

  public void insertMktDmCheckData(String exclude,String fieldCode,String fieldValue) throws Exception {

     if ( fieldValue.length() == 0 || fieldValue.equals("ALL") )
        { return; }

     String isSql="";
     isSql = " insert into mkt_dm_check_data ( "
            + " batch_no ,"
            + " batch_group ,"
            + " exclude_code ,"
            + " seq_no ,"
            + " field_code ,"
            + " field_value "
            + " ) values ( "
            + " :batch_no ,"
            + " :batch_group ,"
            + " :exclude_code ,"
            + " :seq_no ,"
            + " :field_code ,"
            + " :field_value "
            + " ) ";

      setString("batch_no",wp.getValue("ex_batch_no"));
      setString("batch_group",wp.getValue("ex_batch_group"));
      setString("exclude_code",exclude);
      setString("field_code",fieldCode);
      setString("seq_no",""+(seqNo++));
      setString("field_value",fieldValue);
      sqlExec(isSql);
      return;
  }

  public void insertMktDmFormat(String insertField,String insertValue)  throws Exception {

     String isSql="";
     isSql = " insert into mkt_dm_format ( "
            + "batch_no,"
            + "template,"
            + "batch_desc,"
            + "file_mode,"
            + "format_type,"
            + "merg_code_a,"
            + "detail_format,"
            + "active_date_s,"
            + "active_date_e,"
            + insertField
            + "crt_date,"
            + "crt_user "
            + " ) values ( "
            + ":ex_batch_no,"
            + ":template,"
            + ":batch_desc,"
            + ":file_mode,"
            + ":format_type,"
            + ":merg_code_a,"
            + ":detail_format,"
            + ":active_date_s,"
            + ":active_date_e,"
            + insertValue
            + ":crt_date,"
            + ":crt_user ) ";

      if ( wp.getValue("template").equals("Y") ) {
           setString("ex_batch_no","999"+wp.sysTime);
           setString("batch_desc","");
           setString("template","Y");
         }
      else {
           item2ParmStr("ex_batch_no");
           item2ParmStr("batch_desc");
           setString("template","");
         }
      item2ParmStr("file_mode");
      if ( wp.getValue("format_type").length() == 0 ) {
           setString("format_type","0");
         } else {
           setString("format_type",wp.getValue("format_type"));
         }
      setString("merg_code_a","U");
      setString("detail_format",detailCode);
      setString("active_date_s",wp.getValue("active_date_s"));
      setString("active_date_e",wp.getValue("active_date_e"));

      setString("crt_date",wp.sysDate);
      setString("crt_user",wp.loginUser);
      sqlExec(isSql);
      wp.showLogMessage("D","","insert_mkt_dm_format "+sqlRowNum);
      return;
  }

  public void deleteBatchParm() throws Exception {

      String isSql="";
      isSql = "delete mkt_dm_parm "
             + "where batch_no = :ex_batch_no    and "
             + "batch_group    = :ex_batch_group and "
             + "field_code < '600' ";

      item2ParmStr("ex_batch_no");
      item2ParmStr("ex_batch_group");
      sqlExec(isSql);
      wp.showLogMessage("D","","delete_mkt_dm_parm "+sqlRowNum);

      isSql = "delete mkt_dm_check_data "
             + "where batch_no = :ex_batch_no    and "
             + "batch_group    = :ex_batch_group and "
             + "field_code < '600' ";

      item2ParmStr("ex_batch_no");
      item2ParmStr("ex_batch_group");
      sqlExec(isSql);
      wp.showLogMessage("D","","delete_mkt_dm_check_data "+sqlRowNum);
      return;
  }

  public void deleteMktDmformat() throws Exception {

      String isSql="";

      isSql = "delete mkt_dm_format "
             + "where template = 'Y' and format_type = :format_type ";
      setString("format_type",wp.getValue("format_type"));
      sqlExec(isSql);
      wp.showLogMessage("D","","delete_mkt_dm_format template "+sqlRowNum);

      isSql = "delete mkt_dm_format "
             + "where batch_no = :ex_batch_no ";
      item2ParmStr("ex_batch_no");
      sqlExec(isSql);
      wp.showLogMessage("D","","delete_mkt_dm_format "+sqlRowNum);
      return;
  }

  public void deleteMktDmParm(String exclude,String fieldCode) throws Exception {

      String isSql="";
      isSql = "delete mkt_dm_parm "
             + "where batch_no = :ex_batch_no and "
             + "batch_group    = :ex_batch_group and "
             + "exclude_code   = :exclude_code and "
             + "field_code     = :field_code";
      item2ParmStr("ex_batch_no");
      item2ParmStr("ex_batch_group");
      setString("exclude_code",exclude);
      setString("field_code",fieldCode);
      sqlExec(isSql);
      wp.showLogMessage("D","","delete_mkt_dm_parm "+sqlRowNum);
      return;
  }

  public void deleteMktDmCheckData(String exclude,String fieldCode) throws Exception {

      String isSql="";
      isSql = "delete mkt_dm_check_data "
             + "where batch_no = :ex_batch_no and "
             + "batch_group    = :ex_batch_group and "
             + "exclude_code   = :exclude_code and "
             + "field_code     = :field_code";

      item2ParmStr("ex_batch_no");
      item2ParmStr("ex_batch_group");
      setString("exclude_code",exclude);
      setString("field_code",fieldCode);
      sqlExec(isSql);
      wp.showLogMessage("D","","delete_mkt_dm_check_data "+sqlRowNum);
      return;
  }

  /* 下載篩選名單 */
  public void  downLoad() throws Exception {

    String msg="";
    String batchNo  = wp.getValue("ex_batch_no");
    String fileMode = wp.getValue("file_mode");
    String remoteName = "";
    if ( fileMode.equals("T") )
       { remoteName = "DM_LIST_"+batchNo+".txt"; }
     else
       { remoteName = "DM_LIST_"+batchNo+".csv"; }

    wp.showLogMessage("I","","downloadLogFile ftp start "+remoteName);
    try {
          taroko.com.TarokoFTP ftp = new taroko.com.TarokoFTP();

          ftp.localPath = TarokoParm.getInstance().getRootDir() + "/WebData/work"; // TarokoParm.getInstance().getRootDir()
          if ( wp.request.getServerName().substring(0,4).equals("10.1") )
             { ftp.setRemotePath("/ECS/sit/media/dm");  }
          else
          if ( wp.request.getServerName().equals("10.5.109.2") )
             { ftp.setRemotePath("/ECS/ecs2/media/dm"); }
          else
             { ftp.setRemotePath("/ECS/ecs/media/dm"); }

          //ftp.set_remotePath2("media/dm");
          ftp.fileName = remoteName;
          ftp.ftpMode = "BIN";
          if (ftp.getFile(wp) != 0) {
            alertErr("下載檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
            return;
          }
          msg = ftp.getMesg();
    } catch (Exception ex) {
         msg = ex.getMessage();
         alertErr("下載檔案失敗: ,"+msg);
         return;
    }
    alertMsg("下載檔案完成"+msg);
    wp.showLogMessage("I","","downloadLogFile ftp ended "+remoteName);

    wp.setDownload2(remoteName);
    return;
  }

} // end of class
