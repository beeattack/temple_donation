package com.jdea.donationinfo.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jdea.donationinfo.model.DonationInfo;
import com.jdea.donationinfo.repository.DonationInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;

@Service
public class ReportService {

    public byte[] exportReport(List<DonationInfo> data, String reportFormat) throws FileNotFoundException, JRException {
        byte[] report = null;
        //load file and compile it
        // File file = ResourceUtils.getFile("classpath:donation_certificate.jrxml");
        try (InputStream in = getClass().getResourceAsStream("/donation_certificate.jrxml")) {
            JasperReport jasperReport = JasperCompileManager.compileReport(in);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Java Techie");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            if (reportFormat.equalsIgnoreCase("html")) {
                JasperExportManager.exportReportToHtmlFile(jasperPrint, "./donation.html");
            }
            if (reportFormat.equalsIgnoreCase("pdf")) {
                JasperExportManager.exportReportToPdfFile(jasperPrint, "./donation.pdf");
            }

            report = JasperExportManager.exportReportToPdf(jasperPrint);
        }
        catch(Exception ex){
            //
        }
        return report;
        
    }

    public byte[] generatePDFReport(List<DonationInfo> data) {
        byte[] bytes = null;
        JasperReport jasperReport = null;
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "Java Techie");
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            // Check if a compiled report exists
            String inputFileName = "donation_certificate";
            if (this.jasperFileExists(inputFileName)) {
                jasperReport = (JasperReport) JRLoader.loadObject(this.loadJasperFile(inputFileName));
            }
            // Compile report from source and save
            else {
                String jrxml = this.loadJrxmlFile(inputFileName);
                jasperReport = JasperCompileManager.compileReport(jrxml);
                // Save compiled report. Compiled report is loaded next time
                JRSaver.saveObject(jasperReport, this.loadJasperFile(inputFileName));
            }
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            // return the PDF in bytes
            bytes = JasperExportManager.exportReportToPdf(jasperPrint);
        }
        catch (JRException | IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public boolean jrxmlFileExists(String file) {
        // @formatter:off
        try {
            //Path reportFile = Paths.get(reportPath); //Paths.get(properties.getReportLocation().getURI());
            Path reportFile = Path.of("").toAbsolutePath();
            reportFile = reportFile.resolve(file + ".jrxml");
            if (Files.exists(reportFile))
                return true;
        } catch (Exception e) {
            //log.error("Error while trying to get file URI", e);
            return false;
        }
        // @formatter:on
        return false;
    }

    public boolean jasperFileExists(String file) {
        Path reportFile = Path.of("").toAbsolutePath();
        reportFile = reportFile.resolve(file + ".jasper");
        if (Files.exists(reportFile))
            return true;
        return false;
    }

    public File loadJasperFile(String file) {
        Path reportFile = Path.of("").toAbsolutePath();
        reportFile = reportFile.resolve(file + ".jasper");
        return reportFile.toFile();
    }

    public String loadJrxmlFile(String file) {
        // @formatter:off
        try {
            //Path reportFile = Paths.get(reportPath); //Paths.get(properties.getReportLocation().getURI());
            Path reportFile = Path.of("").toAbsolutePath();
            reportFile = reportFile.resolve(file + ".jrxml");
            return reportFile.toString();
        } catch (Exception e) {
            //log.error("Error while trying to get file prefix", e);
            System.out.println(e.getMessage());
            throw new StorageFileNotFoundException("Could not load file", e);
        }
        // @formatter:on
    }
}
