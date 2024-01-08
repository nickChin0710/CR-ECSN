package rskm01;

/**
 * 2020-0319	JH		理由碼
 */

public class Rskm0110Func extends busi.FuncAction {

String kk1 = "", kk2 = "", isRefno = "";
String isRefnoOri = "";
boolean ibDebitFlag = false;
rskm01.RskReceipt oorept = null;

//@Override
//public int querySelect() {
//	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//}

//@Override
//public int dataSelect() {
//	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//}

void selectRskReceipt() {
   daoTid = "A.";
   strSql = "select rept_status, send_flag, send_cnt "
         + ", debit_flag"
         + " from rsk_receipt "
         + " where 1=1"
//         + zzsql.col(kk1, "ctrl_seqno")
//         + zzsql.col(kk2, "bin_type")
//         + " and nvl(mod_seqno,0) =" + wp.mod_seqno()
		;
   
   int ii=0 ;
   if(empty(kk1) == false) {
	   strSql += " and ctrl_seqno = ? ";
	   ii++;
	   setString(ii,kk1);
   }
   
   if(empty(kk2) == false) {
	   strSql += " and bin_type = ? ";
	   ii++;
	   setString(ii,kk2);
   }
   
   if(wp.itemEmpty("mod_seqno") == false) {
	   strSql += " and nvl(mod_seqno,0) = ? ";
	   ii++;
	   setString(ii,wp.modSeqno());
   }
   
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("資料不存在 OR 已被修改");
      return;
   }
}

@Override
public void dataCheck() {
   ibDebitFlag = eqAny(wp.itemStr("debit_flag"), "Y");

   if (wp.itemEmpty("reference_no")) {
      errmsg("帳單參考序號: 不可空白");
      return;
   }

   isRefno = wp.itemStr("reference_no");
   isRefnoOri = wp.itemStr("reference_no_ori");
   kk1 = wp.itemStr("ctrl_seqno");
   kk2 = wp.itemStr("bin_type");

   //--
   if (oorept.checkReasonCode() != 1) {
      errmsg(oorept.getMsg());
      return;
   }

   if (isAdd())
      checkInsert();
   else if (isUpdate())
      checkUpdate();
   else if (isDelete())
      checkDelete();
   else errmsg("Action 不是A,U,D");
   return;
}

void checkInsert() {
   selectBilBill();
   if (empty(kk1)) {
      kk1 = colStr("bill-rsk_ctrl_seqno");
   }
   if (rc != 1) {
      return;
   }

   //-分期是否作過調單-
   if (wp.itemEmpty("reference_no_ori") == false) {
      strSql = "select count(*) as db_cnt"
            + " from bil_bill A, rsk_receipt B"
            + " where A.reference_no =B.reference_no"
            + " and A.reference_no_original =?";
      setString(1, wp.itemStr("reference_no_ori"));
      sqlSelect(strSql);
      if (colInt("db_cnt") > 0) {
         errmsg("分期帳單: 已作過調單");
         return;
      }
   }

   //-redeem-
   if (wp.itemEmpty("BL_payment_type") == false && wp.itemNum("BL_dc_dest_amt") == 0) {
      errmsg("消費金額 不正確, 無法執行此作業");
      return;
   }

   //--

   return;
}

void checkUpdate() {
   kk1 = wp.itemStr("ctrl_seqno");
   kk2 = wp.itemStr("bin_type");
   isRefno = wp.itemStr("reference_no");
   if (empty("kk1")) {
      errmsg("調單流水號: 不可空白");
      return;
   }

   selectRskReceipt();
   if (rc != 1) {
      return;
   }
   if (colStr("A.rept_status").compareTo("30") > 0 || colEq("A.send_flag", "1") == false || colNum("A.send_cnt") > 0) {
      errmsg("此狀態下不可修改, 刪除");
      return;
   }

   if (wp.itemEq("bin_type", "N") && wp.itemEq("rept_type", "1") == false) {
      errmsg("UCARD 調單類別 只可為 影本");
      return;
   }

   //-redeem-
   if (wp.itemEmpty("BL_payment_type") == false &&
         wp.itemNum("BL_dc_dest_amt") == 0) {
      errmsg("消費金額 不正確, 無法執行此作業");
      return;
   }

	/*
IF ib_copy=true THEN //for copy
	if dw_data.object.receipt_seqno[1]=is_receipt_seqno then
	   MessageBox("Msg","請輸入新的調單序號？！")
	   return -1
	end if
	dw_data.object.receipt_seqno[1]
END IF
	 */
}

void checkDelete() {
   selectRskReceipt();
   if (rc != 1)
      return;

   String ss = colStr("A.rept_status");   
   if (ss.compareTo("30") > 0 || colEq("A.send_flag", "1") == false ||
         colNum("A.send_cnt") > 0) {
      errmsg("此狀態下不可刪除？！");
      return;
   }

   return;
}

@Override
public int dbInsert() {
   oorept = new rskm01.RskReceipt();
   oorept.setConn(wp);

   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (oorept.rskm0110Insert() != 1) {
      errmsg(oorept.getMsg());
      return -1;
   }

   //-update bil_bill-
   kk1 = oorept.isCtrlSeqno;
   updateBilBill();
   if (rc != 1)
      return rc;

   wp.colSet("ctrl_seqno", oorept.isCtrlSeqno);
   return rc;
}

public int dbInsertP0015() {
   oorept = new rskm01.RskReceipt();
   oorept.setConn(wp);
   oorept.checkCardBase =false;

   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (oorept.rskm0110Insert() != 1) {
      errmsg(oorept.getMsg());
      return -1;
   }

   //-update bil_bill-
   kk1 = oorept.isCtrlSeqno;
   updateBilBill();
   if (rc != 1)
      return rc;

   wp.colSet("ctrl_seqno", oorept.isCtrlSeqno);
   return rc;
}

@Override
public int dbUpdate() {
   oorept = new rskm01.RskReceipt();
   oorept.setConn(wp);

   actionInit("U");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (oorept.rskm0110Update() != 1) {
      errmsg(oorept.getMsg());
      return -1;
   }

   return rc;
}

@Override
public int dbDelete() {
   oorept = new RskReceipt();
   oorept.setConn(wp);

   actionInit("D");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   if (oorept.dataDelete() != 1) {
      errmsg(oorept.getMsg());
      return -1;
   }

   return rc;
}

@Override
public int dataProc() {
   return 0;
}

void updateBilBill() {
   if (ibDebitFlag) {
      strSql = "update dbb_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post)"
            + ", rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   else {
      strSql = "update bil_bill set"
            + " rsk_post =decode(rsk_post,'','O',rsk_post)"
            + ", rsk_ctrl_seqno =?  "
            + " where reference_no =?"
      ;
   }
   this.sqlExec(strSql, new Object[]{kk1, wp.itemStr("reference_no")});
   if (sqlRowNum != 1) {
      errmsg("update bil[dbb]_bill error; " + sqlErrtext);
   }
}

void selectBilBill() {
   daoTid = "bill-";
   if (ibDebitFlag) {
      strSql = "select rsk_ctrl_seqno "
            + " from dbb_bill B"
            + " where B.reference_no =?";
   }
   else {
      strSql = "select rsk_ctrl_seqno, contract_no "
            + " from bil_bill"
            + " where reference_no =?";
   }

   sqlSelect(strSql, new Object[]{isRefno});
   if (sqlRowNum <= 0) {
      errmsg("select BIL[DBB]_BILL no-find, refno=" + isRefno);
   }

}
}
