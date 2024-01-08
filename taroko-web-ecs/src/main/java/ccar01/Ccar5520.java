package ccar01;
/**
 * 2023-0825   JH    no-data print PDF
 * 2023-1019   JH    orderBy:cell+idPseno
 * 2023-1023   JH    hec/oempay: status_code=0
 * 2023-1030   JH    oempay: MC[wallet_id], Visa[token_type=03]
 * 2023-1211   JH    cond=tw_date
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommDate;
import taroko.com.TarokoPDF;

public class Ccar5520 extends BaseAction implements InfacePdf {
taroko.base.CommDate commDate = new CommDate();

String cellarPhone = "";

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
   } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
   }

}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {

   String lsWhere = " where 1=1 ";
   wp.setQueryMode();
   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.sqlCmd = " select A.cellar_phone , 'HCE' as src_type , A.id_p_seqno , A.crt_date, A.card_no"
       +" from hce_card A"
       +" where A.status_code='0' and A.cellar_phone in ( "
       +" select A.cellar_phone from hce_card A "
       +" where A.status_code ='0' and A.cellar_phone <> '' "
       +sqlCol(wp.itemStr("ex_date1"), "A.crt_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +"    and case when substring(card_no,1,1)='4' then token_type='03' else wallet_id in ('103','206') end"
       +" ) "
       +" union "
       +" select A.cellar_phone  from oempay_card A "
       +" where A.status_code = '0' and A.cellar_phone <> '' "
       +"   and case when substring(card_no,1,1)='4' then token_type='03' else wallet_id in ('103','206') end "
       +sqlCol(wp.itemStr("ex_date1"), "A.crt_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" union "
       +" select A.cellar_phone from epay_card A "
       +" where A.cellar_phone <> '' "
       +sqlCol(wp.itemStr("ex_date1"), "A.link_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +"    and case when substring(card_no,1,1)='4' then token_type='03' else wallet_id in ('103','206') end "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" ) "
       +" union "  //-OEM-----
       +" select A.cellar_phone , 'OEM' as src_type , A.id_p_seqno , A.crt_date, A.card_no"
       +" from oempay_card A"
       +" where A.status_code='0' "
       +"    and case when substring(A.card_no,1,1)='4' then A.token_type='03' else A.wallet_id in ('103','206') end "
       +" and A.cellar_phone in ( "
       +" select A.cellar_phone from hce_card A "
       +" where A.status_code ='0' and A.cellar_phone <> '' "
       +sqlCol(wp.itemStr("ex_date1"), "A.crt_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +"    and case when substring(card_no,1,1)='4' then token_type='03' else wallet_id in ('103','206') end "
       +" ) "
       +" union "
       +" select A.cellar_phone  from oempay_card A "
       +" where A.status_code = '0' and A.cellar_phone <> '' "
       +"    and case when substring(A.card_no,1,1)='4' then A.token_type='03' else A.wallet_id in ('103','206') end "
       +sqlCol(wp.itemStr("ex_date1"), "A.crt_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" union "
       +" select A.cellar_phone from epay_card A "
       +" where A.cellar_phone <> '' "
       +sqlCol(wp.itemStr("ex_date1"), "A.link_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +"    and case when substring(card_no,1,1)='4' then token_type='03' else wallet_id in ('103','206') end "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" ) "
       +" union "  //-EPAY-
       +" select A.cellar_phone , 'EPAY' as src_type , A.id_p_seqno , A.link_date as crt_date, A.card_no"
       +" from epay_card A "
       +" where A.cellar_phone in ( "
       +" select A.cellar_phone from hce_card A "
       +" where A.status_code ='0' and A.cellar_phone <> '' "
       +sqlCol(wp.itemStr("ex_date1"), "A.crt_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +"    and case when substring(card_no,1,1)='4' then token_type='03' else wallet_id in ('103','206') end "
       +" ) "
       +" union "
       +" select A.cellar_phone  from oempay_card A "
       +" where A.status_code = '0' and A.cellar_phone <> '' "
       +"    and case when substring(A.card_no,1,1)='4' then A.token_type='03' else A.wallet_id in ('103','206') end "
       +sqlCol(wp.itemStr("ex_date1"), "A.crt_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" union "
       +" select A.cellar_phone from epay_card A "
       +" where A.cellar_phone <> '' "
       +sqlCol(wp.itemStr("ex_date1"), "A.link_date")
       +" and A.cellar_phone in "
       +" (select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +"    and case when substring(card_no,1,1)='4' then token_type='03' else wallet_id in ('103','206') end "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" ) "
       +" order by cellar_phone, id_p_seqno"
   ;

   wp.pageCountSql = " select count(*) from ( "+wp.sqlCmd+" )";

   pageQuery();


   if (sqlNotFind()) {
      if (eqIgno(strAction, "PDF")) {
         //{cellar_phone}	{id_no}	{chi_name}--
         wp.colSet("cellar_phone", "*****");
         wp.colSet("id_no", "本 日 無 資 料");
         wp.colSet("chi_name", "*****");
         wp.listCount[0] = 1;
         return;
      }
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
   queryAfter();
}

void queryAfter() throws Exception {
   String sql1 = "select card_no , uf_card_name(card_no) as chi_name from hce_card where id_p_seqno = ? and status_code = '0' fetch first 1 rows only ";
   String sql2 = "select card_no , uf_card_name(card_no) as chi_name from oempay_card where id_p_seqno = ? and status_code = '0' fetch first 1 rows only ";
   String sql3 = "select card_no , uf_card_name(card_no) as chi_name from epay_card where id_p_seqno = ? fetch first 1 rows only ";
//		String sql4 = "select uf_idno_id(major_id_p_seqno) as major_id from crd_card where card_no = ? ";
   String sql4 = "select B.id_no as major_id from crd_card A"
       +" join crd_idno B on A.major_id_p_seqno=B.id_p_seqno where A.card_no = ? ";
   String sql5 = "select id_no, chi_name from crd_idno where id_p_seqno = ? ";

   for (int ii = 0; ii < wp.selectCnt; ii++) {
      String ls_idPseqno = wp.colStr(ii, "id_p_seqno");
      sqlSelect(sql5, new Object[]{ls_idPseqno});
      if (sqlRowNum > 0) {
         wp.colSet(ii, "id_no", sqlStr("id_no"));
         wp.colSet(ii, "chi_name", sqlStr("chi_name"));
      }

//			String ls_srcType =wp.colStr(ii,"src_type");
//			if(eqIgno(ls_srcType, "HCE")) {
//				sqlSelect(sql1,new Object[] {ls_idPseqno});
//				if(sqlRowNum > 0) {
//					wp.colSet(ii,"chi_name",sqlStr("chi_name"));
////					wp.colSet(ii,"card_no",sqlStr("card_no"));
//				}
//			}
//			else if(eqIgno(ls_srcType, "OEM")) {
//				sqlSelect(sql2,new Object[] {ls_idPseqno});
//				if(sqlRowNum > 0) {
//					wp.colSet(ii,"chi_name",sqlStr("chi_name"));
////					wp.colSet(ii,"card_no",sqlStr("card_no"));
//				}
//			}
//			else if(eqIgno(ls_srcType, "EPAY")) {
//				sqlSelect(sql3,new Object[] {ls_idPseqno});
//				if(sqlRowNum > 0) {
//					wp.colSet(ii,"chi_name",sqlStr("chi_name"));
////					wp.colSet(ii,"card_no",sqlStr("card_no"));
//				}
//			}

      String ls_cardNo = wp.colStr(ii, "card_no");
      if (empty(ls_cardNo) == false) {
         sqlSelect(sql4, new Object[]{ls_cardNo});
         if (sqlRowNum > 0) {
            wp.colSet(ii, "major_id", sqlStr("major_id"));
         }
      }
   }
}

@Override
public void querySelect() throws Exception {
   cellarPhone = wp.itemStr("data_k1");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (cellarPhone.isEmpty()) {
      alertErr("手機號碼不可空白");
      return;
   }

   wp.sqlCmd = " select A.cellar_phone , 'HCE' as src_type , A.id_p_seqno from hce_card A "
       +" where A.status_code ='0' and A.cellar_phone <> '' "
       +sqlCol(cellarPhone, "A.cellar_phone")
       +" and A.cellar_phone in "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" union "
       +" select A.cellar_phone , 'OEM' as src_type , A.id_p_seqno from oempay_card A "
       +" where A.status_code = '0' and A.cellar_phone <> '' "
       +sqlCol(cellarPhone, "A.cellar_phone")
       +" and A.cellar_phone in  "
       +" (select cellar_phone from epay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" union "
       +" select A.cellar_phone , 'EPAY' as src_type , A.id_p_seqno from epay_card A "
       +" where A.cellar_phone <> '' "
       +sqlCol(cellarPhone, "A.cellar_phone")
       +" and A.cellar_phone in  "
       +" (select cellar_phone from oempay_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0'  "
       +" union "
       +" select cellar_phone from hce_card where id_p_seqno <> A.id_p_seqno and cellar_phone = A.cellar_phone and status_code = '0' "
       +" ) "
       +" group by A.cellar_phone , A.id_p_seqno order by 1 "
   ;

   wp.pageCountSql = " select count(*) from ( "+wp.sqlCmd+" )";

   pageQuery();

   if (sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
   }

   wp.setListCount(0);
   wp.setPageValue();
   queryAfter();

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
   wp.colSet("ex_date1", getSysDate());

}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "Ccar5520";

   wp.pageRows = 9999;
   queryFunc();

   //-cond1--
//	    String cond1 ="綁定日期: "+commString.strToYmd(wp.itemStr("ex_date1"));
//	    wp.colSet("cond1",cond1);
   String ls_date1 = wp.itemStr("ex_date1");
   ls_date1 = commDate.toTwDate(ls_date1);
   String ls_twDate = "中華民國 "+commString.mid(ls_date1, 0, 3)
       +" 年 "+commString.mid(ls_date1, 3, 2)
       +" 月 "+commString.mid(ls_date1, 5, 2)+" 日";
   wp.colSet("tw_ex_date1", ls_twDate);

   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   pdf.excelTemplate = "ccar5520.xlsx";
   pdf.pageCount = 30;
   pdf.sheetNo = 0;
   pdf.procesPDFreport(wp);
   pdf = null;

}

}
