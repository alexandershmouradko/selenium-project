package uk.ndc.csa.utilities.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHelper {
	
	public static void zipit(String sourcePath, String targetPath) throws IOException {

	FileOutputStream fos = new FileOutputStream(targetPath);
	ZipOutputStream zipOut = new ZipOutputStream(fos);

	File fileToZip = new File(sourcePath);

	zip(fileToZip, fileToZip.getName(), zipOut);
	zipOut.close();
	fos.close();
	}

private static void zip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

	if (fileToZip.isHidden()) {
		return;
	}
	if (fileToZip.isDirectory()) {
		File[] files = fileToZip.listFiles();
		for (File file : files) {
			zip(file, fileName + "/" + file.getName(), zipOut);
		}
		return;
	}
	FileInputStream fis = new FileInputStream(fileToZip);
	ZipEntry zipEntry = new ZipEntry(fileName);
	zipOut.putNextEntry(zipEntry);
	byte[] bytes = new byte[1024];
	int length;
	while ((length = fis.read(bytes)) >= 0) {
		zipOut.write(bytes, 0, length);
	}
	fis.close();
}
}



