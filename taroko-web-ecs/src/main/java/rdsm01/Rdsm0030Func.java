package rdsm01;

import taroko.base.Parm2Sql;

public class Rdsm0030Func extends busi.FuncAction {
RdsFunc ioFunc =null;

//==================
@Override
public void dataCheck() {
   if (ibAdd) {
      dataCheckAdd();
   }
   else if (ibUpdate) {
      dataCheckUpdate();
   }
}
void dataCheckUpdate() {
   if (wp.itemEq("give_flag","Y") ==false) {
      errmsg("不是贈送狀態, 請用rdsm0020維護");
      return;
   }
   if (wp.itemEmpty("rd_carno") && wp.itemEmpty("rd_newcarno")) {
      errmsg("車號及變更車號: 不可空白");
      return;
   }
   // get mod_seqno
   Double modSeqno = getModSeqno();
   if (modSeqno == null) {
      return;
   }

   if (modSeqno.doubleValue() != wp.itemNum("rm_mod_seqno")) {
      errmsg("道路救援主檔 已被修改; 請重新讀取資料");
      return;
   }

   if (wp.colEq("rd_type", "E")) {
      if (wp.itemEmpty("rd_validdate")) {
         errmsg("有效日期 不可空白");
         return;
      }
   } else {
      if (wp.itemNum("rd_payamt") != 0) {
         errmsg("免費救援, 不可輸入[自費金額]");
         return;
      }
   }

   // --停用
   if (wp.itemEq("rd_status", "0")) {
      if (wp.itemEmpty("rd_stopdate") || wp.itemEmpty("rd_stoprsn")) {
         errmsg("請輸入 停用日期及原因");
         return;
      }

      if (wp.itemEmpty("rd_newcarno") == false) {
         errmsg("停用不可變更車號");
         return;
      }
   } else {

      // --非停用
      if (wp.itemEmpty("rd_stopdate") == false || wp.itemEmpty("rd_stoprsn") == false) {
         errmsg("不是停用,停用日期及原因需為空白");
         return;
      }
   }

   // --服務類別
//   if (wp.colEq("rd_type", "F")) {
//      if (ibAuto) {
//         if (wfChkRoadfeePv40() == -1)
//            return;
//      } else {
//         if (wfChkRoadfree40() == -1)
//            return;
//      }
//   } else if (wp.colEq("rd_type", "E")) {
//      if (wfChkRoadexpend40() == -1)
//         return;
//   }
}
void dataCheckAdd() {
   if (checkCardNo() == false) {
      return;
   }
   if (checkIdNo() == false) {
      return;
   }

   if (selectRoadMaster()==false) return;
//      if (eqIgno(wp.itemStr("rd_type"), "F")) {
//         if (wfChkRoadfree() == -1)
//            return;
//      } else if (eqIgno(wp.itemStr("rd_type"), "E")) {
//         if (wfChkRoadexpend() == -1)
//            return;
//      }
   String lsRdsPcard =wp.itemStr("rds_pcard");
   //--
   if (empty(lsRdsPcard)) {
      lsRdsPcard =ioFunc.getRdsPcard(wp.itemStr("appl_card_no"));
      wp.itemSet("rds_pcard", lsRdsPcard);
   }
   if (!commString.strIn(lsRdsPcard,",I,V,P")) {
      errmsg("優惠別: 不是I,V,P");
      return;
   }
}

Double getModSeqno() {
   String sql1 = " select mod_seqno from cms_roadmaster "
           +"where 1=1"+commSqlStr.whereRowid;
   setParm(1,wp.itemStr2("rm_rowid"));
   sqlSelect(sql1);
   if (sqlRowNum <= 0) {
      errmsg("道路救援主檔 已不存在; 請重新讀取資料");
      return null;
   }
   return colNum("mod_seqno");
}

boolean selectRoadMaster() {
   String lsCardNo=wp.itemStr("appl_card_no");
   String lsIdNo=wp.itemStr("rm_carmanid");

   String sql1 = " select  count(*) as db_cnt  from cms_roadmaster "
           + " where card_no = ?  and rm_carmanid = ? ";
   sqlSelect(sql1, new Object[] {lsCardNo, lsIdNo});
   if (colNum("db_cnt") > 0) {
      errmsg("此資料已存在, 請由救援主檔中修改");
      return false;
   }

   String lsIdPseqno=wp.itemStr("id_p_seqno");
   sql1 = " select  count(*) as db_cnt  from cms_roadmaster where id_p_seqno = ? ";
   sqlSelect(sql1, lsIdPseqno);
   if (colNum("db_cnt") > 0) {
      errmsg("持卡人己作過免費道路救援");
      return false;
   }

   return true;
}

boolean checkCardNo() {
   String lsCardNo =wp.itemStr("appl_card_no");

   if (empty(lsCardNo)) {
      errmsg("申請卡號 : 不可空白 ");
      return false;
   }

   String sql1 = " select  current_code, id_p_seqno  from crd_card  where card_no = ? ";
   sqlSelect(sql1, lsCardNo);
   if (sqlRowNum <= 0) {
      errmsg("卡號不存在");
      return false;
   }
   if (!eqIgno(colStr("current_code"), "0")) {
      errmsg("此卡為無效卡, 不可登錄");
      return false;
   }

   wp.itemSet("id_p_seqno", colStr("id_p_seqno"));

   return true;
}

boolean checkIdNo() {
   String lsIdno =wp.itemStr("rd_carmanid");
   if (empty(lsIdno)) {
      errmsg("身分證號 : 不可空白 ");
      return false;
   }
   String sql1 = " select  id_p_seqno  from crd_idno where id_no = ? ";
   sqlSelect(sql1, lsIdno);

   if (sqlRowNum <= 0) {
      errmsg("身分證號不存在");
      return false;
   }

   return true;
}

@Override
public int dbInsert() {
   actionInit("A");
   ioFunc =new RdsFunc();
   ioFunc.setConn(wp);

   dataCheck();
   if (rc != 1)
      return rc;

   insertDetail();
   if (rc != 1)
      return rc;

   insertMaster();
   if (rc != 1)
      return rc;

   Double rdPayamt = wp.itemNum("rd_payamt");
   if (rdPayamt > 0) {
      updateBilSysexp();
   }

   return rc;
}

taroko.base.Parm2Sql tt=new Parm2Sql();
int insertDetail() {
   int li_seqNo =ioFunc.autonoDtl(sysDate);

   String lsModtype="O";
   String lsCrtUser=modUser;
   String lsCrtDate=sysDate;
   if (isAdd()) {
      lsModtype="OA";
   }
   else if (isUpdate()) {
      lsModtype="OU";
      lsCrtDate =wp.itemStr("crt_date");
      lsCrtUser =wp.itemStr("crt_user");
   }
   else if (isDelete()) {
      lsModtype="OD";
      lsCrtDate =wp.itemStr("crt_date");
      lsCrtUser =wp.itemStr("crt_user");
   }

   tt.insert("cms_roaddetail");
   tt.parmSet("rd_moddate", sysDate);
   tt.parmSet("rd_seqno", li_seqNo);
   tt.parmSet("rd_modtype", lsModtype);
   tt.parmSet("card_no", wp.itemStr("appl_card_no"));
   tt.parmSet("new_card_no", wp.itemStr("appl_card_no"));
   tt.parmSet("rd_type", wp.itemStr("rd_type"));
   tt.parmSet("appl_card_no", wp.itemStr("appl_card_no"));
   tt.parmSet("group_code", wp.itemStr("group_code"));
   tt.parmSet("rd_carno", wp.itemStr("rd_carno"));
   tt.parmSet("rd_carmanname", wp.itemStr("rd_carmanname"));
   tt.parmSet("rd_carmanid", wp.itemStr("rd_carmanid"));
   tt.parmSet("rd_newcarno", wp.itemStr("rd_newcarno"));
   tt.parmSet("rd_htelno1", wp.itemStr("rd_htelno1"));
   tt.parmSet("rd_htelno2", wp.itemStr("rd_htelno2"));
   tt.parmSet("rd_htelno3", wp.itemStr("rd_htelno3"));
   tt.parmSet("rd_otelno1", wp.itemStr("rd_otelno1"));
   tt.parmSet("rd_otelno2", wp.itemStr("rd_otelno2"));
   tt.parmSet("rd_otelno3", wp.itemStr("rd_otelno3"));
   tt.parmSet("cellar_phone", wp.itemStr("cellar_phone"));
   tt.parmSet("rd_validdate", wp.itemStr("rd_validdate"));
   tt.parmSet("rd_status", wp.itemStr("rd_status"));
   tt.parmSet("rd_payamt", wp.itemNum("rd_payamt"));
   tt.parmSet("rd_payno", wp.itemStr("rd_payno"));
   tt.parmSet("rd_paydate", wp.itemStr("rd_paydate"));
   tt.parmSet("rd_stopdate", wp.itemStr("rd_stopdate"));
   tt.parmSet("rd_stoprsn", wp.itemStr("rd_stoprsn"));
   tt.parmSet("crt_user", lsCrtUser);
   tt.parmSet("crt_date", lsCrtDate);
   tt.parmSet("proj_no", wp.itemStr("proj_no"));
   tt.parmSet("purch_amt", wp.itemNum("purch_amt"));
   tt.parmSet("purch_cnt", wp.itemNum("purch_cnt"));
   tt.parmSet("purch_amt_lyy", wp.itemNum("purch_amt_lyy"));
   tt.parmSet("cardholder_type", wp.itemStr("cardholder_type"));
   tt.parmSet("rds_pcard", wp.itemStr("rds_pcard"));
   tt.parmSet("give_flag", wp.itemStr("give_flag"));
   tt.parmSet("apr_user", modUser);
   tt.parmSet("apr_date", sysDate);
   tt.parmSet("outstanding_yn", wp.itemStr("outstanding_yn"));
   tt.parmSet("outstanding_cond", wp.itemStr("outstanding_cond"));
   tt.parmSet("id_p_seqno", wp.itemStr("id_p_seqno"));
   tt.modxxxSet(modUser,modPgm);

   sqlExec(tt.getSql(),tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert cms_roaddetail error ! ");
   }
   return rc;
}

int insertMaster() {

   String carno = "", oldcarno = "";

   if (!empty(wp.itemStr("rd_newcarno"))) {
      oldcarno = wp.itemStr("rd_carno");
      carno = wp.itemStr("rd_newcarno");
   } else {
      carno = wp.itemStr("rd_carno");
   }

   tt.insert("cms_roadmaster");
   tt.parmSet("card_no", wp.itemStr("appl_card_no"));
   tt.parmSet("rm_type", wp.itemStr("rd_type"));
   tt.parmSet("group_code", wp.itemStr("group_code"));
   tt.parmSet("rm_carmanname", wp.itemStr("rd_carmanname"));
   tt.parmSet("rm_carmanid", wp.itemStr("rd_carmanid"));
   tt.parmSet("rm_htelno1", wp.itemStr("rd_htelno1"));
   tt.parmSet("rm_htelno2", wp.itemStr("rd_htelno2"));
   tt.parmSet("rm_htelno3", wp.itemStr("rd_htelno3"));
   tt.parmSet("rm_otelno1", wp.itemStr("rd_otelno1"));
   tt.parmSet("rm_otelno2", wp.itemStr("rd_otelno2"));
   tt.parmSet("rm_otelno3", wp.itemStr("rd_otelno3"));
   tt.parmSet("cellar_phone", wp.itemStr("cellar_phone"));
   tt.parmSet("rm_status", wp.itemStr("rd_status"));
   tt.parmSet("rm_oldcarno", oldcarno);
   tt.parmSet("rm_carno", carno);
   tt.parmSet("rm_moddate", sysDate);
   tt.parmSet("rm_validdate", wp.itemStr("rd_validdate"));
   tt.parmSet("rm_reason", wp.itemStr("rd_stoprsn"));
   tt.parmSet("rm_payamt", wp.itemNum("rd_payamt"));
   tt.parmSet("rm_paydate", wp.itemStr("rd_paydate"));
   tt.parmSet("rds_pcard", wp.itemStr("rds_pcard"));
   tt.parmSet("give_flag", wp.itemStr("give_flag"));
   tt.parmSet("crt_user", modUser);
   tt.parmSet("crt_date", sysDate);
   tt.parmSet("apr_user", modUser);
   tt.parmSet("apr_date", sysDate);
   tt.parmSet("id_p_seqno", wp.itemStr("id_p_seqno"));
   tt.parmSet("outstanding_yn", wp.itemStr("outstanding_yn"));
   tt.modxxxSet(modUser,modPgm);

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("insert cms_roadmaster error ! ");
   }
   return rc;
}

void updateMaster() {
   String lsCarno = "", lsOldcarno = "";

   if (!empty(wp.itemStr("rd_newcarno"))) {
      lsOldcarno = wp.itemStr("rd_carno");
      lsCarno = wp.itemStr("rd_newcarno");
   } else {
      lsCarno = wp.itemStr("rd_carno");
   }
   String lsRowid =wp.itemStr("rm_rowid");
   if (empty(lsRowid)) {
      strSql = " select count(*) cnt from cms_roadmaster "
              + "where  card_no =? and rm_carmanid =? ";
      setParm(1, wp.itemStr("appl_card_no"));
      setParm(2, wp.itemStr("rd_carmanid"));
      sqlSelect(strSql);
      int liExist=colInt("xx_cnt");
      if (liExist <=0) {
         insertMaster();
         return;
      }
      //isExist--
      errmsg("申請卡號及身分證ID: 已登錄");
      return;
   }

   tt.update("cms_roadmaster");
   tt.parmSet("rm_type", wp.itemStr("rd_type"));
   tt.parmSet("rm_carmanname", wp.itemStr("rd_carmanname"));
   tt.parmSet("rm_carmanid", wp.itemStr("rd_carmanid"));
   tt.parmSet("rm_status", wp.itemStr("rd_status"));
   tt.parmSet("rm_oldcarno", lsOldcarno);
   tt.parmSet("rm_carno", lsCarno);
   tt.parmSet("rm_moddate", wp.itemStr("rd_moddate"));
   tt.parmSet("rm_validdate", wp.itemStr("rd_validdate"));
   tt.parmSet("rm_reason", wp.itemStr("rd_stoprsn"));
   tt.parmSet("rm_payamt", wp.itemStr("rd_payamt"));
   tt.parmSet("rds_pcard", wp.itemStr("rds_pcard"));
   tt.parmSet("apr_user", modUser);
   tt.parmSet("apr_date", sysDate);
   tt.modxxxSet(modUser,modPgm);
   tt.whereRowid(wp.itemStr("rm_rowid"));

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      errmsg("update cms_roadmaster error ! ");
   }
   return;
}

void deleteMaster() {
   strSql = "delete cms_roadmaster where 1=1"  //card_no =:card_no and rm_carmanid =:rm_carmanid";
   +commSqlStr.whereRowid;

   setParm(1, wp.itemStr("rm_rowid"));
//
//   setString("card_no", wp.itemStr("appl_card_no"));
//   setString("rm_carmanid", wp.itemStr("rd_carmanid"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("delete cms_roadmaster error ! ");
   }
   return;
}

int updateBilSysexp() {
   String ls_applCardNo = wp.itemStr("appl_card_no");
   String ls_rdType = wp.itemStr("rd_type");
   if (!eqIgno(ls_rdType, "E"))
      return 0;

   // --AMT
   int ldcAmt = (int) (wp.itemNum("rd_payamt"));
   if (ldcAmt <= 0)
      return 0;

   // --acct_type/key--
   String sql1 = " select " + " acct_type , " + commSqlStr.ufunc("uf_acno_key(p_seqno) as acct_key ")
           + " from crd_card " + " where card_no = ? ";
   sqlSelect(sql1, ls_applCardNo);
   if (sqlRowNum <= 0) {
      return -1;
   }

   String ls_acctType = colStr("acct_type");
   String ls_acctKey = colStr("acct_key");

   tt.insert("bil_sysexp");
   tt.parmSet("card_no", ls_applCardNo);
   tt.parmSet("bill_type", "INCF");
   tt.parmSet("txn_code", "05");
   tt.parmSet("purchase_date", sysDate);
   tt.parmSet("acct_type", ls_acctType);
   tt.parmSet("acct_key", ls_acctKey);
   tt.parmSet("dest_amt", ldcAmt);
   tt.parmSet("dest_curr", "901");
   tt.parmSet("src_amt", ldcAmt);
   tt.parmSet("post_flag", "N");
   tt.modxxxSet(modUser,modPgm);

   sqlExec(tt.getSql(), tt.getParms());
   if (sqlRowNum <= 0) {
      return -1;
   }

   return 1;
}

@Override
public int dbUpdate() {
   actionInit("U");
   ioFunc =new RdsFunc();
   ioFunc.setConn(wp);

   dataCheckUpdate();
   if (rc != 1)
      return rc;

   insertDetail();
   if (rc != 1)
      return rc;

   updateMaster();
   if (rc != 1)
      return rc;

   Double rdPayamt = wp.itemNum("rd_payamt");
   if (rdPayamt > 0) {
      updateBilSysexp();
   }

   return rc;
}

@Override
public int dbDelete() {
   actionInit("D");
   ioFunc =new RdsFunc();
   ioFunc.setConn(wp);
   //--
   if (wp.itemEmpty("rm_rowid")) {
      errmsg("未讀取資料, 不可刪除");
      return rc;
   }

   insertDetail();
   if (rc != 1)
      return rc;

   deleteMaster();
   if (rc != 1)
      return rc;

//   Double rdPayamt = wp.itemNum("rd_payamt");
//   if (rdPayamt > 0) {
//      updateBilSysexp();
//   }

   return rc;
}

@Override
public int dataProc() {
   return 0;
}
}
