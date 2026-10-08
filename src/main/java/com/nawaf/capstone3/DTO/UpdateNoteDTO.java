package com.nawaf.capstone3.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateNoteDTO(@NotNull @Size(max = 1000) String note) {
}
