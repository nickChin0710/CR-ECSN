package rskr03;


import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3440 extends BaseAction implements InfacePdf {

@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         clearFunc(); break;
      case "L": //清畫面--
         strAction = "";
         clearFunc(); break;
      case "Q": //查詢功能
         queryFunc(); break;
      case "M": //瀏覽功能 :skip-page-
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "R": // -資料讀取-
         dataRead(); break;
//      case "A": //新增功能
//      case "U": //更新功能
//      case "D": //刪除功能
//         saveFunc(); break;
//      case "C": // -資料處理-
//         procFunc(); break;
      case "PDF": //-PDF-
         strAction = "PDF";
         pdfPrint(); break;
      default:
         alertErr("actionCode未指定對應功能, action[%s]",wp.buttonCode);
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_date1")) && empty(wp.itemStr("ex_date2"))) {
      alertErr("交易日期 不可空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("交易期間起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_date1"), "B.txn_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "B.txn_date", "<=")
         + sqlCol(wp.itemStr("ex_proc_user"), "A.proc_user", "like%");
   if (eqIgno(wp.itemStr("ex_type"), "1")) {
      lsWhere += " and C.cb_reason_1st<>''";
   }
   else if (eqIgno(wp.itemStr("ex_type"), "2")) {
      lsWhere += " and C.cb_reason_2nd <>'' ";
   }
   else if (eqIgno(wp.itemStr("ex_type"), "3")) {
      lsWhere += " and upper(C.pre_arbi_flag) ='ACCEPT' ";
   }
   else if (eqIgno(wp.itemStr("ex_type"), "4")) {
      lsWhere += " upper(C.pre_comp_flag) ='Y' ";
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " A.case_no ,"
         + " A.idno ,"
         + " A.assign_date ,"
         + " A.card_no ,"
         + " A.proc_user ,"
         + " D.chi_name ,"
         + " B.txn_date ,"
         + " B.txn_amt ,"
         + " C.ctrl_seqno ,"
         + " C.hold_date ,"
         + " C.hold_amt ,"
         + " C.cb_date_1st ,"
         + " C.cb_amt_1st ,"
         + " C.cb_date_2nd ,"
         + " C.cb_amt_2nd ,"
         + " C.pre_arbi_date ,"
         + " C.pre_arbi_flag ,"
         + " C.pre_comp_date ,"
         + " C.pre_comp_flag ,"
         + "uf_bin_type(A.card_no) as bin_type "
   ;
   wp.daoTable = "rsk_ctfc_mast A join rsk_ctfc_txn B on A.case_no = B.case_no join rsk_ctfc_proc C on B.ctrl_seqno = C.ctrl_seqno left join rsk_ctfc_idno D on A.idno =D.id_no";

   wp.whereOrder =" order by B.txn_date,A.idno,A.assign_date";
   pageQuery();

   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();

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
   // TODO Auto-generated method stub

}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "rskr3440";
   //wp.varRows = 999999;
	wp.pageRows =9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   String tt = "";
   if (eqIgno(wp.itemStr("ex_type"), "1")) {
      tt += "列印類別 : 1. 1CB 成功    ";
   }
   else if (eqIgno(wp.itemStr("ex_type"), "2")) {
      tt += "列印類別 : 2. 2CB 成功    ";
   }
   else if (eqIgno(wp.itemStr("ex_type"), "3")) {
      tt += "列印類別 : 3. Pre-Arbitration 成功   ";
   }
   else if (eqIgno(wp.itemStr("ex_type"), "4")) {
      tt += "列印類別 : 4. Pre-Compliance 成功   ";
   }
   tt = "交易期間: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_date2"))
   ;
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3440.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
