/*****************************************************************************
*                   TEXT 報表  樣版檔  處理物件                              *
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*    DATE    Version    AUTHOR                       DESCRIPTION              *
*  --------  -------------------  ------------------------------------------  *
*  109/07/06  V0.00.02    Zuwei     coding standard, rename field method & format                   *
*  110-01-07   V1.00.03    shiyuqi       修改无意义命名                                        
*  111-01-19  V1.00.04    Justin     fix Missing Check against Null           *
*  111-10-26  V1.00.05    Zuwei     copy method closeOutFile & var freeFormat from mega          *                                   *
******************************************************************************/
package com;

import  com.*;
import  java.io.*;
import  java.util.*;
import  com.Big52CNS.CnsResult;

public class FormControl {

  AccessDAO wp = null;

  String[] fieldName = new String[80];

  public String[] formFile = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  public String[] outFile = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
  public int[] wd = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  public int[] formCount = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  public int[] outCount = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

  public String[][] formData = new String[12][200];
  public String[][] outBuffer = new String[12][5000];

  private HashMap<String, Integer> cntHash = new HashMap<String, Integer>();
  private HashMap<String, Integer> strHash = new HashMap<String, Integer>();
  
  public  boolean freeFormat=false;

  String newLine = "", spaces = "", folder = "", debugFlag = "N";
  byte[] carriage = {0x0D, 0x0A, 0x00};
  int serno = 0;
  int dummyLength = 11;

  Big52CNS b2cns = null;

  public FormControl(AccessDAO wp) {
    this.wp = wp;
    for (int i = 0; i < 60; i++) {
      spaces = spaces + "          ";
    }

    folder = System.getenv("PROJ_HOME") + "/media/cyc/";
    // folder = "D:/BANK_ACCT/media/cyc/";
    newLine = new String(carriage, 1, 1);

    return;
  }

  public void openOutFile() throws Exception {

    for (int i = 0; i < outFile.length; i++) {
      if (outFile[i].length() > 0) {
        wd[i] = wp.openBinaryOutput2(folder + outFile[i]);
      }
    }

    b2cns = new Big52CNS();
    return;
  }

  public void closeOutFile() throws Exception {

     for ( int i=0; i<outFile.length; i++ ) {
           if ( outFile[i].length() > 0 )
              { wp.closeBinaryOutput2(wd[i]); }
         }

     return;
  }

  public void loadForm() throws Exception {

    cntHash.clear();
    for (int oi = 0; oi < formFile.length; oi++) {
      if (formFile[oi].length() > 0) {
        getFormData(oi);
      }
    }

    return;
  }

  public void getFormData(int oi) throws Exception {

    int int1 = 0;
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (BufferedReader dr = new BufferedReader(new FileReader(folder + "/form/" + formFile[oi]))) {
      while (dr.ready()) {
        String inputStr = dr.readLine();
        if (inputStr != null) {
			if (inputStr.length() < 9) {
				continue;
			}
			formData[oi][int1] = inputStr;
			int1++;
		}
      } // while

      formCount[oi] = int1;
      dr.close();
    }
    return;
  }

  public void genOutputFile(int oi) throws Exception {

    processFormData(oi);

    for (int i = 0; i < outCount[oi]; i++) {
      if (debugFlag.equals("N") && Arrays.asList("NUMONBIL", "NUCURBIL").contains(outFile[oi])) {
        byte[] outData = b2cns.convCns((outBuffer[oi][i] + newLine).getBytes("MS950")).data;
        for (int j = 0; j < 174; j++) {
          if (outData[j] == 0x00) {
            outData[j] = 0x20;
          }
        }
        byte[] headData = outBuffer[oi][i].substring(0, 14).getBytes("Cp1047");
        for (int k = 0; k < headData.length; k++) {
          outData[k] = headData[k];
        }
        wp.writeBinFile2(wd[oi], outData, 174);
      } else {
        byte[] outData = (outBuffer[oi][i] + newLine).getBytes("MS950");
        wp.writeBinFile2(wd[oi], outData, outData.length);
      }
    }
    // cntHash.clear();
    return;
  }

  public void processFormData(int oi) throws Exception {

    int int1 = 0;
    serno = 0;
    outCount[oi] = 0;
    for (int i = 0; i < formCount[oi]; i++) {
      int1 = 0;
      String label = formData[oi][i].substring(0, 7).trim();
      if (label.length() >= 2) {

        Integer listCount = (Integer) cntHash.get(label);
        if (listCount == null || listCount == 0) {
          continue;
        }

        Integer startPnt = (Integer) strHash.get(label);

        if (startPnt == null) {
          startPnt = 0;
        }

        if (formData[oi][i].trim().length() == dummyLength) {
          outputDummyData(oi, formData[oi][i].substring(8));
          continue;
        }

        for (int j = startPnt; j < (startPnt + listCount); j++) {
          outputFileData(oi, j, formData[oi][i], label);
        }
      } else {
        outputFileData(oi, 0, formData[oi][i], "@");
        continue;
      }
    }

    return;
  }

  public void setDisplayCount(String label, int count) throws Exception {

    cntHash.put(label, count);
    return;
  }

  public void setStartPoint(String label, int startPnt) throws Exception {

    strHash.put(label, startPnt);
    return;
  }

  public void outputDummyData(int oi, String inBuffer) throws Exception {

    int cnt = outCount[oi];
    outBuffer[oi][cnt] = inBuffer;
    outCount[oi]++;
    return;
  }

  public void outputFileData(int oi, int j, String inBuffer, String label) throws Exception {

    serno++;
    String sernData = "" + serno;
    if (sernData.length() <= 3) {
      sernData = "0000".substring(0, 3 - sernData.length()) + sernData;
    }
    wp.setValue("SN", sernData, j);

    if (wp.getValue("WEB_SKIP", j).equals("Y") && outFile[oi].substring(0, 3).equals("WEB")) {
      return;
    } // WEB FILE 跳掉 分期訊息

    int cnt = outCount[oi];
    outBuffer[oi][cnt] = processBufferData(oi, j, inBuffer.substring(8), "R", "{", "}", label);
    outBuffer[oi][cnt] = processBufferData(oi, j, outBuffer[oi][cnt], "L", "[", "]", label);
    outBuffer[oi][cnt] = processBufferData(oi, j, outBuffer[oi][cnt], "M", "<", ">", label);

    outCount[oi]++;

    return;
  }

  public String processBufferData(int oi, int j, String inBuffer, String actCode, String lMark,
      String rMark, String label) throws Exception {

    String tmpBuffer = inBuffer;

    int fieldCount = 0;
    while (true) {
      int pnt1 = tmpBuffer.indexOf(lMark);
      int pnt2 = tmpBuffer.indexOf(rMark);

      if (pnt1 == -1 || pnt2 == -1) {
        break;
      }
      fieldName[fieldCount] = tmpBuffer.substring(pnt1 + 1, pnt2).toUpperCase();
      fieldCount++;
      tmpBuffer = tmpBuffer.substring(pnt2 + 1, tmpBuffer.length());
    }

    String cvtData = inBuffer;

    for (int m = 0; m < fieldCount; m++) {

      String cvtField = fieldName[m];
      if (fieldName[m].equals("N")) {
        cvtField = label + "-" + fieldName[m];
      }

      String replaceValue = wp.getValue(cvtField, j);
      String replaceName = lMark + fieldName[m] + rMark;

      int pntL = cvtData.indexOf(lMark);
      if (pntL == -1) {
        continue;
      }
      int pntR = cvtData.indexOf(rMark);
      if (pntR == -1) {
        continue;
      }

      int valLength = replaceValue.getBytes("MS950").length;
      int n = valLength - replaceName.length();

      if (actCode.equals("M")) {
        cvtData = cvtData.replace(replaceName, replaceValue);
      } else if (n <= 0) {
        if (actCode.equals("R")) {
          replaceValue = spaces.substring(0, n * -1) + replaceValue;
        } else if (actCode.equals("L")) {
          replaceValue = replaceValue + spaces.substring(0, n * -1);
        }
        cvtData = cvtData.replace(replaceName, replaceValue);
        // System.out.println("11-7 "+cvtData);
      } else if (n > 0) {
        if (actCode.equals("R")) {
          if ((pntL - n) < 3) {
            int pntT = 3 - (pntL - n);
            cvtData = cvtData.substring(0, 3) + replaceValue.substring(pntT)
                + cvtData.substring(pntR + 1);
          } else {
            cvtData = cvtData.substring(0, pntL - n) + replaceValue + cvtData.substring(pntR + 1);
          }
        } else if (actCode.equals("L")) {
          if (cvtData.length() > (pntL + valLength)) {
            cvtData =
                cvtData.substring(0, pntL) + replaceValue + cvtData.substring(pntL + valLength);
          } else {
            cvtData = cvtData.substring(0, pntL) + replaceValue;
          }
        }
        // System.out.println("11-8 "+cvtData);
      }
    } // end of for loop

    return cvtData;
  }

} // End of class FormControl
