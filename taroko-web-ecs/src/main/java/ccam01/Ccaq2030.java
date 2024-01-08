package ccam01;
/** 
 * 109-12-30  V1.00.01  shiyuqi       修改无意义命名   
 * 110-10-26  V1.00.02  Justin        中止->終止          
 * 111-02-22  V1.00.03  Ryan       queryFunc() add group  key_value      
 * 111-03-31  V1.00.04  Ryan       where 條件修改         
 * 111-04-29  V1.00.05  Ryan       查詢條件修改           
 * 111-06-08  V1.00.06  Ryan       修改 queryAfter  
 * 111-06-16  V1.00.07  JustinWu    3.中止(實體卡停卡) => 3.中止(已停用)
 * 111-08-01  V1.00.08  Ryan       queryFunc() add group act_code         
 * 111-11-22  V1.00.09  Ryan       取消只查最新一筆資料的條件，新增可指定回覆碼查詢
 * 111-12-07  V1.00.10  Ryan       修正內外畫面票證卡號顯示不一致問題
 * */
import busi.func.OutgoingOppo;

/**
 * 19-1210:    Alex  add initButton
 * 19-0611:    JH    p_xxx >>acno_p_xxx
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-12-30 V2.00.00  Ryan       新增虛擬卡號 欄位顯示                                   *
* */
import ofcapp.BaseAction;

public class Ccaq2030 extends BaseAction {
  String cardNo = "",vCardNo="", isDebitFlag = "";
  String lsVisReason = "";
  String lsMstReason = "";
  String lsFiscReason = "";
  String wfErrorCode = "";
  double ilCardAcctIdx = 0;
  boolean ibDebit = false;
  String outgoingRowid = "",keyValue="";

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
    } else if (eqIgno(wp.buttonCode, "R2")) {
        // -資料讀取-
        strAction = "R";
        cardNo = wp.colStr("card_no");
        if (isEmpty(cardNo)) {
          alertErr("卡號: 不可空白");
          return;
        }
        outgoingQuery();
      }else if (eqIgno(wp.buttonCode, "U1")) {
          /* 停掛重送 */
          saveFunc();
      }else if (eqIgno(wp.buttonCode, "D1")) {
          /* 撤掛重送 */
          saveFunc();
      }
  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.colEq("bin_type", "V")) {
        wp.optionKey = wp.colStr("vis_area_1");
        dddwList("dddw_visa_area1", "cca_sys_parm2", "sys_key", "sys_data1",
            " where sys_id='VISA' and sys_key in('0','A','B','C','D','E','F')");
        wp.optionKey = wp.colStr("vis_area_2");
        dddwShare("dddw_visa_area2");
        wp.optionKey = wp.colStr("vis_area_3");
        dddwShare("dddw_visa_area3");
        wp.optionKey = wp.colStr("vis_area_4");
        dddwShare("dddw_visa_area4");
        wp.optionKey = wp.colStr("vis_area_5");
        dddwShare("dddw_visa_area5");
        wp.optionKey = wp.colStr("vis_area_6");
        dddwShare("dddw_visa_area6");
        wp.optionKey = wp.colStr("vis_area_7");
        dddwShare("dddw_visa_area7");
        wp.optionKey = wp.colStr("vis_area_8");
        dddwShare("dddw_visa_area8");
        wp.optionKey = wp.colStr("vis_area_9");
        dddwShare("dddw_visa_area9");
      } else if (wp.colEq("bin_type", "M")) {
        wp.optionKey = wp.colStr("mast_area_1");
        dddwList("dddw_mast_area1", "cca_sys_parm2", "sys_key", "sys_data1",
            " where sys_id='MAST'");
        wp.optionKey = wp.colStr("mast_area_2");
        dddwShare("dddw_mast_area2");
        wp.optionKey = wp.colStr("mast_area_3");
        dddwShare("dddw_mast_area3");
        wp.optionKey = wp.colStr("mast_area_4");
        dddwShare("dddw_mast_area4");
        wp.optionKey = wp.colStr("mast_area_5");
        dddwShare("dddw_mast_area5");
        wp.optionKey = wp.colStr("mast_area_6");
        dddwShare("dddw_mast_area6");
      }
    } catch (Exception ex) {
    }

  }
  // 111-11-22  V1.00.09  Ryan       取消只查最新一筆資料的條件，新增可指定回覆碼查詢的選項
  @Override
  public void queryFunc() throws Exception {
	  
	selectPtrSysParm();

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no");
        
      if (wp.itemEq("ex_call_status", "1")) {
    	  
  		if(empty(wfErrorCode)) {
			 lsWhere += " and decode(resp_code,'','99',resp_code) = '99' ";
		}else {
			String[] whereErrCode =  wfErrorCode.split(",");
			lsWhere += " and resp_code in ( ";
			for(int n = 0 ; n<whereErrCode.length;n++) {
				lsWhere += "'" + whereErrCode[n] + "'";
				if(n == whereErrCode.length-1) {
					continue;
				}
				lsWhere += ",";
			}
			lsWhere += " ) ";
		}
    	  

      } else if (wp.itemEq("ex_call_status", "2")) {
        lsWhere += " and resp_code ='00' " ;
      } else if(wp.itemEq("ex_call_status", "3")) {
    	  if(wp.itemEmpty("ex_resp_code")) {
    		  alertErr("指定回覆碼不能為空值");
    	      return;
    	  }
    	  lsWhere += sqlCol(wp.itemStr("ex_resp_code"), "resp_code");
      }

    if (!wp.itemEmpty("ex_idno")) {
      lsWhere += " and card_no in "
          + " (select A.card_no from crd_card A join crd_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 "
          + sqlCol(wp.itemStr2("ex_idno"), "B.id_no") + " union all "
          + " select A.card_no from dbc_card A join dbc_idno B on A.id_p_seqno = B.id_p_seqno where 1=1 "
          + sqlCol(wp.itemStr2("ex_idno"), "B.id_no") + " )";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " hex(rowid) as rowid,decode(key_value,'TWMP','','OEMPAY','','TSCC','','IPASS','','ICASH','',reason_code) as tt_reason_code,* ";
    wp.daoTable = " cca_outgoing ";
    wp.whereOrder = " order by crt_date Desc , crt_time Desc ";
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
    queryAfter();
  }

  public void queryAfter() throws Exception {

    String sql1 = " select acct_type , uf_acno_key2(card_no,'') as acct_key"
        + " from cca_card_base where card_no = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.logSql = false;
      if (wp.colEq(ii, "data_from", "1")) {
        wp.colSet(ii, "tt_data_from", "人工");
      } else if (wp.colEq(ii, "data_from", "2")) {
        wp.colSet(ii, "tt_data_from", "批次");
      }

      if (wp.colEq(ii, "key_table", "CARD_BASE_SPEC")) {
        wp.colSet(ii, "tt_key_table", "凍結/特指");
      } else if (wp.colEq(ii, "key_table", "OPPOSITION")) {
        wp.colSet(ii, "tt_key_table", "停掛");
      }

      if (wp.colEmpty(ii, "card_no") == false) {
        sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no")});
        if (sqlRowNum > 0) {
          wp.colSet(ii, "acct_type", sqlStr("acct_type"));
          wp.colSet(ii, "acct_key", sqlStr("acct_key"));
        }
      }
    }
  }

  void checkDebitCard(String cardNo) {
    ibDebit = false;

    busi.func.CrdFunc func = new busi.func.CrdFunc();
    func.setConn(wp);
    ibDebit = func.isDebitcard(cardNo);
  }

  //111-12-07  V1.00.10  Ryan       修正內外畫面票證卡號顯示不一致問題
  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    outgoingRowid = wp.itemStr("data_k2");
    keyValue = wp.itemStr("data_k3");
    vCardNo = wp.itemStr("data_k4");
    String electronicCardNo = wp.itemStr("data_k5");
    dataRead();
    wp.colSet("key_value", keyValue);
    wp.colSet("outgoingRowid", outgoingRowid);
    wp.colSet("key_value", keyValue);
	wp.colSet("electronic_card_no", electronicCardNo); 
  }

  @Override
  public void dataRead() throws Exception {
	boolean isCardBase = true;
    if (empty(cardNo))
      cardNo = itemkk("card_no");

    wp.selectSQL =
        " card_no , debit_flag , card_acct_idx , spec_status as card_spec_status , spec_mst_vip_amt as card_spec_mst_vip_amt , "
            + " spec_del_date as card_spec_del_date , acct_type , uf_acno_key2(acno_p_seqno,debit_flag) as acct_key , "
            + " spec_date as card_spec_date , spec_user as card_spec_user , bin_type ";
    wp.daoTable = " cca_card_base ";
    wp.whereStr = " where 1=1 " + sqlCol(cardNo, "card_no");

    pageSelect();
    
    if (wp.selectCnt<=0){
    	isCardBase = false;
		wp.selectSQL = " card_no , debit_flag , card_acct_idx,bin_type ";
		wp.daoTable = " cca_opposition ";
		wp.whereStr = " where 1=1 " + sqlCol(cardNo, "card_no");
		pageSelect();
    }

    if (sqlNotFind()) {
        alertErr2("查無資料");
        return;
    }
    
    ilCardAcctIdx = wp.colNum("card_acct_idx");
    isDebitFlag = wp.colStr("debit_flag");
    
    wp.selectSQL =
        " block_reason1 , block_reason2 , block_reason3 , block_reason4 , block_reason5 , "
            + " spec_status , spec_del_date , spec_date , spec_user ";
    wp.daoTable = " cca_card_acct ";
//    wp.whereStr = " where 1=1 and card_acct_idx = " + ilCardAcctIdx;
    wp.whereStr = " where 1=1 "+col(ilCardAcctIdx,"card_acct_idx",true);
    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料");
      return;
    }

    checkDebitCard(cardNo);
    
    if(isCardBase==true){
        if (ibDebit) {
            wp.selectSQL = " oppost_date , current_code , new_end_date , electronic_code ";
            wp.daoTable = " dbc_card ";
            wp.whereStr = " where 1=1 " + sqlCol(cardNo, "card_no");
            pageSelect();
          } else {
            wp.selectSQL = " bin_type ,oppost_date , current_code , new_end_date , electronic_code ";
            wp.daoTable = " crd_card ";
            wp.whereStr = " where 1=1 " + sqlCol(cardNo, "card_no");
            pageSelect();
          }
    }
    getVcardData();
   
    dataReadAfter(isCardBase);
    Ccam2010Func func = new Ccam2010Func();
    func.setConn(wp);

    if (ibDebit == false) {
      func.cntSupCard();
      func.cntTpanCard();
    }
   
  }

  void dataReadAfter(boolean isCardBase) {
    if (wp.colEmpty("oppost_date")&&isCardBase)
      return;
    String sql1 = " select " + " oppo_type , " + " oppo_status , " + " neg_del_date , "
        + " opp_remark , " + " oppo_date ," + " mod_seqno , " + " hex(rowid) as rowid, "
    	+ " mst_reason_code, vis_reason_code , fisc_reason_code "
        + " from cca_opposition " 
        + " where card_no = ? ";

    sqlSelect(sql1, new Object[] {cardNo});
    if (sqlRowNum > 0) {
      wp.colSet("oppo_date", sqlStr("oppo_date"));
      wp.colSet("oppo_reason", sqlStr("oppo_status"));
      wp.colSet("neg_del_date", sqlStr("neg_del_date"));
      wp.colSet("opp_remark", sqlStr("opp_remark"));
      wp.colSet("oppo_type", sqlStr("oppo_type"));
      wp.colSet("mod_seqno", sqlStr("mod_seqno"));
      wp.colSet("mst_reason_code", sqlStr("mst_reason_code"));
      wp.colSet("vis_reason_code", sqlStr("vis_reason_code"));
     // wp.colSet("fisc_reason_code", sqlStr("fisc_reason_code"));
      wp.colSet("rowid", sqlStr("rowid"));

      if (eqIgno(sqlStr("oppo_type"), "1")) {
        wp.colSet("tt_oppo_type", "1.一般停用");
      } else if (eqIgno(sqlStr("oppo_type"), "2")) {
        wp.colSet("tt_oppo_type", "2.掛失停用");
      } else if (eqIgno(sqlStr("oppo_type"), "3")) {
        wp.colSet("tt_oppo_type", "3.強制停用");
      } else if (eqIgno(sqlStr("oppo_type"), "4")) {
        wp.colSet("tt_oppo_type", "4.其他停用");
      } else if (eqIgno(sqlStr("oppo_type"), "5")) {
        wp.colSet("tt_oppo_type", "5.偽卡停用");
      }

    }

    Ccam2010Func func = new Ccam2010Func();
    func.setConn(wp);

    func.selectOpposition();

  }

  void outgoingQuery() {
	this.sqlCommit(1);
	String lsBinType = wp.itemStr("bin_type");
	String lsOppoDate = wp.itemStr("oppo_date");  
	if(commString.pos(",TWMP,OEMPAY", wp.itemStr("key_value"))>0){
        Ccaq2030Func func = new Ccaq2030Func();
        func.setConn(wp);
        func.oppoVcard("5");
    	return;
    }
	
    OutgoingOppo ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    ooOutgo.wpCallStatus("");
    if (empty(cardNo))
      return;
    
    setOutgoingValue(lsBinType);
    
    // -NEG-
    ooOutgo.parmClear();
    ooOutgo.iscalltwmp = true;
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = lsBinType;
    ooOutgo.p4Reason = lsMstReason;
    ooOutgo.p8NewEndDate = wp.itemStr("new_end_date");
    ooOutgo.p9CurrentCode = wp.itemStr("oppo_type");
    ooOutgo.p10OppostReason = wp.itemStr("oppo_reason");
    ooOutgo.p11ElectronicCardno = wp.itemStr("electronic_card_no");
    ooOutgo.electronicCode = wp.itemStr("electronic_code");
    ooOutgo.fiscReason = lsFiscReason;
    ooOutgo.oppoNegId("5");
    
    // -VMJ-
    String lsArea = "";
    ooOutgo.p4Reason = lsVisReason;
    if (eqIgno(lsBinType, "M")) {
      ooOutgo.oppoMasterReq2("5");
    } else if (eqIgno(lsBinType, "J")) {
      lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
          + wp.colStr("vis_area_4") + wp.colStr("vis_area_5");
      ooOutgo.p5DelDate = lsOppoDate;
      ooOutgo.p7Region = lsArea;
      ooOutgo.oppoJcbReq("5");
    } else if (eqIgno(lsBinType, "V")) {
      lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
          + wp.colStr("vis_area_4") + wp.colStr("vis_area_5") + wp.colStr("vis_area_6")
          + wp.colStr("vis_area_7") + wp.colStr("vis_area_8") + wp.colStr("vis_area_9");
      ooOutgo.p5DelDate = lsOppoDate;
      if (empty(lsArea)) {
        ooOutgo.p4Reason = "41";
        ooOutgo.p7Region = "0" + commString.space(8);
      } else {
        ooOutgo.p7Region = lsArea;
      }
      ooOutgo.oppoVisaReq("5");
    }
  }

  void setOutgoingValue(String lsBinType) {
		// -get outgo-reason-
		String strSql = "select ncc_opp_type, neg_opp_reason as neg_reason," + " vis_excep_code as visa_reason,"
				+ " mst_auth_code as mast_reason," + " jcb_excp_code as jcb_reason, " + " fisc_opp_code as fisc_reason "
				+ " from cca_opp_type_reason" + " where 1=1"
//	        + commSqlStr.col(wp.itemStr("oppo_reason"), "opp_status");
				+ " and opp_status = ? ";
		sqlSelect(strSql, new Object[] { wp.itemStr("oppo_reason") });

		lsMstReason = sqlStr("neg_reason");
		lsFiscReason = sqlStr("fisc_reason");

		if (eqIgno(lsBinType, "V"))
			lsVisReason = sqlStr("visa_reason");
		else if (this.eqIgno(lsBinType, "M"))
			lsVisReason = sqlStr("mast_reason");
		else if (this.eqIgno(lsBinType, "J"))
			lsVisReason = sqlStr("jcb_reason");

  }
  
  
  void getVcardData(){
	  	if(empty(vCardNo))
	  		return;
	    String sql1 = " select v_card_no ";
        sql1 += " ,decode(status_code,'0','0.正常','1','1.暫停','2','2.終止(人工)','3','3.終止(已停用)','4','4.重複取消','5','5.終止(已過效期)',status_code) as tt_status_code ";
        sql1 += " ,change_date ";
        sql1 += " ,new_end_date ";
	    if(keyValue.equals("OEMPAY")) {
	    	sql1 += " ,decode(wallet_identifier,'101','WALLET REMOTE','102','NFC','103','Apple Pay','216','Google Pay','217','Samsung Pay',wallet_identifier) as tt_wallet_identifier ";
	    	sql1 += " from oempay_card ";
	    }
	    if(keyValue.equals("TWMP")) {
	    	sql1 += " ,'HCE' as tt_wallet_identifier ";
	    	sql1 += " from hce_card ";
	    }
	    sql1 += " where v_card_no = ? ";
        sqlSelect(sql1, new Object[] {vCardNo});
        if (sqlRowNum > 0) {
        	wp.colSet("v_card_no", sqlStr("v_card_no"));
        	wp.colSet("tt_status_code", sqlStr("tt_status_code"));
        	wp.colSet("tt_wallet_identifier", sqlStr("tt_wallet_identifier"));
        	wp.colSet("change_date", sqlStr("change_date"));
          	wp.colSet("new_end_date2", sqlStr("new_end_date"));
        }
  }
  
	
	void selectPtrSysParm() throws Exception {
		String strSql = "SELECT WF_VALUE,WF_VALUE2 FROM PTR_SYS_PARM WHERE WF_PARM = 'SYSPARM' AND WF_KEY = 'CCAB002'";
		sqlSelect(strSql);
		wfErrorCode = sqlStr("WF_VALUE2");
	}
  
  
  @Override
  public void saveFunc() throws Exception {
    Ccaq2030Func func = new Ccaq2030Func();
    func.setConn(wp);

//    rc = func.dbSave(strAction);
    if (eqIgno(strAction,"U1")) {
    	rc = func.dbUpdate();
    	if (rc==1) {
    		alertMsg("停掛重送完成");
    	}
    } 
    if (eqIgno(strAction,"D1")) {
    	rc = func.dbDelete();
    	if (rc==1) {
    		alertMsg("撤掛重送完成");
    	}
    }
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    this.btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
