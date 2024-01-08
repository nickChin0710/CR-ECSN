/**
 * 檔案公用程式 V.2018-0720
 * 2018-0720:	JH		initial
 * 2020-0615:	Yanghan		fix coding scan issue
*  109/07/06  V0.00.02    Zuwei     coding standard, rename field method & format                   * 
*  109/09/04  V1.00.06    Zuwei     code scan issue   
*  111/01/19  V1.00.07    Justin    fix Unchecked Return Value 
*  111-01-21  V1.00.08    Justin    fix Redundant Null Check
 * */
package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.Normalizer;

import org.apache.commons.io.FileUtils;

import Dxc.Util.SecurityUtil;

public class CommFile {

  public boolean fileCopy(String src, String target) {
    try {
        File fs = new File(SecurityUtil.verifyPath(src));
        File ft = new File(SecurityUtil.verifyPath(target));
        boolean result = ft.getParentFile().mkdirs();
        if (result == false) {
    		if (ft.getParentFile().exists() == false) {
    			System.out.println("Fail to create directories");
    		}
    	  }
      FileUtils.copyFile(fs, ft);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  // =*****************************************************************************
  public boolean fileExist(String file1) {
    File file2 = new File(SecurityUtil.verifyPath(file1));
    return file2.exists();
  }

  // =*****************************************************************************
  public boolean fileMerge(String src, String target) throws IOException {
    // second way
//    FileChannel in = null;
//    FileChannel out = null;
    // 2020_0615 resolve Unreleased Resource: Streams by yanghan
    try (
            FileInputStream inStream = new FileInputStream(SecurityUtil.verifyPath(src));
            FileOutputStream outStream = new FileOutputStream(SecurityUtil.verifyPath(target), true);
        	FileChannel in = inStream.getChannel();
        	FileChannel out = outStream.getChannel();) {
      out.position(out.size());
      in.transferTo(0, in.size(), out);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  // =*****************************************************************************
  public boolean fileDelete(String src) {
    File fs = new File(SecurityUtil.verifyPath(src));
    return fs.delete();
  }

  // =*****************************************************************************
  public boolean fileRename(String src, String target) {
	    File fs = new File(SecurityUtil.verifyPath(src));
	    File ft = new File(SecurityUtil.verifyPath(target));
	    boolean result = ft.getParentFile().mkdirs();
	    if (result == false) {
			if (ft.getParentFile().exists() == false) {
				System.out.println("Fail to create directories");
			}
		  }
    return fs.renameTo(ft);
  }

  // =*****************************************************************************
  public void chmod777(String file) {
    // the first way
    File f = new File(SecurityUtil.verifyPath(file));
    f.setExecutable(true, false);
    f.setReadable(true, false);
    f.setWritable(true, false);
    // the second way
    /*
     * Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>(); //add owners permission
     * perms.add(PosixFilePermission.OWNER_READ); perms.add(PosixFilePermission.OWNER_WRITE);
     * perms.add(PosixFilePermission.OWNER_EXECUTE); //add group permissions
     * perms.add(PosixFilePermission.GROUP_READ); perms.add(PosixFilePermission.GROUP_WRITE);
     * perms.add(PosixFilePermission.GROUP_EXECUTE); //add others permissions
     * perms.add(PosixFilePermission.OTHERS_READ); perms.add(PosixFilePermission.OTHERS_WRITE);
     * perms.add(PosixFilePermission.OTHERS_EXECUTE);
     * 
     * Files.setPosixFilePermissions(Paths.get(file), perms);
     */
  }


}
