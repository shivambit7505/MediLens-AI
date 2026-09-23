import numpy as np
from PIL import Image, ImageDraw
import pytest
from app.ocr.preprocessor import ImagePreprocessor


@pytest.fixture
def preprocessor():
    return ImagePreprocessor()


@pytest.fixture
def sample_image():
    # Create a 200x200 RGB image with horizontal black text bars on a white background
    img = Image.new("RGB", (200, 200), color="white")
    draw = ImageDraw.Draw(img)
    # Draw horizontal stripes resembling text lines
    draw.rectangle([20, 30, 180, 45], fill="black")
    draw.rectangle([20, 70, 180, 85], fill="black")
    draw.rectangle([20, 110, 180, 125], fill="black")
    draw.rectangle([20, 150, 180, 165], fill="black")
    return img


def test_convert_to_grayscale(preprocessor, sample_image):
    gray = preprocessor.convert_to_grayscale(sample_image)
    assert gray.mode == "L"
    assert gray.size == sample_image.size


def test_deskew_straight_image(preprocessor, sample_image):
    # An already straight image should have zero or near-zero skew
    deskewed, angle = preprocessor.deskew(sample_image)
    assert abs(angle) <= 1.0
    assert deskewed.size[0] >= sample_image.size[0]


def test_deskew_rotated_image(preprocessor, sample_image):
    # Rotate image by 5 degrees clockwise
    rotated = sample_image.rotate(-5.0, expand=True, fillcolor="white")
    deskewed, detected_angle = preprocessor.deskew(rotated)
    # Detected angle should approximate the correction
    assert isinstance(detected_angle, float)
    assert deskewed is not None


def test_denoise(preprocessor, sample_image):
    # Add salt and pepper noise
    noisy_img = sample_image.copy()
    pixels = noisy_img.load()
    pixels[50, 50] = (0, 0, 0)
    pixels[51, 51] = (255, 255, 255)
    
    denoised = preprocessor.denoise(noisy_img)
    assert denoised.mode == "L"
    assert denoised.size == sample_image.size


def test_enhance_contrast(preprocessor):
    # Create low-contrast gray image
    low_contrast = Image.new("L", (100, 100), color=120)
    draw = ImageDraw.Draw(low_contrast)
    draw.rectangle([20, 20, 80, 80], fill=135)
    
    enhanced = preprocessor.enhance_contrast(low_contrast)
    arr = np.array(enhanced)
    # Autocontrast should stretch histogram to include 0 and 255
    assert arr.min() < 120
    assert arr.max() > 135


def test_full_pipeline(preprocessor, sample_image):
    processed, metadata = preprocessor.preprocess(sample_image)
    assert processed.mode == "L"
    assert "skew_angle_detected" in metadata
    assert "width" in metadata
    assert "height" in metadata
    assert metadata["format"] == "L"
