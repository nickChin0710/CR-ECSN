package rdsm01;

import taroko.com.TarokoPDF;

public class Rdsr0010 extends ofcapp.BaseAction {
@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "PDF":
         doPrint_Pdf();
         break;
      default:
         defaultAction();
   }
}

void doPrint_Pdf() throws Exception {
   wp.reportId = "rdsr0010";
   String cond1 = "";
   cond1 = "異動來源:";
   if (wp.itemEq("ex_type", "1")) {
      cond1 += "Web";
   } else if (wp.itemEq("ex_type", "2")) {
      cond1 += "網銀";
   } else {
      cond1 += "全部";
   }
   cond1 += "  異動狀態:";
   if (wp.itemEmpty("ex_status")) {
      cond1 += "全部";
   } else cond1 +=ecsfunc.DeCodeRdsm.rdStatus(wp.itemStr("ex_status"));
   cond1 += " 異動日期 : " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
           + commString.strToYmd(wp.itemStr("ex_date2"));
   wp.colSet("cond1", cond1);

   wp.pageRows = 9999;
   queryFunc();
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "rdsr0010.xlsx";
   pdf.pageCount = 33;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;
}

@Override
public void dddwSelect() {

}

@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_date1,ex_date2,ex_type,ex_status")) {
      alertErr("查詢條件: 不可全部空白");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期: 起迄錯誤");
      return;
   }



   querySum(queryWhere());

   wp.whereStr =queryWhere();
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}
String queryWhere() {
   String lsWhere = "";
   lsWhere = " where 1=1 "
           +sqlCol(wp.itemStr("ex_date1"),"rd_moddate",">=")
           +sqlCol(wp.itemStr("ex_date2"),"rd_moddate","<=")
           +sqlCol(wp.itemStr("ex_status"),"rd_status")
   ;
   //-異動來源:-
   if (wp.itemEq("ex_type","1")) {
      lsWhere +=" and mod_pgm <>'ECSCDA03'";
   }
   else if (wp.itemEq("ex_type","2")) {
      lsWhere +=" and mod_pgm ='ECSCDA03'";
   }

   return lsWhere;
}

void querySum(String asWhere) throws Exception {
   String sql1 = " select rds_pcard"
           + ", count(*) as xx_cnt "
           + " from cms_roaddetail "
           + asWhere
           +" group by rds_pcard"
           +" order by rds_pcard"
           ;
   sqlSelect(sql1);
   int ll_nrow=sqlRowNum;
   String lsRdsPcard="";
   int ll_tlCnt=0;
   for (int ll = 0; ll <ll_nrow ; ll++) {
      ll_tlCnt +=sqlInt(ll,"xx_cnt");
      String lsPcard=sqlStr(ll,"rds_pcard");
      if (empty(lsPcard)) continue;
      lsRdsPcard +="  "+lsPcard+":"+sqlInt(ll,"xx_cnt")+" 筆";
   }
   wp.colSet("tl_cnt", ll_tlCnt);
   wp.colSet("tl_rds_pcard",lsRdsPcard);
}


@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = ""
           +" rds_pcard, rd_carno, rd_carmanid, rd_carmanname"+
           ", group_code, card_no, rd_status"+
           ", '' as tt_rd_status"+
           ", crt_date, rd_moddate, mod_pgm"
           ;
   wp.daoTable = "cms_roaddetail";
   wp.whereOrder = " order by rd_moddate, card_no, mod_time ";
   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();

   queryAfter();
}

void queryAfter() throws Exception {
   int ll_nrow =wp.listCount[0];
   for (int ll = 0; ll <ll_nrow ; ll++) {
      String lsStatus=wp.colStr(ll,"rd_status");
      String ss=ecsfunc.DeCodeRdsm.rdStatus(lsStatus);
      wp.colSet(ll,"tt_rd_status",ss);
   }
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
