package cmsr02;

import busi.FuncAction;

public class Cmsr0040Func extends FuncAction {

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
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

  public int deleteData() {
    msgOK();
    strSql = " delete cms_casepost where rowid=:rowid ";
    setRowId("rowid", varsStr("rowid"));
    sqlExec(strSql);
    if (sqlRowNum <= 0)
      errmsg("delect casepost error !");
    return rc;
  }

}
