package rdsm01;

import taroko.com.TarokoPDF;

/**
 * 2023-0428   JH    initial
 * */

public class Rdsr0040 extends ofcapp.BaseAction{
@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "PDF":
         doPrint_pdf(); break;
      default:
         defaultAction();
   }
}

void doPrint_pdf() throws Exception {
   wp.reportId = "rdsr0040";
   String cond1 = "";
   cond1 += "異動日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
           + commString.strToYmd(wp.itemStr("ex_date2"));
   cond1 +="    車號: "+wp.itemStr("ex_car_no")+
         "    身分證ID: "+wp.itemStr("ex_idno")+
           "    優惠別: "+wp.itemStr("ex_rds_pcard")
         ;
   wp.colSet("cond1", cond1);

   wp.pageRows = 9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rdsr0040.xlsx";
   pdf.pageCount = 33;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
}

@Override
public void dddwSelect() {
   try {
      String dddw=ecsfunc.DeCodeRdsm.rdsPcard(wp.colStr("ex_rds_pcard"),true);
      wp.colSet("dddw_rds_pcard",dddw);
   }
   catch (Exception ex) {}
}

String condWhere() {
   String lsWhere="where 1=1"+
           sqlStrend(wp.itemStr("ex_date1"),wp.itemStr("ex_date2"),"moddate")+
           sqlCol(wp.itemStr("ex_car_no").toUpperCase(),"upper(rm_carno)", "like%")+
           sqlCol(wp.itemStr("ex_idno"), "rm_carmanid")+
           sqlCol(wp.itemStr("ex_rds_pcard"),"rds_pcard")+
           " and err_code not in ('','00')";
   return lsWhere;
}
@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_date1,ex_date2,ex_car_no,ex_idno,ex_rds_pcard")) {
      alertErr("查詢條件 不可全部空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期: 起迄錯誤");
      return;
   }

   wp.whereStr =condWhere();
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
           + "rm_carmanid, rm_carno, card_no, rds_pcard"
           +", mod_type, moddate, err_code, err_reason"
   +", moddate mod_date"
   +", err_code||'_'||err_reason wk_err_code_reason"
           ;
   wp.daoTable = "cms_roadmaster_log";
   wp.whereOrder = " order by rm_carmanid, rm_carno, moddate, hex(rowid)";
   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
//   queryAfter();
}

@Override
public void querySelect() throws Exception {

}

@Override
public void dataRead() throws Exception {

}

@Override
public void saveFunc() throws Exception {

}

@Override
public void procFunc() throws Exception {

}

@Override
public void initButton() {

}

@Override
public void initPage() {

}
}
