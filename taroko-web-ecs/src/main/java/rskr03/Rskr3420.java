package rskr03;


import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3420 extends BaseAction implements InfacePdf {

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
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
         saveFunc(); break;
      case "C": // -資料處理-
         procFunc(); break;
//      case "XLS":  //-Excel-
//         strAction = "XLS";
// 			xlsPrint(); break;
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
         + " A.proc_user ,"
         + " count(distinct A.case_no) db_cnt_case ,"
         + " sum(decode(nvl(B.ctrl_seqno,''),'',0,1)) db_cnt_ctrl ,"
         + " sum(nvl(B.txn_amt,0)) db_amt ,"
         + " sum(decode(nvl(B.ctrl_seqno,''),'',0,decode(nvl(B.ecs_close_date,''),'',0,1))) db_close_cnt ,"
         + " sum(decode(nvl(B.ctrl_seqno,''),'',0,decode(nvl(B.ecs_close_date,''),'',0,txn_amt))) db_close_amt ,"
         + " sum(decode(nvl(B.ctrl_seqno,''),'',0,decode(nvl(B.ecs_close_date,''),'',1,0))) db_unclose_cnt ,"
         + " sum(decode(nvl(B.ctrl_seqno,''),'',0,decode(nvl(B.ecs_close_date,''),'',txn_amt,0))) db_unclose_amt "
   ;
   wp.daoTable = "rsk_ctfc_mast A left join rsk_ctfc_txn B on A.case_no =B.case_no";
   wp.whereOrder = " group by A.proc_user order by A.proc_user Asc ";

//   wp.pageCountSql = ""
//         + "select count(*) from ( "
//         + " select distinct A.proc_user "
//         + " from rsk_ctfc_mast A left join rsk_ctfc_txn B on A.case_no =B.case_no "
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
   wp.reportId = "rskr3420";
   wp.pageRows = 9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   String tt = "";
   tt = "指定期間: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_date2"))
   ;
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3420.xlsx";
   pdf.pageCount = 35;
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
