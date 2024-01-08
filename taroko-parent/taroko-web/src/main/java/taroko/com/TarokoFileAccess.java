/**
 * 2017-1222: jack: openOutputText() 
 * V00.02	2017-1027	Jack: modify
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-05-18  V1.00.01  Zuwei       add read excel                              *
*  109/09/05  V1.00.06    yanghan     fix code scan issue    
*  111-01-19  V1.00.07    Justin      fix Unchecked Return Value             *
******************************************************************************/
package taroko.com;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

import org.apache.commons.vfs2.tasks.ShowFileTask;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import Dxc.Util.SecurityUtil;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoFileAccess {

  private int maxFetch = 3, maxInFile = 3, maxOutFile = 5;
  private String[] inFile = new String[maxInFile];
  private String[] outFile = new String[maxOutFile];
  public String[] endFile = new String[maxInFile];

  private BufferedReader[] dr = new BufferedReader[maxInFile];
  private BufferedWriter[] dw = new BufferedWriter[maxOutFile];

  private FileInputStream fin = null;
  private DataInputStream dis = null;

  private FileOutputStream fon = null;
  private DataOutputStream dos = null;

  public int[] readCnt = {0, 0}, writeCnt = {0, 0, 0};

  TarokoCommon wp = null;

  public boolean fileDelete(String src) {
    File fs = new File(Normalizer.normalize(src, java.text.Normalizer.Form.NFKD));
    return fs.delete();
  }

  public TarokoFileAccess(TarokoCommon wr) {
    this.wp = wr;
    return;
  }

  public int openInputText2(String parmFile, String fileType) {
    int fr = -1;
    try {
      for (int i = 0; i < maxInFile; i++) {
        if (dr[i] != null) {
          continue;
        }
        // parmFile = wp.dataRoot + "/upload/" + parmFile;
        if (fileType.length() == 0)
          dr[i] = new BufferedReader(new FileReader(parmFile));
        else
          dr[i] =
              new BufferedReader(new InputStreamReader(new FileInputStream(parmFile), fileType));
        inFile[i] = parmFile;
        endFile[i] = "";
        fr = i;
        break;
      }
    } catch (Exception ex) {
      wp.showLogMessage("E", "openInputText", "INPUT FILE ERROR : " + parmFile);
      return -1;
    }

    return fr;
  }

  public int openInputText(String parmFile, String fileType) {
    int fr = -1;
    try {
      for (int i = 0; i < maxInFile; i++) {
        if (dr[i] != null) {
          continue;
        }
        parmFile = TarokoParm.getInstance().getDataRoot() + "/upload/" + parmFile;
        parmFile=SecurityUtil.verifyPath(parmFile);
        if (fileType.length() == 0)
          dr[i] = new BufferedReader(new FileReader(parmFile));
        else
          dr[i] =
              new BufferedReader(new InputStreamReader(new FileInputStream(parmFile), fileType));
        if (parmFile.endsWith(".xls") || parmFile.endsWith(".xlsx")) {
          dr[i].close();
          dr[i] = new BufferedReader(new StringReader(readExcel(parmFile)));
        }
        inFile[i] = parmFile;
        endFile[i] = "";
        fr = i;
        break;
      }
    } catch (Exception ex) {
      wp.showLogMessage("E", "openInputText", "INPUT FILE ERROR : " + parmFile);
      return -1;
    }

    return fr;
  }

  public int closeInputText(int i) {
    try {
      dr[i].close();
      dr[i] = null;
      readCnt[i] = 0;
      endFile[i] = "";
    } catch (Exception ex) {
      wp.showLogMessage("E", "closeInputText", "INPUT FILE ERROR : " + i);
      return -1;
    }

    return i;
  }

  public String readTextFile(int i) throws Exception {
    int inti = i;
    String inString = "";
    if (dr[i].ready() && (inString = dr[inti].readLine()) != null ) {
      readCnt[inti]++;
      return inString;
    }
    endFile[inti] = "Y";
    return "";
  }

  public int openOutputText(String parmFile) {
    return openOutputText(parmFile, "");
  }

  public int openOutputText(String parmFile, String fileType) {
    int fo = -1;
    try {
      for (int i = 0; i < maxOutFile; i++) {
        if (dw[i] != null) {
          continue;
        }
        // parmFile = wp.dataRoot + "/work/" + parmFile;
        parmFile = TarokoParm.getInstance().getWorkDir() + "/" + parmFile;
        parmFile=SecurityUtil.verifyPath(parmFile);
        dw[i] = new BufferedWriter(new FileWriter(parmFile));
        if (fileType.length() == 0) {
          dw[i] = new BufferedWriter(new FileWriter(parmFile));
        } else {
          dw[i] =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(parmFile), fileType));
        }
        outFile[i] = parmFile;
        fo = i;
        break;
      }
    } catch (Exception ex) {
      wp.expHandle(ex);
      return -1;
    }

    return fo;
  }

  public int closeOutputText(int i) {
    try {
      dw[i].close();
      dw[i] = null;
      writeCnt[i] = 0;
    } catch (Exception ex) {
      wp.showLogMessage("E", "closeOutputText", "FILE ERROR output  : " + i);
      return -1;
    }

    return i;
  }

  public boolean writeTextFile(int i, String outData) throws Exception {
    dw[i].write(outData);
    dw[i].flush();
    writeCnt[i]++;
    return true;
  }

  public boolean deleteFile(String fileName) throws Exception {
    fileName = TarokoParm.getInstance().getDataRoot() + "/work/" + fileName;
    File fi = new File(SecurityUtil.verifyPath(fileName));
    if (fi.delete() == false) {
		wp.showLogMessage("I", "", String.format("刪除[%s]失敗", fi.getPath().toString()));
	}
    fi = null;
    return true;
  }

  public boolean openBinaryInput(String parmFile) {
    try {
      parmFile = TarokoParm.getInstance().getDataRoot() + "/work/" + parmFile;
      fin = new FileInputStream(parmFile);
      dis = new DataInputStream(fin);
      inFile[0] = parmFile;
    } catch (Exception ex) {
      wp.showLogMessage("E", "openBinaryInput", "FILE : " + parmFile);
      return false;
    }
    return true;
  }

  public int readBinFile(byte[] inData) throws Exception {
    int inputLen = dis.read(inData);
    readCnt[0]++;
    return inputLen;
  }

  public void closeBinaryInput() throws Exception {
    dis.close();
    dis = null;
    return;
  }

  public boolean openBinaryOutput(String parmFile) {
    try {
      parmFile = TarokoParm.getInstance().getDataRoot() + "/work/" + parmFile;
      fon = new FileOutputStream(parmFile);
      dos = new DataOutputStream(fon);
      outFile[0] = parmFile;
    } catch (Exception ex) {
      wp.showLogMessage("E", "openBinaryOutput", "FILE : " + parmFile);
      return false;
    }
    return true;
  }

  public boolean writeBinFile(byte[] outData, int len) throws Exception {
    dos.write(outData, 0, len);
    dos.flush();
    writeCnt[0]++;
    return true;
  }

  public void closeBinaryOutput() throws Exception {
    dos.close();
    dos = null;
    return;
  }
  
  /**
   *  Read the first sheet data of Excel file
   * @param input
   * @return
   * @throws IOException
   * @throws InvalidFormatException 
   * @throws EncryptedDocumentException 
   */
  private String readExcel(String file) throws IOException, EncryptedDocumentException, InvalidFormatException {
    StringBuilder result = new StringBuilder(2048);
    try (InputStream input = new FileInputStream(SecurityUtil.verifyPath(file));
        Workbook workbook = WorkbookFactory.create(input)) {
      Sheet sheet = workbook.getSheetAt(0);
      int lastRowNum = sheet.getLastRowNum();
      for (int rowNum = 0; rowNum <= lastRowNum; rowNum++) {
          Row row = sheet.getRow(rowNum);
          int minCellNum = row.getFirstCellNum();
          int maxCellNum = row.getLastCellNum();
          List<String> rowList = new ArrayList<String>();
          for (int i = minCellNum; i < maxCellNum; i++) {
              Cell cell = row.getCell(i);
              if (cell == null) {
                  continue;
              }
              result.append(cell.toString());
              if (i < maxCellNum - 1) {
                result.append(",");
              }
          }
          result.append("\n");
      }
    }
    return result.toString();
}


} // end of class TarokoFileAccess
