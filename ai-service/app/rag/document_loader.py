"""Document loader for PDF, Word, and Markdown files."""
import os
from typing import Optional
from app.utils.logger import logger


def load_document(file_path: str) -> Optional[str]:
    """Load text content from a document file.

    Supports: .pdf, .docx, .md, .txt
    """
    ext = os.path.splitext(file_path)[1].lower()

    try:
        if ext == '.pdf':
            return _load_pdf(file_path)
        elif ext == '.docx':
            return _load_docx(file_path)
        elif ext == '.md':
            return _load_text(file_path)
        elif ext == '.txt':
            return _load_text(file_path)
        else:
            logger.warning(f"Unsupported file type: {ext}")
            return None
    except Exception as e:
        logger.error(f"Failed to load document {file_path}: {e}")
        return None


def _load_pdf(file_path: str) -> str:
    """Extract text from PDF using PyMuPDF."""
    import fitz
    doc = fitz.open(file_path)
    texts = []
    for page in doc:
        texts.append(page.get_text())
    doc.close()
    return '\n'.join(texts)


def _load_docx(file_path: str) -> str:
    """Extract text from Word document."""
    from docx import Document
    doc = Document(file_path)
    return '\n'.join([p.text for p in doc.paragraphs if p.text.strip()])


def _load_text(file_path: str) -> str:
    """Read plain text file."""
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.read()
