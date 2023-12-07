package openstack.eco_stack.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Document(collection = "Hypervisor")
public class Hypervisor {

    @Id
    private String id;
    private String name;
    @Builder.Default
    private Set<String> cloudInstanceIds = new HashSet<>();

    public void addToCloudInstanceIds(String cloudInstanceId) {
        this.cloudInstanceIds.add(cloudInstanceId);
    }
}
