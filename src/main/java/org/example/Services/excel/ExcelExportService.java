package org.example.Services.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.example.model.projet.EmployesDTO;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] SUMMARY_HEADERS = {"Indicateur", "Valeur"};
    private static final String[] PROJECT_HEADERS = {
            "ID", "Nom", "Description", "Début", "Fin", "Statut", "Budget (TND)",
            "Durée (jours)", "Nb Employés", "Alloc totale %", "Alloc moyenne %",
            "Budget/Employé", "Budget/Jour", "Efficacité"
    };
    private static final String[] ASSIGNMENT_HEADERS = {
            "ID Affectation", "Employé", "Rôle", "Allocation %", "Début", "Fin", "Surcharge globale", "Risque"
    };
    private static final String[] SINGLE_PROJECT_DETAILS = {
            "ID Projet", "Nom", "Description", "Date début", "Date fin", "Statut", "Budget (TND)",
            "Durée (jours)", "Nombre d'employés", "Allocation totale %", "Allocation moyenne %",
            "Budget par employé", "Budget par jour"
    };

    private final Map<Integer, String> employeeNameMap = new HashMap<>();
    private Map<Integer, List<ProjectAssignment>> assignmentsByProject;
    private Map<Integer, Double> totalAllocationByEmployee;

    // ==================== POUR L'EXPORT GLOBAL ====================

    public void exportAllData(List<Project> projects, List<ProjectAssignment> assignments,
                              List<EmployesDTO> employees, String filePath) throws Exception {
        // Préparer les maps pour les calculs
        assignmentsByProject = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getProject().getProjectId()));
        totalAllocationByEmployee = assignments.stream()
                .collect(Collectors.groupingBy(
                        ProjectAssignment::getEmployeeId,
                        Collectors.summingDouble(ProjectAssignment::getAllocationRate)
                ));

        for (EmployesDTO emp : employees) {
            employeeNameMap.put(emp.getUserId(), emp.getUsername());
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createExecutiveDashboard(workbook, projects, assignments);
            createProjectsSheet(workbook, projects, assignments);
            createAssignmentsSheet(workbook, assignments, projects);
            createStatsSheet(workbook, projects, assignments);
            autoSizeAllSheets(workbook);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    // ==================== POUR L'EXPORT D'UN SEUL PROJET ====================

    public void exportSingleProject(Project project, List<ProjectAssignment> assignments,
                                    List<EmployesDTO> employees, String filePath) throws Exception {
        // Préparer les maps (nécessaires pour les calculs de surcharge)
        totalAllocationByEmployee = assignments.stream()
                .collect(Collectors.groupingBy(
                        ProjectAssignment::getEmployeeId,
                        Collectors.summingDouble(ProjectAssignment::getAllocationRate)
                ));

        for (EmployesDTO emp : employees) {
            employeeNameMap.put(emp.getUserId(), emp.getUsername());
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Feuille unique : Résumé du projet + Affectations
            createProjectDetailSheet(workbook, project, assignments);

            // Feuille séparée : Statistiques
            createSingleProjectStatsSheet(workbook, assignments);

            autoSizeAllSheets(workbook);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    private void createProjectDetailSheet(XSSFWorkbook workbook, Project project, List<ProjectAssignment> assignments) {
        XSSFSheet sheet = workbook.createSheet("Détail du projet");
        int rowNum = 0;

        // Titre et timestamp
        addTimestampRow(sheet, rowNum++, "Détail du projet : " + project.getName());
        rowNum++; // espace

        // Styles
        CellStyle sectionStyle = workbook.createCellStyle();
        Font sectionFont = workbook.createFont();
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints((short) 14);
        sectionFont.setColor(IndexedColors.WHITE.getIndex());
        sectionStyle.setFont(sectionFont);
        sectionStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(11, 99, 206), new DefaultIndexedColorMap()));
        sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sectionStyle.setBorderBottom(BorderStyle.THIN);
        sectionStyle.setBorderTop(BorderStyle.THIN);
        sectionStyle.setBorderLeft(BorderStyle.THIN);
        sectionStyle.setBorderRight(BorderStyle.THIN);

        CellStyle labelStyle = workbook.createCellStyle();
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelFont.setFontHeightInPoints((short) 12);
        labelStyle.setFont(labelFont);
        labelStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(240, 248, 255), new DefaultIndexedColorMap()));
        labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        labelStyle.setBorderBottom(BorderStyle.THIN);
        labelStyle.setBorderTop(BorderStyle.THIN);
        labelStyle.setBorderLeft(BorderStyle.THIN);
        labelStyle.setBorderRight(BorderStyle.THIN);

        CellStyle valueStyle = workbook.createCellStyle();
        Font valueFont = workbook.createFont();
        valueFont.setFontHeightInPoints((short) 12);
        valueStyle.setFont(valueFont);
        valueStyle.setBorderBottom(BorderStyle.THIN);
        valueStyle.setBorderTop(BorderStyle.THIN);
        valueStyle.setBorderLeft(BorderStyle.THIN);
        valueStyle.setBorderRight(BorderStyle.THIN);
        valueStyle.setWrapText(true);

        // Section 1 : Informations générales
        Row section1Row = sheet.createRow(rowNum++);
        Cell section1Cell = section1Row.createCell(0);
        section1Cell.setCellValue("Informations générales");
        section1Cell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 1));

        String[][] generalInfos = {
                {"ID Projet", String.valueOf(project.getProjectId())},
                {"Nom", project.getName()},
                {"Description", project.getDescription() != null ? project.getDescription() : ""},
                {"Date début", project.getStartDate() != null ? project.getStartDate().format(DATE_FORMATTER) : ""},
                {"Date fin", project.getEndDate() != null ? project.getEndDate().format(DATE_FORMATTER) : ""},
                {"Statut", project.getStatus()},
                {"Budget (TND)", String.format("%.2f", project.getBudget())}
        };

        for (String[] info : generalInfos) {
            Row row = sheet.createRow(rowNum++);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(info[0]);
            labelCell.setCellStyle(labelStyle);
            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(info[1]);
            valueCell.setCellStyle(valueStyle);
        }

        rowNum++; // espace

        // Section 2 : Indicateurs de performance
        Row section2Row = sheet.createRow(rowNum++);
        Cell section2Cell = section2Row.createCell(0);
        section2Cell.setCellValue("Indicateurs de performance");
        section2Cell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 1));

        int nbEmployees = assignments.size();
        double totalAlloc = assignments.stream().mapToInt(ProjectAssignment::getAllocationRate).sum();
        double avgAlloc = nbEmployees == 0 ? 0 : totalAlloc / nbEmployees;
        long duration = 0;
        if (project.getStartDate() != null && project.getEndDate() != null) {
            duration = project.getEndDate().toEpochDay() - project.getStartDate().toEpochDay();
        }
        double budgetPerEmployee = nbEmployees == 0 ? 0 : project.getBudget() / nbEmployees;
        double budgetPerDay = duration == 0 ? 0 : project.getBudget() / duration;

        String[][] indicators = {
                {"Durée (jours)", String.valueOf(duration)},
                {"Nombre d'employés affectés", String.valueOf(nbEmployees)},
                {"Allocation totale (%)", String.format("%.2f", totalAlloc)},
                {"Allocation moyenne (%)", String.format("%.2f", avgAlloc)},
                {"Budget par employé (TND)", String.format("%.2f", budgetPerEmployee)},
                {"Budget par jour (TND)", String.format("%.2f", budgetPerDay)}
        };

        for (String[] ind : indicators) {
            Row row = sheet.createRow(rowNum++);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(ind[0]);
            labelCell.setCellStyle(labelStyle);
            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(ind[1]);
            valueCell.setCellStyle(valueStyle);
        }

        rowNum += 2; // deux lignes d'espace avant le tableau des affectations

        // Section 3 : Tableau des affectations
        Row section3Row = sheet.createRow(rowNum++);
        Cell section3Cell = section3Row.createCell(0);
        section3Cell.setCellValue("Affectations");
        section3Cell.setCellStyle(sectionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 7)); // fusion sur 8 colonnes

        // En-têtes du tableau des affectations
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#0FA36B");
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Employé", "Rôle", "Allocation %", "Début", "Fin", "Allocation totale (%)", "Risque"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Données des affectations
        for (ProjectAssignment a : assignments) {
            Row row = sheet.createRow(rowNum++);
            double totalForEmp = totalAllocationByEmployee.getOrDefault(a.getEmployeeId(), 0.0);
            String overloadRisk;
            if (totalForEmp > 100) overloadRisk = "OVERLOADED";
            else if (totalForEmp >= 80) overloadRisk = "HIGH LOAD";
            else overloadRisk = "NORMAL";

            row.createCell(0).setCellValue(a.getIdAssignment());
            row.createCell(1).setCellValue(employeeNameMap.getOrDefault(a.getEmployeeId(), "Emp " + a.getEmployeeId()));
            row.createCell(2).setCellValue(a.getRole());
            row.createCell(3).setCellValue(a.getAllocationRate());
            row.createCell(4).setCellValue(a.getAssignedFrom() != null ? a.getAssignedFrom().format(DATE_FORMATTER) : "");
            row.createCell(5).setCellValue(a.getAssignedTo() != null ? a.getAssignedTo().format(DATE_FORMATTER) : "");
            row.createCell(6).setCellValue(totalForEmp + "%");
            row.createCell(7).setCellValue(overloadRisk);
        }

        // Mise en forme conditionnelle sur la colonne Allocation %
        if (!assignments.isEmpty()) {
            SheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
            ConditionalFormattingRule rule = cf.createConditionalFormattingRule(ComparisonOperator.GT, "100");
            FontFormatting font = rule.createFontFormatting();
            font.setFontColorIndex(IndexedColors.RED.index);
            font.setFontStyle(true, false);
            CellRangeAddress[] regions = {CellRangeAddress.valueOf("D" + (rowNum - assignments.size()) + ":D" + (rowNum - 1))};
            cf.addConditionalFormatting(regions, rule);
        }

        // Ajuster la largeur des colonnes pour les deux premières sections
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        // Pour le tableau, on auto-size après
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSingleProjectStatsSheet(XSSFWorkbook workbook, List<ProjectAssignment> assignments) {
        XSSFSheet sheet = workbook.createSheet("Statistiques");
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#0B63CE");
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 11);
        dataStyle.setFont(dataFont);

        int rowNum = 0;
        Row header1 = sheet.createRow(rowNum++);
        header1.createCell(0).setCellValue("Rôle");
        header1.createCell(1).setCellValue("Nombre");
        header1.getCell(0).setCellStyle(headerStyle);
        header1.getCell(1).setCellStyle(headerStyle);

        Map<String, Long> roleCount = assignments.stream()
                .collect(Collectors.groupingBy(ProjectAssignment::getRole, Collectors.counting()));
        for (Map.Entry<String, Long> e : roleCount.entrySet()) {
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(e.getValue());
        }

        rowNum++; // espace

        Row header2 = sheet.createRow(rowNum++);
        header2.createCell(0).setCellValue("Tranche allocation");
        header2.createCell(1).setCellValue("Nb employés");
        header2.getCell(0).setCellStyle(headerStyle);
        header2.getCell(1).setCellStyle(headerStyle);

        Map<String, Long> allocBands = assignments.stream()
                .map(ProjectAssignment::getAllocationRate)
                .collect(Collectors.groupingBy(rate -> {
                    if (rate <= 50) return "0-50%";
                    else if (rate <= 80) return "51-80%";
                    else if (rate <= 100) return "81-100%";
                    else return ">100%";
                }, Collectors.counting()));
        for (Map.Entry<String, Long> e : allocBands.entrySet()) {
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(e.getValue());
        }
    }

    // ==================== MÉTHODES COMMUNES ====================

    private void autoSizeAllSheets(XSSFWorkbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(0);
                if (row != null) {
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        sheet.autoSizeColumn(j);
                    }
                }
            }
        }
    }

    private void addTimestampRow(Sheet sheet, int rowNum, String title) {
        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        Row timeRow = sheet.createRow(rowNum + 1);
        Cell timeCell = timeRow.createCell(0);
        timeCell.setCellValue("Généré le : " + LocalDateTime.now().format(TIMESTAMP_FORMATTER) +
                " | Par : HR Management System");
        CellStyle timeStyle = sheet.getWorkbook().createCellStyle();
        Font timeFont = sheet.getWorkbook().createFont();
        timeFont.setItalic(true);
        timeFont.setFontHeightInPoints((short) 10);
        timeStyle.setFont(timeFont);
        timeCell.setCellStyle(timeStyle);
    }

    // Méthodes existantes pour l'export global (à conserver)
    private void createExecutiveDashboard(XSSFWorkbook workbook, List<Project> projects, List<ProjectAssignment> assignments) {
        XSSFSheet sheet = workbook.createSheet("Executive Dashboard");
        int rowNum = 0;
        addTimestampRow(sheet, rowNum++, "Rapport Analytique RH - Tableau de Bord");
        rowNum++;
        rowNum = addGlobalKPIs(sheet, rowNum, projects, assignments);
        rowNum++;
        rowNum = addTopProjectsByBudget(sheet, rowNum, projects);
        rowNum++;
        rowNum = addTopOverloadedEmployees(sheet, rowNum, assignments);
        if (projects.size() > 1) {
            createPieChart(workbook, sheet, projects, rowNum + 2);
        }
    }

    private int addGlobalKPIs(Sheet sheet, int startRow, List<Project> projects, List<ProjectAssignment> assignments) {
        XSSFWorkbook workbook = (XSSFWorkbook) sheet.getWorkbook();
        Row headerRow = sheet.createRow(startRow++);
        headerRow.createCell(0).setCellValue("Indicateur");
        headerRow.createCell(1).setCellValue("Valeur");
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#0B63CE");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);

        double totalBudget = projects.stream().mapToDouble(Project::getBudget).sum();
        double avgDuration = projects.stream()
                .filter(p -> p.getStartDate() != null && p.getEndDate() != null)
                .mapToLong(p -> p.getEndDate().toEpochDay() - p.getStartDate().toEpochDay())
                .average().orElse(0);
        Project mostExpensive = projects.stream().max(Comparator.comparingDouble(Project::getBudget)).orElse(null);
        Project mostEmployees = projects.stream()
                .max(Comparator.comparingInt(p -> assignmentsByProject.getOrDefault(p.getProjectId(), List.of()).size()))
                .orElse(null);
        long completed = projects.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        double percentCompleted = projects.isEmpty() ? 0 : (completed * 100.0 / projects.size());
        double utilization = assignments.stream().mapToInt(ProjectAssignment::getAllocationRate).average().orElse(0);

        addKpiRow(sheet, startRow++, "Budget total (TND)", String.format("%.2f", totalBudget));
        addKpiRow(sheet, startRow++, "Durée moyenne (jours)", String.format("%.1f", avgDuration));
        addKpiRow(sheet, startRow++, "Projet le plus cher", mostExpensive != null ? mostExpensive.getName() : "-");
        addKpiRow(sheet, startRow++, "Projet avec le + d'employés", mostEmployees != null ? mostEmployees.getName() : "-");
        addKpiRow(sheet, startRow++, "Projets terminés (%)", String.format("%.1f%%", percentCompleted));
        addKpiRow(sheet, startRow++, "Taux d'utilisation moyen", String.format("%.1f%%", utilization));

        return startRow;
    }

    private void addKpiRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private int addTopProjectsByBudget(Sheet sheet, int startRow, List<Project> projects) {
        XSSFWorkbook workbook = (XSSFWorkbook) sheet.getWorkbook();
        Row headerRow = sheet.createRow(startRow++);
        headerRow.createCell(0).setCellValue("Top 3 projets par budget");
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#0FA36B");
        headerRow.getCell(0).setCellStyle(headerStyle);

        List<Project> top = projects.stream()
                .sorted(Comparator.comparingDouble(Project::getBudget).reversed())
                .limit(3)
                .toList();
        int r = startRow;
        for (Project p : top) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(p.getName());
            row.createCell(1).setCellValue(p.getBudget());
        }
        return r;
    }

    private int addTopOverloadedEmployees(Sheet sheet, int startRow, List<ProjectAssignment> assignments) {
        XSSFWorkbook workbook = (XSSFWorkbook) sheet.getWorkbook();
        Row headerRow = sheet.createRow(startRow++);
        headerRow.createCell(0).setCellValue("Top 3 employés surchargés");
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#EF4444");
        headerRow.getCell(0).setCellStyle(headerStyle);

        List<Map.Entry<Integer, Double>> overloaded = totalAllocationByEmployee.entrySet().stream()
                .filter(e -> e.getValue() > 100)
                .sorted(Comparator.<Map.Entry<Integer, Double>>comparingDouble(Map.Entry::getValue).reversed())
                .limit(3)
                .toList();

        int r = startRow;
        for (Map.Entry<Integer, Double> e : overloaded) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(employeeNameMap.getOrDefault(e.getKey(), "Emp " + e.getKey()));
            row.createCell(1).setCellValue(e.getValue() + "%");
        }
        return r;
    }

    private void createPieChart(XSSFWorkbook workbook, XSSFSheet sheet, List<Project> projects, int anchorRow) {
        XSSFSheet dataSheet = workbook.createSheet("ChartData");
        Map<String, Long> statusCount = projects.stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));
        int row = 0;
        Row header = dataSheet.createRow(row++);
        header.createCell(0).setCellValue("Statut");
        header.createCell(1).setCellValue("Nombre");
        for (Map.Entry<String, Long> e : statusCount.entrySet()) {
            Row r = dataSheet.createRow(row++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(e.getValue());
        }

        if (row <= 1) return;

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, anchorRow, 5, anchorRow + 15);
        XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(dataSheet,
                new CellRangeAddress(1, row - 1, 0, 0));
        XDDFNumericalDataSource<? extends Number> values = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet,
                new CellRangeAddress(1, row - 1, 1, 1));

        XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
        data.addSeries(categories, values);
        chart.plot(data);
    }

    private void createProjectsSheet(XSSFWorkbook workbook, List<Project> projects, List<ProjectAssignment> allAssignments) {
        XSSFSheet sheet = workbook.createSheet("Projets");
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#0FA36B");
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 11);
        dataStyle.setFont(dataFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < PROJECT_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(PROJECT_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        Row timeRow = sheet.createRow(1);
        timeRow.createCell(0).setCellValue("Données à jour au " + LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        CellStyle timeStyle = workbook.createCellStyle();
        Font timeFont = workbook.createFont();
        timeFont.setItalic(true);
        timeFont.setFontHeightInPoints((short) 10);
        timeStyle.setFont(timeFont);
        timeRow.getCell(0).setCellStyle(timeStyle);

        int rowNum = 2;
        for (Project p : projects) {
            Row row = sheet.createRow(rowNum++);

            List<ProjectAssignment> projAssignments = assignmentsByProject.getOrDefault(p.getProjectId(), List.of());
            int nbEmployees = projAssignments.size();
            double totalAlloc = projAssignments.stream().mapToInt(ProjectAssignment::getAllocationRate).sum();
            double avgAlloc = nbEmployees == 0 ? 0 : totalAlloc / nbEmployees;
            long duration = 0;
            if (p.getStartDate() != null && p.getEndDate() != null) {
                duration = p.getEndDate().toEpochDay() - p.getStartDate().toEpochDay();
            }
            double budgetPerEmployee = nbEmployees == 0 ? 0 : p.getBudget() / nbEmployees;
            double budgetPerDay = duration == 0 ? 0 : p.getBudget() / duration;
            double efficiency = budgetPerDay;

            row.createCell(0).setCellValue(p.getProjectId());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getDescription() != null ? p.getDescription() : "");
            row.createCell(3).setCellValue(p.getStartDate() != null ? p.getStartDate().format(DATE_FORMATTER) : "");
            row.createCell(4).setCellValue(p.getEndDate() != null ? p.getEndDate().format(DATE_FORMATTER) : "");
            row.createCell(5).setCellValue(p.getStatus());
            row.createCell(6).setCellValue(p.getBudget());
            row.createCell(7).setCellValue(duration);
            row.createCell(8).setCellValue(nbEmployees);
            row.createCell(9).setCellValue(totalAlloc);
            row.createCell(10).setCellValue(avgAlloc);
            row.createCell(11).setCellValue(budgetPerEmployee);
            row.createCell(12).setCellValue(budgetPerDay);
            row.createCell(13).setCellValue(efficiency);
        }

        if (rowNum > 2) {
            SheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
            ConditionalFormattingRule rule1 = cf.createConditionalFormattingRule(ComparisonOperator.LT, "1000");
            FontFormatting font1 = rule1.createFontFormatting();
            font1.setFontColorIndex(IndexedColors.ORANGE.index);
            font1.setFontStyle(true, false);
            CellRangeAddress[] regions = {CellRangeAddress.valueOf("L2:L" + (rowNum - 1))};
            cf.addConditionalFormatting(regions, rule1);

            ConditionalFormattingRule rule2 = cf.createConditionalFormattingRule(ComparisonOperator.LT, "7");
            FontFormatting font2 = rule2.createFontFormatting();
            font2.setFontColorIndex(IndexedColors.YELLOW.index);
            CellRangeAddress[] regions2 = {CellRangeAddress.valueOf("H2:H" + (rowNum - 1))};
            cf.addConditionalFormatting(regions2, rule2);
        }
    }

    private void createAssignmentsSheet(XSSFWorkbook workbook, List<ProjectAssignment> assignments, List<Project> projects) {
        XSSFSheet sheet = workbook.createSheet("Affectations");
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#0B63CE");
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 11);
        dataStyle.setFont(dataFont);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID Affectation", "Projet", "Employé", "Rôle", "Allocation %", "Début", "Fin", "Allocation totale (%)", "Risque"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Map<Integer, String> projectNameMap = projects.stream()
                .collect(Collectors.toMap(Project::getProjectId, Project::getName));

        int rowNum = 1;
        for (ProjectAssignment a : assignments) {
            Row row = sheet.createRow(rowNum++);
            double totalForEmp = totalAllocationByEmployee.getOrDefault(a.getEmployeeId(), 0.0);
            String overloadRisk;
            if (totalForEmp > 100) overloadRisk = "OVERLOADED";
            else if (totalForEmp >= 80) overloadRisk = "HIGH LOAD";
            else overloadRisk = "NORMAL";

            row.createCell(0).setCellValue(a.getIdAssignment());
            row.createCell(1).setCellValue(projectNameMap.getOrDefault(a.getProject().getProjectId(), "Inconnu"));
            row.createCell(2).setCellValue(employeeNameMap.getOrDefault(a.getEmployeeId(), "Emp " + a.getEmployeeId()));
            row.createCell(3).setCellValue(a.getRole());
            row.createCell(4).setCellValue(a.getAllocationRate());
            row.createCell(5).setCellValue(a.getAssignedFrom() != null ? a.getAssignedFrom().format(DATE_FORMATTER) : "");
            row.createCell(6).setCellValue(a.getAssignedTo() != null ? a.getAssignedTo().format(DATE_FORMATTER) : "");
            row.createCell(7).setCellValue(totalForEmp + "%");
            row.createCell(8).setCellValue(overloadRisk);
        }

        if (rowNum > 1) {
            SheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
            ConditionalFormattingRule rule = cf.createConditionalFormattingRule(ComparisonOperator.GT, "100");
            FontFormatting font = rule.createFontFormatting();
            font.setFontColorIndex(IndexedColors.RED.index);
            font.setFontStyle(true, false);
            CellRangeAddress[] regions = {CellRangeAddress.valueOf("E1:E" + (rowNum - 1))};
            cf.addConditionalFormatting(regions, rule);
        }
    }

    private void createStatsSheet(XSSFWorkbook workbook, List<Project> projects, List<ProjectAssignment> assignments) {
        XSSFSheet sheet = workbook.createSheet("Statistiques");
        CellStyle headerStyle = createHeaderStyle(workbook, IndexedColors.WHITE.getIndex(), "#0FA36B");
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 11);
        dataStyle.setFont(dataFont);

        int rowNum = 0;
        Row header1 = sheet.createRow(rowNum++);
        header1.createCell(0).setCellValue("Statut");
        header1.createCell(1).setCellValue("Nombre");
        header1.getCell(0).setCellStyle(headerStyle);
        header1.getCell(1).setCellStyle(headerStyle);

        Map<String, Long> statusCount = projects.stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));
        for (Map.Entry<String, Long> e : statusCount.entrySet()) {
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(e.getValue());
        }

        rowNum++;
        Row header2 = sheet.createRow(rowNum++);
        header2.createCell(0).setCellValue("Mois de début");
        header2.createCell(1).setCellValue("Nombre");
        header2.getCell(0).setCellStyle(headerStyle);
        header2.getCell(1).setCellStyle(headerStyle);

        Map<String, Long> monthCount = projects.stream()
                .filter(p -> p.getStartDate() != null)
                .collect(Collectors.groupingBy(p -> p.getStartDate().getMonth().toString(), Collectors.counting()));
        for (Map.Entry<String, Long> e : monthCount.entrySet()) {
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(e.getValue());
        }

        rowNum++;
        Row header3 = sheet.createRow(rowNum++);
        header3.createCell(0).setCellValue("Tranche allocation");
        header3.createCell(1).setCellValue("Nb employés");
        header3.getCell(0).setCellStyle(headerStyle);
        header3.getCell(1).setCellStyle(headerStyle);

        Map<String, Long> allocBands = assignments.stream()
                .map(ProjectAssignment::getAllocationRate)
                .collect(Collectors.groupingBy(rate -> {
                    if (rate <= 50) return "0-50%";
                    else if (rate <= 80) return "51-80%";
                    else if (rate <= 100) return "81-100%";
                    else return ">100%";
                }, Collectors.counting()));
        for (Map.Entry<String, Long> e : allocBands.entrySet()) {
            Row r = sheet.createRow(rowNum++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(e.getValue());
        }
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook, short fontColor, String bgColorHex) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(fontColor);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        java.awt.Color awtColor = java.awt.Color.decode(bgColorHex);
        XSSFColor xssfColor = new XSSFColor(awtColor, new DefaultIndexedColorMap());
        style.setFillForegroundColor(xssfColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}