package rskm01;
/**
 * 2022-0418   JH    bug: ptr_sys_idtab.select
 * 2022-0112   JH    I3.R045
 * 2021-0728   JH    ++insert_bil_back_log()
 * 2021-0629   JH    bil_rskok.ctrl_seqno
 * 2021-0618   JH    oo_rskok.ib_debot
 * 2020-0529   JH    仲裁/依從: only times=1
 * 2020-0522   JH    clo_result=10
 * 2020-0414	JH		連動結案rsk_precompl
 * 問交/特交/不合格結案登錄-主管覆核
 */

import busi.FuncAction;

public class Rskp0030Func extends FuncAction {
public String modVersion() {
   return "v22-0420.00";
}
//busi.zzComm zzComm = new busi.zzComm();
RskAcctLog ooAcct = new RskAcctLog();

int iiCloType = 1;

boolean ibDebit = false;
String isGlcurr = "", isRskCtrlSeqno = "";
String isCloResult="";
String isReferenceNo="";


void selectRskProblem() {
   daoTid = "A.";
   strSql = "select reference_no, ctrl_seqno, bin_type, card_no, back_flag , back_date , back_status , "
         + " debit_flag, prb_status,"
         + " p_seqno, acct_type, debit_acct_no,"
         + " prb_src_code, prb_mark, contract_no, "
         + " reference_no_ori,"
         + " org_card_no,"
         + " prb_amount,"
         + " payment_type, txn_code, mcht_no,"
         + " purchase_date, bill_type, "
         + " dest_amt, dc_dest_amt, rsk_type,"
         + " clo_merge_vouch,"
         + " uf_dc_curr(curr_code) as curr_code,"
         + " uf_dc_amt2(prb_amount,dc_prb_amount) as dc_prb_amount,"
   //+", uf_tt_idtab('PRB'||prb_mark||'-CLO-RESULT',clo_result) as tt_clo_result"
//   +" (SELECT wf_desc FROM ptr_sys_idtab WHERE wf_type LIKE '%CLO-RESULT' and wf_type NOT LIKE 'CHGBACK-%' AND wf_id=rsk_problem.clo_result) AS tt_clo_result, "
   ;
   if (iiCloType == 2) {
      strSql += " clo_result_2 as clo_result,"
            + " prb_glmemo3_2 as prb_glmemo3,"
            + " mcht_repay_2 as mcht_repay,"
            + " mcht_close_fee_2 as mcht_close_fee,"
            + " uf_dc_amt2(mcht_repay_2,dc_mcht_repay_2) as dc_mcht_repay,"
            + " close_add_user_2 as close_add_user,"
      ;
   }
   else {
      strSql += " clo_result, prb_glmemo3,"
            + " mcht_repay, mcht_close_fee,"
            + " uf_dc_amt2(mcht_repay,dc_mcht_repay) as dc_mcht_repay,"
            + " close_add_user,"
      ;
   }

   strSql += " '' as xxx"
         + " from rsk_problem"
         + " where 1=1"
         + " and rowid = ? "
         + " and nvl(mod_seqno,0) = ? "
         ;
   
   setRowId(1,varsStr("rowid"));
   setString(2,varsStr("mod_seqno"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg(errOtherModify);
      return;
   }
   colDataToWpItem("A.");
   getWfDesc();
   colDataToWpItem("A.");
   wp.itemSet("A.clo_type", "" + iiCloType);
   isCloResult =colStr("A.clo_result");

}

void getWfDesc() {
	daoTid = "A.";
	String sql1 = "SELECT wf_desc as tt_clo_result FROM ptr_sys_idtab WHERE wf_type LIKE '%CLO-RESULT' and wf_type NOT LIKE 'CHGBACK-%' AND wf_id = ? ";	
	sqlSelect(sql1,new Object[] {colStr("A.clo_result")});			
}

String getRskCtrlSeqno() {
//   return zzComm.rsk_ctrlSeqno_Prbl(colStr("A.ctrl_seqno")
//         , colStr("A.bin_type"), "80" + iiCloType);
   String lsCtrlSeqno=colStr("A.ctrl_seqno");
   if (iiCloType ==1 || iiCloType==2) {
      return lsCtrlSeqno+"-PR80"+iiCloType;
   }
   //--問交
   return lsCtrlSeqno+"-PR801";

}

@Override
public void dataCheck() {
   selectRskProblem();
   if (rc != 1)
      return;

   ibDebit = colEq("A.debit_flag","Y");
   isRskCtrlSeqno = getRskCtrlSeqno();
   isReferenceNo =colStr("A.reference_no");
   busi.func.EcsComm ecsf = new busi.func.EcsComm();
   ecsf.setConn(wp.getConn());
   isGlcurr = ecsf.getCurrCodeGl(colStr("A.curr_code"));
   if (ibDebit == false && empty(isGlcurr)) {
      errmsg("無法取得[會計幣別], 結算幣別=%s; 不可放行", colStr("A.curr_code"));
      return;
   }

   //-國外交易手續費-
   BilBill bill = new BilBill();
   bill.setConn(wp);
   bill.debitFlag(wp.itemStr("debit_flag"));
   boolean lbOversea = bill.isOverseaFee(colStr("A.reference_no"), colStr("A.reference_no_ori"));
   if (lbOversea)
      wp.itemSet("A.oversea_flag", "Y");
   else wp.itemSet("A.oversea_flag", "N");
}

void dataCheckCancel() {
   daoTid = "A.";
   String sql1 = "select reference_no,"
         + " close_apr_date , "
         + " close_add_date_2 , "
         + " close_vouch_date "
         + ", ctrl_seqno, bin_type, debit_flag"
         + ", prb_status"
         + " from rsk_problem "
         + " where 1=1 "
         + " and rowid = ? "
         + " and nvl(mod_seqno,0) = ? " ;
   
   setRowId(1,varsStr("rowid"));
   setString(2,varsStr("mod_seqno"));
   
   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      errmsg("資料錯誤 請重新讀取");
      return;
   }
   ibDebit = colEq("A.debit_flag","Y");
   isRskCtrlSeqno = getRskCtrlSeqno();
   isReferenceNo =colStr("A.reference_no");

   if (colNeq("A.prb_status", "80")) {
      errmsg("不是覆核狀態, 不可解覆核");
      return;
   }

   if (!eqIgno(colStr("A.close_apr_date"), wp.sysDate)) {
      errmsg("不是當日覆核, 不可解覆核");
      return;
   }

   if (!empty(colStr("A.close_add_date_2"))) {
      errmsg("已作二次結案, 不可取消");
      return;
   }

//	if (!empty(colStr("A.close_vouch_date"))) {
//		errmsg("結案啟帳, 不可取消");
//		return;
//	}
   strSql = "select count(*) as db_cnt from rsk_acct_log"
         + " where ctrl_seqno =?"
         + " and bin_type =?"
         + " and rsk_status ='801'"
         + " and table_id ='PRBL'"
         + " and vouch_proc_flag ='Y'";
   setString(1, colStr("A.ctrl_seqno"));
   setString(2, colStr("A.bin_type"));
   sqlSelect(strSql);
   if (colNum("db_cnt") > 0) {
      errmsg("(第一次)結案已啟帳, 不可[解覆核]");
      return;
   }

   //-OK-
   col2wpItem("A.");

   //-check rsk_acct_log-
   strSql = "select count(*) as db_cnt from bil_rskok"
         + " where rsk_ctrl_seqno =?"
         + " and post_flag ='Y'";
   setString(1, isRskCtrlSeqno);
   sqlSelect(strSql);
   if (sqlRowNum < 0) {
      errmsg("select bil_rskok error, kk=" + isRskCtrlSeqno);
      return;
   }
   if (sqlRowNum > 0 && colNum("db_cnt") > 0) {
      errmsg("已執行重新啟帳處理, 不可[解覆核]");
      return;
   }

   //check act_acaj--
   if (ibDebit) {
      strSql = "select count(*) from dba_acaj"
            + " where rsk_ctrl_seqno =?"
            + " and proc_flag ='Y'";
   }
   else {
      strSql = "select count(*) from act_acaj"
            + " where rsk_ctrl_seqno =?"
            + " and process_flag ='Y'";
   }
   setString(1, isRskCtrlSeqno);
   sqlSelect(strSql);
   if (sqlRowNum < 0) {
      errmsg("select act[dba]_acaj error, kk=" + isRskCtrlSeqno);
      return;
   }
   if (sqlRowNum > 0 && colNum("db_cnt") > 0) {
      errmsg("已執行帳務調整處理, 不可[解覆核]");
      return;
   }
}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

void updateChgback(String aRefno) {
   String ls_prbl_result= isCloResult; //colStr("A.clo_result");
//   if (pos("|01,02,03,07,21,31,32,33,34,41,42,43,44,71,72,73,81,82,83,06.18.20.D8.DA", ls_prbl_result) <= 0) {
//      return;
//   }
   String ls_chgb_result="";
   //-06.18.20.D8.DA-
   if (commString.strIn(ls_prbl_result,"|,06.18.20.D8.DA")) {
      ls_chgb_result ="01";
   }
   if (empty(ls_chgb_result)) {
      return;
   }

   strSql = "update rsk_chgback set"
         + " final_close ='S' "
         +", clo_result =decode(clo_result,'',cast(? as varchar(2)),clo_result)"
         + ", close_add_date =" + commSqlStr.sysYYmd
         + ", close_add_user = 'system'"
         + ", close_apr_date =" + commSqlStr.sysYYmd
         + ", close_apr_user = 'system'"
         + " where reference_no =? and reference_seq=0";
   setString(ls_chgb_result);
//   ppp(mod_user);
//   ppp(mod_user);
   setString(aRefno);

   sqlExec(strSql);
   if (sqlRowNum < 0) {
      errmsg("連動更新 RSK_CHGBACK 檔失敗？！");
   }
}

boolean checkSysVouch(String aCd, int aRow) {
   if (empty(aCd)) {
      errmsg("會計分錄套號: 不可空白");
      return false;
   }

   strSql = "select count(*) as db1"
         + " from gen_sys_vouch"
         + " where std_vouch_cd =?";
   setString(1, aCd);
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("select gen_sys_vouch error; kk=" + aCd);
      return false;
   }

   if (aRow == colInt("db1")) {
      return true;
   }

   //--
   errmsg("會計分錄套號: 筆數不對; kk=" + aCd);
   return false;
}

void closeVouchVD() {
   String lsCloResult = colStr("A.clo_result");
   boolean lb_SQ = colEq("A.prb_src_code", "SQ");

   DbaAcaj ooAcaj = new DbaAcaj();
   ooAcaj.setConn(wp);
   ooAcaj.setRskCtrlSeqno(isRskCtrlSeqno);

   BilRskok oo_rskok = new BilRskok();
   oo_rskok.setConn(wp);
   oo_rskok.ibDebit =true;
   oo_rskok.setrskCtrlSeqno(isRskCtrlSeqno);

   //-JH-200522-
//-- 20230714-取消D0
//   if (pos("|D0|10", lsCloResult) > 0) {
//	   //--Y 之前已回存過 
//	   if(colEq("A.back_status","Y")) {
//		   if (checkSysVouch("D007", 2) == false)			   
//		       return;
//	   }	else	{
//		   if (checkSysVouch("D004", 2) == false)
//		       return;
//		   
//		   ooAcaj.rskp0030VdBack("DP01",colStr("A.reference_no"));		   
//	   }
//	   
//      
//      if (lb_SQ && colEq("A.rsk_type", "5")) {
//         if (ooAcaj.rskP0030Insert("RE10") == -1) {
//            errmsg(ooAcaj.mesg());
//            return;
//         }
//         ooAcct.acajFlag("Y");
//      }
//      ooAcct.stdVouchCd("R-D0");
//      return;
//   }
   //-D1-
//-- 20230714- D1 D2 D3 取消
//   if (pos("|D1", lsCloResult) > 0) {
//      if (checkSysVouch("R-D1", 4) == false)
//         return;
//      ooAcct.stdVouchCd("R-D1");
//      return;
//   }
//   //-D2-
//   if (pos("|D2", lsCloResult) > 0) {
//      if (checkSysVouch("R-D2", 4) == false) return;
//      ooAcct.stdVouchCd("R-D2");
//      return;
//   }
//   //--
//   if (pos("|D3", lsCloResult) > 0) {
//      if (checkSysVouch("R-D3", 4) == false) return;
//      ooAcct.stdVouchCd("R-D3");
//      return;
//   }
   //--
   if (pos("|D6", lsCloResult) > 0) {
	   //--20230714 TCB沒有系統列問交了 , 所以只要判斷當初是否有回存
//      if (lb_SQ) {
//         if (oo_rskok.rskP0030Comgl3("D008", 0, 0) == -1) {
//            errmsg(oo_rskok.mesg());
//            return;
//         }
//         ooAcct.rskokFlag("Y");
//      }
//      else {
//         //wf_write_comgl5(I,'DP03',prb_amount,merchant_repay)
//         if (ooAcaj.rskP0030Comgl5("DP03-1") == -1) {
//            errmsg(ooAcaj.mesg());
//            return;
//         }
//         ooAcct.acajFlag("Y");
//      }
	   
	   if(colEq("A.back_flag","Y")) {
		   if (ooAcaj.rskP0030Comgl5("DP03-1") == -1) {
	           errmsg(ooAcaj.mesg());
	           return;
	        }
	        ooAcct.acajFlag("Y");
	   }	   	   
	   
      return;
   }
   //--
   if (pos("|D7", lsCloResult) > 0) {
	   //--若人工新增時有回存後續須作人工加檔、起帳，會計帳才平
	   //--若人工新增時無回存特店退款時會自動回存、起帳
//      if (lb_SQ) {
//         if (oo_rskok.rskP0030RD7() == -1) {
//            errmsg(oo_rskok.mesg());
//            return;
//         }
//         ooAcct.rskokFlag("Y");
//      }
//      else {
//         if (ooAcaj.rskP0030Comgl5("DP03") == -1) {
//            errmsg(ooAcaj.mesg());
//            return;
//         }
//         ooAcct.acajFlag("Y");
//      }
      return;
   }
   //--
   if (pos("|D8,DA", lsCloResult) > 0) {
	   if(colEq("A.back_flag","Y") == false) {
		   ooAcaj.rskp0030VdBack("DP01",colStr("A.reference_no"));
		   ooAcct.acajFlag("Y");
	   }
      return;
   }
   //--
//-- 20230714 取消
//   if (pos("|D9", lsCloResult) > 0) {
//      if (!checkSysVouch("R-D9", 4)) return;
//      if (lb_SQ && colEq("A.rsk_type", 5)) {
//         if (ooAcaj.rskP0030Insert("RE10") == -1) {
//            errmsg(ooAcaj.mesg());
//            return;
//         }
//         ooAcct.acajFlag("Y");
//      }
//      ooAcct.stdVouchCd("R-D9");
//      return;
//   }
//   //--
//   if (pos("|N5", lsCloResult) > 0) {
//      if (lb_SQ) {
//         if (ooAcaj.rskP0030Insert("RE20") == -1) {
//            errmsg(ooAcaj.mesg());
//            return;
//         }
//         ooAcct.acajFlag("Y");
//      }
//      return;
//   }
   
   //--20230714 N9改為D9
   if (pos("|D9", lsCloResult) > 0) {
	   if(colEq("A.back_flag","Y") == false) {
		   ooAcaj.rskp0030VdBack("DP01",colStr("A.reference_no"));
		   ooAcct.acajFlag("Y");
	   }
      return;
   }
      
}

void closeVouch() {
   String lsCloResult = colStr("A.clo_result");
   boolean lb_noSQ = colNeq("A.prb_src_code", "SQ");
   String ls_paytype = colStr("A.payment_type");

   ActAcaj ooAcaj = new ActAcaj();
   ooAcaj.setConn(wp);
   ooAcaj.setRskCtrlSeqno(isRskCtrlSeqno);

   BilRskok oo_rskok = new BilRskok();
   oo_rskok.setConn(wp);
   oo_rskok.ibDebit =false;
   oo_rskok.setrskCtrlSeqno(isRskCtrlSeqno);

   //本行損失==>不恢復[R021]
   if (pos("|11,12,13,14,61,62,63", lsCloResult) > 0) {
      if (lb_noSQ) {
         if (ooAcaj.rskP0030DP02() != 1) {
            errmsg(ooAcaj.getMsg());
            return;
         }
         ooAcct.acajFlag("Y");
      }
      ooAcct.stdVouchCd("A012");
      return;
   }
//   //暫掛偽簽款==>不恢復; 0522:10
//   if (pos("|15|16|17|65|66|67|10", lsCloResult) > 0) {
//      if (lb_noSQ) {
//         if (ooAcaj.rskP0030Comgl4("DP02") != 1) {
//            errmsg(ooAcaj.getMsg());
//            return;
//         }
//         ooAcct.acajFlag("Y");
//      }
//      ooAcct.stdVouchCd("R022");
//      return;
//   }
   //扣款成功==>不恢復
   if (pos("|18,20", lsCloResult) > 0) {
      if (lb_noSQ) {
         if (ooAcaj.rskP0030Comgl4("DP02") != 1) {
            errmsg(ooAcaj.getMsg());
            return;
         }
         ooAcct.acajFlag("Y");
      }
      ooAcct.stdVouchCd("A018");
      return;
   }
//   //暫掛收單問題款==>不恢復
//   if (pos("|19", lsCloResult) > 0) {
//      if (lb_noSQ) {
//         if (ooAcaj.rskP0030Comgl4("DP02") != 1) {
//            errmsg(ooAcaj.getMsg());
//            return;
//         }
//         ooAcct.acajFlag("Y");
//      }
//      ooAcct.vouchFlag("Y");
//      ooAcct.stdVouchCd("R019");
//      return;
//   }
   //特店自動退款==>恢復 
   if (pos("|21", lsCloResult) > 0) {
      if (lb_noSQ) {
         if (ooAcaj.rskP0030Comgl4("DP03") != 1) {
            errmsg(ooAcaj.getMsg());
            return;
         }
         ooAcct.acajFlag("Y");
      }
      else {
         rc = oo_rskok.rskP0030R024();
         if (rc != 1) {
            errmsg(oo_rskok.getMsg());
            return;
         }
         ooAcct.rskokFlag("Y");
      }
      return;
   }
   //C/H付本金利息==>恢復
   if (pos("|31,32,33,34,71,72,73", lsCloResult) > 0) {
      if (lb_noSQ) {
         if (ooAcaj.rskP0030Comgl4("DP03-1") != 1) {
            errmsg(ooAcaj.mesg());
            return;
         }
         ooAcct.acajFlag("Y");
      }
      else {
//         if (eqAny(lsCloResult, "34") && pos("|E|I", ls_paytype) > 0) {
//            if (oo_rskok.rskP0030Comgl3("R261", 0, 0) == -1) {
//               errmsg(oo_rskok.mesg());
//               return;
//            }
//            ooAcct.rskokFlag("Y");
//         }
//         else {
//            if (oo_rskok.rskP0030Comgl3("R026", 0, 0) == -1) {
//               errmsg(oo_rskok.mesg());
//               return;
//            }
//            ooAcct.rskokFlag("Y");
//         }
    	  
    	  if (oo_rskok.rskP0030Comgl3("A016", 0, 0) == -1) {
              errmsg(oo_rskok.mesg());
              return;
           }
           ooAcct.rskokFlag("Y");
    	  
      }
      return;
   }
   //C/H付本金==>恢復
   if (pos("|41,42,43,44,81,82,83", lsCloResult) > 0) {
      if (lb_noSQ) {
         //if wf_write_comgl4(I,'DP03-2',prb_amount,merchant_repay)=-1 then Return -1
         if (ooAcaj.rskP0030Comgl4("DP03-2") == -1) {
            errmsg(ooAcaj.mesg());
            return;
         }
         ooAcct.acajFlag("Y");
      }
      else {
//         if (pos("|44", lsCloResult) > 0 && pos("|E,I", ls_paytype) > 0) {
//            //if wf_write_comgl3(I,prb_amount,merchant_repay,'R271','N','N')=-1 then Return -1
//            if (oo_rskok.rskP0030Comgl3("R271", 0, 0) == -1) {
//               errmsg(oo_rskok.mesg());
//               return;
//            }
//            ooAcct.rskokFlag("Y");
//         }
//         else {
//            //if wf_write_comgl3(I,prb_amount,merchant_repay,'R027','N','N')=-1 then Return -1
//            if (oo_rskok.rskP0030Comgl3("R027", 0, 0) == -1) {
//               errmsg(oo_rskok.mesg());
//               return;
//            }
//            ooAcct.rskokFlag("Y");
//         }
    	  
    	  if (oo_rskok.rskP0030Comgl3("A016", 0, 0) == -1) {
              errmsg(oo_rskok.mesg());
              return;
           }
           ooAcct.rskokFlag("Y");
    	  
      }
      return;
   }
   
   if (pos("|45", lsCloResult) > 0) {
	   if (lb_noSQ) {		   
	       //if wf_write_comgl4(I,'DP03-2',prb_amount,merchant_repay)=-1 then Return -1
		   if (ooAcaj.rskP0030Comgl4("DP03-3") == -1) {
			   errmsg(ooAcaj.mesg());
	           return;
		   }
		   ooAcct.acajFlag("Y");
	   }	else	{
		   if (oo_rskok.rskP0030Comgl3("A016", 0, 0) == -1) {			   
	           errmsg(oo_rskok.mesg());
	           return;
	       }
	       ooAcct.rskokFlag("Y");
	   }
   }
   
   //** 系統列問交  *****************************
   //特店退錯款沖回(系統)
   if (eqAny(lsCloResult, "51")) {
      //if wf_R028_vouch(I)<>1 then Return -1
      ooAcct.stdVouchCd("R028");
      return;
   }
   //== 不合格帳單  ==============================
   //不合格帳單 本行損失
   if (pos("|01,02", lsCloResult) > 0) {
      //if wf_R041_vouch(I)<>1 then Return -1
      ooAcct.stdVouchCd("A012");
      return;
   }
   if (pos("|03", lsCloResult) > 0) {
      //if wf_R042_rskok(I)<>1 then Return -1
      if (oo_rskok.rskP0030Insert("R042") == -1) {
         errmsg(oo_rskok.mesg());
         return;
      }
      ooAcct.rskokFlag("Y");
      return;
   }
   if (pos("|55", lsCloResult) > 0) {
      if (oo_rskok.rskP0030Insert("R055") == -1) {
         errmsg(oo_rskok.mesg());
         return;
      }
      ooAcct.rskokFlag("Y");
      return;
   }
   if (pos("|04,05", lsCloResult) > 0) {
      if (oo_rskok.rskP0030Comgl3("R043", 1, 0) == -1) {
         errmsg(oo_rskok.mesg());
         return;
      }
      ooAcct.rskokFlag("Y");
      return;
   }
   if (pos("|06", lsCloResult) > 0) {
      //wf_R044_vouch(I)
      ooAcct.stdVouchCd("A018");
      return;
   }
   if (pos("|07", lsCloResult) > 0) {
      //wf_R045_vouch(I)
      ooAcct.stdVouchCd("A016");
      return;
   }
   if (eq("I1", lsCloResult)) {
      //-JH:20210728-
//      if (oo_rskok.rskp0030_I1("P-13") == -1) {
//         errmsg(oo_rskok.mesg());
//         return;
//      }
//      ooAcct.rskok_flag("Y");
	   insertBilBackLog();
      ooAcct.rskokFlag("2");  //bil_back_log
      return;
   }
   if (eq("I2", lsCloResult)) {
      if (oo_rskok.rskP0030Insert("R055") == -1) {
         errmsg(oo_rskok.mesg());
         return;
      }
      ooAcct.rskokFlag("Y");
      return;
   }
   if (eq(lsCloResult,"I3")) {
      ooAcct.stdVouchCd("R045");
      return;
   }
   //## 特殊交易(For Acct)=======================
   //C/B 成功結案  (D消費款)==>取消
   if (pos("|93", lsCloResult) > 0) {
      if (colEq("A.prb_mark", "S")) {
         if (ooAcaj.rskP0030Comgl4("DP04") == -1) {
            errmsg(ooAcaj.mesg());
            return;
         }
         ooAcct.acajFlag("Y");
      }
      return;
   }
   //## 特殊交易結案後新增一筆問交 =================
   //特交轉問題交易==>新增 爭議款
   if (pos("|92", lsCloResult) > 0) {
      if (colNeq("A.prb_mark", "S")) {
         return;
      }
      RskProblem oo_prbl = new RskProblem();
      oo_prbl.setConn(wp);
      oo_prbl.varsSet("rowid", varsStr("rowid"));
      if (oo_prbl.rskP0030NewPRBL() == -1) {
         errmsg(oo_prbl.mesg());
         return;
      }
      ooAcct.newPrblFlag("Y");

      if (ooAcaj.rskP0030Comgl4("DP01") == -1) {
         errmsg(ooAcaj.mesg());
         return;
      }
      return;
   }

   //-OK-
//	if (ooAcct.rskP0030_Approve()!=1) {
//		errmsg(ooAcct.mesg());
//	}
   return;
}

void insertBilBackLog() {
   //-bilp0140-
   String lsContrNo =colStr("A.contract_no");
   if (empty(lsContrNo)) {
      return;
   }

   String hBlogContractNo = "";
   String hBlogCardNo = "";
   String hBlogMerchantNo = "";
   String hBlogProductNo = "";
   String hContFeeFlag = "";
   double hBlogCurrTerm = 0;
   double hBlogUnitPriceOld = 0;
   double hBlogRemdAmtOld = 0;
   double h_blog_remd_amt_tail = 0;
   double h_cont_clt_fees_amt = 0;
   double h_cont_clt_unit_price = 0;
   double h_cont_clt_remd_amt = 0;
   double hContFirstRemdAmt = 0;
   double hTmpContTotAmt = 0;
   double hBlogRedeemAmt = 0;
   double hBlogRedeemPoint = 0;
   String h_tmp_cont_first_post_date = "";
   String hContRowid="";

   strSql ="select A.*, hex(rowid) as rowid from bil_contract A "+
           " where A.contract_no =? and A.contract_seq_no =1"+commSqlStr.rownum(1);
   daoTid ="cont.";
   sqlSelect(strSql,lsContrNo);
   if (sqlRowNum <=0) {
      return;
   }

   hBlogContractNo         = colStr("cont.contract_no");
   hBlogCardNo             = colStr("cont.card_no");
   hBlogMerchantNo         = colStr("cont.mcht_no");
   hBlogProductNo          = colStr("cont.product_no");
   hContFeeFlag            = colNvl("cont.fee_flag","L");
   hBlogCurrTerm           = colNum("cont.install_curr_term");
   hBlogUnitPriceOld      = colNum("cont.unit_price");
   hBlogRemdAmtOld        = colNum("cont.first_remd_amt");
   hContFirstRemdAmt      = colNum("cont.first_remd_amt");
   h_blog_remd_amt_tail       = colNum("cont.remd_amt");
   h_cont_clt_fees_amt        = colNum("cont.clt_fees_amt");
   h_cont_clt_unit_price      = colNum("cont.clt_unit_price");
   h_cont_clt_remd_amt        = colNum("cont.clt_remd_amt");
//   hTmpContTotAmt         = col_num("tot_amt");
//   h_tmp_cont_first_post_date = colStr("first_post_date");
   hBlogRedeemAmt          = colNum("cont.redeem_amt");
   hBlogRedeemPoint        = colNum("cont.redeem_point");
   hContRowid = colStr("cont.rowid");

   double hBlogBackTerm = 0;
   double hBlogUnitPriceNew = 0;
   double hBlogRemdAmtNew = 0;
   double hBlogBackAmt=0;
   if (eq(commString.mid(hContFeeFlag,0,1),"F"))
      hBlogBackAmt = (hBlogUnitPriceOld - hBlogUnitPriceNew) * hBlogCurrTerm
              + hContFirstRemdAmt;
   else
      hBlogBackAmt = (hBlogUnitPriceOld - hBlogUnitPriceNew) * hBlogCurrTerm
              + (hBlogRemdAmtOld - hBlogRemdAmtNew);

   if (hBlogCurrTerm == 0) {
      hBlogBackAmt = 0;
   }
   String hContCpsFlag="Y";
   String hCurpBillType =colStr("A.bill_type");
   if (eq(commString.mid(hCurpBillType,0,2),"NC")) {
      hContCpsFlag = "C";
   }

   //String ls_refno =colStr("A.reference_no");
   String lsGlmemo3 =colStr("A.prb_glmemo3");

   if (hBlogCurrTerm >0) {
      sql2Insert("bil_back_log");
      addsqlParm("?","contract_no", hBlogContractNo);
      addsqlParm(", ? ",", contract_seq_no", 1);
      addsqlParm(", ? ",", card_no", hBlogCardNo);
      addsqlParm(", ? ",", product_no", hBlogProductNo);
      addsqlParm(", ? ",", back_kind", "1");
      addsqlParm(", ? ",", mcht_no", hBlogMerchantNo);
      addsqlParm(", ? ",", refund_qty", 1);
      addsqlParm(", ? ",", back_amt", hBlogBackAmt);
      addsqlParm(", ? ",", curr_term", hBlogCurrTerm);
      addsqlParm(", ? ",", back_term", hBlogBackTerm);
      addsqlParm(", ? ",", contract_kind", "1");
      addsqlParm(", ? ",", unit_price_old", hBlogUnitPriceOld);
      addsqlParm(", ? ",", unit_price_new", hBlogUnitPriceNew);
      addsqlParm(", ? ",", remd_amt_old"  , hBlogRemdAmtOld);
      addsqlParm(", ? ",", remd_amt_new"  , hBlogRemdAmtNew);
      addsqlParm(", ? ",", remd_amt_tail" , h_blog_remd_amt_tail);
      addsqlParm(", ? ",", clt_fees_amt"  , h_cont_clt_fees_amt);
      addsqlParm(", ? ",", clt_unit_price", h_cont_clt_unit_price);
      addsqlParm(", ? ",", clt_remd_amt"  , h_cont_clt_remd_amt);
      addsqlParm(", ? ",", post_flag", "N");
      addsqlParm(", ? ",", cps_flag" , hContCpsFlag);
      addsqlParm(", ? ",", fee_flag" , hContFeeFlag);
      addsqlParm(", ? ",", redeem_point", hBlogRedeemPoint);
      addsqlParm(", ? ",", redeem_amt"  , hBlogRedeemAmt);
      //-20210728-reference_no, std_vouch_cd, key_value
      addsqlParm(", ? ",", reference_no", isReferenceNo);
      addsqlParm(", ? ",", std_vouch_cd", "P-12");
      addsqlParm(", ? ",", key_value", lsGlmemo3);
      addsqlParm(", ? ",", mod_user" , modUser);
      addsqlDate(", mod_time");
      addsqlParm(", ? ",", mod_pgm" , modPgm);

      sqlExec(sqlStmt(),sqlParms());
      if (sqlRowNum <=0) {
         errmsg("insert BIL_BACK_LOG error, kk[%s]", hBlogContractNo);
         return;
      }
   }

   //---------------------------------------------------------
   sql2Update("bil_contract");
   addsqlParm("refund_batch_no = ? ", "");
   addsqlParm(", refund_flag = ? ", "Y");
   addsqlParm(", refund_apr_flag = ? ", "Y");
   addsqlParm(", refund_qty = ? ", 1);
   addsqlParm(", unit_price = ? ", 0);
   addsqlParm(", remd_amt = ? ", 0);
   addsqlParm(", first_remd_amt = ? ", 0);
   addsqlParm(", redeem_amt = ? ", 0);
   addsqlParm(", redeem_point = ? ", 0);
   addsqlParm(", refund_reference_no = ? ", isReferenceNo);
   addsqlYmd(", refund_apr_date");
   sqlWhereRowid(hContRowid);

   sqlExec(sqlStmt(),sqlParms());
   if (sqlRowNum <=0) {
      errmsg("update bil_contract error", hBlogContractNo);
   }
}


//-第一次結案-------------------------------------
@Override
public int dataProc() {
   msgOK();

   ooAcct.setConn(wp);
   dataCheck();
   if (rc != 1)
      return rc;

   String lsSeqno = colStr("A.ctrl_seqno");

   //-啟帳+會計分錄------------------------
   ooAcct.initData();
   String lsGlUser = wp.itemStr("A.close_add_user");
   ooAcct.initFunc(lsGlUser);
   ooAcct.tableId("PRBL");
   ooAcct.rskStatus("801");
   ooAcct.ctrlSeqno(lsSeqno);   //wp.itemStr("A.ctrl_seqno")
   ooAcct.binType(wp.itemStr("A.bin_type"));
   ooAcct.referenceNo(isReferenceNo);  //wp.itemStr("A.reference_no")
   ooAcct.debitFlag(wp.itemNvl("A.debit_flag", "N"));  //"N";
   ooAcct.cardNo(wp.itemStr("A.card_no"));
   ooAcct.cloResult(wp.itemStr("A.clo_result"));
   ooAcct.overseaFeeFlag(wp.itemNvl("A.oversea_flag", "N"));
   ooAcct.vouchMergeFlag(wp.itemNvl("A.clo_merge_vouch", "N"));
   ooAcct.glMemo3(wp.itemStr("A.prb_glmemo3"));
   ooAcct.crtUser(lsGlUser);

   if (ibDebit) {
      closeVouchVD();
   }
   else closeVouch();
   if (rc != 1)
      return rc;

   if (ooAcct.dbInsert() != 1) {
      errmsg(ooAcct.mesg());
      return rc;
   }

   updateProblemApprove();
   if (rc != 1) {
      return rc;
   }
   //--
   updateChgback(isReferenceNo);
   if (rc != 1)
      return rc;

   wfRskCtfcTxn();
   //-JH2020-0414-
   updatePrecomplClose(isReferenceNo);
   updatePreArbitClose(isReferenceNo);
   if (rc!=1) {
      rc=1;
   }

   return rc;
}

//-第二次結案-----------------------------------------------------------------------
void dataCheck2() throws Exception  {
   dataCheck();
   if (colNeq("A.prb_status", "83")) {
      errmsg("狀態不是 [結案(二)待覆核]");
      return;
   }
}

public int dataProc2() throws Exception  {
   return 1;
   //-取消二次結案-

//   iiCloType = 2;
//   ooAcct.setConn(wp);
//   String ls_refno =wp.itemStr("A.reference_no");
//   dataCheck_2();
//   if (rc != 1)
//      return rc;
//
//   //-啟帳+會計分錄-
//   ooAcct.init_data();
//   String ls_gluser = wp.itemStr("A.close_add_user");
//   ooAcct.init_func(ls_gluser);
//
//   ooAcct.table_id("PRBL");
//   ooAcct.rsk_status("802");
//   ooAcct.ctrl_seqno(wp.itemStr("A.ctrl_seqno"));
//   ooAcct.bin_type(wp.itemStr("A.bin_type"));
//   ooAcct.reference_no(wp.itemStr("A.reference_no"));
//   ooAcct.debit_flag(wp.item_nvl("A.debit_flag", "N"));
//   ooAcct.card_no(wp.itemStr("A.card_no"));
//   ooAcct.clo_result(wp.itemStr("A.clo_result"));
//   ooAcct.oversea_fee_flag(wp.item_nvl("A.oversea_flag", "N"));
//   ooAcct.vouch_merge_flag("N"); //wp.itemStr("A.clo_merge_vouch");
//   ooAcct.gl_memo3(wp.itemStr("A.prb_glmemo3"));
//
//   if (ibDebit) {
//      closeVouchVD();
//   }
//   else close_Vouch();
//   if (rc != 1)
//      return rc;
//
//   if (ooAcct.dbInsert() != 1) {
//      errmsg(ooAcct.mesg());
//      return rc;
//   }
//
//   update_Problem_approve();
//   if (rc != 1) {
//      return rc;
//   }
//
//   wfRskCtfcTxn();
//
//   //-JH2020-0414-
//   updatePrecomplClose(ls_refno);
//   updatePreArbitClose(ls_refno);
//
//   return rc;
}

void wfRskCtfcTxn() {
   strSql = "select case_no"
         + " from rsk_ctfc_mast"
         + " where card_no =?"
         + commSqlStr.rownum(1);
   setString(1, colStr("A.card_no"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0)
      return;
   String lsCaseNo =colStr("case_no");
   if (empty(lsCaseNo))
      return;

   String lsCtrlSeqno = wp.itemStr("A.ctrl_seqno");
   strSql = "select count(*) as db1"
         + " from rsk_ctfc_txn"
         + " where case_no =?"
         + " and ctrl_seqno =?";
   setString(1, lsCaseNo);
   setString(2, lsCtrlSeqno);
   sqlSelect(strSql);
   if (sqlRowNum <=0) return;
   if (colNum("db1") <= 0) {
      return;
   }

   String lsTtCloResult=colStr("A.tt_clo_result");
   lsTtCloResult =commString.mid(lsTtCloResult,0,60);
   strSql = "update rsk_ctfc_txn set"
         + " ecs_close_date =" + commSqlStr.sysYYmd
         + ", ecs_close_reason =?" //+colStr("A.tt_clo_result").substring(0,60);
         + ", mod_user =?"
         + ", mod_time =sysdate"
         + ", mod_pgm =?"
         + ", mod_seqno =nvl(mod_seqno,0)+1"
         + " where case_no =?"
         + " and ctrl_seqno =?";
   sqlExec(strSql, new Object[]{
         lsTtCloResult,
         modUser,
         modPgm,
           lsCaseNo,
         lsCtrlSeqno
   });
   if (sqlRowNum < 0) {
      wp.log("update RDK_CTFC_TXN error, kk=" + lsCaseNo);
   }
}

int updateProblemApprove() {
   msgOK();
   if (iiCloType == 2) {
      strSql = " update rsk_problem set "
            + " prb_status ='85',"
            + " close_apr_date_2 = to_char(sysdate,'yyyymmdd') , "
            + " close_apr_user_2 =:close_apr_user ,"
            + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =:rowid ";
   }
   else {
      strSql = " update rsk_problem set "
            + " prb_status ='80',"   //'80' , "
            + " close_apr_date = to_char(sysdate,'yyyymmdd') , "
            + " close_apr_user =:close_apr_user ,"
            + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =:rowid ";
   }

   setString("close_apr_user", modUser);
   this.setRowId("rowid", varsStr("rowid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_problem error !");
      return rc;
   }

   return rc;
}

public int cancelProc() {
   iiCloType = 1;
   dataCheckCancel();
   if (rc != 1)
      return rc;

   //-delete:vouch,acaj-
   deleteAcctLog();
   if (rc == -1) {
      return rc;
   }

   updateProblemCancel();

   return rc;
}

void dataCheck_Cancel_2() throws Exception  {
   daoTid = "A.";
   String sql1 = "select "
         + " close_apr_date_2,"
         + " close_add_date_2,"
         + " close2_vouch_date,"
         + " ctrl_seqno, bin_type, debit_flag,"
         + " prb_status"
         + " from rsk_problem "
         + " where 1=1 "
         + " and rowid = ? "
         + " and nvl(mod_seqno,0) = ? ";
   
   setRowId(1,varsStr("rowid"));
   setString(2,varsStr("mod_seqno"));
   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      errmsg("資料錯誤 請重新讀取");
      return;
   }
   ibDebit = colEq("A.debit_flag","Y");
   isRskCtrlSeqno = getRskCtrlSeqno();

   if (colNeq("A.prb_status", "85")) {
      errmsg("不是[結案二]覆核狀態, 不可解覆核");
      return;
   }

   if (!eqIgno(colStr("A.close_apr_date_2"), getSysDate())) {
      errmsg("不是當日覆核, 不可解覆核");
      return;
   }

//	if (!empty(colStr("A.close_vouch_date"))) {
//		errmsg("結案啟帳, 不可取消");
//		return;
//	}
   strSql = "select count(*) as db_cnt from rsk_acct_log"
         + " where ctrl_seqno =?"
         + " and bin_type =?"
         + " and rsk_status ='802'"
         + " and table_id ='PRBL'"
         + " and vouch_proc_flag ='Y'";
   setString(1, colStr("A.ctrl_seqno"));
   setString(2, colStr("A.bin_type"));
   sqlSelect(strSql);
   if (colNum("db_cnt") > 0) {
      errmsg("(第二次)結案已啟帳, 不可[解覆核]");
      return;
   }

   //-OK-
   col2wpItem("A.");

   //-???check rsk_acct_log-
   strSql = "select count(*) as db_cnt from bil_rskok"
         + " where rsk_ctrl_seqno =?"
         + " and post_flag ='Y'";
   setString(1, isRskCtrlSeqno);
   sqlSelect(strSql);
   if (sqlRowNum < 0) {
      errmsg("select bil_rskok error, kk=" + isRskCtrlSeqno);
      return;
   }
   if (sqlRowNum > 0 && colNum("db_cnt") > 0) {
      errmsg("已執行重新啟帳處理, 不可[解覆核]");
      return;
   }

   //check act_acaj--
   if (ibDebit) {
      strSql = "select count(*) from dba_acaj"
            + " where rsk_ctrl_seqno =?"
            + " and proc_flag ='Y'";
   }
   else {
      strSql = "select count(*) from act_acaj"
            + " where rsk_ctrl_seqno =?"
            + " and process_flag ='Y'";
   }
   setString(1, isRskCtrlSeqno);
   sqlSelect(strSql);
   if (sqlRowNum < 0) {
      errmsg("select act[dba]_acaj error, kk=" + isRskCtrlSeqno);
      return;
   }
   if (sqlRowNum > 0 && colNum("db_cnt") > 0) {
      errmsg("已執行帳務調整處理, 不可[解覆核]");
      return;
   }
}

public int cancel_Proc_2() throws Exception  {
   return 1;
   //-取消二次結案-
//   iiCloType = 2;
//   dataCheck_Cancel_2();
//   if (rc != 1)
//      return rc;
//
//   //-delete:vouch,acaj-
//   deleteAcctLog();
//   if (rc == -1) {
//      return rc;
//   }
//
//   updateProblemCancel();
//   if (rc != 1)
//      return rc;
//
//   return rc;
}

void deleteAcctLog() {
   RskAcctLog oo_vouch = new RskAcctLog();
   oo_vouch.setConn(wp);
   rc = oo_vouch.dbDelete(colStr("A.ctrl_seqno")
         , colStr("A.bin_type"), "80" + iiCloType, "PRBL");
   if (rc < 0) {
      errmsg(oo_vouch.getMsg());
      return;
   }

   ActAcaj ooAcaj = new ActAcaj();
   DbaAcaj ooAcaj2 = new DbaAcaj();

   if (colNeq("A.debit_flag", "Y")) {
      ooAcaj.setConn(wp);
      if (ooAcaj.dbDelete(isRskCtrlSeqno) < 0)
         errmsg(ooAcaj.getMsg());
   }
   else {
      ooAcaj2.setConn(wp);
      if (ooAcaj2.dbDelete(isRskCtrlSeqno) < 0)
         errmsg(ooAcaj2.getMsg());
   }

   BilRskok oo_rskok = new BilRskok();
   oo_rskok.setConn(wp);
   if (oo_rskok.dbDelete(isReferenceNo) < 0)
      errmsg(oo_rskok.mesg());

}

int updateProblemCancel() {
   msgOK();
   if (iiCloType == 2) {
      strSql = " update rsk_problem set "
            + " prb_status ='83' ,"
            + " close_apr_date_2 = '' ,"
            + " close_apr_user_2 = '' ,"
            + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =:rowid ";
   }
   else {
      strSql = " update rsk_problem set "
            + " prb_status ='60' ,"
            + " close_apr_date = '' ,"
            + " close_apr_user = '' ,"
            + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =:rowid ";
   }

   this.setRowId("rowid", varsStr("rowid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_problem.Cancel error=" + this.sqlErrtext);
      return rc;
   }

   return rc;
}

String convPreClose() {
   if (empty(isCloResult)) return "";

   String lsClose="";
   if (commString.strIn(isCloResult,"|11.12.13.15.16.17.19.D0.D9.N9")) {
      //11.12.13.15.16.17.19→本行負擔(22)
      //DO.D9.N9→22.本行負擔
      lsClose ="22";
   }
   else if (commString.strIn(isCloResult,"|14")) {
      //14-失敗→收單行拒絕(21)
      lsClose ="21";
   }
   else if (commString.strIn(isCloResult,"|18.20.D8.DA")) {
      //18.20-成功→收單行退款(11)
      //D8.DA→11.收單行退款
      lsClose ="11";
   }
   else if (commString.strIn(isCloResult,"|31.32.33.34.41.42.43.44.D6")) {
      //31.32.33.34.41.42.43.44→持卡人自付(13)
      //D6→13.持卡人自付
      lsClose ="13";
   }
   else if (commString.strIn(isCloResult,"|21.D7")) {
      //21→商店退貨(12)
      //D7→12.商店退貨
      lsClose ="12";
   }
   else if (commString.strIn(isCloResult,"|51.61.62.63.65.66.67.71.72.73.81.82.83")) {
      //51.61.62.63.65.66.67.71.72.73.81.82.83→其他(24)
      lsClose ="24";
   }
   else {
      //其他理由碼→24.其他
      if (ibDebit) {
         lsClose ="24";
      }
   }

   return lsClose;
}

int updatePrecomplClose(String aRefno) {
   if (empty(aRefno))
      return 0;
   String lsClose=convPreClose();
   if (empty(lsClose))
      return 0;

   strSql = "select COMPL_TIMES, pre_close_date, com_close_date "+
         " from rsk_precompl where reference_no =? and reference_seq=0";
   sqlSelect(strSql, aRefno);
   if (sqlRowNum <= 0) return 0;

   int li_time = colInt("compl_times");
   //-只連動預備依從-
   if (li_time != 1) return 0;

   if (li_time == 1) {
      strSql = "update rsk_precompl set"
            + " pre_status ='80'"
            + ", pre_clo_result =?"
            + ", pre_close_date =" + commSqlStr.sysYYmd
            + ", pre_close_user = 'system'"
            + ", close_apr_date =" + commSqlStr.sysYYmd + ", close_apr_user='rskp0030'"
            + ", " + commSqlStr.setModxxx(modUser, modPgm)
            + " where reference_no =? and reference_seq=0";
   }
//   else if (li_time == 2) {
//      strSql = "update rsk_precompl set"
//            + " com_status ='80'"
//            + ", com_clo_result =?"
//            + ", com_close_date =" + commSqlStr.sys_YYmd
//            + ", com_close_user =?"
//            + ", close_apr_date =" + commSqlStr.sys_YYmd + ", close_apr_user='rskp0030'"
//            + ", " + commSqlStr.setMod_xxx(mod_user, mod_pgm)
//            + " where reference_no =? and reference_seq=0";
//   }
   setString(lsClose);
//   ppp(mod_user);
   setString(aRefno);
   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("update rsk_precompl[%s] error, kk[%s]", li_time, aRefno);
      return -1;
   }

   return 1;
}

int updatePreArbitClose(String aRefno) {
   if (empty(aRefno))
      return 0;
   String lsClose=convPreClose();
   if (empty(lsClose)) return 0;

   strSql = "select arbit_times, pre_close_date, arb_close_date "+
         " from rsk_prearbit where reference_no =? and reference_seq=0";
   sqlSelect(strSql, aRefno);
   if (sqlRowNum <= 0) return 0;

   int li_time = colInt("arbit_times");
   //-只連動預備仲裁-
   if (li_time != 1) return 0;

   if (li_time == 1) {
      strSql = "update rsk_prearbit set"
            + " pre_status ='80'"
            + ", pre_result =?"
            + ", pre_close_date =" + commSqlStr.sysYYmd
            + ", pre_close_user = 'system'"
            + ", close_apr_date =" + commSqlStr.sysYYmd + ", close_apr_user='rskp0030'"
            + ", " + commSqlStr.setModxxx(modUser, modPgm)
            + " where reference_no =? and reference_seq=0";
   }
//   else if (li_time == 2) {
//      strSql = "update rsk_prearbit set"
//            + " arb_status ='80'"
//            + ", arb_result =?"
//            + ", arb_close_date =" + commSqlStr.sys_YYmd
//            + ", arb_close_user =?"
//            + ", close_apr_date =" + commSqlStr.sys_YYmd + ", close_apr_user='rskp0030'"
//            + ", " + commSqlStr.setMod_xxx(mod_user, mod_pgm)
//            + " where reference_no =? and reference_seq=0";
//   }
   setString(lsClose);
   //ppp(mod_user);
   setString(aRefno);
   sqlExec(strSql);

   if (sqlRowNum <= 0) {
      errmsg("update rsk_prearbit[%s] error, kk[%s]", li_time, aRefno);
      return -1;
   }

   return 1;
}

}
