package rskr03;
/**
 * 2020-0602   JH    bug-fix
 * 2018-0912:	JH		where-cond
 * */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr3450 extends BaseAction implements InfacePdf {
String lsDate = "", lsStatus = "";

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
   if (empty(wp.itemStr("ex_asgn_date1"))) {
      alertErr("Assign Date 不可空白");
      return;
   }
   if (!empty(wp.itemStr("ex_idno"))) {
      if (wp.itemStr("ex_idno").length() < 7) {
         alertErr("ID 不可小於 7 碼");
         return;
      }
   }

   if (this.chkStrend(wp.itemStr("ex_asgn_date1"), wp.itemStr("ex_asgn_date2")) == false) {
      alertErr("Assign Date起迄：輸入錯誤");
      return;
   }

   if (this.chkStrend(wp.itemStr("ex_add_date1"), wp.itemStr("ex_add_date2")) == false) {
      alertErr("問交登錄日期起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1 and B.ecs_close_date ='' "
         + sqlCol(wp.itemStr("ex_asgn_date1"), "A.assign_date", ">=")
         + sqlCol(wp.itemStr("ex_asgn_date2"), "A.assign_date", "<=")
         + sqlCol(wp.itemStr("ex_idno"), "A.idno", "like%")
         + sqlCol(wp.itemStr("ex_ctrl_seqno"), "B.ctrl_seqno", "like%")
         + sqlCol(wp.itemStr("ex_proc_user"), "A.proc_user", "like%")
         + sqlCol(wp.itemStr("ex_add_date1"), "C.hold_date", ">=")
         + sqlCol(wp.itemStr("ex_add_date2"), "C.hold_date", "<=");
   if (eqIgno(wp.itemStr("ex_proc_flag"), "1")) {
      lsWhere += " and nvl(B.ecs_close_reason,'') ='' "
            + " and not exists (select 1 from rsk_chgback where ctrl_seqno =C.ctrl_seqno "
            + " and card_no =A.card_no and (fst_apr_date<>'' or chg_stage>'1')) ";
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
         + " decode(D.chg_times,2,D.sec_expire_date,D.fst_expire_date) as cb_expr_date ,"
         + " A.card_no ,"
         + " A.case_no ,"
         + " A.idno ,"
         + " A.assign_date ,"
         + " A.proc_user ,"
         + " B.ctrl_seqno ,"
         + " B.txn_date ,"
         + " B.txn_amt ,"
         + " C.hold_date ,"
         + " C.recv_date ,"
         + " C.recv_resp_date ,"
         + " C.cb_date_1st ,"
         + " C.rp_date ,"
         + " C.cb_date_2nd ,"
         + " C.pre_arbi_date ,"
         + " C.arbi_date ,"
         + " C.pre_comp_date ,"
         + " C.comp_date ,"
         + " C.faith_date ,"
         + " uf_ctfc_idname(A.card_no) as db_cname ,"
         + " '' as db_proc_status "
   ;
   wp.daoTable = "rsk_ctfc_mast A left join rsk_ctfc_txn B on A.case_no =B.case_no left join rsk_ctfc_proc C"
         + " on B.ctrl_seqno =C.ctrl_seqno left join rsk_chgback D on A.card_no =D.card_no"
         + " and D.ctrl_seqno =(case when substr(B.ctrl_seqno,1,1) between 'A' and 'Z' then substr(B.ctrl_seqno,2)"
         + " else B.ctrl_seqno end)"
   ;

   wp.whereOrder = " order by 1 Desc ";

   pageQuery();
   queryAfter();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();

}

void queryAfter() {
   for (int ii = 0; ii < wp.selectCnt; ii++) {
      lsDate = "";
      lsStatus = "";
      lsDate = wp.colStr(ii, "hold_date");
      if (!empty(lsDate))
         lsStatus = "10";

      if (chkStrend(wp.colStr(ii, "recv_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "recv_date");
         lsStatus = "20";
      }

      if (chkStrend(wp.colStr(ii, "recv_resp_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "recv_resp_date");
         lsStatus = "22";
      }

      if (chkStrend(wp.colStr(ii, "cb_date_1st"), lsDate) == false) {
         lsDate = wp.colStr(ii, "cb_date_1st");
         lsStatus = "30";
      }

      if (chkStrend(wp.colStr(ii, "rp_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "rp_date");
         lsStatus = "40";
      }

      if (chkStrend(wp.colStr(ii, "cb_date_2nd"), lsDate) == false) {
         lsDate = wp.colStr(ii, "cb_date_2nd");
         lsStatus = "42";
      }

      if (chkStrend(wp.colStr(ii, "pre_arbi_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "pre_arbi_date");
         lsStatus = "50";
      }

      if (chkStrend(wp.colStr(ii, "arbi_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "arbi_date");
         lsStatus = "52";
      }

      if (chkStrend(wp.colStr(ii, "pre_comp_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "pre_comp_date");
         lsStatus = "60";
      }

      if (chkStrend(wp.colStr(ii, "comp_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "comp_date");
         lsStatus = "62";
      }

      if (chkStrend(wp.colStr(ii, "faith_date"), lsDate) == false) {
         lsDate = wp.colStr(ii, "faith_date");
         lsStatus = "70";
      }

      wp.colSet(ii, "db_proc_status", lsStatus);

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
   wp.reportId = "rskr3450";
//   wp.varRows = 999999;
	wp.pageRows =9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   String tt = "";

   tt = "Assign Date: " + commString.strToYmd(wp.itemStr("ex_asgn_date1")) + " -- " + commString.strToYmd(wp.itemStr("ex_asgn_date2"))
   ;
   wp.colSet("cond1", tt);
   wp.colSet("user_id", wp.loginUser);
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3450.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;

   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

}
