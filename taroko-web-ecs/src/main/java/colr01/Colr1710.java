package colr01;
/**
 * 109-06-04  V1.00.00  Tanwei       updated for project coding standard
 *110-01-05  V1.00.01  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
 *112-04-28  V1.00.02  Zuwei Su     產生之PDF與勾選資料不符         *
 *112-06-09  V1.00.03  Ryan     修正產生之PDF與勾選資料不符         *
 *112-06-23  V1.00.04  Sunny     調整商務卡公司產生pdf的內容         *
* */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Colr1710 extends BaseAction implements InfacePdf {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  String lsWhere = "", wkTel = "", wkUser = "", wkPhone = "", wkSysDate = "", chargeNameFlag = "" ;

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub
	  try {
		  wp.optionKey = wp.colStr(0, "ex_acct_type");
	      dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1 and no_collection_flag<>'Y' ");
		 
		  } catch (Exception ex) {
		  }
  }

  @Override
  public void queryFunc() throws Exception {
    chargeNameFlag = wp.itemStr("chargeNameFlag");
    if("Y".equals(chargeNameFlag)) {
      wp.colSet("DEFAULT_CHK", "checked");
    }
    if (itemallEmpty("ex_trans_date1,ex_trans_date2,ex_idno")) {
      alertErr2("[作業日期, 身分證ID] 不可同時空白");
      return;
    }


    if (this.chkStrend(wp.itemStr("ex_trans_date1"), wp.itemStr("ex_trans_date2")) == false) {
      alertErr2("作業日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = 
        " where col_bad_debt.trans_type='3' "
            + " and act_acno.acno_flag<> 'Y' "
        	+ " and ptr_acct_type.no_collection_flag<>'Y' "
            + sqlCol(wp.itemStr("ex_acct_type"), "act_acno.acct_type")
            + sqlCol(wp.itemStr("ex_trans_date1"), "col_bad_debt.trans_date", ">=")
            + sqlCol(wp.itemStr("ex_trans_date2"), "col_bad_debt.trans_date", "<=");

    if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += " and act_acno.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
          + sqlCol(wp.itemStr("ex_idno"), "id_no") + ")";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  /* 20230621 backup
  @Override
  public void queryRead_bk() throws Exception {
    wp.pageControl(); //crd_idno.id_no
    wp.selectSQL = " act_acno.p_seqno , " + " crd_idno.id_no,act_acno.acct_key," + " act_acno.acct_type , "
        + " uf_acno_name(act_acno.ACNO_P_SEQNO) AS chi_name , "
        + " decode(acno_flag,'2',decode("+"'" + chargeNameFlag + "'"+",'Y',crd_corp.charge_name,uf_acno_name(act_acno.ACNO_P_SEQNO)) , "
        + " uf_acno_name(act_acno.ACNO_P_SEQNO)) AS charge_name , "
//        + " act_acno.bill_sending_zip , " + " act_acno.bill_sending_addr1 || "
//        + " act_acno.bill_sending_addr2 || bill_sending_addr3|| bill_sending_addr4 || bill_sending_addr5 AS  bill_sending_addr , "
		+ " crd_idno.resident_zip,crd_idno.resident_addr1 || "
		+ " crd_idno.resident_addr2 || crd_idno.resident_addr3 || crd_idno.resident_addr4 || crd_idno.resident_addr5 AS resident_addr, "
		+ " col_bad_debt.trans_date , " + " col_bad_debt.stmt_cycle , " + " col_bad_debt.src_amt , " + " act_acno.acno_flag ";
    wp.daoTable = " act_acno" 
        + " left join crd_corp on act_acno.corp_p_seqno=crd_corp.corp_p_seqno " 
        + " left join col_bad_debt on act_acno.p_seqno=col_bad_debt.p_seqno "
        + " left join crd_idno on crd_idno.id_p_seqno=act_acno.id_p_seqno";
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();
    queryAfter();
  }
  */
  
  @Override
  public void queryRead() throws Exception {
    wp.pageControl(); //crd_idno.id_no
    wp.selectSQL = " act_acno.p_seqno , " + " crd_corp.corp_no,decode(acno_flag,'2',crd_corp.corp_no,crd_idno.id_no) id_no,"
        + " act_acno.acct_key," + " act_acno.acct_type , "
        + " decode(act_acno.acno_flag,'2',crd_corp.chi_name,crd_idno.chi_name) AS chi_name, "
//        + " decode(act_acno.acno_flag,'2','','先生/小姐 收')  AS letter_title, "
        + " CASE WHEN act_acno.acno_flag<>'2' or decode( "+"'" + chargeNameFlag + "'"+",'Y','Y','N')='Y' "
        + " THEN '先生/小姐 收' ELSE '' END AS letter_title, "
        //+ " CASE WHEN act_acno.acno_flag<>'2' AND "+"'" + chargeNameFlag + "'"+"='Y' "
        + " CASE WHEN act_acno.acno_flag<>'2' or decode( "+"'" + chargeNameFlag + "'"+",'Y','Y','N')='Y' "
        + " THEN '先生/小姐' ELSE '' END AS letter_title2, "
		+ " CASE WHEN act_acno.acno_flag=2 THEN decode( "+"'" + chargeNameFlag + "'"+",'Y',crd_corp.charge_name,crd_corp.chi_name) "
        + " ELSE crd_idno.chi_name END AS charge_name, "
//        + " act_acno.bill_sending_zip , " + " act_acno.bill_sending_addr1 || "
//        + " act_acno.bill_sending_addr2 || bill_sending_addr3|| bill_sending_addr4 || bill_sending_addr5 AS  bill_sending_addr , "
		+ " decode(act_acno.acno_flag,'2',crd_corp.reg_zip,crd_idno.resident_zip) AS resident_zip, "
		+ " decode(act_acno.acno_flag,'2',crd_corp.REG_ADDR1||REG_ADDR2||REG_ADDR3||REG_ADDR4||REG_ADDR5,"
		+ " crd_idno.resident_addr1 ||crd_idno.resident_addr2 || crd_idno.resident_addr3 || crd_idno.resident_addr4 || crd_idno.resident_addr5 )AS resident_addr, "
		+ " col_bad_debt.trans_date , " + " col_bad_debt.stmt_cycle , act_acno.acno_flag ,"
		//+ " col_bad_debt.src_amt , " + " act_acno.acno_flag ";
		+ " CASE WHEN act_acno.acno_flag=2 THEN (SELECT sum(src_amt) FROM col_bad_debt "
		+ " WHERE P_SEQNO IN (SELECT p_seqno FROM act_acno WHERE CORP_P_SEQNO=crd_corp.corp_p_seqno AND acct_type=act_acno.acct_type)) "
		+ " ELSE col_bad_debt.src_amt END AS src_amt ";
    wp.daoTable = " act_acno" 
        + " left join crd_corp on act_acno.corp_p_seqno=crd_corp.corp_p_seqno " 
        + " left join col_bad_debt on act_acno.p_seqno=col_bad_debt.p_seqno "
        + " left join crd_idno on crd_idno.id_p_seqno=act_acno.id_p_seqno "
        + " left join ptr_acct_type on ptr_acct_type.acct_type=act_acno.acct_type ";
    
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();
    queryAfter();
  }

  void queryAfter() {
    int rr = 0;
    rr = wp.selectCnt;
    for (int ii = 0; ii < rr; ii++) {
      int transDateInt = Integer.parseInt(wp.colStr(ii, "trans_date").substring(6, 8));
      String stmtCycle = wp.colStr(ii, "stmt_cycle");
      int stmtCycleInt = Integer.parseInt(wp.colStr(ii, "stmt_cycle"));
      String transDateNew = "";
      if(stmtCycleInt > transDateInt) {
        // 獲取月份
        String transDateIntMonth = wp.colStr(ii, "trans_date").substring(4, 6);
        if("01".equals(transDateIntMonth)) {
          // 儅月份為01的時候  減一的情況下 先將月份改為12  
          transDateIntMonth = "12";
          // 獲取年份  年份減一
          int transDateIntYear = Integer.parseInt(wp.colStr(ii, "trans_date").substring(0, 4)) - 1;
          transDateNew = transDateIntYear + transDateIntMonth + stmtCycle;
        }else {
          int transDate = Integer.parseInt(wp.colStr(ii, "trans_date").substring(0, 6)) -1;
          transDateNew = String.valueOf(transDate) + stmtCycle;
        }
        wp.colSet(ii, "transDateNew",getTwDateString(transDateNew));
        wp.colSet(ii, "transDateNewTwo",getTwDateStringTwo(transDateNew));
      }
      if(stmtCycleInt <= transDateInt) {
        transDateNew = wp.colStr(ii, "trans_date").substring(0, 6) + stmtCycle;
        wp.colSet(ii, "transDateNew",getTwDateString(transDateNew));
        wp.colSet(ii, "transDateNewTwo",getTwDateStringTwo(transDateNew));
      }
      String transDate = wp.colStr(ii, "trans_date");
      wp.colSet(ii, "trans_date",getTwDateString(transDate));
      wp.colSet(ii, "transDateTwo",getTwDateStringTwo(transDate));
    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    wp.colSet("ex_tel", "610");
    wp.colSet("ex_user", "催收經辦");
  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "colr1710";
    dataPrint();
    if (wp.itemBuff("opt").length == 0) {
      alertErr("請選擇要列印資料");
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    TarokoPDF pdf = new TarokoPDF();
    pdf.pageVert = false;

    wp.fileMode = "Y";
    pdf.excelTemplate = "colr1710.xlsx";
    pdf.pageCount = 1;
    pdf.sheetNo = 0;
    pdf.pageVert = true;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

  void dataPrint() {
    selectUser();
//    wkSysDate = getTwDate();

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsAcctType = wp.itemBuff("acct_type");
    String[] lsChiName = wp.itemBuff("chi_name");
    String[] lsChargeName = wp.itemBuff("charge_name");
    String[] lsChargeName2 = wp.itemBuff("charge_name");
    String[] lsLetterTitle = wp.itemBuff("letter_title"); //信封稱謂
    String[] lsLetterTitle2 = wp.itemBuff("letter_title2"); //書信內文稱謂
//    String[] lsBillSendingZip = wp.itemBuff("bill_sending_zip");
//    String[] lsBillSendingAddr = wp.itemBuff("bill_sending_addr");
    String[] lsResidentZip = wp.itemBuff("resident_zip");
    String[] lsResidentAddr = wp.itemBuff("resident_addr");
    String[] lsTransDate = wp.itemBuff("transDateTwo");
    String[] lsTransDateNew = wp.itemBuff("transDateNewTwo");
    String[] lsSrcAmt = wp.itemBuff("src_amt");
    int rr = 0;
    for (int i = 0; i < lsAcctType.length; i++) {
        if(this.checkBoxOptOn(i, aaOpt)==false)
        	continue;
          
          wp.colSet(rr, "ex_acct_type", lsAcctType[i]);
          wp.colSet(rr, "ex_chi_name", lsChiName[i]);
          wp.colSet(rr, "ex_charge_name", lsChargeName[i]+" "+lsLetterTitle[i]);
          wp.colSet(rr, "ex_charge_name2", lsChargeName2[i]+" "+lsLetterTitle2[i]);
          wp.colSet(rr, "ex_letter_title", lsLetterTitle[i]);
          wp.colSet(rr, "ex_letter_title2", lsLetterTitle2[i]);
//          wp.colSet(rr, "ex_bill_sending_zip", lsBillSendingZip[i]);
//          wp.colSet(rr, "ex_bill_sending_addr", lsBillSendingAddr[i]);
          wp.colSet(rr, "ex_resident_zip", lsResidentZip[i]);
          wp.colSet(rr, "ex_resident_addr",lsResidentAddr[i]);
          wp.colSet(rr, "ex_trans_date", lsTransDate[i].replaceAll(" ", ""));
          wp.colSet(rr, "ex_trans_date_new", lsTransDateNew[i].replaceAll(" ", ""));
          String exTransDateNew = wp.colStr("ex_trans_date_new");
          wp.colSet(rr, "ex_trans_date_new1", exTransDateNew.substring(0, exTransDateNew.indexOf("月") + 1));
          wp.colSet(rr, "ex_trans_date_new2", exTransDateNew.substring(exTransDateNew.indexOf("月") + 1));
          wp.colSet(rr, "ex_src_amt", lsSrcAmt[i]);
          String contactInfo = "如有疑問，請電："+wkPhone+"分機"+wkTel+wkUser;
          wp.colSet(rr, "wk_contact_info", contactInfo);
//          String lsDate = "";
//          lsDate = commDate.sysDate();
//          lsDate = commDate.toTwDate(lsDate);
//          wkSysDate = "中  華  民  國  " + lsDate.substring(0, 3) + " 年  " + lsDate.substring(3, 5)
//              + " 月　 " + lsDate.substring(5, 7) + " 日";
          wkSysDate = getTwDate();
          wp.colSet(rr, "wk_sys_date", wkSysDate);
        rr++;
    }
    wp.listCount[0] = wp.itemRows("opt");
  }

  void selectUser() {
    String lsTel = "", lsUser = "" , lsPhone = "";
    lsTel = wp.itemStr("ex_tel");
    lsUser = wp.itemStr("ex_user");
    lsPhone = wp.itemStr("ex_phone");
    if (!empty(lsTel))
      wkTel = lsTel;
    if (!empty(lsUser)) {
      wkUser = lsUser;
    }
    if (!empty(lsPhone)) {
      wkPhone = lsPhone;
      return;
    }
    if (empty(lsTel))
      lsTel = sqlStr("ls_tel");
    if (empty(lsUser))
      lsUser = sqlStr("ls_user");
    wkTel = lsTel;
    wkUser = lsUser;
    return;
  }
  
 public String getTwDateString(String date){
   String dateString = "";
   String lsDate = "";
   lsDate = commDate.toTwDate(date);
   // 需要判斷一下傳入的日期 因爲後面拆分的時候是又不同情況的
   int stringInt = Integer.parseInt(date.substring(0, 4));
    if(stringInt <= 2010) {
        dateString = lsDate.substring(0, 2) + lsDate.substring(2, 4) + lsDate.substring(4, 6);
    }else {
          dateString = lsDate.substring(0, 3) + lsDate.substring(3, 5) + lsDate.substring(5, 7);
        }
    
    
    return dateString;
  }
 
 public String getTwDateStringTwo(String date){
   String dateString = "";
   String lsDate = "";
   lsDate = commDate.toTwDate(date);
   // 需要判斷一下傳入的日期 因爲後面拆分的時候是又不同情況的
   int stringInt = Integer.parseInt(date.substring(0, 4));
    if(stringInt <= 2010) {
        dateString = lsDate.substring(0, 2) + "年" + lsDate.substring(2, 4)
        + "月" + lsDate.substring(4, 6) + "日";
    }else {
          dateString = lsDate.substring(0, 3) + "年 " + lsDate.substring(3, 5)
          + "月" + lsDate.substring(5, 7) + "日";
        }
    
    
    return dateString;
  }

 public String getTwDate() {
    String lsDate = "";
    lsDate = commDate.sysDate();
    lsDate = commDate.toTwDate(lsDate);
    wkSysDate = "中  華  民  國  " + lsDate.substring(0, 3) + " 年  " + lsDate.substring(3, 5)
    + " 月　 " + lsDate.substring(5, 7) + " 日";
    return wkSysDate;
  }

}
