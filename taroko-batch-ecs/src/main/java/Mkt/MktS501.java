/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  2018-1108  V1.00.00    JH          modify                                  *
 *  107/09/25  V1.00.01    Alex        initial                                 *
 *  109-12-10  V1.00.02    tanwei      updated for project coding standard     *
 ******************************************************************************/
package Mkt;

import com.CommFunction;
import com.CommRoutine;
import com.SqlParm;
import com.BaseBatch;

public class MktS501 extends BaseBatch {
private String progname = "DS業務卡片點數處理  109/12/10 V1.00.02";
CommFunction comm = new CommFunction();
CommRoutine comr = null;
// -----------------------------------------------------------------------------
hdata.CrdCard hCard = new hdata.CrdCard();

private String hIssueYm = "";
private String hValidDate = "";
private int hNewCardDays = 0;
private double hAddCardPoint = 0;
private double hSupCardPoint = 0;
private int hAddCardMax = 0;
private int hSupCardMax = 0;
private String hPromoteType = "";
private String hDataCode1 = "";
private String hDataRemark1 = "";
private double hAddPoint1 = 0;
private double hAddPoint2 = 0;
private double hPointAmt = 0;
private int hCutCardMon = 0;
private String isNewcardDate = "";
// --ds
private String hDsNewAddFlag = "";
private double hDsSup0Num = 0;
private double hDsSup1Num = 0;
private String hDsSalesId = "";
private String hDsMangrId = "";
private String hDsPointValidDate = "";
private String hDsPromoteType = "";
private double hDsCardPoint = 0;
private String hDsCardNo = "";
private String hDsIdPSeqno = "";
String hDsPromoteLoc="";
String hDsErrCode="";

// --card
// --------------------------------------------------------------------------
private int tiCard1 = -1;
private int tiCard2 = -1;
private int tiSup0 = -1;
private int tiSup1 = -1;
private int tiSales = -1;
private int tiCard = -1;
com.DataSet dsParm = new com.DataSet();
com.DataSet dsCard = new com.DataSet();
private SqlParm ttDsCardI = null;
private SqlParm ttDsCardU = null;
private SqlParm ttDsCardU2 = null;
// =============================================================================
public static void main(String[] args) {
	MktS501 proc = new MktS501();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit(0);
}

// =============================================================================
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg = args.length;
	if (liArg > 1) {
		printf("Usage : MktS501 [batch_seq]");
		errExit(1);
	}

	dbConnect();

	// comr = new CommRoutine(getDBconnect(), getDBalias());
	if (liArg > 0) {
		hIssueYm = args[0];
		callBatchSeqno(args[liArg - 1]);
	}
	callBatch(0, 0, 0);

	if (empty(hIssueYm)) {
		hIssueYm = commDate.dateAdd(hBusiDate, 0, -1, 0);
	}

	//-新增推廣卡-
   printf("-->新增推廣月份=[%s]",hIssueYm);
	selectMktDsParm11();
	selectCrdCardAdd();
	
	//-卡片停用處理-
	newAddResetPoint();
	selectMktDsParm13();
	selectMktDsCardStop();

	sqlCommit(1);
	endProgram();
}

void selectMktDsParm11() throws Exception {
	sqlCmd = "select * "
		+ " from mkt_ds_parm1 "
		+ " where param_type ='1' "
		+ " and apr_flag ='Y' "
		+ " and valid_date <= ? "
		+ " order by valid_date desc "
		+ commSqlStr.rownum(1);

	ppp(1, hBusiDate);

	sqlSelect();

	hValidDate = colSs("valid_date");
	hNewCardDays = colInt("new_card_days");
	hAddCardPoint = colNum("add_card_point");
	hSupCardPoint = colNum("sup_card_point");
	hAddCardMax = colInt("add_card_max");
	hSupCardMax = colInt("sup_card_max");
//	h_promote_type = col_ss("promote_type");

	isNewcardDate = commDate.dateAdd(hBusiDate, 0, 0,0 - hNewCardDays);

	sqlCmd = " select promote_type,"
		+ " data_code1 , data_code2,"
		+ " data_remark1 , "
		+ " add_point1 , "
		+ " add_point2 , "
		+ " point_amt "
		+ " from mkt_ds_parm1_detl "
		+ " where valid_date = ? "
		+ " and param_type ='1' "
		+ " and apr_flag='Y' "
		+ " order by promote_type, add_point1 ";
	this.sqlQuery(dsParm, sqlCmd, new Object[] {hValidDate});

}

//=============================================================================
void newAddResetPoint() throws Exception {
	
	String lsIssueDate = "" , lsIdPSeqno = "" , lsSalesId = "" , lsCardNo = "";
	
	sqlCmd = " select "
			 + " issue_date , id_p_seqno , sales_id , count(*) as xx_cnt "
			 + " from mkt_ds_card "
			 + " where 1=1 and issue_date like ? "
			 + " and new_add_flag ='1' "
			 + " group by id_p_seqno , issue_date , sales_id "
			 + " having count(*) > 1 "
			 ;
	
	ppp(1,hIssueYm+"%");
	
	sqlSelect();
	
	if(sqlNrow<=0)	return ;
	
	lsIssueDate = colSs("issue_date");
	lsIdPSeqno = colSs("id_p_seqno");
	lsSalesId = colSs("sales_id");
	
	sqlCmd = " select "
			 + " card_no , "
			 + " card_point "
			 + " from mkt_ds_card "
			 + " where issue_date = ? "
			 + " and new_add_flag ='1' "
			 + " and id_p_seqno = ? "
			 + " and sales_id = ? "
			 + " order by card_point Desc ";
		this.sqlQuery(dsCard, sqlCmd, new Object[] {lsIssueDate , lsIdPSeqno , lsSalesId});
	
	for(int ii=1;ii<dsCard.rowCount();ii++){		
		
		if(ttDsCardU2==null){
			ttDsCardU2 = new com.SqlParm();
			sqlCmd = " update mkt_ds_card set "
					 + " new_add_flag = '2' , "
					 + " card_point = ? "
					 + " where card_no = ? "
					 ;
			ttDsCardU2.pfidx =ppStmtCrt("tt_ds_card_u_2","");		
		}
		
		ppp(1,hAddCardPoint);
		ppp(dsCard.colss(ii, "card_no"));
		
		sqlExec(ttDsCardU2.pfidx, ttDsCardU2.getConvParm());
		if (sqlNrow <= 0) {
			sqlerr("update mkt_ds_card error");
			errExit(1);
		}
		
	}
		
}

//=============================================================================
void selectMktDsParm13() throws Exception {
	sqlCmd = " select "
		+ " cut_card_mon "
		+ " from mkt_ds_parm1 "
		+ " where 1=1 "
		+ " and param_type ='3' "
		+ " and apr_flag ='Y' "
		+ " and valid_date <= ? "
		+ " order by valid_date desc "
		+ commSqlStr.rownum(1);
	
	ppp(1, hBusiDate);

	sqlSelect();

	hCutCardMon = colInt("cut_card_mon");

}

//=============================================================================
void selectMktDsCardStop() throws Exception {
	String lsStrDate = commDate.monthAdd(hBusiDate,0 - hCutCardMon) + "01";
	printf("卡片停用:"+lsStrDate+" 停卡月數:"+hCutCardMon);
	fetchExtend = "ds.";
	sqlCmd = " select "
		+ " card_no , "
		+ " issue_date , "
		+ " id_p_seqno "
		+ " from mkt_ds_card "
		+ " where issue_date >= ? "
		+" and oppost_date =''"
		;

	ppp(1, lsStrDate);

	openCursor();

	int llTotCnt=0, llStopCnt=0;
	
	while (fetchTable()) {
		llTotCnt++;
		
		hDsCardNo = colSs("ds.card_no");
		hDsIdPSeqno = colSs("ds.id_p_seqno");
		if (tiCard <= 0) {
			sqlCmd = "select "
				+ " sum(decode(current_code,'0',1,0)) as curr_cnt , "
				+ " max(oppost_date) as max_oppost_date "
				+ " from crd_card "
				+ " where ori_card_no = ? "
				+ " and id_p_seqno = ? ";
			tiCard = ppStmtCrt("ti_card", "");
		}

		ppp(1, hDsCardNo);
		ppp(hDsIdPSeqno);
		sqlSelect(tiCard);

		if (sqlNrow <= 0) {
			continue;
		}

		hCard.oppostDate =colSs("max_oppost_date");

		// --有活卡
		if (colInt("curr_cnt") > 0 || empty(hCard.oppostDate))
			continue;

		//-是否在N月內剪卡-
		String lsDate=commDate.monthAdd(colSs("ds.issue_date"), hCutCardMon);
		if (lsDate.compareTo(commString.left(hBusiDate,6)) > 0) {
			continue;
		}
//		printf("-JJJ->issue_date[%s], card_no[%s], month[%s]",ls_date,h_ds_card_no,h_cut_card_mon);

		updateMktDsCard();
		llStopCnt++;
	}

	printf("卡片停用處理筆數[%s], 符合筆數[%s]", llTotCnt, llStopCnt);
	closeCursor();
}

//=============================================================================
void selectCrdCardAdd() throws Exception {
	sqlCmd = " select "
		+ " card_no , "
		+ " issue_date , "
		+ " card_type , "
		+ " group_code , "
		+ " sup_flag , "
		+ " p_seqno , "
		+ " id_p_seqno , "
		+ " apply_no , "
		+ " introduce_id , "
		+ " promote_emp_no , "
		+ " oppost_date "
		+ " from crd_card "
		+ " where 1=1 "
		+ " and issue_date like ? "
		+ " and card_no =ori_card_no"
		+ " and (introduce_id like 'DS%' or promote_emp_no like 'DS%') "		
		+ " order by id_p_seqno, apply_no ";
	ppp(1, hIssueYm + "%");

	//fetchExtend = "card.";
	openCursor();
	while (fetchTable()) {
		this.totalCnt++;
		
		// --
		hCard.cardNo = colSs("card_no");
		hCard.issueDate = colSs("issue_date");
		hCard.cardType = colSs("card_type");
		hCard.groupCode = colSs("group_code");
		hCard.supFlag = colSs("sup_flag");
		hCard.pSeqno = colSs("p_seqno");
		hCard.idPSeqno = colSs("id_p_seqno");
		hCard.applyNo = colSs("apply_no");
		hCard.introduceId = colSs("introduce_id");
		hCard.promoteEmpNo = colSs("promote_emp_no");
		hCard.oppostDate = colSs("oppost_date");

		// --
		hDsNewAddFlag = "2";
		if (eqIgno(hCard.supFlag, "0") && checkNewCardHolder() == 1) {
			hDsNewAddFlag = "1";
		}

		if (eqIgno(hCard.supFlag, "1")) {
			selectSup1();
		}
		else if (eqIgno(hDsNewAddFlag,"2")) {
			selectSup0();
		}

		hDsErrCode ="";
		selectDsSales();
		if (empty(hDsSalesId))
			hDsErrCode ="01";		//推廣人員:N-find
		
		setCardPoint();  //02.專案地點:未符合
		
		insertMktDsCard();
		resetData();
	}
	
	closeCursor();
}

//=============================================================================
int checkNewCardHolder() throws Exception {

	if (tiCard1 <= 0) {
		sqlCmd = " select "
			+ " count(*) as db_card_1 "
			+ " from crd_card "
			+ " where id_p_seqno = ? "
			+ " and current_code='0' "
			+ " and card_no <> ? "
			+ " and issue_date <= ? ";
		tiCard1 = ppStmtCrt("ti_card1", "");
	}
	ppp(1, hCard.idPSeqno);
	ppp(hCard.cardNo);
	ppp(hCard.issueDate);
	sqlSelect(tiCard1);
	if (sqlNrow>0 && colNum("db_card_1")>0) {
		return -1;
	}

	if (tiCard2 <= 0) {
		sqlCmd = " select count(*) as db_card_2 "
			+ " from crd_card "
			+ " where id_p_seqno = ? "
			+ " and card_no <> ? "
			+ " and issue_date <= ? "
			+ " and oppost_date >= ? ";
		tiCard2 = ppStmtCrt("ti_card2", "");
	}
	ppp(1, hCard.idPSeqno);
	ppp(hCard.cardNo);
	ppp(hCard.issueDate);
	ppp(isNewcardDate);
	sqlSelect(tiCard2);
	if (sqlNrow>0 && colNum("db_card_2")>0)
		return -1;

	return 1;
}

void selectSup0() throws Exception {
	//-第N張正卡-
	
	if (tiSup0 <= 0) {
		sqlCmd = " select "
			+ " count(*) as db_cnt_0 "
			+ " from crd_card "
			+ " where id_p_seqno = ? "
			+ " and sup_flag ='0' "
			+ " and ori_card_no=card_no "
			+ " and issue_date < ? ";
		tiSup0 = ppStmtCrt("ti_sup_0", "");
	}
	ppp(1, hCard.idPSeqno);
	ppp(hCard.issueDate);
	sqlSelect(tiSup0);
	hDsSup0Num = colNum("db_cnt_0") + 1;
}

void selectSup1() throws Exception {
	//-第N張附卡-
	if (tiSup1 <= 0) {
		sqlCmd = " select "
			+ " count(*) as db_cnt_1 "
			+ " from crd_card "
			+ " where id_p_seqno = ? "
			+ " and sup_flag ='1' "
			+ " and ori_card_no=card_no "
			+ " and issue_date < ? ";
		tiSup1 = ppStmtCrt("ti_sup_1", "");
	}
	ppp(1, hCard.idPSeqno);
	ppp(hCard.issueDate);
	sqlSelect(tiSup1);
	hDsSup1Num = colNum("db_cnt_1") + 1;
}

void selectDsSales() throws Exception {
	hDsSalesId ="";
	hDsMangrId ="";
	
	if (tiSales <= 0) {
		sqlCmd = " select sales_id, mangr_id "
			+ " from mkt_ds_sales "
			+ " where sales_id = ? ";
		tiSales = ppStmtCrt("ti_sales", "");
	}
	
	ppp(1, hCard.promoteEmpNo);
	sqlSelect(tiSales);
	if (sqlNrow >0) {
		hDsSalesId = colSs("sales_id");
		hDsMangrId = colSs("mangr_id");
	}
}

void setCardPoint() throws Exception {
	hDsPointValidDate = hValidDate;
	hDsPromoteLoc ="";

	//-推廣地點-
	String lsIntrId =hCard.introduceId;
	if (lsIntrId.length()!=6) {
      hDsPromoteLoc = commString.left(lsIntrId, 4);
   }
   lsIntrId =hDsPromoteLoc;

	if (eqIgno(hCard.supFlag, "1")) {
		if (hDsSup1Num <= hSupCardMax) {
			hDsCardPoint = hSupCardPoint;
		}
		return;
	}

	if (eqIgno(hDsNewAddFlag, "2")) {
		if (hDsSup0Num <= hAddCardMax) {
			hDsCardPoint = hAddCardPoint;
		}
		return;
	}
	
	if (eqIgno(hDsNewAddFlag, "1")) {
		for (int ii = 0; ii < dsParm.rowCount(); ii++) {
			hPromoteType = dsParm.colss(ii, "promote_type");
			hDsPromoteType = hPromoteType;
         if (eq(hPromoteType, "01")) {
            // --團代+地點
            if (eq(hCard.groupCode, dsParm.colss(ii, "data_code1")) &&
            eq(lsIntrId,dsParm.colss(ii,"data_code2"))) {
               hDsCardPoint = dsParm.colnum(ii, "add_point1");
               return;
            }
            else continue;
         }
			else if (eq(hPromoteType, "02")) {
				// --團代
				if (eq(hCard.groupCode, dsParm.colss(ii, "data_code1"))) {
					hDsCardPoint = dsParm.colnum(ii, "add_point1");
					return;
				}
				else continue;
			}
			else if (eq(hPromoteType, "03")) {
				// --推廣地點
				if (eq(lsIntrId, dsParm.colss(ii, "data_code2"))) {
					hDsCardPoint = dsParm.colnum(ii, "add_point1");
					return;
				}
				else continue;
			}
		}
	}
	
	if (hDsCardPoint <=0)
		hDsErrCode ="02";		//專案,地點未符合
	
}

//=============================================================================
void insertMktDsCard() throws Exception {
	if (ttDsCardI == null) {
		ttDsCardI = new com.SqlParm();
		ttDsCardI.sqlFrom = " insert into mkt_ds_card ( "
			+ " card_no ,"
			+ " issue_date ,"
			+ " card_type ,"
			+ " group_code ,"
			+ " sup_flag ,"
			+ " p_seqno ,"
			+ " id_p_seqno ,"
			+ " apply_no ,"
			+ " introduce_id,"
			+ " promote_emp_no ,"
			+ " oppost_date ,"
			+ " new_add_flag ,"
			+ " sup0_num ,"
			+ " sup1_num ,"
			+ " sales_id ,"
			+ " mangr_id ,"
			+ " point_valid_date ,"
			+ " promote_type , promote_loc,"
			+ " card_point ,"
			+" point_err_code,"
			+ " sale_amt_yymm ,"
			+ " mod_user ,"
			+ " mod_time ,"
			+ " mod_pgm ,"
			+ " mod_seqno "
			+ " ) values ( "
			+ ttDsCardI.pmkk(":card_no ,")
			+ ttDsCardI.pmkk(":issue_date ,")
			+ ttDsCardI.pmkk(":card_type ,")
			+ ttDsCardI.pmkk(":group_code ,")
			+ ttDsCardI.pmkk(":sup_flag ,")
			+ ttDsCardI.pmkk(":p_seqno ,")
			+ ttDsCardI.pmkk(":id_p_seqno ,")
			+ ttDsCardI.pmkk(":apply_no ,")
			+ ttDsCardI.pmkk(":introduce_id ,")
			+ ttDsCardI.pmkk(":promote_emp_no ,")
			+ ttDsCardI.pmkk(":oppost_date ,")
			+ ttDsCardI.pmkk(":new_add_flag ,")
			+ ttDsCardI.pmkk(":sup0_num ,")
			+ ttDsCardI.pmkk(":sup1_num ,")
			+ ttDsCardI.pmkk(":sales_id ,")
			+ ttDsCardI.pmkk(":mangr_id ,")
			+ ttDsCardI.pmkk(":point_valid_date ,")
			+ ttDsCardI.pmkk(":promote_type ,")
            + ttDsCardI.pmkk(":promote_loc ,")
			+ ttDsCardI.pmkk(":card_point ,")
			+ttDsCardI.pmkk(":point_err_code,")
			+ ttDsCardI.pmkk(":sale_amt_yymm ,")
			+ modxxxInsert()
			+ " )";
		ttDsCardI.pfidx = ppStmtCrt("tt_ds_card_i-A", ttDsCardI.sqlFrom);
	}

	ttDsCardI.ppp("card_no", hCard.cardNo);
	ttDsCardI.ppp("issue_date", hCard.issueDate);
	ttDsCardI.ppp("card_type", hCard.cardType);
	ttDsCardI.ppp("group_code", hCard.groupCode);
	ttDsCardI.ppp("sup_flag", hCard.supFlag);
	ttDsCardI.ppp("p_seqno", hCard.pSeqno);
	ttDsCardI.ppp("id_p_seqno", hCard.idPSeqno);
	ttDsCardI.ppp("apply_no", hCard.applyNo);
	ttDsCardI.ppp("introduce_id", hCard.introduceId);
	ttDsCardI.ppp("promote_emp_no", hCard.promoteEmpNo);
	ttDsCardI.ppp("oppost_date", hCard.oppostDate);
	ttDsCardI.ppp("new_add_flag", hDsNewAddFlag);
	ttDsCardI.ppp("sup0_num", hDsSup0Num);
	ttDsCardI.ppp("sup1_num", hDsSup1Num);
	ttDsCardI.ppp("sales_id", hDsSalesId);
	ttDsCardI.ppp("mangr_id", hDsMangrId);
	ttDsCardI.ppp("point_valid_date", hDsPointValidDate);
	ttDsCardI.ppp("promote_type", hDsPromoteType);
   ttDsCardI.ppp("promote_loc", hDsPromoteLoc);
	ttDsCardI.ppp("card_point", hDsCardPoint);
	ttDsCardI.ppp("point_err_code", hDsErrCode);
	ttDsCardI.ppp("sale_amt_yymm", commString.mid(hCard.issueDate, 0, 6));

	sqlExec(ttDsCardI.pfidx, ttDsCardI.getConvParm());
	if (sqlNrow <= 0 && sqlDuplRecord) {
	   printf("-->mkt_ds_card.insert 重複, card_no=[%s]",hCard.cardNo);
//		sqlerr("insert mkt_ds_card error");
//		err_exit(1);
	}
	return;
}

void updateMktDsCard() throws Exception {
	if (ttDsCardU == null) {
		ttDsCardU = new com.SqlParm();
		ttDsCardU.sqlFrom = " update mkt_ds_card set "
			+ " oppost_date = ? , "
			+ " cut_proc_date =" + commSqlStr.sysYYmd + " , "
			+ " cut_amt_yymm = ? , "
			+" card_cut_flag ='Y',"
			+commSqlStr.setModXxx(hModUser, hModPgm)
			+ " where card_no = ? ";
		ttDsCardU.pfidx = ppStmtCrt("tt_ds_card_u", ttDsCardU.sqlFrom);
	}

	ttDsCardU.ppp(1, hCard.oppostDate);
	ttDsCardU.ppp(commString.mid(hCard.oppostDate, 0, 6));
//	tt_ds_card_u.ppp(3, h_mod_user);
//	tt_ds_card_u.ppp(4, h_mod_pgm);
	ttDsCardU.ppp(hDsCardNo);

//	ddd(tt_ds_card_u.ddd_sql());
	sqlExec(ttDsCardU.pfidx, ttDsCardU.getConvParm());
	if (sqlNrow <= 0) {
		sqlerr("update mkt_ds_card error");
		errExit(1);
	}

	return;
}

void resetData(){
	hDsNewAddFlag = "";
	hDsSup0Num = 0 ;
	hDsSup1Num = 0 ;
	hDsSalesId = "";
	hDsMangrId = "";
	hDsPointValidDate = "";
	hDsPromoteType = "";
	hDsCardPoint = 0 ;
	hDsErrCode = "";
}

}
