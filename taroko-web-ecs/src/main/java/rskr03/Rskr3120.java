package rskr03;
/**
 * 2020-0917   JH    like%: proc_user, card_no
 * */
import ofcapp.BaseQuery;
import ofcapp.InfacePdf;
import taroko.com.TarokoCommon;
import taroko.com.TarokoPDF;

public class Rskr3120 extends BaseQuery {

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;

   strAction = wp.buttonCode;
   switch (wp.buttonCode) {
      case "L":
         strAction = "";
         clearFunc(); break;
      case "X":
         strAction = "new";
         clearFunc(); break;
      case "Q":
         queryFunc(); break;
      case "M":
         queryRead(); break;
      case "S":
         querySelect(); break;
      case "R":
         dataRead(); break;
      case "PDF":
         doPrint_pdf(); break;
//      case "XLS":  //-Excel-
//         strAction = "XLS";
//         xlsPrint(); break;
      default:
         alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("3120") > 0) {
         wp.optionKey = wp.colStr("ex_proc_type");
         dddwList("dddw_proc_type", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_TYPE'");
      }
   }
   catch (Exception ex) {
   }
}


@Override
public void queryFunc() throws Exception {
   if (empty(wp.itemStr("ex_proc_date1")) &&
         empty(wp.itemStr("ex_proc_date2"))
   ) {
      alertErr("處理日期不可全部空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_proc_date1"), wp.itemStr("ex_proc_date2")) == false) {
      alertErr("處理日期起迄：輸入錯誤");
      return;
   }


   wp.whereStr = " where 1=1 "
         + sqlCol(wp.itemStr("ex_proc_date1"), "proc_date", ">=")
         + sqlCol(wp.itemStr("ex_proc_date2"), "proc_date", "<=")
         + sqlCol(wp.itemStr("ex_proc_user"), "proc_user", "like%")
         + sqlCol(wp.itemStr("ex_tel_no"), "tel_no", "like%")
         + sqlCol(wp.itemStr("ex_proc_remark"), "proc_remark", "%like%")
         + sqlCol(wp.itemStr("ex_proc_type"), "proc_type")
         + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%");
   if (wp.itemEq("ex_wash_amt", "0") == false) {
	  wp.whereStr += sqlCol(wp.itemStr("ex_wash_amt"),"wash_amt_flag");      
   }
   if (wp.itemEq("ex_mesg_terr", "0") == false) {
	  wp.whereStr += sqlCol(wp.itemStr("ex_mesg_terr"),"mesg_terr_flag");      
   }

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""
         + " card_no ,    "
         + " proc_date , "
         + " proc_time ,  "
         + " ctfg_seqno ,  "
         + " proc_type ,  "
         + " tel_no ,  "
         + " contr_result ,  "
         + " proc_user ,  "
         + " proc_remark ,  "
         + " wash_amt_flag,"
         + " mesg_terr_flag "
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereOrder = " order by proc_date, proc_time, card_no ";

   pageQuery();

   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(1);
   wp.setPageValue();
}

@Override
public void querySelect() throws Exception {
   // TODO Auto-generated method stub

}

void doPrint_pdf() throws Exception {
   wp.reportId = "rskr3120";
   String ss = "處理日期: " +
         commString.strToYmd(wp.itemStr("ex_proc_date1")) +
         " -- " + commString.strToYmd(wp.itemStr("ex_proc_date2"));
   wp.colSet("cond_1", ss);
   wp.colSet("user_id", wp.loginUser);
   wp.pageRows = 9999;
   queryFunc();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr3120.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
}

@Override
public void dataRead() throws Exception {
   // TODO Auto-generated method stub

}

}
