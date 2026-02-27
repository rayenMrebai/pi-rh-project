package org.example.Services.pdf;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(11, 99, 206); // #0B63CE
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(15, 163, 107); // #0FA36B
    private static final DeviceRgb BACKGROUND_COLOR = new DeviceRgb(248, 250, 252); // #f8fafc
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void exportAllProjects(List<Project> projects, List<ProjectAssignment> allAssignments, String destPath, String logoPath) throws Exception {
        PdfWriter writer = new PdfWriter(new FileOutputStream(destPath));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        addHeader(document, logoPath, "Rapport complet des projets");
        document.add(new Paragraph("Liste des projets").setFontSize(16).setBold().setFontColor(PRIMARY_COLOR).setMarginTop(20));
        document.add(createProjectTable(projects));

        for (Project p : projects) {
            document.add(new Paragraph("Projet : " + p.getName())
                    .setFontSize(14).setBold().setFontColor(SECONDARY_COLOR).setMarginTop(15));
            List<ProjectAssignment> assignments = allAssignments.stream()
                    .filter(a -> a.getProject().getProjectId() == p.getProjectId())
                    .toList();
            if (assignments.isEmpty()) {
                document.add(new Paragraph("Aucune affectation pour ce projet.").setItalic());
            } else {
                document.add(createAssignmentTable(assignments));
            }
        }
        document.close();
    }

    public void exportSingleProject(Project project, List<ProjectAssignment> assignments, String destPath, String logoPath) throws Exception {
        PdfWriter writer = new PdfWriter(new FileOutputStream(destPath));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        addHeader(document, logoPath, "Détail du projet");
        document.add(new Paragraph("Informations générales").setFontSize(16).setBold().setFontColor(PRIMARY_COLOR).setMarginTop(20));
        document.add(createProjectDetailsTable(project));
        document.add(new Paragraph("Affectations").setFontSize(16).setBold().setFontColor(PRIMARY_COLOR).setMarginTop(20));
        if (assignments.isEmpty()) {
            document.add(new Paragraph("Aucune affectation pour ce projet.").setItalic());
        } else {
            document.add(createAssignmentTable(assignments));
        }
        document.close();
    }

    private void addHeader(Document document, String logoPath, String title) {
        try {
            Image logo = new Image(ImageDataFactory.create(logoPath));
            logo.scaleToFit(80, 40);
            logo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            document.add(logo);
        } catch (Exception ignored) {}
        document.add(new Paragraph(title)
                .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER)
                .setFontColor(PRIMARY_COLOR).setMarginTop(10).setMarginBottom(20));
    }

    private Table createProjectTable(List<Project> projects) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 25, 15, 15, 15, 20}));
        table.setWidth(UnitValue.createPercentValue(100));
        String[] headers = {"ID", "Nom", "Début", "Fin", "Statut", "Budget (TND)"};
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE));
            cell.setBackgroundColor(PRIMARY_COLOR).setTextAlignment(TextAlignment.CENTER).setPadding(5);
            table.addCell(cell);
        }
        for (Project p : projects) {
            table.addCell(createCell(String.valueOf(p.getProjectId()), true));
            table.addCell(createCell(p.getName(), false));
            table.addCell(createCell(p.getStartDate() != null ? p.getStartDate().format(DATE_FORMATTER) : "", false));
            table.addCell(createCell(p.getEndDate() != null ? p.getEndDate().format(DATE_FORMATTER) : "", false));
            table.addCell(createCell(p.getStatus(), false));
            table.addCell(createCell(String.format("%.2f", p.getBudget()), false));
        }
        return table;
    }

    private Table createAssignmentTable(List<ProjectAssignment> assignments) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 20, 25, 15, 15, 15}));
        table.setWidth(UnitValue.createPercentValue(100));
        String[] headers = {"ID", "Employé", "Rôle", "Allocation", "Début", "Fin"};
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE));
            cell.setBackgroundColor(SECONDARY_COLOR).setTextAlignment(TextAlignment.CENTER).setPadding(5);
            table.addCell(cell);
        }
        for (ProjectAssignment a : assignments) {
            table.addCell(createCell(String.valueOf(a.getIdAssignment()), true));
            table.addCell(createCell("Emp " + a.getEmployeeId(), false));
            table.addCell(createCell(a.getRole(), false));
            table.addCell(createCell(a.getAllocationRate() + "%", false));
            table.addCell(createCell(a.getAssignedFrom() != null ? a.getAssignedFrom().format(DATE_FORMATTER) : "", false));
            table.addCell(createCell(a.getAssignedTo() != null ? a.getAssignedTo().format(DATE_FORMATTER) : "", false));
        }
        return table;
    }

    private Table createProjectDetailsTable(Project p) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));
        addDetailRow(table, "ID du projet", String.valueOf(p.getProjectId()));
        addDetailRow(table, "Nom", p.getName());
        addDetailRow(table, "Description", p.getDescription());
        addDetailRow(table, "Date de début", p.getStartDate() != null ? p.getStartDate().format(DATE_FORMATTER) : "");
        addDetailRow(table, "Date de fin", p.getEndDate() != null ? p.getEndDate().format(DATE_FORMATTER) : "");
        addDetailRow(table, "Statut", p.getStatus());
        addDetailRow(table, "Budget (TND)", String.format("%.2f", p.getBudget()));
        return table;
    }

    private void addDetailRow(Table table, String label, String value) {
        Cell cellLabel = new Cell().add(new Paragraph(label).setBold());
        cellLabel.setBackgroundColor(BACKGROUND_COLOR).setPadding(5);
        Cell cellValue = new Cell().add(new Paragraph(value)).setPadding(5);
        table.addCell(cellLabel);
        table.addCell(cellValue);
    }

    private Cell createCell(String text, boolean bold) {
        Paragraph p = new Paragraph(text);
        if (bold) p.setBold();
        return new Cell().add(p).setPadding(5).setTextAlignment(TextAlignment.CENTER);
    }
}