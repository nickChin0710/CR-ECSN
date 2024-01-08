package rskr01;
/**
 * 2020-0618   JH    modify
 * 2020-0428:  Alex  queryFunc fix
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;
//import taroko.com.TarokoPDF_1page;

public class Rskr0290 extends BaseAction implements InfacePdf {
taroko.base.CommDate zzdate = new taroko.base.CommDate();
int iiPrintCnt = 0;
String isPrintForm = "";
@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "PDF": //-PDF-
         strAction = "PDF";
         printData(); break;
      default:
         defaultAction();
   }
}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr("扣款覆核日期 : 起迄 輸入錯誤 !");
      return;
   }

   if (wp.itemEmpty("ex_ctrl_seqno") &&
         wp.itemEmpty("ex_date1") &&
         wp.itemEmpty("ex_date2")) {
      alertErr("控制流水號,扣款覆核日期:不可全部空白");
      return;
   }

   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_bin_type"), "bin_type");
   if (!empty(wp.itemStr("ex_ctrl_seqno"))) {
      lsWhere += sqlCol(wp.itemStr("ex_ctrl_seqno"), "ctrl_seqno", "like%")
      ;
   }
   else {
      lsWhere += " and chg_stage in ('1','3') and sub_stage='30' "
            + sqlCol(wp.itemStr("ex_date1"), "fst_apr_date", ">=")
            + sqlCol(wp.itemStr("ex_date2"), "fst_apr_date", "<=")
      ;
      if (eqIgno(wp.itemStr("ex_bin_type"), "M")) {
         lsWhere += " and mcht_country in ('TWN','TW') ";
      }
   }

   if (wp.itemEq("ex_print","2")) {
      lsWhere +=" and fst_doc_mark ='Y'";
   }
   else if (wp.itemEq("ex_print","1")) {
      lsWhere +=" and fst_doc_mark <>'Y'";
   }
//   if (eq_igno(wp.itemStr("ex_print"), "1")) {
//      lsWhere += " and fst_doc_mark ='1' ";
//   }
//   else if (eq_igno(wp.itemStr("ex_print"), "2")) {
//      lsWhere += " and fst_doc_mark ='Y' ";
//   }
//   else {
//      lsWhere += " and fst_doc_mark in ('1','Y') ";
//   }
   
   if(wp.itemEq("ex_bin_type", "V")){
   	if(wp.itemEq("ex_print_form", "2E1")){
   		lsWhere += " and fst_reason_code not in ('10','10.1','10.2','10.3','10.4','10.5','11','11.1','11.2','11.3',"
   			+ "'12','12.1','12.2','12.3','12.4','12.5','12.6','12.7','13.2','13.3','13.4','13.5','13.6','13.7',"
   			+ "'13.8','13','13.1','13.9') ";
   		wp.colSet("print_form", "2E1");
   	}	else if(wp.itemEq("ex_print_form", "2E2")){
   		lsWhere += " and fst_reason_code in ('10','10.1','10.2','10.3','10.4','10.5') ";
   		wp.colSet("print_form", "2E2");
   	}	else if(wp.itemEq("ex_print_form", "2E3")){
   		lsWhere += " and fst_reason_code in ('11','11.1','11.2','11.3') ";
   		wp.colSet("print_form", "2E3");
   	}	else if(wp.itemEq("ex_print_form", "2E4")){
   		lsWhere += " and fst_reason_code in ('12','12.1','12.2','12.3','12.4','12.5','12.6','12.7') ";
   		wp.colSet("print_form", "2E4");
   	}	else if(wp.itemEq("ex_print_form", "2E5")){
   		lsWhere += " and fst_reason_code in ('13.2','13.3','13.4','13.5','13.6','13.7','13.8') ";
   		wp.colSet("print_form", "2E5");
   	}	else if(wp.itemEq("ex_print_form", "2E6")){
   		lsWhere += " and fst_reason_code in ('13','13.1','13.9') ";
   		wp.colSet("print_form", "2E6");
   	}	else if(wp.itemEmpty("ex_print_form")){
   		wp.colSet("print_form", "");
   	}
   }
   
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();


}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " decode(fst_doc_mark,'1','N','Y') as fst_doc_mark ,"
         + " bin_type , "
         + " ctrl_seqno , "
         + " post_date , "
         + " card_no , "
         + " uf_idno_name(id_p_seqno) as idno_name, "
         + " purchase_date , "
         + " dest_amt , "
         + " mcht_eng_name , "
         + " replace(mcht_chi_name,'　','') as mcht_chi_name , "
         + " txn_code , "
         + " film_no , "
         + " chg_times , "
         + " source_curr , "
         + " source_amt , "
         + " dest_curr , "
         + " fst_twd_amt , "
         + " fst_msg , "
         + " mcht_category , "
         + " fst_amount , "
         + " settl_amt , "
         + " fst_apr_date , "
         + " fst_reason_code , "
         + " debit_flag , "
         + " process_date , "
         + " v_card_no , "
         + " hex(rowid) as rowid "
   ;
   wp.daoTable = " rsk_chgback ";
   wp.whereOrder = " order by card_no, ctrl_seqno ";
   pageQuery();
   
   if (sqlRowNum <= 0) {
      alertErr("查無資料");
      return;
   }
   wp.setListCount(1);
   wp.setPageValue();
   queryAfter();
}

void queryAfter() {
	if(wp.itemEq("ex_bin_type", "V")==false)	return ;
	int il_select=0;
	il_select=wp.selectCnt;
	for(int ii=0;ii<il_select;ii++){
      String ls_reason = "|"+wp.colStr(ii, "fst_reason_code")+"|";
      if (commString.strIn(ls_reason,"|10|10.1|10.2|10.3|10.4|10.5|"))
         wp.colSet(ii, "visa_form", "2E2");
      else if (commString.strIn(ls_reason,"|11|11.1|11.2|11.3|"))
         wp.colSet(ii, "visa_form", "2E3");
      else if (commString.strIn(ls_reason,"|12|12.1|12.2|12.3|12.4|12.5|12.6|12.7|"))
         wp.colSet(ii, "visa_form", "2E4");
      else if (commString.strIn(ls_reason,"|13.2|13.3|13.4|13.5|13.6|13.7|13.8|"))
         wp.colSet(ii, "visa_form", "2E5");
      else if (commString.strIn(ls_reason,"|13|13.1|13.9|"))
         wp.colSet(ii,"visa_form", "2E6");
      else {
         wp.colSet(ii, "visa_form", "2E1");
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
   int llOk = 0, ll_err = 0;
   String[] lsRowid = wp.itemBuff("rowid");
   String[] aaOpt = wp.itemBuff("opt");
   wp.listCount[0] = lsRowid.length;
   if (optToIndex(aaOpt[0]) < 0) {
      alertErr("請點選已列印資料");
      return;
   }

   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0) continue;

      optOkflag(rr);
      int liRc = updateRskChgback(lsRowid[rr]);
      if (liRc == 1) {
         llOk++;
      }
      else {
         ll_err++;
      }
      sqlCommit(liRc);
      optOkflag(rr, liRc);
   }

   alertMsg("列印確認完成 :  成功:" + llOk + " 失敗:" + ll_err);
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
   wp.reportId = "rskr0290";
//   wp.pageRows = 9999;
//   TarokoPDF_1page pdf = new TarokoPDF_1page();
   TarokoPDF pdf = new TarokoPDF();
   pdf.setListIndex(2);

   wp.fileMode = "Y";
   if (eqIgno(wp.itemStr("hh_bin_type"), "V")) {
   	if(empty(isPrintForm)){
   		alertErr("未指定列印格式");
   		wp.respHtml = "TarokoErrorPDF";
   		return ;
   	}
   	
   	if(eqIgno(isPrintForm, "2E1")){
   		pdf.excelTemplate = "rskr0290_visa_2e1.xlsx";
   	}	else if(eqIgno(isPrintForm, "2E2")){
   		pdf.excelTemplate = "rskr0290_visa_2e2.xlsx";
   	}	else if(eqIgno(isPrintForm, "2E3")){
   		pdf.excelTemplate = "rskr0290_visa_2e3.xlsx";
   	}	else if(eqIgno(isPrintForm, "2E4")){
   		pdf.excelTemplate = "rskr0290_visa_2e4.xlsx";
   	}	else if(eqIgno(isPrintForm, "2E5")){
   		pdf.excelTemplate = "rskr0290_visa_2e5.xlsx";
   	}	else if(eqIgno(isPrintForm, "2E6")){
   		pdf.excelTemplate = "rskr0290_visa_2e6.xlsx";
   	}
   	      
   }
   else if (eqIgno(wp.itemStr("hh_bin_type"), "M")) {
      pdf.excelTemplate = "rskr0290_mast.xlsx";
   }
   else if (eqIgno(wp.itemStr("hh_bin_type"), "J")) {
      pdf.excelTemplate = "rskr0290_jcb.xlsx";
   }

   pdf.pageCount = 1;
   pdf.sheetNo = 0;
   pdf.pageVert = true;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

void printData() throws Exception {
   String[] aaCtrlSeqno = wp.itemBuff("ctrl_seqno");
   String[] aaBinType = wp.itemBuff("bin_type");
   String[] aaForm = wp.itemBuff("visa_form");

   String[] ls_opt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("ctrl_seqno");
   int ii = -1;
   
   isPrintForm = wp.itemStr("print_form");
   
   int liCnt = 0 , li_continue = 0 ;
   for (int rr = 0; rr < aaCtrlSeqno.length; rr++) {
      if (!checkBoxOptOn(rr, ls_opt)) continue;
      
      if(liCnt!=0 && eqIgno(aaBinType[rr], "V") && eqIgno(aaForm[rr],isPrintForm)==false){
      	li_continue++ ;
      	continue ;
      }
      
      liCnt++;
      String sql1 = " select A.* "
            + ", uf_idno_name(A.card_no) as db_idno_name "
            + " from rsk_chgback A "
            + " where A.ctrl_seqno = ? "
            + " and A.bin_type = ? ";

      sqlSelect(sql1, new Object[]{aaCtrlSeqno[rr], aaBinType[rr]});
      String ls_card_no = sqlStr("card_no");
      String ls_film_no = sqlStr("film_no");

      if (eqIgno(aaBinType[rr], "V")) {
      	if(liCnt==1 && empty(isPrintForm))	isPrintForm = aaForm[rr];
         ii++;         
         wp.colSet(ii, "ex_card16", ls_card_no);
         wp.colSet(ii, "ex_mcht_eng_name", sqlStr("mcht_eng_name"));
         wp.colSet(ii, "ex_film_no", ls_film_no);
         wp.colSet(ii, "ex_seqno", sqlStr("ctrl_seqno"));
         wp.colSet(ii, "ex_pyyyy", commString.mid(sqlStr("purchase_date"), 4, 4) + commString.mid(sqlStr("purchase_date"), 0, 4));
         wp.colSet(ii, "ex_film_no6", commString.mid(ls_film_no, 1, 6));
         wp.colSet(ii, "ex_card6", commString.mid(ls_card_no, 0, 6));
         wp.colSet(ii, "ex_s_currency", sqlStr("source_curr"));
         wp.colSet(ii, "ex_s_amount", sqlStr("source_amt"));
         wp.colSet(ii, "ex_ncc_code", sqlStr("mcht_category"));
         wp.colSet(ii, "ex_proc_date", commString.mid(sqlStr("process_date"), 4, 4) + commString.mid(sqlStr("process_date"), 0, 4));

         if (!empty(sqlStr("v_card_no"))) {
            wp.colSet(ii, "ex_card16", sqlStr("v_card_no"));
         }
         continue;
      }

      ii++;
      wp.colSet(ii, "ex_seq", "" + (ii + 1));
      wp.colSet(ii, "ex_card16", sqlStr("card_no"));
      wp.colSet(ii, "ex_sign", "+");
      if (eqIgno(sqlStr("txn_code"), "05")) {
         wp.colSet(ii, "ex_trans1", "v");
      }
      else if (eqIgno(sqlStr("txn_code"), "06")) {
         wp.colSet(ii, "ex_trans2", "v");
         wp.colSet(ii, "ex_sign", "-");
      }
      else {
         wp.colSet(ii, "ex_trans3", "v");
      }

      if (eqIgno(sqlStr("chg_times"), "1")) {
         wp.colSet(ii, "ex_stage1", "v");
      }
      else {
         wp.colSet(ii, "ex_stage2", "v");
      }
      wp.colSet(ii, "ex_name", sqlStr("db_idno_name"));
      wp.colSet(ii, "ex_mcht_eng_name", sqlStr("mcht_eng_name"));
      wp.colSet(ii, "ex_film_no", ls_film_no);
      wp.colSet(ii, "ex_reason_code", sqlStr("fst_reason_code"));
      wp.colSet(ii, "ex_seqno", sqlStr("ctrl_seqno"));
      wp.colSet(ii, "ex_yy", commString.mid(zzdate.twDate(), 0, 3));
      wp.colSet(ii, "ex_yyyy", commString.mid(this.getSysDate(), 0, 4));
      wp.colSet(ii, "ex_mm", commString.mid(this.getSysDate(), 4, 2));
      wp.colSet(ii, "ex_dd", commString.mid(this.getSysDate(), 6, 2));
      wp.colSet(ii, "ex_yymmdd", commString.mid(zzdate.twDate(), 0, 3) + " 年 " + commString.mid(this.getSysDate(), 4, 2) + " 月 " + commString.mid(this.getSysDate(), 6, 2) + " 日");
      if (eqIgno(aaBinType[rr], "J")) {
      	String ls_reason_code = commString.lpad(sqlStr("fst_reason_code"), 4, "0");
         wp.colSet(ii, "ex_pyyyy", commString.mid(sqlStr("purchase_date"), 0, 4));
         wp.colSet(ii, "ex_pmm", commString.mid(sqlStr("purchase_date"), 4, 2));
         wp.colSet(ii, "ex_pdd", commString.mid(sqlStr("purchase_date"), 6, 2));
         wp.colSet(ii, "ex_pryyyy", commString.mid(sqlStr("fst_apr_date"), 0, 4));
         wp.colSet(ii, "ex_prmm", commString.mid(sqlStr("fst_apr_date"), 4, 2));
         wp.colSet(ii, "ex_prdd", commString.mid(sqlStr("fst_apr_date"), 6, 2));
         wp.colSet(ii, "ex_s_currency", sqlStr("source_curr"));
         wp.colSet(ii, "ex_s_amount", sqlStr("source_amt"));
         wp.colSet(ii, "ex_d_currency", sqlStr("dest_curr"));
         wp.colSet(ii, "ex_d_amount", sqlStr("fst_twd_amt"));
         wp.colSet(ii, "ex_reason_code", ls_reason_code);
         
         //--reason code v
         String[] ls_reason = {"0501", "0502", "0503", "0507", "0508",
               "0510", "0512", "0513", "0516", "0517",
               "0521", "0522", "0523", "0524", "0525",
               "0526", "0527", "0534", "0535", "0536",
               "0537", "0538", "0541", "0544", "0546",
               "0547", "0554", "0580", "0581", "0582",
               "0583"};
         for (int zz = 0; zz < 31; zz++) {
            if (eqIgno(ls_reason_code, ls_reason[zz])) {
               wp.colSet(ii, "V" + ls_reason[zz], "(v)");
               continue;
            }
            wp.colSet(ii, "V" + ls_reason[zz], "( )");
         }

      }
      else {
         wp.colSet(ii, "ex_mcht_chi_name", sqlStr("mcht_chi_name"));
         wp.colSet(ii, "ex_fee", sqlStr("dest_amt"));
         wp.colSet(ii, "ex_msg", sqlStr("fst_msg"));
         if (zzdate.toTwDate(sqlStr("purchase_date")).length() == 6) {
            wp.colSet(ii, "ex_pyy", commString.mid(zzdate.toTwDate(sqlStr("purchase_date")), 0, 2));
         }
         else {
            wp.colSet(ii, "ex_pyy", commString.mid(zzdate.toTwDate(sqlStr("purchase_date")), 0, 3));
         }
         wp.colSet(ii, "ex_pmm", commString.mid(sqlStr("purchase_date"), 4, 2));
         wp.colSet(ii, "ex_pdd", commString.mid(sqlStr("purchase_date"), 6, 2));
      }

      if (!empty(sqlStr("v_card_no"))) {
         wp.colSet(ii, "ex_card16", sqlStr("v_card_no"));
      }
   }

   if (liCnt <= 0) {
      alertErr("請選擇列印項目");
      wp.respHtml = errPagePDF;
      return;
   }
   alertMsg("列印筆數:"+liCnt+" 跳過筆數:"+li_continue);
   wp.listCount[1] = liCnt;
   pdfPrint();

//   wp.listCount[0] =wp.item_rows("ctrl_seqno");
}

int updateRskChgback(String a_rowid) throws Exception {
   msgOK();
   String sql1 = "update rsk_chgback set "
         + " fst_doc_mark = 'Y' "
         +commSqlStr.modxxxSet(wp.loginUser,wp.modPgm())
         + " where rowid =? ";

   setRowid(1, a_rowid);
   sqlExec(sql1);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgbck error !");
   }
   return rc;
}

}
