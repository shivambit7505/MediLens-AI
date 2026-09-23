from typing import List
from app.ocr.models import OcrLine, OcrToken


class SpatialAnalyzer:
    """
    Groups and aligns OCR tokens into coherent horizontal tabular rows
    based on bounding box geometry.
    """

    def __init__(self, vertical_tolerance_px: int = 15):
        self.vertical_tolerance_px = vertical_tolerance_px

    def cluster_tokens_into_rows(self, tokens: List[OcrToken]) -> List[List[OcrToken]]:
        """
        Clusters tokens whose vertical Y centers fall within `vertical_tolerance_px`.
        Sorts rows from top to bottom, and tokens within each row from left to right.
        """
        if not tokens:
            return []

        # Filter out tokens without bounding boxes
        valid_tokens = [t for t in tokens if t.bounding_box is not None]
        if not valid_tokens:
            return []

        # Sort tokens primarily by Y, secondarily by X
        sorted_tokens = sorted(valid_tokens, key=lambda t: (t.bounding_box.y, t.bounding_box.x))

        rows: List[List[OcrToken]] = []
        for token in sorted_tokens:
            token_y_mid = token.bounding_box.y + (token.bounding_box.height / 2.0)

            # Find matching row
            placed = False
            for row in rows:
                first_token = row[0]
                row_y_mid = first_token.bounding_box.y + (first_token.bounding_box.height / 2.0)
                if abs(token_y_mid - row_y_mid) <= self.vertical_tolerance_px:
                    row.append(token)
                    placed = True
                    break

            if not placed:
                rows.append([token])

        # Sort tokens within each row left to right
        for row in rows:
            row.sort(key=lambda t: t.bounding_box.x)

        return rows

    def parse_plain_text_lines(self, raw_text: str) -> List[str]:
        """
        Splits raw OCR text into non-empty stripped lines for line-by-line parsing.
        """
        lines = [line.strip() for line in raw_text.splitlines() if line.strip()]
        return lines
