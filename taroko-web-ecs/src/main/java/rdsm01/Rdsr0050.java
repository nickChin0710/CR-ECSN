package rdsm01;

import taroko.com.TarokoExcel;

public class Rdsr0050 extends ofcapp.BaseAction{
@Override
public void userAction() throws Exception {
   strAction =wp.buttonCode;
   switch (wp.buttonCode) {
      case "XLS":
         doPrint_xls(); break;
      default:
         defaultAction();
   }
}

void doPrint_xls() throws Exception {
   log("xlsFunction: started--------");
   wp.reportId = "rdsr0050-EXCEL";
   String lsCond = "查詢年月: " + commString.strToYmd(wp.itemStr("ex_date1"))+
           "    持卡人ID: "+wp.itemStr("ex_idno")+
           "    優惠別: "+wp.itemStr("ex_rds_pcard")
           ;
//   wp.colSet("user_id", wp.loginUser);
   wp.colSet("cond1", lsCond);
   TarokoExcel xlsx = new TarokoExcel();
   wp.fileMode = "Y";
   xlsx.excelTemplate = "rdsr0050-EXCEL.xlsx";
   wp.pageRows = 9999;
   queryFunc();
   //--
   lsCond ="合計: "+wp.itemStr("tl_cnt")+"    優惠別: "+wp.itemStr("tl_rds_pcard");
   wp.colSet("ttl_cond",lsCond);

   xlsx.processExcelSheet(wp);
   xlsx.outputExcel();
   xlsx = null;
   log("xlsFunction: ended-------------");
}

@Override
public void dddwSelect() {

}

@Override
public void queryFunc() throws Exception {
   if (itemallEmpty("ex_date1")) {
      alertErr("查詢條件: 不可全部空白");
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
           +sqlCol(wp.itemStr("ex_date1"),"crt_date","like%")
           +sqlCol(wp.itemStr("ex_car_no"),"rm_carno")
           +sqlCol(wp.itemStr("ex_card_no"),"card_no")
           +sqlCol(wp.itemStr("ex_rds_pcard"),"rds_pcard")
   ;

   String lsIdno =wp.itemStr("ex_idno");
   if (!empty(lsIdno)) {
      lsWhere +=" and id_p_seqno in (select id_p_seqno from crd_idno where 1=1"+sqlCol(lsIdno,"id_no")+")";
   }

   return lsWhere;
}

void querySum(String asWhere) throws Exception {
   String sql1 = " select rds_pcard"
           + ", count(*) as xx_cnt "
           + " from cms_roadlist "
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
           +" rds_pcard, rm_carno, rm_carmanid, chi_name"+
           ", group_code, card_no, crt_date"
           +"";

   wp.daoTable = "cms_roadlist";
   wp.whereOrder = " order by rds_pcard, rm_carno, rm_carmanid, card_no";
   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
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
