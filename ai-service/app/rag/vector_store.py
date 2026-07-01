"""FAISS vector store for document embeddings."""
import os
import pickle
import numpy as np
from typing import List, Optional, Tuple
from app.config import VECTOR_STORE_PATH
from app.utils.logger import logger


class VectorStore:
    """FAISS-based vector store with persistence."""

    def __init__(self, dim: int = 1024):
        self.dim = dim
        self.index = None
        self.chunks: List[dict] = []  # [{id, doc_id, chunk_index, content}]
        self._load()

    def add(self, embeddings: List[List[float]], chunks: List[dict]) -> bool:
        """Add embeddings and their corresponding chunks to the store."""
        if not embeddings or not chunks:
            return False
        if len(embeddings) != len(chunks):
            logger.error("Embeddings and chunks length mismatch")
            return False

        try:
            import faiss
        except ImportError:
            logger.warning("FAISS not installed, using numpy fallback")
            return self._add_numpy(embeddings, chunks)

        try:
            vectors = np.array(embeddings).astype('float32')

            if self.index is None:
                self.index = faiss.IndexFlatL2(self.dim)
                self.index.add(vectors)
            else:
                self.index.add(vectors)

            for chunk in chunks:
                self.chunks.append(chunk)

            self._save()
            logger.info(f"Added {len(embeddings)} vectors to store (total: {len(self.chunks)})")
            return True
        except Exception as e:
            logger.error(f"FAISS add failed: {e}")
            return False

    def search(self, query_embedding: List[float], top_k: int = 5) -> List[Tuple[dict, float]]:
        """Search for similar chunks given a query embedding.

        Returns list of (chunk, score) tuples.
        """
        if not self.chunks:
            return []

        try:
            import faiss
        except ImportError:
            return self._search_numpy(query_embedding, top_k)

        try:
            query = np.array([query_embedding]).astype('float32')
            distances, indices = self.index.search(query, min(top_k, len(self.chunks)))

            results = []
            for i, idx in enumerate(indices[0]):
                if idx < len(self.chunks):
                    results.append((self.chunks[idx], float(distances[0][i])))
            return results
        except Exception as e:
            logger.error(f"FAISS search failed: {e}")
            return []

    def _add_numpy(self, embeddings: List[List[float]], chunks: List[dict]) -> bool:
        """Fallback using numpy when FAISS is not available."""
        for emb, chunk in zip(embeddings, chunks):
            self.chunks.append({**chunk, "_embedding": emb})
        self._save()
        return True

    def _search_numpy(self, query_embedding: List[float], top_k: int = 5) -> List[Tuple[dict, float]]:
        """Fallback search using cosine similarity with numpy."""
        query = np.array(query_embedding)
        scored = []
        for chunk in self.chunks:
            emb = chunk.get("_embedding")
            if emb:
                vec = np.array(emb)
                cos_sim = np.dot(query, vec) / (np.linalg.norm(query) * np.linalg.norm(vec))
                scored.append((chunk, float(cos_sim)))

        scored.sort(key=lambda x: x[1], reverse=True)
        return scored[:top_k]

    def remove_doc(self, doc_id: int):
        """Remove all chunks belonging to a document."""
        self.chunks = [c for c in self.chunks if c.get("doc_id") != doc_id]
        self._rebuild_index()
        self._save()

    def clear(self):
        """Clear all vectors."""
        self.chunks = []
        self.index = None
        self._save()

    def _rebuild_index(self):
        """Rebuild FAISS index from remaining chunks."""
        try:
            import faiss
            embeddings = [c.get("_embedding") for c in self.chunks if c.get("_embedding")]
            if embeddings:
                self.index = faiss.IndexFlatL2(self.dim)
                self.index.add(np.array(embeddings).astype('float32'))
            else:
                self.index = None
        except ImportError:
            pass

    def _save(self):
        """Persist to disk."""
        os.makedirs(VECTOR_STORE_PATH, exist_ok=True)
        try:
            # Remove embeddings from chunks for metadata save
            meta_chunks = []
            embeddings_data = []
            for c in self.chunks:
                meta = {k: v for k, v in c.items() if k != "_embedding"}
                meta_chunks.append(meta)
                if "_embedding" in c:
                    embeddings_data.append(c["_embedding"])

            if embeddings_data:
                np.save(os.path.join(VECTOR_STORE_PATH, "embeddings.npy"),
                        np.array(embeddings_data).astype('float32'))

            with open(os.path.join(VECTOR_STORE_PATH, "chunks.pkl"), 'wb') as f:
                pickle.dump(meta_chunks, f)

            if self.index is not None:
                import faiss
                faiss.write_index(self.index,
                                  os.path.join(VECTOR_STORE_PATH, "index.faiss"))
        except Exception as e:
            logger.error(f"Failed to save vector store: {e}")

    def _load(self):
        """Load from disk."""
        try:
            import faiss
            index_path = os.path.join(VECTOR_STORE_PATH, "index.faiss")
            if os.path.exists(index_path):
                self.index = faiss.read_index(index_path)
        except ImportError:
            pass
        except Exception as e:
            logger.warning(f"Failed to load FAISS index: {e}")

        try:
            chunks_path = os.path.join(VECTOR_STORE_PATH, "chunks.pkl")
            if os.path.exists(chunks_path):
                with open(chunks_path, 'rb') as f:
                    meta_chunks = pickle.load(f)

                embeddings_path = os.path.join(VECTOR_STORE_PATH, "embeddings.npy")
                if os.path.exists(embeddings_path):
                    embeddings = np.load(embeddings_path)
                    self.chunks = []
                    for i, meta in enumerate(meta_chunks):
                        if i < len(embeddings):
                            self.chunks.append({**meta, "_embedding": embeddings[i].tolist()})
                        else:
                            self.chunks.append(meta)
                else:
                    self.chunks = meta_chunks

                logger.info(f"Loaded {len(self.chunks)} chunks from disk")
        except Exception as e:
            logger.warning(f"Failed to load chunks: {e}")
            self.chunks = []


# Global singleton
_store: Optional[VectorStore] = None


def get_store() -> VectorStore:
    global _store
    if _store is None:
        _store = VectorStore()
    return _store
