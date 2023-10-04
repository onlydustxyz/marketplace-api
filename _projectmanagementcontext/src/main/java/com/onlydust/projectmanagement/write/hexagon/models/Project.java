package com.onlydust.projectmanagement.write.hexagon.models;

import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@ToString
public class Project {

        private final UUID id;
        private final String name;
        public Project(UUID id, String name) {
                this.id = id;
                this.name = name;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Project project = (Project) o;
                return Objects.equals(id, project.id) && Objects.equals(name, project.name);
        }

        @Override
        public int hashCode() {
                return Objects.hash(id, name);
        }
}
