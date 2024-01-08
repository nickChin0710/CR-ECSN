/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.02  Zuwei       code format                              *
* 110-01-07  V1.00.03  tanwei        修改意義不明確變量                                                                          *   
******************************************************************************/
package busi;

public abstract class FuncEdit extends FuncBase {

  public boolean ibAdd = false;
  public boolean ibUpdate = false;
  public boolean ibDelete = false;

  public FuncEdit() {}

  // public BaseEdit(Connection con1) {
  // conn = con1;
  // }

  public abstract int querySelect();

  public abstract int dataSelect();

  public abstract void dataCheck();

  public abstract int dbInsert();

  public abstract int dbUpdate();

  public abstract int dbDelete();

  public void actionInit(String strName) {
    actionCode = strName;
    ibAdd = isAdd();
    ibUpdate = isUpdate();
    ibDelete = isDelete();
    msgOK();
  }

  public int dbSave(String sAud) {
    actionInit(sAud);
    if (eqIgno(sAud, "A")) {
      return dbInsert();
    } else if (eqIgno(sAud, "U")) {
      return dbUpdate();
    } else if (eqIgno(sAud, "D")) {
      return dbDelete();
    }

    errmsg("DB-action 不是 A/U/D");
    return rc;
  }

}
