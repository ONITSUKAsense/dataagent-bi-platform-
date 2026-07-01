"""Text splitting for RAG document chunking."""
from typing import List


def split_text(text: str, chunk_size: int = 500, chunk_overlap: int = 50) -> List[str]:
    """Split text into overlapping chunks by paragraphs.

    Args:
        text: Input text content
        chunk_size: Target characters per chunk
        chunk_overlap: Overlap characters between chunks

    Returns:
        List of text chunks
    """
    if not text:
        return []

    # Split by paragraphs first
    paragraphs = [p.strip() for p in text.split('\n') if p.strip()]
    chunks = []
    current_chunk = []
    current_size = 0

    for para in paragraphs:
        para_size = len(para)

        # If single paragraph exceeds chunk_size, split by sentences
        if para_size > chunk_size:
            if current_chunk:
                chunks.append('\n'.join(current_chunk))
                current_chunk = []
                current_size = 0

            sentences = _split_sentences(para)
            for sentence in sentences:
                s_len = len(sentence)
                if current_size + s_len > chunk_size and current_chunk:
                    chunks.append('\n'.join(current_chunk))
                    # Keep overlap: take last few sentences
                    overlap_text = '\n'.join(current_chunk)
                    overlap_chars = overlap_text[-chunk_overlap:] if len(overlap_text) > chunk_overlap else overlap_text
                    current_chunk = [overlap_chars] if overlap_chars else []
                    current_size = len(overlap_chars)
                current_chunk.append(sentence)
                current_size += s_len
        else:
            if current_size + para_size > chunk_size and current_chunk:
                chunks.append('\n'.join(current_chunk))
                # Keep overlap
                overlap_text = '\n'.join(current_chunk)
                overlap_chars = overlap_text[-chunk_overlap:] if len(overlap_text) > chunk_overlap else overlap_text
                current_chunk = [overlap_chars] if overlap_chars else []
                current_size = len(overlap_chars)
            current_chunk.append(para)
            current_size += para_size

    if current_chunk:
        chunks.append('\n'.join(current_chunk))

    return chunks


def _split_sentences(text: str) -> List[str]:
    """Split text into sentences."""
    import re
    sentences = re.split(r'(?<=[。！？.!?])\s*', text)
    return [s.strip() for s in sentences if s.strip()]
