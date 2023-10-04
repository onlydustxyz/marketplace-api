package com.onlydust.projectmanagement.read.usecases.projectretrieval;


import com.onlydust.projectmanagement.read.adapters.secondary.queries.ProjectQuery;

public class RetrieveProjectQueryHandler {

    private final ProjectQuery projectQuery;

    public RetrieveProjectQueryHandler(ProjectQuery projectQuery) {
        this.projectQuery = projectQuery;
    }

     ProjectPageVM retrieveAll() {
        return projectQuery.all();
    }

}
