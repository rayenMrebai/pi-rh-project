package org.example.services.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.enums.BonusRuleStatus;
import org.example.enums.SalaireStatus;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelExportService {

    private SalaireService salaireService;
    private BonusRuleService bonusRuleService;

    public ExcelExportService() {
        this.salaireService = new SalaireService();
        this.bonusRuleService = new BonusRuleService();
    }

    /**
     * Exporte les salaires vers Excel selon les filtres
     */
    public String exportToExcel(ExportFilter filter, String savePath) {
        try {
            // 1. Récupérer les salaires filtrés
            List<Salaire> salaires = getSalariesWithFilter(filter);

            if (salaires.isEmpty()) {
                System.out.println("⚠️ Aucun salaire à exporter");
                return null;
            }

            // 2. Créer le workbook
            XSSFWorkbook workbook = new XSSFWorkbook();

            // 3. Créer les feuilles
            createSalariesSheet(workbook, salaires, filter);

            if (filter.isInclureStatistiques()) {
                createStatistiquesSheet(workbook, salaires, filter);
            }

            if (filter.isInclureBonus()) {
                createBonusSheet(workbook, salaires);
            }

            // 4. Générer le nom du fichier
            String filename = generateFilename(filter);
            String fullPath = savePath + File.separator + filename;

            // 5. Sauvegarder
            try (FileOutputStream outputStream = new FileOutputStream(fullPath)) {
                workbook.write(outputStream);
            }

            workbook.close();

            System.out.println("✅ Excel exporté : " + fullPath);
            return fullPath;

        } catch (IOException e) {
            System.err.println("❌ Erreur export Excel : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Récupère les salaires selon les filtres
     */
    private List<Salaire> getSalariesWithFilter(ExportFilter filter) {
        List<Salaire> allSalaries = salaireService.getAll();

        // Charger les règles de bonus pour chaque salaire
        for (Salaire salaire : allSalaries) {
            List<BonusRule> rules = bonusRuleService.getRulesBySalaire(salaire.getId());
            salaire.setBonusRules(rules);
        }

        return allSalaries.stream()
                .filter(s -> matchPeriode(s, filter))
                .filter(s -> matchStatus(s, filter))
                .filter(s -> matchUser(s, filter))
                .filter(s -> matchMontant(s, filter))
                .collect(Collectors.toList());
    }

    private boolean matchPeriode(Salaire salaire, ExportFilter filter) {
        LocalDate date = salaire.getDatePaiement();

        switch (filter.getPeriodeType()) {
            case "TOUS":
                return true;

            case "MOIS":
                return date.getMonthValue() == filter.getMois()
                        && date.getYear() == filter.getAnnee();

            case "ANNEE":
                return date.getYear() == filter.getAnnee();

            case "PERSONNALISEE":
                return !date.isBefore(filter.getDateDebut())
                        && !date.isAfter(filter.getDateFin());

            default:
                return true;
        }
    }

    private boolean matchStatus(Salaire salaire, ExportFilter filter) {
        if (filter.getStatusFilters() == null || filter.getStatusFilters().isEmpty()) {
            return true;
        }
        return filter.getStatusFilters().contains(salaire.getStatus());
    }

    private boolean matchUser(Salaire salaire, ExportFilter filter) {
        if (filter.getUserId() == null) {
            return true;
        }
        return salaire.getUser().getId() == filter.getUserId();
    }

    private boolean matchMontant(Salaire salaire, ExportFilter filter) {
        if (filter.getMontantMin() == null) {
            return true;
        }
        return salaire.getTotalAmount() >= filter.getMontantMin();
    }

    /**
     * Crée la feuille Salaires
     */
    private void createSalariesSheet(XSSFWorkbook workbook, List<Salaire> salaires, ExportFilter filter) {
        Sheet sheet = workbook.createSheet("Salaires");

        int rowNum = 0;

        // Titre
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 LISTE DES SALAIRES - " + getPeriodeLabel(filter));

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // Info nombre
        rowNum++;
        Row infoRow = sheet.createRow(rowNum++);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue(salaires.size() + " salaire(s) exporté(s)");
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));

        rowNum++;

        // En-têtes
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Employé", "Email", "Base (DT)", "Bonus (DT)", "Total (DT)", "Statut", "Date Paiement"};

        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Styles pour les données
        CellStyle normalStyle = createNormalStyle(workbook, false);
        CellStyle grayStyle = createNormalStyle(workbook, true);
        CellStyle numberStyle = createNumberStyle(workbook, false);
        CellStyle numberGrayStyle = createNumberStyle(workbook, true);

        // Données
        boolean gray = false;
        for (Salaire salaire : salaires) {
            Row row = sheet.createRow(rowNum++);

            CellStyle dataStyle = gray ? grayStyle : normalStyle;
            CellStyle numStyle = gray ? numberGrayStyle : numberStyle;

            createCell(row, 0, salaire.getId(), dataStyle);
            createCell(row, 1, salaire.getUser().getName(), dataStyle);
            createCell(row, 2, salaire.getUser().getEmail(), dataStyle);
            createCell(row, 3, salaire.getBaseAmount(), numStyle);
            createCell(row, 4, salaire.getBonusAmount(), numStyle);
            createCell(row, 5, salaire.getTotalAmount(), numStyle);

            Cell statusCell = row.createCell(6);
            statusCell.setCellValue(salaire.getStatus().name());
            statusCell.setCellStyle(createStatusStyle(workbook, salaire.getStatus(), gray));

            createCell(row, 7, salaire.getDatePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dataStyle);

            gray = !gray;
        }

        // Ligne totaux
        rowNum++;
        Row totalRow = sheet.createRow(rowNum++);
        CellStyle totalStyle = createTotalStyle(workbook);

        Cell totalLabel = totalRow.createCell(0);
        totalLabel.setCellValue("TOTAL");
        totalLabel.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        Cell totalBase = totalRow.createCell(3);
        totalBase.setCellFormula("SUM(D6:D" + (rowNum - 1) + ")");
        totalBase.setCellStyle(totalStyle);

        Cell totalBonus = totalRow.createCell(4);
        totalBonus.setCellFormula("SUM(E6:E" + (rowNum - 1) + ")");
        totalBonus.setCellStyle(totalStyle);

        Cell totalTotal = totalRow.createCell(5);
        totalTotal.setCellFormula("SUM(F6:F" + (rowNum - 1) + ")");
        totalTotal.setCellStyle(totalStyle);

        // Ligne moyenne
        Row avgRow = sheet.createRow(rowNum++);

        Cell avgLabel = avgRow.createCell(0);
        avgLabel.setCellValue("MOYENNE");
        avgLabel.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        Cell avgBase = avgRow.createCell(3);
        avgBase.setCellFormula("AVERAGE(D6:D" + (rowNum - 2) + ")");
        avgBase.setCellStyle(totalStyle);

        Cell avgBonus = avgRow.createCell(4);
        avgBonus.setCellFormula("AVERAGE(E6:E" + (rowNum - 2) + ")");
        avgBonus.setCellStyle(totalStyle);

        Cell avgTotal = avgRow.createCell(5);
        avgTotal.setCellFormula("AVERAGE(F6:F" + (rowNum - 2) + ")");
        avgTotal.setCellStyle(totalStyle);

        // Auto-size colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Crée la feuille Statistiques
     */
    private void createStatistiquesSheet(XSSFWorkbook workbook, List<Salaire> salaires, ExportFilter filter) {
        Sheet sheet = workbook.createSheet("Statistiques");

        int rowNum = 0;

        // Titre
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📈 STATISTIQUES - " + getPeriodeLabel(filter));

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        rowNum += 2;

        CellStyle labelStyle = workbook.createCellStyle();
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelStyle.setFont(labelFont);

        CellStyle valueStyle = workbook.createCellStyle();
        valueStyle.setAlignment(HorizontalAlignment.RIGHT);

        // Statistiques
        long payes = salaires.stream().filter(s -> s.getStatus() == SalaireStatus.PAYÉ).count();
        long enCours = salaires.stream().filter(s -> s.getStatus() == SalaireStatus.EN_COURS).count();
        long crees = salaires.stream().filter(s -> s.getStatus() == SalaireStatus.CREÉ).count();

        double totalSalaires = salaires.stream().mapToDouble(Salaire::getTotalAmount).sum();
        double moyenneSalaires = salaires.stream().mapToDouble(Salaire::getTotalAmount).average().orElse(0);
        double maxSalaire = salaires.stream().mapToDouble(Salaire::getTotalAmount).max().orElse(0);
        double minSalaire = salaires.stream().mapToDouble(Salaire::getTotalAmount).min().orElse(0);
        double totalBonus = salaires.stream().mapToDouble(Salaire::getBonusAmount).sum();
        double moyenneBonus = salaires.stream().mapToDouble(Salaire::getBonusAmount).average().orElse(0);

        addStatRow(sheet, rowNum++, "Nombre de salaires", salaires.size(), labelStyle, valueStyle);
        rowNum++;

        addStatRow(sheet, rowNum++, "Salaires PAYÉS", (int) payes, labelStyle, valueStyle);
        addStatRow(sheet, rowNum++, "Salaires EN_COURS", (int) enCours, labelStyle, valueStyle);
        addStatRow(sheet, rowNum++, "Salaires CRÉÉS", (int) crees, labelStyle, valueStyle);
        rowNum++;

        addStatRow(sheet, rowNum++, "Salaire total versé", String.format("%.2f DT", totalSalaires), labelStyle, valueStyle);
        addStatRow(sheet, rowNum++, "Salaire moyen", String.format("%.2f DT", moyenneSalaires), labelStyle, valueStyle);
        addStatRow(sheet, rowNum++, "Salaire maximum", String.format("%.2f DT", maxSalaire), labelStyle, valueStyle);
        addStatRow(sheet, rowNum++, "Salaire minimum", String.format("%.2f DT", minSalaire), labelStyle, valueStyle);
        rowNum++;

        addStatRow(sheet, rowNum++, "Total bonus versés", String.format("%.2f DT", totalBonus), labelStyle, valueStyle);
        addStatRow(sheet, rowNum++, "Bonus moyen", String.format("%.2f DT", moyenneBonus), labelStyle, valueStyle);

        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);
    }

    /**
     * Crée la feuille Bonus
     */
    private void createBonusSheet(XSSFWorkbook workbook, List<Salaire> salaires) {
        Sheet sheet = workbook.createSheet("Détails Bonus");

        int rowNum = 0;

        // Titre
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("💰 DÉTAILS DES RÈGLES DE BONUS");

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        rowNum += 2;

        // En-têtes
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Employé", "Règle", "Pourcentage", "Montant (DT)", "Condition", "Statut"};

        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Données
        CellStyle normalStyle = createNormalStyle(workbook, false);
        CellStyle grayStyle = createNormalStyle(workbook, true);
        CellStyle numberStyle = createNumberStyle(workbook, false);
        CellStyle numberGrayStyle = createNumberStyle(workbook, true);

        boolean gray = false;
        for (Salaire salaire : salaires) {
            if (salaire.getBonusRules() != null && !salaire.getBonusRules().isEmpty()) {
                for (BonusRule rule : salaire.getBonusRules()) {
                    Row row = sheet.createRow(rowNum++);

                    CellStyle dataStyle = gray ? grayStyle : normalStyle;
                    CellStyle numStyle = gray ? numberGrayStyle : numberStyle;

                    createCell(row, 0, salaire.getUser().getName(), dataStyle);
                    createCell(row, 1, rule.getNomRegle(), dataStyle);
                    createCell(row, 2, String.format("%.0f%%", rule.getPercentage()), dataStyle);
                    createCell(row, 3, rule.getBonus(), numStyle);
                    createCell(row, 4, rule.getCondition(), dataStyle);
                    createCell(row, 5, rule.getStatus().name(), dataStyle);

                    gray = !gray;
                }
            }
        }

        // Auto-size
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void addStatRow(Sheet sheet, int rowNum, String label, Object value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);
        if (value instanceof Number) {
            valueCell.setCellValue(((Number) value).doubleValue());
        } else {
            valueCell.setCellValue(value.toString());
        }
        valueCell.setCellStyle(valueStyle);
    }

    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createNormalStyle(XSSFWorkbook workbook, boolean gray) {
        CellStyle style = workbook.createCellStyle();

        if (gray) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createNumberStyle(XSSFWorkbook workbook, boolean gray) {
        CellStyle style = createNormalStyle(workbook, gray);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createStatusStyle(XSSFWorkbook workbook, SalaireStatus status, boolean gray) {
        CellStyle style = createNormalStyle(workbook, gray);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);

        switch (status) {
            case PAYÉ:
                style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                break;
            case EN_COURS:
                style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                break;
            case CREÉ:
                style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                break;
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }

    private CellStyle createTotalStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));

        return style;
    }

    private String getPeriodeLabel(ExportFilter filter) {
        switch (filter.getPeriodeType()) {
            case "MOIS":
                String[] mois = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
                return mois[filter.getMois()] + " " + filter.getAnnee();
            case "ANNEE":
                return "Année " + filter.getAnnee();
            case "PERSONNALISEE":
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return "Du " + filter.getDateDebut().format(fmt) + " au " + filter.getDateFin().format(fmt);
            default:
                return "Tous les salaires";
        }
    }

    private String generateFilename(ExportFilter filter) {
        StringBuilder filename = new StringBuilder("salaires_");

        switch (filter.getPeriodeType()) {
            case "MOIS":
                String[] mois = {"", "janvier", "fevrier", "mars", "avril", "mai", "juin",
                        "juillet", "aout", "septembre", "octobre", "novembre", "decembre"};
                filename.append(mois[filter.getMois()]).append("_").append(filter.getAnnee());
                break;
            case "ANNEE":
                filename.append(filter.getAnnee());
                break;
            case "PERSONNALISEE":
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                filename.append(filter.getDateDebut().format(fmt))
                        .append("_au_")
                        .append(filter.getDateFin().format(fmt));
                break;
            default:
                filename.append("complet_").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        }

        filename.append(".xlsx");
        return filename.toString();
    }
}