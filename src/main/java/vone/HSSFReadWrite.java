package vone;

/* ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * File for HSSF testing/examples
 *
 * THIS IS NOT THE MAIN HSSF FILE!! This is a utility for testing functionality.
 * It does contain sample API usage that may be educational to regular API
 * users.
 */
public final class HSSFReadWrite {

	/**
	 * creates an {@link HSSFWorkbook} the specified OS filename.
	 */
	private static HSSFWorkbook readFile(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		try {
			return new HSSFWorkbook(fis);
		} finally {
			fis.close();
		}
	}

	/**
	 * given a filename this outputs a sample sheet with just a set of
	 * rows/cells.
	 */


	/**
	 * Method main
	 *
	 * Given 1 argument takes that as the filename, inputs it and dumps the cell
	 * values/types out to sys.out.<br/>
	 *
	 * given 2 arguments where the second argument is the word "write" and the
	 * first is the filename - writes out a sample (test) spreadsheet see
	 * {@link HSSFReadWrite#testCreateSampleSheet(String)}.<br/>
	 *
	 * given 2 arguments where the first is an input filename and the second an
	 * output filename (not write), attempts to fully read in the spreadsheet
	 * and fully write it out.<br/>
	 *
	 * given 3 arguments where the first is an input filename and the second an
	 * output filename (not write) and the third is "modify1", attempts to read
	 * in the spreadsheet, deletes rows 0-24, 74-99. Changes cell at row 39, col
	 * 3 to "MODIFIED CELL" then writes it out. Hence this is "modify test 1".
	 * If you take the output from the write test, you'll have a valid scenario.
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("At least one argument expected");
			return;
		}

		String fileName = args[0];
		try {
			if (args.length < 2) {

				HSSFWorkbook wb = HSSFReadWrite.readFile(fileName);

				System.out.println("Data dump:\n");

				for (int k = 0; k < wb.getNumberOfSheets(); k++) {
					HSSFSheet sheet = wb.getSheetAt(k);
					int rows = sheet.getPhysicalNumberOfRows();
					System.out.println("Sheet " + k + " \"" + wb.getSheetName(k) + "\" has " + rows + " row(s).");
					for (int r = 0; r < rows; r++) {
						HSSFRow row = sheet.getRow(r);
						if (row == null) {
							continue;
						}

						// int cells = row.getPhysicalNumberOfCells();
						int cells = row.getLastCellNum();
						System.out.println("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
						for (int c = 0; c < cells; c++) {
							HSSFCell cell = row.getCell(c);
							String value = null;
							if (cell == null) {
								System.out.println("CELL col=" + c + " VALUE= Empty");
								continue;
							}

							switch (cell.getCellTypeEnum()) {

							case FORMULA:
								value = "FORMULA value=" + cell.getCellFormula();
								break;

							case NUMERIC:
								value = "NUMERIC value=" + cell.getNumericCellValue();
								break;

							case STRING:
								value = "STRING value=" + cell.getStringCellValue();
								break;

							default:
							}
							System.out.println("CELL col=" + cell.getColumnIndex() + " VALUE=" + value);
						}
					}
				}
				wb.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
