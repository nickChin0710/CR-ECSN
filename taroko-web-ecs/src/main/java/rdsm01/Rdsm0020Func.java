/**
 * 2023-0814   JH    rd_validdate=YYYYMM
 * 109-04-21  V1.00.00  Tanwei       updated for project coding standard      *
 * 109-11-27  V1.00.01   Justin         remove 驗證正卡
 * 109-12-01  V1.00.02   Justin         修改wfChkRoadfree訊息
 * 110-01-05  V1.00.03  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *
 ******************************************************************************/
package rdsm01;

import busi.FuncAction;
import taroko.base.Parm2Sql;

public class Rdsm0020Func extends FuncAction {
taroko.base.CommDate commDate = new taroko.base.CommDate();
private final String PGM = "rdsm0020";
public String isMsg = "";
// String kk1 = "", kk2 = "", kk3 = "", kk4 = "";
int isSeqno = 0;
boolean ibAuto = false;

RdsFunc ooFunc = new RdsFunc();
String lsRdSts = "", ss = "";
int liRc = 0;

@Override
public void dataCheck() {
   String lsCardNo =wp.itemStr("appl_card_no");

   if (ibAdd) {
      if (checkCardNo() == false) {
         return;
      }
      if (checkIdNo() == false) {
         return;
      }

      if (eqIgno(wp.itemStr("rd_type"), "F")) {
         if (wfChkRoadfree() == -1)
            return;
      }
      else if (eqIgno(wp.itemStr("rd_type"), "E")) {
         if (wfChkRoadexpend() == -1)
            return;
      }
      isSeqno = ooFunc.autonoDtl(commDate.sysDate());
      //-優惠別-
      String lsRdsPcard =ooFunc.getRdsPcard(lsCardNo);
      wp.itemSet("rds_pcard",lsRdsPcard);
   }
   else {
      isSeqno = (int) wp.itemNum("rd_seqno");
   }

   return;
}

public void dataCheckU() {

   // if(wp.item_empty("apr_date")==false){
   // errmsg("資料主管已覆核,不可修改或刪除");
   // return ;
   // }

   if (ibDelete)
      return;

   ooFunc.setConn(wp);
   if (ooFunc.freeIsAuto(wp.itemStr2("appl_card_no")) == 1) {
      ibAuto = true;
   }

   // get mod_seqno
   Double modSeqno = getModSeqno();
   if (modSeqno == null) {
      return;
   }

   if (modSeqno.doubleValue() != wp.itemNum("rm_mod_seqno")) {
      errmsg("道路救援主檔 已被修改; 請重新讀取資料");
      return;
   }

   if (wp.colEq("rd_type", "E")) {
      if (wp.itemEmpty("rd_validdate")) {
         errmsg("有效日期 不可空白");
         return;
      }
   }
   else {
      if (wp.itemNum("rd_payamt") != 0) {
         errmsg("免費救援, 不可輸入[自費金額]");
         return;
      }
   }

   // --停用
   if (wp.itemEq("rd_status", "0")) {
      if (wp.itemEmpty("rd_stopdate") || wp.itemEmpty("rd_stoprsn")) {
         errmsg("請輸入 停用日期及原因");
         return;
      }

      if (wp.itemEmpty("rd_newcarno") == false) {
         errmsg("停用不可變更車號");
         return;
      }
   }
   else {

      // --非停用
      if (wp.itemEmpty("rd_stopdate") == false || wp.itemEmpty("rd_stoprsn") == false) {
         errmsg("不是停用,停用日期及原因需為空白");
         return;
      }
   }

   // --服務類別
   if (wp.colEq("rd_type", "F")) {
      if (ibAuto) {
         if (wfChkRoadfeePv40() == -1)
            return;
      }
      else {
         if (wfChkRoadfree40() == -1)
            return;
      }
   }
   else if (wp.colEq("rd_type", "E")) {
      if (wfChkRoadexpend40() == -1)
         return;
   }

}

private Double getModSeqno() {
   String sql1 = " select mod_seqno from cms_roadmaster where 1=1 and rowid = ? ";
   setRowId(wp.itemStr2("rm_rowid"));
   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      errmsg("道路救援主檔 已不存在; 請重新讀取資料");
      return null;
   }
   return colNum("mod_seqno");
}

boolean checkCardNo() {
   if (empty(wp.itemStr("appl_card_no"))) {
      errmsg("申請卡號 : 不可空白 ");
      return false;
   }

   int liRC = getCurrentCode(wp.itemStr("appl_card_no"));
   if (liRC < 0) {
      return false;
   }

   return true;

}

private int getCurrentCode(String applCardNo) {
   String sql1 = "select  current_code, group_code, card_type  from crd_card  where card_no = ? ";
   sqlSelect(sql1, new Object[]{applCardNo});
   if (sqlRowNum <= 0) {
      errmsg("卡號不存在");
      return -1;
   }
   if (!eqIgno(colStr("current_code"), "0")) {
      errmsg("此卡為無效卡, 不可登錄");
      return -1;
   }

   return 1;
}

boolean checkIdNo() {
   if (empty(wp.itemStr("rd_carmanid"))) {
      errmsg("身分證號 : 不可空白 ");
      return false;
   }
   String sql1 = " select  id_no  from crd_idno where id_no = ? ";
   sqlSelect(sql1, new Object[]{wp.itemStr("rd_carmanid")});

   if (sqlRowNum <= 0) {
      errmsg("身分證號不存在");
      return false;
   }

   return true;

}

@Override
public int dbInsert() {
   actionInit("A");
   ooFunc = new RdsFunc();
   ooFunc.setConn(wp);

   dataCheck();
   if (rc != 1)
      return rc;

   String ls_valiDate=wp.itemStr("rd_validdate");
   if (ls_valiDate.length() >6) {
      ls_valiDate =commString.left(ls_valiDate,6);
      wp.itemSet("rd_validdate",ls_valiDate);
   }

   insertDetail();
   if (rc != 1)
      return rc;

   insertMaster();
   if (rc != 1)
      return rc;

   Double rdPayamt = wp.itemNum("rd_payamt");
   if (rdPayamt > 0) {
      updateBilSysexp();
   }

   return rc;
}

@Override
public int dbUpdate() {
   actionInit("U");
   ooFunc.setConn(wp);

   dataCheck();
   dataCheckU();
   if (rc != 1)
      return rc;

   String ls_valiDate=wp.itemStr("rd_validdate");
   if (ls_valiDate.length() >6) {
      ls_valiDate =commString.left(ls_valiDate,6);
      wp.itemSet("rd_validdate",ls_valiDate);
   }

   insertDetail();
   if (rc != 1)
      return rc;

   updateMaster();
   if (rc != 1)
      return rc;

   Double rdPayamt = wp.itemNum("rd_payamt");
   if (rdPayamt > 0) {
      updateBilSysexp();
   }

   return rc;
}

@Override
public int dbDelete() {
   actionInit("D");
   ooFunc.setConn(wp);

   dataCheck();
   dataCheckU();

   insertDetail();
   if (rc != 1)
      return rc;

   deleteMaster();
   if (rc != 1)
      return rc;

   Double rdPayamt = wp.itemNum("rd_payamt");
   if (rdPayamt > 0) {
      updateBilSysexp();
   }

   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

int wfChkRoadfree() {
   String lsCardNo = "";
   String lsCar = "", lsCarmanid = "";
   String rdStatus = "", rdStopReason = "";
//    String lsMcardNo = "", lsMajorCard = "", lsAplyCard = "" , ss = "", lsSupFlag = "";
//    long llCnt = 0;
//    int ldcTolAmt = 0, ldcTolCnt = 0, ldcTolAmtLyy = 0;
   int liRc = 1;

//    lsMcardNo = wp.itemStr("card_no");
   lsCardNo = wp.itemStr("appl_card_no");
   lsCar = wp.itemStr("rd_carno");
   lsCarmanid = wp.itemStr("rd_carmanid");
   rdStatus = wp.itemStr("rd_status");
   rdStopReason = wp.itemStr("rd_stoprsn");
   ooFunc.lsCardNo = lsCardNo;
   ooFunc.lsCarNo = lsCar;

   // -- 確認卡號是否已作過免費道路救援再登錄
   if (isCardNoInRoadMaster(lsCardNo, lsCarmanid) == false) {
      return -1;
   }

   // --已登錄未覆核--
   if (this.ibAdd) {
      // 確認此卡號是否已登錄待覆核, 不可重複登錄
      if (isCardNoInRoadDetail(lsCardNo, lsCarmanid) == false) {
         return -1;
      }
   }

//    // -- 檢核id_p_seqno是否存在 cms_roadmaster 2021-02-18 Justin
//	String sql3 = " select  count(*) as ll_cnt " 
//	        + " from cms_roadmaster "
//			+ " where ( id_p_seqno in (select id_p_seqno from crd_card where card_no = ? )) "
//			+ " and rm_carmanid = ? and rm_type ='F' ";
//	sqlSelect(sql3, new Object[] { lsCardNo, lsCarmanid });
//
//	if (colInt("ll_cnt") > 0) {
//		
//		switch (rdStatus) {
//		case "4":
//			errmsg(String.format("持卡人己作過免費道路救援，卡號:%s消費不足,未啟用!!", lsCardNo));
//			break;
//		case "0":
//			if ("2".equals(rdStopReason)) {
//				errmsg(String.format("持卡人己作過免費道路救援，卡號:%s消費不足,已停用!!", lsCardNo));
//			}else {
//				errmsg("持卡人己作過免費道路救援登錄!!");
//			}
//		default:
//			errmsg("持卡人己作過免費道路救援登錄!!");
//			break;
//		}
//		
//		return -1;
//	}

   // --BECS-1050805-066 拿掉4種卡 登錄附卡限制
   // String sql3 = " select "
   // + " decode(card_type,'MG',0,'JC',0,'JG',0,'BS',0,nvl(sup_flag,'1')) as ls_sup_flag "
   // + " from crd_card "
   // + " where card_no = ? "
   // ;
   // sqlSelect(sql3,new Object[]{ls_card_no});
   // if(!eq_igno(col_ss("ls_sup_flag"),"0")){
   // errmsg("附卡不可直接登錄");
   // return -1;
   // }

//    // --申請卡之正卡--
//    lsAplyCard = wp.itemStr("appl_card_no");
//    if (!empty(lsAplyCard)) {
//      String sql4 = " select "
//          + " decode(card_type,'MG',card_no,'JC',card_no,'JG',card_no,'BS',card_no,major_card_no) as ls_major_card "
//          + " from crd_card " + " where card_no = ? ";
//      sqlSelect(sql4, new Object[] {lsAplyCard});
//      if (!eqIgno(lsCardNo, colStr("ls_major_card"))) {
//        errmsg("申請卡之正卡不符");
//        return -1;
//      }
//    }

   // --自費金額--
   if (wp.itemNum("rd_payamt") > 0) {
      errmsg("免費道路救援, 金額不可大於 0");
      return -1;
   }

   ibAuto = false;
   if (ooFunc.freeIsAuto(lsCardNo) == 1) {
      ibAuto = true;
   }

   // --車號不可重覆(免費)-------------------
   if (ibAuto == false && empty(lsCar)) {
      errmsg("免費登錄, 車號不可空白");
      return -1;
   }

   if (!empty(lsCar)) {
      liRc = ooFunc.carUniqueFree();
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (liRc > 0) {
         errmsg("車號已登錄[免費], 不可重複登錄");
         return -1;
      }

      liRc = ooFunc.carUniqueExpn();
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (liRc > 0) {
         if (!eqIgno(wp.itemStr("conf_flag1"), "Y")) {
            isMsg = "車號已登錄[自費], 是否再登錄免費";
            rc = 2;
            return 2;
         }
      }
   }

   // -是否有免費資格-
   if (eqIgno(wp.itemStr("rd_status"), "1")) {
      liRc = ooFunc.hasFree();
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (liRc == 0) {
         errmsg("卡號 不符合免費道路救援參數");
         return -1;
      }
      wp.itemSet("cardholder_type", ooFunc.lsChType);
   }

   // -檢核消費金額-
   if (eqIgno(wp.itemStr("rd_status"), "1")) {
      liRc = ooFunc.checkRoadparm2();
      wp.itemSet("proj_no", ooFunc.lsProjNo);
      wp.itemSet("purch_amt", "" + ooFunc.idcPurchAmt);
      wp.itemSet("purch_cnt", "" + ooFunc.ilPurchCnt);
      wp.itemSet("purch_amt_lyy", "" + ooFunc.idcLastAmt);
      wp.colSet("outstanding_cond", ooFunc.outstadningCond);
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      if (liRc == 0) {
         if (!eqIgno(wp.itemStr("conf_flag2"), "Y")) {
            isMsg = ooFunc.getMsg() + "持卡人消費資格不符參數, 是否確定新增";
            rc = 3;
            return 3;
         }
//        wp.itemSet("rd_status", "0"); 2021-02-18 Justin
         wp.itemSet("rd_status", "4");
         wp.itemSet("rd_stoprsn", "2");
         wp.colSet("rd_status", "4");
         wp.colSet("rd_stoprsn", "2");
      }
   }

   //檢核最低應繳金額
   wp.colSet("outstanding_yn", "N");
   if ("Y".equals(ooFunc.outstadningCond)) {
      if (wp.itemEq("conf_flag4", "Y") == false) {
         if (doesSatisfyMinPayableFee() == false) {
            isMsg = "持卡人未繳最低應繳金額，且不會寫入名單。是否存檔";
            rc = 5;
            return 5;
         }
         wp.colSet("outstanding_yn", "Y");
      }
   }

   return 1;
}

private boolean isCardNoInRoadDetail(String lsCardNo, String lsCarmanid) {
   String sql2 = " select  count(*) as ll_cnt2  from cms_roaddetail "
           + " where card_no = ?   and rd_carmanid = ?  and apr_user = '' ";
   sqlSelect(sql2, new Object[]{lsCardNo, lsCarmanid});

   if (colInt("ll_cnt2") > 0) {
      errmsg("此卡號已登錄待覆核, 不可重複登錄");
      return false;
   }
   return true;
}

private boolean isCardNoInRoadMaster(String lsCardNo, String lsCarmanid) {
   String sql1 = " select  count(*) as ll_cnt "
           + " from cms_roadmaster "
           + " where  card_no = ? and rm_carmanid = ? and rm_type ='F' ";
   sqlSelect(sql1, new Object[]{lsCardNo, lsCarmanid});

   if (colInt("ll_cnt") > 0) {
      errmsg("此卡號已做過免費道路救援再登錄");
      return false;
   }
   return true;
}

int wfChkRoadexpend() {
   int ldcVal = 0, llCnt = 0;
   String lsCar = "", lsCardNo = "", lsCarmanid = "";
   lsCardNo = wp.itemStr("appl_card_no");
   lsCar = wp.itemStr("rd_carno");
   lsCarmanid = wp.itemStr("rd_carmanid");
   ooFunc.setConn(wp);
   // --車號
   if (empty(lsCar)) {
      errmsg("登錄車號 不可空白");
      return -1;
   }

   // --已登錄未覆核--
   if (this.ibAdd) {
      String sql1 =
              " select  count(*) as ll_cnt  from cms_roaddetail "
                      + " where appl_card_no = ?  and rd_carno = ?  and rd_carmanid = ?  and apr_user = '' ";
      sqlSelect(sql1, new Object[]{lsCardNo, lsCar, lsCarmanid});
      if (colNum("ll_cnt") > 0) {
         if (!eqIgno(wp.itemStr("conf_flag"), "Y")) {
            errmsg("此卡號已登錄待覆核, 是否重複登錄");
            return 2;
         }
      }
   }

   // String sql2 = " select "
   // + " count(*) as ll_cnt "
   // + " from cms_roadmaster "
   // + " where rm_cardno = ? "
   // + " and rm_type ='E' "
   // + " and rm_carno = ? "
   // ;
   // sqlSelect(sql2,new Object[]{ls_card_no,ls_car});
   // if(sql_nrow<0){
   // errmsg("cms_roadmaster.count() error");
   // return -1;
   // }

   // --(自費)效期--
   if (eqIgno(wp.itemStr("rd_status"), "1")) {
      if (this.chkStrend(wp.itemStr("rd_validdate"), getSysDate().substring(0, 6)) == 1) {
         errmsg("有效期限不可小於系統日期");
         return -1;
      }
   }

   // --自費金額--
   ldcVal = (int) wp.itemNum("rd_payamt");
   if (ldcVal == 0) {
      if (!eqIgno(wp.itemStr("conf_flag3"), "Y")) {
         rc = 4;
         isMsg = "自費金額為 0, 是否由參數取得 !";
         return 4;
      }

      String sql3 =
              " select  nvl(recv_amt,0) as ldc_val  from cms_roadparm " + commSqlStr.rownum(1);
      sqlSelect(sql3);
      wp.colSet("rd_payamt", "" + colNum("ldc_val"));

   }

   return 1;
}

int wfChkRoadexpend40() {
   String lsApplyCardNo = "", lsRdType = "", lsRdCarno = "", lsRdStatus = "",
           lsRmStatus = "";
   double ldcPayment = 0;
   lsApplyCardNo = wp.itemStr2("appl_card_no");
   lsRdType = wp.itemStr2("rd_type");
   lsRdCarno = wp.itemStr2("rd_carno");
   lsRdStatus = wp.itemStr2("rd_status");
   lsRmStatus = wp.itemStr2("rm_status");
   ldcPayment = wp.itemNum("rd_payamt");
   if (eqIgno(lsRdStatus, "3")) {
      errmsg("自費登錄, 不可取消車號");
      return -1;
   }
   else if (eqIgno(lsRdStatus, "2")) {
      if (wp.itemEmpty("rd_newcarno")) {
         errmsg("變更車號, [新車號]不可空白");
         return -1;
      }
   }
   else if (eqIgno(lsRdStatus, "0")) {
      if (wp.itemEq("rd_stoprsn", "2")) {
         errmsg("自費登錄停用原因, 不可指定[消費未達標準]");
         return -1;
      }
   }

   // --使用
   if (eqIgno(lsRdStatus, "0") == false && eqIgno(lsRmStatus, "0") && ldcPayment <= 0) {
      if (wp.itemEq("conf_flag3", "Y") == false) {
         wp.colSet("conf_on", "1==1");
         wp.colSet("conf_type", "3");
         wp.colSet("mesg", "自費續用,是否收費");
         return -1;
      }
   }

   // --停用
   if (eqIgno(lsRdStatus, "0") && ldcPayment > 0) {
      errmsg("自費停用, 自費金額不可大於 0 ");
      return -1;
   }

   // --停用改啟用
   if (eqIgno(lsRmStatus, "0") && eqIgno(lsRdStatus, "1")) {
      if (ldcPayment == 0) {
         if (wp.itemEq("conf_flag4", "Y") == false) {
            wp.colSet("conf_on", "1==1");
            wp.colSet("conf_type", "4");
            wp.colSet("mesg", "自費續用,是否收費");
         }

         if (wp.itemEq("conf_flag5", "Y") == false) {
            wp.colSet("conf_on", "1==1");
            wp.colSet("conf_type", "5");
            wp.colSet("mesg", "自費金額為 0 ,是否由參數取得");
         }
      }
   }
   return 1;
}

int wfChkRoadfeePv40() {
   String lsCar = "", lsRdSts = "", lsRmSts = "", lsStopRsn = "", lsCardNo = "",
           lsCarNew = "";
   double llCnt = 0;
   int liRc = 0;
   lsRdSts = wp.itemStr2("rd_status");
   lsRmSts = wp.itemStr2("rm_status");
   lsCardNo = wp.itemStr2("appl_card_no");
   lsCar = wp.itemStr2("rd_carno");
   lsCarNew = wp.itemStr2("rd_newcarno");
   ooFunc.lsCardNo = lsCardNo;
   ooFunc.lsCarNo = lsCar;

   if (eqIgno(lsRdSts, "0") == false) {
      String sql1 =
              "select count(*) as ll_cnt from crd_card where card_no = ? and current_code = '0' ";
      sqlSelect(sql1, new Object[]{lsCardNo});
      if (colNum("ll_cnt") == 0) {
         errmsg("卡號已無效, 不可再異動");
         return -1;
      }
   }

   if (eqIgno(lsRdSts, "3") && empty(lsCarNew) == false) {
      errmsg("取消車號時, [新車號]須為空白");
      return -1;
   }
   lsStopRsn = wp.itemStr2("rd_stoprsn");
   if (eqIgno(lsRdSts, "0") && empty(lsStopRsn)) {
      errmsg("停用時 需輸入停用原因");
      return -1;
   }

   // --車號唯一
   if (empty(ooFunc.lsCarNo) == false && (eqIgno(lsRdSts, "0") == false
           || (eqIgno(lsRdSts, "0") && eqIgno(lsStopRsn, "2")))) {
      if (ooFunc.carUniqueFree() == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (ooFunc.carUniqueFree() > 0) {
         errmsg("卡號已登錄[免費],不可重複登錄");
         return -1;
      }
      if (ooFunc.carUniqueExpn() == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (ooFunc.carUniqueExpn() > 0) {
         if (wp.itemEq("conf_flag1", "Y") == false) {
            wp.colSet("conf_on", "1==1");
            wp.colSet("conf_type", "1");
            wp.colSet("mesg", "車號已登錄[自費], 是否再登錄免費");
            return -1;
         }
      }
   }

   // --重新啟動是否有免費資格
   if (commString.pos("|0|4", lsRmSts) > 0 && commString.pos("|1|2", lsRdSts) > 0) {
      int hasFreeCode = ooFunc.hasFree();
      if (hasFreeCode == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (hasFreeCode == 0) {
         errmsg("卡號不符合免費道路救援參數");
         return -1;
      }
   }

   // --重新檢核消費金額
   if (commString.pos("|0|4", lsRmSts) > 0 && commString.pos("|1|2", lsRdSts) > 0) {
//    if (eqIgno(lsRmSts, "0") && commString.pos("|1|2", lsRdSts) > 0) {
      liRc = ooFunc.checkRoadparm2();
      wp.colSet("proj_no", ooFunc.lsProjNo);
      wp.colSet("purch_amt", ooFunc.idcPurchAmt);
      wp.colSet("purch_cnt", ooFunc.ilPurchCnt);
      wp.colSet("purch_amt_lyy", ooFunc.idcLastAmt);
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (liRc == 0) {
         if (!eqIgno(wp.itemStr("conf_flag2"), "Y")) {
            wp.colSet("conf_on", "1==1");
            wp.colSet("conf_type", "2");
            isMsg = ooFunc.getMsg() + "持卡人消費資格不符參數, 是否確定新增";
            rc = 3;
            return -1;
         }
         wp.itemSet("rd_status", "4");
         wp.itemSet("rd_stoprsn", "2");
         wp.colSet("rd_status", "4");
         wp.colSet("rd_stoprsn", "2");
      }
   }

   //檢核最低應繳金額
   wp.colSet("outstanding_yn", "N");
   if ("Y".equals(ooFunc.outstadningCond)) {
      if (wp.itemEq("conf_flag4", "Y") == false) {
         if (doesSatisfyMinPayableFee() == false) {
            isMsg = "持卡人未繳最低應繳金額，且不會寫入名單。是否存檔";
            rc = 5;
            return 5;
         }
         wp.colSet("outstanding_yn", "Y");
      }
   }

   return 1;
}

int wfChkRoadfree40() {
   String lsRdStatus = "", lsRmStatus = "", lsCardNo = "", lsCar = "", lsCarNew = "",
           lsSupFlag = "";
   double llCnt = 0, ldcTolAmt = 0, ldcTol_cnt = 0, ldcTolAmtLyy = 0;
   int liRc = 0;

   lsRdStatus = wp.itemStr2("rd_status");
   lsRmStatus = wp.itemStr2("rm_status");
   lsCardNo = wp.itemStr2("appl_card_no");

   ooFunc.lsCardNo = lsCardNo;
   ooFunc.lsCarNo = wp.itemStr2("rd_carno");
   lsCarNew = wp.itemStr2("rd_newcarno");

   if (eqIgno(lsRdStatus, "1")) {
      if (empty(lsCar) && empty(lsCarNew)) {
         errmsg("啟用時 [新]車號不可空白");
         return -1;
      }
   }
   else if (eqIgno(lsRdStatus, "2")) {
      if (eqIgno(lsCar, lsCarNew)) {
         errmsg("變更車號時 [新]車號不可相同");
         return -1;
      }

      if (empty(lsCarNew)) {
         errmsg("變更車號時 新車號不可空白");
         return -1;
      }
   }
   else if (eqIgno(lsRdStatus, "3")) {
      if (empty(lsCar)) {
         errmsg("無車號, 異動狀態不可為 [取消車號]");
         return -1;
      }

      if (empty(lsCarNew) == false) {
         errmsg("取消車號, [新車號]須為空白");
         return -1;
      }
   }

   if (empty(lsCarNew) == false)
      ooFunc.lsCarNo = lsCarNew;

   // --車號唯一
   if (eqIgno(lsRdStatus, "3") == false && empty(ooFunc.lsCarNo) == false) {
      liRc = ooFunc.carUniqueFree(lsCardNo, ooFunc.lsCarNo);
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (liRc > 0) {
         errmsg("車號已登錄[免費], 不可重複登錄");
         return -1;
      }
      liRc = ooFunc.carUniqueExpn(lsCardNo, ooFunc.lsCarNo);
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (liRc > 0) {
         if (wp.itemEq("conf_flag6", "Y") == false) {
            wp.colSet("conf_on", "1==1");
            wp.colSet("conf_type", "6");
            wp.colSet("mesg", "車號已登錄[自費],是否再登錄免費");
         }
      }
   }

   if (commString.pos("|0|4", lsRmStatus) > 0 && commString.pos("|1|2", lsRdStatus) > 0) {
      int hasFreeCode = ooFunc.hasFree();
      if (hasFreeCode == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (hasFreeCode == 0) {
         errmsg("卡號不符合免費道路救援參數");
         return -1;
      }
   }

   // --重新檢核消費金額
   if (commString.pos("|0|4", lsRmStatus) > 0 && commString.pos("|1|2", lsRdStatus) > 0) {
//      if (eqIgno(lsRmSts, "0") && commString.pos("|1|2", lsRdSts) > 0) {
      liRc = ooFunc.checkRoadparm2();
      wp.colSet("proj_no", ooFunc.lsProjNo);
      wp.colSet("purch_amt", ooFunc.idcPurchAmt);
      wp.colSet("purch_cnt", ooFunc.ilPurchCnt);
      wp.colSet("purch_amt_lyy", ooFunc.idcLastAmt);
      if (liRc == -1) {
         errmsg(ooFunc.getMsg());
         return -1;
      }
      else if (liRc == 0) {
         if (!eqIgno(wp.itemStr("conf_flag2"), "Y")) {
            wp.colSet("conf_on", "|| 1==1");
            wp.colSet("conf_type", "2");
            isMsg = ooFunc.getMsg() + "持卡人消費資格不符參數, 是否確定新增";
            rc = 3;
            return -1;
         }
         wp.itemSet("rd_status", "4");
         wp.itemSet("rd_stoprsn", "2");
         wp.colSet("rd_status", "4");
         wp.colSet("rd_stoprsn", "2");
      }
   }

   //檢核最低應繳金額
   wp.colSet("outstanding_yn", "N");
   if ("Y".equals(ooFunc.outstadningCond)) {
      if (wp.itemEq("conf_flag4", "Y") == false) {
         if (doesSatisfyMinPayableFee() == false) {
            isMsg = "持卡人未繳最低應繳金額，且不會寫入名單。是否存檔";
            rc = 5;
            return 5;
         }
         wp.colSet("outstanding_yn", "Y");
      }
   }

   return 1;
}

taroko.base.Parm2Sql tt = new Parm2Sql();

void insertDetail() {
   int li_seqNo = ooFunc.autonoDtl(sysDate);

   String lsModtype = "O";
   String lsCrtUser = modUser;
   String lsCrtDate = sysDate;
   if (isAdd()) {
      lsModtype = "OA";
   }
   else if (isUpdate()) {
      lsModtype = "OU";
      lsCrtDate = wp.itemStr("crt_date");
      lsCrtUser = wp.itemStr("crt_user");
   }
   else if (isDelete()) {
      lsModtype = "OD";
      lsCrtDate = wp.itemStr("crt_date");
      lsCrtUser = wp.itemStr("crt_user");
   }

   tt.insert("cms_roaddetail");
   tt.parmSet("rd_moddate", sysDate);
   tt.parmSet("rd_seqno", li_seqNo);
   tt.parmSet("rd_modtype", lsModtype);
   tt.parmSet("card_no", wp.itemStr("appl_card_no"));
   tt.parmSet("new_card_no", wp.itemStr("appl_card_no"));
   tt.parmSet("rd_type", wp.itemStr("rd_type"));
   tt.parmSet("appl_card_no", wp.itemStr("appl_card_no"));
   tt.parmSet("group_code", wp.itemStr("group_code"));
   tt.parmSet("rd_carno", wp.itemStr("rd_carno").toUpperCase());
   tt.parmSet("rd_carmanname", wp.itemStr("rd_carmanname"));
   tt.parmSet("rd_carmanid", wp.itemStr("rd_carmanid"));
   tt.parmSet("rd_newcarno", wp.itemStr("rd_newcarno").toUpperCase());
   tt.parmSet("rd_htelno1", wp.itemStr("rd_htelno1"));
   tt.parmSet("rd_htelno2", wp.itemStr("rd_htelno2"));
   tt.parmSet("rd_htelno3", wp.itemStr("rd_htelno3"));
   tt.parmSet("rd_otelno1", wp.itemStr("rd_otelno1"));
   tt.parmSet("rd_otelno2", wp.itemStr("rd_otelno2"));
   tt.parmSet("rd_otelno3", wp.itemStr("rd_otelno3"));
   tt.parmSet("cellar_phone", wp.itemStr("cellar_phone"));
   tt.parmSet("rd_validdate", wp.itemStr("rd_validdate"));
   tt.parmSet("rd_status", wp.itemStr("rd_status"));
   tt.parmSet("rd_payamt", wp.itemNum("rd_payamt"));
   tt.parmSet("rd_payno", wp.itemStr("rd_payno"));
   tt.parmSet("rd_paydate", wp.itemStr("rd_paydate"));
   tt.parmSet("rd_stopdate", wp.itemStr("rd_stopdate"));
   tt.parmSet("rd_stoprsn", wp.itemStr("rd_stoprsn"));
   tt.parmSet("crt_user", lsCrtUser);
   tt.parmSet("crt_date", lsCrtDate);
   tt.parmSet("proj_no", wp.itemStr("proj_no"));
   tt.parmSet("purch_amt", wp.itemNum("purch_amt"));
   tt.parmSet("purch_cnt", wp.itemNum("purch_cnt"));
   tt.parmSet("purch_amt_lyy", wp.itemNum("purch_amt_lyy"));
   tt.parmSet("cardholder_type", wp.itemStr("cardholder_type"));
   tt.parmSet("rds_pcard", wp.itemStr("rds_pcard"));
   tt.parmSet("give_flag", wp.itemStr("give_flag"));
   tt.parmSet("apr_user", modUser);
   tt.parmSet("apr_date", sysDate);
   tt.parmSet("outstanding_yn", wp.itemStr("outstanding_yn"));
   tt.parmSet("outstanding_cond", wp.itemStr("outstanding_cond"));
   tt.parmSet("id_p_seqno", wp.itemStr("id_p_seqno"));
   tt.modxxxSet(modUser, modPgm);

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert cms_roaddetail error ! ");
   }
}

public int insertMaster() {
   msgOK();

   String carno = "", oldcarno = "";

   if (!empty(wp.itemStr("rd_newcarno"))) {
      oldcarno = wp.itemStr("rd_carno");
      carno = wp.itemStr("rd_newcarno");
   }
   else {
      carno = wp.itemStr("rd_carno");
   }

   StringBuilder sb = new StringBuilder();
   sb.append(" insert into cms_roadmaster ( ");
   sb.append(" card_no , "); // 1
   sb.append(" rm_type ,  group_code ,  rm_carmanname , "); // 5
   sb.append(" rm_carmanid ,  rm_htelno1 ,  rm_htelno2 ,  rm_htelno3 , ");
   sb.append(" rm_otelno1 , "); // 10
   sb.append(" rm_otelno2 ,  rm_otelno3 ,  cellar_phone ,  rm_status , ");
   sb.append(" rm_oldcarno ,  rm_carno ,  rm_moddate ,  rm_validdate , ");
   sb.append(" rm_reason , "); // 20
   sb.append(" rm_payamt ,  rm_paydate ,  rds_pcard ,  crt_user ,  crt_date , ");
   sb.append(" apr_user , "); // 25
   sb.append(" apr_date , mod_user , mod_time , mod_pgm ,  ");
   sb.append(" id_p_seqno , ");
   sb.append(" outstanding_yn ");
   sb.append(" ) values ( ");
   sb.append(" :card_no , "); // 1
   sb.append(" :rm_type ,  :group_code ,  :rm_carmanname , "); // 5
   sb.append(" :rm_carmanid ,  :rm_htelno1 ,  :rm_htelno2 ,  :rm_htelno3 , ");
   sb.append(" :rm_otelno1 , "); // 10
   sb.append(" :rm_otelno2 ,  :rm_otelno3 ,  :cellar_phone ,  :rm_status , ");
   sb.append(" :rm_oldcarno ,  :rm_carno ,  :rm_moddate ,  :rm_validdate , ");
   sb.append(" :rm_reason , "); // 20
   sb.append(" :rm_payamt ,  :rm_paydate ,  :rds_pcard ,  :crt_user , ");
   sb.append(" :crt_date ,  :apr_user , "); // 25
   sb.append(" :apr_date , :mod_user , sysdate , :mod_pgm , ");
   sb.append(" (select id_p_seqno from crd_card where card_no = :appl_card_no ) , ");
   sb.append(" :outstanding_yn ");
   sb.append(" )");
   strSql = sb.toString();

   setString("card_no", wp.itemStr("appl_card_no"));
   setString("rm_type", wp.itemStr("rd_type"));
   setString("group_code", wp.itemStr("group_code"));
   setString("rm_carmanname", wp.itemStr("rd_carmanname"));
   setString("rm_carmanid", wp.itemStr("rd_carmanid"));
   setString("rm_htelno1", wp.itemStr("rd_htelno1"));
   setString("rm_htelno2", wp.itemStr("rd_htelno2"));
   setString("rm_htelno3", wp.itemStr("rd_htelno3"));
   setString("rm_otelno1", wp.itemStr("rd_otelno1"));
   setString("rm_otelno2", wp.itemStr("rd_otelno2"));
   setString("rm_otelno3", wp.itemStr("rd_otelno3"));
   setString("cellar_phone", wp.itemStr("cellar_phone"));
   setString("rm_status", wp.itemStr("rd_status"));
   setString("rm_oldcarno", oldcarno.toUpperCase());
   setString("rm_carno", carno.toUpperCase());
   setString("rm_moddate", commDate.sysDate());
   setString("rm_validdate", wp.itemStr("rd_validdate"));
   setString("rm_reason", wp.itemStr("rd_stoprsn"));
   setNumber("rm_payamt", wp.itemNum("rd_payamt"));
   setString("rm_paydate", wp.itemStr("rd_paydate"));
   setString("rds_pcard", wp.itemStr("rds_pcard"));
   setString("crt_user", wp.loginUser);
   setString("crt_date", commDate.sysDate());
   setString("apr_user", wp.loginUser);
   setString("apr_date", commDate.sysDate());
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", PGM);
   setString("appl_card_no", wp.itemStr("appl_card_no"));
   setString("outstanding_yn", wp.colStr("outstanding_yn"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("insert cms_roadmaster error ! ");
   }
   return rc;
}

public int updateMaster() {
   msgOK();

   String carno = "", oldcarno = "";

   if (!empty(wp.itemStr("rd_newcarno"))) {
      oldcarno = wp.itemStr("rd_carno");
      carno = wp.itemStr("rd_newcarno");
   }
   else {
      carno = wp.itemStr("rd_carno");
   }

   Boolean isExist = queryMaster();
   if (!isExist) {
      rc = insertMaster();
      return rc;
   }
   StringBuilder sb = new StringBuilder();
   sb.append(" update cms_roadmaster set  rm_type =:rm_type ,");
   sb.append(" rm_carmanname =:rm_carmanname , rm_carmanid =:rm_carmanid ,");
   sb.append(" rm_status =:rm_status , rm_oldcarno =:rm_oldcarno , rm_carno =:rm_carno ,");
   sb.append(" rm_moddate =:rm_moddate , rm_validdate =:rm_validdate ,");
   sb.append(" rm_reason =:rm_reason , rm_payamt =:rm_payamt , rds_pcard =:rds_pcard ,");
   sb.append(" apr_user =:apr_user , apr_date =:apr_date , mod_user =:mod_user ,");
   sb.append(" mod_time = sysdate  where 1=1  and card_no =:ls_key1 ");
   sb.append(" and rm_carmanid =:ls_key2 ");
   strSql = sb.toString();
   setString("rm_type", wp.itemStr("rd_type"));
   setString("rm_carmanname", wp.itemStr("rd_carmanname"));
   setString("rm_carmanid", wp.itemStr("rd_carmanid"));
   setString("rm_status", wp.itemStr("rd_status"));
   setString("rm_oldcarno", oldcarno);
   setString("rm_carno", carno);
   setString("rm_moddate", wp.itemStr("rd_moddate"));
   setString("rm_validdate", wp.itemStr("rd_validdate"));
   setString("rm_reason", wp.itemStr("rd_stoprsn"));
   setNumber("rm_payamt", wp.itemNum("rd_payamt"));
   setString("rds_pcard", wp.itemStr("rds_pcard"));
   setString("apr_user", wp.loginUser);
   setString("apr_date", commDate.sysDate());
   setString("mod_user", wp.loginUser);
   setString("ls_key1", wp.itemStr("appl_card_no"));
   setString("ls_key2", wp.itemStr("rd_carmanid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update cms_roadmaster error ! ");
   }
   return rc;
}

public int deleteMaster() {
   msgOK();

   strSql = "delete cms_roadmaster where card_no =:card_no and rm_carmanid =:rm_carmanid";

   setString("card_no", wp.itemStr("appl_card_no"));
   setString("rm_carmanid", wp.itemStr("rd_carmanid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("delete cms_roadmaster error ! ");
   }
   return rc;
}

int updateBilSysexp() {
   String lsKey1 = "", lsKey2 = " ", lsVal1 = "", lsVal2 = "", lsVal3 = "";
   int ldcAmt = 0;
   lsKey1 = wp.itemStr("appl_card_no");
   lsKey2 = wp.itemStr("rd_type");
   if (!eqIgno(lsKey2, "E"))
      return 0;

   // --AMT
   ldcAmt = Integer.parseInt(wp.itemStr("rd_payamt"));
   if (ldcAmt <= 0)
      return 0;

   // --sysdate
   lsVal1 = commDate.sysDate();

   // --acct_type/key--
   String sql1 = " select " + " acct_type , " + commSqlStr.ufunc("uf_acno_key(p_seqno) as acct_key ")
           + " from crd_card " + " where card_no = ? ";
   sqlSelect(sql1, new Object[]{lsKey1});
   if (sqlRowNum <= 0)
      return -1;

   lsVal2 = colStr("acct_type");
   lsVal3 = colStr("acct_key");

   StringBuilder sb = new StringBuilder();
   sb.append(" insert into bil_sysexp ( card_no ,  bill_type ,  txn_code , ");
   sb.append(" purchase_date ,  acct_type ,  acct_key ,  dest_amt ,  dest_curr , ");
   sb.append(" src_amt ,  post_flag ,  mod_user ,  mod_time ,  mod_pgm , ");
   sb.append(" mod_seqno  ) values (  :card_no ,  'INCF' ,  '05' , ");
   sb.append(" :purchase_date ,  :acct_type ,  :acct_key ,  :dest_amt ,  '901' , ");
   sb.append(" :src_amt ,  'N' ,  :mod_user ,  sysdate ,  :mod_pgm ,  1 ");
   sb.append(" ) ");
   strSql = sb.toString();
   setString("card_no", lsKey1);
   setString("purchase_date", lsVal1);
   setString("acct_type", lsVal2);
   setString("acct_key", lsVal3);
   setString("dest_amt", "" + ldcAmt);
   setString("src_amt", "" + ldcAmt);
   setString("mod_user", wp.loginUser);
   setString("mod_pgm", PGM);

   sqlExec(strSql);

   if (sqlRowNum <= 0)
      return -1;


   return 1;
}

public Boolean queryMaster() {
   strSql = " select count(*) cnt from cms_roadmaster "
           + "where  card_no =:ls_key1 and rm_carmanid =:ls_key2 ";

   setString("ls_key1", wp.itemStr("appl_card_no"));
   setString("ls_key2", wp.itemStr("rd_carmanid"));

   sqlSelect(strSql);
   if (colInt("cnt") > 0) {
      return true;
   }
   else {
      return false;
   }
}

private boolean doesSatisfyMinPayableFee() {
   String sql4 = " select  p_seqno, count(*) as cnt "
           + " from act_acno "
           + " where INT_RATE_MCODE >0 and PAYMENT_RATE1='00' and p_seqno = ?  "
           + " group by p_seqno "
           + " having count(*) >=1 ";
   sqlSelect(sql4, new Object[]{ooFunc.lsPSeqno});
   return colInt("cnt") >= 1;
}
}
