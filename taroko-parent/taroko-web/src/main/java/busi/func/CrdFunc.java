/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-08-03  V1.00.01  Zuwei       fix code scan issue                       *
* 109-12-24  V1.00.02  Justin        parameterize sql
******************************************************************************/
package busi.func;
/**製卡公用程
 * 2019-0718   JH    modify
 * 2019-0610:  JH    p_seqno >> acno_p_seqno
 * 2018-0627:	JH		is_debitcard()
 * 
 * */
import java.sql.Connection;
import java.text.ParseException;
import busi.FuncBase;

public class CrdFunc extends FuncBase {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  // public CrdFunc(Connection con1) {
  // conn = con1;
  // }
  public boolean isDebitcard(String cardNo) {
    String lsCardNo = this.nvl(cardNo);
    if (lsCardNo.length() < 6)
      return false;

    strSql =
        "select count(*) as xx_cnt"
            + " from ptr_bintable"
            + " where ? between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')"
            + " and debit_flag ='Y'";
    setString2(1, cardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0)
      return false;

    if (colInt("xx_cnt") > 0)
      return true;

    return false;
  }

  public int getIdAddr(String aCardNo) {
    if (empty(aCardNo)) {
      return 0;
    }

    this.varsClear();
    boolean lbDebit = isDebitcard(aCardNo);
    if (!lbDebit) {
      getIdAddrCrd(aCardNo);
    } else {
      getIdAddeDbc(aCardNo);
    }

    return rc;
  }

  private void getIdAddrCrd(String aCardNo) {
    strSql =
        "select 'N' as dbcard_flag, A.acno_p_seqno, A.id_p_seqno, A.corp_p_seqno"
            + ", B.bill_sending_zip, B.bill_sending_addr1, B.bill_sending_addr2"
            + ", B.bill_sending_addr3, B.bill_sending_addr4, B.bill_sending_addr5"
            + ", C.id_no, C.chi_name"
            + " from crd_card A join act_acno B on B.acno_p_seqno=A.acno_p_seqno"
            + " join crd_idno C on C.id_p_seqno=A.id_p_seqno" + " where 1=1 and A.card_no = ? ";
    setString(aCardNo);
    this.sqlSelect(strSql);
    if (sqlNotfind) {
      errmsg("查無持卡人資料 [crd_idno, acno], kk=" + aCardNo);
      return;
    }

    varsSet("dbcard_flag", colStr("dbcard_flag"));
    varsSet("acno_p_seqno", colStr("acno_p_seqno"));
    varsSet("id_p_seqno", colStr("id_p_seqno"));
    varsSet("corp_p_seqno", colStr("corp_p_seqno"));
    varsSet("id_no", colStr("id_no"));
    varsSet("chi_name", colStr("chi_name"));
    varsSet("bill_sending_zip", colStr("bill_sending_zip"));
    varsSet("bill_sending_addr1", colStr("bill_sending_addr1"));
    varsSet("bill_sending_addr2", colStr("bill_sending_addr2"));
    varsSet("bill_sending_addr3", colStr("bill_sending_addr3"));
    varsSet("bill_sending_addr4", colStr("bill_sending_addr4"));
    varsSet("bill_sending_addr5", colStr("bill_sending_addr5"));

    return;
  }

  private void getIdAddeDbc(String aCardNo) {
    strSql =
        "select 'N' as dbcard_flag, A.p_seqno as acno_p_seqno, A.id_p_seqno, A.corp_p_seqno"
            + ", B.bill_sending_zip, B.bill_sending_addr1, B.bill_sending_addr2"
            + ", B.bill_sending_addr3, B.bill_sending_addr4, B.bill_sending_addr5"
            + ", C.id_no, C.chi_name" + " from dbc_card A join dba_acno B on B.p_seqno=A.p_seqno"
            + " join dbc_idno C on C.id_p_seqno=A.id_p_seqno" + " where 1=1 and A.card_no = ? ";
    setString(aCardNo);
    this.sqlSelect(strSql);
    if (sqlNotfind) {
      errmsg("查無持卡人資料 [dbc_idno, dba_acno], kk=" + aCardNo);
      return;
    }

    varsSet("dbcard_flag", colStr("dbcard_flag"));
    varsSet("acno_p_seqno", colStr("acno_p_seqno"));
    varsSet("id_p_seqno", colStr("id_p_seqno"));
    varsSet("corp_p_seqno", colStr("corp_p_seqno"));
    varsSet("id_no", colStr("id_no"));
    varsSet("chi_name", colStr("chi_name"));
    varsSet("bill_sending_zip", colStr("bill_sending_zip"));
    varsSet("bill_sending_addr1", colStr("bill_sending_addr1"));
    varsSet("bill_sending_addr2", colStr("bill_sending_addr2"));
    varsSet("bill_sending_addr3", colStr("bill_sending_addr3"));
    varsSet("bill_sending_addr4", colStr("bill_sending_addr4"));
    varsSet("bill_sending_addr5", colStr("bill_sending_addr5"));

    return;
  }

  public String idnoToPseqno(String lsIdno) {
    String sql1 =
        "select id_p_seqno from crd_idno" + " where id_no =?" + " order by id_no_code"
            + commSqlStr.rownum(1);
    setString2(1, lsIdno);
    this.sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      errmsg("不是本行卡友; id=" + lsIdno);
      return "";
    }

    return colStr("id_p_seqno");
  }

  public boolean idnoIsNew(String idPseqno) throws ParseException {
    if (empty(idPseqno)) {
      errmsg("[f_idno_isnew] 參數為 empty or NULL");
      return false;
    }
    strSql =
        "select min(decode(current_code,'0',issue_date,'')) as db_issue0,"
            + " min(issue_date) as db_issue1," + " max(oppost_date) as db_oppost,"
            + " sum(decode(current_code,'0',1,0)) as db_cnt0,"
            // +" max(to_char(add_months(sysdate,-6),'yyyymmdd')) as db_6ymd,"
            // +" max(to_char(sysdate,'yyyy')) as db_yy"
            + " '' as xxx" + " from crd_card" + " where 1=1" + " and major_id_p_seqno = ?";
    setString2(1, idPseqno);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("[idno_isnew] error, value=%s", idPseqno);
      return false;
    }
    String lsTodate = commDate.sysDate();
    String lsYyyy = lsTodate.substring(0, 4);
    String ls6ymd = commDate.dateAdd(lsTodate, 0, -6, 0);
    // -無有效卡-
    String lsIssueYy = colStr("db_issue1").substring(0, 4);
    if (colInt("db_cnt0") == 0) {
      if (eqAny(lsIssueYy, lsYyyy))
        return true;
      return false;
    }
    // -非當年-
    if (eqAny(lsIssueYy, lsYyyy) == false)
      return false;
    // -當年-
    if (colEmpty("db_oppost"))
      return true;
    if (colStr("db_oppost").compareTo(ls6ymd) >= 0)
      return true;

    return false;
  }

  public boolean employeeStatus(String aIdno) throws Exception {
    // -在職員工-
    if (empty(aIdno))
      return false;

    String sql1 =
        "select count(*) as db_cnt" + " from crd_employee" + " where 1=1 and id = ? " 
            + " and status_id in ('1','7')";
    setString(aIdno);
    this.sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return false;

    return (this.colInt("db_cnt") > 0);
  }

  public boolean employeeStatusAcct(String aAcctNo) throws Exception {
    // -在職員工-
    if (empty(aAcctNo))
      return false;

    String sql1 =
        "select count(*) as db_cnt" + " from crd_employee" + " where 1=1 and acct_no = ? "
            + " and status_id in ('1','7')";
    this.sqlSelect(sql1, new Object[] {aAcctNo});
    if (sqlRowNum <= 0)
      return false;

    return (this.colInt("db_cnt") > 0);
  }

  public String idPseqno(String aIdno) throws Exception {
    if (aIdno.length() < 10)
      return "";

    String sql1 =
        "select uf_idno_pseqno(cast(? as varchar(10))) as id_p_seqno from " + commSqlStr.sqlDual;
    setString2(1, aIdno);
    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return "";
    return colStr("id_p_seqno");
  }

  public String transPasswd(int type, String fromPawd) throws Exception {
    long addNum[] = {7, 34, 295, 4326, 76325, 875392, 2468135, 12357924, 123456789};
    int transInt, int1, int2, datalen;
    long dataint = 1;
    String fdig[] =
        {"08122730435961748596", "04112633405865798792", "03162439425768718095",
            "04152236415768798390", "09182035435266718497", "01152930475463788296",
            "07192132465068748593", "02172931455660788394"};
    String tmpstr = "";
    String tmpstr1 = "";
    String toPawd = "";

    if (fromPawd.length() < 1)
      return "";

    if (type == 0) {
      // 加密
      datalen = fromPawd.length();
      for (int1 = 0; int1 < datalen; int1++) {
        int sbn = Integer.parseInt(fromPawd.substring(int1, int1 + 1)) * 2 + 1;
        tmpstr += fdig[int1].substring(sbn, sbn + 1);
      }

      for (int1 = 0; int1 < datalen; int1++) {
        dataint = dataint * 10;
      }
      tmpstr1 = String.valueOf(dataint + Long.parseLong(tmpstr) - addNum[datalen - 1]);
      toPawd = tmpstr1.substring(tmpstr1.length() - datalen);

    } else {
      // 解密
      datalen = fromPawd.length();

      tmpstr1 = String.format("%d", Long.parseLong(fromPawd) + addNum[datalen - 1]);
      tmpstr = tmpstr1.substring(tmpstr1.length() - datalen);
      for (int1 = 0; int1 < datalen; int1++) {
        for (int2 = 0; int2 < 10; int2++) {
          int po = int2 * 2 + 1;
          if (tmpstr.substring(int1, int1 + 1).equals(fdig[int1].substring(po, po + 1))) {
            po = int2 * 2;
            toPawd += fdig[int1].substring(po, po + 1);
            break;
          }
        }
      }
    }

    return toPawd;
  }


}
