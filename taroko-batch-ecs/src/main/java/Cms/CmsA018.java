package Cms;
/**
 * 2023-1116  V1.00.04  Kirin showmess call CmsRdsParm     *    
 * 2023-0713  V1.00.03  JH    rm_reason=''
 * 2023-0705  V1.00.02  JH    rd_senddate=sysdate
 * 2023-0620  V1.00.01  JH    redefine: call CmsRdsParm
 * */
import com.Parm2sql;

import java.text.Normalizer;

public class CmsA018 extends com.BaseBatch {
private final String PROGNAME = "道路救援每日檢核消費條件  2023-1116  V1.00.04";
CmsRdsParm ooParm = null;
//-HH-------------------
String hhRmCarno = "";
String hhRmCarmanname = "";
String hhRmCarmanid = "";
//-------------

//=*****************************************************************************
public static void main(String[] args) {
   CmsA018 proc = new CmsA018();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : CmsA018 [busi_date(08), callbatch_seqno]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length() == 8) {
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
   ooParm = new CmsRdsParm(getDBconnect(), getDBalias());
   ooParm.hBusiDate = hBusiDate;
   ooParm.setLastYear("");
   showLogMessage("I","", "CmsA018 call CmsRdsParm ["+ hBusiDate +"] initialization success.....");
   
   //========
   ooParm.selectCmsRoadParm2();

   selectCmsRoadMaster();

   sqlCommit();
   endProgram();
}

//=================================
void selectCmsRoadMaster() throws Exception {
   sqlCmd = " select hex(rowid) as rowid"
           + ", card_no, rm_status, rm_carno" +
           ", group_code, rm_carmanname, rm_carmanid" +
           ", id_p_seqno, rds_pcard, outstanding_yn"
           + " from cms_roadMaster"
           + " where rm_type='F'"
           + " and rm_status in ('4') "  //未啟用--
           + " order by crt_date, card_no"
   ;

   this.openCursor();
   while (fetchTable()) {
      totalCnt++;
      ooParm.hhInit();
      ooParm.hhRowId = colSs("rowid");
      ooParm.hhCardNo = colSs("card_no");
      ooParm.hhGroupCode = colSs("group_code");
      ooParm.hhRmStatus = colSs("rm_status");
      ooParm.hhIdPseqno = colSs("id_p_seqno");
      ooParm.hhRdsPcard= colSs("rds_pcard");
      ooParm.hhOutstandingYn = colSs("outstanding_yn");
      hhRmCarno = colSs("rm_carno").toUpperCase();
      hhRmCarmanname = colSs("rm_carmanname");
      hhRmCarmanid = colSs("rm_carmanid");

      int liRC = ooParm.checkRoadParm(ooParm.hhCardNo);
      //-不符合-
      if (liRC != 0) {
         continue;
      }
      //-is OK-
      ooParm.hhRmStatus = "1";  //符合

      insertCmsRoaddetail();
      updateCmdRoadMaster();
   }
   closeCursor();
}
//===================
com.Parm2sql ttRdsDA=null;
void insertCmsRoaddetail() throws Exception {
   if (ttRdsDA ==null) {
      ttRdsDA =new com.Parm2sql();
      ttRdsDA.insert("cms_roaddetail");
   }

   int liSeqNo=ooParm.getrdSeqNo(sysDate);

   ttRdsDA.aaaYmd("rd_moddate");  //異動日期
   ttRdsDA.aaa("rd_seqno", liSeqNo);  //登錄序號
   ttRdsDA.aaa("rd_modtype", "B");  //異動來源         O:online, B:batch
   ttRdsDA.aaa("card_no", ooParm.hhCardNo);  //正卡卡號
   //004	new_card_no	VARCHAR (19,0)	新卡號
   ttRdsDA.aaa("rd_type", "F");	//救援類別         F:免費, E:自費
   ttRdsDA.aaa("appl_card_no", ooParm.hhCardNo);  //申請卡號
   ttRdsDA.aaa("group_code", ooParm.hhGroupCode);  //團體代號
   ttRdsDA.aaa("rd_carno", hhRmCarno);  //車號
   ttRdsDA.aaa("rd_carmanname", hhRmCarmanname);  //車主姓名
   ttRdsDA.aaa("rd_carmanid", hhRmCarmanid);  //車主身份証號
   //011	rd_newcarno	VARCHAR (10,0)	新車號
   //012	rd_htelno1	VARCHAR (5,0)	自宅電話1
   //013	rd_htelno2	VARCHAR (15,0)	自宅電話2
   //014	rd_htelno3	VARCHAR (6,0)	自宅電話3
   //015	rd_otelno1	VARCHAR (5,0)	公司電話1
   //016	rd_otelno2	VARCHAR (15,0)	公司電話2
   //017	rd_otelno3	VARCHAR (6,0)	公司電話3
   //018	cellar_phone	VARCHAR (30,0)	手機
   //019	rd_validdate	VARCHAR (8,0)	有效期限         YYMM
   ttRdsDA.aaa("rd_status", ooParm.hhRmStatus);  //	VARCHAR (1,0)	異動狀態 1:啟用, 2:變更車號, 0:停用
   //021	rd_payamt	DECIMAL (13,2)	自費金額
   //022	rd_payno	VARCHAR (12,0)	請款批號
   //023	rd_paydate	VARCHAR (8,0)	請款日期
   //024	rd_stopdate	VARCHAR (8,0)	停用日期
   //025	rd_stoprsn	VARCHAR (10,0)	停用原因
   ttRdsDA.aaa("crt_user", hModPgm);  //登錄者
   ttRdsDA.aaa("crt_date", sysDate);  //登錄日期
   ttRdsDA.aaa("apr_user", hModUser);  //覆核主管
   ttRdsDA.aaaYmd("apr_date");  //覆核日期
   ttRdsDA.aaa("rd_senddate", "");  //由CmsA016每月全部送--
   ttRdsDA.aaa("rd_sendsts", "");  //緊急登錄	 VARCHAR (10,0)	RD_SENDSTS
   ttRdsDA.aaa("rd_sendyn", "");  //	VARCHAR (1,0)	RD_SENDYN
   //033	rd_sendadd	INTEGER (4,0)	RD_SENDADD
   //034	rd_sendstop	INTEGER (4,0)	RD_SENDSTOP
   ttRdsDA.aaa("proj_no", ooParm.hhProjNo);  //	VARCHAR (10,0)	專案代號
   ttRdsDA.aaa("purch_amt", ooParm.hhPurchAmt);  //	DECIMAL (11,0)	累積消費金額
   ttRdsDA.aaa("purch_cnt", ooParm.hhPurchCnt);  //INTEGER (4,0)	累積消費次數
   ttRdsDA.aaa("purch_amt_lyy", ooParm.hhPurchAmtLyy);  //DECIMAL (11,0)	上年累計消費金額
   //039	cardholder_type	VARCHAR (1,0)	卡人類別
   ttRdsDA.aaa("rds_pcard", ooParm.hhRdsPcard);  //免費道路救援類別
   ttRdsDA.aaa("id_p_seqno", ooParm.hhIdPseqno);  //ID編號
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
com.Parm2sql ttRdsMU=null;
void updateCmdRoadMaster() throws Exception {
   if (ttRdsMU ==null) {
      ttRdsMU =new Parm2sql();
      ttRdsMU.update("cms_roadMaster");
   }

   ttRdsMU.aaa("rm_status", ooParm.hhRmStatus);
   ttRdsMU.aaa("rm_reason", "");
   ttRdsMU.aaa("rm_moddate",sysDate);
   ttRdsMU.aaa("outstanding_yn",ooParm.hhOutstandingYn);
   ttRdsMU.aaa("give_flag", "N");
   ttRdsMU.aaaModxxx(hModUser, hModPgm);
   ttRdsMU.aaaWhere("where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)", ooParm.hhRowId);

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