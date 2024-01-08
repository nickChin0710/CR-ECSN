/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  109/07/06  V1.00.00   Zuwei    coding standard, rename field method & format*
*  110-01-07  V1.00.02   shiyuqi  coding standard, rename                    * 
*  111-11-28  V1.00.03   Simon    sync codes with mega                       * 
*****************************************************************************/
package com;
import java.util.*;

@SuppressWarnings("unchecked")
public class  SortObject
{
 private  AccessDAO dao = null;

 private String[]   keyData   = {"","","","","","","","","",""};
 private String[]   sortField = {"","","","","","","","","",""};
 private String[]   sortType  = {"","","","","","","","","",""};

 private HashMap[]  keyHash    = new  HashMap[10];
 private HashMap    sortHash   = new  HashMap();
 private int        keyFieldCnt=0, countLength=0;

 CommFunction  comm = new CommFunction();

 public SortObject(AccessDAO dao)
 {
     this.dao = dao;
     return;
 }

 public void sortLoadData(String[] parmField,int  dataCount) throws Exception {

     keyFieldCnt = parmField.length;
     for( int i=0; i<keyFieldCnt; i++) {
          keyHash[i]  = new HashMap();
          String[] cvtData = parmField[i].split(",");
          sortField[i] = cvtData[0];
          sortType[i]  = "";
          if ( cvtData.length == 2 )
             { sortType[i]  = cvtData[1]; }
      }

     for( int i=0; i<keyFieldCnt; i++)
        { keyHash[i].clear(); }
     sortHash.clear();
     countLength = (""+ dataCount).length();

     for( int i=0; i < dataCount; i++) {
          getSortKeyData(i);
          for( int k=0; k <keyFieldCnt; k++)
             { keyHash[k].put(keyData[k],"");  }
        }

     for( int i=0; i<keyFieldCnt; i++) {
          sortKeyMap(keyHash[i],sortType[i]);
        }

     for( int i=0; i < dataCount; i++) {
          String combKey = combineKeyData(i);
          sortHash.put(combKey,"");
        }

     finalSort();
     return;
  }

 public void sortKeyMap(HashMap keyMap,String sortType) throws Exception {
     String[] keyArray = (String[])(keyMap.keySet().toArray(new String[0]));
     keyMap.clear();

     if ( sortType.equals("DESC") )
        { Arrays.sort(keyArray, Collections.reverseOrder()); }
     else
        { Arrays.sort(keyArray); }

     int i = 0;
     int len = (""+keyArray.length).length();
     for ( String srtKey : keyArray ) {
           keyMap.put(srtKey,comm.fillZero(""+i,len));  i++;
       }

     return;
   }

 public String combineKeyData(int pnt) throws Exception  {
     String combKey = "";
      getSortKeyData(pnt);
      for( int n=0; n <keyFieldCnt; n++)  {
        combKey = combKey + (String)keyHash[n].get(keyData[n]) + "-";
     }
     combKey = combKey + comm.fillZero(""+pnt, countLength);
     return combKey;
  }

 public void  getSortKeyData(int pnt) throws Exception  {
     for( int n=0; n <keyFieldCnt; n++)  {
          keyData[n] = dao.getValue(sortField[n],pnt);
          if ( keyData[n].length() == 0 )
             { keyData[n] = " "; }
        }
     return;
  }

 public void finalSort() throws Exception  {
     String[] keyArray = (String[])(sortHash.keySet().toArray(new String[0]));
     sortHash.clear();
     Arrays.sort(keyArray);
     int i = 0;
     for ( String srtKey : keyArray ) {
     	     String[] cvtData = srtKey.split("-");
     	     int k= cvtData.length -1;
           sortHash.put("#"+i,""+Integer.parseInt(cvtData[k]));  
           i++;
       }

     return;
   }

 public int  getSortIndex(int pnt) throws Exception {
     String ind  =  (String)sortHash.get("#"+pnt);
     if ( ind == null )
        { dao.showLogMessage("E",""," getSortIndex ERROR "+pnt); return 0; }
     int    k  =  Integer.parseInt(ind);
     return k;
  }

 } // end of class SortObject