package st.malike.elastic.report.engine.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.io.IOUtils;
import st.malike.elastic.report.engine.exception.JasperGenerationException;
import st.malike.elastic.report.engine.exception.ReportFormatUnkownException;
import st.malike.elastic.report.engine.exception.TemplateNotFoundException;
import st.malike.elastic.report.engine.service.Generator.ReportFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author malike_st
 */
public class GeneratePDFReport implements GenerateReportService {

    /**
     * @param params
     * @param data
     * @param templateFileLocation
     * @param fileName
     * @param reportFormat
     * @return
     * @throws TemplateNotFoundException
     * @throws JasperGenerationException
     * @throws ReportFormatUnkownException
     */
    @Override
    @SuppressWarnings("unchecked")
    public File generateReport(Map params, List data, String templateFileLocation, String fileName,
                               ReportFormat reportFormat) throws TemplateNotFoundException, JasperGenerationException, ReportFormatUnkownException {
        try {
            if (templateFileLocation == null || templateFileLocation.trim().isEmpty()) {
                throw new TemplateNotFoundException("Template file not found");
            }
            if (reportFormat == null) {
                throw new ReportFormatUnkownException("Report format unknown");
            }
            String generatedFileName = fileName + "." + reportFormat.toString().toLowerCase();
            InputStream reportStream = new FileInputStream(new File(templateFileLocation));
            JasperDesign jd = JRXmlLoader.load(reportStream);
            JasperReport jr = JasperCompileManager.compileReport(jd);
            JasperPrint jp = JasperFillManager.fillReport(jr, params, getDatasource(data));
            byte[] dataInByte;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            exportPdf(jp, byteArrayOutputStream, params);
            if (byteArrayOutputStream.size() == 0) {
                return null;
            }
            byteArrayOutputStream.flush();
            dataInByte = byteArrayOutputStream.toByteArray();
            //write file to temp folder
            File reportFile = new File(System.getProperty("java.io.tmpdir") + File.separator + generatedFileName);
            FileOutputStream outputStream = new FileOutputStream(reportFile);
            IOUtils.write(dataInByte, outputStream);
            outputStream.flush();
            return reportFile;
        } catch (JRException e) {
            throw new JasperGenerationException("Error merging template and data :" + e.getMessage());
        } catch (IOException e) {
            throw new TemplateNotFoundException(templateFileLocation + " not found.");
        } catch (Exception e) {
            throw e;
        }
    }

    private JRDataSource getDatasource(List data) {
        if (data != null) {
            return new JRBeanCollectionDataSource(data);
        }
        return new JRBeanCollectionDataSource(new ArrayList());
    }

    private void exportPdf(JasperPrint jp, ByteArrayOutputStream baos, Map params) {
        // Create a JRPdfExporter instance
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
        try {
            exporter.exportReport();
        } catch (JRException e) {

        }
    }

}
