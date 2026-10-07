package com.nawaf.capstone3.DTO;

public record PdfChunk(int firstPage, int lastPage, byte[] data) {
}