package ccam02;
/** 2019-0612:    JH    p_xxx >>acno_p_xxx
 *  2020-0326:    YH   	update crd_seqno_log>>insert crd_seqno_log
 *  2020-0420  V1.00.01 yanghan 修改了變量名稱和方法名稱
 *  2020-0731  V1.00.02 tanwei 緊急替代卡卡號驗證 
 *  2020-0806  V1.00.03 tanwei 修改緊急替代卡不在區間内驗證
 *  2020-0817  V1.00.04 tanwei 修改緊急替代卡報錯異常
 * */
import busi.FuncAction;

public class Ccam7040Func extends FuncAction {
  public String lsBinNo = "",lsSeqno = "";
  private int existLogNum = 0;

  @Override
  public void dataCheck(){
    lsBinNo = commString.mid(wp.itemStr("new_card_no"), 0, 6);
    lsSeqno = commString.mid(wp.itemStr("new_card_no"), 6, 15);
    if (empty(wp.itemStr("new_card_no"))) {
      errmsg("緊急替代卡卡號 : 不可空白");
      return;
    }

    if (empty(wp.itemStr("new_beg_date2"))) {
      errmsg("有效日期(起) : 不可空白");
      return;
    }

    if (empty(wp.itemStr("new_end_date2"))) {
      errmsg("有效日期(迄) : 不可空白");
      return;
    }

    if (wp.itemNum("new_beg_date2") < commString.strToNum(getSysDate())) {
      errmsg("有效日期(起) : 不可小於系統日");
      return;
    }

    if (wp.itemNum("new_end_date2") < commString.strToNum(getSysDate())) {
      errmsg("有效日期(迄) : 不可小於系統日");
      return;
    }

    if (this.chkStrend(wp.itemStr2("new_beg_date2"), wp.itemStr2("new_end_date2")) == -1) {
      errmsg("有效日期 起迄錯誤");
      return;
    }

      String sqlSelect = "select bin_no, beg_seqno, end_seqno " + "from crd_cardno_range " +
      "where group_code = :group_code and card_type = :card_type and bin_no = :bin_no and card_flag = '1' and post_flag = 'Y' "; 
      item2ParmStr("card_type");
      item2ParmStr("group_code");
      setString("bin_no",lsBinNo); 
      sqlSelect(sqlSelect);
      int recordNum = sqlRowNum;
      boolean flag = false;
      for (int i = 0; i < recordNum; i++) {
        String begSeqno = colStr(i,"beg_seqno"); 
        String endSeqno =colStr(i,"end_seqno"); 
        if (lsSeqno.compareTo(begSeqno) >= 0 && lsSeqno.compareTo(endSeqno) <= 0) { 
          flag = true;
          break;
        }
      }
      if(!flag) {
        errmsg("該卡號不在區間內");
        return; 
      }
      sqlSelect = "select * from crd_prohibit where card_no = :card_no "; 
      setString("card_no",wp.itemStr("new_card_no")); 
      sqlSelect(sqlSelect); 
      if (sqlRowNum > 0) { 
        errmsg("該卡號為禁號");
        return; 
      }
      
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction(); 
      String tmpX15 =commString.mid(wp.itemStr("new_card_no"), 0, 15); 
      String tmpX16 = commString.mid(wp.itemStr("new_card_no"),15, 16); 
      String hSChkDif; 
      try { 
        hSChkDif = comm.cardChkCode(tmpX15); 
        if(!hSChkDif.equals(tmpX16)) {
          errmsg("該指定卡號檢查碼有誤"); 
          return; 
          } 
      } catch (Exception e) {
      errmsg("該指定卡號檢查碼有誤");
      e.printStackTrace(); }
      
      sqlSelect =
      "select reserve,use_date from crd_seqno_log where bin_no = :ls_bin_no and seqno = :ls_seqno "; 
      setString("ls_bin_no", lsBinNo); 
      setString("ls_seqno", lsSeqno); 
      sqlSelect(sqlSelect);
      existLogNum = sqlRowNum;
      if(sqlRowNum > 0) { 
        if (colStr("reserve").equals("Y") && !empty(colStr("use_date"))) {
          errmsg("此號碼已使用 !!"); 
          return; 
          } 
        }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    insertUrgentCard();
    if (rc != 1) {
      return rc;
    }

    insertOnbat2ecs(); 
    if (rc != 1) {
      return rc; 
      }
     
    if (existLogNum > 0) {
      updateLog();
    }else {
      insertLog();
    }
    return rc;
  }

  public int insertUrgentCard() {
    strSql = "insert into cca_urgent_card (" + " card_no , " + " card_type , " + " id_p_seqno , "
        + " acno_p_seqno , " + " corp_p_seqno , " + " new_beg_date , " + " new_end_date , "
        + " assig_flag , " + " old_card_no , " + " new_card_flag , " + " crt_date , "
        + " crt_time , " + " crt_user , " + " apr_date , " + " apr_time , " + " apr_user , "
        + " mod_user , " + " mod_time , " + " mod_pgm , " + " mod_seqno " + " ) values ("
        + " :new_card_no , " + " :card_type , " + " :id_p_seqno , " + " :acno_p_seqno , "
        + " :corp_p_seqno , " + " :new_beg_date2 , " + " :new_end_date2 , " + " 'Y' , "
        + " :card_no , " + " 'Y' , " + " to_char(sysdate,'yyyymmdd') , "
        + " to_char(sysdate,'hh24miss') , " + " :crt_user , " + " to_char(sysdate,'yyyymmdd') , "
        + " to_char(sysdate,'hh24miss') , " + " :apr_user , " + " :mod_user , " + " sysdate , "
        + " :mod_pgm , " + " '1' " + " )";

    item2ParmStr("new_card_no");
    item2ParmStr("card_type");
    item2ParmStr("id_p_seqno");
    setString2("acno_p_seqno", wp.itemStr2("acno_p_seqno"));
    item2ParmStr("corp_p_seqno");
    item2ParmStr("new_beg_date2");
    item2ParmStr("new_end_date2");
    item2ParmStr("card_no");
    setString("crt_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "ccam7040");
    setString("apr_user", wp.itemStr("approval_user"));
    wp.log(strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      // System.out.println("ppp");
      return rc;
    }
    return rc;
  }

  public int insertOnbat2ecs() {
    strSql = "insert into onbat_2ecs (" + " trans_type , " + " to_which , " + " dog , "
        + " proc_mode , " + " proc_status , " + " card_no , " + " old_card_no , "
        + " card_valid_from , " + " card_valid_to , " + " proc_date " + " ) values (" + " '8' , "
        + " '1' , " + " sysdate , " + " 'O' , " + " '0' , " + " :new_card_no , " + " :card_no , "
        + " :new_beg_date2 , " + " :new_end_date2 , " + " to_char(sysdate,'yyyymmdd') " + " )";

    item2ParmStr("new_card_no");
    item2ParmStr("new_beg_date2");
    item2ParmStr("new_end_date2");
    item2ParmStr("card_no");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      // System.out.println("aaa");
      return rc;
    }
    return rc;
  }

  public int insertLog() {
    msgOK();
    lsBinNo = commString.mid(wp.itemStr("new_card_no"), 0, 6);
    lsSeqno = commString.mid(wp.itemStr("new_card_no"), 6, 15);
    strSql = "insert into crd_seqno_log (" + " card_type_sort , " + " bin_no , " + " SEQNO , "
        + " card_type , " + " group_code , " + " card_flag , " + " RESERVE , " + " trans_no , "
        + " CRT_DATE , " + " MOD_TIME ," + " use_id ," + " use_date ," + " card_item ," + " mod_user ,"
        + " unit_code ," + " seqno_old ," + "	MOD_PGM" + " ) values (" + " 0 , " + " :ls_bin_no, "
        + " :ls_seqno , " + " :card_type , " + "	:group_code, " + " 1 , " + " 'Y' , " + " '', "
        + " to_char(sysdate,'yyyymmdd')  , " + " sysdate , " + " :use_id, "+ " to_char(sysdate,'yyyymmdd') , " 
        + " :card_item , " + " :mod_user , " + " :unit_code, " + " :seqno_old , "
        + " 'Ccam7040'" + " )";
    setString("ls_bin_no", lsBinNo);
    setString("ls_seqno", lsSeqno);
    item2ParmStr("card_type");
    item2ParmStr("group_code");
    setString("use_id", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("card_item", wp.itemStr("group_code") + wp.itemStr("card_type"));
    setString("unit_code", wp.itemStr("unit_code"));
    setString("seqno_old", commString.mid(lsSeqno, 0, 9));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      // System.out.println("ccc");
      return rc;
    }
    // System.out.println("添加成功");
    return rc;

  }
  
  public int updateLog() {
    msgOK();
    lsBinNo = commString.mid(wp.itemStr("new_card_no"), 0, 6);
    lsSeqno = commString.mid(wp.itemStr("new_card_no"), 6, 15);
    strSql = "update crd_seqno_log set " + " RESERVE ='Y' ," + " card_item =:card_item ,"
        + " unit_code =:unit_code ," + " use_id =:use_id ,"+ " use_date =to_char(sysdate,'yyyymmdd') ,"
        + " mod_user =:mod_user ," + " mod_pgm =:mod_pgm"
        + " where bin_no =:ls_bin_no " + " and seqno =:ls_seqno";

    setString("ls_bin_no", lsBinNo);
    setString("ls_seqno", lsSeqno);
    item2ParmStr("card_type");
    item2ParmStr("group_code");
    setString("unit_code", wp.itemStr("unit_code"));
    setString("use_id", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "Ccam7040");
    setString("card_item", wp.itemStr("group_code") + wp.itemStr("card_type"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return rc;
    }
    return rc;
    
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
