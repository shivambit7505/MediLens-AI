"""
Tests for ClinicalVectorStore in MediLens AI.
Verifies TF-IDF indexing, cosine ranking, and biomarker boosting.
"""

import pytest
from app.rag.vector_store import ClinicalVectorStore


def test_vector_store_initialization():
    store = ClinicalVectorStore()
    assert len(store.chunks) > 0
    assert len(store.vocabulary) > 0
    assert store.chunk_vectors.shape[0] == len(store.chunks)
    assert store.chunk_vectors.shape[1] == len(store.vocabulary)


def test_search_glucose_retrieves_ada():
    store = ClinicalVectorStore()
    results = store.search_for_biomarker(biomarker_name="glucose", status="HIGH", top_k=2)
    assert len(results) > 0
    top_chunk, score = results[0]
    assert "ADA-2024-GLUCOSE" == top_chunk["chunk_id"]
    assert score > 0.3


def test_search_potassium_retrieves_electrolyte_guideline():
    store = ClinicalVectorStore()
    results = store.search_for_biomarker(biomarker_name="potassium", status="CRITICAL", top_k=2)
    assert len(results) > 0
    top_chunk, score = results[0]
    assert "MAYO-ELECTROLYTES-POTASSIUM" == top_chunk["chunk_id"]


def test_search_creatinine_retrieves_kdigo():
    store = ClinicalVectorStore()
    results = store.search_for_biomarker(biomarker_name="creatinine", status="HIGH", top_k=2)
    assert len(results) > 0
    top_chunk, score = results[0]
    assert "KDIGO-2024-RENAL" == top_chunk["chunk_id"]


def test_search_category_filter():
    store = ClinicalVectorStore()
    results = store.search(query="cholesterol triglycerides", top_k=3, category="LIPID")
    assert len(results) > 0
    for chunk, _ in results:
        assert chunk["category"] == "LIPID"
