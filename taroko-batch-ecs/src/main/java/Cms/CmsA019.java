package Cms;
/**取代CmsA013--
 * 2023-1214  V1.00.06  JH    不符合消費停用不送全峰(CondCard=N)
 * 2023-0828  V1.00.05  JH    rds_pcard
 * 2023-0825  V1.00.04  JH    dataFrom: cms_roadmaster
 * 2023-0717  V1.00.03  JH    ++atm_sum_flag,acct_month,year_type
 * 2023-0713  V1.00.02  JH    rd_send_date=''
 * 2023-0711  V1.00.01  JH    cms_roadlist: dupl
 * 2023-0706  V1.00.00  JH    initial
 * 2023-0620  V1.00.01  JH    redefine: call CmsRdsParm
 * */
import com.Parm2sql;

import java.text.Normalizer;

public class CmsA019 extends com.BaseBatch {
private final String PROGNAME = "道路救援每月檢核消費符合名單  2023-1214  V1.00.06";
CmsRdsParm ooParm = null;
HH hh=new HH();
//-----------
class HH {
   String rm_rowid ="";
   String rm_carno = "";
   String rm_carmanname = "";
   String rm_carnameid = "";
   String rm_status="";
   String rm_reason="";
   String rds_pcard="";
   String maj_card_no ="";
   String maj_idPseqno ="";
   String acno_Pseqno ="";
   String rd_srop_date="";
   String id_pseqno ="";
   String card_no="";
   String group_code="";
   String current_code="";
   String acct_month="";

   void initData() {
      rm_rowid ="";
      rm_carno = "";
      rm_carmanname = "";
      rm_carnameid = "";
      rm_status="";
      rm_reason="";
      rds_pcard="";
      maj_card_no ="";
      maj_idPseqno ="";
      acno_Pseqno ="";
      rd_srop_date="";
      id_pseqno ="";
      card_no="";
      group_code="";
      current_code="";
      acct_month="";
   }
}
//=*****************************************************************************
public static void main(String[] args) {
   CmsA019 proc = new CmsA019();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : CmsA019 [busi_date(08)]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length() == 8) {
         String sG_Args0 = args[0];
         hBusiDate = Normalizer.normalize(sG_Args0, Normalizer.Form.NFKD);
      }
   }

   dbConnect();
   String lsDD =commString.right(hBusiDate,2);
   if (eq(lsDD,"01")==false) {
      printf(" 道路救援每月檢核消費符合名單 每月01日執行");
      okExit(0);
   }

   ooParm = new CmsRdsParm(getDBconnect(), getDBalias(),hBusiDate);
   ooParm.setLastYear("");
   //========
   ooParm.selectCmsRoadParm2();

   deleteCms_roadList();

   selectCmsRoadMaster();

   sqlCommit();
   endProgram();
}
//------------------
void deleteCms_roadList() throws Exception {
   String ls_acctMonth =commString.left(hBusiDate,6);
   //DELETE CMS_ROADLIST WHERE acct_month=?  -- 輸入的營運日(yyyymm) OR 取自TABLE
   sqlCmd ="delete cms_roadlist"
       +" where acct_month =?"
       +" and data_from ='2'"
       ;
   ppp(1, ls_acctMonth);
   sqlExec("");
   printf("delete cms_roadlist yyyyMM[%s], rows[%s]", ls_acctMonth, sqlNrow);

   //---
   //DELETE CMS_ROADDETAIL WHERE RD_MODDATE=?    -- 同上
   //AND upper(mod_pgm) IN ('CMSA019')
   //-JH:cms_roaddetail: 可以保留不用刪除--
   sqlCmd ="delete cms_roaddetail"
       +" where rd_moddate like ?"
       +" and upper(mod_pgm) ='CMSA019'"
       +" and rd_sendyn ='M'"
       ;
   //sqlExec("");
}
//=================================
void selectCmsRoadMaster() throws Exception {
   sqlCmd = " select A.card_no, A.group_code, A.id_p_seqno " +
             ", A.major_id_p_seqno, A.major_card_no, A.acno_p_seqno " +
             ", A.current_code " +
             ", hex(B.rowid) as rm_rowid " +
             ", B.rm_status, B.rm_reason " +
             ", B.rm_carno, B.rm_carmanname, B.rm_carmanid " +
             ", B.rds_pcard, B.outstanding_yn " +
             " from cms_roadmaster B " +
             "   JOIN crd_card A ON B.card_no=A.card_no " +
             " where 1=1 " +
             " AND B.rm_type='F' " +
             " and ( B.rm_status in ('1','2','3','4') " +
             " or (B.rm_status='0' and B.rm_reason='2') ) "
    ;
   //TTT--
   //sqlCmd +=" and card_no in ('4258700002646103')";
   sqlCmd +=" order by A.card_no";

   this.openCursor();
   int ll_ok=0, ll_err=0;
   int ll_mast=0, ll_mastChg=0;
   while (fetchTable()) {
      totalCnt++;
//      dspProcRow(5000);
      processDisplay(5000);

      hh.initData();
      hh.card_no =colSs("card_no");
      hh.maj_card_no =colSs("major_card_no");
      hh.id_pseqno =colSs("id_p_seqno");
      hh.maj_idPseqno =colSs("major_id_p_seqno");
      hh.acno_Pseqno =colSs("acno_p_seqno");
      hh.group_code =colSs("group_code");
      hh.rm_rowid =colSs("rm_rowid");
      hh.rm_status =colSs("rm_status");
      hh.rm_reason =colSs("rm_reason");
      hh.rds_pcard =colSs("rds_pcard");
      hh.rm_carno =colSs("rm_carno");
      hh.rm_carmanname =colSs("rm_carmanname");
      hh.rm_carnameid =colSs("rm_carmanid");
      hh.acct_month =commString.left(hBusiDate,6);
      ooParm.hhInit();

      if (empty(hh.rm_carmanname) || empty(hh.rm_carnameid)) {
         selectCrdIdno(hh.id_pseqno);
      }
      //-卡片停用-
      hh.current_code =colSs("current_code");
      if (!eq(hh.current_code,"0") && !eq(hh.rm_status,"0")) {
         ll_err++;
         //--
         ooParm.hhRmStatus ="0";
         hh.rm_reason ="3";  //-卡片停用-
         ooParm.hhCardNo =hh.card_no;
         hh.rd_srop_date =sysDate;
         ooParm.hhIdPseqno =hh.id_pseqno;
         insertCmsRoaddetail();
         updateCmdRoadMaster();
         //-有效停用--
         //23-1214:停用不寫入cms_roadlist-
//         if (commString.ssIn(hh.rm_status,",1,2,3")) {
//            insertCmsRoadList();
//         }
         continue;
      }

      //-參數查核---
      int liRC=ooParm.checkRoadParm(hh.card_no);
      if (!empty(ooParm.hhAcctMonth)) hh.acct_month =ooParm.hhAcctMonth;
      if (empty(hh.rds_pcard)) hh.rds_pcard =ooParm.hhRdsPcard;

      //-卡片不適用-
      if (!eq(ooParm.hhCondCard,"Y")) {
         if (eq(hh.rm_status,"0")) continue;
      }

      //-已登錄---
      ll_mast++;
      if (liRC ==0) {
         ll_ok++;
         ooParm.hhRmStatus ="1";
         //-符合消費條件---
         if (isRoadOK(hh.rm_status)==false) {
            hh.rm_reason="";
            insertCmsRoaddetail();
            updateCmdRoadMaster();
         }
      }
      else {
         //-不符合消費條件---
         ll_err++;
         //-停用--
         if (isRoadOK(hh.rm_status)==false) continue;
         //-現況=啟用--
         //-JH2023-1214:不符卡片條件一律停用---
//         if (eq(ooParm.hhCondCard,"Y")) {
            ooParm.hhRmStatus ="0";
            //-有效>>暂停-
            if (isRoadOK(hh.rm_status)) {
               hh.rm_reason="2";
               hh.rd_srop_date =sysDate;
            }
            insertCmsRoaddetail();
            updateCmdRoadMaster();
            continue;
//         }
//         else {
//            //-卡片不適用-
//            ooParm.hhRmStatus =hh.rm_status;
//         }
      }
      insertCmsRoadList();
   }
   //--
   closeCursor();
   printf(" 處理筆數[%s], 合格[%s], 不合格[%s]"
           , totalCnt, ll_ok, ll_err);
}
//-------
boolean isRoadOK(String a_status) {
   return commString.ssIn(a_status,",1,2,3,");
}
//==============
int tiIdno=-1;
void selectCrdIdno(String aidPseqno) throws Exception {
   if (tiIdno <=0) {
      sqlCmd ="select id_no, chi_name"+
              " from crd_idno"+
              " where id_p_seqno =?"
              ;
      tiIdno =ppStmtCrt("ti-idno","");
   }
   ppp(1, aidPseqno);
   String cc=daoTid="idno.";
   sqlSelect(tiIdno);
   if (sqlNrow >0) {
      hh.rm_carnameid =colSs(cc+"id_no");
      hh.rm_carmanname =colSs(cc+"chi_name");
   }
}
//===================
Parm2sql ttListA=null;
int insertCmsRoadList() throws Exception {
   if (ttListA ==null) {
      ttListA =new Parm2sql();
      ttListA.insert("cms_roadlist");
   }

   String ls_modType ="N";  //N.新增--
   if (eq(ooParm.hhRmStatus,"0")) {
      ls_modType="D";  //取消
      //-2023-1214:不符合消費停用不送全峰-
      return 0;
   }

   ttListA.aaa("major_card_no", hh.maj_card_no);  //VARCHAR (19,0)	正卡卡號
   ttListA.aaa("card_no"      , ooParm.hhCardNo);  //VARCHAR (19,0)	卡號
   ttListA.aaa("major_id_p_seqno", hh.maj_idPseqno);  //VARCHAR (10,0)	正卡ID流水號
   ttListA.aaa("acno_p_seqno" , hh.acno_Pseqno);  //VARCHAR (10,0)	帳戶流水號
   ttListA.aaa("proj_no"      , ooParm.hhProjNo);  //VARCHAR (10,0)	專案代號
   ttListA.aaa("year_type"    , ""+ooParm.hhYearType);  //x(1,0)	消費年度類別--
   ttListA.aaa("rm_status"    , ooParm.hhRmStatus);  //"1");  //VARCHAR (1,0)	道路救援狀態
   ttListA.aaa("proc_flag"    , "M");  // 處理註記: M.每月名單--
   //008	proc_date           	VARCHAR (8,0)	處理日期
   ttListA.aaa("purch_amt"    , ooParm.hhPurchAmtLyy);  //DECIMAL (14,3)	上年度累積消費額
   //010	purch_row           	DECIMAL (14,3)	上年度累積消費筆數
   ttListA.aaa("tol_amt"      , ooParm.hhPurchAmt);  //DECIMAL (14,3)	消費總金額
   ttListA.aaa("curr_tot_cnt" , ooParm.hhPurchCnt);  //INTEGER (4,0)	"累積消費筆數 	"
   //012	end_card_no         	VARCHAR (19,0)	最後卡號
   ttListA.aaa("id_p_seqno"   , hh.id_pseqno);  //VARCHAR (10,0)	"卡人流水號碼 	"
   ttListA.aaa("rds_pcard"    , hh.rds_pcard);  //VARCHAR (1,0)	免費道路救援類別
   ttListA.aaa("current_code" , hh.current_code);  //VARCHAR (1,0)	狀態碼
   //018	issue_date          	VARCHAR (8,0)	核卡日期
   ttListA.aaa("rm_carmanid"  , hh.rm_carnameid);  //VARCHAR (20,0)	車主身份証號
   ttListA.aaa("rm_carno"     , hh.rm_carno);  //VARCHAR (10,0)	車號
   ttListA.aaa("mod_type"     , ls_modType);  //VARCHAR (1,0)	"異動類別    		"
   ttListA.aaa("acct_month"   , hh.acct_month);  //X(6,0)	帳務年月--
   //023	old_card_no         	VARCHAR (19,0)	舊卡卡號
   //024	purchase_date       	VARCHAR (8,0)	"消費日期			"
   ttListA.aaa("data_from"    ,"2"); //資料來源:1.Online,2.batch--
   ttListA.aaa("free_cnt"     ,1);  //免費次數
   ttListA.aaa("chi_name"     , hh.rm_carmanname);  //VARGRAPH(50,0)	"中文姓名 	    "
   ttListA.aaa("curr_max_amt" ,ooParm.hhMaxPurchAmt);  //--單筆最大金額--
   ttListA.aaa("amt_sum_flag" , ooParm.hhAmtSumFlag);  //X(1,0)	消費金額累積方式--
   ttListA.aaa("send_date"    , "");  //VARCHAR (8,0)	名單寄送日期
   ttListA.aaa("crt_user"     , hModUser);  //VARCHAR (10,0)	建置使用者
   ttListA.aaa("crt_date"     , sysDate);  //ARCHAR (8,0)	建置日期
   ttListA.aaa("give_flag"    , "N");  //VARCHAR (1,0)	贈送註記
   ttListA.aaa("group_code"   , hh.group_code);  //VARCHAR (4,0)	團體代號
   ttListA.aaa("mod_user"     , hModUser);  //VARCHAR (10,0)	異動使用者
   ttListA.aaaDtime("mod_time");  //TIMESTMP(10,6)	異動時間
   ttListA.aaa("mod_pgm"      , hModPgm);  //VARCHAR (20,0)	異動程式

   if (ttListA.ti <=0) {
      ttListA.ti =ppStmtCrt("tt-list-A",ttListA.getSql());
   }

   sqlExec(ttListA.ti, ttListA.getParms());
   if (sqlNrow >0) return 0;
   if (sqlDuplRecord) {
      printf("cms_roadList exist, kk[%s,%s,%s]", ooParm.hhCardNo,ls_modType,sysDate);
      return 1;
   }
   if (sqlNrow <=0) {
      sqlerr("insert cms_roadlist error, kk[%s,%s,%s]", ooParm.hhCardNo,ls_modType,sysDate);
      errExit(1);
   }
   return 0;
}
//-----------
Parm2sql ttRdsDA=null;
void insertCmsRoaddetail() throws Exception {
   if (ttRdsDA ==null) {
      ttRdsDA =new Parm2sql();
      ttRdsDA.insert("cms_roaddetail");
   }

   String ls_sendSts="Y";  //新增--
   if (eq(ooParm.hhRmStatus,"0")) ls_sendSts="D";  //移除--

   int liSeqNo=ooParm.getrdSeqNo(sysDate);

   ttRdsDA.aaaYmd("rd_moddate");  //異動日期
   ttRdsDA.aaa("rd_seqno", liSeqNo);  //登錄序號
   ttRdsDA.aaa("rd_modtype", "B");  //異動來源         O:online, B:batch
   ttRdsDA.aaa("card_no", hh.card_no);  //正卡卡號
   //004	new_card_no	VARCHAR (19,0)	新卡號
   ttRdsDA.aaa("rd_type", "F");	//救援類別         F:免費, E:自費
   ttRdsDA.aaa("appl_card_no", ooParm.hhCardNo);  //申請卡號
   ttRdsDA.aaa("group_code", hh.group_code);  //團體代號
   ttRdsDA.aaa("rd_carno", hh.rm_carno);  //車號
   ttRdsDA.aaa("rd_carmanname", hh.rm_carmanname);  //車主姓名
   ttRdsDA.aaa("rd_carmanid", hh.rm_carnameid);  //車主身份証號
   ttRdsDA.aaa("rd_status", ooParm.hhRmStatus);  // 異動狀態 1:啟用, 2:變更車號, 0:停用
   ttRdsDA.aaa("rd_stopdate", hh.rd_srop_date);  // 停用日期
   ttRdsDA.aaa("rd_stoprsn", hh.rm_reason);  //停用原因
   ttRdsDA.aaa("crt_user", hModUser);  //登錄者
   ttRdsDA.aaa("crt_date", sysDate);  //登錄日期
   ttRdsDA.aaa("apr_user", hModUser);  //覆核主管
   ttRdsDA.aaaYmd("apr_date");  //覆核日期
   ttRdsDA.aaa("rd_senddate", sysDate);  //由CmsA016每月全部送--
   ttRdsDA.aaa("rd_sendsts", ls_sendSts);  //緊急登錄	 VARCHAR (10,0)	RD_SENDSTS
   ttRdsDA.aaa("rd_sendyn", "M");  //VARCHAR (1,0), 月批次送--
   //033	rd_sendadd	INTEGER (4,0)	RD_SENDADD
   //034	rd_sendstop	INTEGER (4,0)	RD_SENDSTOP
   ttRdsDA.aaa("proj_no", ooParm.hhProjNo);  //	VARCHAR (10,0)	專案代號
   ttRdsDA.aaa("purch_amt", ooParm.hhPurchAmt);  //	DECIMAL (11,0)	累積消費金額
   ttRdsDA.aaa("purch_cnt", ooParm.hhPurchCnt);  //INTEGER (4,0)	累積消費次數
   ttRdsDA.aaa("purch_amt_lyy", ooParm.hhPurchAmtLyy);  //DECIMAL (11,0)	上年累計消費金額
   //039	cardholder_type	VARCHAR (1,0)	卡人類別
   ttRdsDA.aaa("rds_pcard", hh.rds_pcard);  //免費道路救援類別
   ttRdsDA.aaa("id_p_seqno", hh.id_pseqno);  //ID編號
   ttRdsDA.aaa("outstanding_yn", ooParm.hhOutstandingYn);  //	VARCHAR (1,0)	未繳最低應繳金額Y/N
   //047	outstanding_cond	VARCHAR (1,0)	未繳最低應繳金額不適用(參數)Y/N
   ttRdsDA.aaa("give_flag", "N");  //贈送註記
   ttRdsDA.aaaModxxx(hModUser, hModPgm);

   if (ttRdsDA.ti <=0) {
      ttRdsDA.ti =ppStmtCrt("tt-rdsd-A",ttRdsDA.getSql());
   }

//	debug=true;
//	dddSql(ttRdsDA.ti, ttRdsDA.getConvParm(false));
//	debug=false;

   sqlExec(ttRdsDA.ti, ttRdsDA.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert Cms_roaddetail error");
      errExit(1);
   }
}
//--------
Parm2sql ttRdsMU=null;
void updateCmdRoadMaster() throws Exception {
   if (ttRdsMU ==null) {
      ttRdsMU =new Parm2sql();
      ttRdsMU.update("cms_roadMaster");
   }

   ttRdsMU.aaa("rm_status", ooParm.hhRmStatus);
   ttRdsMU.aaa("rm_reason", hh.rm_reason);
   ttRdsMU.aaa("rm_moddate",sysDate);
   ttRdsMU.aaa("outstanding_yn",ooParm.hhOutstandingYn);
   ttRdsMU.aaa("give_flag", "N");
   ttRdsMU.aaaModxxx(hModUser, hModPgm);
   ttRdsMU.aaaWhere("where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)", hh.rm_rowid);

   if (ttRdsMU.ti <=0) {
      ttRdsMU.ti =ppStmtCrt("tt-rdsM-U",ttRdsMU.getSql());
   }

   sqlExec(ttRdsMU.ti, ttRdsMU.getParms());
   if (sqlNrow <=0) {
      sqlerr("update cms_roadMaster error");
      errExit(1);
   }
}

}