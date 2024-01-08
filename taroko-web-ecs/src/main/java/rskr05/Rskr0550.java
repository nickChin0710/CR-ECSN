package rskr05;
/**
 * 2023-1023   JH    調整報表格式
 * 2023-1019   JH    join crd_corp by corp_p_seqno or corp_no
 * 2023-0927   JH    ++annou_log.id_no,corp_no
 * 2019-1126   JH    UAT-modify
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 * 2019-1119:  Alex  remove table rsk_p550r7 , change query read table , pdfPrint , dataPrint
 * 2919-1121:  Alex  init user and tel
 * 109-04-28  V1.00.04  Tanwei       updated for project coding standard
 * 110-01-05  V1.00.05  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0550 extends BaseAction implements InfacePdf {
taroko.base.CommDate commDate = new taroko.base.CommDate();

String lsWhere = "", wkTel = "", wkUser = "", wkSysDate = "";

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
   } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
   } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
   } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
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
   if (itemallEmpty("ex_tx_date1,ex_tx_date2,ex_idno,ex_corp_no")) {
      alertErr2("[作業日期, 身分證ID,公司統編] 不可同時空白");
      return;
   }


   if (this.chkStrend(wp.itemStr("ex_tx_date1"), wp.itemStr("ex_tx_date2")) == false) {
      alertErr2("作業日期起迄：輸入錯誤");
      return;
   }

   String lsWhere = " where 1=1 and A.kind_flag ='A' "
       +sqlCol(wp.itemStr("ex_tx_date1"), "A.crt_date", ">=")
       +sqlCol(wp.itemStr("ex_tx_date2"), "A.crt_date", "<=");

   if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
          +sqlCol(wp.itemStr("ex_idno"), "id_no")+" )";
   }

   if (wp.itemEmpty("ex_corp_no") == false) {
      lsWhere += " and A.corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1 "
          +sqlCol(wp.itemStr("ex_corp_no"), "corp_no")+" )";
   }

   if (wp.itemEq("ex_block_flag", "0") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_block_flag"), "A.block_flag");
   }

   if (eqIgno(wp.itemStr("ex_annou_type"), "0") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_annou_type"), "A.annou_type");
   }

   if (wp.itemEq("ex_card_cnt", "Y")) {
      lsWhere += " and A.sup0_card_num + A.sup1_card_num + A.corp_card_num > 0 ";
   } else if (wp.itemEq("ex_card_cnt", "N")) {
      lsWhere += " and A.sup0_card_num + A.sup1_card_num + A.corp_card_num = 0 ";
   }

   if (wp.itemEq("ex_print_flag", "0") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_print_flag"), "A.print_flag");
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " distinct A.crt_date "
       +", A.acct_type, A.id_p_seqno , A.corp_p_seqno "
       +", A.proc_reason, A.block_flag, B.bill_sending_zip "
       +", B.bill_sending_addr1, B.bill_sending_addr2, B.bill_sending_addr3 "
       +", B.bill_sending_addr4, B.bill_sending_addr5 "
       +", decode(substrb(A.from_type,1,1),'3','2',substrb(A.from_type,1,1)) as db_from_type "
       +", C.chi_name as chi_name "  //uf_idno_name(C.id_p_seqno) as chi_name , "
       +", C.id_no as id_no "  //uf_idno_id(C.id_p_seqno) as id_no "
       +", A.card_no , A.annou_type "
       +", (A.sup0_card_num + A.sup1_card_num + A.corp_card_num) as card_cnt "
       +", A.print_flag "
       +", A.id_no as bad_id_no, A.corp_no as bad_corp_no "
       +", hex(A.rowid) as rowid "
   +", D.corp_no, D.charge_name, D.chi_name as corp_name "
   +", D.comm_zip, D.comm_addr1, D.comm_addr2"
       +", D.comm_addr3, D.comm_addr4, D.comm_addr5 "
   ;
   wp.daoTable = " rsk_bad_annou_log A "
       +" join act_acno B on A.acno_p_seqno = B.acno_p_seqno "
       +" left join crd_idno C on B.id_p_seqno = C.id_p_seqno "
//   +" left join crd_corp D on D.corp_p_seqno=A.corp_p_seqno and D.corp_p_seqno<>'' "
       +" left join crd_corp D on (D.corp_p_seqno=A.corp_p_seqno OR D.corp_no=A.corp_no) "
   ;
   wp.whereOrder = " order by 1 , 2 , 4 , 3 ";
   pageQuery();

   if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }
   wp.setListCount(0);
   wp.setPageValue();
   queryAfter();
}

void queryAfter() {
   int rr = 0;
   String ss = "", idCorp = "";

   rr = wp.selectCnt;
   for (int ii = 0; ii < rr; ii++) {
      idCorp = "";
      if (wp.colEmpty(ii, "id_no") == false) {
         idCorp = wp.colStr(ii, "id_no");
      }

      if (wp.colEmpty(ii, "corp_no") == false) {
         if (empty(idCorp))
            idCorp = wp.colStr(ii, "corp_no");
         else
            idCorp += " / "+wp.colStr(ii, "corp_no");
      }

      wp.colSet(ii, "wk_id_corp", idCorp);

      ss = "";
      if (eqIgno(wp.colStr(ii, "proc_reason"), "E2")) {
         ss = "他行強停";
      } else if (eqIgno(wp.colStr(ii, "proc_reason"), "38")) {
         ss = "退票";
      } else if (eqIgno(wp.colStr(ii, "proc_reason"), "F1")) {
         ss = "支票拒往";
      } else if (eqIgno(wp.colStr(ii, "proc_reason"), "H1")) {
         ss = "轉催收戶";
      }
      String addr1 = "", addr2 = "";
      addr1 = wp.colStr(ii, "bill_sending_addr1")+wp.colStr(ii, "bill_sending_addr2")+wp.colStr(ii, "bill_sending_addr3");
      addr2 = wp.colStr(ii, "bill_sending_addr4")+wp.colStr(ii, "bill_sending_addr5");
      if (empty(addr1) && empty(addr2)) {
         //-公司通訊地址---
         wp.colSet(ii,"bill_sending_zip", wp.colStr(ii,"comm_zip"));
         addr1 = wp.colStr(ii, "comm_addr1")+wp.colStr(ii, "comm_addr2")+wp.colStr(ii, "comm_addr3");
         addr2 = wp.colStr(ii, "comm_addr4")+wp.colStr(ii, "comm_addr5");
      }
      wp.colSet(ii, "addr1", addr1);
      wp.colSet(ii, "addr2", addr2);

      wp.colSet(ii, "tx_rsn", ss);
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

}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   //--暫時先放 XXX 等確定後再修改
   wp.colSet("ex_tel", "0000");
   wp.colSet("ex_user", "XXX");
}

@Override
public void pdfPrint() throws Exception {
   wp.reportId = "rskr0550";
   dataPrint();
   if (wp.listCount[0] == 0) {
      alertErr2("請選擇要列印資料");
      wp.respHtml = "TarokoErrorPDF";
      return;
   }
   TarokoPDF pdf = new TarokoPDF();
   pdf.pageVert = false;

   wp.fileMode = "Y";
   pdf.excelTemplate = "rskr0550.xlsx";
   pdf.pageCount = 1;
   pdf.sheetNo = 0;
   pdf.pageVert = true;
   pdf.procesPDFreport(wp);
   pdf = null;
   sqlCommit(1);
   //--
   wp.listCount[0] = wp.itemRows("id_no");
   return;

}

void dataPrint() throws Exception {
   selectUser();

   String[] aaOpt = wp.itemBuff("opt");
   String[] lsIdno = wp.itemBuff("id_no");
   String[] lsAcctType = wp.itemBuff("acct_type");
   String[] lsChiName = wp.itemBuff("chi_name");
   String[] lsChargeName = wp.itemBuff("charge_name");
   String[] lsAnnouType = wp.itemBuff("annou_type");
   String[] lsAddrZip = wp.itemBuff("bill_sending_zip");
   String[] lsAddr1 = wp.itemBuff("addr1");
   String[] lsAddr2 = wp.itemBuff("addr2");
   String[] lsCorpName = wp.itemBuff("corp_name");
   String[] lsRowid = wp.itemBuff("rowid");
   String[] lsCrtDate = wp.itemBuff("crt_date");

   wp.listCount[0] = wp.itemRows("id_no");
   int zz = 0;
   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = (int) optToIndex(aaOpt[ii]);
      if (rr < 0) continue;

      getTwDate(lsCrtDate[rr]);
      wp.colSet(zz, "ex_idno", lsIdno[rr]);
      wp.colSet(zz, "ex_acct_type", lsAcctType[rr]);
      String ls_badCorp=wp.itemStr(rr,"bad_corp_no");
      String ls_corpPseqno =wp.itemStr(rr, "corp_p_seqno");
      String ls_idPseqno =wp.itemStr(rr, "id_p_seqno");
      //-6.退票-
      if (eqIgno("6",lsAnnouType[rr])) {
         if (notEmpty(ls_badCorp)) {
            if (empty(lsIdno[rr])) {
               //--公司戶負責人退票信函.pdf--
               wp.colSet(zz, "ex_chi_name1", lsCorpName[rr]);
               wp.colSet(zz, "ex_chi_name2", lsChargeName[rr]+"　先生/小姐");
               wp.colSet(zz, "ex_tx_rsn_1", "");
               wp.colSet(zz, "ex_tx_rsn_2", "　　茲因貴公司因存款不足退票，依本行商務卡約定");
               wp.colSet(zz, "ex_tx_rsn_3", "條款第二十二條第二項第四款約定，本行自即日起停");
               wp.colSet(zz, "ex_tx_rsn_4", "止貴公司使用本行商務卡之權利，特此通知。");
               wp.colSet(zz, "ex_name1", "　　　"+lsCorpName[rr]);
               wp.colSet(zz, "ex_name2", "　　　法定代理人　"+lsChargeName[rr]);
            } else {
               //--公司戶負責人個人卡退票信函.pdf--
               wp.colSet(zz, "ex_chi_name1", lsChiName[rr]+"　先生/小姐");
               wp.colSet(zz, "ex_tx_rsn_1", "");
               wp.colSet(zz, "ex_tx_rsn_2", "　　台端因　"+lsCorpName[rr]+"");
               wp.colSet(zz, "ex_tx_rsn_3", "存款不足遭退票註記，依本行個人信用卡約定條款第");
               wp.colSet(zz, "ex_tx_rsn_4", "二十一條第二項第四款約定，即日起暫停台端使用本");
               wp.colSet(zz, "ex_tx_rsn_5", "行信用卡之權利，特此通知。");
               wp.colSet(zz, "ex_name1", "　　　"+lsChiName[rr]+"　君");
            }
         }
         else {
            //--個人戶退票信函.pdf--
            wp.colSet(zz, "ex_chi_name1", lsChiName[rr]+"　先生/小姐");
            wp.colSet(zz, "ex_tx_rsn_1", "");
            wp.colSet(zz, "ex_tx_rsn_2", "");
            wp.colSet(zz, "ex_tx_rsn_3", "　　台端因存款不足退票註記，依本行信用卡約定條款之約");
            wp.colSet(zz, "ex_tx_rsn_4", "定，即日起暫停台端使用本行信用卡之權利，特此通知。");
            wp.colSet(zz, "ex_tx_rsn_5", "");
            wp.colSet(zz, "ex_name1", "　　　"+lsChiName[rr]+"　君");
         }
      }
      else if (eqIgno("1",lsAnnouType[rr])) {
         //1.他行強停--
         wp.colSet(zz, "ex_chi_name1", lsChiName[rr]+"　先生/小姐");
         wp.colSet(zz, "ex_tx_rsn_1", "　　台端因信用卡遭同業強制停卡，依本行個人信用卡");
         wp.colSet(zz, "ex_tx_rsn_2", "約定條款第二十一條信用卡使用之限制，即日起停止您");
         wp.colSet(zz, "ex_tx_rsn_3", "使用本行信用卡之權利。另依第二十二條約定台端已喪");
         wp.colSet(zz, "ex_tx_rsn_4", "失期限利益，延後付款期限視為全部到期，不再享有繳");
         wp.colSet(zz, "ex_tx_rsn_5", "納最低應繳金額權利，請於下期帳單寄至後一次繳清，");
         wp.colSet(zz, "ex_tx_rsn_6", "特此通知。");
         wp.colSet(zz, "ex_name1", "　　　"+lsChiName[rr]+"　君");
      }
      else if (eqIgno("2",lsAnnouType[rr])) {
         //2.支票拒往--
         if (!empty(ls_badCorp)) {
            //-公司戶負責人票交拒往信函.pdf--
            if (empty(lsIdno[rr])) {
               wp.colSet(zz, "ex_chi_name1", lsCorpName[rr]);
               wp.colSet(zz, "ex_chi_name2", lsChargeName[rr]+"　先生/小姐");
               wp.colSet(zz, "ex_tx_rsn_1", "　　茲因貴公司票交拒往，依本行商務卡約定條款第");
               wp.colSet(zz, "ex_tx_rsn_2", "二十二條第一項第四款約定，本行自即日起停止貴公");
               wp.colSet(zz, "ex_tx_rsn_3", "司使用本行商務卡之權利。另依第二十三條約定，本");
               wp.colSet(zz, "ex_tx_rsn_4", "行主張貴公司已喪失期限利益，延後付款期限視為全");
               wp.colSet(zz, "ex_tx_rsn_5", "部到期，貴公司不再享有繳納最低應繳金額權利，請");
               wp.colSet(zz, "ex_tx_rsn_6", "於下期帳單寄達後一次繳清全部帳款為荷，特此通知。");
               wp.colSet(zz, "ex_name1", "　　　"+lsCorpName[rr]);
               wp.colSet(zz, "ex_name2", "　　　法定代理人　"+lsChargeName[rr]);
            }
            else {
               //-公司戶負責人個人卡票交拒往信函.pdf--
               wp.colSet(zz, "ex_chi_name1", lsChiName[rr]+"　先生/小姐");
               wp.colSet(zz, "ex_tx_rsn_1", "　　台端因　"+lsCorpName[rr]);
               wp.colSet(zz, "ex_tx_rsn_2", "票交拒往，依本行個人信用卡約定條款第二十一條第");
               wp.colSet(zz, "ex_tx_rsn_3", "一項第五款約定，本行自即日起停止台端使用本行信");
               wp.colSet(zz, "ex_tx_rsn_4", "用卡之權利。另依第二十二條約定，本行主張台端已");
               wp.colSet(zz, "ex_tx_rsn_5", "喪失期限利益，延後付款期限視為全部到期，台端不");
               wp.colSet(zz, "ex_tx_rsn_6", "再享有繳納最低應繳金額權利，請於下期帳單寄達後");
               wp.colSet(zz, "ex_tx_rsn_7", "一次繳清全部帳款為荷，特此通知。");
               wp.colSet(zz, "ex_name1", "　　　"+lsChiName[rr]+"　君");
            }
         }
         else {
            //-個人戶票交拒往信函.pdf--
            wp.colSet(zz, "ex_chi_name1", lsChiName[rr]+"　先生/小姐");
            wp.colSet(zz, "ex_tx_rsn_1", "　　台端信用卡因票交拒往，依本行個人信用卡約定");
            wp.colSet(zz, "ex_tx_rsn_2", "條款第二十一條信用卡使用之限制，即日起停止您使");
            wp.colSet(zz, "ex_tx_rsn_3", "用本行信用卡之權利。另依第二十二條約定台端已喪");
            wp.colSet(zz, "ex_tx_rsn_4", "失期限利益，延後付款期限視為全部到期，不再享有");
            wp.colSet(zz, "ex_tx_rsn_5", "繳納最低應繳金額權利，請於下期帳單寄至後一次繳");
            wp.colSet(zz, "ex_tx_rsn_6", "清，特此通知。");
            wp.colSet(zz, "ex_name1", "　　　"+lsChiName[rr]+"　君");
         }
      }
      else if (eqIgno("9",lsAnnouType[rr])) {
         //-9.轉催停用-
         wp.colSet(zz, "ex_chi_name1", lsChiName[rr]+"　先生/小姐");
         wp.colSet(zz, "ex_tx_rsn_1", "");
         wp.colSet(zz, "ex_tx_rsn_2", "　　台端因遭本行轉為催收戶，依信用卡約定條");
         wp.colSet(zz, "ex_tx_rsn_3", "款之約定，即日起已停止您使用本行信用卡之權利，");
         wp.colSet(zz, "ex_tx_rsn_4", "特此通知。");
         wp.colSet(zz, "ex_name1", "　　　"+lsChiName[rr]+"　君");
      }

      wp.colSet(zz, "ex_temp1", "　　順　　　　　頌");
      wp.colSet(zz, "ex_temp2", "　安　　　　　祺");
      wp.colSet(zz, "ex_addr_zip", lsAddrZip[rr]);
      wp.colSet(zz, "ex_addr1", lsAddr1[rr]);
      wp.colSet(zz, "ex_addr2", lsAddr2[rr]);
      wp.colSet(zz, "wk_sys_date", wkSysDate);
      zz++;
      updateLog(lsRowid[rr]);
   }
   wp.listCount[0] = zz;
}

void selectUser() {
   String lsTel = "", lsUser = "";
   lsTel = wp.itemStr("ex_tel");
   lsUser = wp.itemStr("ex_user");
   if (!empty(lsTel))
      wkTel = lsTel;
   if (!empty(lsUser)) {
      wkUser = lsUser;
      return;
   }

   String sql1 = " select "+" wf_value as ls_user , "+" wf_value2 as ls_tel "
       +" from ptr_sys_parm "+" where wf_parm = 'SYSPARM' "+" and wf_key = 'COLR5550' ";
   sqlSelect(sql1);
   if (empty(lsTel))
      lsTel = sqlStr("ls_tel");
   if (empty(lsUser))
      lsUser = sqlStr("ls_user");
   wkTel = lsTel;
   wkUser = lsUser;
   return;
}

void getTwDate(String aDate) {
   String lsDate = "", lsMonth = "", lsDay = "";
   lsDate = aDate;
   lsDate = commDate.toTwDate(lsDate);
   lsMonth = lsDate.substring(3, 5);
   lsDay = lsDate.substring(5, 7);
   wkSysDate = "中　　　華　　　民　　　國　　"+lsDate.substring(0, 3)+"　 年 　"+commString.strToInt(lsMonth)+"　 月　"+commString.strToInt(lsDay)+"　日";
//    wkSysDate = "中　　　華　　　民　　　國　　" + lsDate.substring(0, 3) + "　 年 　" + lsDate.substring(3, 5)
//        + "　 月　" + lsDate.substring(5, 7) + "　日";
}

void updateLog(String aRowid) throws Exception {
   String sql1 = "";

   sql1 = " update rsk_bad_annou_log set print_flag ='Y' ";
   sql1 += " where 1=1"+commSqlStr.whereRowid;
   setString(1, aRowid);
   sqlExec(sql1);
}

}
