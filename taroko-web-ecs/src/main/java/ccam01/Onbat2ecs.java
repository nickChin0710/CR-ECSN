package ccam01;
/** onbat_2ecs公用程式
 * 2019-1224   JH    modify
 * 2019-0606:  JH    p_seqno >> acno_p_seqno
 * 2018-0427:	JH		initial
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * */
import busi.FuncBase;

public class Onbat2ecs extends FuncBase {
  busi.SqlPrepare sp = new busi.SqlPrepare();
  public Hhdata hh = new Hhdata();

  public int ccam2030Spec(String aCardNo, String aSpec) {
    msgOK();

    strSql = "select card_no, acct_type, id_p_seqno, acno_p_seqno, 'N' as vd_flag"
        + " from crd_card where card_no =? and current_code ='0'"
        + " union select card_no, acct_type, id_p_seqno, p_seqno as acno_p_seqno, 'Y' as vd_flag"
        + " from dbc_card where card_no=? and current_code ='0'";
    setString2(1, aCardNo);
    setString(aCardNo);

    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("查無卡片資料, kk[%s]", aCardNo);
      return rc;
    }

    hh.initData();
    hh.transType = "3";
    hh.toWhich = 1;
    hh.procMode = "O";
    hh.procStatus = 0;
    hh.acctType = colStr("acct_type");
    hh.idPSeqno = colStr("id_p_seqno");
    hh.acnoPSeqno = colStr("acno_p_seqno");
    hh.cardNo = colStr("card_no");
    hh.blockReason1 = aSpec;
    hh.blockReason2 = "";
    hh.blockReason3 = "";
    hh.blockReason4 = "";
    hh.blockReason5 = "";
    hh.debitFlag = colStr("vd_flag");
    insertOnbat2ecs();

    return 1;
  }

  public int ccam2040CardBlock(String vdFlag, String pSeqno, String[] aBlock) {
    msgOK();

    if (eq(vdFlag, "Y")) {
      strSql = "select card_no, acct_type, id_p_seqno, p_seqno as acno_p_seqno, 'Y' as vd_flag"
          + " from dbc_card where p_seqno=? and current_code ='0'";
    } else {
      strSql = "select card_no, acct_type, id_p_seqno, acno_p_seqno, 'N' as vd_flag"
          + " from crd_card where acno_p_seqno =? and current_code ='0'";
    }
    setString2(1, pSeqno);

    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return 0;

    int llCard = sqlRowNum;

    for (int ll = 0; ll < llCard; ll++) {
      hh.initData();
      hh.transType = "3";
      hh.toWhich = 1;
      hh.procMode = "O";
      hh.procStatus = 0;
      hh.acctType = colStr(ll, "acct_type");
      hh.idPSeqno = colStr(ll, "id_p_seqno");
      hh.acnoPSeqno = colStr(ll, "acno_p_seqno");
      hh.cardNo = colStr(ll, "card_no");
      hh.blockReason1 = aBlock[0];
      hh.blockReason2 = aBlock[1];
      hh.blockReason3 = aBlock[2];
      hh.blockReason4 = aBlock[3];
      hh.blockReason5 = aBlock[4];
      hh.debitFlag = colStr(ll, "vd_flag");

      insertOnbat2ecs();
    }

    return 1;
  }

  private int insertOnbat2ecs() {
    sp.sql2Insert("onbat_2ecs");
    sp.ppstr2("trans_type", hh.transType);
    sp.ppint2("to_which", hh.toWhich);
    sp.ppdate("dog");
    sp.ppdate("dop");
    sp.ppstr2("proc_mode", "O");
    sp.ppint2("proc_status", 0);
    sp.ppstr2("acct_type", hh.acctType);
    sp.ppstr2("id_p_seqno", hh.idPSeqno);
    sp.ppstr2("acno_p_seqno", hh.acnoPSeqno);
    sp.ppstr2("card_no", hh.cardNo);
    sp.ppymd("proc_date");
    sp.ppstr2("block_reason1", hh.blockReason1);
    sp.ppstr2("block_reason2", hh.blockReason2);
    sp.ppstr2("block_reason3", hh.blockReason3);
    sp.ppstr2("block_reason4", hh.blockReason4);
    sp.ppstr2("block_reason5", hh.blockReason5);
    sp.ppstr2("debit_flag", hh.debitFlag);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      errmsg("onbat_2ecs.insert error, kk[%s]", hh.cardNo);
      return -1;
    }

    return 1;
  }

  public int ccam2040CardUnBlock(String vdFlag, String pSeqno) {

    if (eq(vdFlag, "Y")) {
      strSql = "select card_no, acct_type, id_p_seqno, p_seqno as acno_p_seqno, 'Y' as vd_flag"
          + " from dbc_card where p_seqno =? and current_code ='0'";
    } else {
      strSql = "select card_no, acct_type, id_p_seqno, acno_p_seqno, 'N' as vd_flag"
          + " from crd_card where acno_p_seqno =? and current_code ='0'";
    }
    setString2(1, pSeqno);

    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return 0;

    busi.SqlPrepare sp = new busi.SqlPrepare();
    int llCard = sqlRowNum;

    for (int ll = 0; ll < llCard; ll++) {
      hh.initData();
      hh.transType = "3";
      hh.toWhich = 1;
      hh.procMode = "O";
      hh.procStatus = 0;
      hh.acctType = colStr(ll, "acct_type");
      hh.idPSeqno = colStr(ll, "id_p_seqno");
      hh.acnoPSeqno = colStr(ll, "acno_p_seqno");
      hh.debitFlag = colStr(ll, "vd_flag");
      hh.cardNo = colStr(ll, "card_no");
      hh.blockReason1 = "";
      hh.blockReason2 = "";
      hh.blockReason3 = "";
      hh.blockReason4 = "";
      hh.blockReason5 = "";

      insertOnbat2ecs();
    }

    return 1;
  }

  class Hhdata {
    public String transType = "";
    public int toWhich = 0;
    public String dog = "";
    public String dop = "";
    public String procMode = "";
    public int procStatus = 0;
    public String cardIndicator = "";
    public String paymentType = "";
    public String acctType = "";
    public String idPSeqno = "";
    public String acnoPSeqno = "";
    public String cardNo = "";
    public String oldCardNo = "";
    public String procDate = "";
    public String blockReason1 = "";
    public String blockReason2 = "";
    public String blockReason3 = "";
    public String blockReason4 = "";
    public String blockReason5 = "";
    public String debitFlag = "";

    public void initData() {
      transType = "";
      toWhich = 0;
      dog = "";
      dop = "";
      procMode = "";
      procStatus = 0;
      cardIndicator = "";
      paymentType = "";
      acctType = "";
      idPSeqno = "";
      acnoPSeqno = "";
      cardNo = "";
      oldCardNo = "";
      procDate = "";
      blockReason1 = "";
      blockReason2 = "";
      blockReason3 = "";
      blockReason4 = "";
      blockReason5 = "";
      debitFlag = "";
    }
  }



}
