package mu.rekolt.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import mu.rekolt.model.Delivery;
import mu.rekolt.model.Member;
import mu.rekolt.util.Format;
import mu.rekolt.util.RoundMoney;

//  Writes the season report as a Word document with Apache POI (poi-ooxml).
//  Holds no arithmetic: every figure comes from DocxReportFigures. This class
//  only decides where the numbers sit on the page.
public final class WriteDocxReport {

    private WriteDocxReport() { }

    private static final String Font          = "Times New Roman";
    private static final String HeaderShading = "D9D9D9";

    //  Column headings of a member's delivery table
    private static final String[] Columns =
            { "Slip", "Week", "Produce", "Mass (kg)", "Grade", "Net payable (MUR)" };

    public static void write(DocxReportFigures figures, Path target) throws IOException {

        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }

        try (XWPFDocument document = new XWPFDocument();
             OutputStream out = Files.newOutputStream(target)) {

            writeCover(document, figures);

            for (Member member : figures.members()) {
                writeMemberSection(document, figures, member);
            }

            writeClosingSection(document, figures);

            document.write(out);
        }
    }

    //  Cover
    private static void writeCover(XWPFDocument document, DocxReportFigures figures) {
        heading(document, "REKOLT PLANTERS' COOPERATIVE", 20, ParagraphAlignment.CENTER);
        heading(document, "Season 2026 payment statements", 14, ParagraphAlignment.CENTER);

        line(document, "Generated " + figures.generatedAtStamp(), ParagraphAlignment.CENTER);
        line(document, figures.memberCount() + " members, "
                + figures.deliveryCount() + " deliveries, "
                + figures.rejectedCount() + " rejected, "
                + Format.kg(figures.seasonMassKg()) + " kg collected", ParagraphAlignment.CENTER);

        line(document, "", ParagraphAlignment.LEFT);
        line(document, "Each member's statement begins on its own page. The closing section "
                + "totals the season and reconciles with the member figures.", ParagraphAlignment.LEFT);
    }

    //  One member per page
    private static void writeMemberSection(XWPFDocument document, DocxReportFigures figures, Member member) {

//      The page break sits on the FIRST paragraph of the new section, not the
//      last of the previous one, so removing a member never leaves an orphan break
        XWPFParagraph title = document.createParagraph();
        title.setPageBreak(true);
        XWPFRun titleRun = title.createRun();
        titleRun.setText(member.getMemberId() + "   " + member.getName());
        titleRun.setBold(true);
        titleRun.setFontSize(15);
        titleRun.setFontFamily(Font);

        line(document, member.deliveryCount() + " deliveries, "
                + Format.kg(member.totalMassKg()) + " kg", ParagraphAlignment.LEFT);

//      The delivery table
        List<Delivery> deliveries = figures.deliveriesOf(member);

        XWPFTable table = document.createTable(deliveries.size() + 1, Columns.length);
        table.setWidth("100%");

        XWPFTableRow headerRow = table.getRow(0);
        for (int column = 0; column < Columns.length; column++) {
            XWPFTableCell cell = headerRow.getCell(column);
            cell.setColor(HeaderShading);
            cellText(cell, Columns[column], true,
                    column >= 3 ? ParagraphAlignment.RIGHT : ParagraphAlignment.LEFT);
        }

        int rowIndex = 1;
        for (Delivery delivery : deliveries) {
            XWPFTableRow row = table.getRow(rowIndex);
            cellText(row.getCell(0), delivery.getDeliveryId(),               false, ParagraphAlignment.LEFT);
            cellText(row.getCell(1), String.valueOf(delivery.getWeek()),     false, ParagraphAlignment.LEFT);
            cellText(row.getCell(2), delivery.getProduceCode(),              false, ParagraphAlignment.LEFT);
            cellText(row.getCell(3), Format.kg(delivery.getMassKg()),        false, ParagraphAlignment.RIGHT);
            cellText(row.getCell(4), delivery.getGrade().toString(),         false, ParagraphAlignment.RIGHT);
            cellText(row.getCell(5), Format.money(delivery.netPayable()),    false, ParagraphAlignment.RIGHT);
            rowIndex++;
        }

//      The deductions and the net
        line(document, "", ParagraphAlignment.LEFT);
        line(document, "Value after grade and category" + tab()
                + Format.money(figures.grossOf(member)), ParagraphAlignment.LEFT);
        line(document, "Less commission (" + figures.commissionPercentage() + "%)" + tab()
                + "- " + Format.money(figures.commissionOf(member)), ParagraphAlignment.LEFT);
        line(document, "Less transport levy (" + Format.money(figures.levyPerKg()) + " MUR/kg)" + tab()
                + "- " + Format.money(figures.levyOf(member)), ParagraphAlignment.LEFT);

//      The net payable in bold
        XWPFParagraph netParagraph = document.createParagraph();
        XWPFRun netRun = netParagraph.createRun();
        netRun.setText("NET PAYABLE" + tab() + Format.money(figures.netOf(member)) + " MUR");
        netRun.setBold(true);
        netRun.setFontSize(12);
        netRun.setFontFamily(Font);

//      The signature line
        line(document, "", ParagraphAlignment.LEFT);
        line(document, "", ParagraphAlignment.LEFT);
        line(document, "Received in full: ......................................"
                + "        Date: ........................", ParagraphAlignment.LEFT);
        line(document, member.getName() + "  (" + member.getMemberId() + ")", ParagraphAlignment.LEFT);
    }

    //  Closing totals
    private static void writeClosingSection(XWPFDocument document, DocxReportFigures figures) {

        XWPFParagraph title = document.createParagraph();
        title.setPageBreak(true);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("SEASON TOTALS");
        titleRun.setBold(true);
        titleRun.setFontSize(15);
        titleRun.setFontFamily(Font);

        String[] headings = { "Member", "Name", "Deliveries", "Mass (kg)", "Net payable (MUR)" };

        XWPFTable table = document.createTable(figures.memberCount() + 2, headings.length);
        table.setWidth("100%");

        XWPFTableRow headerRow = table.getRow(0);
        for (int column = 0; column < headings.length; column++) {
            XWPFTableCell cell = headerRow.getCell(column);
            cell.setColor(HeaderShading);
            cellText(cell, headings[column], true,
                    column >= 2 ? ParagraphAlignment.RIGHT : ParagraphAlignment.LEFT);
        }

        int rowIndex = 1;
        for (Member member : figures.members()) {
            XWPFTableRow row = table.getRow(rowIndex);
            cellText(row.getCell(0), member.getMemberId(), false, ParagraphAlignment.LEFT);
            cellText(row.getCell(1), member.getName(),     false, ParagraphAlignment.LEFT);
            cellText(row.getCell(2), String.valueOf(member.deliveryCount()), false, ParagraphAlignment.RIGHT);
            cellText(row.getCell(3), Format.kg(member.totalMassKg()),        false, ParagraphAlignment.RIGHT);
            cellText(row.getCell(4), Format.money(figures.netOf(member)),    false, ParagraphAlignment.RIGHT);
            rowIndex++;
        }

        XWPFTableRow totalRow = table.getRow(rowIndex);
        cellText(totalRow.getCell(0), "TOTAL", true, ParagraphAlignment.LEFT);
        cellText(totalRow.getCell(1), figures.memberCount() + " members", true, ParagraphAlignment.LEFT);
        cellText(totalRow.getCell(2), String.valueOf(figures.deliveryCount()), true, ParagraphAlignment.RIGHT);
        cellText(totalRow.getCell(3), Format.kg(figures.seasonMassKg()),       true, ParagraphAlignment.RIGHT);
        cellText(totalRow.getCell(4), Format.money(figures.seasonNet()),       true, ParagraphAlignment.RIGHT);

        line(document, "", ParagraphAlignment.LEFT);
        line(document, "Value after grade and category" + tab()
                + Format.money(figures.seasonGross()), ParagraphAlignment.LEFT);
        line(document, "Less commission (" + figures.commissionPercentage() + "%)" + tab()
                + "- " + Format.money(figures.seasonCommission()), ParagraphAlignment.LEFT);
        line(document, "Less transport levy" + tab()
                + "- " + Format.money(figures.seasonLevy()), ParagraphAlignment.LEFT);

//      Only printed when rounding actually left a residue
        if (!RoundMoney.sameToTheCent(figures.roundingAdjustment(), 0.0)) {
            line(document, "Rounding adjustment" + tab()
                    + Format.money(figures.roundingAdjustment()), ParagraphAlignment.LEFT);
        }

        XWPFParagraph netParagraph = document.createParagraph();
        XWPFRun netRun = netParagraph.createRun();
        netRun.setText("SEASON NET PAYABLE" + tab() + Format.money(figures.seasonNet()) + " MUR");
        netRun.setBold(true);
        netRun.setFontSize(12);
        netRun.setFontFamily(Font);

        line(document, "", ParagraphAlignment.LEFT);
        line(document, figures.reconciles()
                ? "Reconciled: the member statements above add up to this total exactly."
                : "WARNING: the member statements differ from this total by "
                  + Format.money(figures.reconciliationGap()) + " MUR.", ParagraphAlignment.LEFT);
    }

    //  Two tabs, to push a figure to the right of its caption
    private static String tab() {
        return "\t\t";
    }

    private static void heading(XWPFDocument document, String text, int size, ParagraphAlignment align) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(align);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(size);
        run.setFontFamily(Font);
    }

    private static void line(XWPFDocument document, String text, ParagraphAlignment align) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(align);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(11);
        run.setFontFamily(Font);
    }

    //  A new cell already holds one empty paragraph, so this writes into that one.
//  addParagraph() would leave a blank line above every value.
    private static void cellText(XWPFTableCell cell, String text, boolean bold, ParagraphAlignment align) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(align);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(10);
        run.setFontFamily(Font);
    }
}