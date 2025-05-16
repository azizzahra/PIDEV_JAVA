package services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import model.Owner;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CertificateService {

    public void generateCertificate(Owner owner) throws IOException {
        // 1. Create document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // 2. Prepare content
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Page settings
            PDRectangle pageSize = page.getMediaBox();
            float pageWidth = pageSize.getWidth();
            float yPosition = 700; // Start from top

            // 3. Add title (centered)
            addCenteredText(contentStream, "Certificate of Completion",
                    PDType1Font.HELVETICA_BOLD, 24, pageWidth, yPosition);
            yPosition -= 50;

            // 4. Add subtitle
            addCenteredText(contentStream, "This certifies that:",
                    PDType1Font.HELVETICA, 18, pageWidth, yPosition);
            yPosition -= 40;

            // 5. Add owner name (highlighted)
            contentStream.setNonStrokingColor(new Color(59, 89, 152)); // Blue color
            addCenteredText(contentStream, owner.getName(),
                    PDType1Font.HELVETICA_BOLD, 28, pageWidth, yPosition);
            contentStream.setNonStrokingColor(Color.BLACK); // Reset color
            yPosition -= 50;

            // 6. Add formation details
            addCenteredText(contentStream, "has successfully completed",
                    PDType1Font.HELVETICA, 16, pageWidth, yPosition);
            yPosition -= 30;

            addCenteredText(contentStream, owner.getFormationName(),
                    PDType1Font.HELVETICA_BOLD, 20, pageWidth, yPosition);
            yPosition -= 50;

            // 7. Add QR code
            try {
                BufferedImage qrImage = generateQRCodeImage(
                        "Owner: " + owner.getName() + "\n" +
                                "Course: " + owner.getFormationName() + "\n" +
                                "ID: " + owner.getId(),
                        200, 200);

                PDImageXObject pdQrImage = LosslessFactory.createFromImage(document, qrImage);
                float qrSize = 150;
                float qrX = (pageWidth - qrSize) / 2; // Center horizontally
                contentStream.drawImage(pdQrImage, qrX, yPosition - qrSize, qrSize, qrSize);
                yPosition -= (qrSize + 20);

                // QR code label
                addCenteredText(contentStream, "Scan to verify",
                        PDType1Font.HELVETICA_OBLIQUE, 10, pageWidth, yPosition);
                yPosition -= 30;
            } catch (WriterException e) {
                System.err.println("QR code error: " + e.getMessage());
            }

            // 8. Add signature line
            addCenteredText(contentStream, "_________________________",
                    PDType1Font.HELVETICA, 14, pageWidth, yPosition);
            yPosition -= 20;
            addCenteredText(contentStream, "Authorized Signature",
                    PDType1Font.HELVETICA_OBLIQUE, 12, pageWidth, yPosition);
        }

        // 9. Save and open
        File file = new File("Certificate_" + owner.getId() + ".pdf");
        document.save(file);
        document.close();
        openPDF(file);
    }

    private void addCenteredText(PDPageContentStream contentStream, String text,
                                 PDType1Font font, int fontSize,
                                 float pageWidth, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        contentStream.newLineAtOffset((pageWidth - textWidth) / 2, yPosition);
        contentStream.showText(text);
        contentStream.endText();
    }

    private BufferedImage generateQRCodeImage(String text, int width, int height)
            throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    private void openPDF(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                System.err.println("Error opening PDF: " + e.getMessage());
            }
        }
    }
}