/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
 *  109/07/22  V1.00.01    Zuwei     coding standard, rename field method & format                   *
*  109/09/04  V1.00.06    Zuwei     code scan issue    
* 111-01-19  V1.00.03  Justin       fix J2EE Bad Practices: Leftover Debug Code
* 111-01-21  V1.00.04  Justin       fix Redundant Null Check
******************************************************************************/
package dbTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Dxc.Util.SecurityUtil;

public class DbTool {

    public DbTool() {
        // TODO Auto-generated constructor stub
    }


    public static void main(String[] args) throws IOException {

        // 加密檔案 參數有三個 => A C:\DXC\QrCodeGateay\parm\jdbc.properties
        // C:\DXC\QrCodeGateay\parm\jdbc.properties.en
        // 參數1=> A:加密檔案,
        // 參數2=> 明碼檔案名稱(含 path)
        // 參數3=> 加密後的檔案名稱(含 path)

        // 解密 args => B C:\DXC\QrCodeGateay\parm\jdbc.properties.en
        // C:\DXC\QrCodeGateay\parm\jdbc.properties
        // 參數1=> B:解密檔案
        // 參數2=> 加密的檔案名稱(含 path)
        // 參數3=> 解密後的明碼檔案名稱(含 path)
        // 範例指令=> C:\Howard\Tools\openjdk\jdk-11\bin\java -cp banktools.jar dbTools.DbTool B
        // C:\DXC\QrCodeGateay\parm\jdbc.properties.en C:\DXC\QrCodeGateay\parm\jdbc.properties

        if (args.length != 3) {
            System.out.println("參數錯誤!");
            System.out.println("若要加密檔案，則參數有三個：");
            System.out.println("參數1=> A");
            System.out.println("參數2=> 明碼檔案名稱(含 path)");
            System.out.println("參數3=> 加密後的檔案名稱(含 path)");
            System.out.println("若要解密檔案，則參數有三個：");
            System.out.println("參數1=> B");
            System.out.println("參數2=> 加密的檔案名稱(含 path)");
            System.out.println("參數3=> 解密後的明碼檔案名稱(含 path)");
            return;
        }
        if (args[0].equals("A")) {
            String sLSourceFile = args[1];
            String sLTargetFileName = args[2];
            encryptedFile(sLSourceFile, sLTargetFileName);
            System.out.println("===================");
            System.out.println("加密完成! 加密後的檔案是 " + sLTargetFileName);

        } else if (args[0].equals("B")) {
            String sLSourceFile = args[1];
            String sLTargetFileName = args[2];
            decryptedFile(sLSourceFile, sLTargetFileName);
            System.out.println("===================");
            System.out.println("解密完成! 解密後的檔案是 " + sLTargetFileName);
        }
        // TODO Auto-generated method stub

        /*
         * String sL_Str1 = "jdbc.username=username"; String sL_Str2 = "jdbc.password=xxxyyzz";
         * 
         * System.out.println(Utils.encodedString(sL_Str1));
         * System.out.println(Utils.encodedString(sL_Str2));
         * 
         * String sL_ProjPath = "C:\\DXC\\QrCodeGateay"; //String sL_ProjPath = "C:\\DXC\\ICash";
         * 
         * String sP_SourceFile= sL_ProjPath + "\\parm\\jdbc.properties"; String sP_TargetFileName=
         * sL_ProjPath + "\\parm\\text.txt"; encryptedFile(sP_SourceFile, sP_TargetFileName);
         * 
         * 
         * String sP_TargetFileName2= sL_ProjPath + "\\parm\\text2.txt";
         * decryptedFile(sP_TargetFileName, sP_TargetFileName2);
         * 
         * System.out.println("ok..");
         */
    }

    public static int encryptedFile(String sPSourceFile, String sPTargetFileName)
            throws IOException {
        int nLResult = 0;
        sPSourceFile = SecurityUtil.verifyPath(sPSourceFile);
        sPTargetFileName = SecurityUtil.verifyPath(sPTargetFileName);
        Path lSrcFilePath = Paths.get(sPSourceFile);
        if (!Files.exists(lSrcFilePath)) {

            return -1;
        }



        Path lTargetFilePath = Paths.get(sPTargetFileName);
        if (Files.exists(lTargetFilePath)) {
            Files.delete(lTargetFilePath);

        }

        

        Path lSourceFilePath = Paths.get(sPSourceFile);



        try (BufferedWriter lWriter = Files.newBufferedWriter(lTargetFilePath, StandardCharsets.UTF_8);
        	 BufferedReader br = Files.newBufferedReader(lSourceFilePath, StandardCharsets.UTF_8);) {

            String sLSrcContent = "", sLTargetContent = "";
            while ((sLSrcContent = br.readLine()) != null) {
                sLTargetContent = Utils.encodedString(sLSrcContent);
                lWriter.write(sLTargetContent);
                lWriter.newLine();
            }

        } catch (IOException e) {
            nLResult = -2;
            e.printStackTrace();

        }

        return nLResult;
    }

    public static int encryptedFileOld(String sPSourceFile, String sPTargetFileName)
            throws IOException {
        int nLResult = 0;

        Path lSrcFilePath = Paths.get(sPSourceFile);
        if (!Files.exists(lSrcFilePath)) {

            return -1;
        }



        Path lTargetFilePath = Paths.get(sPTargetFileName);
        if (Files.exists(lTargetFilePath)) {
            Files.delete(lTargetFilePath);

        }

        


        try (BufferedWriter lWriter = Files.newBufferedWriter(lTargetFilePath);
        	 BufferedReader br = new BufferedReader(new FileReader(sPSourceFile))) {

            String sLSrcContent = "", sLTargetContent = "";
            while ((sLSrcContent = br.readLine()) != null) {
                sLTargetContent = Utils.encodedString(sLSrcContent);
                lWriter.write(sLTargetContent);
                lWriter.newLine();
            }

        } catch (IOException e) {
            nLResult = -2;
            e.printStackTrace();
        }
        return nLResult;
    }

    public static int decryptedFile(String sPSourceFile, String sPTargetFileName)
            throws IOException {
        int nLResult = 0;

        sPSourceFile = SecurityUtil.verifyPath(sPSourceFile);
        Path lSrcFilePath = Paths.get(sPSourceFile);
        if (!Files.exists(lSrcFilePath)) {

            return -1;
        }

        sPTargetFileName = SecurityUtil.verifyPath(sPTargetFileName);
        Path lTargetFilePath = Paths.get(sPTargetFileName);
        if (Files.exists(lTargetFilePath)) {
            Files.delete(lTargetFilePath);

        }

        

        Path lSourceFilePath = Paths.get(sPSourceFile);



        try (BufferedWriter lWriter = Files.newBufferedWriter(lTargetFilePath, StandardCharsets.UTF_8);
        	 BufferedReader br = Files.newBufferedReader(lSourceFilePath, StandardCharsets.UTF_8);) {

            String sLSrcContent = "", sLTargetContent = "";
            while ((sLSrcContent = br.readLine()) != null) {
                sLTargetContent = Utils.decodedString(sLSrcContent);
                lWriter.write(sLTargetContent);
                lWriter.newLine();
            }

        } catch (IOException e) {
            nLResult = -2;
            e.printStackTrace();

        }

        return nLResult;
    }

    public static int decryptedFileOld(String sPSourceFile, String sPTargetFileName)
            throws IOException {
        int nLResult = 0;

        Path lSrcFilePath = Paths.get(sPSourceFile);
        if (!Files.exists(lSrcFilePath)) {

            return -1;
        }

        Path lTargetFilePath = Paths.get(sPTargetFileName);
        if (Files.exists(lTargetFilePath)) {
            Files.delete(lTargetFilePath);

        }

        
        try (BufferedWriter lWriter = Files.newBufferedWriter(lTargetFilePath);
        	 BufferedReader br = new BufferedReader(new FileReader(sPSourceFile)); ) {

            String sLSrcContent = "", sLTargetContent = "";
            while ((sLSrcContent = br.readLine()) != null) {
                sLTargetContent = Utils.decodedString(sLSrcContent);
                lWriter.write(sLTargetContent);
                lWriter.newLine();
            }

        } catch (IOException e) {
            nLResult = -2;
            e.printStackTrace();
        }

        return nLResult;
    }

}
