package ccam02;
/**
 * 2023-1222   JH    card_no order by current_code,card_no
 * */
import ofcapp.BaseAction;

public class Ccap3080 extends BaseAction {

	@Override
	public void userAction() throws Exception {
		strAction = wp.buttonCode;
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
		if(chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
			alertErr2("鍵檔日期: 輸入錯誤");
			return ;
		}
		
		if(wp.itemEmpty("ex_idno") == false && wp.itemEmpty("ex_card_no") == false) {
			alertErr2("身分證ID、商務卡卡號不可同時輸入");
			return ;
		}

		String lsWhere = " where 1=1 "
				+ sqlCol(wp.itemStr("ex_crt_date1"),"crt_date",">=")
				+ sqlCol(wp.itemStr("ex_crt_date2"),"crt_date","<=")
				+ sqlCol(wp.itemStr("ex_mod_user"),"mod_user","like%")
				;
		
		if(wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and idno_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"),"id_no")
					+" ) "
					;
		}
		
		if(wp.itemEmpty("ex_card_no") == false) {
			lsWhere += " and acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 "
					+ sqlCol(wp.itemStr("ex_card_no"),"card_no")
					+" ) "
					;
		}
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();		
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " acct_type , corp_p_seqno , idno_p_seqno , with_sup_card , start_date , end_date , "
				+ " crt_date , crt_user , mod_user , to_char(mod_time,'hh24miss') as mod_time2 , mod_pgm , acno_p_seqno , mod_audcode , "
				+ " decode(mod_audcode,'A','新增','U','修改','D','刪除') as tt_audcode , hex(rowid) as rowid , '' as error_desc "
				;
		
		wp.daoTable = " cca_vip_t ";
		wp.whereOrder = " order by acct_type Asc ";
		pageQuery();
		
		if(sqlNotFind()) {
			alertErr2("此條件查無資料");
			return ;
		}
		
		wp.setPageValue();
		wp.setListCount(0);
		queryAfter();
	}

	void queryAfter() throws Exception {
		
		String sql1 = "select card_no from crd_card where acno_p_seqno = ? "
          +" order by current_code, card_no"
          +commSqlStr.rownum(1);
		String sql2 = "select id_no from crd_idno where id_p_seqno = ? ";
		
		for(int ii=0;ii<wp.selectCnt;ii++) {
			if(wp.colEq(ii,"acct_type", "01")) {
				sqlSelect(sql2,new Object[] {wp.colStr(ii,"idno_p_seqno")});
				if(sqlRowNum > 0)
					wp.colSet(ii,"wk_id_card", sqlStr("id_no"));
			}	else	{
				sqlSelect(sql1,new Object[] {wp.colStr(ii,"acno_p_seqno")});
				if(sqlRowNum > 0)
					wp.colSet(ii,"wk_id_card", sqlStr("card_no"));
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
		ccam02.Ccap3080Func func = new ccam02.Ccap3080Func();
		func.setConn(wp);
		
		int llOk = 0 , llErr = 0;
		
		String[] opt = wp.itemBuff("opt");
		String[] acctType = wp.itemBuff("acct_type");
		String[] corpPSeqno = wp.itemBuff("corp_p_seqno");
		String[] idnoPSeqno = wp.itemBuff("idno_p_seqno");		
		String[] modAudCode = wp.itemBuff("mod_audcode");
		String[] startDate = wp.itemBuff("start_date");
		String[] endDate = wp.itemBuff("end_date");
		String[] withSupCard = wp.itemBuff("with_sup_card");
		String[] acnoPSeqno = wp.itemBuff("acno_p_seqno");
		wp.listCount[0] = wp.itemRows("rowid");
	    optNumKeep(wp.listCount[0]);
	    if (optToIndex(opt[0]) < 0) {
	      alertErr2("請選取欲處理之資料");
	      return;
	    }
		
	    int rr = -1;
	    for(int ii=0;ii<opt.length;ii++) {
	    	rr = this.optToIndex(opt[ii]);
	        if (rr < 0) {
	          continue;
	        }
	        
	        func.varsSet("acct_type", acctType[rr]);
	        func.varsSet("corp_p_seqno", corpPSeqno[rr]);
	        func.varsSet("idno_p_seqno", idnoPSeqno[rr]);
	        func.varsSet("mod_audcode", modAudCode[rr]);
	        func.varsSet("start_date", startDate[rr]);
	        func.varsSet("end_date", endDate[rr]);
	        func.varsSet("with_sup_card", withSupCard[rr]);
	        func.varsSet("acno_p_seqno",acnoPSeqno[rr]);
	        
	        optOkflag(rr);
	        rc = func.dataProc();
	        sqlCommit(rc);
	        optOkflag(rr, rc);
	        if(rc == 1) {
	        	llOk ++;
	        }	else	{
	        	llErr ++ ; 
	        	wp.colSet(rr,"error_desc", func.errorDesc);
	        }
	        
	    }
	    
	    alertMsg("覆核完成; OK=" + llOk + ", ERR=" + llErr);

	}

	@Override
	public void initButton() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
