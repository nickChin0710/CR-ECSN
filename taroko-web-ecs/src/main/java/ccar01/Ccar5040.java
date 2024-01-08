package ccar01;
/**
 * 2023-1023   JH    交易成功,排除沖正交易
 * 2023-1207   JH    report modify
 * 2023-1211   JH    mcht not in (hi-mcht)
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommDate;
import taroko.com.TarokoPDF;

public class Ccar5040 extends BaseAction implements InfacePdf {
taroko.base.CommDate commDate=new CommDate();

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X":
         /* 轉換顯示畫面 */
         strAction = "new";
         clearFunc();
         break;
      case "Q":
         /* 查詢功能 */
         strAction = "Q";
         queryFunc();
         break;
      case "R":
         // -資料讀取-
         strAction = "R";
         dataRead();
         break;
      case "A":
         /* 新增功能 */
         saveFunc();
         break;
      case "U":
         /* 更新功能 */
         saveFunc();
         break;
      case "D":
         /* 刪除功能 */
         saveFunc();
         break;
      case "M":
         /* 瀏覽功能 :skip-page */
         queryRead();
         break;
      case "S":
         /* 動態查詢 */
         querySelect();
         break;
      case "L":
         /* 清畫面 */
         strAction = "";
         clearFunc();
         break;
      case "C":
         // -資料處理-
         procFunc();
         break;
      case "PDF":
         pdfPrint();
         break;
      default:
         break;
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {

   taroko.base.CommDate commDate = new taroko.base.CommDate();

   String ls_txDate2 = wp.itemStr("ex_query_date");
   if (empty(ls_txDate2)) {
      alertErr("查詢日期: 不可空白");
      return;
   }
   String ls_txDate = commDate.dateAdd(ls_txDate2, 0, 0, -5);

   //-沖正交易auth_type=R-
   String lsWhere = " where 1=1 "
       +" and cacu_amount <>'N' "
       +sqlCol(ls_txDate, "tx_date", ">=")
       +sqlCol(ls_txDate2, "tx_date", "<")
       +" and consume_country in ("
       +"select country_code from cca_country where high_risk = 'Y' "
       +" union select bin_country from cca_country where high_risk ='Y' ) ";

   lsWhere += " and id_p_seqno in ("
       +" select id_p_seqno from cca_auth_txlog where 1=1 "
       +" and consume_country in ("
       +"select country_code from cca_country where high_risk = 'Y' "
       +" union select bin_country from cca_country where high_risk ='Y' ) "
       +" and cacu_amount <>'N' "
       +sqlCol(ls_txDate, "tx_date", ">=")
       +sqlCol(ls_txDate2, "tx_date", "<")
       +" group by id_p_seqno having sum(nt_amt) >= 500000 "
       +" ) "
   ;

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();
   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " uf_card_name(card_no) as chi_name , uf_idno_id2(id_p_seqno,acct_type) as id_no , card_no , "
       +" tx_date , tx_date as auth_date , consume_country , ori_amt , nt_amt , mcc_code , pos_mode , id_p_seqno "
   ;
   wp.daoTable = " cca_auth_txlog ";
   wp.whereOrder = " order by 2 , 3 ";

   pageQuery();
   if (sqlNotFind()) {
      if (eqIgno(wp.buttonCode,"PDF")) {
         wp.colSet(0,"card_no","本日無資料");
         wp.listCount[0] =1;
      }
      else {
         alertErr("此條件查無資料");
         return;
      }
   }

   if (sqlNotFind() ==false) {
      wp.setListCount(0);
      wp.setPageValue();
      queryAfter();
   }
   if (eqIgno(wp.buttonCode,"PDF")) {
      String ls_queryDate=wp.itemStr("ex_query_date");
      ls_queryDate =commDate.dateAdd(ls_queryDate,0,0,-1);
      ls_queryDate =commDate.toTwDate(ls_queryDate);
      String ls_twDate ="中華民國 "+commString.mid(ls_queryDate,0,3)
          +" 年 "+commString.mid(ls_queryDate,3,2)
          +" 月 "+commString.mid(ls_queryDate,5,2)+" 日";
      wp.colSet("tw_query_date",ls_twDate);
   }
}

void queryAfter() throws Exception {

   String sql1 = " select home_area_code1||home_tel_no1||home_tel_ext1 as home_tel , "
       +" office_area_code1||office_tel_no1||office_tel_ext1 as office_tel "
       +" from crd_idno where id_p_seqno = ? ";

   String sql2 = " select home_area_code1||home_tel_no1||home_tel_ext1 as home_tel , "
       +" office_area_code1||office_tel_no1||office_tel_ext1 as office_tel "
       +" from dbc_idno where id_p_seqno = ? ";

   for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "id_p_seqno")});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "home_tel", sqlStr("home_tel"));
         wp.colSet(ii, "office_tel", sqlStr("office_tel"));
      } else {
         sqlSelect(sql2, new Object[]{wp.colStr(ii, "id_p_seqno")});
         if (sqlRowNum > 0) {
            wp.colSet(ii, "home_tel", sqlStr("home_tel"));
            wp.colSet(ii, "office_tel", sqlStr("office_tel"));
         }
      }
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
   wp.colSet("ex_query_date", getSysDate());

}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "Ccar5040";

//	    String cond1 = "異動日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
//	        + commString.strToYmd(wp.itemStr("ex_date2"));
//	    wp.colSet("cond1", cond1);
   wp.pageRows = 9999;
   queryFunc();

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "ccar5040.xlsx";
   pdf.pageCount = 20;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;

}

}
