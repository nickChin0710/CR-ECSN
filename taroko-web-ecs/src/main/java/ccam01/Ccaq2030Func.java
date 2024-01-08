package ccam01;

/****************************************************************************
*                              MODIFICATION LOG                             *
*                                                                           *
*   DATE      Version   AUTHOR      DESCRIPTION                             *
*  109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
* * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                  
*  110-01-21 V1.00.02  ryan             add ooOppo.isDebit      
*  110-12-01 V1.00.03  ryan             update oempay iso                                                              *  
*  111-08-01 V1.00.04  ryan             修正NCCC撤掛未帶原因碼問題                                                              *  
*/

import busi.func.OutgoingBlock;
import busi.func.OutgoingOppo;



public class Ccaq2030Func extends busi.FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  OutgoingOppo ooOppo = null;
  OutgoingBlock ooBlock = null;

  private String isNegReasonOri = "";
  String cardNo = "";
  private String isVmjReasonOri = "";
  private String isFiscReasonOri = "";
  private String isAreaOri = "", isArea = "", isMArea[] = new String[6];
  private String isOutgoingArea = "";

  @Override
  public void dataCheck() {

  }

  @Override
  public int dbInsert() {
    return 0;
  }

  void checkExceptionArea() {
    /*
     * if (wp.item_empty("excep_flag")) return;
     */
    String lsDate = "";
    if (wp.itemEq("bin_type", "V")) {
      lsDate = wp.itemStr("vis_purg_date_1");
      isOutgoingArea =
          wp.itemStr("vis_area_1") + wp.itemStr("vis_area_2") + wp.itemStr("vis_area_2")
              + wp.itemStr("vis_area_4") + wp.itemStr("vis_area_5") + wp.itemStr("vis_area_6")
              + wp.itemStr("vis_area_7") + wp.itemStr("vis_area_8") + wp.itemStr("vis_area_9");
      if (empty(isOutgoingArea) == false && empty(lsDate)) {
        errmsg("VISA刪除日期: 不可空白");
        return;
      }

      if (!empty(lsDate) && commDate.sysComp(lsDate) > 0) {
        errmsg("VISA刪除日期不可小於今日");
        return;
      }

      return;
    }
    // -JCB-
    if (wp.itemEq("bin_type", "J")) {
      isOutgoingArea = wp.itemStr("jcb_area_1") + wp.itemStr("jcb_area_2")
          + wp.itemStr("jcb_area_3") + wp.itemStr("jcb_area_4") + wp.itemStr("jcb_area_5");
      lsDate = wp.itemStr("jcb_date1");
      if (empty(isOutgoingArea) == false && empty(lsDate)) {
        errmsg("JCB刪除日期不可空白");
        return;
      }
      if (empty(lsDate) == false && commDate.sysComp(lsDate) > 0) {
        errmsg("JCB刪除日期不可小於今日");
        return;
      }
      return;
    }
    // -MasterCard-
    if (wp.itemEq("bin_type", "M")) {
      for (int ii = 1; ii <= 6; ii++) {
        if (wp.itemEmpty("mast_area_" + ii))
          continue;
        lsDate = wp.itemStr("mast_date" + ii);
        if (empty(lsDate)) {
          errmsg("MASTER對應地區之刪除日期不可空白");
          return;
        }
        if (commDate.sysComp(lsDate) > 0) {
          errmsg("MASTER刪除日期不可小於今日");
          return;
        }
      }
      return;
    }

  }

  @Override
  public int dbUpdate() {

    cardNo = wp.colStr("card_no");
    checkExceptionArea();
    if (rc != 1)
      return rc;
    // -outgoing修改-
    if(commString.pos(",TWMP,OEMPAY", wp.itemStr("key_value"))>0){
    	oppoVcard("");
	} else if (!wp.itemEq("current_code", "0")) {
		if (wp.colEmpty("oppo_reason") == false) {
			outgoingUpdateOppo();
		} else {
			outgoingUpdateBlock();
		}
	}else {
		errmsg("卡片未停用不可停掛重送");
	    return rc;
	}
	return rc;
  }

  private void outgoingUpdateBlock() {
    String lsBlock = getBlockCode();
    if (empty(lsBlock)) {
      errmsg("卡戶/卡片: 未凍結OR特指  不可重送處理");
      return;
    }

    OutgoingBlock ooOutgo = new OutgoingBlock();
    ooOutgo.setConn(wp);

    ooOutgo.selectCcaSpecCode(lsBlock);

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.colStr("bin_type");
    ooOutgo.isDebit = wp.colEq("debit_flag", "Y");
    ooOutgo.oriNegReason = wp.colStr("neg_reason_code");
    ooOutgo.oriVmjReason = wp.colStr("vmj_reason_code");
    // -Neg_del_date-
    if (wp.colEq("card_spec_status", lsBlock)) {
      wp.colSet("aa.neg_del_date", wp.colStr("card_spec_del_date"));
    } else if (wp.colEq("spec_status", lsBlock)) {
      wp.colSet("aa.neg_del_date", wp.colStr("spec_del_date"));
    } else {
      String endDate = wp.colStr("new_end_date");
      wp.colSet("aa.neg_del_date", commDate.dateAdd(endDate, 0, 0, 1));
    }

    rc = ooOutgo.ccaQ2030Update(lsBlock);
    if (rc == -1) {
      errmsg(ooOutgo.getMsg());
    }

    return;
  }

  private void outgoingUpdateOppo() {
    ooOppo = new OutgoingOppo();
    ooOppo.setConn(wp);
    ooOppo.iscalltwmp = true;
    ooOppo.p9CurrentCode = wp.itemStr("current_code");
    ooOppo.p11ElectronicCardno = wp.itemStr("electronic_card_no");
    ooOppo.electronicCode = wp.itemStr("electronic_code");
    ooOppo.isDebit = wp.itemEq("debit_flag", "Y");
    cardNo = wp.colStr("card_no");
    String lsBinType = wp.colStr("bin_type");

    selectCcaOpposition(cardNo);
   
    if (rc != 1)
      return;
    // -outgoing.Delete-====================================================
    String lsNegDelDate = colStr("neg_del_date");

    if (notEmpty(isFiscReasonOri)) {
      ooOppo.fiscReason = isFiscReasonOri;
      ooOppo.oppoNegId(cardNo, "3", "", lsNegDelDate, lsBinType);
    }

    if (notEmpty(isVmjReasonOri)) {
      if (eq(lsBinType, "M")) {
        if (empty(isAreaOri)) {
          // f_ftp2master_req2(is_card_no, ls_date, ls_time,'3', '0',ls_vmj_reason)
          ooOppo.oppoMasterReq2(cardNo, "3", isVmjReasonOri, "0");
        } else {
          // f_ftp2master_req2(is_card_no, ls_date, ls_time,'3', '0',ls_vmj_reason)
          ooOppo.oppoMasterReq2(cardNo, "3", isVmjReasonOri, "0");
          // 2master_req(is_card_no, ls_date, ls_time, is_card_type, '3', ls_vmj_reason,
          // is_outgoing_area_ori)
          ooOppo.oppoMasterReq(cardNo, "3", isVmjReasonOri, "M", isMArea);
        }
      } else if (eq(lsBinType, "J")) {
        // 2jcb_req(is_card_no,is_neg_del_date,'0',ls_vmj_reason,is_outgoing_area_ori)
        ooOppo.oppoJcbReq(cardNo, "0", isVmjReasonOri, lsNegDelDate, isAreaOri);
      } else if (eq(lsBinType, "V")) {
        // 2visa_req(is_card_no,ls_date,ls_time,is_neg_del_date,'3',ls_vmj_reason,is_outgoing_area_ori)
        ooOppo.oppoVisaReq(cardNo, "3", isVmjReasonOri, lsNegDelDate, isAreaOri);
      }
    }

    // -outgoing.Insert-=========================================================
    String lsOppoReason = wp.colStr("oppo_reason");
    ooOppo.selectCcaOppTypeReason(lsOppoReason);
    lsNegDelDate = wp.colStr("neg_del_date");
    if (empty(lsNegDelDate))
      lsNegDelDate = commDate.dateAdd(wp.colStr("new_end_date"), 0, 0, 1);

    setExceptCode();
    String lsVmjReason = "";
    if (notEmpty(ooOppo.strNegReason)) {
      // 2neg_id(is_card_no,is_card_type, is_neg_reason,is_neg_cap,is_neg_del_date,"1")
      ooOppo.fiscReason = ooOppo.strFiscReason;
      ooOppo.oppoNegId(cardNo, "1", ooOppo.strNegReason, lsNegDelDate, lsBinType);
    }
    if (eq(lsBinType, "M")) {
      ooOppo.masterDate = colStr("neg_del_date");
      if (empty(ooOppo.masterDate))
      ooOppo.masterDate = colStr("new_end_date");
      lsVmjReason = ooOppo.strMastReason;
      if (notEmpty(lsVmjReason)) {
        if (empty(isArea)) {
          // 2master_req2(is_card_no, as_date, as_time,as_file,'0',is_vmj_reason)
          ooOppo.oppoMasterReq2(cardNo, "1", lsVmjReason, "0");
        } else {
          // 2master_req2(is_card_no, as_date, as_time,as_file,'0',is_vmj_reason)
          ooOppo.oppoMasterReq2(cardNo, "1", lsVmjReason, "0");
          // 2master_req(is_card_no, as_date, as_time,
          // is_card_type,as_file,is_vmj_reason,is_outgoing_area)
          ooOppo.oppoMasterReq(cardNo, "1", lsVmjReason, "M", isMArea);
        }
      }
    } else if (eq(lsBinType, "J")) {
      lsVmjReason = ooOppo.strJcbReason;
      if (notEmpty(lsVmjReason)) {
    	 if (wp.colEmpty("jcb_date1") == false) {
            lsNegDelDate = commDate.dateAdd(wp.colStr("jcb_date1"),0,0,1);
         }
        // 2jcb_req(is_card_no,ls_vis_purg_date1,as_file,is_vmj_reason,is_outgoing_area)
        ooOppo.oppoJcbReq(cardNo, "1", lsVmjReason, lsNegDelDate, isArea);
      }
    } else if (eq(lsBinType, "V")) {
      lsVmjReason = ooOppo.strVisaReason;
      if (notEmpty(lsVmjReason)) {
        if (wp.colEmpty("vis_purg_date_1") == false) {
            lsNegDelDate = commDate.dateAdd(wp.colStr("vis_purg_date_1"),0,0,1);
        }
        // 2visa_req(is_card_no,ls_vis_purg_date1,as_file,is_vmj_reason,is_outgoing_area)
        ooOppo.oppoVisaReq(cardNo, "1", lsVmjReason, lsNegDelDate, isArea);
      }
    }
    return;
  }

  void setExceptCode() {
    if (wp.colEq("bin_type", "J")) {
      isArea = wp.itemNvl("jcb_area_1", "0") + wp.itemNvl("jcb_area_2", "0")
          + wp.itemNvl("jcb_area_3", "0") + wp.itemNvl("jcb_area_4", "0")
          + wp.itemNvl("jcb_area_5", "0");
    } else if (wp.colEq("bin_type", "V")) {
      isArea = wp.itemNvl("vis_area_1", " ") + wp.itemNvl("vis_area_2", " ")
          + wp.itemNvl("vis_area_3", " ") + wp.itemNvl("vis_area_4", " ")
          + wp.itemNvl("vis_area_5", " ") + wp.itemNvl("vis_area_6", " ")
          + wp.itemNvl("vis_area_7", " ") + wp.itemNvl("vis_area_8", " ")
          + wp.itemNvl("vis_area_9", " ");
    } else if (wp.colEq("bin_type", "M")) {
      isArea = wp.itemNvl("mast_area_1", " ") + this.strMid(wp.itemStr("mast_date1"), 2, 6)
          + wp.itemNvl("mast_area_2", " ") + this.strMid(wp.itemStr("mast_date2"), 2, 6)
          + wp.itemNvl("mast_area_3", " ") + this.strMid(wp.itemStr("mast_date3"), 2, 6)
          + wp.itemNvl("mast_area_4", " ") + this.strMid(wp.itemStr("mast_date4"), 2, 6)
          + wp.itemNvl("mast_area_5", " ") + this.strMid(wp.itemStr("mast_date5"), 2, 6)
          + wp.itemNvl("mast_area_6", " ") + this.strMid(wp.itemStr("mast_date6"), 2, 6);
      for (int ii = 0; ii < 6; ii++) {
        isMArea[ii] =
            wp.itemNvl("mast_area_" + ii, " ") + this.strMid(commDate.dateAdd(wp.itemStr("mast_date" + ii),0,0,1), 2, 6);
      }
    }
  }

  void outgoingNccc() {
    // wf_set_lbl_n(); //Set NCCC-ISO response to BOX
    // ls_neg_rsp_code = gs_resp_code
  }

  @Override
  public int dbDelete() {
    cardNo = wp.colStr("card_no");
    selectCcaOpposition(cardNo);
    String oppoStatus = colStr("oppo_status");
    // -outgoing刪除-
    if(commString.pos(",TWMP,OEMPAY", wp.itemStr("key_value"))>0){
    	oppoVcard("3");
    }else if (wp.itemEq("current_code", "0")) {
		if (!empty(oppoStatus)) {
			outgoingDeleteOppo();
		} else {
			outgoingDeleteBlock();
		}
	}else {
		 errmsg("卡片停用中不可撤掛重送");
	     return rc;
	}
    return rc;
  }

  void selectCcaOpposition(String aCardNo) {
    strSql = "select mst_reason_code, vis_reason_code," + " neg_del_date," + " fisc_reason_code, oppo_status,"
        + " vis_area_1, vis_area_2, vis_area_3," + " vis_area_4, vis_area_5, vis_area_6,"
        + " vis_area_7, vis_area_8, vis_area_9,"
        + " vis_purg_date_1, vis_purg_date_2, vis_purg_date_3,"
        + " vis_purg_date_4, vis_purg_date_5, vis_purg_date_6,"
        + " vis_purg_date_7, vis_purg_date_8, vis_purg_date_9 " + " from cca_opposition"
        + " where card_no =?";
    setString2(1, aCardNo);

    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("卡片停用資料, 不存在");
      return;
    }
    String neg_del_date = colStr("neg_del_date");
    isNegReasonOri = colStr("mst_reason_code");
    isVmjReasonOri = colStr("vis_reason_code");
    isFiscReasonOri = colStr("fisc_reason_code");
    String lsBinType = wp.colStr("bin_type");
    if (eqIgno(lsBinType, "J")) {
      isAreaOri = colNvl("vis_area_1", "0") + colNvl("vis_area_2", "0")
          + colNvl("vis_area_3", "0") + colNvl("vis_area_4", "0") + colNvl("vis_area_5", "0");
    } else if (eqIgno(lsBinType, "V")) {
      isAreaOri =
          colNvl("vis_area_1", " ") + colNvl("vis_area_2", " ") + colNvl("vis_area_3", " ")
              + colNvl("vis_area_4", " ") + colNvl("vis_area_5", " ") + colNvl("vis_area_6", " ")
              + colNvl("vis_area_7", " ") + colNvl("vis_area_8", " ") + colNvl("vis_area_9", " ");
    } else if (eqIgno(lsBinType, "M")) {
      isAreaOri = "";
      isAreaOri = colNvl("vis_area_1", " ") + this.strMid(colStr("vis_purg_date_1"), 2, 6)
          + colNvl("vis_area_2", " ") + this.strMid(colStr("vis_purg_date_2"), 2, 6)
          + colNvl("vis_area_3", " ") + this.strMid(colStr("vis_purg_date_3"), 2, 6)
          + colNvl("vis_area_4", " ") + this.strMid(colStr("vis_purg_date_4"), 2, 6)
          + colNvl("vis_area_5", " ") + this.strMid(colStr("vis_purg_date_5"), 2, 6)
          + colNvl("vis_area_6", " ") + this.strMid(colStr("vis_purg_date_6"), 2, 6);
      for (int ii = 0; ii < 6; ii++) {
        isMArea[ii] =
            colNvl("vis_area_" + ii, " ") + this.strMid(colStr("vis_purg_date_" + ii), 2, 6);
      }
    }
  }

  void outgoingDeleteOppo() {
    OutgoingOppo ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    ooOutgo.iscalltwmp = true;
//    selectCcaOpposition(cardNo);

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.colStr("bin_type"); // is_bin_type;
    ooOutgo.p9CurrentCode = wp.itemStr("current_code");
    ooOutgo.p11ElectronicCardno = wp.itemStr("electronic_card_no");
    ooOutgo.electronicCode = wp.itemStr("electronic_code");
    ooOutgo.p4Reason = isNegReasonOri;
    ooOutgo.p5DelDate = colStr("neg_del_date");
    ooOutgo.fiscReason = isFiscReasonOri;
    if (!empty(isNegReasonOri)) {
      rc = ooOutgo.oppoNegId("3");
      if (rc != 1) {
        errmsg("Neg_id:" + ooOutgo.getMsg());
        return;
      }
    }
    
    if (!empty(isVmjReasonOri)) {
      ooOutgo.p4Reason = isVmjReasonOri;
      ooOutgo.p5DelDate = colStr("neg_del_date");
      ooOutgo.p7Region = isAreaOri;
      if (eq(ooOutgo.p2BinType, "M")) {
        ooOutgo.p6VipAmt = "0";
        // "開始傳送 ["+is_card_no+"] 至 Master...MCC102"
        rc = ooOutgo.oppoMasterReq2("3");
        if (rc != 1) {
          errmsg("Master_req2:" + ooOutgo.getMsg());
          return;
        }
        if (!empty(isAreaOri)) {
          // "開始傳送 ["+is_card_no+"] 至 Master...MCC103"
          rc = ooOutgo.oppoMasterReq("3", isMArea);
          if (rc != 1) {
            errmsg("Master_req:" + ooOutgo.getMsg());
            return;
          }
        }
      } else if (eq(ooOutgo.p2BinType, "J")) {
        // "開始傳送 ["+is_card_no+"] 至 JCB......"
        rc = ooOutgo.oppoJcbReq("0");
        if (rc != 1) {
          errmsg("Jcb_req:" + ooOutgo.getMsg());
          return;
        }
      } else if (eq(ooOutgo.p2BinType, "V")) {
        // "開始傳送 ["+is_card_no+"] 至 VISA......"
        rc = ooOutgo.oppoVisaReq("3");
        if (rc != 1) {
          errmsg("Visa_req:" + ooOutgo.getMsg());
          return;
        }
      }
      // wp.col_set("vmj_resp_code",oo_outgo.vmj_resp_code);
    }
    // --IBM-----------
    // if (ib_debit && !empty(is_neg_reason_ori)) {
    // oo_outgo.parm_Clear();
    // oo_outgo.p1_card_no =ls_card_no;
    // oo_outgo.pp_bank_acctno = col_ss("oppo.bank_acct_no");
    // oo_outgo.p4_reason =is_neg_reason_ori;
    // oo_outgo.p5_del_date =col_ss("oppo.neg_del_date");
    // oo_outgo.p2_bin_type =is_bin_type;
    // //delete
    // rc =oo_outgo.oppo_Ibm_negfile("3");
    // if (rc!=1) {
    // errmsg("IBM:"+oo_outgo.getMsg());
    // return;
    // }
    // wp.col_set("neg_resp_code",oo_outgo.neg_resp_code);
    // }

  }

  String getBlockCode() {
    String lsBlock = "";
    String specDelDate = wp.colStr("card_spec_del_date");
    if (commString.strComp(specDelDate, this.getSysDate()) >= 0) {
      lsBlock = wp.colStr("card_spec_status");
    }
    if (wp.colEmpty("spec_status") == false) {
      specDelDate = wp.colStr("spec_del_date");
      if (notEmpty(specDelDate) && commString.strComp(specDelDate, getSysDate()) >= 0) {
        lsBlock = wp.colStr("spec_status");
      }
    }

    specDelDate = wp.colStr("block_reason5");
    if (notEmpty(specDelDate))
      lsBlock = specDelDate;
    specDelDate = wp.colStr("block_reason4");
    if (notEmpty(specDelDate))
      lsBlock = specDelDate;
    specDelDate = wp.colStr("block_reason3");
    if (notEmpty(specDelDate))
      lsBlock = specDelDate;
    specDelDate = wp.colStr("block_reason2");
    if (notEmpty(specDelDate))
      lsBlock = specDelDate;
    specDelDate = wp.colStr("block_reason1");
    if (notEmpty(specDelDate))
      lsBlock = specDelDate;

    return lsBlock;
  }

  void outgoingDeleteBlock() {
    String lsBlock = getBlockCode();

    OutgoingBlock ooOutgo = new OutgoingBlock();
    ooOutgo.setConn(wp);

    ooOutgo.selectCcaSpecCode(lsBlock);

    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.colStr("bin_type");
    ooOutgo.isDebit = wp.colEq("debit_flag", "Y");

    rc = ooOutgo.ccaQ2030Delete(lsBlock);
    if (rc == -1) {
      errmsg(ooOutgo.getMsg());
    }

    return;
  }

  int oppoVcard(String actCode){
	 
	  String sqlSelect = "select card_no,v_card_no,electronic_card_no,act_code,reason_code,current_code,new_end_date,bin_type from cca_outgoing where hex(rowid) = ? ";
	  setString2(1,wp.itemStr("outgoingRowid"));
	  sqlSelect(sqlSelect);
	  if(sqlRowNum<=0){
		  errmsg("select cca_outgoing err");
		  return rc; 
	  }
	  
	  OutgoingOppo ooOutgo = new OutgoingOppo();
	  ooOutgo.setConn(wp);
	  ooOutgo.iscalltwmp = true;
	  ooOutgo.isReOutgoing = true;
	  ooOutgo.vCardRowid = wp.itemStr("outgoingRowid");
	  ooOutgo.p1CardNo = colStr("card_no");
	  ooOutgo.p2BinType = colStr("bin_type");
	  ooOutgo.vCardNos = colStr("v_card_no");
	  if(empty(actCode))
		  actCode = colStr("act_code");
	  ooOutgo.p4Reason = colStr("reason_code");
	  ooOutgo.p8NewEndDate = colStr("new_end_date");
	  ooOutgo.p9CurrentCode = colStr("current_code");
	  if(wp.itemEq("key_value", "TWMP"))
		  ooOutgo.oppoTwmpReq(actCode);
	  if(wp.itemEq("key_value", "OEMPAY"))
		  ooOutgo.oppoOempayReq(actCode);
	  
	  return rc; 
  }
  
  @Override
  public int dataProc() {
    return 0;
  }
}
