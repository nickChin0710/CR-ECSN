package rskr03;
/**
 * 2020-0929   JH    rsk_ctfg_mail_reg.Insert
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.Parm2Sql;
import taroko.com.TarokoPDF;

public class Rskr3130 extends BaseAction implements InfacePdf {
String isCardNo = "", isFindType = "", isCname = "", isIdno = "", isZip = "", isLetterType = "",
      isAddr1 = "", isAddr2 = "", isAddr3 = "", isAddr4 = "", isAddr5 = "", isCtfgSeqno = "";

@Override
public void userAction() throws Exception {
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      checkUpdate();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }
   else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   }
   else if (eqIgno(wp.buttonCode, "XLS")) {   //-Excel-
      strAction = "XLS";
//			xlsPrint();
   }
   else if (eqIgno(wp.buttonCode, "PDF")) {   //-PDF-
      strAction = "PDF";
      printData();
   }

}

@Override
public void dddwSelect() {
   // TODO Auto-generated method stub

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_proc_date1"), wp.itemStr("ex_proc_date2")) == false) {
      alertErr("處理日期 起迄錯誤!");
      return;
   }
/*		
		if(empty(wp.itemStr("ex_card_no")) && empty(wp.itemStr("ex_idno"))){
			alertErr("卡號 , 身分證ID 不可同時空白 !");
			return;
		}
*/
   String lsWhere = " where A.ctfg_seqno = B.ctfg_seqno ";
   
   lsWhere += sqlCol(wp.itemStr("ex_proc_date1"),"A.proc_date",">=")
		   + sqlCol(wp.itemStr("ex_proc_date2"),"A.proc_date","<=")
		   ;   

   if (!empty(wp.itemStr("ex_card_no"))) {
      lsWhere += sqlCol(wp.itemStr("ex_card_no"), "A.card_no", "like%");
   }
   else if (!wp.itemEmpty("ex_idno")) {
      lsWhere += " and A.id_p_seqno in "
            + " (select id_p_seqno from crd_idno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"id_no")
            + " union all "
            + " select id_p_seqno from dbc_idno where 1=1 " +sqlCol(wp.itemStr("ex_idno"),"id_no")+") "
      ;
   }

   if (eqIgno(wp.itemStr("ex_report_type"), "1")) {
      lsWhere += " and B.find_type ='換卡' ";
   }
   else {
      lsWhere += " and B.find_type <>'換卡' ";
   }

   lsWhere += " and nvl(B.rels_code,'')='' "
         + " and A.proc_date >= B.warn_date "
         + " and A.proc_type ='郵寄' "
         + " and nvl(A.mail_date,'')='' ";

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();


}

@Override
public void queryRead() throws Exception {
   wp.selectSQL = ""
         + " A.card_no ,"
         + " A.proc_date ,"
         + " A.ctfg_seqno ,"
         + " A.mail_date ,"
         + " B.find_type ,"
         + " '' as db_idno ,"
         + " '' as db_id_cname ,"
         + " '' as db_zip_code ,"
         + " '' as db_addr1 ,"
         + " '' as db_addr2 ,"
         + " '' as db_addr3 ,"
         + " '' as db_addr4 ,"
         + " '' as db_addr5 ,"
         + " hex(A.rowid) as rowid "
   ;
   wp.daoTable = " rsk_ctfg_proc A, rsk_ctfg_mast B ";

   pageQuery();
   if (sqlRowNum <= 0) {
      alertErr("查無資料");
      return;
   }
   queryAfter();
   wp.setListCount(0);

}

void queryAfter() throws Exception {
   for (int ii = 0; ii < wp.selectCnt; ii++) {

      String sql1 = " select "
            + " b.id_no , "
            + " b.chi_name , "
            + " a.bill_sending_zip , "
            + " a.bill_sending_addr1 , "
            + " a.bill_sending_addr2 , "
            + " a.bill_sending_addr3 , "
            + " a.bill_sending_addr4 , "
            + " a.bill_sending_addr5 "
            + " from crd_idno b, act_acno a, crd_card c "
            + " where b.id_p_seqno = c.id_p_seqno "
            + " and a.acno_p_seqno = c.acno_p_seqno "
            + " and c.card_no = ? ";
      sqlSelect(sql1, new Object[]{wp.colStr(ii, "card_no")});

      if (sqlRowNum <= 0) {
         String sql2 = " select "
               + " b.id_no , "
               + " b.chi_name , "
               + " a.bill_sending_zip , "
               + " a.bill_sending_addr1 , "
               + " a.bill_sending_addr2 , "
               + " a.bill_sending_addr3 , "
               + " a.bill_sending_addr4 , "
               + " a.bill_sending_addr5 "
               + " from dbc_idno b, dba_acno a, dbc_card c "
               + " where b.id_p_seqno = c.id_p_seqno "
               + " and a.p_seqno = c.p_seqno "
               + " and c.card_no = ? ";
         sqlSelect(sql2, new Object[]{wp.colStr(ii, "card_no")});
         if (sqlRowNum <= 0) continue;
      }

      wp.colSet(ii, "db_idno", sqlStr("id_no"));
      wp.colSet(ii, "db_id_cname", sqlStr("chi_name"));
      wp.colSet(ii, "db_zip_code", sqlStr("bill_sending_zip"));
      wp.colSet(ii, "db_addr1", sqlStr("bill_sending_addr1"));
      wp.colSet(ii, "db_addr2", sqlStr("bill_sending_addr2"));
      wp.colSet(ii, "db_addr3", sqlStr("bill_sending_addr3"));
      wp.colSet(ii, "db_addr4", sqlStr("bill_sending_addr4"));
      wp.colSet(ii, "db_addr5", sqlStr("bill_sending_addr5"));

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
   wp.reportId = "Rskr3130";
   wp.pageRows = 9999;
   TarokoPDF pdf = new TarokoPDF();
   wp.fileMode = "Y";
   if (eqIgno(wp.itemStr("ex_report_type"), "1")) {
      pdf.excelTemplate = "rskr3130_1.xlsx";
   }
   else {
      pdf.excelTemplate = "rskr3130_2.xlsx";
   }
   pdf.pageCount = 1;
   pdf.sheetNo = 0;
   pdf.pageVert = true;
   pdf.procesPDFreport(wp);
   pdf = null;
   return;

}

void printData() throws Exception {
   String[] lsCardNo = wp.itemBuff("card_no");
   String[] lsCtfgSeqno = wp.itemBuff("ctfg_seqno");
   String[] lsProcDate = wp.itemBuff("proc_date");
   String[] lsFindType = wp.itemBuff("find_type");
   String[] lsMailDate = wp.itemBuff("mail_date");
   String[] lsDbIdno = wp.itemBuff("db_idno");
   String[] lsDbIdCname = wp.itemBuff("db_id_cname");
   String[] lsDbZipCode = wp.itemBuff("db_zip_code");
   String[] lsDbAddr1 = wp.itemBuff("db_addr1");
   String[] lsDbAddr2 = wp.itemBuff("db_addr2");
   String[] lsDbAddr3 = wp.itemBuff("db_addr3");
   String[] lsDbAddr4 = wp.itemBuff("db_addr4");
   String[] lsDbAddr5 = wp.itemBuff("db_addr5");
   String[] ls_opt = wp.itemBuff("opt");
   wp.listCount[0] = lsCardNo.length;
   int ii = -1;
   for (int rr = 0; rr < lsCardNo.length; rr++) {
      if (!checkBoxOptOn(rr, ls_opt)) continue;
      ii++;
      wp.colSet(ii, "ex_ctfg_seqno", lsCtfgSeqno[rr]);
      wp.colSet(ii, "ex_zip_code", lsDbZipCode[rr]);
      wp.colSet(ii, "ex_addr", lsDbAddr1[rr] + lsDbAddr2[rr] + lsDbAddr3[rr]);
      wp.colSet(ii, "ex_addr2", lsDbAddr4[rr] + lsDbAddr5[rr]);
      wp.colSet(ii, "ex_id_cname", lsDbIdCname[rr]);
      wp.colSet(ii, "ex_proc_date", wp.sysDate);
//			if(eqIgno(wp.itemStr("ex_report_type"),"1")){
//				wp.colSet(ii,"report_type", "1");
//			}	else	{
//				wp.colSet(ii,"report_type", "2");
//			}
   }

   if (ii < 0) {
      alertErr("請選擇列印項目");
      wp.respHtml = "TarokoErrorPDF";
      return;
   }
   wp.listCount[0] = (ii + 1);
   pdfPrint();
}

void checkUpdate() throws Exception {
   int ll_ok = 0, ll_err = 0;
   String lsReportType = "";
   String[] lsCardNo = wp.itemBuff("card_no");
   String[] lsCtfgSeqno = wp.itemBuff("ctfg_seqno");
   String[] lsFindType = wp.itemBuff("find_type");
   String[] lsDbIdno = wp.itemBuff("db_idno");
   String[] lsDbIdCname = wp.itemBuff("db_id_cname");
   String[] lsDbZipCode = wp.itemBuff("db_zip_code");
   String[] lsDbAddr1 = wp.itemBuff("db_addr1");
   String[] lsDbAddr2 = wp.itemBuff("db_addr2");
   String[] lsDbAddr3 = wp.itemBuff("db_addr3");
   String[] lsDbAddr4 = wp.itemBuff("db_addr4");
   String[] lsDbAddr5 = wp.itemBuff("db_addr5");
   String[] lsRowid = wp.itemBuff("rowid");
   lsReportType = wp.itemStr("ex_report_type");
   String[] aaOpt = wp.itemBuff("opt");
   wp.listCount[0] = lsCardNo.length;
   if (optToIndex(aaOpt[0]) < 0) {
      alertErr("請點選欲處理資料");
      return;
   }
   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0) continue;

      optOkflag(rr);
      int li_rc = updateData(lsRowid[rr]);
      if (li_rc < 0) {
         ll_err++;
         this.dbRollback();
         wp.colSet(rr, "ok_flag", "X");
//            optOkflag(rr,li_rc);
         continue;
      }

      isCardNo = lsCardNo[rr];
      isFindType = lsFindType[rr];
      isCname = lsDbIdCname[rr];
      isIdno = lsDbIdno[rr];
      isZip = lsDbZipCode[rr];
      isAddr1 = lsDbAddr1[rr];
      isAddr2 = lsDbAddr2[rr];
      isAddr3 = lsDbAddr3[rr];
      isAddr4 = lsDbAddr4[rr];
      isAddr5 = lsDbAddr5[rr];
      isCtfgSeqno = lsCtfgSeqno[rr];
      isLetterType = lsReportType;

      li_rc = insertData();
      optOkflag(rr, li_rc);
      if (li_rc == -0) {
         ll_err++;
         this.dbRollback();
         wp.colSet(rr, "ok_flag", "X");
         continue;
      }

      sqlCommit(li_rc);
      ll_ok++;
      wp.colSet(rr, "ok_flag", "V");
      continue;
   }

   alertMsg("列印確認完成 : OK:" + ll_ok + " ERR:" + ll_err);

}

int updateData(String ex_rowid) throws Exception  {
   msgOK();
   String sql1 = " update rsk_ctfg_proc set "
         + " mail_date = to_char(sysdate,'yyyymmdd') , "
         + " mod_time = sysdate , "
         + " mod_user =:mod_user , "
         + " mod_seqno = nvl(mod_seqno,0)+1 "
         + " where rowid =:rowid ";
   setString("mod_user", wp.loginUser);
   setRowid("rowid", ex_rowid);
   sqlExec(sql1);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_ctfg_proc error !");
      return -1;
   }
   return 1;
}

int insertData() throws Exception  {

   Parm2Sql tt=new Parm2Sql();
   tt.insert("rsk_ctfg_mail_reg");
   tt.parmYmd("crt_date");
   tt.parmTime("crt_time");
   tt.parmSet("card_no", isCardNo);
   tt.parmSet("ctfg_seqno", isCtfgSeqno);
   tt.parmSet("find_type", isFindType);
   tt.parmSet("mail_flag", "1");
   tt.parmSet("send_flag", "1");
   tt.parmYmd("send_date");
   tt.parmTime("send_time");
   tt.parmSet("chi_name", isCname);
   tt.parmSet("id_no", isIdno);
   tt.parmSet("zip_code", isZip);
   tt.parmSet("addr1", isAddr1);
   tt.parmSet("addr2", isAddr2);
   tt.parmSet("addr3", isAddr3);
   tt.parmSet("addr4", isAddr4);
   tt.parmSet("addr5", isAddr5);
   tt.parmSet("letter_type", isLetterType);
   tt.parmSet("mod_user", wp.loginUser);
   tt.parmDtime("mod_time");
   tt.parmSet("mod_pgm", wp.modPgm());

   sqlExec(tt.getSql(),tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert rsk_ctfg_mail_reg error !");
      return -1;
   }

   return 1;
}

}
