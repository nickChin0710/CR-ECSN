/*=***************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/07/18  V1.00.1     Alex     bugfix
* 107/03/12  V1.00.0     Alex     initial                                    *
* 2018-0719					JH			test-OK
*  109/12/04  V1.00.01    shiyuqi       updated for project coding standard   * 
******************************************************************************/
package Cms;

import com.CommFunction;
import com.BaseBatch;


public class CmsA011 extends BaseBatch {
private String progname = "道路救援免費白金效期更正處理程式  109/12/04  V1.00.01 ";
//CommFunction comm = new CommFunction();
//==============================================

// ---------------------------------------------------------------------------
private String hRoadRowid = "";
private String hCardNewEndDate = "";
String hRoadCardNo ="";
// ---------------------------------------------------------------------------
private int tiRoadmasterU=-1;

int commit=1;

//==***************************************************************************
public static void main(String[] args) {
	CmsA011 proc = new CmsA011();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);

	int liArg = args.length;
	if (liArg > 1) {
		printf("Usage : CmsA011");
		errExit(1);
	}

	if (!connectDataBase())
		errExit(1);

	this.selectPtrBusinday();
	setModXxx();
	callBatch(0, 0, 0);

	selectCmsRoadmaster();
	printf("累計處理筆數 : [%s]", totalCnt);

	sqlCommit(commit);
	endProgram();
}

// ---------------------------------------------------------------------------
void selectCmsRoadmaster() throws Exception {
	this.fetchExtend = "main.";
	sqlCmd = " select "
		+ " hex(B.rowid) as rowid,"
		+ " substr(a.new_end_date,1,6) as card_new_end_date,"
		+" A.card_no "
		+ " from crd_card A join cms_roadmaster B on A.card_no =B.card_no"
		+ " where 1=1 "
		+ " and A.new_end_date not like b.rm_validdate||'%' "
		+ " and A.card_note ='P' "
		+ " and b.rm_status <> '0' "
		+ " and b.rm_type = 'F' ";

	openCursor();

	while (fetchTable()) {
		// ---
		hRoadRowid = colSs("main.rowid");
		hCardNewEndDate = colSs("main.card_new_end_date");
		hRoadCardNo =colSs("main.card_no");

		totalCnt++;
		
//		ddd("%s. card_no[%s]...",totalCnt,h_road_card_no);
		
		updateCmsRoadmaster();
		
		if (debug) {
			if (totalCnt >1000)
				break;
		}
	}
}

// ---------------------------------------------------------------------------
void updateCmsRoadmaster() throws Exception {
	if (tiRoadmasterU <=0) {
		sqlCmd = " update cms_roadmaster set "
			+ " rm_validdate = ? , "
			+ " mod_time = sysdate , "
			+ " mod_pgm = ? "
			+ " where rowid = ? ";
		tiRoadmasterU =ppStmtCrt("ti_roadmaster_U","");
	}

	ppp(1, hCardNewEndDate);
	ppp(2, hModPgm);
	setRowId(3, hRoadRowid);
	sqlExec(tiRoadmasterU);

	if (sqlNrow <= 0) {
		sqlerr("update cms_roadmaster error, kk[%s]",hRoadCardNo);
		errExit(1);
	}
}

//--EEEEEEEEEEEE--
}
