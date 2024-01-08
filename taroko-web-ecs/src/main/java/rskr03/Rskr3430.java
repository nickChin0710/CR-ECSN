package rskr03;
/**
 * 2020-0923   JH    modify
 *
 * */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3430 extends BaseAction implements InfacePdf {

@Override
public void userAction() throws Exception {
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
   if (empty(wp.itemStr("ex_date1"))) {
      alertErr("請輸入 指定期間");
      return;
   }

   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("指定期間起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_date1"), "A.assign_date", ">=")
         + sqlCol(wp.itemStr("ex_date2"), "A.assign_date", "<=");

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " a.source_type ,"
         + " count(distinct a.case_no) as db_cnt_case ,"
         + " count(distinct a.idno) as db_cnt_idno ,"
         + " sum(decode(nvl(b.ctrl_seqno,''),'',0,1)) as db_cnt_ctrl ,"
         + " sum(nvl(b.txn_amt,0)) as db_amt "
   ;
   wp.daoTable = "rsk_ctfc_mast a left join rsk_ctfc_txn b on a.case_no = b.case_no";
   wp.whereOrder = " group by a.source_type order by a.source_type Asc ";

//   wp.pageCountSql = ""
//         + "select count(*) from ( "
//         + " select distinct a.source_type "
//         + " from rsk_ctfc_mast a left join rsk_ctfc_txn b on a.case_no = b.case_no "
//         + wp.whereStr
//         + " )"
//   ;

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
   wp.reportId = "rskr3430";
   wp.pageRows = 9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   String tt = "";
   tt = "指定期間: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_date2"))
   ;
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3430.xlsx";
   pdf.pageCount = 35;
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
