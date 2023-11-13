package com.example.demo.controller;


import com.example.demo.tables.Products;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(path = "api/v1/schedule")
@AllArgsConstructor
public class ScheduleCronController {
    private final Resource templateResource = new ClassPathResource("templates/productsheet.docx");
    private final String outputDirectory = "templateoutput";

    @GetMapping("/generateProductSheet")
    public void generateProductSheet(HttpServletResponse response){
        try{
            if (!outputDirectoryExists()) {
                createOutputDirectory();
            }
            InputStream templateStream = templateResource.getInputStream();
            XWPFDocument templateDoc = new XWPFDocument(templateStream);
            List<Products> products = Arrays.asList(
                    new Products(1L, 10, 25.0, 15.0, "Electronics", 20.0, "Laptop"),
                    new Products(2L, 20, 10.0, 5.0, "Clothing", 8.0, "T-Shirt"),
                    new Products(3L, 5, 50.0, 35.0, "Electronics", 45.0, "Smartphone")
            );

            XWPFTable table = findTableWithPlaceholders(templateDoc);

            if (table != null) {
                int placeholderRowIndex = findPlaceholderRowIndex(table);
                int rowIdx = 1;

                for (Products product : products) {
                    XWPFTableRow newRow = table.createRow();
                    replacePlaceholdersInRow(newRow, product);
                    rowIdx++;
                }
                table.removeRow(0);
            }
            String outputPath = "src/main/resources/" + outputDirectory + "/output.docx";
            FileOutputStream output = new FileOutputStream(outputPath);
            templateDoc.write(output);
            output.close();
            InputStream outputDocument = new FileInputStream(outputPath);
            FileCopyUtils.copy(outputDocument, response.getOutputStream());

            response.setHeader("Content-Disposition", "attachment; filename=output.docx");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private boolean outputDirectoryExists() {
        Resource outputResource = new ClassPathResource(outputDirectory);
        return outputResource.exists();
    }
    private void createOutputDirectory() throws IOException {
        File directory = new File(outputDirectory);
        if (!directory.exists() && directory.mkdir()) {
            System.out.println("Created directory: " + directory.getAbsolutePath());
        }
    }

    private XWPFTable findTableWithPlaceholders(XWPFDocument document) {
        for (XWPFTable table : document.getTables()) {
            if (table.getRows().size() > 0) {
                return table;
            }
        }
        return null;
    }

    private int findPlaceholderRowIndex(XWPFTable table) {
        for (int i = 0; i < table.getRows().size(); i++) {
            XWPFTableRow row = table.getRow(i);
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    for (XWPFRun run : paragraph.getRuns()) {
                        String text = run.getText(0);
                        if (text != null && text.contains("{id}")) {
                            return i;
                        }
                    }
                }
            }
        }
        return 1;
    }

    private void replacePlaceholdersInRow(XWPFTableRow row, Products product) {
        List<XWPFTableCell> cells = row.getTableCells();
        cells.get(0).setText(String.valueOf(product.getId()));
        cells.get(1).setText(String.valueOf(product.getAvailableQuantity()));
        cells.get(2).setText(String.valueOf(product.getBuyingPrice()));
        cells.get(3).setText(String.valueOf(product.getSellingPrice()));
        cells.get(4).setText(String.valueOf(product.getSellingPriceAtDebt()));
        cells.get(5).setText(product.getItem());
        cells.get(6).setText(product.getCategory());
    }

}
