package onlydust.com.marketplace.api.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HackathonHelper {
    private final Faker faker = new Faker();
    @Autowired
    private DatabaseHelper databaseHelper;

    @SneakyThrows
    public Hackathon.Id createHackathon(Hackathon.Status status, List<String> labels, List<ProjectId> projectIds) {
        final UUID id = UUID.randomUUID();
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("slug", faker.rickAndMorty().character());
        parameters.put("title", faker.rickAndMorty().location());
        parameters.put("status", status.name());
        parameters.put("description", faker.lorem().sentence());
        parameters.put("location", faker.lorem().word());
        parameters.put("budget", faker.random().nextInt(0, 1000000));
        parameters.put("startDate", ZonedDateTime.now());
        parameters.put("endDate", ZonedDateTime.now());
        parameters.put("index", faker.random().nextInt(0, 1000000));
        parameters.put("githubLabels", "{" + String.join("\",", labels) + "}");
        parameters.put("links", new ObjectMapper().writeValueAsString(List.of(faker.internet().url())));

        databaseHelper.executeQuery(
                """
                        INSERT INTO public.hackathons (id, slug, status, title, description, location, budget, start_date, end_date, links, index, github_labels)
                        VALUES (:id, :slug , cast(:status as hackathon_status), :title, :description, :location, :budget, :startDate, :endDate, cast(:links as jsonb), :index, cast(:githubLabels as text[]));
                        """
                , parameters
        );

        for (ProjectId projectId : projectIds) {
            databaseHelper.executeQuery(
                    """
                            INSERT INTO public.hackathon_projects (hackathon_id, project_id)
                            VALUES (:hackathonId, :projectId);
                            """
                    , Map.of("hackathonId", id, "projectId", projectId.value()));
        }

        return Hackathon.Id.of(id);
    }
}
