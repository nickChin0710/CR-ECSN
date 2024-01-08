/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-13  V1.00.01  Zuwei       updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-22  V1.00.01  Zuwei       coding standard      *
*  109-12-24   V1.00.02 Justin        add get and set noClear
*  110-01-08  V1.00.03  tanwei      修改意義不明確變量                                                                          * 
*  110-01-30  V1.00.04  Justin        fix selection bugs
*  111-01-07  V1.00.05  Justin    optimize the SQL script by replacing + with StringBuilder *
******************************************************************************/
package taroko.base;
/*SQL 轉換公用程式 V.2018-1106
	2018-1106:	JH		ppp(col,xxx)
 * 2018-0727:	JH		++ppp(...)
 * 
 * */
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "deprecation"})
public class SqlParm {

  private HashMap<String, Object> parmHash = new HashMap<String, Object>();
  private HashMap<Integer, Object> sortHash = new HashMap<Integer, Object>();
  private int parmCnt = 0;
  private String parmFlag = "";
  private String convertSQL = "";
  private String convertWhere = "";
  private String questionRemarkWhere = "";



private Object[] convParm;
  // private String internalCall="";
  private boolean noClear = false;
  private int numOfQMOfSelect = 0, numOfQMOfDaoTable = 0, numOfQMOfWhere = 0;

  public void setSqlParmNoClear(boolean isNoClear) {
	  noClear = isNoClear;
  }
  public boolean getNoClear() {
	  return this.noClear;
  }
  // -<<<-2018-0727--
  public void setString2(int col, String strName) {
    parmCnt = col;
    setString(col, strName);
  }

  public void setString(String strName) {
    parmCnt++;
    setString(parmCnt, strName);
  }

  public void setDouble2(int col, double strName) {
    parmCnt = col;
    setDouble(col, strName);
  }

  public void setDouble(Double dou) {
    parmCnt++;
    setDouble(parmCnt, dou);
  }

  public void setInt2(int col, int num) {
    parmCnt = col;
    setInt(col, num);
  }

  public void setInt(int num) {
    parmCnt++;
    setInt(parmCnt, num);
  }

  // ->>>-2018-0727--
  public void setString2(String col, String strName) {
    this.setString(col, strName);
  }

  public void setInt2(String col, int num1) {
    this.setInt(col, num1);
  }

  public void setDouble2(String col, double num1) {
    this.setDouble(col, num1);
  }

  public int getParmcnt() {
    return parmCnt;
  }

  public void setInt(int num, int parmNum) {
    sortHash.put(num, parmNum);
    parmFlag = "N";
    parmCnt = num;
    return;
  }

  public void setInt(String parmField, int parmNum) {
    parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
    parmFlag = "S";
    return;
  }

  public void setDouble(int num, double parmNum) {
    sortHash.put(num, parmNum);
    parmFlag = "N";
    parmCnt = num;
    return;
  }

  public void setDouble(String parmField, double parmNum) {
    parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmNum);
    parmFlag = "S";
    return;
  }

  public void setNumber(String col, double num1) {
    setDouble(col, num1);
  }

  public void setNumber(String col) {
    setDouble(col, 0);
  }

  public void setString(int num, String parmString) {
    sortHash.put(num, parmString);
    parmFlag = "N";
    parmCnt = num;
    return;
  }

  public void setString(String parmField, String parmString) {
    parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(), parmString);
    parmFlag = "S";
    return;
  }

  // public void setEmptyString(String col) {
  // setString(col,"");
  // }
  public void setRowId(int num, String parmRowId) // throws Exception
  {
    sortHash.put(num, hex2Byte(parmRowId));
    parmFlag = "N";
    parmCnt = num;
    return;
  }

  public void setRowId(String parmField, String parmRowId) // throws Exception
  {
    parmHash.put("" + (100 - parmField.trim().length()) + ":" + parmField.trim(),
        hex2Byte(parmRowId));
    parmFlag = "S";
    return;
  }

  byte[] hex2Byte(String strName) {
    int len = strName.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(strName.charAt(i), 16) << 4) + Character.digit(strName.charAt(i + 1), 16));
    return data;
  }

  // -VV------------------------------------------------------

  public String getConvSQL() {
    return convertSQL;
  }

  public Object[] getConvParm() {
    return convParm;
  }

  private String replaceStrParmWithQuestMark(String sqlStr) {
	String convertSQL = sqlStr;
	Object[] keys1 = parmHash.keySet().toArray();
      Arrays.sort(keys1);
      for (Object parmKey : keys1) {
        String kk = parmKey.toString();
        String replaceField = kk.substring(kk.indexOf(":"));
        // -相同變數取代-
        int ii = sqlStr.indexOf(replaceField);
        while (ii > 0) {
          convertSQL = convertSQL.replaceFirst(replaceField, "?");
          sortHash.put(ii, parmHash.get(parmKey));
          parmCnt++;
          sqlStr =
              sqlStr.replaceFirst(replaceField,
                  new String(new char[replaceField.length()]).replace("\0", "-"));
          // -next-value-
          ii = sqlStr.indexOf(replaceField);
        }
      }
      return convertSQL;
}

private String replaceStrParmWithQuestMark(String selectSQL, String daoTable, String whereStr, String whereOrder) {
	  String convertSQL = getSQLString(selectSQL, daoTable, whereStr, whereOrder);

	  String questionRemarkWhere = whereStr;
	  int numOfQMOfSelect = 0, numOfQMOfDaoTable = 0, numOfQMOfWhere = 0;
	  
	  Object[] keys1 = parmHash.keySet().toArray();
      Arrays.sort(keys1);
      for (Object parmKey : keys1) {
        String kk = parmKey.toString();
        String replaceField = kk.substring(kk.indexOf(":"));
        // -相同變數取代-
        
        //select
        int ii = selectSQL.indexOf(replaceField);
        while (ii > 0) {
          convertSQL = convertSQL.replaceFirst(replaceField, "?");
          sortHash.put(ii, parmHash.get(parmKey));
          parmCnt++;
          numOfQMOfSelect ++;
          selectSQL =
        		  selectSQL.replaceFirst(replaceField,
                  new String(new char[replaceField.length()]).replace("\0", "-"));
          // -next-value-
          ii = selectSQL.indexOf(replaceField);
        }
        
        int lenOfSelect = selectSQL.length();
        
        //daotable
        ii = daoTable.indexOf(replaceField);
        while (ii > 0) {
          convertSQL = convertSQL.replaceFirst(replaceField, "?");
          sortHash.put(ii + lenOfSelect, parmHash.get(parmKey));
          parmCnt++;
          numOfQMOfDaoTable++;
          daoTable =
        		  daoTable.replaceFirst(replaceField,
                  new String(new char[replaceField.length()]).replace("\0", "-"));
          // -next-value-
          ii = daoTable.indexOf(replaceField);
        }
        
        int lenOfSelectAndTable = lenOfSelect + daoTable.length();
        
        // where
        ii = whereStr.indexOf(replaceField);
        while (ii > 0) {
          questionRemarkWhere = questionRemarkWhere.replace(replaceField, "?");
          convertSQL = convertSQL.replaceFirst(replaceField, "?");
          sortHash.put(ii + lenOfSelectAndTable, parmHash.get(parmKey));
          parmCnt++;
          numOfQMOfWhere++;
          whereStr =
        		  whereStr.replaceFirst(replaceField,
                  new String(new char[replaceField.length()]).replace("\0", "-"));
          // -next-value-
          ii = whereStr.indexOf(replaceField);
        }
        
        int lenOfSelectAndTableAndWhere = whereStr.length() + lenOfSelectAndTable;
        
        // whereOrder
        ii = whereOrder.indexOf(replaceField);
        while (ii > 0) {
          questionRemarkWhere = questionRemarkWhere.replace(replaceField, "?");
          convertSQL = convertSQL.replaceFirst(replaceField, "?");
          sortHash.put(ii + lenOfSelectAndTableAndWhere, parmHash.get(parmKey));
          parmCnt++;
          whereOrder =
        		  whereOrder.replaceFirst(replaceField,
                  new String(new char[replaceField.length()]).replace("\0", "-"));
          // -next-value-
          ii = whereOrder.indexOf(replaceField);
        }
        
      }
      setNumOfQMOfSelect(numOfQMOfSelect);
      setNumOfQMOfDaoTable(numOfQMOfDaoTable);
      setNumOfQMOfWhere(numOfQMOfWhere);
      setQuestionRemarkWhere(questionRemarkWhere);
      
      return convertSQL;
}

  public void clear() {
    sortHash.clear();
    parmHash.clear();
    parmFlag = "";
    parmCnt = 0;
    noClear = false;
  }

	private int countQuestMarkNum(String selectSQL) {
		int cnt = 0;
		for (int i = 0; i < selectSQL.length(); i++) {
			if (selectSQL.charAt(i) == '?') {
				cnt++;
			}
		}
		return cnt;
	}
	public boolean nameSqlParm(String selectSQL, String daoTable, String whereStr, String whereOrder, Object[] param) {
		convertSQL = getSQLString(selectSQL, daoTable, whereStr, whereOrder);
	    convParm = null;
	    convertWhere = whereStr;
	
	    if (Arrays.asList("N", "S").contains(parmFlag) == false) {
	      return false;
	    }
	
	    if (parmFlag.equals("S")) {
	    	convertSQL = replaceStrParmWithQuestMark(selectSQL, daoTable, whereStr, whereOrder);
	    }else {
	    	computeQuestMarkNum(selectSQL, daoTable, whereStr);
	    	setQuestionRemarkWhere(whereStr);
	    }
	
	    if (parmCnt == 0) {
	        return false;
	    }
	
	    // _convParm = new Object[parmCnt];
	    convParm = new Object[sortHash.size()];
	    Object[] keys2 = sortHash.keySet().toArray();
	    Arrays.sort(keys2);
	    int i = 0;
	    for (Object sortData : keys2) {
	      convParm[i++] = sortHash.get(sortData);
	    }
	    
	    int startIndex = getNumOfQMOfSelect() + getNumOfQMOfDaoTable();
	    this.convertWhere = 
    			convertQuestRemarkIntoWhereStr(getQuestionRemarkWhere(), convParm, startIndex, getNumOfQMOfWhere());
	
	    if (noClear) {
	      if (parmFlag.equalsIgnoreCase("S")) {
	        sortHash.clear();
	        parmCnt = 0;
	      }
	      noClear = false;
	      return true;
	    }
	
	    clear();
	    return true;
	}
	public String getSQLString(String selectSQL, String daoTable, String whereStr, String whereOrder) {
		StringBuilder bf = new StringBuilder()
	    .append(" SELECT ").append(selectSQL)
	    .append(" FROM ").append(daoTable)
	    .append(" ").append(whereStr);
		
		if (whereOrder.length() != 0) {
			bf.append(" ").append(whereOrder);
		}
		return bf.toString();
	}
	
	public String getSQLString(String selectSQL, String daoTable, String whereStr) {
		return getSQLString(selectSQL, daoTable, whereStr, "");
	}
	public boolean nameSqlParm(String cvtSql, String whereStr, Object[] parm) {
		convertSQL = cvtSql;
	    convParm = null;
	    convertWhere = whereStr;

	    if (Arrays.asList("N", "S").contains(parmFlag) == false) {
	      return false;
	    }

	    if (parmFlag.equals("S")) {
	    	if (whereStr.trim().length() != 0 && cvtSql.indexOf(whereStr) != -1) {
	    		convertSQL = replaceStrParmWithQuestMark(cvtSql, whereStr);
			}else {
				convertSQL = replaceStrParmWithQuestMark(cvtSql);
			}
	    }else {
	    	if (whereStr.trim().length() != 0 && cvtSql.indexOf(whereStr) != -1) {
	    		String firstPartOfSql = cvtSql.substring(0, cvtSql.indexOf(whereStr));
	    		
	    		int numOfQMOfSelect = countQuestMarkNum(firstPartOfSql);
	    		int numOfQMOfWhere = countQuestMarkNum(whereStr);
	    		
	    		setNumOfQMOfSelect(numOfQMOfSelect);
	    		setNumOfQMOfWhere(numOfQMOfWhere);
	    		
	    		setQuestionRemarkWhere(whereStr);
			}
	    }

	    if (parmCnt == 0) {
	        return false;
	    }
	    
	    // _convParm = new Object[parmCnt];
	    convParm = new Object[sortHash.size()];
	    Object[] keys2 = sortHash.keySet().toArray();
	    Arrays.sort(keys2);
	    int i = 0;
	    for (Object sortData : keys2) {
	      convParm[i++] = sortHash.get(sortData);
	    }
	    
	    this.convertWhere = 
    			convertQuestRemarkIntoWhereStr(whereStr, convParm, numOfQMOfSelect, numOfQMOfWhere);	    

	    if (noClear) {
	      if (parmFlag.equalsIgnoreCase("S")) {
	        sortHash.clear();
	        parmCnt = 0;
	      }
	      noClear = false;
	      return true;
	    }

	    clear();
	    return true;
	}
	public boolean nameSqlParm(String cvtSql, boolean bNoClear) {
	    noClear = bNoClear;
	    return nameSqlParm(cvtSql);
	  }
	public boolean nameSqlParm(String cvtSql) {
	    convertSQL = cvtSql;
	    convParm = null;
	
	    if (Arrays.asList("N", "S").contains(parmFlag) == false) {
	      return false;
	    }
	
	    if (parmFlag.equals("S")) {
	    	convertSQL = replaceStrParmWithQuestMark(cvtSql);
	    }
	
	    if (parmCnt == 0) {
	      return false;
	    }
	
	    // _convParm = new Object[parmCnt];
	    convParm = new Object[sortHash.size()];
	    Object[] keys2 = sortHash.keySet().toArray();
	    Arrays.sort(keys2);
	    int i = 0;
	    for (Object sortData : keys2) {
	      convParm[i++] = sortHash.get(sortData);
	    }
	
	    if (noClear) {
	      if (parmFlag.equalsIgnoreCase("S")) {
	        sortHash.clear();
	        parmCnt = 0;
	      }
	      noClear = false;
	      return true;
	    }
	
	    clear();
	    return true;
	  }
	private String replaceStrParmWithQuestMark(String sqlStr, String whereStr) {
		int startOfWhere = sqlStr.indexOf(whereStr);
		int endOfWhere  = startOfWhere + whereStr.length()-1 ;
		int numOfQMOf1 = 0, numOfQMOf2 = 0;
		String convertSQL = sqlStr;
		String questionMarkWhere = whereStr;
		Object[] keys1 = parmHash.keySet().toArray();
		Arrays.sort(keys1);
		for (Object parmKey : keys1) {
			String kk = parmKey.toString();
			String replaceField = kk.substring(kk.indexOf(":"));
			// -相同變數取代-
			int ii = sqlStr.indexOf(replaceField);
			while (ii > 0) {
				convertSQL = convertSQL.replaceFirst(replaceField, "?");
				sortHash.put(ii, parmHash.get(parmKey));
				parmCnt++;
				if ( ii < startOfWhere) {
					numOfQMOf1++;
				}else if (ii <= endOfWhere) {
					numOfQMOf2++;
					questionMarkWhere = questionMarkWhere.replaceFirst(replaceField, "?");
				}
				sqlStr = sqlStr.replaceFirst(replaceField,
						new String(new char[replaceField.length()]).replace("\0", "-"));
				// -next-value-
				ii = sqlStr.indexOf(replaceField);
			}
		}
		setNumOfQMOfSelect(numOfQMOf1);
		setNumOfQMOfWhere(numOfQMOf2);
		setQuestionRemarkWhere(questionMarkWhere);
		return convertSQL;
	}
	private void computeQuestMarkNum(String selectSQL, String daoTable, String whereStr) {
		
		int numOfQMOfSelect = countQuestMarkNum(selectSQL);
		int numOfQMOfDaoTable = countQuestMarkNum(daoTable);
		int numOfQMOfWhere = countQuestMarkNum(whereStr);
		
		setNumOfQMOfSelect(numOfQMOfSelect);
		setNumOfQMOfDaoTable(numOfQMOfDaoTable);
		setNumOfQMOfWhere(numOfQMOfWhere);
		
	}
	public int getNumOfQMOfSelect() {
		return numOfQMOfSelect;
	}
	private void setNumOfQMOfSelect(int numOfQMOfSelect) {
		this.numOfQMOfSelect = numOfQMOfSelect;
	}
	public int getNumOfQMOfDaoTable() {
		return numOfQMOfDaoTable;
	}
	private void setNumOfQMOfDaoTable(int numOfQMOfDaoTable) {
		this.numOfQMOfDaoTable = numOfQMOfDaoTable;
	}
	public int getNumOfQMOfWhere() {
		return numOfQMOfWhere;
	}
	private void setNumOfQMOfWhere(int numOfQMOfWhere) {
		this.numOfQMOfWhere = numOfQMOfWhere;
	}
	public String getConvertWhere() {
		return convertWhere;
	}
	public String getQuestionRemarkWhere() {
		return questionRemarkWhere;
	}
	private void setQuestionRemarkWhere(String questionRemarkWhere) {
		this.questionRemarkWhere = questionRemarkWhere;
	}
	private String convertQuestRemarkIntoWhereStr(String whereString, Object[] convParm, int startIndex, int convertNum) {
		
		if (convertNum != 0) {
	    	for (int j = 0; j < convertNum; j++) {
	    		whereString = 
	    				whereString.replaceFirst("\\?", "'" + convParm[startIndex + j].toString() + "'");
			}
		}
		return whereString;
		
	}
	public String getConvertWhere(String selectSQL, String daoTable, String whereStr, Object[] param) {
		computeQuestMarkNum(selectSQL, daoTable, whereStr);
    	int startIndex = getNumOfQMOfSelect() + getNumOfQMOfDaoTable();
    	return 
				convertQuestRemarkIntoWhereStr(whereStr, param, startIndex, getNumOfQMOfWhere());
	}
	
	public String getConvertWhere(String sqlCmd, String whereStr, Object[] param) {
		if (whereStr.trim().length() != 0 && sqlCmd.indexOf(whereStr) != -1) {
    		String firstPartOfSql = sqlCmd.substring(0, sqlCmd.indexOf(whereStr));
    		
    		int numOfQMOfSelect = countQuestMarkNum(firstPartOfSql);
    		int numOfQMOfWhere = countQuestMarkNum(whereStr);
    		
    		setNumOfQMOfSelect(numOfQMOfSelect);
    		setNumOfQMOfWhere(numOfQMOfWhere);
    		
    		return convertQuestRemarkIntoWhereStr(whereStr, param, numOfQMOfSelect, numOfQMOfWhere);
		}else {
			return whereStr;
		}
		
	}
	

}
