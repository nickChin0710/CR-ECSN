/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  --------- ----------  ----------------------------------------- *
 * 112/02/18  V1.00.01  Zuwei Su    program initial                           *
 * 2023-0704  V1.00.01  JH          re-coding                                 *
 * 2023/11/22 V1.00.03  Kirin       每月1日執行                                 *                                                                           *
 ******************************************************************************/
package Mkt;

import Cms.CmsRightParm;
import com.DataSet;
import com.Parm2sql;
import com.BaseBatch;

import java.text.Normalizer;

public class MktE020 extends BaseBatch {
private final String PROGNAME = "卡友各項權益統計-機場接送/停車  2023-0704  V1.00.01";
// ------------------------------------------------------
Cms.CmsRightParm ooParm = null;
//-HH--
int hhUseCnt = 0;
double hhRcvAnnualFee = 0;
String hhMatchFlag = "";
String isAcctYear = "";

//--

//=*****************************************************************************
public static void main(String[] args) {
    MktE020 proc = new MktE020();

//	proc.debug = true;
    proc.runCheck = true;
    proc.mainProcess(args);
    proc.systemExit();
}
@Override
protected void dataProcess(String[] args) throws Exception {
    dspProgram(PROGNAME);

    if (args.length > 2) {
        printf("Usage : CmsE020 [busi_date(08), callbatch_seqno]");
        okExit(0);
    }

    if (args.length >= 1) {
        if (args[0].length()==8) {
            String sG_Args0 = args[0];
            hBusiDate = Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
        }
        callBatchSeqno(args[0]);
    }
    if (args.length == 2) {
        callBatchSeqno(args[1]);
    }

    dbConnect();
    callBatch(0, 0, 0);

    ooParm = new CmsRightParm(getDBconnect(), getDBalias(),hBusiDate);
    ooParm.setYymm();
    

    if (!hBusiDate.substring(6).equals("01")) {                            
        showLogMessage("I", "", "本程式只在每月1日執行, 本日非執行日!! process end...." +"本日="+ hBusiDate );
        okExit(0);
     }  
    
    printf("-- 資料處理年月[%s], busiDate[%s], acctMonth[%s]"
            , ooParm.isProcYymm, hBusiDate, ooParm.isAcctYymm);
    isAcctYear =commString.left(ooParm.isAcctYymm,4);

//   deleteMkt_right_year_detl();
//   sqlCommit();

    selectCmsRightParm();
//   if (dsParm.rowCount() <=0) {
//      printf("-- 未指定貴賓室參數(cmsm4210)");
//      okExit(0);
//   }

    sqlCommit();
    endProgram();
}
//=================
com.DataSet dsParm=new DataSet();
com.DataSet dsDetl=new DataSet();
void selectCmsRightParm() throws Exception {
    sqlCmd ="select * from cms_right_parm"+
            " where apr_flag='Y' and active_status='Y'"+
            " and ? between proj_date_s and decode(proj_date_e,'','20991231',proj_date_e)"+
            " and card_hldr_flag ='2'"+  //卡友
            " and item_no in ('08','09')"+
    "";
    //-TTT--
//    sqlCmd +=" and proj_code='2023080002' and item_no='08' ";
//    sqlCmd +=" order by item_no, proj_code";

    ppp(1, hBusiDate);
    Object[] parms =getSqlParm();
    sqlQuery(dsParm,"",parms);
    int llNrow =dsParm.rowCount();
    if (llNrow <=0) {
        printf("-- 未指定機場接送/停車參數(cmsm4210)");
        return;
    }

    sqlCmd = " select A.proj_code||'-'||A.item_no||'-'||A.data_type as kk_data"
            +", A.data_code, A.data_code2, A.data_code3"
            +" from cms_right_parm_detl A join cms_right_parm B"+
            " on A.proj_code=B.proj_code and A.item_no=B.item_no"
            +" where A.apr_flag ='Y' and A.table_id = 'RIGHT'"
            +" and B.apr_flag='Y' and B.active_status ='Y' and B.card_hldr_flag='2'"
            +" and B.item_no in ('08','09')"
            +" AND ? between B.proj_date_s and decode(B.proj_date_e,'','20991231',B.proj_date_e)"
            +" order by 1"
    ;
    this.sqlQuery(dsDetl, sqlCmd,new Object[]{hBusiDate});
    dsDetl.loadKeyData_dupl("kk_data");

    ooParm.setDsParm(dsParm);
    ooParm.setDsDetl(dsDetl);

    for (int ll = 0; ll <llNrow ; ll++) {
        dsParm.listCurr(ll);
        String lsItemNo =dsParm.colSs("item_no");
        String ls_projCode=dsParm.colSs("proj_code");
        printf("--process item_no[%s], proj_code[%s]...", lsItemNo, ls_projCode);

        selectCrdCard();
    }
}
//------------------
void selectCrdCard() throws Exception {
    String lsProjCode=dsParm.colSs("proj_code");
    String lsItemNo=dsParm.colSs("item_no");
    boolean condAcctType =dsParm.colEq("acct_type_flag","Y");
    boolean condGroupCard =dsParm.colEq("group_card_flag","Y");
    if (!condAcctType && !condGroupCard) {
        printf(" 專案代碼[%s] 未指定帳戶類別, 卡種+團代", lsProjCode);
        return;
    }

    int pp=0;
    sqlCmd ="select A.card_no, A.major_card_no, A.id_p_seqno, A.major_id_p_seqno"+
            ", A.group_code, A.card_type, A.acct_type"+
            ", A.issue_date"+
            " FROM crd_card A "+
            " WHERE A.current_code='0'";
    if (condAcctType) {
        sqlCmd +=" AND A.acct_type IN (SELECT data_code FROM cms_right_parm_detl"+
                " WHERE apr_flag='Y' AND data_type='01' AND proj_code=? and item_no=?)";
        ppp(++pp,lsProjCode);
        ppp(++pp, lsItemNo);
    }
    if (condGroupCard) {
        sqlCmd +=" AND EXISTS (SELECT 1 FROM cms_right_parm_detl"+
                " WHERE apr_flag='Y' AND data_type='02' AND proj_code=? and item_no=?"+
                " AND A.group_code like data_code||'%'"+
                " AND A.card_type like data_code2||'%'"+
                " )"
        ;
        ppp(++pp, lsProjCode);
        ppp(++pp, lsItemNo);
    }
    sqlCmd +=" order by A.card_no";

    int ll_totCnt=0;
    openCursor();
    while(fetchTable()) {
        ll_totCnt++;
        totalCnt++;
        dspProcRow(2000);

        ooParm.initHh();
        String ls_cardNo =colSs("card_no");
        ooParm.selectCrdCard(ls_cardNo);
        if (empty(ooParm.hhCardNo)) continue;
        //--
        ooParm.checkProjCode(dsParm.getCurrRow());
        if (ooParm.hhFreeCnt >0) hhMatchFlag ="Y";
        else hhMatchFlag ="N";

        //--消費金額累積方式: 0.不計算消費 1.By ID計算 2.正附卡合併計算 3.正附卡分開計算--
        if (ooParm.hhCurrTotAmt <=0) {
            String ls_consumType =dsParm.colNvl("consume_type","0");
            String ls_itType =dsParm.colNvl("it_1_type","1");
            ooParm.selectBilBillCur(ooParm.isAcctYymm,ooParm.isAcctYymm,0,ls_itType,ls_consumType);
        }

        //selectCms_ppcard_visit();
        selectCycAfee();

        updateCmsRightYearDtl();
        updateCmsRightYear();
    }
    closeCursor();
    printf(" PPCard:proj_code[%s], item_no[%s], totCnt[%s]", lsProjCode, lsItemNo, ll_totCnt);
}
//=======================
//---------
int tiAfee=-1;
void selectCycAfee() throws Exception {
    hhRcvAnnualFee =0;
    if (tiAfee <=0) {
        sqlCmd ="select sum(rcv_annual_fee) rcv_fee"+
                " from cyc_afee"+
                " where card_no =?"+
                " and card_fee_date like ?"+
                "";
        tiAfee =ppStmtCrt("ti-afee","");
    }

    String ls_date1 =commString.left(ooParm.isAcctYymm,4)+"%";
    ppp(1,ooParm.hhCardNo);
    ppp(ls_date1);
    sqlSelect(tiAfee);
    if (sqlNrow >0) {
        hhRcvAnnualFee =colNum("rcv_fee");
    }
}
//======================
Parm2sql ttUyeard=null;
void updateCmsRightYearDtl() throws Exception {
    if (ttUyeard ==null) {
        ttUyeard =new Parm2sql();
        ttUyeard.update("cms_right_year_dtl");
    }

//   ttUyeard.aaa("acct_month", ooParm.isAcctYymm);          	//-VARCHAR (6,0)  帳務年月
//   ttUyeard.aaa("id_p_seqno", ooParm.hh_id_pseqno);          	//-VARCHAR (10,0)  卡人流水號碼
//   ttUyeard.aaa("card_no", ooParm.hh_card_no);             	//-VARCHAR (19,0)  卡號
//   ttUyeard.aaa("item_no", ooParm.hh_item_no);             	//-VARCHAR (2,0)  權益類別
//   ttUyeard.aaa("proj_code", ooParm.hh_proj_code);           	//-VARCHAR (10,0)  適用專案
    ttUyeard.aaa("use_cnt", hhUseCnt);             	//-INTEGER (4,0)  已使用次數
    //006	free_per_amt        	//-DECIMAL (12,0)  每次折抵金額
    ttUyeard.aaa("curr_month_amt", ooParm.hhCurrTotAmt);      	//-DECIMAL (12,0)  消費金額
    ttUyeard.aaa("curr_month_cnt", ooParm.hhCurrTotCnt);      	//-DECIMAL (12,0)  消費次數
    //009	platform_kind_amt   	//-DECIMAL (12,0)  排除一般消費金額
    //010	platform_kind_cnt   	//-DECIMAL (12,0)  排除一般消費次數
    //011	used_next_cnt       	"//-INTEGER (4,0)  預支使用次數 	  "
    //012	gift_cnt            	"//-INTEGER (4,0)  加贈次數     	  "
    //013	bonus_cnt           	//-INTEGER (4,0)  紅利兌換贈送次數
    ttUyeard.aaa("rcv_annual_fee", hhRcvAnnualFee);      	//-DECIMAL (10,2)  應收年費
//   ttUyeard.aaa("acct_type", ooParm.hh_acc           	//-VARCHAR (2,0)  帳戶類別
    //023	card_type           	//-VARCHAR (2,0)  卡片種類
    //024	group_code          	//-VARCHAR (4,0)  團體代號
    ttUyeard.aaa("free_type", ooParm.hhFreeType);           	//-VARCHAR (1,0)  免費次數類別
    ttUyeard.aaa("free_cnt", ooParm.hhFreeCnt);            	//-INTEGER (4,0)  免費次數
    ttUyeard.aaa("match_flag", hhMatchFlag);          	//-VARCHAR (1,0)  符合優惠註記
    ttUyeard.aaa("crt_date", sysDate);            	//-VARCHAR (8,0)  建檔日期
    ttUyeard.aaa("crt_user", hModUser);            	//-VARCHAR (10,0)  建檔經辦
    //017	cal_seqno           	//-DECIMAL (10,0)  權益流水號
    ttUyeard.aaaModxxx(hModUser, hModPgm);
    //018	mod_user            	//-VARCHAR (10,0)  異動使用者
    //019	mod_time            	//-TIMESTMP(10,6)  異動時間
    //020	mod_pgm             	//-VARCHAR (20,0)  異動程式
    //021	mod_seqno           	//-DECIMAL (10,0)  異動註記
    ttUyeard.aaaWhere(" where acct_month=?", ooParm.isAcctYymm);
    ttUyeard.aaaWhere(" and id_p_seqno=?", ooParm.hhIdPseqno);
    ttUyeard.aaaWhere(" and card_no=?", ooParm.hhCardNo);
    ttUyeard.aaaWhere(" and item_no=?", ooParm.hhItemNo);
    ttUyeard.aaaWhere(" and proj_code=?", ooParm.hhProjCode);

    if (ttUyeard.ti <=0) {
        ttUyeard.ti =ppStmtCrt("tt-U-yearD",ttUyeard.getSql());
    }

    sqlExec(ttUyeard.ti, ttUyeard.getParms());
    if (sqlNrow <0) {
        sqlerr("update cms_right_year_dtl error");
        errExit(1);
    }
    if (sqlNrow ==0) {
        insertCmsRightYearDtl();
    }
}
//---------
Parm2sql ttAyeard=null;
void insertCmsRightYearDtl() throws Exception {
    if (ttAyeard ==null) {
        ttAyeard =new Parm2sql();
        ttAyeard.insert("cms_right_year_dtl");
    }

    ttAyeard.aaa("acct_month", ooParm.isAcctYymm);          	//-VARCHAR (6,0)  帳務年月
    ttAyeard.aaa("id_p_seqno", ooParm.hhIdPseqno);          	//-VARCHAR (10,0)  卡人流水號碼
    ttAyeard.aaa("card_no", ooParm.hhCardNo);             	//-VARCHAR (19,0)  卡號
    ttAyeard.aaa("item_no", ooParm.hhItemNo);             	//-VARCHAR (2,0)  權益類別
    ttAyeard.aaa("proj_code", ooParm.hhProjCode);           	//-VARCHAR (10,0)  適用專案
    ttAyeard.aaa("use_cnt", hhUseCnt);             	//-INTEGER (4,0)  已使用次數
    ttAyeard.aaa("free_per_amt", 0);        	//-DECIMAL (12,0)  每次折抵金額
    ttAyeard.aaa("curr_month_amt", ooParm.hhCurrTotAmt);      	//-DECIMAL (12,0)  消費金額
    ttAyeard.aaa("curr_month_cnt", ooParm.hhCurrTotCnt);      	//-DECIMAL (12,0)  消費次數
    //009	platform_kind_amt   	//-DECIMAL (12,0)  排除一般消費金額
    //010	platform_kind_cnt   	//-DECIMAL (12,0)  排除一般消費次數
    //011	used_next_cnt       	"//-INTEGER (4,0)  預支使用次數 	  "
    //012	gift_cnt            	"//-INTEGER (4,0)  加贈次數     	  "
    //013	bonus_cnt           	//-INTEGER (4,0)  紅利兌換贈送次數
    ttAyeard.aaa("rcv_annual_fee", hhRcvAnnualFee);      	//-DECIMAL (10,2)  應收年費
    ttAyeard.aaa("acct_type", ooParm.hhAcctType);           	//-VARCHAR (2,0)  帳戶類別
    ttAyeard.aaa("card_type", ooParm.hhCardType);           	//-VARCHAR (2,0)  卡片種類
    ttAyeard.aaa("group_code", ooParm.hhGroupCode);          	//-VARCHAR (4,0)  團體代號
    ttAyeard.aaa("free_type", ooParm.hhFreeType);           	//-VARCHAR (1,0)  免費次數類別
    ttAyeard.aaa("free_cnt", ooParm.hhFreeCnt);            	//-INTEGER (4,0)  免費次數
    ttAyeard.aaa("match_flag", hhMatchFlag);          	//-VARCHAR (1,0)  符合優惠註記
    ttAyeard.aaa("crt_date", sysDate);            	//-VARCHAR (8,0)  建檔日期
    ttAyeard.aaa("crt_user", hModUser);            	//-VARCHAR (10,0)  建檔經辦
    //017	cal_seqno           	//-DECIMAL (10,0)  權益流水號
    ttAyeard.aaaModxxx(hModUser, hModPgm);

    if (ttAyeard.ti <=0) {
        ttAyeard.ti =ppStmtCrt("tt-A-yearD", ttAyeard.getSql());
    }

    sqlExec(ttAyeard.ti, ttAyeard.getParms());
    if (sqlNrow <=0) {
        sqlerr("insert cms_right_year_dtl error");
        errExit(1);
    }
}
//--------
Parm2sql ttUyear=null;
void updateCmsRightYear() throws Exception {
    if (ttUyear ==null) {
        ttUyear =new Parm2sql();
        ttUyear.update("cms_right_year");
    }

    //--
    ttUyear.aaa("free_type", ooParm.hhFreeType);           	//-VARCHAR (1,0)  免費次數類別
    ttUyear.aaa("free_cnt", ooParm.hhFreeCnt);            	//-INTEGER (4,0)  免費次數
    ttUyear.aaa("use_cnt", hhUseCnt);             	//-INTEGER (4,0)  已使用次數
    //011	free_per_amt        	//-DECIMAL (12,0)  每次折抵金額
    ttUyear.aaa("curr_proj_amt", ooParm.hhCurrProjAmt);       	//-DECIMAL (12,0)  當年專案金額
    ttUyear.aaa("last_proj_amt", ooParm.hhLastProjAmt);       	//-DECIMAL (12,0)  前年專案金額
    ttUyear.aaa("last_year_consume", ooParm.hhLastYearConsume);   	//-DECIMAL (12,0)  前年消費金額
    ttUyear.aaa("curr_year_consume", ooParm.hhCurrYearConsume);   	//-DECIMAL (12,0)  當年消費金額
    ttUyear.aaa("curr_year_cnt", ooParm.hhCurrYearCnt);       	//-DECIMAL (12,0)  當年消費次數
    ttUyear.aaa("last_year_cnt", ooParm.hhLastYearCnt);       	//-DECIMAL (12,0)  前年消費次數
    ttUyear.aaa("rcv_annual_fee", hhRcvAnnualFee);      	//-DECIMAL (10,2)  應收年費
    ttUyear.aaa("free_cal_date", sysDate);       	//-VARCHAR (8,0)  權益計算日期
//   ttAyear.aaa("card_hldr_flag", "2");      	//-VARCHAR (1,0)  是否行員
//   ttAyear.aaa("debut_type", ooParm.hh_debut_type);          	//-VARCHAR (1,0)  卡友身分
//   ttAyear.aaa("crt_date", sysDate);            	//-VARCHAR (8,0)  建檔日期
//   ttAyear.aaa("crt_user", hModUser);            	//-VARCHAR (10,0)  建檔經辦
    //030	cal_seqno           	//-DECIMAL (10,0)  權益流水號
    ttUyear.aaaModxxx(hModUser, hModPgm);
    //KK--
    ttUyear.aaaWhere(" where acct_year =?", isAcctYear);          	//-VARCHAR (10,0)  卡人流水號碼
    ttUyear.aaaWhere(" and id_p_seqno =?", ooParm.hhIdPseqno);          	//-VARCHAR (10,0)  卡人流水號碼
    ttUyear.aaaWhere(" and card_no =?", ooParm.hhCardNo);             	//-VARCHAR (19,0)  卡號
    ttUyear.aaaWhere(" and item_no =?", ooParm.hhItemNo);             	//-VARCHAR (2,0)  權益類別
    ttUyear.aaaWhere(" and proj_code =?", ooParm.hhProjCode);           	//-VARCHAR (10,0)  適用專案

    if (ttUyear.ti <=0) {
        ttUyear.ti =ppStmtCrt("tt-U-year", ttUyear.getSql());
    }

    sqlExec(ttUyear.ti, ttUyear.getParms());
    if (sqlNrow <0) {
        sqlerr("update cms_right_year error, kk[%s]",ooParm.hhCardNo);
        errExit(1);
    }
    if (sqlNrow ==0) {
        insertCmsRightYear();
    }
}
//----
Parm2sql ttAyear=null;
void insertCmsRightYear() throws Exception {
    if (ttAyear ==null) {
        ttAyear =new Parm2sql();
        ttAyear.insert("cms_right_year");
    }
    //--
    ttAyear.aaa("acct_year", isAcctYear);           	//-VARCHAR (4,0)  帳務年度
    ttAyear.aaa("id_p_seqno", ooParm.hhIdPseqno);          	//-VARCHAR (10,0)  卡人流水號碼
    ttAyear.aaa("card_no", ooParm.hhCardNo);             	//-VARCHAR (19,0)  卡號
    ttAyear.aaa("acct_type", ooParm.hhAcctType);           	//-VARCHAR (2,0)  帳戶類別
    ttAyear.aaa("card_type", ooParm.hhCardType);           	//-VARCHAR (2,0)  卡片種類
    ttAyear.aaa("group_code", ooParm.hhGroupCode);          	//-VARCHAR (4,0)  團體代號
    ttAyear.aaa("item_no", ooParm.hhItemNo);             	//-VARCHAR (2,0)  權益類別
    ttAyear.aaa("proj_code", ooParm.hhProjCode);           	//-VARCHAR (10,0)  適用專案
    ttAyear.aaa("free_type", ooParm.hhFreeType);           	//-VARCHAR (1,0)  免費次數類別
    ttAyear.aaa("free_cnt", ooParm.hhFreeCnt);            	//-INTEGER (4,0)  免費次數
    ttAyear.aaa("use_cnt", hhUseCnt);             	//-INTEGER (4,0)  已使用次數
    //011	free_per_amt        	//-DECIMAL (12,0)  每次折抵金額
    ttAyear.aaa("curr_proj_amt", ooParm.hhCurrProjAmt);       	//-DECIMAL (12,0)  當年專案金額
    ttAyear.aaa("last_proj_amt", ooParm.hhLastProjAmt);       	//-DECIMAL (12,0)  前年專案金額
    ttAyear.aaa("last_year_consume", ooParm.hhLastYearConsume);   	//-DECIMAL (12,0)  前年消費金額
    ttAyear.aaa("curr_year_consume", ooParm.hhCurrYearConsume);   	//-DECIMAL (12,0)  當年消費金額
    ttAyear.aaa("curr_year_cnt", ooParm.hhCurrYearCnt);       	//-DECIMAL (12,0)  當年消費次數
    ttAyear.aaa("last_year_cnt", ooParm.hhLastYearCnt);       	//-DECIMAL (12,0)  前年消費次數
    //018	platform_kind_amt   	//-DECIMAL (12,0)  當年排除一般消費金額
    //019	platform_kind_cnt   	//-DECIMAL (12,0)  當年排除一般消費次數
    //020	used_next_cnt       	"//-INTEGER (4,0)  預支使用次數 		"
    //021	gift_cnt            	"//-INTEGER (4,0)  加贈次數     		"
    //022	bonus_cnt           	"//-INTEGER (4,0)  紅利兌換贈送次數  	"
    ttAyear.aaa("rcv_annual_fee", hhRcvAnnualFee);      	//-DECIMAL (10,2)  應收年費
    //024	rm_carno            	//-VARCHAR (10,0)  車號
    ttAyear.aaa("free_cal_date", sysDate);       	//-VARCHAR (8,0)  權益計算日期
    ttAyear.aaa("card_hldr_flag", "2");      	//-VARCHAR (1,0)  是否行員
    ttAyear.aaa("debut_type", ooParm.hhDebutType);          	//-VARCHAR (1,0)  卡友身分
    ttAyear.aaa("crt_date", sysDate);            	//-VARCHAR (8,0)  建檔日期
    ttAyear.aaa("crt_user", hModUser);            	//-VARCHAR (10,0)  建檔經辦
    //030	cal_seqno           	//-DECIMAL (10,0)  權益流水號
    ttAyear.aaaModxxx(hModUser, hModPgm);

    if (ttAyear.ti <=0) {
        ttAyear.ti =ppStmtCrt("tt-A-year", ttAyear.getSql());
    }

    sqlExec(ttAyear.ti, ttAyear.getParms());
    if (sqlNrow <=0) {
        sqlerr("insert cms_right_year error, kk[%s]", ooParm.hhCardNo);
        errExit(1);
    }
}
}
