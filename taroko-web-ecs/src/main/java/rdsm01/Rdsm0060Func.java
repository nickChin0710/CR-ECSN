package rdsm01;

import busi.SqlPrepare;
import taroko.base.Parm2Sql;

public class Rdsm0060Func extends busi.FuncAction {
   String hh_seqNo="0";
   String hh_idNo="";
String hh_idNo7="";
   String hh_idPseqno="";
   String hh_majidPseqno="";
   String hh_acctType="";
   String hh_acnoPseqno="";
   String hh_majCardNo="";
   String hh_groupCode="";
   String hh_stmtCycle="";
   String hh_pCard="";
   String hh_cardNo="";
String hh_cardNo3="";
   String hh_carNo="";
   String hh_carNo6="";
   String hh_carChiName="";
   String hh_purchDate="";
   String hh_purchDate2="";
   String hh_carManId="";
   String hh_procFlag="";
   String hh_pCardCode="";
   String hh_pCardName="";

taroko.base.Parm2Sql tt=new Parm2Sql();
//===============================================

void initData() {
   hh_seqNo="0";
   hh_idNo="";
   hh_idNo7="";
   hh_idPseqno="";
   hh_majidPseqno="";
   hh_acctType="";
   hh_acnoPseqno="";
   hh_majCardNo="";
   hh_groupCode="";
   hh_stmtCycle="";
   hh_pCard="";
   hh_cardNo="";
   hh_cardNo3="";
   hh_carNo="";
   hh_carNo6="";
   hh_carChiName="";
   hh_purchDate="";
   hh_purchDate2="";
   hh_carManId="";
   hh_procFlag="Y";
   hh_pCardCode="";
   hh_pCardName="";
}

@Override
public void dataCheck() {

   selectCrdIdno();
   if (rc !=1) return;
   selectCmsRoadMaster();
   if (rc !=1) return;
   selectCrdCard();
   if (rc !=1) return;

}

void selectCrdCard() {
   if (empty(hh_cardNo)) return;

   String sql1="select A.current_code, A.group_code"+
           ", A.major_card_no, A.major_id_p_seqno"+
           ", B.acno_p_seqno, B.stmt_cycle, B.acct_type"+
           " from crd_card A left join act_acno B on A.acno_p_seqno=B.acno_p_seqno"+
           " where A.card_no =?"
           ;
   setString(1, hh_cardNo);
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      errmsg("卡號不存在");
      return;
   }

   hh_acctType =colStr("acct_type");
   hh_acnoPseqno =colStr("acno_p_seqno");
   hh_stmtCycle =colStr("stmt_cycle");
   hh_groupCode =colStr("group_code");
   hh_majCardNo =colStr("major_card_no");
   hh_majidPseqno =colStr("major_id_p_seqno");

   String ss=colStr("current_code");
   if (!eq(ss,"0")) {
      errmsg("卡號不是有效卡");
   }
}

void selectCmsRoadMaster() {
   String sql1="select count(*) as rm_cnt"+
           " from cms_roadmaster"+
           " where rm_carmanid =? and id_p_seqno =?"
           ;
   setString(1, hh_idNo);
   setString(2, hh_idPseqno);
   sqlSelect(sql1);
   if (sqlRowNum <=0 || colNum("rm_cnt") <=0) {
      errmsg("ID錯誤!!不存在道路救援");
      return;
   }

   //String[] cardVal = {"0停用", "1新增車號", "2變更車號", "3取消車號", "4未啟用"};
   sql1 ="select card_no"+
           ", rm_carno, group_code, rm_carmanname"+
           ", rm_carmanid, rm_oldcarno, rm_status, rm_validdate"+
           ", rds_pcard, id_p_seqno"+
           " from cms_roadmaster"+
           " where substr(rm_carno,1,6) =? and rm_carno<>'' "+
           " and rm_carmanid =?"+
           " order by decode(rm_status,'1',1,'2',2,'3',3,'4',4,'0',9,8)"
           ;
   setString(1, hh_carNo6);
   setString(2, hh_idNo);
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      errmsg("車號不存在道路救援");
      return;
   }
   int ll=0;
   hh_carNo =colStr("rm_carno");
   hh_cardNo =colStr(ll,"card_no");
   hh_carManId =colStr(ll,"rm_carmanid");

   //019	rm_carmanid	VARCHAR (20,0)	車主身份証號
   //020	rm_carno	VARCHAR (10,0)	車號
   sql1 ="select count(*) as list_idcnt"+
           " from cms_roadlist"+
           " where rm_carmanid =?"
           ;
   setString(1, hh_carManId);
   sqlSelect(sql1);
   if (sqlRowNum <=0 || colNum("list_idcnt")<=0) {
      errmsg("ID錯誤!!不存在優惠名單");
      return;
   }

   sql1 ="select count(*) as list_carcnt"+
           " from cms_roadlist"+
           " where 1=1"+
           " and rm_carno =?"
   ;
   setString(1, hh_carNo);
   sqlSelect(sql1);
   if (sqlRowNum <=0 || colNum("list_carcnt")<=0) {
      errmsg("車號錯誤 不存在優惠名單");
      return;
   }
}
void selectCrdIdno() {
   if (empty(hh_idNo7)) {
      errmsg("ID錯誤!!不存在卡人檔");
      return;
   }
   //3.1.	檢核txt ID  (前3後4) 是否存在【CRD_IDNO】ID_NO
   String sql1 ="select id_no, id_p_seqno "
           +" from crd_idno"
           +" where id_no like ?"
           +commSqlStr.rownum(1)
           ;
   String ls_idNo=commString.left(hh_idNo7,3)+"%"+commString.right(hh_idNo7,4);
   setString(1, ls_idNo);
   sqlSelect(sql1);
   if (sqlRowNum <=0) {
      errmsg("ID錯誤!!不存在卡人檔");
      return;
   }

   hh_idNo =colStr("id_no");
   hh_idPseqno =colStr("id_p_seqno");
}

void dataParser() {
   String ss="";
   String[] tt = new String[2];

   tt[0] = wp.itemStr("tx_data");
   //序號:(5碼)
   hh_seqNo=commString.bbToken(tt,5);
   //1.1.	合庫代號: (19碼) (338+8個中文字)
   commString.bbToken(tt,3);
   commString.bbToken(tt,16);
   //1.2.	優惠別:(3碼代號+16碼中文). à3個檔優惠別分別是001,003,004 ,對應現有優惠別P,I,V
   //P=001 ,I=003 ,V=004
   hh_pCardCode =commString.bbToken(tt,3).trim();
   hh_pCardName =commString.bbToken(tt,16).trim();
   if (eqIgno(hh_pCardCode,"001")) hh_pCard="P";
   else if (eqIgno(hh_pCardCode,"003")) hh_pCard="I";
   else if (eqIgno(hh_pCardCode,"004")) hh_pCard="V";
   //1.3.	Id: (前3後4,共7碼)
   hh_idNo7=commString.bbToken(tt,7);
   //1.4.	卡號:(第8-10碼,共3碼),
   hh_cardNo3 =commString.bbToken(tt,3);
   //1.5.	登錄日期:(10碼:MM/DD/YYYY)
   ss=commString.bbToken(tt,10);
   hh_purchDate =commString.mid(ss,6)
           +commString.mid(ss,0,2)+commString.mid(ss,3,2);
   //1.6.	年度到期日:(8碼:MM/DD/YYYY)
   ss=commString.bbToken(tt,10);
   hh_purchDate2 =commString.mid(ss,6)
           +commString.mid(ss,0,2)+commString.mid(ss,3,2);
   //1.7.	車號:(取前6碼):
   hh_carNo6=commString.bbToken(tt,6);
   //1.8.	名字:(12碼),6個中文字
   hh_carChiName =commString.bbToken(tt,12);

}

@Override
public int dbInsert() {
   msgOK();
   initData();
   dataParser();
   dataCheck();
   String ls_procMsg =getMsg();
   if (rc ==-1) hh_procFlag="1";

   tt.insert("bil_mcht_apply_tmp");
   tt.parmYmd("crt_date");
   tt.parmTime("crt_time");
   tt.parmSet("file_name", wp.itemStr("zz_file_name"));  //VARCHAR (50,0)	檔案名稱
   tt.parmSet("data_seqno", hh_seqNo);  //	VARCHAR (10,0)	資料序號
   tt.parmSet("file_type", "05");  //	VARCHAR (2,0)	檔案類別
   tt.parmSet("data_seq1", commString.strToInt(hh_seqNo));  //	INTEGER (4,0)	資料序號1
   tt.parmSet("id_p_seqno", hh_idPseqno);  //	VARCHAR (10,0)	帳戶流水號碼
   tt.parmSet("major_id_p_seqno", hh_majidPseqno);  //	VARCHAR (10,0)	帳戶流水號碼
   tt.parmSet("vd_flag", "N");  //	VARCHAR (1,0)	VD旗標
   tt.parmSet("acct_type", hh_acctType);  //	VARCHAR (2,0)	帳戶帳號類別碼
   tt.parmSet("acno_p_seqno", hh_acnoPseqno);  //	VARCHAR (10,0)	帳戶流水號
   tt.parmSet("major_card_no", hh_majCardNo);  //	VARCHAR (19,0)	正卡卡號
   tt.parmSet("group_code", hh_groupCode);  //	VARCHAR (4,0)	團體代號
   tt.parmSet("stmt_cycle", hh_stmtCycle);  //	VARCHAR (2,0)	關帳期
   tt.parmSet("mcht_no", "");  //	VARCHAR (20,0)	特店代號
   tt.parmSet("product_no", hh_pCard);  //	VARGRAPH(8,0)	產品代號
   tt.parmSet("card_no", hh_cardNo);  //	VARCHAR (19,0)	卡號
   tt.parmSet("car_no", hh_carNo);  //	VARCHAR (8,0)	車號
   tt.parmSet("chi_name", hh_carChiName);  //	VARGRAPH(50,0)	中文姓名
   tt.parmSet("purchase_date", hh_purchDate);  //	VARCHAR (8,0)	消費日期
   tt.parmSet("purchase_date_e", hh_purchDate2);  //VARCHAR (8,0) 消費日期_迄
   //tt.addsqlParm("purchase_time	VARCHAR (8,0)	消費時間
   tt.parmSet("project_no", "道路救援");  //	VARGRAPH(60,0)	活動方案
   tt.parmSet("service_no", hh_cardNo3+hh_idNo7+hh_carNo6);
   tt.parmSet("service_code", hh_pCardCode);  //優惠別:(3碼代號+16碼中文).
   tt.parmSet("service_name", hh_pCardName);  //
   tt.parmSet("proc_date", sysDate);  //	VARCHAR (8,0)	處理日期
   tt.parmSet("proc_flag", hh_procFlag);  //	VARCHAR (1,0)	處理註記      1:成功 2:退件 3:分期 N:未處理
   tt.parmSet("free_proc_result", ls_procMsg);  //	VARGRAPH(50,0)	檢核結果
   tt.parmSet("id_no", hh_idNo);  //	VARCHAR (20,0)	身分證號碼
   tt.modxxxSet(modUser, modPgm);

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum !=1) {
      sqlErr("insert bil_mcht_apply_tmp error");
      wp.log(getMsg());
   }
   return rc;
}

@Override
public int dbUpdate() {
   return 0;
}

@Override
public int dbDelete() {
   return 0;
}

@Override
public int dataProc() {
   return 0;
}
}
