package com.medilens.document;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    private static final float RENDER_DPI = 200f; // High quality DPI for clinical OCR

    public record RenderedPage(int pageNumber, byte[] imageBytes, int width, int height) {}

    public List<RenderedPage> processDocumentToPages(Path filePath, String mimeType) throws IOException {
        if ("application/pdf".equalsIgnoreCase(mimeType)) {
            return processPdf(filePath.toFile());
        } else if ("image/png".equalsIgnoreCase(mimeType) || "image/jpeg".equalsIgnoreCase(mimeType)) {
            return processSingleImage(filePath);
        } else {
            throw new IllegalArgumentException("Unsupported MIME type for document processing: " + mimeType);
        }
    }

    private List<RenderedPage> processPdf(File pdfFile) throws IOException {
        List<RenderedPage> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            logger.info("Rendering {} PDF pages at {} DPI from {}", pageCount, RENDER_DPI, pdfFile.getName());

            for (int pageIdx = 0; pageIdx < pageCount; pageIdx++) {
                BufferedImage bImage = renderer.renderImageWithDPI(pageIdx, RENDER_DPI, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bImage, "PNG", baos);
                byte[] bytes = baos.toByteArray();

                pages.add(new RenderedPage(
                        pageIdx + 1,
                        bytes,
                        bImage.getWidth(),
                        bImage.getHeight()
                ));
            }
        }
        return pages;
    }

    private List<RenderedPage> processSingleImage(Path imagePath) throws IOException {
        List<RenderedPage> pages = new ArrayList<>();
        byte[] bytes = Files.readAllBytes(imagePath);
        BufferedImage bImage = ImageIO.read(imagePath.toFile());
        int width = bImage != null ? bImage.getWidth() : 0;
        int height = bImage != null ? bImage.getHeight() : 0;

        pages.add(new RenderedPage(1, bytes, width, height));
        return pages;
    }
}
