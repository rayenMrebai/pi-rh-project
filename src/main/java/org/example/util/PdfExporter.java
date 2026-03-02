package org.example.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.model.user.UserAccount;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void exportUsersToPdf(List<UserAccount> users, String filePath) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
        Paragraph title = new Paragraph("Liste des utilisateurs", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        float[] columnWidths = {1f, 2f, 3f, 2f, 2f, 3f, 3f};
        table.setWidths(columnWidths);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        String[] headers = {"ID", "Nom d'utilisateur", "Email", "Rôle", "Statut", "Créé le", "Dernière connexion"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (UserAccount user : users) {
            table.addCell(createCell(String.valueOf(user.getUserId()), cellFont));
            table.addCell(createCell(user.getUsername(), cellFont));
            table.addCell(createCell(user.getEmail(), cellFont));
            table.addCell(createCell(user.getRole().toString(), cellFont));
            table.addCell(createCell(user.getAccountStatus(), cellFont));
            table.addCell(createCell(user.getAccountCreatedDate().format(DATE_FORMATTER), cellFont));
            String lastLogin = user.getLastLogin() != null ? user.getLastLogin().format(DATE_FORMATTER) : "Jamais";
            table.addCell(createCell(lastLogin, cellFont));
        }

        document.add(table);
        document.close();
    }

    private static PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }
}