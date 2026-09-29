"""
MediLens AI - Local In-Memory Vector Store & Similarity Ranker
Computes cosine similarity across clinical evidence chunks using TF-IDF term weighting
and biomarker exact-match boosting. Ensures deterministic, zero-external-dependency retrieval.
"""

import math
import re
from typing import List, Dict, Any, Tuple, Optional
import numpy as np

from app.rag.knowledge_catalog import CLINICAL_KNOWLEDGE_BASE


def _tokenize(text: str) -> List[str]:
    """Basic alphanumeric word tokenizer with lowercasing."""
    return re.findall(r"\b[a-zA-Z0-9_\-\+]+\b", text.lower())


class ClinicalVectorStore:
    def __init__(self, chunks: Optional[List[Dict[str, Any]]] = None):
        self.chunks = chunks if chunks is not None else list(CLINICAL_KNOWLEDGE_BASE)
        self.vocabulary: Dict[str, int] = {}
        self.idf: np.ndarray = np.array([])
        self.chunk_vectors: np.ndarray = np.array([])
        self._build_index()

    def _build_index(self):
        """Constructs vocabulary and TF-IDF vectors for all knowledge chunks."""
        doc_tokens_list: List[List[str]] = []
        df_counts: Dict[str, int] = {}

        for chunk in self.chunks:
            # Composite representation: title, summary, significance, lifestyle, tags, biomarkers
            composite_text = " ".join([
                chunk.get("title", ""),
                chunk.get("summary", ""),
                chunk.get("clinical_significance", ""),
                chunk.get("lifestyle_and_followup", ""),
                " ".join(chunk.get("biomarkers", [])),
                " ".join(chunk.get("tags", [])),
            ])
            tokens = _tokenize(composite_text)
            doc_tokens_list.append(tokens)

            unique_terms = set(tokens)
            for term in unique_terms:
                df_counts[term] = df_counts.get(term, 0) + 1

        # Build sorted vocabulary
        self.vocabulary = {term: idx for idx, term in enumerate(sorted(df_counts.keys()))}
        num_docs = len(self.chunks)
        vocab_size = len(self.vocabulary)

        if vocab_size == 0 or num_docs == 0:
            self.idf = np.zeros(0)
            self.chunk_vectors = np.zeros((num_docs, 0))
            return

        # Calculate smooth IDF: log((N + 1) / (df + 1)) + 1
        self.idf = np.zeros(vocab_size)
        for term, idx in self.vocabulary.items():
            df = df_counts.get(term, 0)
            self.idf[idx] = math.log((num_docs + 1.0) / (df + 1.0)) + 1.0

        # Build document TF-IDF matrix
        vectors = np.zeros((num_docs, vocab_size))
        for doc_idx, tokens in enumerate(doc_tokens_list):
            tf_counts: Dict[int, int] = {}
            for t in tokens:
                if t in self.vocabulary:
                    t_idx = self.vocabulary[t]
                    tf_counts[t_idx] = tf_counts.get(t_idx, 0) + 1

            doc_len = max(len(tokens), 1)
            for t_idx, count in tf_counts.items():
                tf = count / doc_len
                vectors[doc_idx, t_idx] = tf * self.idf[t_idx]

            # Normalize to unit vector for cosine distance
            norm = np.linalg.norm(vectors[doc_idx])
            if norm > 1e-8:
                vectors[doc_idx] /= norm

        self.chunk_vectors = vectors

    def _vectorize_query(self, query: str) -> np.ndarray:
        """Projects raw query text into normalized TF-IDF feature space."""
        tokens = _tokenize(query)
        vocab_size = len(self.vocabulary)
        vec = np.zeros(vocab_size)
        if vocab_size == 0 or not tokens:
            return vec

        query_len = len(tokens)
        for t in tokens:
            if t in self.vocabulary:
                t_idx = self.vocabulary[t]
                vec[t_idx] += 1.0

        for t_idx in range(vocab_size):
            if vec[t_idx] > 0:
                tf = vec[t_idx] / query_len
                vec[t_idx] = tf * self.idf[t_idx]

        norm = np.linalg.norm(vec)
        if norm > 1e-8:
            vec /= norm
        return vec

    def search(
        self, query: str, top_k: int = 3, category: Optional[str] = None
    ) -> List[Tuple[Dict[str, Any], float]]:
        """
        Retrieves top_k relevant evidence chunks ranked by cosine similarity
        with exact biomarker boost.
        """
        if len(self.chunks) == 0:
            return []

        q_vec = self._vectorize_query(query)
        q_norm = np.linalg.norm(q_vec)

        query_lower = query.lower()
        query_tokens = set(_tokenize(query))

        scores: List[Tuple[Dict[str, Any], float]] = []

        for idx, chunk in enumerate(self.chunks):
            if category and chunk.get("category") != category:
                continue

            # Cosine similarity
            base_score = 0.0
            if q_norm > 1e-8:
                base_score = float(np.dot(self.chunk_vectors[idx], q_vec))

            # Biomarker exact match boosting:
            boost = 0.0
            chunk_biomarkers = [b.lower() for b in chunk.get("biomarkers", [])]
            for b in chunk_biomarkers:
                if b in query_lower:
                    boost += 0.5
                    break
                # Check token intersection
                b_tokens = set(_tokenize(b))
                if b_tokens and b_tokens.issubset(query_tokens):
                    boost += 0.3
                    break

            total_score = base_score + boost
            if total_score > 0.05:
                scores.append((chunk, total_score))

        # Sort descending by score
        scores.sort(key=lambda x: x[1], reverse=True)
        return scores[:top_k]

    def search_for_biomarker(
        self, biomarker_name: str, status: Optional[str] = None, top_k: int = 2
    ) -> List[Tuple[Dict[str, Any], float]]:
        """Targeted retrieval using biomarker name and clinical status keywords."""
        query_parts = [biomarker_name]
        if status:
            if status == "HIGH":
                query_parts.extend(["elevated", "high", "hyper"])
            elif status == "LOW":
                query_parts.extend(["decreased", "low", "deficiency", "hypo"])
            elif status == "CRITICAL":
                query_parts.extend(["critical", "emergency", "severe", "life-threatening"])

        query = " ".join(query_parts)
        return self.search(query=query, top_k=top_k)
