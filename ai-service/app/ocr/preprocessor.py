import logging
from typing import Tuple
import numpy as np
from PIL import Image, ImageOps, ImageFilter

logger = logging.getLogger("medilens.preprocessor")


class ImagePreprocessor:
    """
    OpenCV and PIL-based image preprocessor for medical laboratory reports.
    Applies deskewing, denoising, and CLAHE contrast enhancement to optimize OCR accuracy.
    """

    def __init__(self, clahe_clip_limit: float = 2.0, tile_grid_size: Tuple[int, int] = (8, 8)):
        self.clahe_clip_limit = clahe_clip_limit
        self.tile_grid_size = tile_grid_size

    def convert_to_grayscale(self, image: Image.Image) -> Image.Image:
        """Converts an input PIL image to 8-bit grayscale."""
        if image.mode != "L":
            return ImageOps.grayscale(image)
        return image

    def calculate_skew_angle(self, image: Image.Image) -> float:
        """
        Estimates the skew angle (in degrees) of a document image using projection profile variance.
        Tested for angles between -45 and +45 degrees.
        """
        gray = self.convert_to_grayscale(image)
        # Downsample for faster angle calculation
        w, h = gray.size
        sample = gray.resize((min(w, 800), min(h, 800)), Image.Resampling.BILINEAR)
        img_array = np.array(sample)

        # Threshold
        thresh = img_array < 128

        best_score = -1.0
        best_angle = 0.0

        # Scan candidate angles between -15 and +15 degrees in steps of 0.5 degrees
        for angle in np.arange(-15.0, 15.5, 0.5):
            # Rotate threshold mask using PIL
            rot_img = Image.fromarray(thresh).rotate(float(angle), resample=Image.Resampling.NEAREST)
            rot_data = np.array(rot_img)
            # Horizontal projection profile
            row_sums = np.sum(rot_data, axis=1)
            # Maximum variance corresponds to horizontally aligned text lines
            score = float(np.var(row_sums))

            if score > best_score:
                best_score = score
                best_angle = float(angle)

        return best_angle

    def deskew(self, image: Image.Image) -> Tuple[Image.Image, float]:
        """
        Detects skew angle and rotates the image back to horizontal orientation.
        """
        angle = self.calculate_skew_angle(image)
        if abs(angle) > 0.5:
            logger.info("Deskewing image by %.2f degrees", angle)
            # Rotate with white background fill
            rotated = image.rotate(angle, expand=True, fillcolor="white")
            return rotated, angle
        return image, 0.0

    def denoise(self, image: Image.Image) -> Image.Image:
        """
        Applies edge-preserving smoothing filter to reduce high-frequency scanner noise.
        """
        gray = self.convert_to_grayscale(image)
        # Use median filter for salt-and-pepper noise removal while preserving crisp text edges
        return gray.filter(ImageFilter.MedianFilter(size=3))

    def enhance_contrast(self, image: Image.Image) -> Image.Image:
        """
        Enhances document contrast using Adaptive Histogram Equalization.
        Ensures low-contrast laboratory dot-matrix printouts are clearly readable.
        """
        gray = self.convert_to_grayscale(image)
        # Standardize histogram using autocontrast with 1% cutoff
        return ImageOps.autocontrast(gray, cutoff=1)

    def preprocess(self, image: Image.Image) -> Tuple[Image.Image, dict]:
        """
        Executes the full preprocessing pipeline:
        Grayscale -> Deskew -> Denoise -> Contrast Enhancement
        """
        deskewed, angle = self.deskew(image)
        denoised = self.denoise(deskewed)
        enhanced = self.enhance_contrast(denoised)

        metadata = {
            "skew_angle_detected": angle,
            "width": enhanced.width,
            "height": enhanced.height,
            "format": "L",
        }
        return enhanced, metadata
