package com.onlydust.projectmanagement.read.usecases.projectretrieval;

public record ProjectPageVM(int total, int page, int size, ProjectVM[] projects) {
}
